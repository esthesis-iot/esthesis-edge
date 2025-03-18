package esthesis.edge.modules.enedis.service;

import esthesis.edge.dto.QueueItemDTO;
import esthesis.edge.model.DeviceEntity;
import esthesis.edge.model.DeviceModuleConfigEntity;
import esthesis.edge.modules.enedis.EnedisUtil;
import esthesis.edge.modules.enedis.client.EnedisClient;
import esthesis.edge.modules.enedis.config.EnedisConstants;
import esthesis.edge.modules.enedis.config.EnedisProperties;
import esthesis.edge.modules.enedis.dto.datahub.EnedisConsumptionLoadCurveDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisDailyConsumptionDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisDailyConsumptionMaxPowerDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisDailyProductionDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisProductionLoadCurveDTO;
import esthesis.edge.services.DeviceService;
import esthesis.edge.services.QueueService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * A service to fetch data from the Enedis API.
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class EnedisFetchService {

  @Inject
  @RestClient
  @SuppressWarnings("java:S6813")
  EnedisClient enedisClient;

  private final DeviceService deviceService;
  private final QueueService dataService;
  private final EnedisProperties enedisProperties;
  private final EnedisELPMapperService enedisELPMapperService;

  /**
   * A helper method to increase by one the number of errors for a specific configuration key. If
   * the key does not exist, it will be created with value "1".
   *
   * @param hardwareId The hardware ID of the device.
   * @param configKey  The configuration key.
   */
  private void increaseErrors(String hardwareId, String configKey) {
    Optional<DeviceModuleConfigEntity> config = DeviceModuleConfigEntity.getConfig(hardwareId,
        configKey);
    if (config.isPresent()) {
      int currentErrorVal = Integer.parseInt(config.get().getConfigValue());
      config.get().setConfigValue(String.valueOf(currentErrorVal + 1));
    } else {
      DeviceModuleConfigEntity newConfig = DeviceModuleConfigEntity.create(configKey, "1");
      newConfig.setDevice(DeviceEntity.findByHardwareId(hardwareId).orElseThrow());
      newConfig.persist();
    }
  }

  /**
   * A helper method to reset (to zero) the number of errors for a specific configuration key.
   *
   * @param hardwareId The hardware ID of the device.
   * @param configKey  The configuration key.
   */
  private void resetErrors(String hardwareId, String configKey) {
    Optional<DeviceModuleConfigEntity> config = DeviceModuleConfigEntity.getConfig(hardwareId,
        configKey);
    if (config.isPresent()) {
      config.get().setConfigValue("0");
    } else {
      log.warn("Failed to reset errors for device '{}' as config key '{}' does not exist.",
          hardwareId, configKey);
    }
  }

  /**
   * Fetch daily consumption data from Enedis API.
   *
   * @param hardwareId  The hardware ID of the device.
   * @param enedisPrm   The Enedis PRM.
   * @param accessToken The access token.
   * @return
   */
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public int fetchDailyConsumption(String hardwareId, String enedisPrm, String accessToken) {
    // Fetch data.
    String lastFetch = EnedisUtil.instantToYmd(deviceService
        .getDeviceConfigValueAsInstant(hardwareId, EnedisConstants.CONFIG_DC_LAST_FETCHED_AT)
        .orElse(Instant.now().minus(Duration.ofDays(enedisProperties.pastDaysInit()))));
    log.debug("Fetching Daily Consumption for device '{}', from '{}'.", hardwareId, lastFetch);
    EnedisDailyConsumptionDTO dailyConsumptionDTO = null;
    try {
      dailyConsumptionDTO = enedisClient.getDailyConsumption(
          lastFetch, EnedisUtil.instantToYmd(Instant.now()),
          enedisPrm, "Bearer " + accessToken);
      resetErrors(hardwareId, EnedisConstants.CONFIG_DC_ERRORS);
      log.debug("Fetched Daily Consumption '{}'.", dailyConsumptionDTO);
    } catch (Exception e) {
      log.warn("Failed to fetch Daily Consumption for device '{}'.", hardwareId, e);
      increaseErrors(hardwareId, EnedisConstants.CONFIG_DC_ERRORS);
    }

    // Queue data for processing.
    int itemsQueued = 0;
    if (dailyConsumptionDTO != null) {
      if (!dailyConsumptionDTO.getMeterReading().getIntervalReading().isEmpty()) {
        log.debug("Queuing Daily Consumption:\n{}",
            enedisELPMapperService.toELP(dailyConsumptionDTO));
        dataService.queue(
            QueueItemDTO.builder()
                .id(UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .hardwareId(hardwareId)
                .dataObject(enedisELPMapperService.toELP(dailyConsumptionDTO))
                .build());

        // Update last fetched at, only if data was fetched. This is due to the fact that data might
        // not be available at the time of fetching, however it may become available later on.
        deviceService.updateDeviceConfig(hardwareId, EnedisConstants.CONFIG_DC_LAST_FETCHED_AT,
            Instant.now().toString());

        itemsQueued++;
      } else {
        log.debug("No Daily Consumption data to queue.");
      }
    }

    return itemsQueued;
  }

  /**
   * Fetch daily consumption max power data from Enedis API.
   *
   * @param hardwareId  The hardware ID of the device.
   * @param enedisPrm   The Enedis PRM.
   * @param accessToken The access token.
   * @return
   */
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public int fetchDailyConsumptionMaxPower(String hardwareId, String enedisPrm,
      String accessToken) {
    // Fetch data.
    String lastFetch = EnedisUtil.instantToYmd(deviceService
        .getDeviceConfigValueAsInstant(hardwareId, EnedisConstants.CONFIG_DCMP_LAST_FETCHED_AT)
        .orElse(Instant.now().minus(Duration.ofDays(enedisProperties.pastDaysInit()))));
    log.debug("Fetching Daily Consumption Max Power for device '{}', from '{}'.", hardwareId,
        lastFetch);
    EnedisDailyConsumptionMaxPowerDTO dailyConsumptionMaxPowerDTO = null;
    try {
      dailyConsumptionMaxPowerDTO =
          enedisClient.getDailyConsumptionMaxPower(
              lastFetch, EnedisUtil.instantToYmd(Instant.now()),
              enedisPrm, "Bearer " + accessToken);
      log.debug("Fetched Daily Consumption Max Power '{}'.", dailyConsumptionMaxPowerDTO);
      resetErrors(hardwareId, EnedisConstants.CONFIG_DCMP_ERRORS);
    } catch (Exception e) {
      log.warn("Failed to fetch Daily Consumption Max Power for device '{}'.", hardwareId, e);
      increaseErrors(hardwareId, EnedisConstants.CONFIG_DCMP_ERRORS);
    }

    int itemsQueued = 0;
    if (dailyConsumptionMaxPowerDTO != null) {
      // Queue data for processing.
      if (!dailyConsumptionMaxPowerDTO.getMeterReading().getIntervalReading().isEmpty()) {
        log.debug("Queuing Daily Consumption Max Power:\n{}",
            enedisELPMapperService.toELP(dailyConsumptionMaxPowerDTO));
        dataService.queue(
            QueueItemDTO.builder()
                .id(UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .hardwareId(hardwareId)
                .dataObject(enedisELPMapperService.toELP(dailyConsumptionMaxPowerDTO))
                .build());

        // Update last fetched at, only if data was fetched. This is due to the fact that data might
        // not be available at the time of fetching, however it may become available later on.
        deviceService.updateDeviceConfig(hardwareId, EnedisConstants.CONFIG_DCMP_LAST_FETCHED_AT,
            Instant.now().toString());

        itemsQueued++;
      }
    } else {
      log.debug("No Daily Consumption Max Power data to queue.");
    }

    return itemsQueued;
  }

  /**
   * Fetch daily production data from Enedis API.
   *
   * @param hardwareId  The hardware ID of the device.
   * @param enedisPrm   The Enedis PRM.
   * @param accessToken The access token.
   * @return
   */
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public int fetchDailyProduction(String hardwareId, String enedisPrm, String accessToken) {
    // Fetch data.
    String lastFetch = EnedisUtil.instantToYmd(deviceService
        .getDeviceConfigValueAsInstant(hardwareId, EnedisConstants.CONFIG_DP_LAST_FETCHED_AT)
        .orElse(Instant.now().minus(Duration.ofDays(enedisProperties.pastDaysInit()))));
    log.debug("Fetching Daily Production for device '{}', from '{}'.", hardwareId, lastFetch);
    EnedisDailyProductionDTO dailyProductionDTO = null;
    try {
      dailyProductionDTO = enedisClient.getDailyProduction(
          lastFetch, EnedisUtil.instantToYmd(Instant.now()),
          enedisPrm, "Bearer " + accessToken);
      log.debug("Fetched Daily Production '{}'.", dailyProductionDTO);
      resetErrors(hardwareId, EnedisConstants.CONFIG_DP_ERRORS);
    } catch (Exception e) {
      log.warn("Failed to fetch Daily Production for device '{}'.", hardwareId, e);
      increaseErrors(hardwareId, EnedisConstants.CONFIG_DP_ERRORS);
    }

    int itemsQueued = 0;
    if (dailyProductionDTO != null) {
      // Queue data for processing.
      if (!dailyProductionDTO.getMeterReading().getIntervalReading().isEmpty()) {
        log.debug("Queuing Daily Production:\n{}",
            enedisELPMapperService.toELP(dailyProductionDTO));
        dataService.queue(
            QueueItemDTO.builder()
                .id(UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .hardwareId(hardwareId)
                .dataObject(enedisELPMapperService.toELP(dailyProductionDTO))
                .build());

        // Update last fetched at, only if data was fetched. This is due to the fact that data might
        // not be available at the time of fetching, however it may become available later on.
        deviceService.updateDeviceConfig(hardwareId, EnedisConstants.CONFIG_DP_LAST_FETCHED_AT,
            Instant.now().toString());

        itemsQueued++;
      }
    } else {
      log.debug("No Daily Production data to queue.");
    }

    return itemsQueued;
  }

  /**
   * Fetch consumption load curve data from Enedis API.
   *
   * @param hardwareId  The hardware ID of the device.
   * @param enedisPrm   The Enedis PRM.
   * @param accessToken The access token.
   * @return
   */
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public int fetchConsumptionLoadCurve(String hardwareId, String enedisPrm, String accessToken) {
    // Fetch data.
    String lastFetch = EnedisUtil.instantToYmd(deviceService
            .getDeviceConfigValueAsInstant(hardwareId, EnedisConstants.CONFIG_CLC_LAST_FETCHED_AT)
            .orElse(Instant.now().minus(Duration.ofDays(enedisProperties.pastDaysInit()))));
    log.debug("Fetching Consumption Load Curve for device '{}', from '{}'.", hardwareId, lastFetch);
    EnedisConsumptionLoadCurveDTO consumptionLoadCurveDTO = null;
    try {
      consumptionLoadCurveDTO = enedisClient.getConsumptionLoadCurve(
              lastFetch, EnedisUtil.instantToYmd(Instant.now()),
              enedisPrm, "Bearer " + accessToken);
      resetErrors(hardwareId, EnedisConstants.CONFIG_CLC_ERRORS);
      log.debug("Fetched Consumption Load Curve '{}'.", consumptionLoadCurveDTO);
    } catch (Exception e) {
      log.warn("Failed to fetch Consumption Load Curve for device '{}'.", hardwareId, e);
      increaseErrors(hardwareId, EnedisConstants.CONFIG_CLC_ERRORS);
    }

    // Queue data for processing.
    int itemsQueued = 0;
    if (consumptionLoadCurveDTO != null) {
      if (!consumptionLoadCurveDTO.getMeterReading().getIntervalReading().isEmpty()) {
        log.debug("Queuing Consumption Load Curve:\n{}",
                enedisELPMapperService.toELP(consumptionLoadCurveDTO));
        dataService.queue(
                QueueItemDTO.builder()
                        .id(UUID.randomUUID().toString())
                        .createdAt(Instant.now())
                        .hardwareId(hardwareId)
                        .dataObject(enedisELPMapperService.toELP(consumptionLoadCurveDTO))
                        .build());

        // Update last fetched at, only if data was fetched. This is due to the fact that data might
        // not be available at the time of fetching, however it may become available later on.
        deviceService.updateDeviceConfig(hardwareId, EnedisConstants.CONFIG_CLC_LAST_FETCHED_AT,
                Instant.now().toString());

        itemsQueued++;
      } else {
        log.debug("No Consumption Load Curve data to queue.");
      }
    }

    return itemsQueued;
  }

  /**
   * Fetch production load curve data from Enedis API.
   *
   * @param hardwareId  The hardware ID of the device.
   * @param enedisPrm   The Enedis PRM.
   * @param accessToken The access token.
   * @return
   */
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public int fetchProductionLoadCurve(String hardwareId, String enedisPrm, String accessToken) {
    // Fetch data.
    String lastFetch = EnedisUtil.instantToYmd(deviceService
            .getDeviceConfigValueAsInstant(hardwareId, EnedisConstants.CONFIG_PLC_LAST_FETCHED_AT)
            .orElse(Instant.now().minus(Duration.ofDays(enedisProperties.pastDaysInit()))));
    log.debug("Fetching Production Load Curve for device '{}', from '{}'.", hardwareId, lastFetch);
    EnedisProductionLoadCurveDTO productionLoadCurveDTO = null;
    try {
      productionLoadCurveDTO = enedisClient.getProductionLoadCurve(
              lastFetch, EnedisUtil.instantToYmd(Instant.now()),
              enedisPrm, "Bearer " + accessToken);
      resetErrors(hardwareId, EnedisConstants.CONFIG_PLC_ERRORS);
      log.debug("Fetched Production Load Curve '{}'.", productionLoadCurveDTO);
    } catch (Exception e) {
      log.warn("Failed to fetch Production Load Curve for device '{}'.", hardwareId, e);
      increaseErrors(hardwareId, EnedisConstants.CONFIG_PLC_ERRORS);
    }

    // Queue data for processing.
    int itemsQueued = 0;
    if (productionLoadCurveDTO != null) {
      if (!productionLoadCurveDTO.getMeterReading().getIntervalReading().isEmpty()) {
        log.debug("Queuing Production Load Curve:\n{}",
                enedisELPMapperService.toELP(productionLoadCurveDTO));
        dataService.queue(
                QueueItemDTO.builder()
                        .id(UUID.randomUUID().toString())
                        .createdAt(Instant.now())
                        .hardwareId(hardwareId)
                        .dataObject(enedisELPMapperService.toELP(productionLoadCurveDTO))
                        .build());

        // Update last fetched at, only if data was fetched. This is due to the fact that data might
        // not be available at the time of fetching, however it may become available later on.
        deviceService.updateDeviceConfig(hardwareId, EnedisConstants.CONFIG_PLC_LAST_FETCHED_AT,
                Instant.now().toString());

        itemsQueued++;
      } else {
        log.debug("No Production Load Curve data to queue.");
      }
    }

    return itemsQueued;
  }
}
