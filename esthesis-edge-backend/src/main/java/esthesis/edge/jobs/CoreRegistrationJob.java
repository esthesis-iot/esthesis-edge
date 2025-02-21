package esthesis.edge.jobs;

import esthesis.edge.config.EdgeProperties;
import esthesis.edge.services.DeviceService;
import esthesis.edge.services.EsthesisCoreService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static esthesis.edge.modules.enedis.config.EnedisConstants.MODULE_NAME;

/**
 * Job for registering pending devices with esthesis CORE.
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class CoreRegistrationJob {

    private final EsthesisCoreService esthesisCoreService;
    private final DeviceService deviceService;
    private final EdgeProperties edgeProperties;

    /**
     * Executes the registration of pending devices with esthesis CORE JOB.
     */
    @Scheduled(cron = "{esthesis.edge.core.registration.cron}")
    public void execute() {
        if (edgeProperties.core().registration().enabled()) {
            log.debug("Core registration job started.");
            deviceService.listDevicesPendingCoreRegistration(MODULE_NAME).forEach(device -> {
                try {
                    // Retrieve the tags for the device.
                    List<String> tags = device.getTags() != null ? List.of(device.getTags().split(",")) : List.of();

                    // Call the esthesis CORE service to register the device.
                    esthesisCoreService.registerDevice(device.getHardwareId(), tags);
                } catch (Exception e) {
                    log.error("Failed to register device '{}'.", device.getHardwareId(), e);
                }
            });
            log.debug("Core registration job finished.");
        } else {
            log.debug("Core registration job skipped due to CORE registration being disabled.");
        }
    }
}
