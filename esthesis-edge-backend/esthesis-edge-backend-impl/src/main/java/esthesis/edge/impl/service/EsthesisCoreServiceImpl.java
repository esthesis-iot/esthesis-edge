package esthesis.edge.impl.service;

import esthesis.common.agent.dto.AgentRegistrationRequest;
import esthesis.common.agent.dto.AgentRegistrationResponse;
import esthesis.common.exception.QDoesNotExistException;
import esthesis.common.util.EsthesisCommonConstants.Device.Capability;
import esthesis.common.util.EsthesisCommonConstants.Device.Type;
import esthesis.edge.api.service.EsthesisCoreService;
import esthesis.edge.impl.model.DeviceEntity;
import esthesis.service.agent.resource.AgentResource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.operator.OperatorCreationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class EsthesisCoreServiceImpl implements EsthesisCoreService {

  @Inject
  @RestClient
  AgentResource agentResource;

  @Override
  public void registerDevice(String hardwareId, List<String> tags) {
    DeviceEntity device = DeviceEntity.findByHardwareId(hardwareId).orElseThrow(() ->
        new QDoesNotExistException("Device with hardware ID {} does not exist.", hardwareId));

    try {
      AgentRegistrationRequest request = AgentRegistrationRequest.builder()
          .hardwareId(hardwareId)
          .type(Type.EDGE)
          .capability(Capability.PING)
          .tags(tags.isEmpty() ? null : String.join(",", tags))
          .build();
      AgentRegistrationResponse response = agentResource.register(request);
      device.setCertificate(response.getCertificate());
      device.setPublicKey(response.getPublicKey());
      device.setPrivateKey(response.getPrivateKey());
      device.persist();
      log.info("Device with hardware id '{}' registered with esthesis CORE.", hardwareId);
    } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException |
             OperatorCreationException | NoSuchProviderException e) {
      log.error("Failed to register device with hardware id '{}'.", hardwareId, e);
    }
  }
}
