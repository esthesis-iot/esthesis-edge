package esthesis.edge.clients;

import static io.smallrye.common.constraint.Assert.assertFalse;
import static io.smallrye.common.constraint.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import esthesis.edge.testcontainers.HiveMQTC;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(value = HiveMQTC.class, restrictToAnnotatedClass = true)
class MqttPublisherTest {

  @ConfigProperty(name = "test.mqtt.port")
  Optional<Integer> mqttPort;

  @Test
  void connectDisconnectTest() throws MqttException {
    MqttPublisher mqttPublisher = new MqttPublisher("tcp://localhost:" + mqttPort.orElseThrow());
    mqttPublisher.connect();
    assertTrue(mqttPublisher.isConnected());
    mqttPublisher.disconnect();
    assertFalse(mqttPublisher.isConnected());
  }

  @Test
  void publishTest() throws MqttException {
    MqttPublisher mqttPublisher = new MqttPublisher("tcp://localhost:" + mqttPort.orElseThrow());
    mqttPublisher.connect();
    assertDoesNotThrow(() -> mqttPublisher.publish("test", "test"));
    mqttPublisher.disconnect();
  }

}
