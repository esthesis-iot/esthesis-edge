# API documentation

esthesis EDGE provides an OpenAPI 3.0 documentation for the API. The documentation is available at the following URL:
```
http://{host:port}/api/openapi-ui
```

## Admin vs Public API resources
The API provides public endpoints that can be accessed by anyone, as well as private endpoints that require
authentication. Authentication takes place via a custom HTTP header using a secret token.

- The name of the HTTP header is `X-ESTHESIS-EDGE-ADMIN-SECRET`.
- The expected value of the header is defined via the `ESTHESIS_EDGE_ADMIN_SECRET` environment variable, or can be
set in a Helm installation via `--set esthesis.edge.admin.secret`.