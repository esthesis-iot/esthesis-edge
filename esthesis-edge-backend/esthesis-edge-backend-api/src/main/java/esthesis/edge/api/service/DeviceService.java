package esthesis.edge.api.service;

import esthesis.edge.api.dto.DeviceDTO;
import java.util.List;
import java.util.Optional;


public interface DeviceService {

  DeviceDTO createDevice(DeviceDTO deviceDTO);

  DeviceDTO createDevice(DeviceDTO deviceDTO, List<String> tags);

  void updateDeviceConfig(String deviceId, String configKey, String cofigValue);

  List<DeviceDTO> listDevices();

  void deleteDevice(String hardwareId);

  void deleteAllDevices();

  long countDevices();

  Optional<String> getDeviceConfigValue(String hardwareId, String configKey);
}
