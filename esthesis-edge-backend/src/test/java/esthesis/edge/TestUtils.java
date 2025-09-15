package esthesis.edge;

import esthesis.edge.dto.QueueItemDTO;
import esthesis.edge.model.DeviceEntity;
import esthesis.edge.model.DeviceModuleConfigEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class TestUtils {

  @Transactional
  public void createDevice(String hardwareId, String moduleName) {
    createDevice(hardwareId);
    DeviceEntity deviceEntity = DeviceEntity.findByHardwareId(hardwareId).orElseThrow();
    deviceEntity.setModuleName(moduleName);
    deviceEntity.persist();

  }

  @Transactional
  public DeviceEntity createDevice(String hardwareId) {
    DeviceEntity deviceEntity = new DeviceEntity();
    deviceEntity.setId(UUID.randomUUID().toString());
    deviceEntity.setHardwareId(hardwareId);
    deviceEntity.setModuleName("test");
    deviceEntity.setEnabled(true);
    deviceEntity.setCreatedAt(Instant.now());
    deviceEntity.setCoreRegisteredAt(Instant.now());
    deviceEntity.persist();

    return deviceEntity;
  }

  @Transactional
  public void createDeviceWithoutCoreRegistration(String hardwareId) {
    DeviceEntity device =  this.createDevice(hardwareId);
    device.setCoreRegisteredAt(null);
    device.persist();
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

  @Transactional
  public QueueItemDTO createQueueItem(String id, String hardwareId, String dataObject) {
    QueueItemDTO queueItem = new QueueItemDTO();
    queueItem.setId(id);
    queueItem.setHardwareId(hardwareId);
    queueItem.setDataObject(dataObject);
    queueItem.setProcessedLocalAt(null);
    queueItem.setProcessedCoreAt(null);
    queueItem.setCreatedAt(Instant.now());

    return queueItem;
  }
}
