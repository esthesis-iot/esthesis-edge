package esthesis.edge.testcontainers;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import lombok.SneakyThrows;
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
  @SneakyThrows
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

    String s = loadResourceFileAsText("mqtt/ca.crt");
    System.out.println(s);

    return Map.of(
        "test.mqtt.port", container.getFirstMappedPort().toString(),
        "esthesis.edge.core.push.url",
        "ssl://localhost:" + container.getFirstMappedPort().toString(),
        "esthesis.edge.core.cert",
        Base64.getEncoder().encodeToString(loadResourceFileAsText("mqtt/ca.crt").getBytes(
            StandardCharsets.UTF_8))
    );
  }

  @Override
  public void stop() {
    container.stop();
  }
}
