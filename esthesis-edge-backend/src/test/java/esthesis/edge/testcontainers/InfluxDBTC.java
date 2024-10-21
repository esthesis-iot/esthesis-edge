package esthesis.edge.testcontainers;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.InfluxDBContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;

@Slf4j
public class InfluxDBTC implements QuarkusTestResourceLifecycleManager {

  GenericContainer container;

  @Override
  public Map<String, String> start() {
    container = new InfluxDBContainer(
        DockerImageName.parse("influxdb").withTag("2.7.10"))
        .withAdminToken("test")
        .withBucket("edge")
        .withOrganization("esthesis")
        .withExposedPorts(8086);

    container.start();
    container.followOutput(new Slf4jLogConsumer(log));

    return Map.of("esthesis.edge.local.influx-db.url",
        "http://localhost:" + container.getFirstMappedPort().toString(),
        "esthesis.edge.local.influx-db.token", "test");

  }

  @Override
  public void stop() {
    container.stop();
  }
}
