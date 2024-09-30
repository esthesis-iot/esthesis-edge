package esthesis.edge.modules.enedis.service;

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
  private final EnedisProperties cfg;

  private Instant calculateRPMExpiration(Instant createdAt) {
    ZonedDateTime zonedDateTime = createdAt.atZone(ZoneId.systemDefault());
    ZonedDateTime newDateTime = zonedDateTime.plus(Period.parse(cfg.selfRegistration().duration()));

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
              .build());
      log.info("Device with hardwareId '{}' created.", deviceDTO.getHardwareId());
    }
  }

  public EnedisConfigDTO getConfig() {
    EnedisConfigDTO config = new EnedisConfigDTO();
    config.setEnabled(cfg.enabled())
        .setCron(cfg.cron())
        .setClientId(cfg.clientId())
        .setClientSecret(cfg.clientSecret())
        .setMaxDevices(cfg.maxDevices())
        .setSelfRegistrationEnabled(cfg.selfRegistration().enabled())
        .setSelfRegistrationStateChecking(cfg.selfRegistration().stateChecking())
        .setSelfRegistrationWelcomeUrl(cfg.selfRegistration().welcomeUrl().orElse(null))
        .setSelfRegistrationRedirectUrl(cfg.selfRegistration().redirectUrl())
        .setSelfRegistrationDuration(cfg.selfRegistration().duration())
        .setSelfRegistrationPageLogo1Url(cfg.selfRegistration().page().logo1Url().orElse(null))
        .setSelfRegistrationPageLogo1Alt(cfg.selfRegistration().page().logo1Alt().orElse(null))
        .setSelfRegistrationPageLogo2Url(cfg.selfRegistration().page().logo2Url().orElse(null))
        .setSelfRegistrationPageLogo2Alt(cfg.selfRegistration().page().logo2Alt().orElse(null))
        .setSelfRegistrationPageRegistrationTitle(
            cfg.selfRegistration().page().registration().title())
        .setSelfRegistrationPageRegistrationMessage(
            cfg.selfRegistration().page().registration().message())
        .setSelfRegistrationPageSuccessTitle(cfg.selfRegistration().page().success().title())
        .setSelfRegistrationPageSuccessMessage(cfg.selfRegistration().page().success().message())
        .setSelfRegistrationPageErrorTitle(cfg.selfRegistration().page().error().title())
        .setSelfRegistrationPageErrorMessage(cfg.selfRegistration().page().error().message());

    return config;
  }

  public String getSelfRegistrationPage(String state) {
    return new TemplateDTO(EnedisTemplates.SELF_REGISTRATION)
        .data("title", cfg.selfRegistration().page().registration().title())
        .data("logo1", cfg.selfRegistration().page().logo1Url().orElse(""))
        .data("logo1Alt", cfg.selfRegistration().page().logo1Alt().orElse(""))
        .data("logo2", cfg.selfRegistration().page().logo2Url().orElse(""))
        .data("logo2Alt", cfg.selfRegistration().page().logo2Alt().orElse(""))
        .data("state", state)
        .data("clientId", cfg.clientId())
        .data("message", cfg.selfRegistration().page().registration().message())
        .data("duration", cfg.selfRegistration().duration())
        .render();
  }

  public String getRegistrationSuccessfulPage() {
    return new TemplateDTO(EnedisTemplates.REGISTRATION_SUCCESSFUL)
        .data("logo1", cfg.selfRegistration().page().logo1Url().orElse(""))
        .data("logo1Alt", cfg.selfRegistration().page().logo1Alt().orElse(""))
        .data("logo2", cfg.selfRegistration().page().logo2Url().orElse(""))
        .data("logo2Alt", cfg.selfRegistration().page().logo2Alt().orElse(""))
        .data("title", cfg.selfRegistration().page().success().title())
        .data("message", cfg.selfRegistration().page().success().message())
        .render();
  }

  public String getErrorPage() {
    return new TemplateDTO(EnedisTemplates.ERROR)
        .data("logo1", cfg.selfRegistration().page().logo1Url().orElse(""))
        .data("logo1Alt", cfg.selfRegistration().page().logo1Alt().orElse(""))
        .data("logo2", cfg.selfRegistration().page().logo2Url().orElse(""))
        .data("logo2Alt", cfg.selfRegistration().page().logo2Alt().orElse(""))
        .data("title", cfg.selfRegistration().page().error().title())
        .data("message", cfg.selfRegistration().page().error().message())
        .render();
  }
}
