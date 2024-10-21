package esthesis.edge.clients;

import esthesis.edge.crypto.EdgeCryptoUtil;
import esthesis.edge.crypto.dto.SSLSocketFactoryCertificateDTO;
import esthesis.edge.crypto.dto.SSLSocketFactoryDTO;
import esthesis.edge.crypto.dto.SSLSocketFactoryPrivateKeyDTO;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import javax.net.ssl.SSLSocketFactory;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Helper class to publish MQTT messages to esthesis CORE.
 */
@Slf4j
public class MqttPublisher {

  // The MQTT client.
  private final MqttClient client;

  /**
   * Create a new instance of the MQTT publisher.
   *
   * @param url The MQTT broker URL.
   * @throws MqttException If the instance could not be created.
   */
  public MqttPublisher(String url) throws MqttException {
    this.client = new MqttClient(url, MqttClient.generateClientId(), new MemoryPersistence());
  }

  /**
   * Connect to the MQTT broker.
   *
   * @throws MqttException If the connection could not be established.
   */
  public void connect() throws MqttException {
    client.connect();
  }

  /**
   * Connect to the MQTT broker with SSL.
   *
   * @param coreCertAsPem   The CORE certificate as PEM.
   * @param clientCertAsPem The client certificate as PEM.
   * @param clientKeyAsPem  The client private key as PEM.
   * @param keyAlgorithm    The private key algorithm.*
   */
  public void connect(String coreCertAsPem, String clientCertAsPem, String clientKeyAsPem,
      String keyAlgorithm)
  throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException,
         UnrecoverableKeyException, KeyManagementException, MqttException, InvalidKeySpecException {
    // Create the SSL socket factory.
    SSLSocketFactoryDTO sslSocketFactoryDTO = new SSLSocketFactoryDTO();

    // Add CORE certificate to trusted certificates.
    sslSocketFactoryDTO.setTrustedCertificates(
        List.of(SSLSocketFactoryCertificateDTO.builder()
            .name(client.getServerURI())
            .pemCertificate(coreCertAsPem)
            .build()));

    // Add client certificate and private key.
    sslSocketFactoryDTO.setClientCertificate(SSLSocketFactoryCertificateDTO.builder()
        .name("edge-cert")
        .pemCertificate(clientCertAsPem)
        .build());
    sslSocketFactoryDTO.setClientPrivateKey(SSLSocketFactoryPrivateKeyDTO.builder()
        .name("edge-private-key")
        .pemPrivateKey(clientKeyAsPem)
        .algorithm(keyAlgorithm)
        .build());

    SSLSocketFactory sslSocketFactory = EdgeCryptoUtil.createSSLSocketFactory(sslSocketFactoryDTO);

    // Connect to the broker with SSL.
    MqttConnectOptions options = new MqttConnectOptions();
    options.setSocketFactory(sslSocketFactory);
    client.connect(options);
  }

  /**
   * Publish a message to a topic.
   *
   * @param topic   The topic to publish to.
   * @param message The message to publish.
   * @throws MqttException If the message could not be published.
   */
  public void publish(String topic, String message) throws MqttException {
    log.debug("Publishing message to topic '{}': '{}'.", topic, message);
    client.publish(topic, message.getBytes(), 0, false);
  }

  /**
   * Disconnect from the MQTT broker.
   *
   * @throws MqttException If the disconnection could not be established.
   */
  public void disconnect() throws MqttException {
    client.disconnect();
  }

  /**
   * Checks if the MQTT client is connected.
   *
   * @return True if the client is connected, false otherwise.
   */
  public boolean isConnected() {
    return client.isConnected();
  }
}
