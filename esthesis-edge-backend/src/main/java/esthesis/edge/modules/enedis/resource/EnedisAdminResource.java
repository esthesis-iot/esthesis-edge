package esthesis.edge.modules.enedis.resource;

import esthesis.edge.modules.enedis.dto.EnedisConfigDTO;
import esthesis.edge.modules.enedis.service.EnedisService;
import esthesis.edge.security.AdminEndpoint;
import esthesis.edge.security.ModuleEndpoint;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/enedis/admin")
@RequiredArgsConstructor
public class EnedisAdminResource {

  private final EnedisService enedisService;

  @GET
  @AdminEndpoint
  @Path("/config")
  @ModuleEndpoint(enabledProperty = "esthesis.edge.modules.enedis.enabled")
  @Produces("application/json")
  public EnedisConfigDTO getConfiguration() {
    return enedisService.getConfig();
  }
}
