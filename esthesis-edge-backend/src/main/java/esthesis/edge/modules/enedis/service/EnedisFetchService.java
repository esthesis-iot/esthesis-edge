package esthesis.edge.modules.enedis.service;

import esthesis.edge.dto.QueueItemDTO;
import esthesis.edge.modules.enedis.client.EnedisClient;
import esthesis.edge.modules.enedis.config.EnedisConstants;
import esthesis.edge.modules.enedis.config.EnedisProperties;
import esthesis.edge.modules.enedis.dto.datahub.EnedisMesureDTO;
import esthesis.edge.services.DeviceService;
import esthesis.edge.services.FetchHelperService;
import esthesis.edge.services.QueueService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import static esthesis.edge.modules.enedis.config.EnedisConstants.MAX_PAST_DAYS_LOAD_CURVE;

/**
 * A service to fetch data from the Enedis API.
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class EnedisFetchService {

  // The maximum number of days a single load curve request may span, as per the Enedis API.
  private static final int MAX_LOAD_CURVE_WINDOW_DAYS = 7;

  @Inject
  @RestClient
  @SuppressWarnings("java:S6813")
  EnedisClient enedisClient;

  private final DeviceService deviceService;
  private final QueueService dataService;
  private final EnedisProperties enedisProperties;
  private final EnedisELPMapperService enedisELPMapperService;
  private final FetchHelperService fetchHelperService;

  /**
   * A call to one of the Enedis mesure_synchrone_auto sub-resources for a date window.
   */
  @FunctionalInterface
  private interface MesureCall {

    EnedisMesureDTO fetch(String startDate, String endDate);
  }

  /**
   * Fetch daily consumption data from Enedis API.
   *
   * @param hardwareId  The hardware ID of the device.
   * @param enedisPrm   The Enedis PRM.
   * @param accessToken The access token.
   * @return The number of items queued.
   */
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public int fetchDailyConsumption(String hardwareId, String enedisPrm, String accessToken) {
    return fetchAndQueue("Daily Consumption", hardwareId,
        EnedisConstants.CONFIG_DC_LAST_FETCHED_AT, EnedisConstants.CONFIG_DC_ERRORS,
        enedisProperties.pastDaysInit(), false,
        (start, end) -> enedisClient.getDailyConsumption(start, end, enedisPrm,
            "Bearer " + accessToken),
        enedisProperties.fetchTypes().dc().category(),
        enedisProperties.fetchTypes().dc().measurement());
  }

  /**
   * Fetch daily consumption max power data from Enedis API.
   *
   * @param hardwareId  The hardware ID of the device.
   * @param enedisPrm   The Enedis PRM.
   * @param accessToken The access token.
   * @return The number of items queued.
   */
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public int fetchDailyConsumptionMaxPower(String hardwareId, String enedisPrm,
      String accessToken) {
    return fetchAndQueue("Daily Consumption Max Power", hardwareId,
        EnedisConstants.CONFIG_DCMP_LAST_FETCHED_AT, EnedisConstants.CONFIG_DCMP_ERRORS,
        enedisProperties.pastDaysInit(), false,
        (start, end) -> enedisClient.getDailyConsumptionMaxPower(start, end, enedisPrm,
            enedisProperties.fetchTypes().dcmp().measuringPeriod(),
            enedisProperties.fetchTypes().dcmp().physicalQuantity(), "Bearer " + accessToken),
        enedisProperties.fetchTypes().dcmp().category(),
        enedisProperties.fetchTypes().dcmp().measurement());
  }

  /**
   * Fetch daily production data from Enedis API.
   *
   * @param hardwareId  The hardware ID of the device.
   * @param enedisPrm   The Enedis PRM.
   * @param accessToken The access token.
   * @return The number of items queued.
   */
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public int fetchDailyProduction(String hardwareId, String enedisPrm, String accessToken) {
    return fetchAndQueue("Daily Production", hardwareId,
        EnedisConstants.CONFIG_DP_LAST_FETCHED_AT, EnedisConstants.CONFIG_DP_ERRORS,
        enedisProperties.pastDaysInit(), false,
        (start, end) -> enedisClient.getDailyProduction(start, end, enedisPrm,
            "Bearer " + accessToken),
        enedisProperties.fetchTypes().dp().category(),
        enedisProperties.fetchTypes().dp().measurement());
  }

  /**
   * Fetch consumption load curve data from Enedis API.
   *
   * @param hardwareId  The hardware ID of the device.
   * @param enedisPrm   The Enedis PRM.
   * @param accessToken The access token.
   * @return The number of items queued.
   */
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public int fetchConsumptionLoadCurve(String hardwareId, String enedisPrm, String accessToken) {
    return fetchAndQueue("Consumption Load Curve", hardwareId,
        EnedisConstants.CONFIG_CLC_LAST_FETCHED_AT, EnedisConstants.CONFIG_CLC_ERRORS,
        Math.min(enedisProperties.pastDaysInit(), MAX_PAST_DAYS_LOAD_CURVE), true,
        (start, end) -> enedisClient.getConsumptionLoadCurve(start, end, enedisPrm,
            "Bearer " + accessToken),
        enedisProperties.fetchTypes().clc().category(),
        enedisProperties.fetchTypes().clc().measurement());
  }

  /**
   * Fetch production load curve data from Enedis API.
   *
   * @param hardwareId  The hardware ID of the device.
   * @param enedisPrm   The Enedis PRM.
   * @param accessToken The access token.
   * @return The number of items queued.
   */
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public int fetchProductionLoadCurve(String hardwareId, String enedisPrm, String accessToken) {
    return fetchAndQueue("Production Load Curve", hardwareId,
        EnedisConstants.CONFIG_PLC_LAST_FETCHED_AT, EnedisConstants.CONFIG_PLC_ERRORS,
        Math.min(enedisProperties.pastDaysInit(), MAX_PAST_DAYS_LOAD_CURVE), true,
        (start, end) -> enedisClient.getProductionLoadCurve(start, end, enedisPrm,
            "Bearer " + accessToken),
        enedisProperties.fetchTypes().plc().category(),
        enedisProperties.fetchTypes().plc().measurement());
  }

  /**
   * Fetches one Enedis sub-resource for a device, maps the response to eLP and queues it. A failure
   * of either the client call or the mapping is counted as an error against the given errors key
   * and never propagates, so that the error counter is committed with the surrounding transaction.
   *
   * @param label          A description of the data fetched, used in logs.
   * @param hardwareId     The hardware ID of the device.
   * @param lastFetchedKey The device configuration key holding the last fetched at timestamp.
   * @param errorsKey      The device configuration key holding the errors counter.
   * @param pastDaysInit   The number of days in the past to fetch from, when never fetched before.
   * @param loadCurve      Whether the request window is limited to the maximum window of a load
   *                       curve request. In that case, the last fetched timestamp becomes the end
   *                       of the window, so that a longer gap is caught up over multiple runs.
   * @param call           The call to the Enedis API.
   * @param category       The eLP category.
   * @param measurement    The eLP measurement.
   * @return The number of items queued.
   */
  private int fetchAndQueue(String label, String hardwareId, String lastFetchedKey,
      String errorsKey, int pastDaysInit, boolean loadCurve, MesureCall call, String category,
      String measurement) {
    Instant now = Instant.now();
    // Dates are taken in UTC, as the stored load curve last fetched at is UTC midnight and would
    // otherwise read back as the previous day on a JVM with a negative time zone offset.
    LocalDate start = deviceService
        .getDeviceConfigValueAsInstant(hardwareId, lastFetchedKey)
        .orElse(now.minus(Duration.ofDays(pastDaysInit)))
        .atZone(ZoneOffset.UTC).toLocalDate();
    LocalDate today = now.atZone(ZoneOffset.UTC).toLocalDate();
    LocalDate end = loadCurve ? min(today, start.plusDays(MAX_LOAD_CURVE_WINDOW_DAYS)) : today;
    // Enedis rejects a request whose end date is not after its start date.
    if (!start.isBefore(end)) {
      log.debug("Nothing to fetch yet for {} of device '{}', last fetched on '{}'.", label,
          hardwareId, start);
      return 0;
    }
    log.debug("Fetching {} for device '{}', from '{}' to '{}'.", label, hardwareId, start, end);

    String elp;
    try {
      EnedisMesureDTO dto = call.fetch(start.toString(), end.toString());
      log.debug("Fetched {} '{}'.", label, dto);
      elp = enedisELPMapperService.toELP(dto, category, measurement);
    } catch (Exception e) {
      log.warn("Failed to fetch {} for device '{}'.", label, hardwareId, e);
      fetchHelperService.increaseErrors(hardwareId, errorsKey);
      return 0;
    }
    fetchHelperService.resetErrors(hardwareId, errorsKey);

    // Update last fetched at only if data was fetched. This is due to the fact that data might
    // not be available at the time of fetching, however it may become available later on. The
    // exception is a load curve window that ended before today: it is final, so an empty one
    // is skipped, otherwise it would be requested forever and block the catch-up.
    if (elp == null || elp.isBlank()) {
      log.debug("No {} data to queue.", label);
      if (loadCurve && end.isBefore(today)) {
        deviceService.updateDeviceConfig(hardwareId, lastFetchedKey,
            end.atStartOfDay(ZoneOffset.UTC).toInstant().toString());
      }
      return 0;
    }

    log.debug("Queuing {}:\n{}", label, elp);
    dataService.queue(
        QueueItemDTO.builder()
            .id(UUID.randomUUID().toString())
            .createdAt(Instant.now())
            .hardwareId(hardwareId)
            .dataObject(elp)
            .build());
    deviceService.updateDeviceConfig(hardwareId, lastFetchedKey,
        loadCurve ? end.atStartOfDay(ZoneOffset.UTC).toInstant().toString()
            : Instant.now().toString());

    return 1;
  }

  private static LocalDate min(LocalDate a, LocalDate b) {
    return a.isBefore(b) ? a : b;
  }
}
