docker run --rm \
  --name hivemq \
  -p 9883:8883 \
  -v $(pwd)/server-keystore.jks:/opt/hivemq/certs/server-keystore.jks \
  -v $(pwd)/server-truststore.jks:/opt/hivemq/certs/server-truststore.jks \
  -v $(pwd)/config.xml:/opt/hivemq/conf/config.xml \
  hivemq/hivemq-ce:2024.7
