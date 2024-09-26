package esthesis.edge.impl.resource;

import esthesis.edge.api.security.AdminEndpoint;
import esthesis.edge.api.service.DeviceService;
import esthesis.edge.impl.model.DeviceEntity;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/admin")
@RequiredArgsConstructor
public class AdminResource {
  private final DeviceService deviceService;

  @GET
  @AdminEndpoint
  @Path("/devices")
  @Produces("application/json")
  public List<DeviceEntity> listDevices() {
    //return deviceService.listDevices();
    return null;
  }

  @DELETE
  @AdminEndpoint
  @Path("/device/{deviceId}")
  public Response deleteDeviceByDeviceId(@PathParam("deviceId") String deviceId) {
    deviceService.deleteDeviceById(deviceId);

    return Response.ok().build();
  }

  @DELETE
  @AdminEndpoint
  @Path("/devices")
  public Response deleteAllDevices() {
    deviceService.deleteAllDevices();

    return Response.ok().build();
  }
}
