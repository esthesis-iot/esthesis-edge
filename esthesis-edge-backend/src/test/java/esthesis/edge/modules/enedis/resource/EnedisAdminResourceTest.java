package esthesis.edge.modules.enedis.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import esthesis.edge.TestUtils;
import esthesis.edge.security.AdminRequestFilter;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

@QuarkusTest
class EnedisAdminResourceTest {

  @ConfigProperty(name = "esthesis.edge.admin-secret")
  String adminSecret;

  @Inject
  TestUtils testUtils;

  @Test
  void getConfiguration() {
    given()
        .header(AdminRequestFilter.ADMIN_SECRET_HEADER_NAME, adminSecret)
        .when().get("/enedis/admin/config")
        .then()
        .statusCode(200)
        .body(not(is(emptyOrNullString())));
  }

  @Test
  void fetchData() {
    given()
        .header(AdminRequestFilter.ADMIN_SECRET_HEADER_NAME, adminSecret)
        .when().get("/enedis/admin/fetch")
        .then()
        .statusCode(200);
  }

  @Test
  void errors() {
    given()
        .header(AdminRequestFilter.ADMIN_SECRET_HEADER_NAME, adminSecret)
        .when().get("/enedis/admin/errors")
        .then()
        .statusCode(200);
  }

  @Test
  void resetErrors() {
    testUtils.createDevice("test");
    given()
        .header(AdminRequestFilter.ADMIN_SECRET_HEADER_NAME, adminSecret)
        .when().put("/enedis/admin/reset-errors?hardwareId=test")
        .then()
        .statusCode(200);
  }
}
