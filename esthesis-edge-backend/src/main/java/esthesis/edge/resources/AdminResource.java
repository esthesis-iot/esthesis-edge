package esthesis.edge.resources;

import esthesis.edge.dto.DeviceDTO;
import esthesis.edge.dto.QueueItemDTO;
import esthesis.edge.jobs.PurgeJob;
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
 * Administration resource for EDGE. Access to the resources defined here need to use the configured
 * secret as a Bearer Authorization header.
 */
@Slf4j
@Path("/admin")
@RequiredArgsConstructor
public class AdminResource {

  private final DeviceService deviceService;
  private final QueueService queueService;
  private final SyncJob syncJob;
  private final PurgeJob purgeJob;

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

  /**
   * Lists all devices registered in the system.
   *
   * @return A list of devices.
   */
  @GET
  @AdminEndpoint
  @Path("/devices")
  @Produces("application/json")
  public List<DeviceDTO> listDevices() {
    return deviceService.listDevices();
  }

  /**
   * Deletes a device by its hardware ID. Note that the device is not deleted in esthesis CORE.
   *
   * @param hardwareId The hardware ID of the device to delete.
   * @return 200 OK if the device was deleted.
   */
  @DELETE
  @AdminEndpoint
  @Path("/device/{hardwareId}")
  public Response deleteDeviceByHardwareId(@PathParam("hardwareId") String hardwareId) {
    deviceService.deleteDevice(hardwareId);

    return Response.ok().build();
  }

  /**
   * Deletes all devices registered in the system. Note that the devices are not deleted in
   * esthesis
   *
   * @return 200 OK if the devices were deleted.
   */
  @DELETE
  @AdminEndpoint
  @Path("/devices")
  public Response deleteAllDevices() {
    deviceService.deleteAllDevices();

    return Response.ok().build();
  }

  /**
   * Lists all items in the queue.
   *
   * @return A list of queue items.
   */
  @GET
  @AdminEndpoint
  @Path("/queue")
  @Produces("application/json")
  public List<QueueItemDTO> listQueue() {
    return queueService.list();
  }

  /**
   * Triggers the {@link SyncJob}.
   */
  @POST
  @AdminEndpoint
  @Path("/sync")
  @Produces("application/json")
  public void sync() {
    syncJob.execute();
  }

  /**
   * Triggers the {@link PurgeJob}.
   */
  @POST
  @AdminEndpoint
  @Path("/purge")
  @Produces("application/json")
  public void purge() {
    purgeJob.execute();
  }

}
