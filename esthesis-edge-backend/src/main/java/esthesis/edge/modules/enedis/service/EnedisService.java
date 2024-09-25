package esthesis.edge.modules.enedis.service;

import static esthesis.edge.modules.ModuleConstants.DEVICE_COL_HARDWARE_ID;
import static esthesis.edge.modules.enedis.EnedisConstants.MODULE_NAME;

import esthesis.edge.model.DeviceEntity;
import esthesis.edge.model.DeviceModuleConfigEntity;
import esthesis.edge.modules.enedis.EnedisConstants;
import esthesis.edge.modules.enedis.dto.EnedisConfigDTO;
import esthesis.edge.service.DeviceService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
@Transactional
@ApplicationScoped
@RequiredArgsConstructor
public class EnedisService {

  private final DeviceService deviceService;
  private final Config configProvider;

  @ConfigProperty(name = "esthesis.edge.modules.enedis.self-registration.duration")
  String duration;
//  // 10 permits per second.
//  private final RateLimiter perSecondLimiter = RateLimiter.create(10);
//  // ~2.78 permits per second (for 10,000 per hour).
//  private final RateLimiter perHourLimiter = RateLimiter.create(10000.0 / 3600.0);

  private Instant calculateRPMExpiration(Instant createdAt) {
    ZonedDateTime zonedDateTime = createdAt.atZone(ZoneId.systemDefault());
    ZonedDateTime newDateTime = zonedDateTime.plus(Period.parse(duration));

    return newDateTime.toInstant();
  }

  public void createDevice(String usagePointId) {
    String hardwareId = MODULE_NAME + "-" + usagePointId;
    DeviceEntity device = DeviceEntity.find(DEVICE_COL_HARDWARE_ID, hardwareId).firstResult();

    if (device == null) {
      for (String enedisId : usagePointId.split(";")) {
        Instant now = Instant.now();
        deviceService.createDevice(
            DeviceEntity.builder()
                .id(UUID.randomUUID().toString())
                .hardwareId(hardwareId)
                .moduleName(MODULE_NAME)
                .createdAt(now)
                .enabled(true)
                .config(
                    DeviceModuleConfigEntity.create(EnedisConstants.CONFIG_RPM, enedisId))
                .config(DeviceModuleConfigEntity.create(EnedisConstants.CONFIG_RPM_ENABLED_AT,
                    now.toString()))
                .config(DeviceModuleConfigEntity.create(EnedisConstants.CONFIG_RPM_EXPIRES_AT,
                    calculateRPMExpiration(now).toString()))
                .build());
        log.info("Device with hardwareId '{}' created.", hardwareId);
      }
    } else {
      deviceService.updateDeviceConfig(device.getId(),
          Map.of(EnedisConstants.CONFIG_RPM_EXPIRES_AT,
              calculateRPMExpiration(Instant.now()).toString()));
      log.info("Device with hardwareId '{}' already registered, updated RPM expiration date only.",
          hardwareId);
    }
  }

  public EnedisConfigDTO getConfig() {
    EnedisConfigDTO config = new EnedisConfigDTO();
    final String PREFIX = "esthesis.edge.modules.enedis.";
    config.setEnabled(
        configProvider.getOptionalValue(PREFIX + "enabled", Boolean.class).orElse(null));
    config.setCron(configProvider.getOptionalValue(PREFIX + "cron", String.class).orElse(null));
    config.setClientId(
        configProvider.getOptionalValue(PREFIX + "client-id", String.class).orElse(null));
    config.setClientSecret(
        configProvider.getOptionalValue(PREFIX + "client-secret", String.class).orElse(null));
    config.setAdminSecret(
        configProvider.getOptionalValue(PREFIX + "admin-secret", String.class).orElse(null));
    config.setMaxDevices(
        configProvider.getOptionalValue(PREFIX + "max-devices", Integer.class).orElse(null));
    config.setSelfRegistrationEnabled(
        configProvider.getOptionalValue(PREFIX + "self-registration.enabled", Boolean.class)
            .orElse(null));
    config.setSelfRegistrationStateChecking(
        configProvider.getOptionalValue(PREFIX + "self-registration.state-checking", Boolean.class)
            .orElse(null));
    config.setSelfRegistrationWelcomeUrl(
        configProvider.getOptionalValue(PREFIX + "self-registration.welcome-url", String.class)
            .orElse(null));
    config.setSelfRegistrationRedirectUrl(
        configProvider.getOptionalValue(PREFIX + "self-registration.redirect-url", String.class)
            .orElse(null));
    config.setSelfRegistrationDuration(
        configProvider.getOptionalValue(PREFIX + "self-registration.duration", String.class)
            .orElse(null));
    config.setSelfRegistrationPageLogoUrl(
        configProvider.getOptionalValue(PREFIX + "self-registration.page.logoUrl", String.class)
            .orElse(null));
    config.setSelfRegistrationPageLogoAlt(
        configProvider.getOptionalValue(PREFIX + "self-registration.page.logoAlt", String.class)
            .orElse(null));
    config.setSelfRegistrationPageRegistrationTitle(
        configProvider.getOptionalValue(PREFIX + "self-registration.page.registration.title",
            String.class).orElse(null));
    config.setSelfRegistrationPageRegistrationMessage(
        configProvider.getOptionalValue(PREFIX + "self-registration.page.registration.message",
            String.class).orElse(null));
    config.setSelfRegistrationPageSuccessTitle(
        configProvider.getOptionalValue(PREFIX + "self-registration.page.success.title",
            String.class).orElse(null));
    config.setSelfRegistrationPageSuccessMessage(
        configProvider.getOptionalValue(PREFIX + "self-registration.page.success.message",
            String.class).orElse(null));
    config.setSelfRegistrationPageErrorTitle(
        configProvider.getOptionalValue(PREFIX + "self-registration.page.error.title", String.class)
            .orElse(null));
    config.setSelfRegistrationPageErrorMessage(
        configProvider.getOptionalValue(PREFIX + "self-registration.page.error.message",
            String.class).orElse(null));

    return config;
  }
}
