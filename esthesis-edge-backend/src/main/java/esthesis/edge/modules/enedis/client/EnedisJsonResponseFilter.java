package esthesis.edge.modules.enedis.client;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

/**
 * The Enedis gateway responds with Content-Type application/octet-stream on its GET endpoints
 * (the ITC and mesure APIs; verified against the sandbox on 2026-08-21), which prevents the REST
 * client from mapping the JSON bodies to DTOs. Every Enedis response body this client consumes is
 * JSON, so rewrite the response Content-Type accordingly.
 */
public class EnedisJsonResponseFilter implements ClientResponseFilter {

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) {
        responseContext.getHeaders().putSingle(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
    }
}
