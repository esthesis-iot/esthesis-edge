package esthesis.edge.api.service;

import esthesis.edge.api.dto.DeviceDTO;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing devices.
 */
public interface DeviceService {

  /**
   * Create a new device. If the device already exists (based on its hardwareId), only the
   * configuration will be updated.
   *
   * @param deviceDTO the device to create.
   * @return the created device.
   */
  DeviceDTO createDevice(DeviceDTO deviceDTO);

  /**
   * Disables a device.
   *
   * @param hardwareId the hardware id of the device to disable
   * @return the updated status of the device.
   */
  boolean disableDevice(String hardwareId);

  /**
   * Check if a device has status enabled.
   *
   * @param hardwareId the hardware id of the device to check.
   * @return the status of the device.
   */
  boolean isEnabled(String hardwareId);

  /**
   * Create a new device with tags.
   *
   * @param deviceDTO the device to create.
   * @param tags      the tags to associate with the device.
   * @return the created device.
   */
  DeviceDTO createDevice(DeviceDTO deviceDTO, List<String> tags);

  /**
   * Update the configuration of a device.
   *
   * @param deviceId   the ID of the device to update.
   * @param configKey  the key of the configuration to update.
   * @param cofigValue the value of the configuration to update.
   */
  void updateDeviceConfig(String deviceId, String configKey, String cofigValue);

  /**
   * Get all devices.
   *
   * @return a list of all devices.
   */
  List<DeviceDTO> listDevices();

  /**
   * Get all devices created by specific module.
   *
   * @param moduleName the name of the module that created the device.
   * @return a list of all devices for the module.
   */
  List<DeviceDTO> listDevices(String moduleName);

  /**
   * Get all active devices.
   * @param moduleName the name of the module that created the device.
   * @return a list of all active devices for the module.
   */
  List<DeviceDTO> listActiveDevices(String moduleName);

  /**
   * Delete a device by its hardware ID.
   *
   * @param hardwareId the hardware ID of the device.
   */
  void deleteDevice(String hardwareId);

  /**
   * Delete all devices.
   */
  void deleteAllDevices();

  /**
   * Count the number of devices.
   *
   * @return the number of devices.
   */
  long countDevices();

  /**
   * Count the number of devices created by a specific module.
   *
   * @param moduleName the name of the module.
   * @return the number of devices for the module.
   */
  long countDevices(String moduleName);

  /**
   * Get the value of a device configuration.
   *
   * @param hardwareId the hardware ID of the device.
   * @param configKey  the key of the configuration.
   * @return the value of the configuration, if it exists.
   */
  Optional<String> getDeviceConfigValueAsString(String hardwareId, String configKey);

  /**
   * Get the value of a device configuration.
   *
   * @param hardwareId the hardware ID of the device.
   * @param configKey  the key of the configuration.
   * @return the value of the configuration, if it exists.
   */
  Optional<Instant> getDeviceConfigValueAsInstant(String hardwareId, String configKey);

}
