# Main configuration

The main configuration options of esthesis EDGE consist of the following parameters:

## Global configuration

| **NAME**                      | **DESCRIPTION**                                                                       |
|-------------------------------|---------------------------------------------------------------------------------------|
| --set global.imagePullSecrets | The name of the secret to use when pulling the esthesis EDGE image from the registry. |
| --set global.storageClass     | The storage class to use when creating persistent volumes.                            |
| --set global.esthesisRegistry | The URL of the esthesis registry to use when pulling images.                          |
| --set global.timezone         | The timezone to use when running the esthesis EDGE service.                           |

## General configuration

| **NAME**                                                                                                      | **DESCRIPTION**                                                                                                                                                        |
|---------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **QUARKUS_DATASOURCE_USERNAME**<br/>--set quarkus.datasource.username                                         | The username to connect with to the MariaDB database.<br/>Default: esthesis-edge                                                                                       |
| **QUARKUS_DATASOURCE_PASSWORD**<br/>--set quarkus.datasource.password                                         | The password to connect with to the MariaDB database.<br/>Default: esthesis-edge                                                                                       |
| **QUARKUS_DATASOURCE_JDBC_URL**<br/>--set quarkus.datasource.jdbc.url                                         | The JDBC URL of the MariaDB database to use.<br/>Default: jdbc:mariadb://localhost:4306/esthesis-edge                                                                  |
| **QUARKUS_REST_CLIENT_ESTHESIS_AGENT_SERVICE_CLIENT**<br/>--set quarkus.restClient.esthesisAgentServiceClient | The URL of the esthesis Agent Service.<br/>Default: http://localhost:59070                                                                                             |
| **ESTHESIS_EDGE_ADMIN_SECRET**<br/>--set esthesis.edge.admin.secret                                           | A secret token to use when calling the admin API.<br/>Default: ca820829-328f-41e0-9207-ef42372f94c3                                                                    |
| **ESTHESIS_EDGE_SYNC_CRON**<br/>--set esthesis.edge.sync.cron                                                 | A Quartz-type cron expression, specifying the frequency in which esthesis EDGE tries to syncronise queued data to InfluxDB and esthesis CORE.<br/>Default: 0 0 * * * ? |
| **ESTHESIS_EDGE_PURGE_CRON**<br/>--set esthesis.edge.purge.cron                                               | A Quartz-type cron expression, specifying the frequency in which data purge is initiated.<br/>Default: 0 0 0 * * ?                                                     |
| **ESTHESIS_EDGE_PURGE_SUCCESSFUL_MINUTES**<br/>--set esthesis.edge.purge.successfulMinutes                    | The number of minutes after which successfully synced data is purged.<br/>Default: 60                                                                                  |                                                              
| **ESTHESIS_EDGE_PURGE_QUEUED_MINUTES**<br/>--set esthesis.edge.queuedMinutes                                  | The number of minutes after which queued data is purged.<br/>Default: 10080 (1 week)                                                                                   |                                                                                                                              

## Service and Ingress configuration

| **NAME**                                             | **DESCRIPTION**                                                                                |
|------------------------------------------------------|------------------------------------------------------------------------------------------------|
| --set esthesis.edge.service.port                     | The port on which the esthesis EDGE service is exposed.<br/>Default: 80                        |
| --set esthesis.edge.service.type                     | The type of the service to use when exposing the esthesis EDGE service.<br/>Default: ClusterIP |
| --set esthesis.edge.ingress.enabled                  | Whether to create an Ingress resource for the esthesis EDGE service or not.<br/>Default: false |
| --set esthesis.edge.ingress.certManagerClusterIssuer | The name of the ClusterIssuer to use when creating the Ingress resource.                       |
| --set esthesis.edge.ingress.certManagerIssuer        | The name of the Issuer to use when creating the Ingress resource.                              |
| --set esthesis.edge.ingress.className                | The class name of the Ingress resource to use when creating the Ingress resource.              |
| --set esthesis.edge.ingress.hostname                 | The host name to use when creating the Ingress resource.                                       |

## InfluxDB configuration (local data sync)

| **NAME**                                                                               | **DESCRIPTION**                                                                          |
|----------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------|
| **ESTHESIS_EDGE_LOCAL_ENABLED**<br/>--set esthesis.edge.local.enabled                  | Whether syncronising data to the InfluxDB database is enabled or not.<br/>Default: false |
| **ESTHESIS_EDGE_LOCAL_INFLUX_DB_URL**<br/>--set esthesis.edge.local.influxDb.url       | The URL of the InfluxDB database to use.<br/>Default: http://localhost:9086              |
| **ESTHESIS_EDGE_LOCAL_INFLUX_DB_TOKEN**<br/>--set esthesis.edge.local.influxDb.token   | The token to use when connecting to the InfluxDB database.<br/>Default: esthesis-edge    |
| **ESTHESIS_EDGE_LOCAL_INFLUX_DB_BUCKET**<br/>--set esthesis.edge.local.influxDb.bucket | The bucket to use when connecting to the InfluxDB database.<br/>Default: edge            |
| **ESTHESIS_EDGE_LOCAL_INFLUX_DB_ORG**<br/>--set esthesis.edge.local.influxDb.org       | The organisation to use when connecting to the InfluxDB database.<br/>Default: esthesis  |

## esthesis CORE configuration (core data sync)

| **NAME**                                                                                      | **DESCRIPTION**                                                                                         |
|-----------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------|
| **ESTHESIS_EDGE_CORE_PUSH_ENABLED**<br/>--set esthesis.edge.core.push.enabled                 | Whether syncronising data to the esthesis CORE is enabled or not.<br/>Default: false                    |
| **ESTHESIS_EDGE_CORE_PUSH_URL**<br/>--set esthesis.edge.core.push.url                         | The URL of the MQTT server used by esthesis CORE to use.<br/>Default: ssl://mosquitto.esthesis:8883     |
| **ESTHESIS_EDGE_CORE_PUSH_TOPIC_TELEMETRY**<br/>--set esthesis.edge.core.push.topicTelemetry  | The topic to use when pushing telemetry data to esthesis CORE.<br/>Default: esthesis/telemetry          |
| **ESTHESIS_EDGE_CORE_PUSH_TOPIC_PING**<br/>--set esthesis.edge.core.push.topicPing            | The topic to use when pushing ping data to esthesis CORE.<br/>Default: esthesis/ping                    |
| **ESTHESIS_EDGE_CORE_REGISTRATION_ENABLED**<br/>--set esthesis.edge.core.registration.enabled | Whether new esthesis EDGE devices are registered as devices in esthesis CORE or not.<br/>Default: false |
| **ESTHESIS_EDGE_CORE_REGISTRATION_SECRET**<br/>--set esthesis.edge.core.registration.secret   | The secret token to use when registering new devices in esthesis CORE.                                  |
| **ESTHESIS_EDGE_CORE_TAGS**<br/>--set esthesis.edge.core.registration.tags                    | The tags used to identify the device in esthesis CORE.<br/>Default: edge                                |
| **ESTHESIS_EDGE_CORE_KEY_ALGORITHM**<br/>--set esthesis.edge.core.keyAlgorithm                | The algorithm used to generate the device keys by esthesis CORE.<br/>Default: RSA                       |
