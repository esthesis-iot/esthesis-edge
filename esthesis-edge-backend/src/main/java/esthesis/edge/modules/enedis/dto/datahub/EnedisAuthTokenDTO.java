package esthesis.edge.modules.enedis.dto.datahub;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.Instant;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * A DTO representing the authentication token received from Enedis.
 */
@Data
@ToString
@NoArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
public class EnedisAuthTokenDTO {

  // The access token.
  @JsonProperty("access_token")
  private String accessToken;

  // The scope of the token.
  private String scope;

  // The type of the token.
  @JsonProperty("token_type")
  private String tokenType;

  // The time when the token expires.
  @JsonProperty("expires_in")
  private Instant expiresOn;

  /**
   * Sets the time when the token expires.
   *
   * @param expiresIn The number of seconds until the token expires.
   */
  public void setExpiresOn(int expiresIn) {
    this.expiresOn = Instant.now().plusSeconds(expiresIn);
  }
}
