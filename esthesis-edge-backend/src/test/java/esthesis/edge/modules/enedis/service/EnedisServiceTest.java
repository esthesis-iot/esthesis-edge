package esthesis.edge.modules.enedis.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

import lombok.SneakyThrows;
import org.eclipse.microprofile.rest.client.inject.RestClient;
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
        assertEquals(123456L, request.getAutorisationId());
        assertEquals(List.of("ACTIF"), request.getEtatCode());
        assertEquals("ACCES", request.getServiceType());
        // comptage=true would return only the count, without the serviceSouscrit list.
        assertFalse(request.isComptage());
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
