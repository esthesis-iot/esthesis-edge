package esthesis.edge.modules.deddie.client;

import esthesis.edge.modules.deddie.dto.DeddieCurvesActiveConsumptionDTO;
import esthesis.edge.modules.deddie.dto.DeddieCurvesEnergyInjectedDTO;
import esthesis.edge.modules.deddie.dto.DeddieCurvesEnergyProducedDTO;
import esthesis.edge.modules.deddie.dto.DeddieCurvesReactivePowerDTO;
import esthesis.edge.modules.deddie.dto.DeddieCurvesSearchParametersDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

/**
 * Deddie API REST client.
 */

@ApplicationScoped
@RegisterRestClient(configKey = "DeddieClient")
public interface DeddieClient {

    /**
     * Fetches energy curves active consumption data from Deddie API.
     *
     * @param body  The search parameters for the curves.
     * @param token The authentication token.
     * @param scope The scope of the request.
     * @return The energy curves active consumption data.
     */
    @POST
    @Path("/getCurvesv2")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    DeddieCurvesActiveConsumptionDTO getCurvesCurvesActiveConsumption(DeddieCurvesSearchParametersDTO body,
                                                                      @HeaderParam("token") String token,
                                                                      @HeaderParam("scope") String scope);

    /**
     * Fetches energy curves reactive power data from Deddie API.
     *
     * @param body  The search parameters for the curves.
     * @param token The authentication token.
     * @param scope The scope of the request.
     * @return The energy curves reactive power data.
     */
    @POST
    @Path("/getCurvesv2")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    DeddieCurvesReactivePowerDTO getCurvesReactivePower(DeddieCurvesSearchParametersDTO body,
                                                        @HeaderParam("token") String token,
                                                        @HeaderParam("scope") String scope);


    /**
     * Fetches energy curves energy produced data from Deddie API.
     *
     * @param body  The search parameters for the curves.
     * @param token The authentication token.
     * @param scope The scope of the request.
     * @return The energy curves energy produced data.
     */
    @POST
    @Path("/getCurvesv2")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    DeddieCurvesEnergyProducedDTO getCurvesEnergyProduced(DeddieCurvesSearchParametersDTO body,
                                                          @HeaderParam("token") String token,
                                                          @HeaderParam("scope") String scope);

    /**
     * Fetches energy curves energy injected data from Deddie API.
     *
     * @param body  The search parameters for the curves.
     * @param token The authentication token.
     * @param scope The scope of the request.
     * @return The energy curves energy injected data.
     */
    @POST
    @Path("/getCurvesv2")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    DeddieCurvesEnergyInjectedDTO getCurvesEnergyInjected(DeddieCurvesSearchParametersDTO body,
                                                          @HeaderParam("token") String token,
                                                          @HeaderParam("scope") String scope);

    /**
     * Retrieves a list of supplies associated with a given tax number.
     *
     * @param taxNumber The tax number to search for supplies.
     * @param token     The authentication token.
     * @param scope     The scope of the request.
     * @return A list of Strings which contain the supplies associated with the tax number.
     * The format of each string is "supply_number (start_date-end_date)".
     * example: "1234567890  (16/01/2004-14/08/2025)".
     *
     */
    @POST
    @Path("/retrieveSuppliesList")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    List<String> retrieveSuppliesList(String taxNumber,
                                      @HeaderParam("token") String token,
                                      @HeaderParam("scope") String scope);


}
