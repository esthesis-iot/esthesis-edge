package esthesis.edge.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.wildfly.common.Assert.assertTrue;

import esthesis.common.agent.dto.AgentRegistrationRequest;
import esthesis.common.agent.dto.AgentRegistrationResponse;
import esthesis.edge.clients.EsthesisAgentServiceClient;
import esthesis.edge.dto.DeviceDTO;
import esthesis.edge.model.DeviceEntity;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


@QuarkusTest
@Transactional
class DeviceServiceTest {

  @Inject
  DeviceService deviceService;

  @InjectMock
  @RestClient
  EsthesisAgentServiceClient esthesisAgentServiceClient;

  @BeforeEach
  public void setup() {
    // Mock the esthesis CORE registration.
    when(esthesisAgentServiceClient.register(any(AgentRegistrationRequest.class)))
        .thenReturn(new AgentRegistrationResponse());
  }

  private DeviceEntity createTestDevice(String hardwareId) {
    DeviceEntity deviceEntity = new DeviceEntity();
    deviceEntity.setId(UUID.randomUUID().toString());
    deviceEntity.setHardwareId(hardwareId);
    deviceEntity.setModuleName("test");
    deviceEntity.setEnabled(true);
    deviceEntity.setCreatedAt(Instant.now());
    deviceEntity.persist();

    return deviceEntity;
  }

  @Test
  void deleteDevice() {
    // Create a test device
    String hardwareId = UUID.randomUUID().toString();
    createTestDevice(hardwareId);

    // Ensure the device exists
    List<DeviceDTO> devicesBeforeDelete = deviceService.listDevices();
    assertNotNull(devicesBeforeDelete);
    assertFalse(devicesBeforeDelete.isEmpty());

    // Delete the device
    deviceService.deleteDevice(hardwareId);

    // Ensure the device is deleted
    List<DeviceDTO> devicesAfterDelete = deviceService.listDevices();
    assertNotNull(devicesAfterDelete);
  }

  @Test
  void deleteAllDevices() {
    createTestDevice(UUID.randomUUID().toString());
    deviceService.deleteAllDevices();

    assertTrue(DeviceEntity.count() == 0);
  }

  @Test
  void countDevices() {
    createTestDevice(UUID.randomUUID().toString());
    assertTrue(deviceService.countDevices() >= 0);
    assertTrue(deviceService.countDevices("test") >= 0);
  }

  @Test
  void createDevice() {
    // Device with no config.
    DeviceDTO deviceDTO = new DeviceDTO();
    deviceDTO.setHardwareId(UUID.randomUUID().toString());
    deviceDTO.setModuleName("test");
    deviceDTO.setEnabled(true);
    assertNotNull(deviceService.createDevice(deviceDTO));

    // Device with config.
    deviceDTO = new DeviceDTO();
    deviceDTO.setHardwareId(UUID.randomUUID().toString());
    deviceDTO.setModuleName("test");
    deviceDTO.setEnabled(true);
    deviceDTO
        .setModuleConfig(Map.of("key", "value"))
        .setModuleConfig(Map.of("key2", "value2"))
        .setModuleConfig(Map.of("key3", "value3"));
    assertNotNull(deviceService.createDevice(deviceDTO));

    // Device with no config with tags.
    deviceDTO = new DeviceDTO();
    deviceDTO.setHardwareId(UUID.randomUUID().toString());
    deviceDTO.setModuleName("test");
    deviceDTO.setEnabled(true);
    assertNotNull(deviceService.createDevice(deviceDTO, List.of("tag1", "tag2")));
  }

  @Test
  void disableDevice() {
    // Create a test device
    String hardwareId = UUID.randomUUID().toString();
    createTestDevice(hardwareId);

    // Ensure the device is enabled
    assertTrue(deviceService.isEnabled(hardwareId));

    // Disable the device
    deviceService.disableDevice(hardwareId);

    // Ensure the device is disabled
    assertFalse(deviceService.isEnabled(hardwareId));
  }

  @Test
  void isEnabled() {
    // Create a test device
    String hardwareId = UUID.randomUUID().toString();
    createTestDevice(hardwareId);

    // Ensure the device is enabled
    assertTrue(deviceService.isEnabled(hardwareId));
  }

  @Test
  void updateDeviceConfig() {
    String hardwareId = UUID.randomUUID().toString();
    createTestDevice(hardwareId);
    deviceService.updateDeviceConfig(hardwareId, "key1", "val1");
    DeviceEntity.findByHardwareId(hardwareId)
        .ifPresent(
            deviceEntity -> {
              assertNotNull(deviceEntity.getModuleConfig());
              assertFalse(deviceEntity.getModuleConfig().isEmpty());
              assertEquals("key1", deviceEntity.getModuleConfig().getFirst().getConfigKey());
              assertEquals("val1", deviceEntity.getModuleConfig().getFirst().getConfigValue());
            });

    deviceService.updateDeviceConfig(hardwareId, "key1", "val2");
    DeviceEntity.findByHardwareId(hardwareId)
        .ifPresent(
            deviceEntity -> {
              assertNotNull(deviceEntity.getModuleConfig());
              assertFalse(deviceEntity.getModuleConfig().isEmpty());
              assertEquals("key1", deviceEntity.getModuleConfig().getFirst().getConfigKey());
              assertEquals("val2", deviceEntity.getModuleConfig().getFirst().getConfigValue());
            });
  }

  @Test
  void listDevices() {
    createTestDevice(UUID.randomUUID().toString());
    List<DeviceDTO> devices = deviceService.listDevices();
    assertNotNull(devices);
    assertFalse(devices.isEmpty());

    devices = deviceService.listDevices("test");
    assertNotNull(devices);
    assertFalse(devices.isEmpty());

    devices = deviceService.listActiveDevices("test");
    assertNotNull(devices);
    assertFalse(devices.isEmpty());
  }

  @Test
  void getDeviceConfigValueAsString() {
    // Create device, then update config, then get config.
    String hardwareId = UUID.randomUUID().toString();
    createTestDevice(hardwareId);
    String keyName = UUID.randomUUID().toString();
    String keyValue = UUID.randomUUID().toString();
    deviceService.updateDeviceConfig(hardwareId, keyName, keyValue);
    assertEquals(keyValue,
        deviceService.getDeviceConfigValueAsString(hardwareId, keyName).orElse(null));

    // Create device with config, then get config.
    String hardwareId2 = UUID.randomUUID().toString();
    DeviceDTO deviceDTO = new DeviceDTO();
    deviceDTO.setHardwareId(hardwareId2);
    deviceDTO.setModuleName("test");
    deviceDTO.setEnabled(true);
    deviceDTO.setModuleConfig(Map.of(keyName, keyValue));
    deviceService.createDevice(deviceDTO);
    assertEquals(keyValue,
        deviceService.getDeviceConfigValueAsString(hardwareId2, keyName).orElse(null));

    String hardwareId3 = UUID.randomUUID().toString();
    keyName = "instant";
    Instant keyValueInstant = Instant.now();
    DeviceDTO deviceDTO2 = new DeviceDTO();
    deviceDTO2.setHardwareId(hardwareId3);
    deviceDTO2.setModuleName("test");
    deviceDTO2.setEnabled(true);
    deviceDTO2.setModuleConfig(Map.of(keyName, keyValueInstant.toString()));
    deviceService.createDevice(deviceDTO2);
    assertEquals(keyValueInstant,
        deviceService.getDeviceConfigValueAsInstant(hardwareId3, keyName).orElse(null));
  }

}
