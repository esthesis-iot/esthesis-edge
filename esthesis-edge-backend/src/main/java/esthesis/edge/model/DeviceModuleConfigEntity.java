package esthesis.edge.model;

import static org.hibernate.annotations.OnDeleteAction.CASCADE;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
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
  private String configKey;

  @NotBlank
  @Length(max = 1024)
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
}
