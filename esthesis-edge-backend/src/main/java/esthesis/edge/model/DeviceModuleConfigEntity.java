package esthesis.edge.model;

import static org.hibernate.annotations.OnDeleteAction.CASCADE;

import com.fasterxml.jackson.annotation.JsonIgnore;
import esthesis.edge.config.EdgeConstants;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.validator.constraints.Length;

/**
 * Represents the configuration of a module of a device.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "device_module_config")
public class DeviceModuleConfigEntity extends PanacheEntityBase {

  @Id
  @NotBlank
  @Length(max = 36)
  private String id;

  // The name of the configuration value (key).
  @NotBlank
  @Length(max = 255)
  @Column(nullable = false, name = "config_key")
  private String configKey;

  // The value of the configuration.
  @NotBlank
  @Length(max = 1024)
  @Column(length = 1024, nullable = false, name = "config_value")
  private String configValue;

  // The device to which this configuration belongs.
  @JsonIgnore
  @OnDelete(action = CASCADE)
  @ManyToOne(cascade = CascadeType.ALL)
  private DeviceEntity device;

  /**
   * A helper method to create a new instance of the {@link DeviceModuleConfigEntity}. Note that you
   * need to set the {@link DeviceEntity} to the returned instance.
   *
   * @param key   The key of the configuration.
   * @param value The value of the configuration.
   * @return A new instance of the {@link DeviceModuleConfigEntity}.
   */
  public static DeviceModuleConfigEntity create(String key, String value) {
    return DeviceModuleConfigEntity.builder()
        .id(UUID.randomUUID().toString())
        .configKey(key)
        .configValue(value)
        .build();
  }

  /**
   * Deletes all configurations for a device.
   *
   * @param hardwareId The hardware ID of the device.
   */
  public static void deleteConfigForDevice(String hardwareId) {
    DeviceEntity deviceEntity = DeviceEntity.findByHardwareId(hardwareId).orElseThrow();
    DeviceModuleConfigEntity.delete(EdgeConstants.DBCOL_DEVICE, deviceEntity);
  }

  /**
   * Finds the configuration value for a given hardware ID and configuration key.
   *
   * @param hardwareId The hardware ID of the device.
   * @param configKey  The configuration key.
   * @return The configuration value if it exists.
   */
  public static Optional<String> findConfigValue(String hardwareId, String configKey) {
    return getConfig(hardwareId, configKey)
        .map(DeviceModuleConfigEntity::getConfigValue);
  }

  /**
   * Updates the configuration value for a given hardware ID and configuration key.
   *
   * @param hardwareId The hardware ID of the device.
   * @param key        The configuration key.
   * @param newValue   The new value of the configuration.
   */
  public static void updateConfigValue(String hardwareId, String key, String newValue) {
    Optional<DeviceModuleConfigEntity> config = getConfig(hardwareId, key);
    config.ifPresent(
        deviceModuleConfigEntity -> deviceModuleConfigEntity.setConfigValue(newValue));
  }

  /**
   * Finds the configuration for a given hardware ID and configuration key.
   *
   * @param hardwareId The hardware ID of the device.
   * @param configKey  The configuration key.
   * @return The configuration if it exists.
   */
  public static Optional<DeviceModuleConfigEntity> getConfig(String hardwareId, String configKey) {
    DeviceEntity deviceEntity = DeviceEntity.findByHardwareId(hardwareId).orElseThrow();
    return DeviceModuleConfigEntity.find(
            EdgeConstants.DBCOL_DEVICE + " = ?1 and " +
                EdgeConstants.DBCOL_CONFIG_KEY + " = ?2", deviceEntity, configKey)
        .firstResultOptional();
  }
}
