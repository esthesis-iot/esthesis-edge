package esthesis.edge.modules.enedis.resource;

import com.google.common.collect.EvictingQueue;
import esthesis.edge.modules.enedis.service.EnedisService;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
@Path("/enedis/public")
@RequiredArgsConstructor
public class EnedisPublicResource {

  // A list of states being generated while displaying the welcome page.
  private static final EvictingQueue<String> states = EvictingQueue.create(1000);

  @ConfigProperty(name = "esthesis.edge.modules.enedis.self-registration.enabled")
  boolean isSelfRegistrationEnabled;

  @ConfigProperty(name = "esthesis.edge.modules.enedis.self-registration.welcome-url")
  Optional<String> welcomeUrl;
  @ConfigProperty(name = "esthesis.edge.modules.enedis.self-registration.page.registration.title")
  String pageTitle;
  @ConfigProperty(name = "esthesis.edge.modules.enedis.self-registration.page.logoUrl")
  Optional<String> pageLogoUrl;
  @ConfigProperty(name = "esthesis.edge.modules.enedis.self-registration.page.logoAlt")
  Optional<String> pageLogoAlt;
  @ConfigProperty(name = "esthesis.edge.modules.enedis.self-registration.page.registration.message")
  String pageMessage;
  @ConfigProperty(name = "esthesis.edge.modules.enedis.client-id")
  String clientId;
  @ConfigProperty(name = "esthesis.edge.modules.enedis.self-registration.duration")
  String duration;
  @ConfigProperty(name = "esthesis.edge.modules.enedis.self-registration.page.success.message")
  String pageSuccessMessage;
  @ConfigProperty(name = "esthesis.edge.modules.enedis.self-registration.page.success.title")
  String pageSuccessTitle;
  @ConfigProperty(name = "esthesis.edge.modules.enedis.self-registration.page.error.message")
  String pageErrorMessage;
  @ConfigProperty(name = "esthesis.edge.modules.enedis.self-registration.page.error.title")
  String pageErrorTitle;
  @ConfigProperty(name = "esthesis.edge.modules.enedis.self-registration.state-checking")
  boolean stateChecking;

  @Inject
  @Location("enedis/welcome.html")
  Template welcomePage;
  @Inject
  @Location("enedis/registration-successful.html")
  Template successPage;
  @Inject
  @Location("enedis/error.html")
  Template errorPage;

  @Inject
  EnedisService enedisService;
  /**
   * Check if the given state is known (i.e. has been created before when rendering the welcome page).
   * @param state The state to check.
   * @return True if the state is known, false otherwise.
   */
  public boolean isKnownState(String state) {
    return states.contains(state) || !stateChecking;
  }

  @GET
  @Path("welcome")
  @Produces(MediaType.TEXT_HTML)
  public Response welcome() {
    if (isSelfRegistrationEnabled) {
      if (welcomeUrl.isPresent() && StringUtils.isNotBlank(welcomeUrl.get())) {
        return Response.status(Status.FOUND).header("Location", welcomeUrl).build();
      } else {
        String state = UUID.randomUUID().toString();
        states.add(state);
        log.debug("State '{}' generated.", state);
        return Response.ok(
            welcomePage
              .data("title", pageTitle)
              .data("logo", pageLogoUrl)
              .data("logoAlt", pageLogoAlt)
              .data("state", state)
              .data("clientId", clientId)
              .data("message", pageMessage)
              .data("duration", duration)
              .render()
        ).build();
      }
    } else {
      return Response.status(Status.FORBIDDEN).entity("Self-registration is disabled").build();
    }
  }

  @GET
  @Path("redirect-handler")
  @Produces(MediaType.TEXT_HTML)
  public Response redirectHandler(@QueryParam("State") String state,
      @QueryParam("usage_point_id") String usagePointId, @QueryParam("code") String code) {
    // Check the state received is one we have previously created, if not return an error.
    if (com.cronutils.utils.StringUtils.isEmpty(state) || !isKnownState(state)) {
      log.error("Received invalid state '{}'.", state);
      return Response.status(Response.Status.BAD_REQUEST).entity("Invalid state.").build();
    }

    try {
      enedisService.createDevice(usagePointId);
      return Response.ok(
          successPage
              .data("logo", pageLogoUrl)
              .data("logoAlt", pageLogoAlt)
              .data("title", pageSuccessTitle)
              .data("message", pageSuccessMessage)
              .render()
      ).build();
    } catch (Exception ex) {
      log.error("Error while creating device(s).", ex);
      return Response.ok(
          errorPage
              .data("logo", pageLogoUrl)
              .data("logoAlt", pageLogoAlt)
              .data("title", pageErrorTitle)
              .data("message", pageErrorMessage)
              .render()
      ).build();
    }
  }
}
