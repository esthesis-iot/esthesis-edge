package esthesis.edge.modules.enedis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.edge.api.dto.QueueItemDTO;
import esthesis.edge.api.service.DataService;
import esthesis.edge.api.service.DeviceService;
import esthesis.edge.modules.enedis.EnedisConstants;
import esthesis.edge.modules.enedis.EnedisUtil;
import esthesis.edge.modules.enedis.client.EnedisClient;
import esthesis.edge.modules.enedis.dto.datahub.EnedisDailyConsumptionDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
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

  private final Instance<DeviceService> deviceService;
  private final Instance<DataService> dataService;
  private final ObjectMapper objectMapper;

  /**
   * Fetch daily consumption data from Enedis API.
   * @param hardwareId The hardware ID of the device.
   * @param enedisPrm The Enedis PRM.
   */
  public void fetchDailyConsumption(String hardwareId, String enedisPrm) {
    // Fetch data.
    String lastFetch = EnedisUtil.InstantToYmd(deviceService.get()
        .getDeviceConfigValueAsInstant(hardwareId, EnedisConstants.CONFIG_CQ_LAST_FETCHED_AT)
        .orElse(Instant.EPOCH));
    log.debug("Fetching Daily Consumption for device '{}', from '{}'.", hardwareId, lastFetch);
//      EnedisDailyConsumptionDTO dailyConsumption = enedisRestClient.getDailyConsumption(
//          lastFetch, EnedisUtil.InstantToYYYYMMDD(Instant.now()),
//          enedisPrm, "Bearer " + enedisAuthTokenDTO.getAccessToken());
    EnedisDailyConsumptionDTO dailyConsumptionDTO = null;
    try {
      dailyConsumptionDTO = objectMapper.readValue(MockData.CQD, EnedisDailyConsumptionDTO.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    log.debug("Fetched Daily Consumption '{}'.", dailyConsumptionDTO);

    // Queue data for processing.
    log.debug("Queuing Daily Consumption:\n{}", dailyConsumptionDTO.toELP());
    dataService.get().queue(
        QueueItemDTO.builder()
            .id(UUID.randomUUID().toString())
            .createdAt(Instant.now())
            .hardwareId(hardwareId)
            .dataObject(dailyConsumptionDTO.toELP())
            .build());
  }
}
