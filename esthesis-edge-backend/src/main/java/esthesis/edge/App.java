package esthesis.edge;

import esthesis.common.banner.BannerUtil;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class App {

  void onStart(@Observes StartupEvent ev) {
    BannerUtil.showBanner("esthesis EDGE");
  }
}
