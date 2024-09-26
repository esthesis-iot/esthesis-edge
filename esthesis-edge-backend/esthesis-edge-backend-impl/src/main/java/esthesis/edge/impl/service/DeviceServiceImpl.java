package esthesis.edge.impl.service;

import esthesis.edge.api.dto.DeviceDTO;
import esthesis.edge.api.service.DeviceService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;

@Transactional
@ApplicationScoped
public class DeviceServiceImpl implements DeviceService {

  @Override
  public DeviceDTO createDevice(DeviceDTO deviceDTO) {
    return null;
  }

  @Override
  public void updateDeviceConfig(String deviceId, Map<String, String> config) {

  }

  @Override
  public List<DeviceDTO> listDevices() {
    return List.of();
  }

  @Override
  public void deleteDeviceById(String deviceId) {

  }

  @Override
  public void deleteAllDevices() {

  }
}
