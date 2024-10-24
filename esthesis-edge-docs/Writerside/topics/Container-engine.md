---
title: Container engine
---

# Container engine installation guide
esthesis EDGE publishes linux/amd64 and linux/arm64 container images as part of its release process. Images are pushed
to [Docker Hub](https://hub.docker.com/repository/docker/esthesisiot/esthesis-edge/general) and are freely available to 
everyone.

## Requirements
- A container engine runtime.
- MariaDB database.
- Access to an InfluxDB database (optional, if you need [local data sync](How-it-works.md#local-data-sync)).
- Access to esthesis CORE (optional, if you need [esthesis CORE data sync](How-it-works.md#esthesis-core-data-sync)).

## Standalone installation
The standalone installation instantiates esthesis EDGE in your container engine without bringing up additional 
components, such as MariaDB and InfluxDB. At minimum, you need to provide access to a MariaDB for your container to be 
operational. You can start esthesis EDGE as a standalone container using the following command:
```Docker

```

## Compose installation
We provide a Docker Compose file, so that you can instantiate esthesis EDGE together with all its dependencies to
support local data sync.