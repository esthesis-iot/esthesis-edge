package esthesis.edge.modules.deddie.resource;

import esthesis.common.exception.QProcessingException;
import esthesis.edge.modules.deddie.config.DeddieProperties;
import esthesis.edge.modules.deddie.service.DeddieService;
import io.quarkus.test.Mock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.restassured.http.ContentType;
import io.smallrye.config.SmallRyeConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class DeddiePublicResourceTest {

    @Inject
    SmallRyeConfig smallRyeConfig;

    @Mock
    @ApplicationScoped
    DeddieProperties deddieProperties() {
        return smallRyeConfig.getConfigMapping(DeddieProperties.class);
    }

    @InjectSpy
    DeddieProperties deddieProperties;

    @InjectSpy
    DeddieService deddieService;

    @Test
    void selfRegistrationOK() {
        System.setProperty("esthesis.edge.modules.deddie.enabled", "true");
        given()
                .when().get("/deddie/public/self-registration")
                .then()
                .log().all()
                .statusCode(200)
                .body(not(is(emptyOrNullString())));
    }

    @Test
    void selfRegistrationModuleDisabled() {
        System.setProperty("esthesis.edge.modules.deddie.enabled", "false");

        given()
                .when().get("/deddie/public/self-registration")
                .then()
                .log().all()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void selfRegistrationDisabled() {
        System.setProperty("esthesis.edge.modules.deddie.enabled", "true");
        when(deddieProperties.selfRegistration()).thenReturn(
                mock(DeddieProperties.SelfRegistration.class));
        when(deddieProperties.selfRegistration().enabled()).thenReturn(false);

        given()
                .when().get("/deddie/public/self-registration")
                .then()
                .log().all()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }


    @Test
    void selfRegistrationMaxDevices() {
        System.setProperty("esthesis.edge.modules.deddie.enabled", "true");
        when(deddieProperties.maxDevices()).thenReturn(-1);

        given()
                .when().get("/deddie/public/self-registration")
                .then()
                .log().all()
                .statusCode(Response.Status.TOO_MANY_REQUESTS.getStatusCode());
    }


    @Test
    void redirectHandlerOK() {
        System.setProperty("esthesis.edge.modules.deddie.enabled", "true");
        doNothing().when(deddieService).createDevices(anyString(), anyString(), anyList());

        given()
                .contentType(ContentType.URLENC)
                .formParam("taxNumber", "123456789")
                .formParam("accessToken", "test")
                .formParam("supplyNumbers", "123456789")
                .when()
                .post("/deddie/public/redirect-handler")
                .then()
                .log().all()
                .statusCode(200)
                .body(not(is(emptyOrNullString())));

        verify(deddieService).createDevices(anyString(), anyString(), anyList());
    }

    @Test
    void redirectHandlerNOK() {
        System.setProperty("esthesis.edge.modules.deddie.enabled", "true");

        doThrow(new QProcessingException("Test exception"))
                .when(deddieService).createDevices(anyString(), anyString(), anyList());

        given()
                .contentType(ContentType.URLENC)
                .formParam("taxNumber", "123456789")
                .formParam("accessToken", "test")
                .formParam("supplyNumbers", "123456789")
                .when()
                .post("/deddie/public/redirect-handler")
                .then()
                .log().all()
                .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .body(not(is(emptyOrNullString())));

        verify(deddieService).createDevices(anyString(), anyString(), anyList());
    }

}