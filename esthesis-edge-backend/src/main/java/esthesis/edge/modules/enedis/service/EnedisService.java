package esthesis.edge.modules.enedis.service;

import static esthesis.edge.config.EdgeConstants.EDGE;
import static esthesis.edge.modules.enedis.config.EnedisConstants.MODULE_NAME;

import esthesis.edge.dto.DeviceDTO;
import esthesis.edge.dto.TemplateDTO;
import esthesis.edge.model.DeviceEntity;
import esthesis.edge.model.DeviceModuleConfigEntity;
import esthesis.edge.modules.enedis.client.EnedisClient;
import esthesis.edge.modules.enedis.config.EnedisConstants;
import esthesis.edge.modules.enedis.config.EnedisProperties;
import esthesis.edge.modules.enedis.dto.EnedisConfigDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisAuthTokenDTO;
import esthesis.edge.modules.enedis.templates.EnedisTemplates;
import esthesis.edge.services.DeviceService;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;


/**
 * Service for handling Enedis module related operations.
 */
@Slf4j
@ApplicationScoped
public class EnedisService {

  private final DeviceService deviceService;
  private final EnedisProperties enedisProperties;
  private final EnedisFetchService enedisFetchService;
  @RestClient
  @Inject
  EnedisClient enedisRestClient;
  // A local reference of the access token, to not keep refreshing when not needed.
  private EnedisAuthTokenDTO enedisAuthTokenDTO;
  // Rate limiters for Enedis API.
  private RateLimiter perSecondLimiter;
  private RateLimiter perHourLimiter;

  public EnedisService(DeviceService deviceService, EnedisProperties enedisProperties,
      EnedisFetchService enedisFetchService) {
    this.deviceService = deviceService;
    this.enedisProperties = enedisProperties;
    this.enedisFetchService = enedisFetchService;

    perSecondLimiter = RateLimiter.of("perSecondLimiter", RateLimiterConfig.custom()
        .limitForPeriod(EnedisConstants.REQUESTS_PER_SECOND)
        .limitRefreshPeriod(Duration.ofSeconds(1))
        .build());
    perHourLimiter = RateLimiter.of("perHourLimiter", RateLimiterConfig.custom()
        .limitForPeriod(EnedisConstants.REQUESTS_PER_HOUR)
        .limitRefreshPeriod(Duration.ofHours(1))
        .build());
  }

  /**
   * Calculate the expiration time for the PMR token.
   *
   * @param createdAt The time the token was created.
   * @return The expiration time.
   */
  private Instant calculatePMRExpiration(Instant createdAt) {
    ZonedDateTime zonedDateTime = createdAt.atZone(ZoneId.systemDefault());
    ZonedDateTime newDateTime = zonedDateTime.plus(
        Period.parse(enedisProperties.selfRegistration().duration()));

    return newDateTime.toInstant();
  }

  /**
   * Check if the authentication token has expired. For each device, subtract 3 seconds off the
   * expiration time to ensure the token can be used throughout the duration of the request.
   *
   * @return True if the token has expired, false otherwise.
   */
  private boolean hasAuthTokenExpired() {
    if (enedisAuthTokenDTO == null) {
      return true;
    }

    return Instant.now().isAfter(enedisAuthTokenDTO.getExpiresOn());
  }

  private String createHardwareId(String usagePointId) {
    return MODULE_NAME + "-" + usagePointId;
  }

  /**
   * Refresh the authentication token if it has expired.
   */
  public void refreshAuthToken() {
    if (hasAuthTokenExpired()) {
      log.debug("Refreshing access token for Enedis.");
      enedisAuthTokenDTO = enedisRestClient.getAuthToken("client_credentials",
          enedisProperties.clientId(), enedisProperties.clientSecret());
      log.debug("Access token refreshed '{}'.", enedisAuthTokenDTO);
    } else {
      log.debug("Previously obtained access token is still valid, re-using it.");
    }
  }

  /**
   * Create a new device for the given usage point ID.
   *
   * @param usagePointId The usage point ID.
   */
  @Transactional
  public void createDevice(String usagePointId) {
    String hardwareId = createHardwareId(usagePointId);

    for (String enedisId : usagePointId.split(";")) {
      Instant now = Instant.now();
      DeviceDTO deviceDTO = deviceService.createDevice(
          DeviceDTO.builder()
              .hardwareId(hardwareId)
              .moduleName(MODULE_NAME)
              .createdAt(now)
              .enabled(true)
              .config(EnedisConstants.CONFIG_PRM, enedisId)
              .config(EnedisConstants.CONFIG_PMR_ENABLED_AT, now.toString())
              .config(EnedisConstants.CONFIG_PMR_EXPIRES_AT, calculatePMRExpiration(now).toString())
              .build(), List.of(MODULE_NAME, EDGE));
      log.info("Device with hardwareId '{}' created.", deviceDTO.getHardwareId());
    }
  }

  /**
   * Get the Enedis module configuration.
   *
   * @return The Enedis module configuration.
   */
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

  /**
   * Get the self registration page.
   *
   * @param state The state to pass to the self registration page.
   * @return The self registration page.
   */
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

  /**
   * Get the registration successful page.
   *
   * @return The registration successful page.
   */
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

  /**
   * Get the error page.
   *
   * @return The error page.
   */
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

  /**
   * Get the total number of registered devices, enabled and disabled.
   *
   * @return The total number of registered devices.
   */
  public long countDevices() {
    return deviceService.countDevices();
  }

  /**
   * Fetches new data from Enedis API.
   */
  @Scheduled(cron = "{esthesis.edge.modules.enedis.cron}")
  public void fetchData() {
    log.debug("Fetching data from Enedis.");

    // Get all Enedis devices.
    List<DeviceDTO> devices = deviceService.listActiveDevices(MODULE_NAME);
    log.debug("Found '{}' active devices to fetch data for.", devices.size());
    if (devices.isEmpty()) {
      return;
    }

    for (DeviceDTO device : devices) {
      // Check if the PMR for this device is still active. If not, set the device as disabled and
      // skip fetching data.
      Optional<Instant> pmrExpiresAtOpt = deviceService
          .getDeviceConfigValueAsInstant(device.getHardwareId(),
              EnedisConstants.CONFIG_PMR_EXPIRES_AT);
      if (pmrExpiresAtOpt.isPresent()) {
        Instant pmrExpiresAt = pmrExpiresAtOpt.get();
        if (pmrExpiresAt.isBefore(Instant.now())) {
          log.info("PMR for device '{}' has expired, disabling this device.",
              device.getHardwareId());
          deviceService.disableDevice(device.getHardwareId());
          continue;
        }
      } else {
        log.warn("No PMR expiration date found for device '{}'.",
            device.getHardwareId());
      }

      // Refresh auth token.
      refreshAuthToken();

      // Fetch data.
      String hardwareId = device.getHardwareId();
      String enedisPrm = deviceService
          .getDeviceConfigValueAsString(hardwareId, EnedisConstants.CONFIG_PRM)
          .orElseThrow();
      log.debug("Fetching data for device '{}'.", hardwareId);

      // Daily Consumption.
      int dcErrors = deviceService.getDeviceConfigValueAsString(hardwareId,
          EnedisConstants.CONFIG_DC_ERRORS).map(Integer::parseInt).orElse(0);
      if (enedisProperties.fetchTypes().dc().enabled() &&
          dcErrors < enedisProperties.fetchTypes().dc().errorsThreshold()) {
        perSecondLimiter.acquirePermission();
        perHourLimiter.acquirePermission();
        int itemsQueued = enedisFetchService.fetchDailyConsumption(hardwareId, enedisPrm,
            enedisAuthTokenDTO.getAccessToken());
        log.debug("Queued '{}' items from Daily Consumption API.", itemsQueued);
      }

      // Daily Consumption Max Power.
      int dcmpErrors = deviceService.getDeviceConfigValueAsString(hardwareId,
          EnedisConstants.CONFIG_DCMP_ERRORS).map(Integer::parseInt).orElse(0);
      if (enedisProperties.fetchTypes().dcmp().enabled()
          && dcmpErrors < enedisProperties.fetchTypes().dcmp().errorsThreshold()) {
        perSecondLimiter.acquirePermission();
        perHourLimiter.acquirePermission();
        int itemsQueued = enedisFetchService.fetchDailyConsumptionMaxPower(hardwareId, enedisPrm,
            enedisAuthTokenDTO.getAccessToken());
        log.debug("Queued '{}' items from Daily Consumption Max Power API.", itemsQueued);
      }

      // Daily Production.
      int dpErrors = deviceService.getDeviceConfigValueAsString(hardwareId,
          EnedisConstants.CONFIG_DP_ERRORS).map(Integer::parseInt).orElse(0);
      if (enedisProperties.fetchTypes().dp().enabled() &&
          dpErrors < enedisProperties.fetchTypes().dp().errorsThreshold()) {
        perSecondLimiter.acquirePermission();
        perHourLimiter.acquirePermission();
        int itemsQueued = enedisFetchService.fetchDailyProduction(hardwareId, enedisPrm,
            enedisAuthTokenDTO.getAccessToken());
        log.debug("Queued '{}' items from Daily Production API.", itemsQueued);
      }

      log.debug("Data fetch completed for device '{}'.", hardwareId);
    }
  }

  /**
   * Get the devices that have fetch errors above the defined threshold.
   *
   * @return The devices that have fetch errors.
   */
  public List<DeviceEntity> getFetchErrors() {
    return DeviceModuleConfigEntity
        .find("(configKey = 'dc_errors' and CAST(configValue AS INTEGER) >= ?1) or " +
                "(configKey = 'dcmp_errors' and CAST(configValue AS INTEGER) >= ?2) or " +
                "(configKey = 'dp_errors' and CAST(configValue AS INTEGER) >= ?3)",
            enedisProperties.fetchTypes().dc().errorsThreshold(),
            enedisProperties.fetchTypes().dcmp().errorsThreshold(),
            enedisProperties.fetchTypes().dp().errorsThreshold())
        .list()
        .stream()
        .map(entity -> ((DeviceModuleConfigEntity) entity).getDevice())
        .distinct()
        .toList();
  }

  /**
   * Reset the fetch errors for the given device.
   *
   * @param hardwareId The hardware ID of the device.
   * @return The device entity.
   */
  @Transactional
  public DeviceEntity resetFetchErrors(String hardwareId) {
    log.debug("Resetting fetch errors for device '{}'.", hardwareId);
    DeviceModuleConfigEntity.updateConfigValue(hardwareId, EnedisConstants.CONFIG_DC_ERRORS, "0");
    DeviceModuleConfigEntity.updateConfigValue(hardwareId, EnedisConstants.CONFIG_DCMP_ERRORS, "0");
    DeviceModuleConfigEntity.updateConfigValue(hardwareId, EnedisConstants.CONFIG_DP_ERRORS, "0");

    return DeviceEntity.findByHardwareId(hardwareId).orElseThrow();
  }
}
