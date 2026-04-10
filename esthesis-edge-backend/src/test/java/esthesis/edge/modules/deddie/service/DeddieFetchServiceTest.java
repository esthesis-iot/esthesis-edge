package esthesis.edge.modules.deddie.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.edge.TestUtils;
import esthesis.edge.dto.QueueItemDTO;
import esthesis.edge.modules.deddie.config.DeddieConstants;
import esthesis.edge.modules.deddie.client.DeddieClient;
import esthesis.edge.modules.deddie.dto.DeddieCurvesActiveConsumptionDTO;
import esthesis.edge.modules.deddie.dto.DeddieCurvesEnergyInjectedDTO;
import esthesis.edge.modules.deddie.dto.DeddieCurvesEnergyProducedDTO;
import esthesis.edge.modules.deddie.dto.DeddieCurvesReactivePowerDTO;
import esthesis.edge.services.DeviceService;
import esthesis.edge.services.QueueService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
class DeddieFetchServiceTest {

    @Inject
    TestUtils testUtils;

    @Inject
    DeddieFetchService deddieFetchService;

    @InjectMock
    @RestClient
    DeddieClient deddieClient;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    QueueService queueService;

    @Inject
    DeviceService deviceService;

    @BeforeEach
    @Transactional
    void setUp() {
        queueService.list().forEach(queue -> queueService.remove(queue.getId()));
    }

    @Test
    @SneakyThrows
    void fetchCacData() {
        String hardwareId = "deddie-test1";
        testUtils.createDevice(hardwareId);

        DeddieCurvesActiveConsumptionDTO dto = objectMapper.readValue("""
                        {
                            "curveSearchParameters": {
                                "analysisType": 1,
                                "classType": "active",
                                "confirmedDataFlag": false,
                                "fromDate": "2025-08-14T09:00:00+00:00",
                                "hourAnalysisFlag": false,
                                "supplyNumber": "123456789",
                                "taxNumber": "987654321",
                                "toDate": "2025-08-18T09:00:00+00:00"
                            },
                            "curves": [
                                {
                                    "certifiedFlag": false,
                                    "consumption": "0.172",
                                    "meterDate": "15/08/2025 00:15"
                                }
                            ]
                        }
                        """
                , DeddieCurvesActiveConsumptionDTO.class);

        when(deddieClient.getCurvesCurvesActiveConsumption(any(), anyString(), anyString())).thenReturn(dto);

        assertEquals(1, deddieFetchService.fetchCacData(hardwareId,
                "test",
                "test",
                "test"));
    }

    @Test
    @SneakyThrows
    void fetchCacDataSkipsInvalidCurvesBeforeQueueing() {
        String hardwareId = "deddie-test-invalid-mixed";
        testUtils.createDevice(hardwareId);

        DeddieCurvesActiveConsumptionDTO dto = objectMapper.readValue("""
                        {
                            "curveSearchParameters": {
                                "analysisType": 1,
                                "classType": "active",
                                "confirmedDataFlag": false,
                                "fromDate": "2025-08-14T09:00:00+00:00",
                                "hourAnalysisFlag": false,
                                "supplyNumber": "123456789",
                                "taxNumber": "987654321",
                                "toDate": "2025-08-18T09:00:00+00:00"
                            },
                            "curves": [
                                {
                                    "certifiedFlag": false,
                                    "consumption": "0.172",
                                    "meterDate": "15/08/2025 00:15"
                                },
                                {
                                    "certifiedFlag": false,
                                    "consumption": null,
                                    "meterDate": "15/08/2025 00:30"
                                },
                                {
                                    "certifiedFlag": false,
                                    "consumption": "bad-value",
                                    "meterDate": "15/08/2025 00:45"
                                }
                            ]
                        }
                        """
                , DeddieCurvesActiveConsumptionDTO.class);

        when(deddieClient.getCurvesCurvesActiveConsumption(any(), anyString(), anyString())).thenReturn(dto);

        assertEquals(1, deddieFetchService.fetchCacData(hardwareId,
                "test",
                "test",
                "test"));

        List<QueueItemDTO> queuedItems = queueService.list();
        assertEquals(1, queuedItems.size());
        assertTrue(queuedItems.getFirst().getDataObject().contains("=0.172f"));
        assertFalse(queuedItems.getFirst().getDataObject().contains("nullf"));
        assertFalse(queuedItems.getFirst().getDataObject().contains("bad-valuef"));
        assertTrue(deviceService.getDeviceConfigValueAsInstant(hardwareId,
                DeddieConstants.CONFIG_CAC_LAST_FETCHED_AT).isPresent());
    }

    @Test
    @SneakyThrows
    void fetchCacDataDoesNotQueueOnlyInvalidCurves() {
        String hardwareId = "deddie-test-invalid-only";
        testUtils.createDevice(hardwareId);
        Instant lastFetchedAt = Instant.parse("2025-08-10T00:00:00Z");
        testUtils.setDeviceConfig(hardwareId,
                DeddieConstants.CONFIG_CAC_LAST_FETCHED_AT,
                lastFetchedAt.toString());

        DeddieCurvesActiveConsumptionDTO dto = objectMapper.readValue("""
                        {
                            "curveSearchParameters": {
                                "analysisType": 1,
                                "classType": "active",
                                "confirmedDataFlag": false,
                                "fromDate": "2025-08-14T09:00:00+00:00",
                                "hourAnalysisFlag": false,
                                "supplyNumber": "123456789",
                                "taxNumber": "987654321",
                                "toDate": "2025-08-18T09:00:00+00:00"
                            },
                            "curves": [
                                {
                                    "certifiedFlag": false,
                                    "consumption": null,
                                    "meterDate": "15/08/2025 00:30"
                                },
                                {
                                    "certifiedFlag": false,
                                    "consumption": "bad-value",
                                    "meterDate": "15/08/2025 00:45"
                                }
                            ]
                        }
                        """
                , DeddieCurvesActiveConsumptionDTO.class);

        when(deddieClient.getCurvesCurvesActiveConsumption(any(), anyString(), anyString())).thenReturn(dto);

        assertEquals(0, deddieFetchService.fetchCacData(hardwareId,
                "test",
                "test",
                "test"));

        assertTrue(queueService.list().isEmpty());
        assertEquals(lastFetchedAt,
                deviceService.getDeviceConfigValueAsInstant(hardwareId,
                        DeddieConstants.CONFIG_CAC_LAST_FETCHED_AT).orElseThrow());
    }

    @Test
    @SneakyThrows
    void fetchCrpData() {
        String hardwareId = "deddie-test2";
        testUtils.createDevice(hardwareId);

        DeddieCurvesReactivePowerDTO dto = objectMapper.readValue("""
                        {
                            "curveSearchParameters": {
                                "analysisType": 1,
                                "classType": "reactive",
                                "confirmedDataFlag": false,
                                "fromDate": "2025-08-14T09:00:00+00:00",
                                "hourAnalysisFlag": false,
                                "supplyNumber": "123456789",
                                "taxNumber": "987654321",
                                "toDate": "2025-08-18T09:00:00+00:00"
                            },
                            "curves": [
                                {
                                    "certifiedFlag": false,
                                    "consumption": "0.172",
                                    "meterDate": "15/08/2025 00:15"
                                }
                            ]
                        }
                        """
                , DeddieCurvesReactivePowerDTO.class);

        when(deddieClient.getCurvesReactivePower(any(), anyString(), anyString())).thenReturn(dto);

        assertEquals(1, deddieFetchService.fetchCrpData(hardwareId,
                "test",
                "test",
                "test"));
    }

    @Test
    @SneakyThrows
    void fetchCepData() {
        String hardwareId = "deddie-test3";
        testUtils.createDevice(hardwareId);

        DeddieCurvesEnergyProducedDTO dto = objectMapper.readValue("""
                        {
                            "curveSearchParameters": {
                                "analysisType": 1,
                                "classType": "produced",
                                "confirmedDataFlag": false,
                                "fromDate": "2025-08-14T09:00:00+00:00",
                                "hourAnalysisFlag": false,
                                "supplyNumber": "123456789",
                                "taxNumber": "987654321",
                                "toDate": "2025-08-18T09:00:00+00:00"
                            },
                            "curves": [
                                {
                                    "certifiedFlag": false,
                                    "consumption": "0.172",
                                    "meterDate": "15/08/2025 00:15"
                                }
                            ]
                        }
                        """
                , DeddieCurvesEnergyProducedDTO.class);

        when(deddieClient.getCurvesEnergyProduced(any(), anyString(), anyString())).thenReturn(dto);

        assertEquals(1, deddieFetchService.fetchCepData(hardwareId,
                "test",
                "test",
                "test"));

    }

    @Test
    @SneakyThrows
    void fetchCeiData() {

        String hardwareId = "deddie-test4";
        testUtils.createDevice(hardwareId);

        DeddieCurvesEnergyInjectedDTO dto = objectMapper.readValue("""
                        {
                            "curveSearchParameters": {
                                "analysisType": 1,
                                "classType": "injected",
                                "confirmedDataFlag": false,
                                "fromDate": "2025-08-14T09:00:00+00:00",
                                "hourAnalysisFlag": false,
                                "supplyNumber": "123456789",
                                "taxNumber": "987654321",
                                "toDate": "2025-08-18T09:00:00+00:00"
                            },
                            "curves": [
                                {
                                    "certifiedFlag": false,
                                    "consumption": "0.172",
                                    "meterDate": "15/08/2025 00:15"
                                }
                            ]
                        }
                        """
                , DeddieCurvesEnergyInjectedDTO.class);

        when(deddieClient.getCurvesEnergyInjected(any(), anyString(), anyString())).thenReturn(dto);

        assertEquals(1, deddieFetchService.fetchCeiData(hardwareId,
                "test",
                "test",
                "test"));
    }
}