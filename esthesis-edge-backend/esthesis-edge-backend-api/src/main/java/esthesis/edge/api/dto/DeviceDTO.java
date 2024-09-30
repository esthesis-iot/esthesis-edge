package esthesis.edge.api.dto;

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
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
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

  @Singular("config")
  private Map<String, String> moduleConfig;

}
