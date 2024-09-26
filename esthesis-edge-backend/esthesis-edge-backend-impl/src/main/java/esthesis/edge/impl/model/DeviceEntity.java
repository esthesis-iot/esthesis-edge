package esthesis.edge.impl.model;

import static jakarta.persistence.FetchType.EAGER;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import org.hibernate.validator.constraints.Length;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "device")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DeviceEntity extends PanacheEntityBase {
  // The unique identifier for the device.
  @Id
  @NotBlank
  private String id;

  // The unique identifier for the hardware id of the device.
  @NotEmpty
  @Length(min = 3, max = 512)
  @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Only alphanumeric characters, hyphens, and underscores are allowed.")
  private String hardwareId;

  // The name of the module that created this device.
  @NotEmpty
  @Length(min = 3, max = 255)
  private String moduleName;

  // The public key for the device.
  private String publicKey;

  // The private key for the device.
  private String privateKey;

  // The certificate for the device.
  private String certificate;

  // The date and time when the device was created.
  @NotNull
  private Instant createdAt;

  // Whether this disable is enabled or not (disabled devices are ignored when syncing data).
  @NotNull
  private Boolean enabled;

  @Singular("config")
  @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true, fetch = EAGER)
  private List<DeviceModuleConfigEntity> moduleConfig;
}
