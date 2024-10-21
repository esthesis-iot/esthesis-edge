package esthesis.edge.testcontainers;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.hivemq.HiveMQContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@Slf4j
public class HiveMQSSLTC implements QuarkusTestResourceLifecycleManager {

  HiveMQContainer container;

  private static File loadResourceFile(String resourceFileName) {
    String resourcePath = Objects.requireNonNull(
        HiveMQSSLTC.class.getClassLoader().getResource(resourceFileName)).getPath();
    return new File(resourcePath);
  }

  public static String loadResourceFileAsText(String resourceFileName) throws IOException {
    return new String(
        java.nio.file.Files.readAllBytes(loadResourceFile(resourceFileName).toPath()));
  }

  @Override
  public Map<String, String> start() {
    File keystore = loadResourceFile("mqtt/server-keystore.jks");
    File truststore = loadResourceFile("mqtt/server-truststore.jks");
    File config = loadResourceFile("mqtt/config.xml");

    container = new HiveMQContainer(
        DockerImageName.parse("hivemq/hivemq-ce").withTag("2024.7"))
        .withCopyFileToContainer(
            MountableFile.forHostPath(keystore.getAbsolutePath()),
            "/opt/hivemq/certs/server-keystore.jks")
        .withCopyFileToContainer(
            MountableFile.forHostPath(truststore.getAbsolutePath()),
            "/opt/hivemq/certs/server-truststore.jks")
        .withCopyFileToContainer(
            MountableFile.forHostPath(config.getAbsolutePath()),
            "/opt/hivemq/conf/config.xml")
        .withExposedPorts(8883);
    container.start();
    container.followOutput(new Slf4jLogConsumer(log));

    return Map.of("test.mqtt.port", container.getFirstMappedPort().toString());
  }

  @Override
  public void stop() {
    container.stop();
  }
}
