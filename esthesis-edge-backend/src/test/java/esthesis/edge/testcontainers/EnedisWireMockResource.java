package esthesis.edge.testcontainers;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Map;

/**
 * Starts a WireMock server impersonating the Enedis API gateway and points the EnedisClient REST
 * client at it, so tests exercise the real REST client (paths, query parameters, request bodies
 * and Jackson deserialization) instead of a Mockito mock.
 *
 * <p>Response fixtures live under src/test/resources/wiremock/enedis/__files and replay the
 * payloads of the official Enedis sandbox mocks (see the "Guide d'utilisation des mocks" recette
 * document).
 */
public class EnedisWireMockResource implements QuarkusTestResourceLifecycleManager {

  private static WireMockServer wireMockServer;

  public static WireMockServer getServer() {
    return wireMockServer;
  }

  @Override
  public Map<String, String> start() {
    wireMockServer = new WireMockServer(
        wireMockConfig().dynamicPort().usingFilesUnderClasspath("wiremock/enedis"));
    wireMockServer.start();

    return Map.of("quarkus.rest-client.EnedisClient.url", wireMockServer.baseUrl());
  }

  @Override
  public void stop() {
    if (wireMockServer != null) {
      wireMockServer.stop();
      wireMockServer = null;
    }
  }
}
