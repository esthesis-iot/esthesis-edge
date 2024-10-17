package esthesis.edge.security;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.Provider;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * A filter that checks if the request is an admin request. Admin endpoints are identified by the
 * presence of the {@link AdminEndpoint} annotation, and need to include a Bearer token in the
 * authorization header. The value of the token is defined in application.yaml as
 * esthesis.edge.admin-secret.
 */
@Provider
@AdminEndpoint
public class AdminRequestFilter implements ContainerRequestFilter {

  // The name of the authorisation header that contains the admin secret.
  public static final String ADMIN_SECRET_HEADER_NAME = "X-ESTHESIS-EDGE-ADMIN-SECRET";

  // The admin secret.
  @ConfigProperty(name = "esthesis.edge.admin-secret")
  Optional<String> adminSecret;

  /**
   * Filters the request context to check if the request is an admin request.
   *
   * @param requestContext request context.
   */
  @Override
  public void filter(ContainerRequestContext requestContext) {
    if (adminSecret.isEmpty()
        || requestContext.getHeaderString(ADMIN_SECRET_HEADER_NAME) == null
        || !requestContext.getHeaderString(ADMIN_SECRET_HEADER_NAME).equals(adminSecret.get())) {
      requestContext.abortWith(Response.status(Status.UNAUTHORIZED).build());
    }
  }
}
