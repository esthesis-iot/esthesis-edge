package esthesis.edge.model;

import static jakarta.persistence.FetchType.EAGER;

import com.fasterxml.jackson.annotation.JsonInclude;
import esthesis.edge.config.EdgeConstants;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import org.hibernate.validator.constraints.Length;

/**
 * Represents a device that is managed by EDGE.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "device", indexes = {
    @Index(name = "idx_hardwareId", columnList = "hardware_id"),
    @Index(name = "idx_module", columnList = "module_name"),
    @Index(name = "idx_enabled", columnList = "enabled"),
    @Index(name = "idx_module_enabled", columnList = "module_name, enabled"),
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DeviceEntity extends PanacheEntityBase {

  // The unique identifier for the device.
  @Id
  @NotBlank
  private String id;

  // The unique identifier for the hardware id of the device.
  @NotEmpty
  @Length(min = 3, max = 512)
  @Column(unique = true, length = 512, nullable = false, name = "hardware_id")
  @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Only alphanumeric characters, hyphens, and underscores are allowed.")
  private String hardwareId;

  // The name of the module that created this device.
  @NotEmpty
  @Length(min = 3, max = 255)
  @Column(nullable = false, name = "module_name")
  private String moduleName;

  // The public key for the device.
  @Column(length = 4096, name = "public_key")
  private String publicKey;

  // The private key for the device.
  @Column(length = 4096, name = "private_key")
  private String privateKey;

  // The certificate for the device.
  @Column(length = 4096)
  private String certificate;

  // The date and time when the device was created.
  @NotNull
  @Column(nullable = false, name = "created_at")
  private Instant createdAt;

  // Whether this disable is enabled or not (disabled devices are ignored when fetching data).
  @NotNull
  @Column(nullable = false)
  private Boolean enabled;

  // Module-specific device configuration options.
  @Singular("config")
  @Column(name = "module_config")
  @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true, fetch = EAGER)
  private List<DeviceModuleConfigEntity> moduleConfig;

  // The date and time when the device was registered with esthesis CORE.
  @Column(name = "core_registered_at")
  private Instant coreRegisteredAt;

  // The tags to associate with the device separated by commas.
  @Column(length = 512, name = "tags")
  private String tags;

  /**
   * Finds a device by its hardware ID.
   *
   * @param hardwareId The hardware ID of the device.
   * @return The device entity, if found.
   */
  public static Optional<DeviceEntity> findByHardwareId(String hardwareId) {
    return find(EdgeConstants.DBCOL_HARDWARE_ID, hardwareId).firstResultOptional();
  }

  /**
   * Deletes a device by its hardwareID.
   *
   * @param hardwareId The hardware ID of the device.
   */
  public static void deleteByHardwareId(String hardwareId) {
    delete(EdgeConstants.DBCOL_HARDWARE_ID, hardwareId);
  }
}
