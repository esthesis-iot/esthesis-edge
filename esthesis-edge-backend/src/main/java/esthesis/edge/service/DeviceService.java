package esthesis.edge.service;

import esthesis.edge.model.DeviceEntity;
import esthesis.edge.model.DeviceModuleConfigEntity;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;

@Transactional
@ApplicationScoped
public class DeviceService {

  /**
   * Create a new device. Using this method you can also set the configuration of the device, by
   * providing a map of key-value pairs. Configuration entries will be automatically linked to the
   * device.
   *
   * @param deviceEntity The device to create.
   * @return The created device.
   */
  public DeviceEntity createDevice(DeviceEntity deviceEntity) {
    deviceEntity.getModuleConfig().forEach(config -> config.setDevice(deviceEntity));
    deviceEntity.persist();

    return deviceEntity;
  }

  /**
   * Update the configuration of a device. Existing configurations will be updated, new
   * configurations will be created.
   *
   * @param deviceId The ID of the device to update.
   * @param config   The new configuration.
   */
  public void updateDeviceConfig(String deviceId, Map<String, String> config) {
    DeviceEntity deviceEntity = DeviceEntity.findById(deviceId);
    // If a config exists, update it. If it doesn't exist, create a new one.
    config.forEach((key, value) -> {
      DeviceModuleConfigEntity configEntity = deviceEntity.getModuleConfig().stream()
          .filter(c -> c.getConfigKey().equals(key))
          .findFirst()
          .orElseGet(() -> {
            DeviceModuleConfigEntity newConfig = DeviceModuleConfigEntity.create(key, value);
            newConfig.setDevice(deviceEntity);
            deviceEntity.getModuleConfig().add(newConfig);
            return newConfig;
          });
      configEntity.setConfigValue(value);
    });
  }

  public List<DeviceEntity> listDevices() {
    return DeviceEntity.listAll(Sort.by("hardwareId"));
  }

  public void deleteDeviceById(String deviceId) {
    DeviceEntity.findById(deviceId).delete();
  }

  public void deleteAllDevices() {
    DeviceEntity.deleteAll();
  }
}
