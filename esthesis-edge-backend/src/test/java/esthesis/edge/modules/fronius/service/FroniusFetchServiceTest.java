package esthesis.edge.modules.fronius.service;

import esthesis.edge.modules.fronius.FroniusTestUtils;
import esthesis.edge.modules.fronius.client.FroniusClient;
import esthesis.edge.services.DeviceService;
import esthesis.edge.services.QueueService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@QuarkusTest
class FroniusFetchServiceTest {

    @Inject
    FroniusFetchService froniusFetchService;

    @InjectMock
    @RestClient
    FroniusClient froniusClient;

    @Inject
    QueueService queueService;

    @Inject
    DeviceService deviceService;

    @Inject
    FroniusTestUtils froniusTestUtils;

    @Inject
    FroniusService froniusService;


    @BeforeEach
    @Transactional
    void setUp() {
        queueService.list().forEach(queue -> queueService.remove(queue.getId()));
        deviceService.deleteAllDevices();

    }

    @Test
    void fetchPowerFlowRealtimeData() {

        froniusService.updateDevices();

        // Mock the FroniusClient to return a valid response.
        when(froniusClient.getPowerFlowRealtimeData()).thenReturn(froniusTestUtils.createFroniusPowerFlowRealtimeDataDTO());

        // Verify the queue size before fetching data.
        assertTrue(queueService.list().isEmpty());

        // Perform the fetch operation.
        int queuedItems = froniusFetchService.fetchPowerFlowRealtimeData();

        // Verify the queue size after fetching data.
        assertEquals(1, queuedItems);

        // Verify the content of the queued item.
        assertEquals("energy dailyProduction=15912i 2025-03-13T11:01:41Z", queueService.peek().orElseThrow().getDataObject());

    }

}