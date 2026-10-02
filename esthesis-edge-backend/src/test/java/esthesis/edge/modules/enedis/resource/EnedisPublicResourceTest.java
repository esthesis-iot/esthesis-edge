package esthesis.edge.modules.enedis.resource;

import esthesis.common.exception.QProcessingException;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
    void redirectHandlerMissingParams() {
        given()
                .when().get("/enedis/public/redirect-handler?State=1&code=1")
                .then()
                .statusCode(Status.BAD_REQUEST.getStatusCode())
                .body(not(is(emptyOrNullString())));
        verify(enedisService, never()).createDevice(any());
    }

    @Test
    void redirectHandlerCreateDeviceFailure() {
        doThrow(new QProcessingException("Failed to parse Enedis contract data."))
                .when(enedisService).createDevice(any(String.class));
        given()
                .when().get("/enedis/public/redirect-handler?State=1&usage_point_id=1&code=1")
                .then()
                .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .body(not(is(emptyOrNullString())));
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
    void redirectHandlerLowercaseStateWithStateChecking() {
        doNothing().when(enedisService).createDevice(any(String.class));
        when(enedisClient.getAuthToken(any(String.class), any(String.class), any(String.class)))
                .thenReturn(new EnedisAuthTokenDTO().setAccessToken("test").setScope("test").setTokenType("test"));
        when(enedisClient.getSubscribedServices(any(EnedisSubscribedServicesRequestDTO.class), any(String.class)))
                .thenReturn(new EnedisSubscribedServicesResponseDTO().setServiceSouscrit(List.of(
                        new EnedisSubscribedServicesResponseDTO.ServiceSouscritDTO().setPointId("1")
                )));

        // Render the self-registration page (state checking is off in the test profile) to
        // generate a known state.
        String page = given()
                .when().get("/enedis/public/self-registration")
                .then()
                .statusCode(200)
                .extract().asString();
        Matcher matcher = Pattern.compile("state=([0-9a-f-]+)").matcher(page);
        assertTrue(matcher.find(), "The self-registration page must embed the generated state.");
        String state = matcher.group(1);

        // Enable state checking, the way redirectHandlerInvalidState does, but delegating to the
        // real configuration so that the success page can still be rendered.
        EnedisProperties.SelfRegistration selfRegistration = mock(
                EnedisProperties.SelfRegistration.class,
                delegatesTo(enedisProperties.selfRegistration()));
        when(enedisProperties.selfRegistration()).thenReturn(selfRegistration);
        when(selfRegistration.stateChecking()).thenReturn(true);

        given()
                .when().get("/enedis/public/redirect-handler?autorisation_id=1&state=" + state)
                .then()
                .statusCode(200);
        verify(enedisService).createDevice("1");

        // State checking really is active: an unknown state is still rejected.
        given()
                .when().get("/enedis/public/redirect-handler?autorisation_id=1&state=unknown")
                .then()
                .statusCode(Status.BAD_REQUEST.getStatusCode());
        verify(enedisService, times(1)).createDevice(any());
    }

    @Test
    void redirectHandlerConsentRefused() {
        doNothing().when(enedisService).createDevice(any(String.class));
        given()
                .when().get("/enedis/public/redirect-handler"
                        + "?error=invalid_request&error_description=demande_non_aboutie&state=1")
                .then()
                .statusCode(Status.BAD_REQUEST.getStatusCode())
                .body(containsString("Error connecting account"));
        verify(enedisService, never()).createDevice(any());
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
