package esthesis.edge.modules.fronius.service;

import esthesis.edge.dto.DeviceDTO;
import esthesis.edge.modules.fronius.config.FroniusProperties;
import esthesis.edge.services.DeviceService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static esthesis.edge.config.EdgeConstants.EDGE;
import static esthesis.edge.modules.fronius.config.FroniusConstants.MODULE_NAME;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class FroniusService {

    private final FroniusProperties froniusProperties;
    private final FroniusFetchService froniusFetchService;
    private final DeviceService deviceService;


    @Scheduled(cron = "{esthesis.edge.modules.fronius.cron}")
    public void fetchData() {
        if (!froniusProperties.enabled()) {
            return;
        }
        log.debug("Fetching data from Fronius...");

        // Fetch Power Flow Realtime Data.
        if (froniusProperties.fetchTypes().pfr().enabled()) {
            int queuedItems = froniusFetchService.fetchPowerFlowRealtimeData();
            log.debug("Queued {} items from  Power Flow Realtime API ", queuedItems);
        }

    }

    @Transactional
    public void updateDevices() {
        List<String> hardwareIds = new ArrayList<>();

        // Power Flow Realtime Inverters devices.
        if (froniusProperties.fetchTypes().pfr().enabled()) {
            hardwareIds.addAll(froniusProperties.fetchTypes().pfr().invertersHardwareIds());
        }

        // Retrieve existing devices from the database.
        List<DeviceDTO> existingDevices = deviceService.listDevices(MODULE_NAME);
        List<String> existingHardwareIds = existingDevices.stream().map(DeviceDTO::getHardwareId).toList();

        // Filter new hardware IDs to be registered.
        List<String> newHardwareIds = hardwareIds.stream()
                .filter(hardwareId -> !existingHardwareIds.contains(hardwareId))
                .toList();

        // Register new devices in the database.
        newHardwareIds.forEach(this::createDevice);

        // Check devices that are not in the configuration anymore and should be disabled.
        List<String> devicesToDisable = existingHardwareIds.stream()
                .filter(hardwareId -> !hardwareIds.contains(hardwareId))
                .toList();

        devicesToDisable.forEach(deviceService::disableDevice);

        // Check devices that were disabled but are now in the configuration and should be enabled.
        List<String> devicesToEnable = existingDevices.stream()
                .filter(device -> !device.getEnabled())
                .filter(device -> hardwareIds.contains(device.getHardwareId()))
                .map(DeviceDTO::getHardwareId)
                .toList();

        devicesToEnable.forEach(deviceService::enableDevice);

    }

    private void createDevice(String hardwareId) {
        DeviceDTO.DeviceDTOBuilder deviceDTOBuilder = DeviceDTO.builder()
                .hardwareId(hardwareId)
                .createdAt(Instant.now())
                .enabled(true)
                .moduleName(MODULE_NAME);

        deviceDTOBuilder.tags(String.join(",", MODULE_NAME, EDGE));
        deviceService.createDevice(deviceDTOBuilder.build());
    }


}
