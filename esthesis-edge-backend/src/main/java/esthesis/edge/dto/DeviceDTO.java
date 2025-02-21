package esthesis.edge.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

/**
 * A DTO representing an EDGE device.
 */
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DeviceDTO {

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

  // The configuration for the module that created this device.
  @Singular("config")
  private Map<String, String> moduleConfig;

  // The date and time when the device was registered with esthesis CORE.
  private Instant coreRegisteredAt;

  // The tags associated with the device separated by commas.
  private String tags;

}
