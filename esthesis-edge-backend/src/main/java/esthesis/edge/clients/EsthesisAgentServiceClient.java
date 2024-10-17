package esthesis.edge.clients;

import esthesis.common.agent.dto.AgentRegistrationRequest;
import esthesis.common.agent.dto.AgentRegistrationResponse;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * A REST client to communicate with the agent functionality of esthesis CORE.
 */
@RegisterRestClient(configKey = "EsthesisAgentServiceClient")
public interface EsthesisAgentServiceClient {

  /**
   * Register a device with esthesis CORE.
   *
   * @param agentRegistrationRequest the agent registration request
   * @return the agent registration response
   */
  @POST
  @Path(value = "/api/v1/register")
  AgentRegistrationResponse register(@Valid AgentRegistrationRequest agentRegistrationRequest);
}
