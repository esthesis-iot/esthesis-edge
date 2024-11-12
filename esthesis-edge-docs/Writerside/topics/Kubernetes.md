# Kubernetes

esthesis EDGE can be installed on Kubernetes using Helm. The Helm chart is available as an OCI artifact in the 
esthesis EDGE GitHub [Docker Hub repository](https://hub.docker.com/repository/docker/esthesisiot/esthesis-edge-helm/general).

## Requirements
- A Kubernetes cluster with Ingress support.
- [Helm](https://helm.sh)

## Installation
To install esthesis EDGE on Kubernetes, execute the following commands:

```Bash
helm install edge oci://registry-1.docker.io/esthesisiot/esthesis-edge-helm --version {version}
```

The above command will install the full stack of esthesis EDGE, consisting of esthesis EDGE backend, MariaDB, 
and InfluxDB. The default values of the Helm chart will properly configure the above components to work with each other,
however no services will be exposed nor any module will be enabled. You can configure esthesis EDGE by providing a
values file while installing the Helm chart, or passing command-line arguments to the `helm install` command. 

For a full list of configuration options, see the [](Configuration.md) section of the documentation.