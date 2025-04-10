package esthesis.edge.services;

import esthesis.common.agent.dto.AgentRegistrationRequest;
import esthesis.common.agent.dto.AgentRegistrationRequest.AgentRegistrationRequestBuilder;
import esthesis.common.agent.dto.AgentRegistrationResponse;
import esthesis.common.exception.QDoesNotExistException;
import esthesis.common.util.EsthesisCommonConstants.Device.Capability;
import esthesis.common.util.EsthesisCommonConstants.Device.Type;
import esthesis.edge.clients.EsthesisAgentServiceClient;
import esthesis.edge.config.EdgeProperties;
import esthesis.edge.model.DeviceEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Service class for interacting with esthesis CORE.
 */
@Slf4j
@Transactional
@ApplicationScoped
@RequiredArgsConstructor
public class EsthesisCoreService {

  @Inject
  @RestClient
  EsthesisAgentServiceClient esthesisAgentServiceClient;
  private final EdgeProperties edgeProperties;

  /**
   * Registers a device with esthesis CORE.
   *
   * @param hardwareId The hardware ID of the device to register.
   */
  public void registerDevice(String hardwareId) {
    DeviceEntity device = DeviceEntity.findByHardwareId(hardwareId).orElseThrow(() ->
        new QDoesNotExistException("Device with hardware ID '{}' does not exist.", hardwareId));

    try {
      // Create registration request.
      AgentRegistrationRequestBuilder requestBuilder = AgentRegistrationRequest.builder()
          .hardwareId(hardwareId)
          .type(Type.EDGE)
          .capability(Capability.PING)
          .capability(Capability.TELEMETRY)
          .capability(Capability.METADATA);
      if (StringUtils.isNotBlank(device.getTags())) {
        requestBuilder.tags(device.getTags());
      }
      if (StringUtils.isNotBlank(device.getAttributes())) {
        requestBuilder.attributes(device.getAttributes());
      }

      // Add registration secret, if present.
      Optional<String> secret = edgeProperties.core().registration().secret();
      if (secret.isPresent() && StringUtils.isNotBlank(secret.get())) {
        requestBuilder.registrationSecret(secret.get());
      }

      // Register device with esthesis CORE.
      AgentRegistrationResponse response = esthesisAgentServiceClient.register(
          requestBuilder.build());
      device.setCertificate(response.getCertificate());
      device.setPublicKey(response.getPublicKey());
      device.setPrivateKey(response.getPrivateKey());
      device.setCoreRegisteredAt(Instant.now());
      device.persist();
      log.info("Device with hardware ID '{}' registered with esthesis CORE.", hardwareId);
    } catch (Exception e) {
      log.error("Failed to register device with hardware ID '{}'.", hardwareId, e);
    }
  }
}
