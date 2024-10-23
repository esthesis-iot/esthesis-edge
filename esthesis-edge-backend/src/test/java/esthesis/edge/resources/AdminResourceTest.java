package esthesis.edge.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import esthesis.edge.jobs.PurgeJob;
import esthesis.edge.jobs.SyncJob;
import esthesis.edge.security.AdminRequestFilter;
import esthesis.edge.services.DeviceService;
import esthesis.edge.services.QueueService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AdminResourceTest {

  @ConfigProperty(name = "esthesis.edge.admin-secret")
  String adminSecret;

  @InjectMock
  DeviceService deviceService;

  @InjectMock
  QueueService queueService;

  @InjectMock
  SyncJob syncJob;

  @InjectMock
  PurgeJob purgeJob;

  @Test
  void testAuthOK() {
    given()
        .header(AdminRequestFilter.ADMIN_SECRET_HEADER_NAME, adminSecret)
        .when().get("/admin/auth")
        .then()
        .statusCode(200)
        .body(is("OK"));
  }

  @Test
  void testAuthNOK() {
    given()
        .when().get("/admin/auth")
        .then()
        .statusCode(401);
    given()
        .header(AdminRequestFilter.ADMIN_SECRET_HEADER_NAME, UUID.randomUUID().toString())
        .when().get("/admin/auth")
        .then()
        .statusCode(401);
  }

  @Test
  void testListDevices() {
    given()
        .header(AdminRequestFilter.ADMIN_SECRET_HEADER_NAME, adminSecret)
        .when().get("/admin/devices")
        .then()
        .statusCode(200);
  }

  @Test
  void testDeleteDeviceByHardwareId() {
    given()
        .header(AdminRequestFilter.ADMIN_SECRET_HEADER_NAME, adminSecret)
        .when().delete("/admin/device/1234")
        .then()
        .statusCode(200);
  }

  @Test
  void testDeleteAllDevices() {
    given()
        .header(AdminRequestFilter.ADMIN_SECRET_HEADER_NAME, adminSecret)
        .when().delete("/admin/devices")
        .then()
        .statusCode(200);
  }

  @Test
  void testListQueue() {
    given()
        .header(AdminRequestFilter.ADMIN_SECRET_HEADER_NAME, adminSecret)
        .when().get("/admin/queue")
        .then()
        .statusCode(200);
  }

  @Test
  void testSync() {
    given()
        .header(AdminRequestFilter.ADMIN_SECRET_HEADER_NAME, adminSecret)
        .when().post("/admin/sync")
        .then()
        .statusCode(200);
  }

  @Test
  void testPurge() {
    given()
        .header(AdminRequestFilter.ADMIN_SECRET_HEADER_NAME, adminSecret)
        .when().post("/admin/purge")
        .then()
        .statusCode(200);
  }
}
