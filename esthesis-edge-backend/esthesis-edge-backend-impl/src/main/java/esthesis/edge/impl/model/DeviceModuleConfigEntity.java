package esthesis.edge.impl.model;

import static org.hibernate.annotations.OnDeleteAction.CASCADE;

import com.fasterxml.jackson.annotation.JsonIgnore;
import esthesis.edge.api.util.EdgeConstants;
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

  @NotBlank
  @Length(max = 255)
  @Column(nullable = false)
  private String configKey;

  @NotBlank
  @Length(max = 1024)
  @Column(length = 1024, nullable = false)
  private String configValue;

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

  public static void deleteConfigForDevice(String hardwareId) {
    DeviceEntity deviceEntity = DeviceEntity.findByHardwareId(hardwareId).orElseThrow();
    DeviceModuleConfigEntity.delete(EdgeConstants.DBCOL_DEVICE, deviceEntity);
  }

  public static Optional<String> findConfigValue(String hardwareId, String configKey) {
    return getConfig(hardwareId, configKey)
        .map(DeviceModuleConfigEntity::getConfigValue);
  }

  public static Optional<DeviceModuleConfigEntity> getConfig(String hardwareId, String configKey) {
    DeviceEntity deviceEntity = DeviceEntity.findByHardwareId(hardwareId).orElseThrow();
    return DeviceModuleConfigEntity.find(
            EdgeConstants.DBCOL_DEVICE + " = ?1 and "+
            EdgeConstants.DBCOL_CONFIG_KEY + " = ?2", deviceEntity, configKey)
        .firstResultOptional();
  }
}
