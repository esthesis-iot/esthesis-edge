package esthesis.edge.impl.service;

import esthesis.edge.api.dto.DeviceDTO;
import esthesis.edge.api.service.DeviceService;
import esthesis.edge.api.service.EsthesisCoreService;
import esthesis.edge.api.util.EdgeProperties;
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
  private final EdgeProperties edgeProperties;
  private final EsthesisCoreService esthesisCoreService;

  public DeviceDTO createDevice(DeviceDTO deviceDTO, List<String> tags) {
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
    }

    // Persist the device.
    deviceEntity.persist();

    // Create the module configuration for the device.
    for (Map.Entry<String, String> entry : deviceDTO.getModuleConfig().entrySet()) {
      updateDeviceConfig(deviceDTO.getHardwareId(), entry.getKey(), entry.getValue());
    }

    // Register the device with esthesis CORE.
    if (edgeProperties.core().registration().enabled()) {
      esthesisCoreService.registerDevice(deviceDTO.getHardwareId(), tags);
    }

    return deviceMapper.toDTO(deviceEntity);
  }

  @Override
  public DeviceDTO createDevice(DeviceDTO deviceDTO) {
    return createDevice(deviceDTO, null);
  }

  @Override
  public boolean disableDevice(String hardwareId) {
    DeviceEntity deviceEntity = DeviceEntity.findByHardwareId(hardwareId).orElseThrow();
    deviceEntity.setEnabled(false);
    deviceEntity.persist();

    return deviceEntity.getEnabled();
  }

  @Override
  public boolean isEnabled(String hardwareId) {
    return DeviceEntity.findByHardwareId(hardwareId).map(DeviceEntity::getEnabled).orElse(false);
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
  public List<DeviceDTO> listDevices(String moduleName) {
    return deviceMapper.toDTO(DeviceEntity.list("moduleName", moduleName));
  }

  @Override
  public List<DeviceDTO> listActiveDevices(String moduleName) {
    return deviceMapper.toDTO(
        DeviceEntity.list("moduleName = ?1 and enabled = ?2", moduleName, true));
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
  public long countDevices(String moduleName) {
    return DeviceEntity.count("moduleName", moduleName);
  }

  @Override
  public Optional<String> getDeviceConfigValueAsString(String hardwareId, String configKey) {
    return DeviceModuleConfigEntity.findConfigValue(hardwareId, configKey);
  }

  @Override
  public Optional<Instant> getDeviceConfigValueAsInstant(String hardwareId, String configKey) {
    return getDeviceConfigValueAsString(hardwareId, configKey).map(Instant::parse);
  }
}
