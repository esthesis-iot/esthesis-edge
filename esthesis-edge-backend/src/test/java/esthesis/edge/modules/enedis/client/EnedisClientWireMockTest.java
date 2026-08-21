package esthesis.edge.modules.enedis.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.WireMockServer;
import esthesis.common.agent.dto.AgentRegistrationRequest;
import esthesis.common.agent.dto.AgentRegistrationResponse;
import esthesis.edge.clients.EsthesisAgentServiceClient;
import esthesis.edge.model.DeviceEntity;
import esthesis.edge.modules.enedis.dto.datahub.EnedisAlimentationAutoDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisAuthTokenDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisDailyConsumptionMaxPowerDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisDonneesGeneralesAutoDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisSituationContractAutoDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisSynthContractAutoDTO;
import esthesis.edge.testcontainers.EnedisWireMockResource;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response.Status;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration tests exercising the REAL EnedisClient REST client against a WireMock server that
 * replays the payloads of the official Enedis sandbox mocks. Unlike the Mockito-based tests,
 * these verify the endpoint paths, query parameters, request bodies and the wire-level Jackson
 * deserialization of the new DataConnect APIs.
 */
@QuarkusTest
@QuarkusTestResource(value = EnedisWireMockResource.class, restrictToAnnotatedClass = true)
class EnedisClientWireMockTest {

  private static final String BEARER = "Bearer mock-access-token";

  @Inject
  @RestClient
  EnedisClient enedisClient;

  @InjectMock
  @RestClient
  EsthesisAgentServiceClient esthesisAgentServiceClient;

  WireMockServer wireMock;

  @BeforeEach
  void setup() {
    wireMock = EnedisWireMockResource.getServer();
    wireMock.resetAll();

    when(esthesisAgentServiceClient.register(any(AgentRegistrationRequest.class)))
        .thenReturn(new AgentRegistrationResponse());

    wireMock.stubFor(post(urlPathEqualTo("/oauth2/v3/token"))
        .willReturn(aResponse().withHeader("Content-Type", "application/json")
            .withBodyFile("token.json")));
  }

  @Test
  void getAuthTokenParsesToken() {
    EnedisAuthTokenDTO token = enedisClient.getAuthToken("client_credentials", "id", "secret");

    assertEquals("mock-access-token", token.getAccessToken());
    wireMock.verify(postRequestedFor(urlPathEqualTo("/oauth2/v3/token"))
        .withRequestBody(containing("grant_type=client_credentials")));
  }

  @Test
  void getSubscribedServicesParsesPointId() {
    wireMock.stubFor(post(urlPathEqualTo("/subscribed_services/v1"))
        .withRequestBody(matchingJsonPath("$.autorisationId", equalTo("11")))
        .willReturn(aResponse().withHeader("Content-Type", "application/json")
            .withBodyFile("subscribed-services-11.json")));

    var response = enedisClient.getSubscribedServices(
        new esthesis.edge.modules.enedis.dto.datahub.EnedisSubscribedServicesRequestDTO()
            .setAutorisationId(11L).setEtatCode(List.of("ACTIF")).setServiceType("ACCES")
            .setComptage(false), BEARER);

    assertEquals(5, response.getNbTotalServices());
    assertEquals("14000000000001", response.getServiceSouscrit().getFirst().getPointId());
    assertEquals(11L,
        response.getServiceSouscrit().getFirst().getAutorisation().getAutorisationId());
  }

  @Test
  void getSituationContractAutoParsesVaryingElements() {
    stubItcGet("situation_contrat_auto", "99999999999999", "situation-contrat-99999999999999.json");
    stubItcGet("situation_contrat_auto", "11111111111111", "situation-contrat-11111111111111.json");

    List<EnedisSituationContractAutoDTO> singlePhase =
        enedisClient.getSituationContractAuto(BEARER, "99999999999999");
    assertEquals(1, singlePhase.size());
    assertEquals("C1", singlePhase.getFirst().getSegment());
    assertEquals("850", singlePhase.getFirst().getSubscribedPower().getValue());
    // The live sandbox spells this "balance_responsible_party" (unlike the swagger).
    assertEquals("ACM_X", singlePhase.getFirst().getBalanceResponsableParty());
    assertEquals(5, singlePhase.getFirst().getDistributionTariffProfile().size());

    // The sandbox mock returns two contracts with widely varying field presence.
    List<EnedisSituationContractAutoDTO> threePhase =
        enedisClient.getSituationContractAuto(BEARER, "11111111111111");
    assertEquals(2, threePhase.size());
    assertEquals("C2", threePhase.get(0).getSegment());
    assertEquals("P1", threePhase.get(1).getSegment());
  }

  @Test
  void getSituationContractAutoParsesWithProperJsonContentType() {
    // The Enedis swaggers declare application/json even though the gateway currently sends
    // application/octet-stream on GETs. Should Enedis ever fix the header to match their spec,
    // EnedisJsonResponseFilter must stay a no-op — responses labelled as JSON (with or without
    // a charset suffix) must keep parsing identically.
    wireMock.stubFor(get(urlPathEqualTo("/situation_contrat_auto/v1/99999999999999"))
        .willReturn(aResponse().withHeader("Content-Type", "application/json;charset=UTF-8")
            .withBodyFile("situation-contrat-99999999999999.json")));

    List<EnedisSituationContractAutoDTO> contracts =
        enedisClient.getSituationContractAuto(BEARER, "99999999999999");
    assertEquals(1, contracts.size());
    assertEquals("C1", contracts.getFirst().getSegment());
    assertEquals("ACM_X", contracts.getFirst().getBalanceResponsableParty());
  }

  @Test
  void getDailyConsumptionMaxPowerParsesWithProperJsonContentType() {
    wireMock.stubFor(
        get(urlPathEqualTo("/mesure_synchrone_auto/v1/metering_data/daily_consumption_max_power"))
            .withQueryParam("grandeurPhysique", equalTo("PMA"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json")
                .withBodyFile("dcmp-pma.json")));

    EnedisDailyConsumptionMaxPowerDTO dto = enedisClient.getDailyConsumptionMaxPower(
        "2025-05-01", "2025-07-14", "99999999999999", "P1D", "PMA", BEARER);
    assertEquals(List.of("9656"), dto.getMeterReading().getIntervalReading().getFirst().getValue());
  }

  @Test
  void getSynthContractAutoParses() {
    stubItcGet("synth_contrat_auto", "99999999999999", "synth-contrat.json");

    EnedisSynthContractAutoDTO dto = enedisClient.getSynthContractAuto(BEARER, "99999999999999");
    assertEquals(2, dto.getSegments().size());
    assertEquals("2021-03-01T00:00:00+0100", dto.getConsumptionLastActivationDate());
  }

  @Test
  void getDonneesGeneralesAutoParses() {
    stubItcGet("donnees_generales_auto", "99999999999999", "donnees-generales.json");

    EnedisDonneesGeneralesAutoDTO dto =
        enedisClient.getDonneesGeneralesAuto(BEARER, "99999999999999");
    assertEquals("1 rue du test", dto.getAddress().getNumberStreetName());
  }

  @Test
  void getAlimentationAutoParses() {
    stubItcGet("alimentation_auto", "99999999999999", "alimentation.json");

    EnedisAlimentationAutoDTO dto = enedisClient.getAlimentationAuto(BEARER, "99999999999999");
    assertEquals("Alimenté", dto.getConnectionState());
    assertEquals(999999999L, dto.getSerialNumber());
    assertEquals(1100.0, dto.getConsumptionConnectionPower().getValue());
  }

  @Test
  void getDailyConsumptionMaxPowerPmaScalars() {
    wireMock.stubFor(
        get(urlPathEqualTo("/mesure_synchrone_auto/v1/metering_data/daily_consumption_max_power"))
            .withQueryParam("usage_point_id", equalTo("99999999999999"))
            .withQueryParam("measuring_period", equalTo("P1D"))
            .withQueryParam("grandeurPhysique", equalTo("PMA"))
            .willReturn(aResponse().withHeader("Content-Type", "application/octet-stream")
                .withBodyFile("dcmp-pma.json")));

    EnedisDailyConsumptionMaxPowerDTO dto = enedisClient.getDailyConsumptionMaxPower(
        "2025-05-01", "2025-07-14", "99999999999999", "P1D", "PMA", BEARER);

    assertEquals(List.of("VA"), dto.getMeterReading().getReadingType().getUnit());
    assertEquals(3, dto.getMeterReading().getIntervalReading().size());
    assertEquals(List.of("9656"), dto.getMeterReading().getIntervalReading().getFirst().getValue());
  }

  @Test
  void getDailyConsumptionMaxPowerToutArrays() {
    wireMock.stubFor(
        get(urlPathEqualTo("/mesure_synchrone_auto/v1/metering_data/daily_consumption_max_power"))
            .withQueryParam("usage_point_id", equalTo("11111111111111"))
            .withQueryParam("measuring_period", equalTo("P1D"))
            .withQueryParam("grandeurPhysique", equalTo("TOUT"))
            .willReturn(aResponse().withHeader("Content-Type", "application/octet-stream")
                .withBodyFile("dcmp-tout.json")));

    EnedisDailyConsumptionMaxPowerDTO dto = enedisClient.getDailyConsumptionMaxPower(
        "2025-05-01", "2025-07-14", "11111111111111", "P1D", "TOUT", BEARER);

    assertEquals(4, dto.getMeterReading().getReadingType().getUnit().size());
    assertEquals(List.of("9656", "9724", "9298", "9904"),
        dto.getMeterReading().getIntervalReading().getFirst().getValue());
  }

  @Test
  void redirectHandlerAutorisationIdEndToEnd() {
    // Full new-flow chain through the real REST client: autorisation_id=11 is exchanged for
    // PRM 14000000000001 via subscribed_services, whose contract situation is then fetched.
    wireMock.stubFor(post(urlPathEqualTo("/subscribed_services/v1"))
        .withRequestBody(matchingJsonPath("$.autorisationId", equalTo("11")))
        .withRequestBody(matchingJsonPath("$.comptage", equalTo("false")))
        .willReturn(aResponse().withHeader("Content-Type", "application/json")
            .withBodyFile("subscribed-services-11.json")));
    stubItcGet("situation_contrat_auto", "14000000000001", "situation-contrat-14000000000001.json");
    stubItcGet("synth_contrat_auto", "14000000000001", "synth-contrat.json");
    stubItcGet("alimentation_auto", "14000000000001", "alimentation.json");
    stubItcGet("donnees_generales_auto", "14000000000001", "donnees-generales.json");

    given()
        .when().get("/enedis/public/redirect-handler?State=1&autorisation_id=11&code=1")
        .then()
        .statusCode(200);

    DeviceEntity device = DeviceEntity.findByHardwareId("enedis-14000000000001").orElseThrow();
    assertNotNull(device.getAttributes());
    assertTrue(device.getAttributes().contains("segment=C5"));
    assertTrue(device.getAttributes().contains("usagePointStatus=Alimenté"));
  }

  @Test
  void redirectHandlerAutorisationIdWithoutServicesReturns400() {
    // The sandbox mock for autorisationId=10 returns no subscribed services.
    wireMock.stubFor(post(urlPathEqualTo("/subscribed_services/v1"))
        .withRequestBody(matchingJsonPath("$.autorisationId", equalTo("10")))
        .willReturn(aResponse().withHeader("Content-Type", "application/json")
            .withBodyFile("subscribed-services-10.json")));

    given()
        .when().get("/enedis/public/redirect-handler?State=1&autorisation_id=10&code=1")
        .then()
        .statusCode(Status.BAD_REQUEST.getStatusCode());
  }

  private void stubItcGet(String api, String usagePointId, String bodyFile) {
    // EnedisJsonResponseFilter must make these mappable anyway.
    wireMock.stubFor(get(urlPathEqualTo("/" + api + "/v1/" + usagePointId))
        .willReturn(aResponse().withHeader("Content-Type", "application/octet-stream")
            .withBodyFile(bodyFile)));
  }
}
