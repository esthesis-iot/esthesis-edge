package esthesis.edge.modules.fronius.service;

import esthesis.edge.dto.QueueItemDTO;
import esthesis.edge.modules.fronius.FroniusUtil;
import esthesis.edge.modules.fronius.client.FroniusClient;
import esthesis.edge.modules.fronius.config.FroniusProperties;
import esthesis.edge.modules.fronius.dto.FroniusPowerFlowRealtimeDataDTO;
import esthesis.edge.services.DeviceService;
import esthesis.edge.services.FetchHelperService;
import esthesis.edge.services.QueueService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static esthesis.edge.modules.fronius.config.FroniusConstants.CONFIG_PFR_ERRORS;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class FroniusFetchService {

    @Inject
    @RestClient
    FroniusClient froniusClient;

    private final FetchHelperService fetchHelperService;
    private final FroniusELPMapperService froniusELPMapperService;
    private final QueueService dataService;
    private final FroniusProperties froniusProperties;
    @Inject
    DeviceService deviceService;


    /**
     * Fetches power flow realtime data from Fronius.
     *
     * @return the number of items queued for processing/sync.
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public int fetchPowerFlowRealtimeData() {
        // Get the hardware ids of the inverters from configuration.
        List<String> invertersHardwareIds = new ArrayList<>(froniusProperties.fetchTypes().pfr().invertersHardwareIds());

        // Filter out disabled devices.
        invertersHardwareIds.removeIf(hardwareId -> {
            boolean deviceEnabled= deviceService.isEnabled(hardwareId);
            if (!deviceEnabled) {
                log.debug("Device with hardware id '{}' is disabled. Skipping data fetch for PFR.", hardwareId);
            }
            return !deviceEnabled;
        });

        // Filter out devices that reached the errors threshold.
        invertersHardwareIds.removeIf(hardwareId -> {
            int pfrErrors = deviceService.getDeviceConfigValueAsString(hardwareId, CONFIG_PFR_ERRORS)
                    .map(Integer::parseInt).orElse(0);
            boolean errorThresholdReached = pfrErrors >= froniusProperties.fetchTypes().pfr().errorsThreshold();

            if (errorThresholdReached) {
                log.debug("Device with hardware id '{}' reached the errors threshold. Skipping data fetch for PFR.", hardwareId);
            }

            return errorThresholdReached;
        });

        // Check if there are any valid devices to fetch data for.
        if (invertersHardwareIds.isEmpty()) {
            log.warn("No valid devices available to fetch data for Power Flow Realtime Data.");
            return 0;
        }

        log.debug("Fetching power flow realtime data from Fronius Solar API.");
        FroniusPowerFlowRealtimeDataDTO powerFlowRealtimeDataDTO = null;
        try {
            powerFlowRealtimeDataDTO = froniusClient.getPowerFlowRealtimeData();
            log.debug("Fetched power flow realtime data from Fronius Solar API: {}", powerFlowRealtimeDataDTO);

        } catch (Exception e) {
            log.warn("Failed to fetch power flow realtime data from Fronius Solar API.", e);
            for (String hardwareId : invertersHardwareIds) {
                fetchHelperService.increaseErrors(hardwareId, CONFIG_PFR_ERRORS);
            }
        }

        AtomicInteger itemsQueued = new AtomicInteger();

        if (powerFlowRealtimeDataDTO == null) {
            log.warn("No Power Flow Realtime Data available to queue.");
            return 0;
        }

        // Extract the timestamp from the response or use the current time if not available.
        Instant timestamp =
                Optional.ofNullable(powerFlowRealtimeDataDTO.getHead().getTimeStamp())
                        .map(FroniusUtil::offsetDateTimeToInstant)
                        .orElse(Instant.now());

        // Extract inverters from the response.
        Map<String, FroniusPowerFlowRealtimeDataDTO.Inverter> inverters =
                powerFlowRealtimeDataDTO.getBody().getData().getInverters();

        // Queue data for each inverter to be processed.
        if (inverters.isEmpty()) {
            log.warn("No Inverters found for Power Flow Realtime Data request.");
            return 0;
        }

        // For each inverter, queue the data to be processed.
        inverters.forEach((inverterId, inverter) -> {
            if (!invertersHardwareIds.isEmpty()) {
                String hardwareId = invertersHardwareIds.removeFirst();


                dataService.queue(
                        QueueItemDTO.builder()
                                .id(UUID.randomUUID().toString())
                                .createdAt(Instant.now())
                                .hardwareId(hardwareId)
                                .dataObject(froniusELPMapperService.toELP(inverter, timestamp))
                                .build());

                fetchHelperService.resetErrors(hardwareId, CONFIG_PFR_ERRORS);

                itemsQueued.getAndIncrement();


            } else {
                log.warn("There's no hardware id available to queue the data for inverter id '{}'", inverterId);
            }
        });

        if(!invertersHardwareIds.isEmpty()){
            log.warn("There are hardware ids available that were not used to queue data: '{}'", invertersHardwareIds);
        }


        return itemsQueued.get();

    }
}
