package esthesis.edge.mappers;

import esthesis.edge.dto.DeviceDTO;
import esthesis.edge.dto.DeviceDTO.DeviceDTOBuilder;
import esthesis.edge.model.DeviceEntity;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Helper mappers for DeviceEntity.
 */
@ApplicationScoped
public class DeviceMapper {

  /**
   * Convert DeviceDTO to DeviceEntity.
   *
   * @param deviceEntity the DeviceEntity to convert.
   * @return the DeviceDTO.
   */
  public DeviceDTO toDTO(DeviceEntity deviceEntity) {
    DeviceDTOBuilder deviceDTOBuilder = DeviceDTO.builder()
            .hardwareId(deviceEntity.getHardwareId())
            .moduleName(deviceEntity.getModuleName())
            .publicKey(deviceEntity.getPublicKey())
            .privateKey(deviceEntity.getPrivateKey())
            .certificate(deviceEntity.getCertificate())
            .createdAt(deviceEntity.getCreatedAt())
            .tags(deviceEntity.getTags())
            .coreRegisteredAt(deviceEntity.getCoreRegisteredAt())
            .enabled(deviceEntity.getEnabled());
    if (deviceEntity.getModuleConfig() != null && !deviceEntity.getModuleConfig().isEmpty()) {
      deviceEntity.getModuleConfig().forEach(
              config -> deviceDTOBuilder.config(config.getConfigKey(), config.getConfigValue()));
    }

    return deviceDTOBuilder.build();
  }

  /**
   * Convert DeviceEntity to DeviceDTO.
   *
   * @param deviceEntities the DeviceEntities to convert.
   * @return the DeviceDTO.
   */
  public List<DeviceDTO> toDTO(List<DeviceEntity> deviceEntities) {
    return deviceEntities.stream().map(this::toDTO).toList();
  }
}
