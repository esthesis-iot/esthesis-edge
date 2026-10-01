package esthesis.edge.modules.enedis.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import esthesis.edge.TestUtils;
import esthesis.edge.dto.QueueItemDTO;
import esthesis.edge.modules.enedis.client.EnedisClient;
import esthesis.edge.modules.enedis.config.EnedisConstants;
import esthesis.edge.modules.enedis.dto.datahub.EnedisMesureDTO;
import esthesis.edge.services.DeviceService;
import esthesis.edge.services.QueueService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

@QuarkusTest
class EnedisFetchServiceTest {

    @Inject
    TestUtils testUtils;

    @Inject
    EnedisFetchService enedisFetchService;

    @Inject
    DeviceService deviceService;

    @Inject
    QueueService queueService;

    @InjectMock
    @RestClient
    EnedisClient enedisRestClient;

    @InjectSpy
    EnedisELPMapperService enedisELPMapperService;

    private static EnedisMesureDTO dto(String physique, String value, String date) {
        return new EnedisMesureDTO().setGrandeur(List.of(new EnedisMesureDTO.Grandeur()
                .setGrandeurPhysique(physique)
                .setPoints(List.of(new EnedisMesureDTO.Point().setV(value).setD(date)))));
    }

    private long queued(String hardwareId) {
        return queueService.list().stream().filter(i -> hardwareId.equals(i.getHardwareId()))
                .count();
    }

    private String config(String hardwareId, String key) {
        return deviceService.getDeviceConfigValueAsString(hardwareId, key).orElse(null);
    }

    @Test
    void fetchDailyConsumptionQueuesOneItem() {
        String hardwareId = "fetch-dc";
        testUtils.createDevice(hardwareId);
        when(enedisRestClient.getDailyConsumption(any(String.class), any(String.class),
                any(String.class), any(String.class)))
                .thenReturn(dto("EA", "540", "2026-09-25"));

        Instant before = Instant.now();
        assertEquals(1, enedisFetchService.fetchDailyConsumption(hardwareId, "test", "test"));

        assertEquals(1, queued(hardwareId));
        QueueItemDTO item = queueService.list().stream()
                .filter(i -> hardwareId.equals(i.getHardwareId())).findFirst().orElseThrow();
        assertTrue(item.getDataObject().contains("=540i 2026-09-25T23:59:59Z"));
        Instant lastFetched = Instant.parse(
                config(hardwareId, EnedisConstants.CONFIG_DC_LAST_FETCHED_AT));
        assertFalse(lastFetched.isBefore(before.minusSeconds(1)));
    }

    @Test
    void fetchDailyConsumptionMaxPower() {
        String hardwareId = "fetch-dcmp";
        testUtils.createDevice(hardwareId);
        when(enedisRestClient.getDailyConsumptionMaxPower(any(String.class), any(String.class),
                any(String.class), any(String.class), any(String.class), any(String.class)))
                .thenReturn(dto("PMA", "9656", "2026-09-25 04:12:00"));

        assertEquals(1,
                enedisFetchService.fetchDailyConsumptionMaxPower(hardwareId, "test", "test"));
        assertEquals(1, queued(hardwareId));
    }

    @Test
    void fetchDailyProduction() {
        String hardwareId = "fetch-dp";
        testUtils.createDevice(hardwareId);
        when(enedisRestClient.getDailyProduction(any(String.class), any(String.class),
                any(String.class), any(String.class)))
                .thenReturn(dto("EA", "540", "2026-09-25"));

        assertEquals(1, enedisFetchService.fetchDailyProduction(hardwareId, "test", "test"));
        assertEquals(1, queued(hardwareId));
    }

    @Test
    void fetchConsumptionLoadCurve() {
        String hardwareId = "fetch-clc";
        testUtils.createDevice(hardwareId);
        when(enedisRestClient.getConsumptionLoadCurve(any(String.class), any(String.class),
                any(String.class), any(String.class)))
                .thenReturn(dto("PA", "540", "2026-09-25 03:00:00"));

        assertEquals(1, enedisFetchService.fetchConsumptionLoadCurve(hardwareId, "test", "test"));
        assertEquals(1, queued(hardwareId));
    }

    @Test
    void fetchProductionLoadCurve() {
        String hardwareId = "fetch-plc";
        testUtils.createDevice(hardwareId);
        when(enedisRestClient.getProductionLoadCurve(any(String.class), any(String.class),
                any(String.class), any(String.class)))
                .thenReturn(dto("PA", "540", "2026-09-25 03:00:00"));

        assertEquals(1, enedisFetchService.fetchProductionLoadCurve(hardwareId, "test", "test"));
        assertEquals(1, queued(hardwareId));
    }

    @Test
    void unparseableResponseQueuesNothingAndDoesNotThrow() {
        String hardwareId = "fetch-unparseable";
        testUtils.createDevice(hardwareId);
        testUtils.setDeviceConfig(hardwareId, EnedisConstants.CONFIG_DC_ERRORS, "3");
        // A sandbox placeholder date is skipped by the mapper: nothing to queue, last fetched
        // unchanged, and it must not escape as an exception from the fetch.
        when(enedisRestClient.getDailyConsumption(any(String.class), any(String.class),
                any(String.class), any(String.class)))
                .thenReturn(dto("EA", "540", "20XX-XX-XX"));

        assertEquals(0, enedisFetchService.fetchDailyConsumption(hardwareId, "test", "test"));

        assertEquals(0, queued(hardwareId));
        assertNull(config(hardwareId, EnedisConstants.CONFIG_DC_LAST_FETCHED_AT));
        assertEquals("0", config(hardwareId, EnedisConstants.CONFIG_DC_ERRORS));
    }

    @Test
    void mapperFailureCountsAsErrorAndDoesNotThrow() {
        String hardwareId = "fetch-mapper-failure";
        testUtils.createDevice(hardwareId);
        when(enedisRestClient.getDailyConsumption(any(String.class), any(String.class),
                any(String.class), any(String.class)))
                .thenReturn(dto("EA", "540", "2026-09-25"));
        doThrow(new IllegalStateException("mapping failed")).when(enedisELPMapperService)
                .toELP(any(EnedisMesureDTO.class), any(String.class), any(String.class));

        assertEquals(0, enedisFetchService.fetchDailyConsumption(hardwareId, "test", "test"));

        assertEquals("1", config(hardwareId, EnedisConstants.CONFIG_DC_ERRORS));
        assertEquals(0, queued(hardwareId));
        assertNull(config(hardwareId, EnedisConstants.CONFIG_DC_LAST_FETCHED_AT));
    }

    @Test
    void clientErrorIncrementsErrors() {
        String hardwareId = "fetch-client-error";
        testUtils.createDevice(hardwareId);
        when(enedisRestClient.getDailyConsumption(any(String.class), any(String.class),
                any(String.class), any(String.class)))
                .thenThrow(new WebApplicationException(403));

        assertEquals(0, enedisFetchService.fetchDailyConsumption(hardwareId, "test", "test"));

        assertEquals("1", config(hardwareId, EnedisConstants.CONFIG_DC_ERRORS));
        assertEquals(0, queued(hardwareId));
        assertNull(config(hardwareId, EnedisConstants.CONFIG_DC_LAST_FETCHED_AT));
    }

    @Test
    void emptyResponseDoesNotAdvanceLastFetched() {
        String hardwareId = "fetch-empty";
        testUtils.createDevice(hardwareId);
        Instant previous = Instant.now().minus(Duration.ofDays(3));
        testUtils.setDeviceConfig(hardwareId, EnedisConstants.CONFIG_DC_LAST_FETCHED_AT,
                previous.toString());
        testUtils.setDeviceConfig(hardwareId, EnedisConstants.CONFIG_DC_ERRORS, "3");
        when(enedisRestClient.getDailyConsumption(any(String.class), any(String.class),
                any(String.class), any(String.class)))
                .thenReturn(new EnedisMesureDTO().setGrandeur(List.of()));

        assertEquals(0, enedisFetchService.fetchDailyConsumption(hardwareId, "test", "test"));

        assertEquals(previous.toString(),
                config(hardwareId, EnedisConstants.CONFIG_DC_LAST_FETCHED_AT));
        assertEquals("0", config(hardwareId, EnedisConstants.CONFIG_DC_ERRORS));
        assertEquals(0, queued(hardwareId));
    }

    @Test
    void loadCurveWindowIsCappedAtSevenDays() {
        String hardwareId = "fetch-clc-window";
        testUtils.createDevice(hardwareId);
        Instant lastFetched = Instant.now().minus(Duration.ofDays(20));
        testUtils.setDeviceConfig(hardwareId, EnedisConstants.CONFIG_CLC_LAST_FETCHED_AT,
                lastFetched.toString());
        when(enedisRestClient.getConsumptionLoadCurve(any(String.class), any(String.class),
                any(String.class), any(String.class)))
                .thenReturn(dto("PA", "540", "2026-09-25 03:00:00"));

        assertEquals(1, enedisFetchService.fetchConsumptionLoadCurve(hardwareId, "test", "test"));

        LocalDate start = lastFetched.atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate end = start.plusDays(7);
        verify(enedisRestClient).getConsumptionLoadCurve(eq(start.toString()), eq(end.toString()),
                eq("test"), any(String.class));
        assertEquals(end.atStartOfDay(ZoneOffset.UTC).toInstant(),
                Instant.parse(config(hardwareId, EnedisConstants.CONFIG_CLC_LAST_FETCHED_AT)));
    }

    @Test
    void loadCurveWindowEndsTodayWhenGapIsShort() {
        String hardwareId = "fetch-plc-window";
        testUtils.createDevice(hardwareId);
        Instant lastFetched = Instant.now().minus(Duration.ofDays(2));
        testUtils.setDeviceConfig(hardwareId, EnedisConstants.CONFIG_PLC_LAST_FETCHED_AT,
                lastFetched.toString());
        when(enedisRestClient.getProductionLoadCurve(any(String.class), any(String.class),
                any(String.class), any(String.class)))
                .thenReturn(dto("PA", "540", "2026-09-25 03:00:00"));

        assertEquals(1, enedisFetchService.fetchProductionLoadCurve(hardwareId, "test", "test"));

        LocalDate today = Instant.now().atZone(ZoneOffset.UTC).toLocalDate();
        verify(enedisRestClient).getProductionLoadCurve(
                eq(lastFetched.atZone(ZoneOffset.UTC).toLocalDate().toString()),
                eq(today.toString()), eq("test"), any(String.class));
        assertEquals(today.atStartOfDay(ZoneOffset.UTC).toInstant(),
                Instant.parse(config(hardwareId, EnedisConstants.CONFIG_PLC_LAST_FETCHED_AT)));
    }

    @Test
    void sameDayRerunSkipsTheRequestWithoutCountingAnError() {
        String hardwareId = "fetch-clc-same-day";
        testUtils.createDevice(hardwareId);
        Instant todayStart = Instant.now().atZone(ZoneOffset.UTC).toLocalDate()
                .atStartOfDay(ZoneOffset.UTC).toInstant();
        testUtils.setDeviceConfig(hardwareId, EnedisConstants.CONFIG_CLC_LAST_FETCHED_AT,
                todayStart.toString());
        testUtils.setDeviceConfig(hardwareId, EnedisConstants.CONFIG_CLC_ERRORS, "2");

        assertEquals(0, enedisFetchService.fetchConsumptionLoadCurve(hardwareId, "test", "test"));

        verifyNoInteractions(enedisRestClient);
        assertEquals("2", config(hardwareId, EnedisConstants.CONFIG_CLC_ERRORS));
        assertEquals(todayStart.toString(),
                config(hardwareId, EnedisConstants.CONFIG_CLC_LAST_FETCHED_AT));
        assertEquals(0, queued(hardwareId));
    }

    @Test
    void sameDayRerunOfDailyTypeSkipsTheRequest() {
        String hardwareId = "fetch-dc-same-day";
        testUtils.createDevice(hardwareId);
        Instant fetchedToday = Instant.now();
        testUtils.setDeviceConfig(hardwareId, EnedisConstants.CONFIG_DC_LAST_FETCHED_AT,
                fetchedToday.toString());

        assertEquals(0, enedisFetchService.fetchDailyConsumption(hardwareId, "test", "test"));

        verifyNoInteractions(enedisRestClient);
        assertEquals(fetchedToday.toString(),
                config(hardwareId, EnedisConstants.CONFIG_DC_LAST_FETCHED_AT));
    }

    @Test
    void emptyHistoricalLoadCurveWindowAdvancesLastFetched() {
        String hardwareId = "fetch-clc-empty-window";
        testUtils.createDevice(hardwareId);
        Instant lastFetched = Instant.now().minus(Duration.ofDays(20));
        testUtils.setDeviceConfig(hardwareId, EnedisConstants.CONFIG_CLC_LAST_FETCHED_AT,
                lastFetched.toString());
        testUtils.setDeviceConfig(hardwareId, EnedisConstants.CONFIG_CLC_ERRORS, "3");
        when(enedisRestClient.getConsumptionLoadCurve(any(String.class), any(String.class),
                any(String.class), any(String.class)))
                .thenReturn(new EnedisMesureDTO().setGrandeur(List.of()));

        assertEquals(0, enedisFetchService.fetchConsumptionLoadCurve(hardwareId, "test", "test"));

        LocalDate end = lastFetched.atZone(ZoneOffset.UTC).toLocalDate().plusDays(7);
        assertEquals(end.atStartOfDay(ZoneOffset.UTC).toInstant(),
                Instant.parse(config(hardwareId, EnedisConstants.CONFIG_CLC_LAST_FETCHED_AT)));
        assertEquals("0", config(hardwareId, EnedisConstants.CONFIG_CLC_ERRORS));
        assertEquals(0, queued(hardwareId));
    }

    @Test
    void emptyLoadCurveWindowEndingTodayDoesNotAdvanceLastFetched() {
        String hardwareId = "fetch-plc-empty-today";
        testUtils.createDevice(hardwareId);
        Instant lastFetched = Instant.now().minus(Duration.ofDays(2));
        testUtils.setDeviceConfig(hardwareId, EnedisConstants.CONFIG_PLC_LAST_FETCHED_AT,
                lastFetched.toString());
        when(enedisRestClient.getProductionLoadCurve(any(String.class), any(String.class),
                any(String.class), any(String.class)))
                .thenReturn(new EnedisMesureDTO().setGrandeur(List.of()));

        assertEquals(0, enedisFetchService.fetchProductionLoadCurve(hardwareId, "test", "test"));

        assertEquals(lastFetched.toString(),
                config(hardwareId, EnedisConstants.CONFIG_PLC_LAST_FETCHED_AT));
    }
}
