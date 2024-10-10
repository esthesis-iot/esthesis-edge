package esthesis.edge.impl.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttPublisher {

  private MqttClient client;

  public MqttPublisher(String url) throws MqttException {
//    this.client = new MqttClient("ssl://your-mqtt-server:8883", MqttClient.generateClientId(), new MemoryPersistence());
    this.client = new MqttClient(url, MqttClient.generateClientId(), new MemoryPersistence());
  }

  public void connect() throws MqttException {
    client.connect();
  }

  public void connect(String cert, String key, String keyPassword)
  throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException,
         UnrecoverableKeyException, KeyManagementException, MqttException {
    // Load the certificate dynamically
    X509Certificate certificate = loadCertificate(new ByteArrayInputStream(cert.getBytes(
        StandardCharsets.UTF_8)));
    KeyStore keyStore = createKeyStore(certificate,
        new ByteArrayInputStream(key.getBytes(StandardCharsets.UTF_8)), keyPassword);

    // Create SSL context with the dynamically loaded certificate
    SSLContext sslContext = createSSLContext(keyStore, keyPassword);
    MqttConnectOptions options = new MqttConnectOptions();
    options.setSocketFactory(sslContext.getSocketFactory());

    // Connect to the broker with SSL
    client.connect(options);
  }

  private X509Certificate loadCertificate(InputStream certInputStream) throws CertificateException {
    CertificateFactory cf = CertificateFactory.getInstance("X.509");

    return (X509Certificate) cf.generateCertificate(certInputStream);
  }

  private KeyStore createKeyStore(X509Certificate cert, InputStream keyInputStream,
      String keyPassword)
  throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
    keyStore.load(null, keyPassword.toCharArray());
    keyStore.setCertificateEntry("cert", cert);

    return keyStore;
  }

  private SSLContext createSSLContext(KeyStore keyStore, String keyPassword)
  throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException,
         KeyManagementException {
    TrustManagerFactory tmf = TrustManagerFactory.getInstance(
        TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(keyStore);

    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    kmf.init(keyStore, keyPassword.toCharArray());

    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

    return sslContext;
  }

  public void publish(String topic, String message) throws MqttException {
    client.publish(topic, message.getBytes(), 0, false);
  }

  public void disconnect() throws MqttException {
    client.disconnect();
  }
}
