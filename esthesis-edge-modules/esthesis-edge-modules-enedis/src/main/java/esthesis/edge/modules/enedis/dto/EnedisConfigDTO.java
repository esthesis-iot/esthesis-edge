package esthesis.edge.modules.enedis.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
public class EnedisConfigDTO {
  private Boolean enabled;
  private String cron;
  private String clientId;
  private String clientSecret;
  private String adminSecret;
  private Integer maxDevices;
  private Boolean selfRegistrationEnabled;
  private Boolean selfRegistrationStateChecking;
  private String selfRegistrationWelcomeUrl;
  private String selfRegistrationRedirectUrl;
  private String selfRegistrationDuration;
  private String selfRegistrationPageLogoUrl;
  private String selfRegistrationPageLogoAlt;
  private String selfRegistrationPageRegistrationTitle;
  private String selfRegistrationPageRegistrationMessage;
  private String selfRegistrationPageSuccessTitle;
  private String selfRegistrationPageSuccessMessage;
  private String selfRegistrationPageErrorTitle;
  private String selfRegistrationPageErrorMessage;
  private String selfRegistrationPageLogo1Url;
  private String selfRegistrationPageLogo1Alt;
  private String selfRegistrationPageLogo2Url;
  private String selfRegistrationPageLogo2Alt;
}
