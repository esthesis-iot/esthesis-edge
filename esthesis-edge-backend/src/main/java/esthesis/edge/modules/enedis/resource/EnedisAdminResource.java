package esthesis.edge.modules.enedis.resource;

import esthesis.edge.model.DeviceEntity;
import esthesis.edge.modules.enedis.dto.EnedisConfigDTO;
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

/**
 * Administration resource for Enedis module. Access to the resources defined here need to use the
 * configured secret as a Bearer authorization header.
 */
@Slf4j
@Path("/enedis/admin")
@RequiredArgsConstructor
public class EnedisAdminResource {

  private final EnedisService enedisService;

  /**
   * Gets the configuration of the Enedis module.
   *
   * @return the configuration of the Enedis module.
   */
  @GET
  @AdminEndpoint
  @Path("/config")
  @Produces("application/json")
  @ModuleEndpoint(enabledProperty = "esthesis.edge.modules.enedis.enabled")
  public EnedisConfigDTO getConfiguration() {
    return enedisService.getConfig();
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
  public DeviceEntity resetErrors(@QueryParam("hardwareId") String hardwareId) {
    return enedisService.resetFetchErrors(hardwareId);
  }
}
