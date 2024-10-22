package esthesis.edge;

import esthesis.edge.model.DeviceEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class TestUtils {

  /**
   * Create a device with the given hardware ID.
   *
   * @param hardwareId the hardware ID of the device to create.
   * @return the created device entity.
   */
  @Transactional
  public DeviceEntity createDevice(String hardwareId) {
    DeviceEntity deviceEntity = new DeviceEntity();
    deviceEntity.setId(UUID.randomUUID().toString());
    deviceEntity.setHardwareId(hardwareId);
    deviceEntity.setModuleName("test");
    deviceEntity.setEnabled(true);
    deviceEntity.setCreatedAt(Instant.now());
    deviceEntity.persist();

    return deviceEntity;
  }
}
