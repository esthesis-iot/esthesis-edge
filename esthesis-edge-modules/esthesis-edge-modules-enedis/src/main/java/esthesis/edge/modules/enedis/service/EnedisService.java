package esthesis.edge.modules.enedis.service;

import static esthesis.edge.api.util.EdgeConstants.EDGE;
import static esthesis.edge.modules.enedis.EnedisConstants.MODULE_NAME;

import esthesis.edge.api.dto.DeviceDTO;
import esthesis.edge.api.dto.TemplateDTO;
import esthesis.edge.api.service.DeviceService;
import esthesis.edge.modules.enedis.EnedisConstants;
import esthesis.edge.modules.enedis.EnedisProperties;
import esthesis.edge.modules.enedis.dto.EnedisConfigDTO;
import esthesis.edge.modules.enedis.templates.EnedisTemplates;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

//  // 10 permits per second.
//  private final RateLimiter perSecondLimiter = RateLimiter.create(10);
//  // ~2.78 permits per second (for 10,000 per hour).
//  private final RateLimiter perHourLimiter = RateLimiter.create(10000.0 / 3600.0);

@Slf4j
@Transactional
@ApplicationScoped
@RequiredArgsConstructor
public class EnedisService {

  private final Instance<DeviceService> deviceService;
  private final EnedisProperties enedisProperties;

  private Instant calculateRPMExpiration(Instant createdAt) {
    ZonedDateTime zonedDateTime = createdAt.atZone(ZoneId.systemDefault());
    ZonedDateTime newDateTime = zonedDateTime.plus(
        Period.parse(enedisProperties.selfRegistration().duration()));

    return newDateTime.toInstant();
  }

  public void createDevice(String usagePointId) {
    String hardwareId = MODULE_NAME + "-" + usagePointId;

    for (String enedisId : usagePointId.split(";")) {
      Instant now = Instant.now();
      DeviceDTO deviceDTO = deviceService.get().createDevice(
          DeviceDTO.builder()
              .hardwareId(hardwareId)
              .moduleName(MODULE_NAME)
              .createdAt(now)
              .enabled(true)
              .config(EnedisConstants.CONFIG_RPM, enedisId)
              .config(EnedisConstants.CONFIG_RPM_ENABLED_AT, now.toString())
              .config(EnedisConstants.CONFIG_RPM_EXPIRES_AT, calculateRPMExpiration(now).toString())
              .build(), List.of(MODULE_NAME, EDGE));
      log.info("Device with hardwareId '{}' created.", deviceDTO.getHardwareId());
    }
  }

  public EnedisConfigDTO getConfig() {
    EnedisConfigDTO config = new EnedisConfigDTO();
    config.setEnabled(enedisProperties.enabled())
        .setCron(enedisProperties.cron())
        .setClientId(enedisProperties.clientId())
        .setClientSecret(enedisProperties.clientSecret())
        .setMaxDevices(enedisProperties.maxDevices())
        .setSelfRegistrationEnabled(enedisProperties.selfRegistration().enabled())
        .setSelfRegistrationStateChecking(enedisProperties.selfRegistration().stateChecking())
        .setSelfRegistrationWelcomeUrl(
            enedisProperties.selfRegistration().welcomeUrl().orElse(null))
        .setSelfRegistrationRedirectUrl(enedisProperties.selfRegistration().redirectUrl())
        .setSelfRegistrationDuration(enedisProperties.selfRegistration().duration())
        .setSelfRegistrationPageLogo1Url(
            enedisProperties.selfRegistration().page().logo1Url().orElse(null))
        .setSelfRegistrationPageLogo1Alt(
            enedisProperties.selfRegistration().page().logo1Alt().orElse(null))
        .setSelfRegistrationPageLogo2Url(
            enedisProperties.selfRegistration().page().logo2Url().orElse(null))
        .setSelfRegistrationPageLogo2Alt(
            enedisProperties.selfRegistration().page().logo2Alt().orElse(null))
        .setSelfRegistrationPageRegistrationTitle(
            enedisProperties.selfRegistration().page().registration().title())
        .setSelfRegistrationPageRegistrationMessage(
            enedisProperties.selfRegistration().page().registration().message())
        .setSelfRegistrationPageSuccessTitle(
            enedisProperties.selfRegistration().page().success().title())
        .setSelfRegistrationPageSuccessMessage(
            enedisProperties.selfRegistration().page().success().message())
        .setSelfRegistrationPageErrorTitle(
            enedisProperties.selfRegistration().page().error().title())
        .setSelfRegistrationPageErrorMessage(
            enedisProperties.selfRegistration().page().error().message());

    return config;
  }

  public String getSelfRegistrationPage(String state) {
    return new TemplateDTO(EnedisTemplates.SELF_REGISTRATION)
        .data("title", enedisProperties.selfRegistration().page().registration().title())
        .data("logo1", enedisProperties.selfRegistration().page().logo1Url().orElse(""))
        .data("logo1Alt", enedisProperties.selfRegistration().page().logo1Alt().orElse(""))
        .data("logo2", enedisProperties.selfRegistration().page().logo2Url().orElse(""))
        .data("logo2Alt", enedisProperties.selfRegistration().page().logo2Alt().orElse(""))
        .data("state", state)
        .data("clientId", enedisProperties.clientId())
        .data("message", enedisProperties.selfRegistration().page().registration().message())
        .data("duration", enedisProperties.selfRegistration().duration())
        .render();
  }

  public String getRegistrationSuccessfulPage() {
    return new TemplateDTO(EnedisTemplates.REGISTRATION_SUCCESSFUL)
        .data("logo1", enedisProperties.selfRegistration().page().logo1Url().orElse(""))
        .data("logo1Alt", enedisProperties.selfRegistration().page().logo1Alt().orElse(""))
        .data("logo2", enedisProperties.selfRegistration().page().logo2Url().orElse(""))
        .data("logo2Alt", enedisProperties.selfRegistration().page().logo2Alt().orElse(""))
        .data("title", enedisProperties.selfRegistration().page().success().title())
        .data("message", enedisProperties.selfRegistration().page().success().message())
        .render();
  }

  public String getErrorPage() {
    return new TemplateDTO(EnedisTemplates.ERROR)
        .data("logo1", enedisProperties.selfRegistration().page().logo1Url().orElse(""))
        .data("logo1Alt", enedisProperties.selfRegistration().page().logo1Alt().orElse(""))
        .data("logo2", enedisProperties.selfRegistration().page().logo2Url().orElse(""))
        .data("logo2Alt", enedisProperties.selfRegistration().page().logo2Alt().orElse(""))
        .data("title", enedisProperties.selfRegistration().page().error().title())
        .data("message", enedisProperties.selfRegistration().page().error().message())
        .render();
  }

  public long countDevices() {
    return deviceService.get().countDevices();
  }
}
