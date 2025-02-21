package esthesis.edge.modules.enedis.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import esthesis.common.agent.dto.AgentRegistrationRequest;
import esthesis.common.agent.dto.AgentRegistrationResponse;
import esthesis.edge.TestUtils;
import esthesis.edge.clients.EsthesisAgentServiceClient;
import esthesis.edge.model.DeviceEntity;
import esthesis.edge.modules.enedis.client.EnedisClient;
import esthesis.edge.modules.enedis.config.EnedisConstants;
import esthesis.edge.modules.enedis.dto.datahub.EnedisAuthTokenDTO;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.UUID;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class EnedisServiceTest {

  @InjectMock
  @RestClient
  EnedisClient enedisRestClient;

  @Inject
  EnedisService enedisService;

  @InjectMock
  @RestClient
  EsthesisAgentServiceClient esthesisAgentServiceClient;

  @InjectMock
  EnedisFetchService enedisFetchService;

  @Inject
  TestUtils testUtils;

  @BeforeEach
  public void setup() {
    // Mock the esthesis CORE registration.
    when(esthesisAgentServiceClient.register(any(AgentRegistrationRequest.class)))
        .thenReturn(new AgentRegistrationResponse());
  }

  @Test
  void refreshAuthToken() {
    EnedisAuthTokenDTO dto = new EnedisAuthTokenDTO();
    dto.setAccessToken("test");
    dto.setExpiresOn(1000);
    dto.setScope("test");
    dto.setTokenType("test");

    when(enedisRestClient.getAuthToken(any(String.class), any(String.class), any(String.class)))
        .thenReturn(dto);
    enedisRestClient.getAuthToken("test", "test", "test");
    assertDoesNotThrow(() -> enedisService.refreshAuthToken());
  }

  @Test
  void createDevice() {
    enedisService.createDevice("test");
    assertNotNull(DeviceEntity.findByHardwareId("test"));
  }

  @Test
  void getSelfRegistrationPage() {
    String state = UUID.randomUUID().toString();
    assertTrue(enedisService.getSelfRegistrationPage(state).contains(state));
  }

  @Test
  void getRegistrationSuccessfulPage() {
    assertNotNull(enedisService.getRegistrationSuccessfulPage());
  }

  @Test
  void getErrorPage() {
    assertNotNull(enedisService.getErrorPage());
  }

  @Test
  void countDevices() {
    String hardwareId = UUID.randomUUID().toString();
    testUtils.createDevice(hardwareId);
    assertTrue(enedisService.countDevices() >= 1);
  }

  @Test
  void getFetchErrors() {
    assertNotNull(enedisService.getFetchErrors());
  }

  @Test
  void resetFetchErrors() {
    String hardwareId = UUID.randomUUID().toString();
    testUtils.createDevice(hardwareId);
    assertNotNull(enedisService.resetFetchErrors(hardwareId));
  }

  @Test
  void fetchData() {
    String hardwareId = UUID.randomUUID().toString();
    testUtils.createDevice(hardwareId, EnedisConstants.MODULE_NAME);

    EnedisAuthTokenDTO accessToken = new EnedisAuthTokenDTO();
    accessToken.setAccessToken("test");
    accessToken.setExpiresOn(1000);
    accessToken.setScope("test");
    accessToken.setTokenType("test");
    when(enedisRestClient.getAuthToken(any(String.class), any(String.class), any(String.class)))
        .thenReturn(accessToken);
    testUtils.setDeviceConfig(hardwareId, EnedisConstants.CONFIG_PRM, "test");
    when(enedisFetchService.fetchDailyConsumption(any(String.class), any(String.class),
        any(String.class))).thenReturn(0);
    when(enedisFetchService.fetchDailyConsumptionMaxPower(any(String.class), any(String.class),
        any(String.class))).thenReturn(0);
    when(enedisFetchService.fetchDailyProduction(any(String.class), any(String.class),
        any(String.class))).thenReturn(0);

    assertDoesNotThrow(() -> enedisService.fetchData());
  }

}
