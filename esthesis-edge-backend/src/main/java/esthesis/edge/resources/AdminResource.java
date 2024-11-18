package esthesis.edge.resources;

import esthesis.edge.config.EdgeProperties;
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
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Administration resource for EDGE. Access to the resources defined here need to use the configured
 * secret as a Bearer Authorization header.
 */
@Slf4j
@Path("/admin")
@RequiredArgsConstructor
@Tag(name = "AdminResource", description = "Administration resources for esthesis EDGE, requiring"
    + " authentication with the configured secret.")
public class AdminResource {

  private final DeviceService deviceService;
  private final QueueService queueService;
  private final SyncJob syncJob;
  private final PurgeJob purgeJob;
  private final EdgeProperties edgeProperties;

  /**
   * Endpoint to check if the admin endpoint is working.
   *
   * @return "OK" if the endpoint is working.
   */
  @GET
  @AdminEndpoint
  @Path("/auth")
  @Produces("text/plain")
  @Operation(
      summary = "Check authentication",
      description = "This endpoint allows you to check if authentication is properly configured.")
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
  @Operation(
      summary = "List all registered devices",
      description = "List all devices registered in the system, "
          + "together with their metadata.")
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
  @Operation(
      summary = "Delete a specific device",
      description = "Delete a specific device by its hardware ID. "
          + "Note that the device is not deleted in esthesis CORE, nor its data is removed from "
          + "the locally-synced InfluxDB.")
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
  @Operation(
      summary = "Delete all registered devices",
      description = "Delete all devices registered in the system. "
          + "Note that the devices are not deleted in esthesis CORE, nor their data is removed from "
          + "the locally-synced InfluxDB.")
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
  @Operation(
      summary = "List all items in the queue",
      description = "List all items in the queue, together with their metadata.")
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
  @Operation(
      summary = "Initiate data synchronization",
      description = "Initiate data synchronization, trying to sync all data from esthesis EDGE"
          + "queue to the underlying InfluxDB and esthesis CORE.")
  public Response sync() {
    syncJob.execute();

    return Response.ok().build();
  }

  /**
   * Triggers the {@link PurgeJob}.
   */
  @POST
  @AdminEndpoint
  @Path("/purge")
  @Produces("application/json")
  @Operation(
      summary = "Initiate data purge",
      description = "Initiate data purge, trying to remove all data in esthesis EDGE queue "
          + "according to the configured retention policy.")
  public Response purge() {
    purgeJob.execute();

    return Response.ok().build();
  }

  @GET
  @AdminEndpoint
  @Path("/config")
  @Produces("application/text")
  @Operation(
      summary = "Show current configuration",
      description = "Show the current configuration of the esthesis EDGE.")
  public String config() {
    return edgeProperties.toString();
  }
}
