package esthesis.edge.modules.fronius;

import esthesis.edge.modules.fronius.config.FroniusProperties;
import esthesis.edge.modules.fronius.service.FroniusService;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class FroniusModule {

    private final FroniusProperties froniusProperties;
    private final FroniusService froniusService;

    public void onStart(@Observes StartupEvent event) {
        if (froniusProperties.enabled()) {
            log.info("Fronius module is enabled.");
            froniusService.updateDevices();
        }
    }
}
