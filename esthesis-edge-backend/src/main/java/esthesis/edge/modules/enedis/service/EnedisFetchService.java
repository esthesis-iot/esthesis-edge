package esthesis.edge.modules.enedis.service;

import esthesis.edge.dto.QueueItemDTO;
import esthesis.edge.modules.enedis.EnedisUtil;
import esthesis.edge.modules.enedis.client.EnedisClient;
import esthesis.edge.modules.enedis.config.EnedisConstants;
import esthesis.edge.modules.enedis.config.EnedisProperties;
import esthesis.edge.modules.enedis.dto.datahub.EnedisDailyConsumptionDTO;
import esthesis.edge.services.DeviceService;
import esthesis.edge.services.QueueService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * A service to fetch data from Enedis API.
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class EnedisFetchService {

  @RestClient
  @Inject
  EnedisClient enedisRestClient;

  private final DeviceService deviceService;
  private final QueueService dataService;
  private final EnedisProperties enedisProperties;

  /**
   * Fetch daily consumption data from Enedis API.
   *
   * @param hardwareId  The hardware ID of the device.
   * @param enedisPrm   The Enedis PRM.
   * @param accessToken The access token.
   */
  public void fetchDailyConsumption(String hardwareId, String enedisPrm, String accessToken) {
    // Fetch data.
    String lastFetch = EnedisUtil.InstantToYmd(deviceService
        .getDeviceConfigValueAsInstant(hardwareId, EnedisConstants.CONFIG_DC_LAST_FETCHED_AT)
        .orElse(Instant.now().minus(Duration.ofDays(enedisProperties.pastDaysInit()))));
    log.debug("Fetching Daily Consumption for device '{}', from '{}'.", hardwareId, lastFetch);
    EnedisDailyConsumptionDTO dailyConsumptionDTO = enedisRestClient.getDailyConsumption(
        lastFetch, EnedisUtil.InstantToYmd(Instant.now()),
        enedisPrm, "Bearer " + accessToken);
    log.debug("Fetched Daily Consumption '{}'.", dailyConsumptionDTO);

    // Queue data for processing.
    if (!dailyConsumptionDTO.getMeterReading().getIntervalReading().isEmpty()) {
      log.debug("Queuing Daily Consumption:\n{}", dailyConsumptionDTO.toELP());
      dataService.queue(
          QueueItemDTO.builder()
              .id(UUID.randomUUID().toString())
              .createdAt(Instant.now())
              .hardwareId(hardwareId)
              .dataObject(dailyConsumptionDTO.toELP())
              .build());

      // Update last fetched at, only if data was fetched. This is due to the fact that data might
      // not be available at the time of fetching, however it may become available later on.
      deviceService.updateDeviceConfig(hardwareId, EnedisConstants.CONFIG_DC_LAST_FETCHED_AT,
          Instant.now().toString());
    } else {
      log.debug("No Daily Consumption data to queue.");
    }
  }
}
