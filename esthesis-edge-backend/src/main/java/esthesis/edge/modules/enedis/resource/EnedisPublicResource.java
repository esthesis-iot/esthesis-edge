package esthesis.edge.modules.enedis.resource;

import com.google.common.collect.EvictingQueue;
import esthesis.edge.modules.enedis.config.EnedisProperties;
import esthesis.edge.modules.enedis.service.EnedisService;
import esthesis.edge.security.ModuleEndpoint;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@Path("/enedis/public")
@RequiredArgsConstructor
public class EnedisPublicResource {

  // The list of random states being generated while displaying the welcome page.
  private static final EvictingQueue<String> states = EvictingQueue.create(1000);
  private final EnedisProperties cfg;
  private final EnedisService enedisService;

  /**
   * Check if the given state is known (i.e. has been created before when rendering the welcome
   * page).
   *
   * @param state The state to check.
   * @return True if the state is known, false otherwise.
   */
  private boolean isKnownState(String state) {
    return states.contains(state) || !cfg.selfRegistration().stateChecking();
  }

  @GET
  @Path("self-registration")
  @Produces(MediaType.TEXT_HTML)
  @ModuleEndpoint(enabledProperty = "esthesis.edge.modules.enedis.enabled")
  public Response selfRegistration() {
    if (cfg.selfRegistration().enabled()) {
      // Check if the maximum allowed number of devices has been reached.
      if (enedisService.countDevices() >= cfg.maxDevices()) {
        return Response.status(Status.TOO_MANY_REQUESTS)
            .entity("No more Enedis devices allowed.").build();
      }

      if (StringUtils.isNotBlank(cfg.selfRegistration().welcomeUrl().orElse(""))) {
        return Response.status(Status.FOUND).header("Location", cfg.selfRegistration().welcomeUrl())
            .build();
      } else {
        String state = UUID.randomUUID().toString();
        states.add(state);
        log.debug("State '{}' generated.", state);
        return Response.ok(enedisService.getSelfRegistrationPage(state)).build();
      }
    } else {
      return Response.status(Status.FORBIDDEN).entity("Self-registration is disabled").build();
    }
  }

  @GET
  @Path("redirect-handler")
  @Produces(MediaType.TEXT_HTML)
  @ModuleEndpoint(enabledProperty = "esthesis.edge.modules.enedis.enabled")
  public Response redirectHandler(@QueryParam("State") String state,
      @QueryParam("usage_point_id") String usagePointId, @QueryParam("code") String code) {
    // Check the state received is one we have previously created, if not return an error.
    if (StringUtils.isEmpty(state) || !isKnownState(state)) {
      log.error("Received invalid state '{}'.", state);
      return Response.status(Response.Status.BAD_REQUEST).entity("Invalid state.").build();
    }

    // Check if the maximum allowed number of devices has been reached.
    if (enedisService.countDevices() >= cfg.maxDevices()) {
      return Response.status(Status.TOO_MANY_REQUESTS)
          .entity("No more Enedis devices allowed.").build();
    }

    // Create the device.
    try {
      enedisService.createDevice(usagePointId);
      return Response.ok(enedisService.getRegistrationSuccessfulPage()).build();
    } catch (Exception ex) {
      log.error("Error while creating device(s).", ex);
      return Response.ok(enedisService.getRegistrationSuccessfulPage()).build();
    }
  }
}
