# Examples

Here are some possible examples of setting up esthesis EDGE in different environments. Those examples contain sample
values for some of the critical environment variables, and you should replace them with your own values.

## Standalone installation
Start esthesis EDGE as a Docker container, connecting it to an external MariaDB and InfluxDB. Enable Enedis module, and
configure local data sync, as well as esthesis CORE data sync and device registration:

```Bash
docker run -d \
  --pull=always \
  -p 9080:8080 \
  -e QUARKUS_DATASOURCE_JDBC_URL="jdbc:mariadb://mariadb:3306/esthesis-edge" \
  -e QUARKUS_REST_CLIENT_ESTHESIS_AGENT_SERVICE_CLIENT_URL="http://esthesis:59070" \
  -e ESTHESIS_EDGE_ADMIN_SECRET="e35b9b5d-8831-4466-ac50-419b2a89c8b6" \
  -e ESTHESIS_EDGE_LOCAL_ENABLED="true" \
  -e ESTHESIS_EDGE_LOCAL_INFLUX_DB_URL="http://influxdb:8086" \
  -e QUARKUS_REST_CLIENT_ENEDIS_CLIENT_URL=https://gw.ext.prod-sandbox.api.enedis.fr \
  -e ESTHESIS_EDGE_LOCAL_INFLUX_DB_TOKEN="Bfe-aqkUXEh9owtXKSu1V5zZ4D_1L88kD365dFYrv_trVjjqSoGEqo41j3aXfrflxfibE6PCWILchlqUymrWTw==" \
  -e ESTHESIS_EDGE_CORE_PUSH_ENABLED="true" \
  -e ESTHESIS_EDGE_CORE_PUSH_URL="ssl://mosquitto:8883" \
  -e ESTHESIS_EDGE_CORE_REGISTRATION_ENABLED="true" \
  -e ESTHESIS_EDGE_MODULES_ENEDIS_ENABLED="true" \
  -e ESTHESIS_EDGE_MODULES_ENEDIS_CLIENT_ID="clientId" \
  -e ESTHESIS_EDGE_MODULES_ENEDIS_CLIENT_SECRET="clientSecret" \
  -e ESTHESIS_EDGE_MODULES_ENEDIS_FETCH_TYPES_DC_ENABLED="true" \
  -e ESTHESIS_EDGE_MODULES_ENEDIS_FETCH_TYPES_DCMP_ENABLED="true" \
  -e ESTHESIS_EDGE_MODULES_ENEDIS_FETCH_TYPES_DP_ENABLED="true" \
  -e ESTHESIS_EDGE_MODULES_ENEDIS_SELF_REGISTRATION_ENABLED="true" \
  -e ESTHESIS_EDGE_MODULES_ENEDIS_SELF_REGISTRATION_STATE_CHECKING="true" \
  -e ESTHESIS_EDGE_CORE_CERT="LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUMxRENDQWJ5Z0F3SUJBZ0lCQVRBTkJna3Foa2lHOXcwQkFRc0ZBREFiTVJrd0Z3WURWUVFEREJCbGMzUm8KWlhOcGN5MWpiM0psTFdOaE1CNFhEVEkwTURZeE9UQTVNRGt4TkZvWERUTTVNVEl6TURJeU1EQXdNRm93R3pFWgpNQmNHQTFVRUF3d1FaWE4wYUdWemFYTXRZMjl5WlMxallUQ0NBU0l3RFFZSktvWklodmNOQVFFQkJRQURnZ0VQCkFEQ0NBUW9DZ2dFQkFOandnNUFqMG9vOWtycDJ0NGdTR2FORFVFbSt6WnkxUytvSlBKYTJMcXN3VU1halFyQlMKVjROY3FsSGVYc3lNcGJKclBZNHlGZGx0WldnUFBTVXgvck1qUmV4ZlBNSVc5cENucFZscEZaNGZqTXhBOWptaAo5MnB1NTZuNEc0M0hiOTQyOHI0bmRsSmNmVDNqRGkwNC9JVFpYNXAyejZQekpMTVhaWVE0d3hlOWdJVm44WHplClhoUXRmV3FuTWNRRGFQek9zUUtrd3RML3Azak13cnZYOURGWjNFMFFQOUpNT2dmdzBhZlB2ZGd0ckkzbStIMWoKY3RHQkZKUWx6ZGdNUlh6M01XQXg0WjVEM3pKUE9ycDQ1SGswL1l3S3V2WlRpTjJsVUZXOTRscVVCTW9lYjdOOAphb3RTbzh5dHR3REtTMzNEcnFQaVhiNWJJcm14VUdYMUlWTUNBd0VBQWFNak1DRXdEd1lEVlIwVEFRSC9CQVV3CkF3RUIvekFPQmdOVkhROEJBZjhFQkFNQ0FRWXdEUVlKS29aSWh2Y05BUUVMQlFBRGdnRUJBQjJhSnl3MVJQQy8KUllrcEpTaG9JQjZiUFM1aHN4cHRrOExPaFNLQytab294cE5zTU05S1AxVEJrSjY3ZXQvYWs2RXRiMTIxSlNCdQpUVWFCWWVLcm9xMi9LNGI5a1E1ZVJRTGt6SUtrYTY1WkJGR0xuaklSR0pYL255SHJtanNMOUxRUTlXYWdpcDhiCms3VDYyOWhVWkkveVlmRGs4MWtaV0ROaEtwNjdZNEQwL1I0dFZZSTBBdEh3R2tpTEFETTB2M3JPMWt5RjNsYVoKV3VZcG9PVktmQkp5NXhKQmJXVGJVeGR0dU5VY25KeUIrZ054RFV3WnRoL3AxMWJzV2ltT05heFBWZGpFN1RpVgpMUWdwZ0tNeVdCL2hxK3lBQmJ0NkVlSWdvd0gvSVJjS0xiUEswOTd5alp6c25vYzAvdkUyK0o5WTBtOU9BQjlsCkZXdkl0Q1NRQ3hrPQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCg==" \
  --name esthesis-edge \
  esthesisiot/esthesis-edge:latest
```

- **Test administration API authentication**
    
    ```curl -v -H "X-ESTHESIS-EDGE-ADMIN-SECRET: e35b9b5d-8831-4466-ac50-419b2a89c8b6" localhost:9080/admin/auth```

- **Get registered devices**
    
    ```curl -H "X-ESTHESIS-EDGE-ADMIN-SECRET: e35b9b5d-8831-4466-ac50-419b2a89c8b6" localhost:9080/admin/devices```

- **List queued data**

    ```curl -H "X-ESTHESIS-EDGE-ADMIN-SECRET: e35b9b5d-8831-4466-ac50-419b2a89c8b6" localhost:9080/admin/queue```

- **Open Enedis self-registration page**

  [](http://localhost:9080/enedis/public/self-registration)

- **Get Enedis configuration**

    ```curl -H "X-ESTHESIS-EDGE-ADMIN-SECRET: e35b9b5d-8831-4466-ac50-419b2a89c8b6" localhost:9080/enedis/admin/config```

- **Get Edge configuration**

    ```curl -H "X-ESTHESIS-EDGE-ADMIN-SECRET: e35b9b5d-8831-4466-ac50-419b2a89c8b6" localhost:9080/admin/config```

## Compose installation
Start esthesis EDGE using Docker Compose, including a MariaDB as well as an InfluxDB databases. Enable Enedis module, 
and  configure local data sync, as well as esthesis CORE data sync and device registration:

```Bash
oras pull docker.io/esthesisiot/esthesis-edge-docker-compose:latest
```

```Bash
ESTHESIS_EDGE_SERVICE_PORT=10080 \
ESTHESIS_EDGE_INFLUX_DB_SERVICE_PORT=10086 \
QUARKUS_REST_CLIENT_ESTHESIS_AGENT_SERVICE_CLIENT_URL="http://esthesis:59070" \
ESTHESIS_EDGE_ADMIN_SECRET="e35b9b5d-8831-4466-ac50-419b2a89c8b6" \
ESTHESIS_EDGE_LOCAL_ENABLED="true" \
QUARKUS_REST_CLIENT_ENEDIS_CLIENT_URL="https://gw.ext.prod-sandbox.api.enedis.fr" \
ESTHESIS_EDGE_LOCAL_INFLUX_DB_TOKEN="Bfe-aqkUXEh9owtXKSu1V5zZ4D_1L88kD365dFYrv_trVjjqSoGEqo41j3aXfrflxfibE6PCWILchlqUymrWTw==" \
ESTHESIS_EDGE_CORE_PUSH_ENABLED="true" \
ESTHESIS_EDGE_CORE_PUSH_URL="ssl://mosquitto:8883" \
ESTHESIS_EDGE_CORE_REGISTRATION_ENABLED="true" \
ESTHESIS_EDGE_MODULES_ENEDIS_ENABLED="true" \
ESTHESIS_EDGE_MODULES_ENEDIS_CLIENT_ID="clientId" \
ESTHESIS_EDGE_MODULES_ENEDIS_CLIENT_SECRET="clientSecret" \
ESTHESIS_EDGE_MODULES_ENEDIS_FETCH_TYPES_DC_ENABLED="true" \
ESTHESIS_EDGE_MODULES_ENEDIS_FETCH_TYPES_DCMP_ENABLED="true" \
ESTHESIS_EDGE_MODULES_ENEDIS_FETCH_TYPES_DP_ENABLED="true" \
ESTHESIS_EDGE_MODULES_ENEDIS_SELF_REGISTRATION_ENABLED="true" \
ESTHESIS_EDGE_MODULES_ENEDIS_SELF_REGISTRATION_STATE_CHECKING="true" \
ESTHESIS_EDGE_CORE_CERT="LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUMxRENDQWJ5Z0F3SUJBZ0lCQVRBTkJna3Foa2lHOXcwQkFRc0ZBREFiTVJrd0Z3WURWUVFEREJCbGMzUm8KWlhOcGN5MWpiM0psTFdOaE1CNFhEVEkwTURZeE9UQTVNRGt4TkZvWERUTTVNVEl6TURJeU1EQXdNRm93R3pFWgpNQmNHQTFVRUF3d1FaWE4wYUdWemFYTXRZMjl5WlMxallUQ0NBU0l3RFFZSktvWklodmNOQVFFQkJRQURnZ0VQCkFEQ0NBUW9DZ2dFQkFOandnNUFqMG9vOWtycDJ0NGdTR2FORFVFbSt6WnkxUytvSlBKYTJMcXN3VU1halFyQlMKVjROY3FsSGVYc3lNcGJKclBZNHlGZGx0WldnUFBTVXgvck1qUmV4ZlBNSVc5cENucFZscEZaNGZqTXhBOWptaAo5MnB1NTZuNEc0M0hiOTQyOHI0bmRsSmNmVDNqRGkwNC9JVFpYNXAyejZQekpMTVhaWVE0d3hlOWdJVm44WHplClhoUXRmV3FuTWNRRGFQek9zUUtrd3RML3Azak13cnZYOURGWjNFMFFQOUpNT2dmdzBhZlB2ZGd0ckkzbStIMWoKY3RHQkZKUWx6ZGdNUlh6M01XQXg0WjVEM3pKUE9ycDQ1SGswL1l3S3V2WlRpTjJsVUZXOTRscVVCTW9lYjdOOAphb3RTbzh5dHR3REtTMzNEcnFQaVhiNWJJcm14VUdYMUlWTUNBd0VBQWFNak1DRXdEd1lEVlIwVEFRSC9CQVV3CkF3RUIvekFPQmdOVkhROEJBZjhFQkFNQ0FRWXdEUVlKS29aSWh2Y05BUUVMQlFBRGdnRUJBQjJhSnl3MVJQQy8KUllrcEpTaG9JQjZiUFM1aHN4cHRrOExPaFNLQytab294cE5zTU05S1AxVEJrSjY3ZXQvYWs2RXRiMTIxSlNCdQpUVWFCWWVLcm9xMi9LNGI5a1E1ZVJRTGt6SUtrYTY1WkJGR0xuaklSR0pYL255SHJtanNMOUxRUTlXYWdpcDhiCms3VDYyOWhVWkkveVlmRGs4MWtaV0ROaEtwNjdZNEQwL1I0dFZZSTBBdEh3R2tpTEFETTB2M3JPMWt5RjNsYVoKV3VZcG9PVktmQkp5NXhKQmJXVGJVeGR0dU5VY25KeUIrZ054RFV3WnRoL3AxMWJzV2ltT05heFBWZGpFN1RpVgpMUWdwZ0tNeVdCL2hxK3lBQmJ0NkVlSWdvd0gvSVJjS0xiUEswOTd5alp6c25vYzAvdkUyK0o5WTBtOU9BQjlsCkZXdkl0Q1NRQ3hrPQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCg==" \
docker compose up
```
- **Test administration API authentication**

  ```curl -v -H "X-ESTHESIS-EDGE-ADMIN-SECRET:e35b9b5d-8831-4466-ac50-419b2a89c8b6" localhost:10080/admin/auth```

- **Get registered devices**

  ```curl -H "X-ESTHESIS-EDGE-ADMIN-SECRET: e35b9b5d-8831-4466-ac50-419b2a89c8b6" localhost:10080/admin/devices```

- **List queued data**

  ```curl -H "X-ESTHESIS-EDGE-ADMIN-SECRET: e35b9b5d-8831-4466-ac50-419b2a89c8b6" localhost:10080/admin/queue```

- **Open Enedis self-registration page**

  [](http://localhost:8080/enedis/public/self-registration)

- **Get Enedis configuration**

  ```curl -H "X-ESTHESIS-EDGE-ADMIN-SECRET: e35b9b5d-8831-4466-ac50-419b2a89c8b6" localhost:10080/enedis/admin/config```

- **Get Edge configuration**

  ```curl -H "X-ESTHESIS-EDGE-ADMIN-SECRET: e35b9b5d-8831-4466-ac50-419b2a89c8b6" localhost:10080/admin/config```

## Kubernetes installation
Start esthesis EDGE in Kubernetes, deploying all supporting infrastructure, including MariaDB and InfluxDB. Enable Enedis
module, and configure local data sync, as well as esthesis CORE data sync and device registration. The esthesis EDGE
service is exposed as a LoadBalancer service:

```Bash
helm install edge oci://registry-1.docker.io/esthesisiot/esthesis-edge-helm --version 
```

```Bash
helm upgrade --install edge . \
  --set global.esthesisRegistry="192.168.50.211:5000/esthesis" \
  --set global.timezone="Europe/Athens" \
  --set quarkus.log.level="DEBUG" \
  --set quarkus.restClient.esthesisAgentServiceClient.url="http://192.168.40.236:59070" \
  --set quarkus.restClient.enedisClient.url="https://gw.ext.prod-sandbox.api.enedis.fr" \
  --set esthesis.edge.adminSecret="e35b9b5d-8831-4466-ac50-419b2a89c8b6" \
  --set esthesis.edge.service.type="LoadBalancer" \
  --set esthesis.edge.local.enabled="true" \
  --set esthesis.edge.local.influxDb.token="fVfC4PNVzPnO_qsYY4X3lsvYmqIZNFHE3kE6fI8hpZqR" \
  --set esthesis.edge.local.influxDb.url="http://edge-influxdb:8086" \
  --set esthesis.edge.core.push.enabled="true" \
  --set esthesis.edge.core.push.url="ssl://mosquitto.esthesis:8883" \
  --set esthesis.edge.core.registration.enabled="true" \
  --set esthesis.edge.core.registration.secret="ac50419b2a89c8b6" \
  --set esthesis.edge.core.cert="LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUMxRENDQWJ5Z0F3SUJBZ0lCQVRBTkJna3Foa2lHOXcwQkFRc0ZBREFiTVJrd0Z3WURWUVFEREJCbGMzUm8KWlhOcGN5MWpiM0psTFdOaE1CNFhEVEkwTURZeE9UQTVNRGt4TkZvWERUTTVNVEl6TURJeU1EQXdNRm93R3pFWgpNQmNHQTFVRUF3d1FaWE4wYUdWemFYTXRZMjl5WlMxallUQ0NBU0l3RFFZSktvWklodmNOQVFFQkJRQURnZ0VQCkFEQ0NBUW9DZ2dFQkFOandnNUFqMG9vOWtycDJ0NGdTR2FORFVFbSt6WnkxUytvSlBKYTJMcXN3VU1halFyQlMKVjROY3FsSGVYc3lNcGJKclBZNHlGZGx0WldnUFBTVXgvck1qUmV4ZlBNSVc5cENucFZscEZaNGZqTXhBOWptaAo5MnB1NTZuNEc0M0hiOTQyOHI0bmRsSmNmVDNqRGkwNC9JVFpYNXAyejZQekpMTVhaWVE0d3hlOWdJVm44WHplClhoUXRmV3FuTWNRRGFQek9zUUtrd3RML3Azak13cnZYOURGWjNFMFFQOUpNT2dmdzBhZlB2ZGd0ckkzbStIMWoKY3RHQkZKUWx6ZGdNUlh6M01XQXg0WjVEM3pKUE9ycDQ1SGswL1l3S3V2WlRpTjJsVUZXOTRscVVCTW9lYjdOOAphb3RTbzh5dHR3REtTMzNEcnFQaVhiNWJJcm14VUdYMUlWTUNBd0VBQWFNak1DRXdEd1lEVlIwVEFRSC9CQVV3CkF3RUIvekFPQmdOVkhROEJBZjhFQkFNQ0FRWXdEUVlKS29aSWh2Y05BUUVMQlFBRGdnRUJBQjJhSnl3MVJQQy8KUllrcEpTaG9JQjZiUFM1aHN4cHRrOExPaFNLQytab294cE5zTU05S1AxVEJrSjY3ZXQvYWs2RXRiMTIxSlNCdQpUVWFCWWVLcm9xMi9LNGI5a1E1ZVJRTGt6SUtrYTY1WkJGR0xuaklSR0pYL255SHJtanNMOUxRUTlXYWdpcDhiCms3VDYyOWhVWkkveVlmRGs4MWtaV0ROaEtwNjdZNEQwL1I0dFZZSTBBdEh3R2tpTEFETTB2M3JPMWt5RjNsYVoKV3VZcG9PVktmQkp5NXhKQmJXVGJVeGR0dU5VY25KeUIrZ054RFV3WnRoL3AxMWJzV2ltT05heFBWZGpFN1RpVgpMUWdwZ0tNeVdCL2hxK3lBQmJ0NkVlSWdvd0gvSVJjS0xiUEswOTd5alp6c25vYzAvdkUyK0o5WTBtOU9BQjlsCkZXdkl0Q1NRQ3hrPQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCg==" \
  --set esthesis.edge.modules.enedis.enabled="true" \
  --set esthesis.edge.modules.enedis.clientId="ezLeB5eMv5IJKZFykDfYXjS0jdAa" \
  --set esthesis.edge.modules.enedis.clientSecret="nFfbBuxVZU4IfwNSe6uhxaIPmJsa" \
  --set esthesis.edge.modules.enedis.fetchTypes.dc.enabled="true" \
  --set esthesis.edge.modules.enedis.fetchTypes.dcmp.enabled="true" \
  --set esthesis.edge.modules.enedis.fetchTypes.dp.enabled="true" \
  --set esthesis.edge.modules.enedis.selfRegistration.enabled="true" \
  --set esthesis.edge.modules.enedis.selfRegistration.stateChecking="false"
```

- **Find service IP address** 
    ```EDGE_SRV_IP=$(kubectl get svc esthesis-edge-service -o jsonpath='{.status.loadBalancer.ingress[0].ip}')```

- **Test administration API authentication**

  ```curl -v -H "X-ESTHESIS-EDGE-ADMIN-SECRET: e35b9b5d-8831-4466-ac50-419b2a89c8b6" $EDGE_SRV_IP/admin/auth```

- **Get registered devices**

  ```curl -H "X-ESTHESIS-EDGE-ADMIN-SECRET: e35b9b5d-8831-4466-ac50-419b2a89c8b6" $EDGE_SRV_IP/admin/devices```

- **List queued data**

  ```curl -H "X-ESTHESIS-EDGE-ADMIN-SECRET: e35b9b5d-8831-4466-ac50-419b2a89c8b6" $EDGE_SRV_IP/admin/queue```

- **Open Enedis self-registration page**

  - Display the IP of the esthesis EDGE service:
  `echo $EDGE_SRV_IP`
  - Use the above IP:
  [](http://{IP}}/enedis/public/self-registration)

- **Get Enedis configuration**

  ```curl -H "X-ESTHESIS-EDGE-ADMIN-SECRET: e35b9b5d-8831-4466-ac50-419b2a89c8b6" $EDGE_SRV_IP/enedis/admin/config```

- **Get Edge configuration**

  ```curl -H "X-ESTHESIS-EDGE-ADMIN-SECRET: e35b9b5d-8831-4466-ac50-419b2a89c8b6" $EDGE_SRV_IP/admin/config```
