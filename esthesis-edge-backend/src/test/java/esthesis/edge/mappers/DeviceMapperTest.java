package esthesis.edge.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import esthesis.edge.dto.DeviceDTO;
import esthesis.edge.model.DeviceEntity;
import esthesis.edge.model.DeviceModuleConfigEntity;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class DeviceMapperTest {

  @Test
  void toDTOFromEntity() {
    DeviceEntity deviceEntity = new DeviceEntity();
    deviceEntity.setHardwareId("hardwareId");
    deviceEntity.setModuleName("moduleName");
    deviceEntity.setPublicKey("publicKey");
    deviceEntity.setPrivateKey("privateKey");
    deviceEntity.setCertificate("certificate");
    deviceEntity.setCreatedAt(Instant.now());
    deviceEntity.setEnabled(true);

    DeviceMapper deviceMapper = new DeviceMapper();
    DeviceDTO deviceDTO = deviceMapper.toDTO(deviceEntity);

    assertNotNull(deviceDTO);
    assertEquals(deviceEntity.getHardwareId(), deviceDTO.getHardwareId());
    assertEquals(deviceEntity.getModuleName(), deviceDTO.getModuleName());
    assertEquals(deviceEntity.getPublicKey(), deviceDTO.getPublicKey());
    assertEquals(deviceEntity.getPrivateKey(), deviceDTO.getPrivateKey());
    assertEquals(deviceEntity.getCertificate(), deviceDTO.getCertificate());
    assertEquals(deviceEntity.getCreatedAt(), deviceDTO.getCreatedAt());
    assertEquals(deviceEntity.getEnabled(), deviceDTO.getEnabled());
  }

  @Test
  void toDTOFromEntityList() {
    DeviceEntity deviceEntity1 = new DeviceEntity();
    deviceEntity1.setHardwareId("hardwareId1");
    deviceEntity1.setModuleName("moduleName1");
    deviceEntity1.setPublicKey("publicKey1");
    deviceEntity1.setPrivateKey("privateKey1");
    deviceEntity1.setCertificate("certificate1");
    deviceEntity1.setCreatedAt(Instant.now());
    deviceEntity1.setEnabled(true);

    DeviceEntity deviceEntity2 = new DeviceEntity();
    deviceEntity2.setHardwareId("hardwareId2");
    deviceEntity2.setModuleName("moduleName2");
    deviceEntity2.setPublicKey("publicKey2");
    deviceEntity2.setPrivateKey("privateKey2");
    deviceEntity2.setCertificate("certificate2");
    deviceEntity2.setCreatedAt(Instant.now());
    deviceEntity2.setEnabled(false);

    List<DeviceEntity> deviceEntities = List.of(deviceEntity1, deviceEntity2);

    DeviceMapper deviceMapper = new DeviceMapper();
    List<DeviceDTO> deviceDTOs = deviceMapper.toDTO(deviceEntities);

    assertNotNull(deviceDTOs);
    assertEquals(2, deviceDTOs.size());

    DeviceDTO deviceDTO1 = deviceDTOs.get(0);
    assertEquals(deviceEntity1.getHardwareId(), deviceDTO1.getHardwareId());
    assertEquals(deviceEntity1.getModuleName(), deviceDTO1.getModuleName());
    assertEquals(deviceEntity1.getPublicKey(), deviceDTO1.getPublicKey());
    assertEquals(deviceEntity1.getPrivateKey(), deviceDTO1.getPrivateKey());
    assertEquals(deviceEntity1.getCertificate(), deviceDTO1.getCertificate());
    assertEquals(deviceEntity1.getCreatedAt(), deviceDTO1.getCreatedAt());
    assertEquals(deviceEntity1.getEnabled(), deviceDTO1.getEnabled());

    DeviceDTO deviceDTO2 = deviceDTOs.get(1);
    assertEquals(deviceEntity2.getHardwareId(), deviceDTO2.getHardwareId());
    assertEquals(deviceEntity2.getModuleName(), deviceDTO2.getModuleName());
    assertEquals(deviceEntity2.getPublicKey(), deviceDTO2.getPublicKey());
    assertEquals(deviceEntity2.getPrivateKey(), deviceDTO2.getPrivateKey());
    assertEquals(deviceEntity2.getCertificate(), deviceDTO2.getCertificate());
    assertEquals(deviceEntity2.getCreatedAt(), deviceDTO2.getCreatedAt());
    assertEquals(deviceEntity2.getEnabled(), deviceDTO2.getEnabled());
  }

  @Test
  void toDTOFromEntityWithConfig() {
    DeviceEntity deviceEntity = new DeviceEntity();
    deviceEntity.setHardwareId("hardwareId");
    deviceEntity.setModuleName("moduleName");
    deviceEntity.setPublicKey("publicKey");
    deviceEntity.setPrivateKey("privateKey");
    deviceEntity.setCertificate("certificate");
    deviceEntity.setCreatedAt(Instant.now());
    deviceEntity.setEnabled(true);
    deviceEntity.setModuleConfig(List.of(
        DeviceModuleConfigEntity.builder()
            .configKey("configKey1")
            .configValue("configValue1")
            .build()
    ));
    DeviceMapper deviceMapper = new DeviceMapper();
    DeviceDTO deviceDTO = deviceMapper.toDTO(deviceEntity);
  }
}
