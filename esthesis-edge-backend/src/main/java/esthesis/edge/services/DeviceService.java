package esthesis.edge.services;

import esthesis.edge.config.EdgeProperties;
import esthesis.edge.dto.DeviceDTO;
import esthesis.edge.mappers.DeviceMapper;
import esthesis.edge.model.DeviceEntity;
import esthesis.edge.model.DeviceModuleConfigEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for managing devices.
 */
@Slf4j
@Transactional
@ApplicationScoped
@RequiredArgsConstructor
public class DeviceService {

  private final DeviceMapper deviceMapper;
  private final EdgeProperties edgeProperties;
  private final EsthesisCoreService esthesisCoreService;

  /**
   * Creates a new device.
   *
   * @param deviceDTO The device to create.
   * @param tags      The tags to associate with the device.
   * @return The created device.
   */
  public DeviceDTO createDevice(DeviceDTO deviceDTO, List<String> tags) {
    DeviceEntity deviceEntity =
            DeviceEntity.findByHardwareId(deviceDTO.getHardwareId()).orElse(null);

    boolean newDevice = deviceEntity == null;

    // Create or update the device.
    if (newDevice) {
      deviceEntity = new DeviceEntity();
      deviceEntity.setId(UUID.randomUUID().toString());
      deviceEntity.setHardwareId(deviceDTO.getHardwareId());
      deviceEntity.setModuleName(deviceDTO.getModuleName());
      deviceEntity.setCreatedAt(Instant.now());
      deviceEntity.setEnabled(deviceDTO.getEnabled());
      if (tags != null && !tags.isEmpty()) {
        deviceEntity.setTags(String.join(",", tags));
      }
    }

    // Persist the device.
    deviceEntity.persist();

    if (newDevice) {
      log.info("Device with hardwareId '{}' created.", deviceDTO.getHardwareId());
    } else {
      log.info("Device with hardwareId '{}' updated.", deviceDTO.getHardwareId());
    }


    // Create the module configuration for the device.
    if (deviceDTO.getModuleConfig() != null) {
      for (Map.Entry<String, String> entry : deviceDTO.getModuleConfig().entrySet()) {
        updateDeviceConfig(deviceDTO.getHardwareId(), entry.getKey(), entry.getValue());
      }
    }

    // Register the device with esthesis CORE.
    if (edgeProperties.core().registration().enabled()) {
      esthesisCoreService.registerDevice(deviceDTO.getHardwareId(), tags);
    }

    return deviceMapper.toDTO(deviceEntity);
  }

  /**
   * Creates a new device.
   *
   * @param deviceDTO The device to create.
   * @return The created device.
   */
  public DeviceDTO createDevice(DeviceDTO deviceDTO) {
    return createDevice(deviceDTO, null);
  }

  /**
   * Disables a device.
   *
   * @param hardwareId The hardware ID of the device to disable.
   * @return True if the device was disabled, false otherwise.
   */
  public boolean disableDevice(String hardwareId) {
    DeviceEntity deviceEntity = DeviceEntity.findByHardwareId(hardwareId).orElseThrow();
    deviceEntity.setEnabled(false);
    deviceEntity.persist();

    return deviceEntity.getEnabled();
  }

  /**
   * Checks if a device is enabled.
   *
   * @param hardwareId The hardware ID of the device to enable.
   * @return True if the device was enabled, false otherwise.
   */
  public boolean isEnabled(String hardwareId) {
    return DeviceEntity.findByHardwareId(hardwareId).map(DeviceEntity::getEnabled).orElse(false);
  }

  /**
   * Updates the configuration option for a device.
   *
   * @param hardwareId The hardware ID of the device.
   * @param configKey  The configuration key.
   * @param cofigValue The configuration value.
   */
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

  /**
   * Lists all devices.
   *
   * @return The list of devices.
   */
  public List<DeviceDTO> listDevices() {
    return deviceMapper.toDTO(DeviceEntity.listAll());
  }

  /**
   * Lists all devices created by a specific module.
   *
   * @param moduleName The module name.
   * @return The list of devices.
   */
  public List<DeviceDTO> listDevices(String moduleName) {
    return deviceMapper.toDTO(DeviceEntity.list("moduleName", moduleName));
  }

  /**
   * Lists all active devices for a specific module.
   *
   * @param moduleName The module name.
   * @return The list of active devices.
   */
  public List<DeviceDTO> listActiveDevices(String moduleName) {
    return deviceMapper.toDTO(
        DeviceEntity.list("moduleName = ?1 and enabled = ?2", moduleName, true));
  }

  /**
   * Deletes a device by its hardware ID.
   *
   * @param hardwareId The hardware ID of the device to delete.
   */
  public void deleteDevice(String hardwareId) {
    DeviceEntity.deleteByHardwareId(hardwareId);
  }

  /**
   * Deletes all devices.
   */
  public void deleteAllDevices() {
    DeviceEntity.deleteAll();
  }

  /**
   * Counts all devices.
   *
   * @return The number of devices.
   */
  public long countDevices() {
    return DeviceEntity.count();
  }

  /**
   * Counts all devices created by a specific module.
   *
   * @param moduleName The module name.
   * @return The number of devices.
   */
  public long countDevices(String moduleName) {
    return DeviceEntity.count("moduleName", moduleName);
  }

  /**
   * Gets a configuration value for a device.
   *
   * @param hardwareId The hardware ID of the device.
   * @param configKey  The configuration key to search for.
   */
  public Optional<String> getDeviceConfigValueAsString(String hardwareId, String configKey) {
    return DeviceModuleConfigEntity.findConfigValue(hardwareId, configKey);
  }

  /**
   * Gets a configuration value for a device as an Instant.
   *
   * @param hardwareId The hardware ID of the device.
   * @param configKey  The configuration key to search for.
   */
  public Optional<Instant> getDeviceConfigValueAsInstant(String hardwareId, String configKey) {
    return getDeviceConfigValueAsString(hardwareId, configKey).map(Instant::parse);
  }

  /**
   * Lists all devices pending registration with esthesis CORE.
   *
   * @param moduleName The module name.
   * @return The list of devices.
   */
  public List<DeviceDTO> listDevicesPendingCoreRegistration(String moduleName) {
    return deviceMapper.toDTO(DeviceEntity.list("moduleName = ?1 and coreRegisteredAt IS NULL", moduleName));
  }
}
