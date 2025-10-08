package esthesis.edge.modules.enedis.resource;

import esthesis.edge.modules.enedis.client.EnedisClient;
import esthesis.edge.modules.enedis.config.EnedisProperties;
import esthesis.edge.modules.enedis.dto.datahub.EnedisAuthTokenDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisSubscribedServicesRequestDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisSubscribedServicesResponseDTO;
import esthesis.edge.modules.enedis.service.EnedisService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.Mock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.smallrye.config.SmallRyeConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response.Status;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class EnedisPublicResourceTest {

    @Inject
    SmallRyeConfig smallRyeConfig;

    @Mock
    @ApplicationScoped
    EnedisProperties enedisProperties() {
        return smallRyeConfig.getConfigMapping(EnedisProperties.class);
    }

    @InjectSpy
    EnedisProperties enedisProperties;

    @InjectSpy
    EnedisService enedisService;

    @InjectMock
    @RestClient
    EnedisClient enedisClient;

    @BeforeEach
    void setUp() {
        System.setProperty("esthesis.edge.modules.enedis.enabled", "true");
    }

    @Test
    void selfRegistrationOK() {
        given()
                .when().get("/enedis/public/self-registration")
                .then()
                .statusCode(200)
                .body(not(is(emptyOrNullString())));
    }

    @Test
    void selfRegistrationModuleDisabled() {
        System.setProperty("esthesis.edge.modules.enedis.enabled", "false");
        given()
                .when().get("/enedis/public/self-registration")
                .then()
                .statusCode(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void selfRegistrationDisabled() {
        when(enedisProperties.selfRegistration()).thenReturn(
                mock(EnedisProperties.SelfRegistration.class));
        when(enedisProperties.selfRegistration().enabled()).thenReturn(false);
        given()
                .when().get("/enedis/public/self-registration")
                .then()
                .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void selfRegistrationMaxDevices() {
        when(enedisProperties.maxDevices()).thenReturn(-1);
        given()
                .when().get("/enedis/public/self-registration")
                .then()
                .statusCode(Status.TOO_MANY_REQUESTS.getStatusCode());
    }

    @Test
    void redirectHandlerUsagePointId() {
        doNothing().when(enedisService).createDevice(any(String.class));
        given()
                .when().get("/enedis/public/redirect-handler?State=1&usage_point_id=1&code=1")
                .then()
                .statusCode(200);
        verify(enedisService).createDevice("1");
    }

    @Test
    void redirectHandlerAuthorisationId() {
        doNothing().when(enedisService).createDevice(any(String.class));

        when(enedisClient.getAuthToken(any(String.class), any(String.class), any(String.class)))
                .thenReturn(new EnedisAuthTokenDTO().setAccessToken("test").setScope("test").setTokenType("test"));

        when(enedisClient.getSubscribedServices(any(EnedisSubscribedServicesRequestDTO.class), any(String.class)))
                .thenReturn(new EnedisSubscribedServicesResponseDTO().setServiceSouscrit(List.of(
                        new EnedisSubscribedServicesResponseDTO.ServiceSouscritDTO().setPointId("1")
                )));

        given()
                .when().get("/enedis/public/redirect-handler?State=1&autorisation_id=1&code=1")
                .then()
                .statusCode(200);

        verify(enedisService).createDevice("1");
    }

    @Test
    void redirectHandlerInvalidState() {
        doNothing().when(enedisService).createDevice(any(String.class));
        when(enedisProperties.selfRegistration()).thenReturn(
                mock(EnedisProperties.SelfRegistration.class));
        when(enedisProperties.selfRegistration().stateChecking()).thenReturn(true);
        given()
                .when().get("/enedis/public/redirect-handler?State=1&usage_point_id=1&code=1")
                .then()
                .statusCode(Status.BAD_REQUEST.getStatusCode())
                .body(not(is(emptyOrNullString())));
    }

    @Test
    void redirectHandlerMaxDevices() {
        doNothing().when(enedisService).createDevice(any(String.class));
        when(enedisProperties.maxDevices()).thenReturn(-1);
        given()
                .when().get("/enedis/public/redirect-handler?State=1&usage_point_id=1&code=1")
                .then()
                .statusCode(Status.TOO_MANY_REQUESTS.getStatusCode())
                .body(not(is(emptyOrNullString())));
    }
}
