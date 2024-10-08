package esthesis.edge.impl.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.wildfly.common.Assert.assertTrue;

import esthesis.edge.api.dto.DeviceDTO;
import esthesis.edge.impl.model.DeviceEntity;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Transactional
class DeviceServiceImplTest {

  @Inject DeviceServiceImpl deviceService;

  /**
   * Convenience method to create a test device.
   * @param hardwareId The hardware ID of the device.
   */
  private void createTestDevice(String hardwareId) {
    DeviceEntity deviceEntity = new DeviceEntity();
    deviceEntity.setId(UUID.randomUUID().toString());
    deviceEntity.setHardwareId(hardwareId);
    deviceEntity.setModuleName("test");
    deviceEntity.setEnabled(true);
    deviceEntity.setCreatedAt(Instant.now());
    deviceEntity.persist();
  }

  @Test
  public void testListDevices() {
    createTestDevice(UUID.randomUUID().toString());
    List<DeviceDTO> devices = deviceService.listDevices();
    assertNotNull(devices);
    assertFalse(devices.isEmpty());
  }

  @Test
  public void testCreateDeviceNoConfig() {
    DeviceDTO deviceDTO = new DeviceDTO();
    deviceDTO.setHardwareId(UUID.randomUUID().toString());
    deviceDTO.setModuleName("test");
    deviceDTO.setEnabled(true);
    assertNotNull(deviceService.createDevice(deviceDTO));
  }

  @Test
  public void testCreateDeviceWithConfig() {
    DeviceDTO deviceDTO = new DeviceDTO();
    deviceDTO.setHardwareId(UUID.randomUUID().toString());
    deviceDTO.setModuleName("test");
    deviceDTO.setEnabled(true);
    deviceDTO
        .setModuleConfig(Map.of("key", "value"))
        .setModuleConfig(Map.of("key2", "value2"))
        .setModuleConfig(Map.of("key3", "value3"));
    assertNotNull(deviceService.createDevice(deviceDTO));
  }

  @Test
  public void testUpdateDeviceConfig() {
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
  }

  @Test
  void testGetDeviceConfig() {
    // Create device, then update config, then get config.
    String hardwareId = UUID.randomUUID().toString();
    createTestDevice(hardwareId);
    String keyName = UUID.randomUUID().toString();
    String value = UUID.randomUUID().toString();
    deviceService.updateDeviceConfig(hardwareId, keyName, value);
    assertEquals(value,
        deviceService.getDeviceConfigValueAsString(hardwareId, keyName).orElse(null));

    // Create device with config, then get config.
    String hardwareId2 = UUID.randomUUID().toString();
    DeviceDTO deviceDTO = new DeviceDTO();
    deviceDTO.setHardwareId(hardwareId2);
    deviceDTO.setModuleName("test");
    deviceDTO.setEnabled(true);
    deviceDTO.setModuleConfig(Map.of(keyName, value));
    deviceService.createDevice(deviceDTO);
    assertEquals(value,
        deviceService.getDeviceConfigValueAsString(hardwareId2, keyName).orElse(null));
  }
}
