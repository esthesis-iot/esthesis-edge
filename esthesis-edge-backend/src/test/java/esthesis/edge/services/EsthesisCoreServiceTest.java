package esthesis.edge.services;

import esthesis.common.agent.dto.AgentRegistrationRequest;
import esthesis.common.agent.dto.AgentRegistrationResponse;
import esthesis.edge.TestUtils;
import esthesis.edge.clients.EsthesisAgentServiceClient;
import esthesis.edge.model.DeviceEntity;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
@Transactional
class EsthesisCoreServiceTest {

    @Inject
    EsthesisCoreService esthesisCoreService;

    @InjectMock
    @RestClient
    EsthesisAgentServiceClient esthesisAgentServiceClient;

    @Inject
    TestUtils testUtils;


    @BeforeEach
    void setUp() {
        // Mock the esthesis CORE registration.
        when(esthesisAgentServiceClient.register(any(AgentRegistrationRequest.class)))
                .thenReturn(new AgentRegistrationResponse()
                        .setCertificate("test-certificate")
                        .setPrivateKey("test-private-key")
                        .setMqttServer("test-mqtt-server")
                        .setPublicKey("test-public-key")
                        .setRootCaCertificate("test-root-ca-certificate"));

        testUtils.createDevice("test");
    }

    @Test
    void registerDevice() {
        String hardwareId = "test";
        esthesisCoreService.registerDevice(hardwareId, null);
        DeviceEntity device = DeviceEntity.findByHardwareId(hardwareId).orElseThrow();
        assertNotNull(device);
        assertNotNull(device.getCertificate());
        assertNotNull(device.getPrivateKey());
        assertNotNull(device.getPublicKey());
        assertNotNull(device.getCoreRegisteredAt());
    }
}