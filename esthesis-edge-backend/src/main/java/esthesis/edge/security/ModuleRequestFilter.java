package esthesis.edge.security;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.Provider;
import java.lang.reflect.Method;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.Config;

@Slf4j
@Provider
@ModuleEndpoint
public class ModuleRequestFilter implements ContainerRequestFilter {

  @Context
  private ResourceInfo info;

  @Inject
  Config configProvider;

  /**
   * Get the ModuleEndpoint annotation from the resource method.
   *
   * @param resourceMethod The resource method to scan for the annotation.
   * @return An Optional containing the ModuleEndpoint annotation if it exists, otherwise an empty Optional.
   */
  private Optional<ModuleEndpoint> getModuleEndpointAnnotation(Method resourceMethod) {
    String methodName = resourceMethod.getName();
    Class<?>[] paramTypes = resourceMethod.getParameterTypes();
    Class<?> declaringClass = resourceMethod.getDeclaringClass();
    Method declaredMethod;
    try {
      declaredMethod = declaringClass.getDeclaredMethod(methodName, paramTypes);
    } catch (NoSuchMethodException e) {
      return Optional.empty();
    }

    return Optional.ofNullable(declaredMethod.getAnnotation(ModuleEndpoint.class));
  }

  @Override
  public void filter(ContainerRequestContext requestContext) {
    getModuleEndpointAnnotation(info.getResourceMethod()).ifPresent(moduleEndpoint -> {
      String enabledProperty = moduleEndpoint.enabledProperty();
      configProvider.getOptionalValue(enabledProperty, Boolean.class).ifPresent(enabled -> {
        if (Boolean.FALSE.equals(enabled)) {
          log.debug("Module is not enabled via @ModuleEndpoint annotation.");
          requestContext.abortWith(Response.status(Status.NOT_FOUND).build());
        }
      });
    });
  }
}
