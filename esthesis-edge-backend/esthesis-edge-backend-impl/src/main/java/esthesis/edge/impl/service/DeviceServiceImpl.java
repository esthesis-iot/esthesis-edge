package esthesis.edge.impl.service;

import esthesis.edge.api.dto.DeviceDTO;
import esthesis.edge.api.service.DeviceService;
import esthesis.edge.impl.mapper.DeviceMapper;
import esthesis.edge.impl.model.DeviceEntity;
import esthesis.edge.impl.model.DeviceModuleConfigEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@Transactional
@ApplicationScoped
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

  private final DeviceMapper deviceMapper;

  /**
   * Create a new device. If a device with the same hardwareId already exists, it will be updated
   * (updates only include the module's configuration, any existing configuration will be deleted).
   *
   * @param deviceDTO The device to create.
   * @return The created device.
   */
  @Override
  public DeviceDTO createDevice(DeviceDTO deviceDTO) {
    DeviceEntity deviceEntity =
        DeviceEntity.findByHardwareId(deviceDTO.getHardwareId()).orElse(null);

    // Create or update the device.
    if (deviceEntity == null) {
      deviceEntity = new DeviceEntity();
      deviceEntity.setId(UUID.randomUUID().toString());
      deviceEntity.setHardwareId(deviceDTO.getHardwareId());
      deviceEntity.setModuleName(deviceDTO.getModuleName());
      deviceEntity.setCreatedAt(Instant.now());
      deviceEntity.setEnabled(deviceDTO.getEnabled());
    } else {
      DeviceModuleConfigEntity.deleteConfigForDevice(deviceDTO.getHardwareId());
    }

    // Create the module configuration for the device.
    if (deviceDTO.getModuleConfig() != null && !deviceDTO.getModuleConfig().isEmpty()) {
      deviceEntity.setModuleConfig(new ArrayList<>());
      for (Map.Entry<String, String> entry : deviceDTO.getModuleConfig().entrySet()) {
        DeviceModuleConfigEntity configEntity = new DeviceModuleConfigEntity();
        configEntity.setId(UUID.randomUUID().toString());
        configEntity.setDevice(deviceEntity);
        configEntity.setConfigKey(entry.getKey());
        configEntity.setConfigValue(entry.getValue());

        deviceEntity.getModuleConfig().add(configEntity);
      }
    }

    // Persist the device.
    deviceEntity.persist();

    return deviceMapper.toDTO(deviceEntity);
  }

  @Override
  public void updateDeviceConfig(String hardwareId, String configKey, String cofigValue) {
    DeviceModuleConfigEntity config =
        DeviceModuleConfigEntity.getConfig(hardwareId, configKey).orElse(null);
    DeviceEntity deviceEntity = DeviceEntity.findByHardwareId(hardwareId).orElseThrow();
    if (config == null) {
      config = new DeviceModuleConfigEntity();
      config.setId(UUID.randomUUID().toString());
      config.setDevice(deviceEntity);
      config.setConfigKey(configKey);
    }
    config.setConfigValue(cofigValue);
    if (deviceEntity.getModuleConfig() == null) {
      deviceEntity.setModuleConfig(new ArrayList<>());
    }
    deviceEntity.getModuleConfig().add(config);
    config.persist();
  }

  @Override
  public List<DeviceDTO> listDevices() {
    return deviceMapper.toDTO(DeviceEntity.listAll());
  }

  @Override
  public void deleteDevice(String hardwareId) {
    DeviceEntity.deleteByHardwareId(hardwareId);
  }

  @Override
  public void deleteAllDevices() {
    DeviceEntity.deleteAll();
  }

  @Override
  public long countDevices() {
    return DeviceEntity.count();
  }

  @Override
  public Optional<String> getDeviceConfigValue(String hardwareId, String configKey) {
    return DeviceModuleConfigEntity.findConfigValue(hardwareId, configKey);
  }
}
