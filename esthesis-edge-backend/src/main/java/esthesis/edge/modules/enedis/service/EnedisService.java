package esthesis.edge.modules.enedis.service;

import static esthesis.edge.config.EdgeConstants.EDGE;
import static esthesis.edge.modules.enedis.config.EnedisConstants.MODULE_NAME;

import esthesis.common.exception.QProcessingException;
import esthesis.edge.dto.DeviceDTO;
import esthesis.edge.dto.DeviceDTO.DeviceDTOBuilder;
import esthesis.edge.dto.TemplateDTO;
import esthesis.edge.model.DeviceEntity;
import esthesis.edge.model.DeviceModuleConfigEntity;
import esthesis.edge.modules.enedis.client.EnedisClient;
import esthesis.edge.modules.enedis.config.EnedisConstants;
import esthesis.edge.modules.enedis.config.EnedisProperties;
import esthesis.edge.modules.enedis.dto.datahub.EnedisAlimentationAutoDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisAuthTokenDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisDonneesGeneralesAutoDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisSituationContractAutoDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisSubscribedServicesRequestDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisSubscribedServicesResponseDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisSynthContractAutoDTO;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private static final String RATE_LIMIT_REACHED = "Enedis rate limit reached, stopping this "
            + "fetch run at device '{}'; the remaining data is fetched on the next run.";

    // A local reference of the access token, to not keep refreshing when not needed.
    private EnedisAuthTokenDTO enedisAuthTokenDTO;
    // Rate limiters for Enedis API.
    private RateLimiter perSecondLimiter = createPerSecondLimiter();
    private RateLimiter perHourLimiter = createPerHourLimiter();

    /**
     * Create the per-second Enedis rate limiter. A caller waits at most 2 seconds for a permit.
     *
     * @return The rate limiter.
     */
    static RateLimiter createPerSecondLimiter() {
        return RateLimiter.of("perSecondLimiter",
                RateLimiterConfig.custom()
                        .limitForPeriod(EnedisConstants.REQUESTS_PER_SECOND)
                        .limitRefreshPeriod(Duration.ofSeconds(1))
                        .timeoutDuration(Duration.ofSeconds(2))
                        .build());
    }

    /**
     * Create the per-hour Enedis rate limiter. A caller waits up to the next refresh (1 hour) for
     * a permit once the hourly quota is used up.
     *
     * @return The rate limiter.
     */
    static RateLimiter createPerHourLimiter() {
        return RateLimiter.of("perHourLimiter",
                RateLimiterConfig.custom()
                        .limitForPeriod(EnedisConstants.REQUESTS_PER_HOUR)
                        .limitRefreshPeriod(Duration.ofHours(1))
                        .timeoutDuration(Duration.ofHours(1))
                        .build());
    }

    /**
     * Replace the rate limiters. Only meant for tests.
     *
     * @param perSecondLimiter The per-second rate limiter.
     * @param perHourLimiter   The per-hour rate limiter.
     */
    void setRateLimiters(RateLimiter perSecondLimiter, RateLimiter perHourLimiter) {
        this.perSecondLimiter = perSecondLimiter;
        this.perHourLimiter = perHourLimiter;
    }

    /**
     * Acquire a permit for one Enedis API call from both the per-hour and the per-second rate
     * limiters. The per-hour permit is requested first (it may involve a long wait), so that the
     * per-second limit is enforced at call time, even after waiting for the hourly refresh. The
     * per-second permit is only requested once the per-hour one is granted, so a per-second
     * denial after the hourly acquire costs one hourly permit.
     *
     * @return True if both permits were granted, false if either was denied.
     */
    private boolean acquireEnedisPermit() {
        return perHourLimiter.acquirePermission() && perSecondLimiter.acquirePermission();
    }

    /**
     * Acquire a permit for one Enedis API call on the registration path, failing the current
     * operation if it is denied. Registration runs on a request thread (inside a transaction), so
     * it never waits for the hourly quota to refresh: when no per-hour permit is available right
     * now, it fails immediately.
     */
    private void requireEnedisPermit() {
        // Check-then-acquire race: another thread may take the last permit in between, in which
        // case acquireEnedisPermit() waits; reservePermission() would close this gap.
        if (perHourLimiter.getMetrics().getAvailablePermissions() <= 0) {
            throw new QProcessingException("Enedis hourly request quota reached, try again later.");
        }
        if (!acquireEnedisPermit()) {
            throw new QProcessingException("Enedis rate limit reached, please try again later.");
        }
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
            enedisAuthTokenDTO = enedisClient.getAuthToken("client_credentials",
                    enedisProperties.clientId(), enedisProperties.clientSecret());
            log.debug("Access token refreshed '{}'.", enedisAuthTokenDTO);
        } else {
            log.debug("Previously obtained access token is still valid, re-using it.");
        }
    }

    /**
     * Create a new device for the given usage point ID. Multiple PRMs can be registered at once by
     * separating them with a semicolon.
     *
     * @param usagePointId The usage point ID.
     */
    @Transactional
    public void createDevice(String usagePointId) {
        // Create a device for each PRM.
        for (String enedisId : usagePointId.split(";")) {
            Instant now = Instant.now();
            DeviceDTOBuilder deviceDTOBuilder = DeviceDTO.builder()
                    .hardwareId(createHardwareId(enedisId))
                    .moduleName(MODULE_NAME)
                    .createdAt(now)
                    .enabled(true)
                    .config(EnedisConstants.CONFIG_PRM, enedisId)
                    .config(EnedisConstants.CONFIG_PMR_ENABLED_AT, now.toString())
                    .config(EnedisConstants.CONFIG_PMR_EXPIRES_AT, calculatePMRExpiration(now).toString());

            refreshAuthToken();
            Set<String> segments = applyContractAttributes(deviceDTOBuilder, enedisId);
            applySynthContractAttributes(deviceDTOBuilder, enedisId,
                    segments.contains(EnedisConstants.SEGMENT_TYPE_CONSUMER),
                    segments.contains(EnedisConstants.SEGMENT_TYPE_PRODUCER));
            applyAlimentationAttributes(deviceDTOBuilder, enedisId);
            applyGeneralDataAttributes(deviceDTOBuilder, enedisId);

            // Create the device.
            deviceDTOBuilder.tags(String.join(",", MODULE_NAME, EDGE));
            deviceService.createDevice(deviceDTOBuilder.build());
        }
    }

    /**
     * Fetch the contractual situation of the given PRM and populate the device configuration
     * (consumer/producer) and the device attributes shared with esthesis CORE. A PRM that both
     * consumes and injects is returned with one contract per segment (e.g. C5 and P4), in which
     * case it is registered as both a consumer and a producer. At least one supported segment is
     * required to schedule data fetching; every other field is optional (Enedis may omit any of
     * them depending on the contract).
     *
     * @param deviceDTOBuilder The device builder to populate.
     * @param enedisId         The PRM.
     * @return The supported segments found for the PRM, in the order consumer, producer.
     */
    private Set<String> applyContractAttributes(DeviceDTOBuilder deviceDTOBuilder, String enedisId) {
        requireEnedisPermit();
        try {
            List<EnedisSituationContractAutoDTO> contracts = enedisClient.getSituationContractAuto(
                    "Bearer " + enedisAuthTokenDTO.getAccessToken(), enedisId);
            if (contracts == null || contracts.isEmpty()) {
                throw new QProcessingException(
                        "No contract situation returned by Enedis for PRM '{}'.", enedisId);
            }

            // Check if the PRM is for a producer, a consumer, or both (one contract per segment).
            Optional<EnedisSituationContractAutoDTO> consumerContract = findContractBySegment(
                    contracts, EnedisConstants.SEGMENT_TYPE_CONSUMER);
            Optional<EnedisSituationContractAutoDTO> producerContract = findContractBySegment(
                    contracts, EnedisConstants.SEGMENT_TYPE_PRODUCER);
            if (consumerContract.isEmpty() && producerContract.isEmpty()) {
                throw new QProcessingException("Unknown segment type '{}' for PRM '{}'.",
                        contracts.stream().map(EnedisSituationContractAutoDTO::getSegment)
                                .filter(Objects::nonNull).collect(Collectors.joining(",")), enedisId);
            }
            Set<String> segments = new LinkedHashSet<>();
            if (consumerContract.isPresent()) {
                deviceDTOBuilder.config(EnedisConstants.CONFIG_CONSUMER, "true");
                segments.add(EnedisConstants.SEGMENT_TYPE_CONSUMER);
            }
            if (producerContract.isPresent()) {
                deviceDTOBuilder.config(EnedisConstants.CONFIG_PRODUCER, "true");
                segments.add(EnedisConstants.SEGMENT_TYPE_PRODUCER);
            }
            deviceDTOBuilder.attribute("segment", String.join("/", segments));

            // The general contract attributes come from the consumer contract, if any.
            EnedisSituationContractAutoDTO contractDTO = consumerContract
                    .orElseGet(producerContract::orElseThrow);

            // Add Enedis device attributes for esthesis CORE.
            String subscribedPower = contractDTO.getSubscribedPower() != null
                    ? StringUtils.defaultString(contractDTO.getSubscribedPower().getValue())
                    + StringUtils.defaultString(contractDTO.getSubscribedPower().getUnit()) : "";
            String distributionTariffProfiles = contractDTO.getDistributionTariffProfile() == null
                    ? "" : contractDTO.getDistributionTariffProfile().stream()
                    .filter(tariffProfile -> tariffProfile.getPower() != null)
                    .map(tariffProfile -> tariffProfile.getName() + " "
                            + StringUtils.defaultString(tariffProfile.getPower().getValue())
                            + StringUtils.defaultString(tariffProfile.getPower().getUnit()))
                    .collect(Collectors.joining(","));

            setAttributeIfNotBlank(deviceDTOBuilder, "contractType", contractDTO.getContractType());
            setAttributeIfNotBlank(deviceDTOBuilder, "balanceResponsableParty",
                    contractDTO.getBalanceResponsableParty());
            setAttributeIfNotBlank(deviceDTOBuilder, "contractor", contractDTO.getContractor());
            setAttributeIfNotBlank(deviceDTOBuilder, "contractStart", contractDTO.getContractStart());
            setAttributeIfNotBlank(deviceDTOBuilder, "distributionTariff",
                    contractDTO.getDistributionTariff());
            setAttributeIfNotBlank(deviceDTOBuilder, "distributionMobilePeak",
                    contractDTO.getDistributionMobilePeak());
            setAttributeIfNotBlank(deviceDTOBuilder, "pricingStructure",
                    contractDTO.getPricingStructure());
            setAttributeIfNotBlank(deviceDTOBuilder, "supplierMobilePeak",
                    contractDTO.getSupplierMobilePeak());
            setAttributeIfNotBlank(deviceDTOBuilder, "subscribedPower", subscribedPower);
            setAttributeIfNotBlank(deviceDTOBuilder, "distributionTariffProfiles",
                    distributionTariffProfiles);

            return segments;
        } catch (QProcessingException e) {
            throw e;
        } catch (Exception e) {
            throw new QProcessingException(
                    "Failed to parse Enedis contract data for PRM '" + enedisId + "'.", e);
        }
    }

    /**
     * Find the first contract of the given segment.
     *
     * @param contracts The contracts returned by Enedis for a PRM.
     * @param segment   The segment to look for.
     * @return The contract of the given segment, if any.
     */
    private static Optional<EnedisSituationContractAutoDTO> findContractBySegment(
            List<EnedisSituationContractAutoDTO> contracts, String segment) {
        return contracts.stream().filter(contract -> segment.equals(contract.getSegment()))
                .findFirst();
    }

    /**
     * Fetch the contractual summary of the given PRM and populate the meterType,
     * lastActivationDate and (for PRMs that both consume and inject) generationLastActivationDate
     * device attributes. Failures are logged and skipped, registration only requires the
     * contractual situation.
     *
     * @param deviceDTOBuilder The device builder to populate.
     * @param enedisId         The PRM.
     * @param consumer         Whether the PRM is a consumer, to pick the relevant activation date.
     * @param producer         Whether the PRM is a producer.
     */
    private void applySynthContractAttributes(DeviceDTOBuilder deviceDTOBuilder, String enedisId,
            boolean consumer, boolean producer) {
        try {
            // Inside the try: a denied permit (e.g. hourly quota reached) skips this optional step.
            requireEnedisPermit();
            EnedisSynthContractAutoDTO synthContract = enedisClient.getSynthContractAuto(
                    "Bearer " + enedisAuthTokenDTO.getAccessToken(), enedisId);
            if (synthContract == null) {
                return;
            }
            setAttributeIfNotBlank(deviceDTOBuilder, "meterType", synthContract.getServicesLevel());
            String lastActivationDate = consumer
                    ? synthContract.getConsumptionLastActivationDate()
                    : synthContract.getGenerationLastActivationDate();
            setAttributeIfNotBlank(deviceDTOBuilder, "lastActivationDate", lastActivationDate);
            if (consumer && producer) {
                setAttributeIfNotBlank(deviceDTOBuilder, "generationLastActivationDate",
                        synthContract.getGenerationLastActivationDate());
            }
            setAttributeIfNotBlank(deviceDTOBuilder, "lastSubscribedPowerChangeDate",
                    synthContract.getLastSubscribedPowerChangeDate());
        } catch (Exception e) {
            log.warn("Could not fetch Enedis contractual summary for PRM '{}', skipping the "
                    + "related device attributes.", enedisId, e);
        }
    }

    /**
     * Fetch the supply situation of the given PRM and populate the usagePointStatus and related
     * device attributes. Failures are logged and skipped.
     *
     * @param deviceDTOBuilder The device builder to populate.
     * @param enedisId         The PRM.
     */
    private void applyAlimentationAttributes(DeviceDTOBuilder deviceDTOBuilder, String enedisId) {
        try {
            // Inside the try: a denied permit (e.g. hourly quota reached) skips this optional step.
            requireEnedisPermit();
            EnedisAlimentationAutoDTO alimentation = enedisClient.getAlimentationAuto(
                    "Bearer " + enedisAuthTokenDTO.getAccessToken(), enedisId);
            if (alimentation == null) {
                return;
            }
            setAttributeIfNotBlank(deviceDTOBuilder, "usagePointStatus",
                    alimentation.getConnectionState());
            setAttributeIfNotBlank(deviceDTOBuilder, "voltageLevel", alimentation.getVoltageLevel());
            setAttributeIfNotBlank(deviceDTOBuilder, "phaseCount", alimentation.getPhaseCount());
            if (alimentation.getSerialNumber() != null) {
                deviceDTOBuilder.attribute("serialNumber",
                        String.valueOf(alimentation.getSerialNumber()));
            }
        } catch (Exception e) {
            log.warn("Could not fetch Enedis supply situation for PRM '{}', skipping the related "
                    + "device attributes.", enedisId, e);
        }
    }

    /**
     * Fetch the general data of the given PRM and populate the installation address device
     * attribute. Failures are logged and skipped.
     *
     * @param deviceDTOBuilder The device builder to populate.
     * @param enedisId         The PRM.
     */
    private void applyGeneralDataAttributes(DeviceDTOBuilder deviceDTOBuilder, String enedisId) {
        try {
            // Inside the try: a denied permit (e.g. hourly quota reached) skips this optional step.
            requireEnedisPermit();
            EnedisDonneesGeneralesAutoDTO generalData = enedisClient.getDonneesGeneralesAuto(
                    "Bearer " + enedisAuthTokenDTO.getAccessToken(), enedisId);
            if (generalData == null || generalData.getAddress() == null) {
                return;
            }
            EnedisDonneesGeneralesAutoDTO.Address address = generalData.getAddress();
            String installationAddress = Stream.of(address.getStaircaseFloorApartment(),
                            address.getBuilding(), address.getNumberStreetName(), address.getLocality(),
                            address.getPostalCodeCity())
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining(" "));
            setAttributeIfNotBlank(deviceDTOBuilder, "installationAddress", installationAddress);
            setAttributeIfNotBlank(deviceDTOBuilder, "inseeCode", address.getInseeCode());
        } catch (Exception e) {
            log.warn("Could not fetch Enedis general data for PRM '{}', skipping the related "
                    + "device attributes.", enedisId, e);
        }
    }

    private static void setAttributeIfNotBlank(DeviceDTOBuilder deviceDTOBuilder, String name,
            String value) {
        if (StringUtils.isNotBlank(value)) {
            deviceDTOBuilder.attribute(name, value);
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
                .data("authorizationUrl", enedisProperties.selfRegistration().authorizationUrl())
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
     * Fetches new data from Enedis API. When a rate limit permit is denied, the run stops without
     * touching the error counters nor the last fetch timestamps; the remaining devices are fetched
     * on the next run.
     */
    @Scheduled(cron = "{esthesis.edge.modules.enedis.cron}",
            concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
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
                    if (!acquireEnedisPermit()) {
                        log.warn(RATE_LIMIT_REACHED, hardwareId);
                        return;
                    }
                    // Waiting for the permit may have outlived the access token.
                    refreshAuthToken();
                    int itemsQueued = enedisFetchService.fetchDailyConsumption(hardwareId, enedisPrm,
                            enedisAuthTokenDTO.getAccessToken());
                    log.debug("Queued '{}' items from Daily Consumption API.", itemsQueued);
                }

                // Daily Consumption Max Power.
                int dcmpErrors = deviceService.getDeviceConfigValueAsString(hardwareId,
                        EnedisConstants.CONFIG_DCMP_ERRORS).map(Integer::parseInt).orElse(0);
                if (enedisProperties.fetchTypes().dcmp().enabled()
                        && dcmpErrors < enedisProperties.fetchTypes().dcmp().errorsThreshold()) {
                    if (!acquireEnedisPermit()) {
                        log.warn(RATE_LIMIT_REACHED, hardwareId);
                        return;
                    }
                    // Waiting for the permit may have outlived the access token.
                    refreshAuthToken();
                    int itemsQueued = enedisFetchService.fetchDailyConsumptionMaxPower(hardwareId, enedisPrm,
                            enedisAuthTokenDTO.getAccessToken());
                    log.debug("Queued '{}' items from Daily Consumption Max Power API.", itemsQueued);
                }

                // Consumption Load Curve.
                int clcErrors = deviceService.getDeviceConfigValueAsString(hardwareId,
                        EnedisConstants.CONFIG_CLC_ERRORS).map(Integer::parseInt).orElse(0);
                if (enedisProperties.fetchTypes().clc().enabled() &&
                        clcErrors < enedisProperties.fetchTypes().clc().errorsThreshold()) {
                    if (!acquireEnedisPermit()) {
                        log.warn(RATE_LIMIT_REACHED, hardwareId);
                        return;
                    }
                    // Waiting for the permit may have outlived the access token.
                    refreshAuthToken();
                    int itemsQueued = enedisFetchService.fetchConsumptionLoadCurve(hardwareId, enedisPrm,
                            enedisAuthTokenDTO.getAccessToken());
                    log.debug("Queued '{}' items from Consumption Load Curve API.", itemsQueued);
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
                    if (!acquireEnedisPermit()) {
                        log.warn(RATE_LIMIT_REACHED, hardwareId);
                        return;
                    }
                    // Waiting for the permit may have outlived the access token.
                    refreshAuthToken();
                    int itemsQueued = enedisFetchService.fetchDailyProduction(hardwareId, enedisPrm,
                            enedisAuthTokenDTO.getAccessToken());
                    log.debug("Queued '{}' items from Daily Production API.", itemsQueued);
                }

                // Production Load Curve.
                int plcErrors = deviceService.getDeviceConfigValueAsString(hardwareId,
                        EnedisConstants.CONFIG_PLC_ERRORS).map(Integer::parseInt).orElse(0);
                if (enedisProperties.fetchTypes().plc().enabled() &&
                        plcErrors < enedisProperties.fetchTypes().plc().errorsThreshold()) {
                    if (!acquireEnedisPermit()) {
                        log.warn(RATE_LIMIT_REACHED, hardwareId);
                        return;
                    }
                    // Waiting for the permit may have outlived the access token.
                    refreshAuthToken();
                    int itemsQueued = enedisFetchService.fetchProductionLoadCurve(hardwareId, enedisPrm,
                            enedisAuthTokenDTO.getAccessToken());
                    log.debug("Queued '{}' items from Production Load Curve API.", itemsQueued);
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
                                "(configKey = 'dp_errors' and CAST(configValue AS INTEGER) >= ?3) or " +
                                "(configKey = 'clc_errors' and CAST(configValue AS INTEGER) >= ?4) or " +
                                "(configKey = 'plc_errors' and CAST(configValue AS INTEGER) >= ?5)",
                        enedisProperties.fetchTypes().dc().errorsThreshold(),
                        enedisProperties.fetchTypes().dcmp().errorsThreshold(),
                        enedisProperties.fetchTypes().dp().errorsThreshold(),
                        enedisProperties.fetchTypes().clc().errorsThreshold(),
                        enedisProperties.fetchTypes().plc().errorsThreshold())
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
        DeviceModuleConfigEntity.updateConfigValue(hardwareId, EnedisConstants.CONFIG_CLC_ERRORS, "0");
        DeviceModuleConfigEntity.updateConfigValue(hardwareId, EnedisConstants.CONFIG_PLC_ERRORS, "0");

        return DeviceEntity.findByHardwareId(hardwareId).orElseThrow();
    }

    /**
     * Fetch the usage point ID (Enedis PRM) associated with the given authorization ID, by
     * querying the Enedis subscribed services API.
     *
     * @param authorizationId The authorization ID received on the redirect callback.
     * @return The usage point ID (PRM).
     */
    public String fetchUsagePointId(Long authorizationId) {
        refreshAuthToken();

        // comptage must be false: true returns only the number of matching services, without the
        // serviceSouscrit list carrying the PRM.
        EnedisSubscribedServicesRequestDTO request = new EnedisSubscribedServicesRequestDTO()
                .setAutorisationId(authorizationId)
                .setEtatCode(List.of("ACTIF"))
                .setServiceType("ACCES")
                .setComptage(false);

        requireEnedisPermit();
        EnedisSubscribedServicesResponseDTO response = enedisClient.getSubscribedServices(request,
                "Bearer " + enedisAuthTokenDTO.getAccessToken());

        if (response == null || response.getServiceSouscrit() == null
                || response.getServiceSouscrit().isEmpty()) {
            throw new QProcessingException(
                    "No active subscribed service found for authorization id '{}'.", authorizationId);
        }
        // Several services for one PRM are normal (one per measure type), several PRMs are not.
        long distinctPointIds = response.getServiceSouscrit().stream()
                .map(EnedisSubscribedServicesResponseDTO.ServiceSouscritDTO::getPointId)
                .distinct().count();
        if (distinctPointIds > 1) {
            log.warn("Subscribed services for '{}' distinct usage points returned for "
                    + "authorization id '{}', using the first one.",
                    distinctPointIds, authorizationId);
        }

        return response.getServiceSouscrit().getFirst().getPointId();
    }
}
