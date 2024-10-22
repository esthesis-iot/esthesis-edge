package esthesis.edge;

import esthesis.edge.model.DeviceEntity;
import esthesis.edge.model.DeviceModuleConfigEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class TestUtils {

  @Transactional
  public DeviceEntity createDevice(String hardwareId, String moduleName) {
    createDevice(hardwareId);
    DeviceEntity deviceEntity = DeviceEntity.findByHardwareId(hardwareId).orElseThrow();
    deviceEntity.setModuleName(moduleName);
    deviceEntity.persist();

    return deviceEntity;
  }

  @Transactional
  public DeviceEntity createDevice(String hardwareId) {
    DeviceEntity deviceEntity = new DeviceEntity();
    deviceEntity.setId(UUID.randomUUID().toString());
    deviceEntity.setHardwareId(hardwareId);
    deviceEntity.setModuleName("test");
    deviceEntity.setEnabled(true);
    deviceEntity.setCreatedAt(Instant.now());
    deviceEntity.persist();

    return deviceEntity;
  }

  @Transactional
  public void setDeviceConfig(String hardwareId, String configKey, String configValue) {
    DeviceEntity deviceEntity = DeviceEntity.findByHardwareId(hardwareId).orElseThrow();

    DeviceModuleConfigEntity configEntity = new DeviceModuleConfigEntity();
    configEntity.setId(UUID.randomUUID().toString());
    configEntity.setConfigKey(configKey);
    configEntity.setConfigValue(configValue);
    configEntity.setDevice(deviceEntity);
    deviceEntity.getModuleConfig().add(configEntity);

    deviceEntity.persist();
  }
}
