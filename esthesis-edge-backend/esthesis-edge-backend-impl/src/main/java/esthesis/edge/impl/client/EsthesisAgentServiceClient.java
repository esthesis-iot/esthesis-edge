package esthesis.edge.impl.client;

import esthesis.common.agent.dto.AgentRegistrationRequest;
import esthesis.common.agent.dto.AgentRegistrationResponse;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "EsthesisAgentServiceClient")
public interface EsthesisAgentServiceClient {
  @POST
  @Path(value = "/api/v1/register")
  AgentRegistrationResponse register(@Valid AgentRegistrationRequest agentRegistrationRequest);
}
