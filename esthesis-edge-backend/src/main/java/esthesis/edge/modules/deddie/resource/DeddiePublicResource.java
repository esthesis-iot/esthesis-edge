package esthesis.edge.modules.deddie.resource;

import esthesis.edge.modules.deddie.config.DeddieProperties;
import esthesis.edge.modules.deddie.service.DeddieService;
import esthesis.edge.security.ModuleEndpoint;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Slf4j
@Path("/deddie/public")
@Tag(name = "DeddiePublicResource", description = "Public API endpoints for the Deddie module. "
        + "This resource is used to handle the self-registration of Deddie devices.")
@RequiredArgsConstructor
public class DeddiePublicResource {

    private final DeddieProperties deddieProperties;
    private final DeddieService deddieService;

    /**
     * Handles the self-registration of Deddie devices. This endpoint will present the users with a
     * form to input their Deddie'S credentials necessary for using the Deddie API.
     *
     * @return a response containing the self-registration page.
     */
    @GET
    @Path("self-registration")
    @Produces(MediaType.TEXT_HTML)
    @ModuleEndpoint(enabledProperty = "esthesis.edge.modules.deddie.enabled")
    public Response selfRegistration() {
        if (deddieProperties.selfRegistration().enabled()) {
            // Check if the maximum number of devices has been reached.
            if (deddieService.countDevices() >= deddieProperties.maxDevices()) {
                return Response.status(Response.Status.TOO_MANY_REQUESTS)
                        .entity("Maximum number of devices reached.")
                        .build();
            }

            return Response.ok(deddieService.getSelfRegistrationPage()).build();

        } else {
            return Response.status(Response.Status.FORBIDDEN).entity("Self-registration is disabled.").build();
        }
    }

    @POST
    @Path("redirect-handler")
    @Produces(MediaType.TEXT_HTML)
    @ModuleEndpoint(enabledProperty = "esthesis.edge.modules.deddie.enabled")
    public Response redirectHandler(@FormParam("taxNumber") String taxNumber,
                                    @FormParam("accessToken") String accessToken,
                                    @FormParam("supplyNumbers") List<String> supplyNumbers) {
        if (deddieProperties.selfRegistration().enabled()) {
            // Check if the maximum number of devices has been reached.
            if (deddieService.countDevices() >= deddieProperties.maxDevices()) {
                return Response.ok(deddieService.getRegistrationErrorPage("Maximum number of devices reached.")).build();
            }

            try {
                deddieService.createDevices(accessToken, taxNumber, supplyNumbers);
                return Response.ok(deddieService.getRegistrationSuccessfulPage()).build();

            }catch (Exception e){
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(deddieService.getRegistrationErrorPage(
                        "We were unable to complete your registration. Please verify the information provided and try again."))
                        .build();
            }

        } else {
            return Response.status(Response.Status.FORBIDDEN).entity(deddieService.getRegistrationErrorPage("Self-registration is disabled.")).build();
        }
    }
}
