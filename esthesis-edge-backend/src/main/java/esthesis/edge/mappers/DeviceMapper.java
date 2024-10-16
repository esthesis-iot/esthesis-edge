package esthesis.edge.mappers;

import esthesis.edge.dto.DeviceDTO;
import esthesis.edge.dto.DeviceDTO.DeviceDTOBuilder;
import esthesis.edge.model.DeviceEntity;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class DeviceMapper {

  public DeviceDTO toDTO(DeviceEntity deviceEntity) {
    DeviceDTOBuilder deviceDTOBuilder = DeviceDTO.builder()
        .hardwareId(deviceEntity.getHardwareId())
        .moduleName(deviceEntity.getModuleName())
        .publicKey(deviceEntity.getPublicKey())
        .privateKey(deviceEntity.getPrivateKey())
        .certificate(deviceEntity.getCertificate())
        .createdAt(deviceEntity.getCreatedAt())
        .enabled(deviceEntity.getEnabled());
    if (deviceEntity.getModuleConfig() != null && !deviceEntity.getModuleConfig().isEmpty()) {
      deviceEntity.getModuleConfig().forEach(
          config -> deviceDTOBuilder.config(config.getConfigKey(), config.getConfigValue()));
    }

    return deviceDTOBuilder.build();
  }

  public List<DeviceDTO> toDTO(List<DeviceEntity> deviceEntities) {
    return deviceEntities.stream().map(this::toDTO).collect(Collectors.toList());
  }
}
