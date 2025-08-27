package esthesis.edge.modules.deddie.service;

import esthesis.edge.TestUtils;
import esthesis.edge.model.DeviceEntity;
import esthesis.edge.modules.deddie.client.DeddieClient;
import esthesis.edge.services.DeviceService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Slf4j
@QuarkusTest
class DeddieServiceTest {

    @Inject
    DeddieService deddieService;

    @InjectMock
    @RestClient
    DeddieClient deddieClient;

    @InjectMock
    DeddieFetchService deddieFetchService;
    @Inject
    TestUtils testUtils;
    @Inject
    DeviceService deviceService;

    @BeforeEach
    @Transactional
    void setUp() {
        DeviceEntity.deleteAll();
    }

    @Test
    void createDevices() {
        String taxNumber = "0123456789";
        String supplyNumber = "1234567890";

        when(deddieClient.retrieveSuppliesList(anyString(), anyString(), anyString()))
                .thenReturn(List.of("1234567890  (16/01/2025-14/08/2025)"));

        deddieService.createDevices("testAccessToken", taxNumber, List.of(supplyNumber));

        String expectedHardwareId = "DEDDIE-" + taxNumber + "-" + supplyNumber;

        assertNotNull(DeviceEntity.findByHardwareId(expectedHardwareId));
    }

    @Test
    void fetchData() {
        String hardwareId = UUID.randomUUID().toString();
        testUtils.createDevice(hardwareId);

        when(deddieFetchService.fetchCepData(anyString(), anyString(), anyString(), anyString())).thenReturn(0);
        when(deddieFetchService.fetchCeiData(anyString(), anyString(), anyString(), anyString())).thenReturn(0);
        when(deddieFetchService.fetchCrpData(anyString(), anyString(), anyString(), anyString())).thenReturn(0);
        when(deddieFetchService.fetchCeiData(anyString(), anyString(), anyString(), anyString())).thenReturn(0);


        assertDoesNotThrow(() -> deddieService.fetchData());

    }
}