package esthesis.edge.modules.enedis.routes;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
@ApplicationScoped
public class EnedisRoute extends RouteBuilder {

  @ConfigProperty(name = "esthesis.edge.modules.enedis.enabled")
  boolean isEnabled;

  @ConfigProperty(name = "esthesis.edge.modules.enedis.cron")
  Optional<String> cronExpression;

  @Override
  public void configure() throws Exception {
    if (isEnabled && cronExpression.isPresent() && !cronExpression.get().isEmpty()) {
      log.info("Enedis route is scheduled to run with cron expression '{}'.", cronExpression.get());
//      fromF("quartz://%s?cron=%s", "enedis", cronExpression.get())
//          .routeId("enedis-route")
//          .to("direct:enedis");
    } else {
      log.debug("Enedis route is not scheduled to run.");
    }
  }
}
