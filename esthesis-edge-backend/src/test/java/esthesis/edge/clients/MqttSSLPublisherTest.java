package esthesis.edge.clients;

import static io.smallrye.common.constraint.Assert.assertFalse;
import static io.smallrye.common.constraint.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import esthesis.edge.testcontainers.HiveMQSSLTC;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(value = HiveMQSSLTC.class, restrictToAnnotatedClass = true)
class MqttSSLPublisherTest {

  @ConfigProperty(name = "test.mqtt.port")
  Optional<Integer> mqttPort;

  @Test
  void connectDisconnectTest()
  throws MqttException, IOException, UnrecoverableKeyException, CertificateException,
         KeyStoreException, NoSuchAlgorithmException, InvalidKeySpecException,
         KeyManagementException {
    MqttPublisher mqttPublisher = new MqttPublisher("ssl://localhost:" + mqttPort.orElseThrow());
    String serverCert = HiveMQSSLTC.loadResourceFileAsText("mqtt/server.crt");
    String clientCert = HiveMQSSLTC.loadResourceFileAsText("mqtt/client.crt");
    String clientKey = HiveMQSSLTC.loadResourceFileAsText("mqtt/client.key");
    mqttPublisher.connect(serverCert, clientCert, clientKey, "RSA");
    assertTrue(mqttPublisher.isConnected());
    mqttPublisher.disconnect();
    assertFalse(mqttPublisher.isConnected());
  }

  @Test
  void publishTest()
  throws MqttException, IOException, UnrecoverableKeyException, CertificateException,
         KeyStoreException, NoSuchAlgorithmException, InvalidKeySpecException,
         KeyManagementException {
    MqttPublisher mqttPublisher = new MqttPublisher("ssl://localhost:" + mqttPort.orElseThrow());
    String serverCert = HiveMQSSLTC.loadResourceFileAsText("mqtt/server.crt");
    String clientCert = HiveMQSSLTC.loadResourceFileAsText("mqtt/client.crt");
    String clientKey = HiveMQSSLTC.loadResourceFileAsText("mqtt/client.key");
    mqttPublisher.connect(serverCert, clientCert, clientKey, "RSA");
    assertDoesNotThrow(() -> mqttPublisher.publish("test", "test"));
    mqttPublisher.disconnect();
  }

}
