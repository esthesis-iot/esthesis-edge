package esthesis.edge.modules.fronius.client;

import esthesis.edge.modules.fronius.dto.FroniusPowerFlowRealtimeDataDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Fronius REST client.
 */

@ApplicationScoped
@RegisterRestClient(configKey = "FroniusClient")
public interface FroniusClient {


    /**
     * Retrieves real-time data on the current state of the local energy grid, including grid power, load consumption,
     * and generation from all inverters and batteries. The sum of all powers may not equal zero due to asynchronous data collection.
     * All inverters and batteries are reported regardless of visibility settings.
     *
     * @return the current real-time power flow data
     */
    @GET
    @Path("v1/GetPowerFlowRealtimeData.fcgi")
    @Produces(MediaType.APPLICATION_JSON)
    FroniusPowerFlowRealtimeDataDTO getPowerFlowRealtimeData();
}
