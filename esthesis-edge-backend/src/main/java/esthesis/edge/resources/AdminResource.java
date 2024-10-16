package esthesis.edge.resources;

import esthesis.edge.dto.DeviceDTO;
import esthesis.edge.dto.QueueItemDTO;
import esthesis.edge.jobs.SyncJob;
import esthesis.edge.security.AdminEndpoint;
import esthesis.edge.services.DeviceService;
import esthesis.edge.services.QueueService;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AdminResource is a REST resource that provides endpoints for administrative tasks.
 */
@Slf4j
@Path("/admin")
@RequiredArgsConstructor
public class AdminResource {

  private final DeviceService deviceService;
  private final QueueService queueService;
  private final SyncJob syncJob;

  /**
   * Endpoint to check if the admin endpoint is working.
   *
   * @return "OK" if the endpoint is working.
   */
  @GET
  @AdminEndpoint
  @Path("/auth")
  @Produces("text/plain")
  public String auth() {
    return "OK";
  }

  @GET
  @AdminEndpoint
  @Path("/devices")
  @Produces("application/json")
  public List<DeviceDTO> listDevices() {
    return deviceService.listDevices();
  }

  @DELETE
  @AdminEndpoint
  @Path("/device/{hardwareId}")
  public Response deleteDeviceByHardwareId(@PathParam("hardwareId") String hardwareId) {
    deviceService.deleteDevice(hardwareId);

    return Response.ok().build();
  }

  @DELETE
  @AdminEndpoint
  @Path("/devices")
  public Response deleteAllDevices() {
    deviceService.deleteAllDevices();

    return Response.ok().build();
  }

  @GET
  @AdminEndpoint
  @Path("/queue")
  @Produces("application/json")
  public List<QueueItemDTO> listQueue() {
    return queueService.list();
  }

  @POST
  @AdminEndpoint
  @Path("/sync")
  @Produces("application/json")
  public void sync() {
    syncJob.sync();
  }

}
