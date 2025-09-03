package esthesis.edge.modules.deddie;

import esthesis.edge.modules.deddie.config.DeddieProperties;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class DeddieModule {

    private final DeddieProperties deddieProperties;

    public void onStart(@Observes StartupEvent ev) {
        if (deddieProperties.enabled()) {
            log.info("Deddie module is enabled.");
        }
    }
}
