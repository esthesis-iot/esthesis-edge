package esthesis.edge.api.security;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Provider
@AdminEndpoint
public class AdminRequestFilter implements ContainerRequestFilter {

  private static final String ADMIN_SECRET_HEADER_NAME = "X-ESTHESIS-EDGE-ADMIN-SECRET";
  @ConfigProperty(name = "esthesis.edge.admin-secret")
  Optional<String> adminSecret;

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    if (adminSecret.isEmpty()
        || requestContext.getHeaderString(ADMIN_SECRET_HEADER_NAME) == null
        || !requestContext.getHeaderString(ADMIN_SECRET_HEADER_NAME).equals(adminSecret.get())) {
      requestContext.abortWith(Response.status(Status.UNAUTHORIZED).build());
    }
  }
}
