package esthesis.edge.modules.enedis.service;

import static esthesis.edge.config.EdgeConstants.EDGE;
import static esthesis.edge.modules.enedis.config.EnedisConstants.MODULE_NAME;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.common.exception.QProcessingException;
import esthesis.edge.dto.DeviceDTO;
import esthesis.edge.dto.DeviceDTO.DeviceDTOBuilder;
import esthesis.edge.dto.TemplateDTO;
import esthesis.edge.model.DeviceEntity;
import esthesis.edge.model.DeviceModuleConfigEntity;
import esthesis.edge.modules.enedis.client.EnedisClient;
import esthesis.edge.modules.enedis.config.EnedisConstants;
import esthesis.edge.modules.enedis.config.EnedisProperties;
import esthesis.edge.modules.enedis.dto.datahub.EnedisAuthTokenDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisContractDTO;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;


/**
 * Service for handling Enedis module related operations.
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class EnedisService {

  @Inject
  @RestClient
  EnedisClient enedisClient;

  private final DeviceService deviceService;
  private final EnedisProperties enedisProperties;
  private final EnedisFetchService enedisFetchService;
  private final ObjectMapper objectMapper;

  // A local reference of the access token, to not keep refreshing when not needed.
  private EnedisAuthTokenDTO enedisAuthTokenDTO;
  // Rate limiters for Enedis API.
  private final RateLimiter perSecondLimiter = RateLimiter.of("perSecondLimiter",
      RateLimiterConfig.custom()
          .limitForPeriod(EnedisConstants.REQUESTS_PER_SECOND)
          .limitRefreshPeriod(Duration.ofSeconds(1))
          .build());
  private final RateLimiter perHourLimiter = RateLimiter.of("perHourLimiter",
      RateLimiterConfig.custom()
          .limitForPeriod(EnedisConstants.REQUESTS_PER_HOUR)
          .limitRefreshPeriod(Duration.ofHours(1))
          .build());

//  public EnedisService(DeviceService deviceService, EnedisProperties enedisProperties,
//      EnedisFetchService enedisFetchService) {
//  public EnedisService() {
//    this.deviceService = deviceService;
//    this.enedisProperties = enedisProperties;
//    this.enedisFetchService = enedisFetchService;
//
//    perSecondLimiter = RateLimiter.of("perSecondLimiter", RateLimiterConfig.custom()
//        .limitForPeriod(EnedisConstants.REQUESTS_PER_SECOND)
//        .limitRefreshPeriod(Duration.ofSeconds(1))
//        .build());
//    perHourLimiter = RateLimiter.of("perHourLimiter", RateLimiterConfig.custom()
//        .limitForPeriod(EnedisConstants.REQUESTS_PER_HOUR)
//        .limitRefreshPeriod(Duration.ofHours(1))
//        .build());
//  }

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
      enedisAuthTokenDTO = enedisClient.getAuthToken("client_credentials",
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

    // Create a device for each PRM.
    for (String enedisId : usagePointId.split(";")) {
      Instant now = Instant.now();
      DeviceDTOBuilder deviceDTOBuilder = DeviceDTO.builder()
          .hardwareId(hardwareId)
          .moduleName(MODULE_NAME)
          .createdAt(now)
          .enabled(true)
          .config(EnedisConstants.CONFIG_PRM, enedisId)
          .config(EnedisConstants.CONFIG_PMR_ENABLED_AT, now.toString())
          .config(EnedisConstants.CONFIG_PMR_EXPIRES_AT, calculatePMRExpiration(now).toString());

      // Check if the PRM is for a producer or a consumer.
      refreshAuthToken();
      try {
        EnedisContractDTO contractDTO = objectMapper.readValue(
            enedisClient.getContracts(enedisId, "Bearer " + enedisAuthTokenDTO.getAccessToken()),
            EnedisContractDTO.class);
        String segmentType = contractDTO.getCustomer().getUsagePoints()[0].getContracts()
            .getSegment();
        if (segmentType.equals(EnedisConstants.SEGMENT_TYPE_CONSUMER)) {
          deviceDTOBuilder.config(EnedisConstants.CONFIG_CONSUMER, "true");
        } else if (segmentType.equals(EnedisConstants.SEGMENT_TYPE_PRODUCER)) {
          deviceDTOBuilder.config(EnedisConstants.CONFIG_PRODUCER, "true");
        } else {
          throw new QProcessingException("Unknown segment type for PRM '{}'.", segmentType);
        }
        deviceDTOBuilder.attribute("segment", segmentType);

        // Add Enedis device attributes for esthesis CORE.
        String usagePointStatus = contractDTO.getCustomer().getUsagePoints()[0].getUsagePoint()
            .getUsagePointStatus();
        String meterType = contractDTO.getCustomer().getUsagePoints()[0].getUsagePoint()
            .getMeterType();
        String subscribedPower = contractDTO.getCustomer().getUsagePoints()[0].getContracts()
            .getSubscribedPower();
        String lastActivationDate = contractDTO.getCustomer().getUsagePoints()[0].getContracts()
            .getLastActivationDate();
        String distributionTariff = contractDTO.getCustomer().getUsagePoints()[0].getContracts()
            .getDistributionTariff();
        String offpeakHours = contractDTO.getCustomer().getUsagePoints()[0].getContracts()
            .getOffpeakHours();
        String contractType = contractDTO.getCustomer().getUsagePoints()[0].getContracts()
            .getContractType();
        String contractStatus = contractDTO.getCustomer().getUsagePoints()[0].getContracts()
            .getContractStatus();
        String lastDistributionTariffChangeDate = contractDTO.getCustomer()
            .getUsagePoints()[0].getContracts().getLastDistributionTariffChangeDate();
        if (StringUtils.isNotBlank(usagePointStatus)) {
          deviceDTOBuilder.attribute("usagePointStatus", usagePointStatus);
        }
        if (StringUtils.isNotBlank(meterType)) {
          deviceDTOBuilder.attribute("meterType", meterType);
        }
        if (StringUtils.isNotBlank(subscribedPower)) {
          deviceDTOBuilder.attribute("subscribedPower", subscribedPower);
        }
        if (StringUtils.isNotBlank(lastActivationDate)) {
          deviceDTOBuilder.attribute("lastActivationDate", lastActivationDate);
        }
        if (StringUtils.isNotBlank(distributionTariff)) {
          deviceDTOBuilder.attribute("distributionTariff", distributionTariff);
        }
        if (StringUtils.isNotBlank(offpeakHours)) {
          deviceDTOBuilder.attribute("offpeakHours", offpeakHours);
        }
        if (StringUtils.isNotBlank(contractType)) {
          deviceDTOBuilder.attribute("contractType", contractType);
        }
        if (StringUtils.isNotBlank(contractStatus)) {
          deviceDTOBuilder.attribute("contractStatus", contractStatus);
        }
        if (StringUtils.isNotBlank(lastDistributionTariffChangeDate)) {
          deviceDTOBuilder.attribute("lastDistributionTariffChangeDate",
              lastDistributionTariffChangeDate);
        }
      } catch (JsonProcessingException e) {
        throw new QProcessingException("Failed to parse Enedis contract data.", e);
      }

      // Create the device.
      deviceDTOBuilder.tags(String.join(",", MODULE_NAME, EDGE));
      deviceService.createDevice(deviceDTOBuilder.build());
    }
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
        .data("logo3", enedisProperties.selfRegistration().page().logo3Url().orElse(""))
        .data("logo3Alt", enedisProperties.selfRegistration().page().logo3Alt().orElse(""))
        .data("buttonUrl", enedisProperties.selfRegistration().page().buttonUrl())
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
        .data("logo3", enedisProperties.selfRegistration().page().logo3Url().orElse(""))
        .data("logo3Alt", enedisProperties.selfRegistration().page().logo3Alt().orElse(""))
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
        .data("logo3", enedisProperties.selfRegistration().page().logo3Url().orElse(""))
        .data("logo3Alt", enedisProperties.selfRegistration().page().logo3Alt().orElse(""))
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
    if (!enedisProperties.enabled()) {
      return;
    }
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
        log.warn("No PMR expiration date found for device '{}'.", device.getHardwareId());
      }

      // Refresh auth token.
      refreshAuthToken();

      // Fetch data.
      String hardwareId = device.getHardwareId();
      String enedisPrm = deviceService
          .getDeviceConfigValueAsString(hardwareId, EnedisConstants.CONFIG_PRM)
          .orElseThrow();
      log.debug("Fetching data for device '{}'.", hardwareId);


      // Consumer data.
      if (deviceService.getDeviceConfigValueAsBoolean(device.getHardwareId(),
              EnedisConstants.CONFIG_CONSUMER).orElse(false)) {
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
      } else {
        log.debug("Device ID '{}' is not a consumer.", hardwareId);
      }

      // Producer data.
      if (deviceService.getDeviceConfigValueAsBoolean(device.getHardwareId(),
          EnedisConstants.CONFIG_PRODUCER).orElse(false)) {
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
      } else {
        log.debug("Device ID '{}' is not a producer.", hardwareId);
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
