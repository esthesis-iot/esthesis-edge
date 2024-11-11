# Main configuration

The main configuration options of esthesis EDGE consist of the following parameters:

## General configuration

| **NAME**                                              | **DESCRIPTION**                                                                                                                                                        |
|-------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **QUARKUS_DATASOURCE_USERNAME**                       | The username to connect with to the MariaDB database.<br/>Default: esthesis-edge                                                                                       |
| **QUARKUS_DATASOURCE_PASSWORD**                       | The password to connect with to the MariaDB database.<br/>Default: esthesis-edge                                                                                       |
| **QUARKUS_DATASOURCE_JDBC_URL**                       | The JDBC URL of the MariaDB database to use.<br/>Default: jdbc:mariadb://localhost:4306/esthesis-edge                                                                  |
| **QUARKUS_REST_CLIENT_ESTHESIS_AGENT_SERVICE_CLIENT** | The URL of the esthesis Agent Service.<br/>Default: http://localhost:59070                                                                                             | 
| **QUARKUS_REST_CLIENT_ENEDIS_CLIENT**                 | The URL of the Enedis API.<br/>Default: https://ext.prod-sandbox.api.enedis.fr                                                                                         |
| **ESTHESIS_EDGE_ADMIN_SECRET**                        | A secret token to use when calling the admin API.<br/>Default: ca820829-328f-41e0-9207-ef42372f94c3                                                                    |
| **ESTHESIS_EDGE_SYNC_CRON**                           | A Quartz-type cron expression, specifying the frequency in which esthesis EDGE tries to syncronise queued data to InfluxDB and esthesis CORE.<br/>Default: 0 0 * * * ? |
| **ESTHESIS_EDGE_PURGE_CRON**                          | A Quartz-type cron expression, specifying the frequency in which data purge is initiated.<br/>Default: 0 0 0 * * ?                                                     |
| **ESTHESIS_EDGE_PURGE_SUCCESSFUL_MINUTES**            | The number of minutes after which successfully synced data is purged.<br/>Default: 60                                                                                  |                                                              
| **ESTHESIS_EDGE_PURGE_QUEUED_MINUTES**                | The number of minutes after which queued data is purged.<br/>Default: 10080 (1 week)                                                                                   |                                                                                                                              

## InfluxDB configuration (local data sync)

| **NAME**                                 | **DESCRIPTION**                                                                          |
|------------------------------------------|------------------------------------------------------------------------------------------|
| **ESTHESIS_EDGE_LOCAL_ENABLED**          | Whether syncronising data to the InfluxDB database is enabled or not.<br/>Default: false |
| **ESTHESIS_EDGE_LOCAL_INFLUX_DB_URL**    | The URL of the InfluxDB database to use.<br/>Default: http://localhost:9086              |
| **ESTHESIS_EDGE_LOCAL_INFLUX_DB_TOKEN**  | The token to use when connecting to the InfluxDB database.<br/>Default: esthesis-edge    |
| **ESTHESIS_EDGE_LOCAL_INFLUX_DB_BUCKET** | The bucket to use when connecting to the InfluxDB database.<br/>Default: edge            |
| **ESTHESIS_EDGE_LOCAL_INFLUX_DB_ORG**    | The organisation to use when connecting to the InfluxDB database.<br/>Default: esthesis  |

## esthesis CORE configuration (core data sync)

| **NAME**                                    | **DESCRIPTION**                                                                                         |
|---------------------------------------------|---------------------------------------------------------------------------------------------------------|
| **ESTHESIS_EDGE_CORE_PUSH_ENABLED**         | Whether syncronising data to the esthesis CORE is enabled or not.<br/>Default: false                    |
| **ESTHESIS_EDGE_CORE_PUSH_URL**             | The URL of the MQTT server used by esthesis CORE to use.<br/>Default: ssl://mosquitto.esthesis:8883     |
| **ESTHESIS_EDGE_CORE_PUSH_TOPIC_TELEMETRY** | The topic to use when pushing telemetry data to esthesis CORE.<br/>Default: esthesis/telemetry          |
| **ESTHESIS_EDGE_CORE_PUSH_TOPIC_PING**      | The topic to use when pushing ping data to esthesis CORE.<br/>Default: esthesis/ping                    |
| **ESTHESIS_EDGE_CORE_REGISTRATION_ENABLED** | Whether new esthesis EDGE devices are registered as devices in esthesis CORE or not.<br/>Default: false |
| **ESTHESIS_EDGE_CORE_REGISTRATION_SECRET**  | The secret token to use when registering new devices in esthesis CORE.                                  |
| **ESTHESIS_EDGE_CORE_TAGS**                 | The tags used to identify the device in esthesis CORE.<br/>Default: edge                                |
| **ESTHESIS_EDGE_CORE_KEY_ALGORITHM**        | The algorithm used to generate the device keys by esthesis CORE.<br/>Default: RSA                       |

