package esthesis.edge.modules.enedis.resource;

import esthesis.edge.model.DeviceEntity;
import esthesis.edge.modules.enedis.config.EnedisProperties;
import esthesis.edge.modules.enedis.service.EnedisService;
import esthesis.edge.security.AdminEndpoint;
import esthesis.edge.security.ModuleEndpoint;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Administration resource for Enedis module, requiring authentication with the configured secret.
 */
@Slf4j
@Path("/enedis/admin")
@RequiredArgsConstructor
@Tag(name = "EnedisAdminResource", description = "Administration resource for Enedis module, "
    + "requiring authentication with the configured secret.")
public class EnedisAdminResource {

  private final EnedisService enedisService;
  private final EnedisProperties enedisProperties;

  /**
   * Gets the configuration of the Enedis module.
   *
   * @return the configuration of the Enedis module.
   */
  @GET
  @AdminEndpoint
  @Path("/config")
  @Produces("application/text")
  @ModuleEndpoint(enabledProperty = "esthesis.edge.modules.enedis.enabled")
  @Operation(
      summary = "Show current configuration of Enedis module",
      description = "Show the current configuration of the Enedis module.")
  public String getConfiguration() {
    return enedisProperties.toString();
  }

  /**
   * Triggers the data fetch job, effectively querying Enedis for device data.
   *
   * @return a response indicating the success of the operation.
   */
  @GET
  @AdminEndpoint
  @Path("/fetch")
  @Produces("application/json")
  @ModuleEndpoint(enabledProperty = "esthesis.edge.modules.enedis.enabled")
  @Operation(
      summary = "Fetch data from Enedis",
      description = "Initiates data fetching from Enedis for all devices configured in the module.")
  public Response fetchData() {
    enedisService.fetchData();
    return Response.ok().build();
  }

  /**
   * Gets the list of devices that have encountered errors during the data fetch job. Only devices
   * with at least one of their error counters above threshold are returned.
   *
   * @return the list of devices that have encountered errors during the data fetch job.
   */
  @GET
  @AdminEndpoint
  @Path("/errors")
  @Produces("application/json")
  @ModuleEndpoint(enabledProperty = "esthesis.edge.modules.enedis.enabled")
  @Operation(
      summary = "Show devices with fetch errors",
      description = "Show the devices that have encountered errors during the data fetch job, "
          + "together with their error counters.")
  public List<DeviceEntity> errors() {
    return enedisService.getFetchErrors();
  }

  /**
   * Resets the error counters of a device.
   *
   * @param hardwareId the hardware ID of the device to reset.
   * @return the device entity with the error counters reset.
   */
  @PUT
  @AdminEndpoint
  @Path("/reset-errors")
  @Produces("application/json")
  @ModuleEndpoint(enabledProperty = "esthesis.edge.modules.enedis.enabled")
  @Operation(
      summary = "Reset fetch errors for a device",
      description = "Reset the error counters for a device that has encountered errors during the "
          + "data fetch job.")
  public DeviceEntity resetErrors(@QueryParam("hardwareId") String hardwareId) {
    return enedisService.resetFetchErrors(hardwareId);
  }
}
