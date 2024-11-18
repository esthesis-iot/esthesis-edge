package esthesis.edge;

import esthesis.common.banner.BannerUtil;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.ws.rs.core.Application;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

/**
 * The main application class for esthesis EDGE.
 */
@Slf4j
@ApplicationScoped
@OpenAPIDefinition(
    info = @Info(
        title = "esthesis EDGE - API",
        version = "",
        contact = @Contact(
            name = "esthesis",
            url = "https://esthes.is",
            email = "esthesis@eurodyn.com")),
    security = @SecurityRequirement(name = "X-ESTHESIS-EDGE-ADMIN-SECRET")
)
public class App extends Application {

  void onStart(@Observes StartupEvent ev) {
    BannerUtil.showBanner("esthesis EDGE");
  }
}
