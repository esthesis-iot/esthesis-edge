package esthesis.edge.api.service;

import esthesis.edge.api.dto.DeviceDTO;
import java.util.List;
import java.util.Map;


public interface DeviceService {

  DeviceDTO createDevice(DeviceDTO deviceDTO);

  void updateDeviceConfig(String deviceId, Map<String, String> config);

  List<DeviceDTO> listDevices();

  void deleteDeviceById(String deviceId);

  void deleteAllDevices();
}
