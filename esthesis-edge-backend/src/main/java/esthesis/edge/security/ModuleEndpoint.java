package esthesis.edge.security;

import jakarta.ws.rs.NameBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to be used on JAX-RS resources to specify that the resource is a module endpoint. This
 * facilitates disabling the module by setting a configuration property.
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ModuleEndpoint {

  // The name of the configuration property specifying if the module is active or not.
  String enabledProperty() default "";
}
