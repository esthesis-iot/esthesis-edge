package esthesis.edge.modules.enedis.dto.datahub;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.Instant;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString
@NoArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
public class EnedisAuthTokenDTO {
  @JsonProperty("access_token")
  private String accessToken;
  private String scope;
  @JsonProperty("token_type")
  private String tokenType;
  @JsonProperty("expires_in")
  private Instant expiresOn;

  public void setExpiresOn(int expiresIn) {
    this.expiresOn = Instant.now().plusSeconds(expiresIn);
  }
}
