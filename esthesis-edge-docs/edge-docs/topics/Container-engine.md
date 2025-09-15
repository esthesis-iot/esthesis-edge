---
title: Container engine
---

# Container engine installation guide
esthesis EDGE publishes `linux/amd64` and `linux/arm64` container images as part of its release process. Images are pushed
to [Docker Hub](https://hub.docker.com/repository/docker/esthesisiot/esthesis-edge/general).

## Requirements
- A container engine runtime.
- A MariaDB (11.x or newer) database. 
- Access to an InfluxDB (2.7.x or newer) database (optional, if you need [local data sync](How-it-works.md#local-data-sync)).
- Access to esthesis CORE (optional, if you need [esthesis CORE data sync](How-it-works.md#esthesis-core-data-sync)).

## Standalone installation
The standalone installation instantiates esthesis EDGE in your container engine without bringing up additional 
components, such as MariaDB and InfluxDB. At minimum, you need to provide access to a MariaDB for your container to be 
operational. 

The following command demonstrates how you can start esthesis EDGE as a Docker container, specifying a
minimal configuration providing local data sync to an existing MariaDB. For a full list of configuration option, 
see [](Configuration.md).
```Docker
docker run \
    -p 8080:8080 \
    -e QUARKUS_DATASOURCE_JDBC_URL=jdbc:mariadb://some-mariadb:3306/esthesis-edge \
    -e QUARKUS_DATASOURCE_USERNAME=root \
    -e QUARKUS_DATASOURCE_PASSWORD=root
    esthesisiot/esthesis-edge:latest
```

You can quickly test esthesis EDGE is up and running by executing:
```Bash
curl localhost:8080/q/health
```

## Compose installation
A Docker Compose file is provided to instantiate esthesis EDGE together with all its dependencies to support local data 
sync. The default configuration will instantiate esthesis EDGE, together with a MariaDB database, as well as an InfluxDB 
database. You can download the Docker Compose file from the esthesis EDGE 
[GitHub repository](https://github.com/esthesis-iot/esthesis-edge), or download it with [ORAS](https://oras.land) via:
\
`oras pull docker.io/esthesisiot/esthesis-edge-docker-compose:latest`

To start esthesis EDGE with Docker Compose, execute:
```Bash
docker compose up
```

<tip>
Neither standalone nor compose installations will initiate any of the modules of esthesis EDGE. To start a module
you need to specify the relevant module configuration options. For a full list of configuration options, see the
Configuration section of the documentation.
</tip>
<warning>
Both standalone and compose installations reference the "latest" version of esthesis EDGE. If you are using either for
a production installation, it is highly recommended to specify a version tag to ensure the stability of your deployment.
</warning>