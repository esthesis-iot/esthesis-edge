package esthesis.edge.modules.fronius.service;

import esthesis.edge.services.DeviceService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class FroniusServiceTest {

    @Inject
    FroniusService froniusService;

    @InjectMock
    FroniusFetchService froniusFetchService;

    @Inject
    DeviceService deviceService;

    @BeforeEach
    void setUp() {
        deviceService.deleteAllDevices();
        when(froniusFetchService.fetchPowerFlowRealtimeData()).thenReturn(0);
    }

    @Test
    void fetchData() {
        assertDoesNotThrow(() -> froniusService.fetchData());
        verify(froniusFetchService).fetchPowerFlowRealtimeData();
    }

    @Test
    void updateDevices() {
        // Verify that the device list is empty before the update.
        assertEquals(0, deviceService.countDevices());

        // Run the updateDevices method to register devices.
        froniusService.updateDevices();

        // Verify that the new device was registered.
        assertTrue(deviceService.countDevices() > 0);

    }
}