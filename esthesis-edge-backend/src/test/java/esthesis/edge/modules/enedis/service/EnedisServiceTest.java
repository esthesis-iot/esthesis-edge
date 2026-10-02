package esthesis.edge.modules.enedis.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.common.agent.dto.AgentRegistrationRequest;
import esthesis.common.agent.dto.AgentRegistrationResponse;
import esthesis.common.exception.QProcessingException;
import esthesis.edge.TestUtils;
import esthesis.edge.clients.EsthesisAgentServiceClient;
import esthesis.edge.model.DeviceEntity;
import esthesis.edge.modules.enedis.client.EnedisClient;
import esthesis.edge.modules.enedis.config.EnedisConstants;
import esthesis.edge.modules.enedis.dto.datahub.EnedisAlimentationAutoDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisAuthTokenDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisDonneesGeneralesAutoDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisSituationContractAutoDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisSubscribedServicesRequestDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisSubscribedServicesResponseDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisSynthContractAutoDTO;
import esthesis.edge.services.DeviceService;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.quarkus.arc.ClientProxy;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.SneakyThrows;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@QuarkusTest
class EnedisServiceTest {

    @InjectMock
    @RestClient
    EnedisClient enedisRestClient;

    @Inject
    EnedisService enedisService;

    @InjectMock
    @RestClient
    EsthesisAgentServiceClient esthesisAgentServiceClient;

    @InjectMock
    EnedisFetchService enedisFetchService;

    @Inject
    TestUtils testUtils;

    @Inject
    DeviceService deviceService;

    @Inject
    ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        // Mock the esthesis CORE registration.
        when(esthesisAgentServiceClient.register(any(AgentRegistrationRequest.class)))
                .thenReturn(new AgentRegistrationResponse());
        // Mock the Enedis auth token, the service refreshes it before remote calls.
        EnedisAuthTokenDTO accessToken = new EnedisAuthTokenDTO();
        accessToken.setAccessToken("test");
        accessToken.setExpiresOn(1000);
        accessToken.setScope("test");
        accessToken.setTokenType("test");
        when(enedisRestClient.getAuthToken(any(String.class), any(String.class), any(String.class)))
                .thenReturn(accessToken);
    }

    @AfterEach
    public void restoreRateLimiters() {
        // The service is application scoped, restore the real limiters for the other tests.
        enedisService.setRateLimiters(EnedisService.createPerSecondLimiter(),
                EnedisService.createPerHourLimiter());
    }

    /**
     * A rate limiter that always grants or always denies a permit.
     */
    private static RateLimiter rateLimiter(boolean permitted) {
        RateLimiter rateLimiter = mock(RateLimiter.class);
        when(rateLimiter.acquirePermission()).thenReturn(permitted);
        // Report a permit as available, so a denial is decided by acquirePermission() only.
        RateLimiter.Metrics metrics = mock(RateLimiter.Metrics.class);
        when(metrics.getAvailablePermissions()).thenReturn(1);
        when(rateLimiter.getMetrics()).thenReturn(metrics);
        return rateLimiter;
    }

    /**
     * A real per-hour rate limiter with only the given number of permits left, which would make a
     * caller wait for 5 seconds (instead of the real 1 hour) for the next permit.
     */
    private static RateLimiter hourLimiterWithPermits(int permits) {
        RateLimiter rateLimiter = RateLimiter.of("limitedHourLimiter",
                RateLimiterConfig.custom()
                        .limitForPeriod(Math.max(permits, 1))
                        .limitRefreshPeriod(Duration.ofHours(1))
                        .timeoutDuration(Duration.ofSeconds(5))
                        .build());
        if (permits <= 0) {
            rateLimiter.drainPermissions();
        }
        return rateLimiter;
    }

    private static RateLimiter exhaustedHourLimiter() {
        return hourLimiterWithPermits(0);
    }

    /**
     * Replace the access token cached by the service, bypassing the client proxy.
     */
    @SneakyThrows
    private void setCachedAuthToken(EnedisAuthTokenDTO token) {
        Field field = EnedisService.class.getDeclaredField("enedisAuthTokenDTO");
        field.setAccessible(true);
        field.set(ClientProxy.unwrap(enedisService), token);
    }

    /**
     * Create an active Enedis consumer device with a valid PMR and the given daily consumption
     * error counter.
     */
    private String createConsumerDevice(String dcErrors) {
        String hardwareId = UUID.randomUUID().toString();
        testUtils.createDevice(hardwareId, EnedisConstants.MODULE_NAME);
        testUtils.setDeviceConfig(hardwareId, EnedisConstants.CONFIG_PRM, "test");
        testUtils.setDeviceConfig(hardwareId, EnedisConstants.CONFIG_CONSUMER, "true");
        testUtils.setDeviceConfig(hardwareId, EnedisConstants.CONFIG_PMR_EXPIRES_AT,
                Instant.now().plus(1, ChronoUnit.DAYS).toString());
        testUtils.setDeviceConfig(hardwareId, EnedisConstants.CONFIG_DC_ERRORS, dcErrors);
        return hardwareId;
    }

    @Test
    void fetchDataStopsWhenRateLimitReached() {
        String hardwareId = createConsumerDevice("3");
        enedisService.setRateLimiters(rateLimiter(true), rateLimiter(false));

        assertDoesNotThrow(() -> enedisService.fetchData());

        verifyNoInteractions(enedisFetchService);
        assertEquals("3", deviceService.getDeviceConfigValueAsString(hardwareId,
                EnedisConstants.CONFIG_DC_ERRORS).orElse(null));
    }

    @Test
    void fetchDataStopsWhenPerSecondRateLimitReached() {
        createConsumerDevice("0");
        enedisService.setRateLimiters(rateLimiter(false), rateLimiter(true));

        assertDoesNotThrow(() -> enedisService.fetchData());

        verifyNoInteractions(enedisFetchService);
    }

    @Test
    void fetchDataFetchesWhenPermitted() {
        String hardwareId = createConsumerDevice("0");
        enedisService.setRateLimiters(rateLimiter(true), rateLimiter(true));

        enedisService.fetchData();

        verify(enedisFetchService).fetchDailyConsumption(eq(hardwareId), eq("test"), anyString());
    }

    @Test
    void fetchDataAcquiresHourPermitBeforeSecondPermit() {
        createConsumerDevice("0");
        List<String> acquisitions = new CopyOnWriteArrayList<>();
        RateLimiter perSecond = rateLimiter(true);
        when(perSecond.acquirePermission()).thenAnswer(invocation -> acquisitions.add("second"));
        RateLimiter perHour = rateLimiter(true);
        when(perHour.acquirePermission()).thenAnswer(invocation -> acquisitions.add("hour"));
        enedisService.setRateLimiters(perSecond, perHour);

        enedisService.fetchData();

        assertFalse(acquisitions.isEmpty());
        for (int i = 0; i < acquisitions.size(); i++) {
            assertEquals(i % 2 == 0 ? "hour" : "second", acquisitions.get(i));
        }
    }

    @Test
    void fetchDataDoesNotTakeSecondPermitWhenHourPermitDenied() {
        createConsumerDevice("0");
        RateLimiter perSecond = rateLimiter(true);
        enedisService.setRateLimiters(perSecond, rateLimiter(false));

        enedisService.fetchData();

        verify(perSecond, never()).acquirePermission();
        verifyNoInteractions(enedisFetchService);
    }

    @Test
    void fetchDataRefreshesTokenAfterWaitingForPermit() {
        createConsumerDevice("0");
        // Waiting for a permit takes long enough for the cached token to expire.
        EnedisAuthTokenDTO stale = new EnedisAuthTokenDTO();
        stale.setAccessToken("stale-token");
        stale.setExpiresOn(-10);
        RateLimiter perHour = rateLimiter(true);
        when(perHour.acquirePermission()).thenAnswer(invocation -> {
            setCachedAuthToken(stale);
            return true;
        });
        enedisService.setRateLimiters(rateLimiter(true), perHour);
        EnedisAuthTokenDTO fresh = new EnedisAuthTokenDTO();
        fresh.setAccessToken("fresh-token");
        fresh.setExpiresOn(1000);
        when(enedisRestClient.getAuthToken(any(String.class), any(String.class), any(String.class)))
                .thenReturn(fresh);
        setCachedAuthToken(null);

        try {
            enedisService.fetchData();

            ArgumentCaptor<String> tokens = ArgumentCaptor.forClass(String.class);
            verify(enedisFetchService, atLeastOnce()).fetchDailyConsumption(anyString(), anyString(),
                    tokens.capture());
            verify(enedisFetchService, atLeastOnce()).fetchDailyConsumptionMaxPower(anyString(),
                    anyString(), tokens.capture());
            verify(enedisFetchService, atLeastOnce()).fetchConsumptionLoadCurve(anyString(),
                    anyString(), tokens.capture());
            assertTrue(tokens.getAllValues().stream().allMatch("fresh-token"::equals));
        } finally {
            setCachedAuthToken(null);
        }
    }

    @Test
    void createDeviceFailsFastWhenHourlyQuotaUsedUp() {
        enedisService.setRateLimiters(EnedisService.createPerSecondLimiter(),
                exhaustedHourLimiter());

        long start = System.nanoTime();
        assertThrows(QProcessingException.class, () -> enedisService.createDevice("x"));

        assertTrue(Duration.ofNanos(System.nanoTime() - start).compareTo(Duration.ofSeconds(3)) < 0);
        verify(enedisRestClient, never()).getSituationContractAuto(any(String.class),
                any(String.class));
        assertTrue(DeviceEntity.findByHardwareId("enedis-x").isEmpty());
    }

    @Test
    void fetchUsagePointIdFailsFastWhenHourlyQuotaUsedUp() {
        enedisService.setRateLimiters(EnedisService.createPerSecondLimiter(),
                exhaustedHourLimiter());

        long start = System.nanoTime();
        assertThrows(QProcessingException.class, () -> enedisService.fetchUsagePointId(11L));

        assertTrue(Duration.ofNanos(System.nanoTime() - start).compareTo(Duration.ofSeconds(3)) < 0);
        verify(enedisRestClient, never()).getSubscribedServices(
                any(EnedisSubscribedServicesRequestDTO.class), any(String.class));
    }

    @SneakyThrows
    @Test
    void createDeviceThrowsWhenRateLimitReached() {
        when(enedisRestClient.getSituationContractAuto(any(String.class), any(String.class)))
                .thenReturn(objectMapper.readValue(getSituationContractAutoResponse(),
                        new TypeReference<>() {}));
        enedisService.setRateLimiters(rateLimiter(true), rateLimiter(false));

        assertThrows(QProcessingException.class,
                () -> enedisService.createDevice("rate-limited"));
        assertTrue(DeviceEntity.findByHardwareId("enedis-rate-limited").isEmpty());
    }

    @Test
    void fetchUsagePointIdThrowsWhenRateLimitReached() {
        EnedisSubscribedServicesResponseDTO responseDTO = new EnedisSubscribedServicesResponseDTO();
        EnedisSubscribedServicesResponseDTO.ServiceSouscritDTO serviceSouscrit =
                new EnedisSubscribedServicesResponseDTO.ServiceSouscritDTO();
        serviceSouscrit.setPointId("14000000000001");
        responseDTO.setServiceSouscrit(List.of(serviceSouscrit));
        when(enedisRestClient.getSubscribedServices(any(EnedisSubscribedServicesRequestDTO.class),
                any(String.class))).thenReturn(responseDTO);
        enedisService.setRateLimiters(rateLimiter(true), rateLimiter(false));

        assertThrows(QProcessingException.class, () -> enedisService.fetchUsagePointId(11L));
    }

    @Test
    void refreshAuthToken() {
        EnedisAuthTokenDTO dto = new EnedisAuthTokenDTO();
        dto.setAccessToken("test");
        dto.setExpiresOn(1000);
        dto.setScope("test");
        dto.setTokenType("test");

        when(enedisRestClient.getAuthToken(any(String.class), any(String.class), any(String.class)))
                .thenReturn(dto);
        enedisRestClient.getAuthToken("test", "test", "test");
        assertDoesNotThrow(() -> enedisService.refreshAuthToken());
    }

    @SneakyThrows
    @Test
    void createDevice() {
        String situationContractAutoResponse = getSituationContractAutoResponse();
        when(enedisRestClient.getSituationContractAuto(any(String.class), any(String.class)))
                .thenReturn(objectMapper.readValue(situationContractAutoResponse, new TypeReference<>() {}));
        enedisService.createDevice("test");

        DeviceEntity device = DeviceEntity.findByHardwareId("enedis-test").orElseThrow();
        assertTrue(device.getAttributes().contains("segment=C5"));
        assertTrue(device.getAttributes().contains("contractType=Contrat CARD-I"));
        assertTrue(device.getAttributes().contains("subscribedPower=30kW"));
        assertTrue(device.getAttributes().contains("contractStart=2021-01-01T00:00:00+0100"));
    }

    @SneakyThrows
    @Test
    void createDeviceMultiPrm() {
        // Each PRM in a semicolon-separated list must get its own hardware id.
        when(enedisRestClient.getSituationContractAuto(any(String.class), any(String.class)))
                .thenReturn(objectMapper.readValue(getSituationContractAutoResponse(),
                        new TypeReference<>() {}));
        enedisService.createDevice("111;222");
        assertTrue(DeviceEntity.findByHardwareId("enedis-111").isPresent());
        assertTrue(DeviceEntity.findByHardwareId("enedis-222").isPresent());
    }

    @Test
    void createDeviceEmptySituationThrows() {
        when(enedisRestClient.getSituationContractAuto(any(String.class), any(String.class)))
                .thenReturn(List.of());
        QProcessingException exception = assertThrows(QProcessingException.class,
                () -> enedisService.createDevice("nocontract"));
        assertTrue(exception.getMessage().contains("No contract situation"));
    }

    @SneakyThrows
    @Test
    void createDeviceUnknownSegmentThrows() {
        when(enedisRestClient.getSituationContractAuto(any(String.class), any(String.class)))
                .thenReturn(objectMapper.readValue("""
                        [{"usage_point_id": "123", "segment": "XX"}]
                        """, new TypeReference<>() {}));
        QProcessingException exception = assertThrows(QProcessingException.class,
                () -> enedisService.createDevice("badsegment"));
        assertTrue(exception.getMessage().contains("Unknown segment type"));
        assertTrue(exception.getMessage().contains("XX"));
    }

    @SneakyThrows
    @Test
    void createDeviceConsumerAndProducer() {
        // A PRM consuming and injecting has a C5 and a P4 contract, it must be both a consumer and
        // a producer.
        when(enedisRestClient.getSituationContractAuto(any(String.class), any(String.class)))
                .thenReturn(objectMapper.readValue(readFixture("situation-contrat-c5-p4.json"),
                        new TypeReference<>() {}));
        when(enedisRestClient.getSynthContractAuto(any(String.class), any(String.class)))
                .thenReturn(objectMapper.readValue(readFixture("synth-contrat-c5-p4.json"),
                        EnedisSynthContractAutoDTO.class));

        enedisService.createDevice("c5p4");

        DeviceEntity device = DeviceEntity.findByHardwareId("enedis-c5p4").orElseThrow();
        assertEquals(Optional.of(true), deviceService.getDeviceConfigValueAsBoolean(
                "enedis-c5p4", EnedisConstants.CONFIG_CONSUMER));
        assertEquals(Optional.of(true), deviceService.getDeviceConfigValueAsBoolean(
                "enedis-c5p4", EnedisConstants.CONFIG_PRODUCER));
        assertTrue(device.getAttributes().contains("segment=C5/P4"));
        // General attributes come from the C5 contract.
        assertTrue(device.getAttributes().contains("contractType=Contrat Protocole501"));
        assertTrue(device.getAttributes().contains("subscribedPower=6kVA"));
        assertTrue(device.getAttributes().contains("meterType=Communicant (ouvert aux services)"));
        assertTrue(device.getAttributes().contains("lastActivationDate=2022-03-15T00:00:00+0100"));
        assertTrue(device.getAttributes()
                .contains("generationLastActivationDate=2023-06-01T00:00:00+0200"));
    }

    @SneakyThrows
    @Test
    void createDeviceProducerAndConsumerContractOrderIndependent() {
        // The P4 contract listed first must not change the segment order nor the contract the
        // general attributes come from.
        List<EnedisSituationContractAutoDTO> contracts = objectMapper.readValue(
                readFixture("situation-contrat-c5-p4.json"), new TypeReference<>() {});
        when(enedisRestClient.getSituationContractAuto(any(String.class), any(String.class)))
                .thenReturn(List.of(contracts.get(1), contracts.get(0)));

        enedisService.createDevice("p4c5");

        DeviceEntity device = DeviceEntity.findByHardwareId("enedis-p4c5").orElseThrow();
        assertTrue(device.getAttributes().contains("segment=C5/P4"));
        assertTrue(device.getAttributes().contains("contractType=Contrat Protocole501"));
    }

    @SneakyThrows
    @Test
    void createDeviceProducerOnlyUsesGenerationActivationDate() {
        when(enedisRestClient.getSituationContractAuto(any(String.class), any(String.class)))
                .thenReturn(objectMapper.readValue("""
                        [{"usage_point_id": "123", "segment": "P4", "contract_type": "Contrat CRAE"}]
                        """, new TypeReference<>() {}));
        when(enedisRestClient.getSynthContractAuto(any(String.class), any(String.class)))
                .thenReturn(objectMapper.readValue(readFixture("synth-contrat-c5-p4.json"),
                        EnedisSynthContractAutoDTO.class));

        enedisService.createDevice("p4only");

        DeviceEntity device = DeviceEntity.findByHardwareId("enedis-p4only").orElseThrow();
        assertTrue(device.getAttributes().contains("segment=P4"));
        assertTrue(device.getAttributes().contains("contractType=Contrat CRAE"));
        assertTrue(device.getAttributes().contains("lastActivationDate=2023-06-01T00:00:00+0200"));
        assertFalse(device.getAttributes().contains("generationLastActivationDate"));
    }

    @SneakyThrows
    @Test
    void createDeviceIgnoresOtherSegmentsAlongsideSupportedOnes() {
        when(enedisRestClient.getSituationContractAuto(any(String.class), any(String.class)))
                .thenReturn(objectMapper.readValue("""
                        [{"usage_point_id": "123", "segment": "XX", "contract_type": "Other"},
                         {"usage_point_id": "123", "segment": "P4", "contract_type": "Contrat CRAE"}]
                        """, new TypeReference<>() {}));

        enedisService.createDevice("otherseg");

        DeviceEntity device = DeviceEntity.findByHardwareId("enedis-otherseg").orElseThrow();
        assertTrue(device.getAttributes().contains("segment=P4"));
        assertTrue(device.getAttributes().contains("contractType=Contrat CRAE"));
        assertEquals(Optional.empty(), deviceService.getDeviceConfigValueAsBoolean(
                "enedis-otherseg", EnedisConstants.CONFIG_CONSUMER));
    }

    @SneakyThrows
    @Test
    void createDeviceWithAllItcApis() {
        when(enedisRestClient.getSituationContractAuto(any(String.class), any(String.class)))
                .thenReturn(objectMapper.readValue(getSituationContractAutoResponse(),
                        new TypeReference<>() {}));
        when(enedisRestClient.getSynthContractAuto(any(String.class), any(String.class)))
                .thenReturn(objectMapper.readValue("""
                        {
                          "segments": [{"segment": "C5"}],
                          "generation_last_activation_date": "2019-05-06T00:00:00+0200",
                          "consumption_last_activation_date": "2021-03-01T00:00:00+0100",
                          "last_subscribed_power_change_date": "2022-01-02T00:00:00+0100",
                          "services_level": "2"
                        }
                        """, EnedisSynthContractAutoDTO.class));
        when(enedisRestClient.getAlimentationAuto(any(String.class), any(String.class)))
                .thenReturn(objectMapper.readValue("""
                        {
                          "serial_number": 999999999,
                          "connection_state": "Alimenté",
                          "voltage_level": "HTA",
                          "consumption_connection_power": {"value": 1100.0, "unit": "kW"},
                          "generation_connection_power": {"value": 1000.0, "unit": "kW"},
                          "nominal_service_voltage": {"value": 20000.0, "unit": "V"}
                        }
                        """, EnedisAlimentationAutoDTO.class));
        when(enedisRestClient.getDonneesGeneralesAuto(any(String.class), any(String.class)))
                .thenReturn(objectMapper.readValue("""
                        {
                          "address": {
                            "number_street_name": "1 rue du test",
                            "postal_code_city": "75001 PARIS",
                            "insee_code": "75001"
                          }
                        }
                        """, EnedisDonneesGeneralesAutoDTO.class));

        enedisService.createDevice("itcfull");

        DeviceEntity device = DeviceEntity.findByHardwareId("enedis-itcfull").orElseThrow();
        assertTrue(device.getAttributes().contains("meterType=2"));
        assertTrue(device.getAttributes().contains("lastActivationDate=2021-03-01T00:00:00+0100"));
        assertTrue(device.getAttributes().contains("usagePointStatus=Alimenté"));
        assertTrue(device.getAttributes().contains("voltageLevel=HTA"));
        assertTrue(device.getAttributes().contains("serialNumber=999999999"));
        assertTrue(device.getAttributes().contains("installationAddress=1 rue du test 75001 PARIS"));
        assertTrue(device.getAttributes().contains("inseeCode=75001"));
    }

    @SneakyThrows
    @Test
    void createDeviceItcFailuresTolerated() {
        // Only situation_contrat_auto is required; the other ITC APIs failing must not prevent
        // device registration.
        when(enedisRestClient.getSituationContractAuto(any(String.class), any(String.class)))
                .thenReturn(objectMapper.readValue(getSituationContractAutoResponse(),
                        new TypeReference<>() {}));
        when(enedisRestClient.getSynthContractAuto(any(String.class), any(String.class)))
                .thenThrow(new RuntimeException("synth_contrat_auto unavailable"));
        when(enedisRestClient.getAlimentationAuto(any(String.class), any(String.class)))
                .thenThrow(new RuntimeException("alimentation_auto unavailable"));
        when(enedisRestClient.getDonneesGeneralesAuto(any(String.class), any(String.class)))
                .thenThrow(new RuntimeException("donnees_generales_auto unavailable"));

        enedisService.createDevice("itcpartial");

        DeviceEntity device = DeviceEntity.findByHardwareId("enedis-itcpartial").orElseThrow();
        assertTrue(device.getAttributes().contains("segment=C5"));
        assertFalse(device.getAttributes().contains("meterType"));
        assertFalse(device.getAttributes().contains("usagePointStatus"));
    }

    @SneakyThrows
    @Test
    void createDeviceSkipsOptionalStepsWhenHourlyQuotaReached() {
        // Only the situation contract gets a permit, the optional ITC steps find the quota used up.
        when(enedisRestClient.getSituationContractAuto(any(String.class), any(String.class)))
                .thenReturn(objectMapper.readValue(getSituationContractAutoResponse(),
                        new TypeReference<>() {}));
        when(enedisRestClient.getSynthContractAuto(any(String.class), any(String.class)))
                .thenReturn(objectMapper.readValue("""
                        {"services_level": "2",
                         "consumption_last_activation_date": "2021-03-01T00:00:00+0100"}
                        """, EnedisSynthContractAutoDTO.class));
        when(enedisRestClient.getAlimentationAuto(any(String.class), any(String.class)))
                .thenReturn(objectMapper.readValue("""
                        {"connection_state": "Alimenté", "voltage_level": "HTA"}
                        """, EnedisAlimentationAutoDTO.class));
        when(enedisRestClient.getDonneesGeneralesAuto(any(String.class), any(String.class)))
                .thenReturn(objectMapper.readValue("""
                        {"address": {"number_street_name": "1 rue du test"}}
                        """, EnedisDonneesGeneralesAutoDTO.class));
        enedisService.setRateLimiters(EnedisService.createPerSecondLimiter(),
                hourLimiterWithPermits(1));

        assertDoesNotThrow(() -> enedisService.createDevice("quota"));

        DeviceEntity device = DeviceEntity.findByHardwareId("enedis-quota").orElseThrow();
        assertTrue(device.getAttributes().contains("segment=C5"));
        assertFalse(device.getAttributes().contains("meterType"));
        assertFalse(device.getAttributes().contains("usagePointStatus"));
        assertFalse(device.getAttributes().contains("installationAddress"));
        verify(enedisRestClient, never()).getSynthContractAuto(any(String.class), any(String.class));
        verify(enedisRestClient, never()).getAlimentationAuto(any(String.class), any(String.class));
        verify(enedisRestClient, never()).getDonneesGeneralesAuto(any(String.class),
                any(String.class));
    }

    @SneakyThrows
    @Test
    void createDeviceMinimalContract() {
        // Enedis may omit any contract field; only the segment is required.
        when(enedisRestClient.getSituationContractAuto(any(String.class), any(String.class)))
                .thenReturn(objectMapper.readValue("""
                        [{"usage_point_id": "123", "segment": "P4"}]
                        """, new TypeReference<>() {}));
        enedisService.createDevice("minimal");
        DeviceEntity device = DeviceEntity.findByHardwareId("enedis-minimal").orElseThrow();
        assertTrue(device.getAttributes().contains("segment=P4"));
    }

    @Test
    void getSelfRegistrationPage() {
        String state = UUID.randomUUID().toString();
        assertTrue(enedisService.getSelfRegistrationPage(state).contains(state));
    }

    @Test
    void selfRegistrationPageUsesAuthorizeV2() {
        assertTrue(enedisService.getSelfRegistrationPage("s")
            .contains("dataconnect/v2/oauth2/authorize?client_id="));
    }

    @Test
    void getRegistrationSuccessfulPage() {
        assertNotNull(enedisService.getRegistrationSuccessfulPage());
    }

    @Test
    void getErrorPage() {
        assertNotNull(enedisService.getErrorPage());
    }

    @Test
    void countDevices() {
        String hardwareId = UUID.randomUUID().toString();
        testUtils.createDevice(hardwareId);
        assertTrue(enedisService.countDevices() >= 1);
    }

    @Test
    void getFetchErrors() {
        assertNotNull(enedisService.getFetchErrors());
    }

    @Test
    void resetFetchErrors() {
        String hardwareId = UUID.randomUUID().toString();
        testUtils.createDevice(hardwareId);
        assertNotNull(enedisService.resetFetchErrors(hardwareId));
    }

    @Test
    void getFetchErrorsIncludesLoadCurves() {
        String hardwareId = UUID.randomUUID().toString();
        testUtils.createDevice(hardwareId);
        testUtils.setDeviceConfig(hardwareId, EnedisConstants.CONFIG_CLC_ERRORS, "10");

        assertTrue(enedisService.getFetchErrors().stream()
                .anyMatch(device -> hardwareId.equals(device.getHardwareId())));
    }

    @Test
    void resetFetchErrorsResetsLoadCurves() {
        String hardwareId = UUID.randomUUID().toString();
        testUtils.createDevice(hardwareId);
        testUtils.setDeviceConfig(hardwareId, EnedisConstants.CONFIG_CLC_ERRORS, "10");
        testUtils.setDeviceConfig(hardwareId, EnedisConstants.CONFIG_PLC_ERRORS, "10");

        enedisService.resetFetchErrors(hardwareId);

        assertEquals("0", deviceService.getDeviceConfigValueAsString(hardwareId,
                EnedisConstants.CONFIG_CLC_ERRORS).orElse(null));
        assertEquals("0", deviceService.getDeviceConfigValueAsString(hardwareId,
                EnedisConstants.CONFIG_PLC_ERRORS).orElse(null));
    }

    @Test
    void fetchData() {
        String hardwareId = UUID.randomUUID().toString();
        testUtils.createDevice(hardwareId, EnedisConstants.MODULE_NAME);

        EnedisAuthTokenDTO accessToken = new EnedisAuthTokenDTO();
        accessToken.setAccessToken("test");
        accessToken.setExpiresOn(1000);
        accessToken.setScope("test");
        accessToken.setTokenType("test");
        when(enedisRestClient.getAuthToken(any(String.class), any(String.class), any(String.class)))
                .thenReturn(accessToken);
        testUtils.setDeviceConfig(hardwareId, EnedisConstants.CONFIG_PRM, "test");
        when(enedisFetchService.fetchDailyConsumption(any(String.class), any(String.class),
                any(String.class))).thenReturn(0);
        when(enedisFetchService.fetchDailyConsumptionMaxPower(any(String.class), any(String.class),
                any(String.class))).thenReturn(0);
        when(enedisFetchService.fetchDailyProduction(any(String.class), any(String.class),
                any(String.class))).thenReturn(0);

        assertDoesNotThrow(() -> enedisService.fetchData());
    }

    @Test
    void fetchDataRunsConsumerAndProducerBranches() {
        String hardwareId = createConsumerDevice("0");
        testUtils.setDeviceConfig(hardwareId, EnedisConstants.CONFIG_PRODUCER, "true");
        enedisService.setRateLimiters(rateLimiter(true), rateLimiter(true));

        enedisService.fetchData();

        verify(enedisFetchService).fetchDailyConsumption(eq(hardwareId), eq("test"), anyString());
        verify(enedisFetchService).fetchDailyConsumptionMaxPower(eq(hardwareId), eq("test"),
                anyString());
        verify(enedisFetchService).fetchConsumptionLoadCurve(eq(hardwareId), eq("test"),
                anyString());
        verify(enedisFetchService).fetchDailyProduction(eq(hardwareId), eq("test"), anyString());
        verify(enedisFetchService).fetchProductionLoadCurve(eq(hardwareId), eq("test"),
                anyString());
    }

    @Test
    void fetchUsagePointId() {
        Long authorizationId = 123456L;

        EnedisSubscribedServicesResponseDTO responseDTO = new EnedisSubscribedServicesResponseDTO();
        EnedisSubscribedServicesResponseDTO.ServiceSouscritDTO serviceSouscrit = new EnedisSubscribedServicesResponseDTO.ServiceSouscritDTO();
        serviceSouscrit.setPointId("99999999999999");
        responseDTO.setServiceSouscrit(List.of(serviceSouscrit));

        ArgumentCaptor<EnedisSubscribedServicesRequestDTO> requestCaptor =
                ArgumentCaptor.forClass(EnedisSubscribedServicesRequestDTO.class);
        when(enedisRestClient.getSubscribedServices(requestCaptor.capture(), any(String.class)))
                .thenReturn(responseDTO);

        assertEquals("99999999999999", enedisService.fetchUsagePointId(authorizationId));

        EnedisSubscribedServicesRequestDTO request = requestCaptor.getValue();
        assertEquals(123456L, request.getAutorisationId().longValue());
        assertEquals(List.of("ACTIF"), request.getEtatCode());
        assertEquals("ACCES", request.getServiceType());
        // comptage=true would return only the count, without the serviceSouscrit list.
        assertFalse(request.getComptage());
    }

    @Test
    void fetchUsagePointIdNoServicesThrows() {
        // The Enedis sandbox mock for autorisationId=10 returns {"nbTotalServices": 0}.
        EnedisSubscribedServicesResponseDTO responseDTO = new EnedisSubscribedServicesResponseDTO();
        responseDTO.setNbTotalServices(0);

        when(enedisRestClient.getSubscribedServices(any(EnedisSubscribedServicesRequestDTO.class),
                any(String.class))).thenReturn(responseDTO);

        assertThrows(QProcessingException.class, () -> enedisService.fetchUsagePointId(10L));
    }

    @Test
    void fetchUsagePointIdMultipleServicesReturnsFirst() {
        EnedisSubscribedServicesResponseDTO responseDTO = new EnedisSubscribedServicesResponseDTO();
        EnedisSubscribedServicesResponseDTO.ServiceSouscritDTO first = new EnedisSubscribedServicesResponseDTO.ServiceSouscritDTO();
        first.setPointId("14000000000001");
        EnedisSubscribedServicesResponseDTO.ServiceSouscritDTO second = new EnedisSubscribedServicesResponseDTO.ServiceSouscritDTO();
        second.setPointId("14000000000002");
        responseDTO.setServiceSouscrit(List.of(first, second));

        when(enedisRestClient.getSubscribedServices(any(EnedisSubscribedServicesRequestDTO.class),
                any(String.class))).thenReturn(responseDTO);

        assertEquals("14000000000001", enedisService.fetchUsagePointId(11L));
    }

    @SneakyThrows
    private String readFixture(String fileName) {
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream("wiremock/enedis/__files/" + fileName)) {
            return new String(Objects.requireNonNull(in).readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String getSituationContractAutoResponse() {
        return """
                [
                  {
                    "usage_point_id": "3232947408",
                    "contract_start": "2021-01-01T00:00:00+0100",
                    "contract_type": "Contrat CARD-I",
                    "contractor": "RENNES METROPOLE",
                    "balance_responsable_party": "ACM_010",
                    "pricing_structure": "Nouvelle Offre",
                    "distribution_tariff": "Tarif HTA Courte Utilisation",
                    "supplier_tariff_profile": [
                      {
                        "name": "Heures Creuses Hiver/Saison Haute",
                        "power": {
                          "unit": "kW",
                          "value": "30"
                        }
                      }
                    ],
                    "distribution_tariff_profile": [
                      {
                        "name": "Heures Creuses Hiver/Saison Haute",
                        "power": {
                          "unit": "kW",
                          "value": "30"
                        }
                      }
                    ],
                    "supplier_mobile_peak": "string",
                    "distribution_mobile_peak": "string",
                    "subscribed_power": {
                      "unit": "kW",
                      "value": "30"
                    },
                    "segment": "C5",
                    "customer": {
                      "customer": {
                        "adress": {
                          "line1": "string",
                          "line2": "string",
                          "line3": "string",
                          "line4": "4 RUE HENRI FREVILLE",
                          "line5": "CS 20723",
                          "line6": "35207 RENNES",
                          "line7": "FR (France)"
                        },
                        "contact_data": {
                          "email": "m.belay@rennesmetropole.fr",
                          "landline": "624136309",
                          "phone": "223621058"
                        },
                        "person": {
                          "title": "M",
                          "lastname": "BELAY",
                          "firstname": "Martin"
                        },
                        "organization": {
                          "name": "RENNES METROPOLE",
                          "commercial_name": "string",
                          "business_code": "string",
                          "siret_number": "24350013900262",
                          "siren_number": "243500139"
                        }
                      }
                    }
                  }
                ]
                """;
    }

}
