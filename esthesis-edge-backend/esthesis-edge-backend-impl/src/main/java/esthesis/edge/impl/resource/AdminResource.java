package esthesis.edge.impl.resource;

import esthesis.edge.api.dto.DeviceDTO;
import esthesis.edge.api.dto.QueueItemDTO;
import esthesis.edge.api.security.AdminEndpoint;
import esthesis.edge.api.service.DataService;
import esthesis.edge.api.service.DeviceService;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
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
  private final DataService dataService;

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
    return dataService.list();
  }

}
