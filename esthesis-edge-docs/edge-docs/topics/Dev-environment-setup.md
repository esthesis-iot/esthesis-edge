# Dev environment setup

In the following sections you can see how to set up a development environment for esthesis EDGE.

The development environment consists of a MariaDB to hold esthesis EDGE's data, and an InfluxDB in which data is synced
to. The development environment can be set up using Docker Compose.

## Requirements

- A Docker Engine.
- A Docker Desktop or Docker client supporting the `compose` command.

## Setting up supporting infrastructure

To set up the supporting infrastructure (MariaDB and InfluxDB), you can use the Docker Compose file available under
the "_dev" directory in the root of the project, and execute:

```shell
docker compose up -d
```

### Access to supporting resources

The resources that become available after setting up the development environment are:

| **Resource** | **URL/host**       | **Credentials**                       |
|----------|----------------|-----------------------------------|
| MariaDB  | localhost:4306 | esthesis-system / esthesis-system |
| InfluxDB | localhost:9086 | esthesis-system / esthesis-system |

## Running the services
esthesis EDGE only consists of a backend service, for now. To run the backend service, you can use the `dev.sh` script
available under `esthesis-edge-backend` directory. The default configuration paramaters of the above Docker compose as
well as the ones setup in `application.yaml` will get you up and running with no additional changes.

## Notes
1. You can create a `local-env.sh` script alongside the `dev.sh` script to customise your local development environment. 
If such a file exist, it will be sourced by the `dev.sh`. Your `local-env.sh` file can be used to set environment variables
unique to your own development environment, for example:
    ```
    export ESTHESIS_EDGE_MODULES_ENEDIS_CLIENT_ID="myclientId"
    export ESTHESIS_EDGE_MODULES_ENEDIS_CLIENT_SECRET="myclientSecret"
    export ESTHESIS_EDGE_CORE_MQTT_CERT="myesthesisCRT-as-base64" 
    export ESTHESIS_EDGE_MODULES_ENEDIS_ENABLED="true"
    ```