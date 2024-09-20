package esthesis.edge;

import esthesis.common.banner.BannerUtil;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class App implements QuarkusApplication {

  @Override
  public int run(String... args) throws Exception {
    BannerUtil.showBanner("esthesis EDGE");

    return 0;
  }
}
