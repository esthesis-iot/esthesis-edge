package esthesis.edge.modules.enedis.client;

import esthesis.edge.modules.enedis.dto.datahub.EnedisAuthTokenDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisConsumptionLoadCurveDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisDailyConsumptionDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisDailyConsumptionMaxPowerDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisDailyProductionDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisProductionLoadCurveDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Enedis REST client.
 */
@ApplicationScoped
@RegisterRestClient(configKey = "EnedisClient")
public interface EnedisClient {

  /**
   * Get an authentication token from Enedis.
   *
   * @param grantType    The grant type (client_credentials).
   * @param clientId     The Enedis application client ID.
   * @param clientSecret The Enedis application client secret.
   * @return The authentication token.
   */
  @POST
  @Path("oauth2/v3/token")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  EnedisAuthTokenDTO getAuthToken(
      @FormParam("grant_type") String grantType,
      @FormParam("client_id") String clientId,
      @FormParam("client_secret") String clientSecret
  );

  /**
   * Get daily consumption data from Enedis.
   *
   * @param startDate    The start date to fetch from, in YYYY-MM-DD format.
   * @param endDate      The end date to fetch to, in YYYY-MM-DD format.
   * @param usagePointId The usage point ID (Enedis PRM).
   * @param bearerToken  The bearer token to authenticate with.
   * @return The daily consumption data.
   */
  @GET
  @Path("metering_data_dc/v5/daily_consumption")
  @Produces(MediaType.APPLICATION_JSON)
  EnedisDailyConsumptionDTO getDailyConsumption(
      @QueryParam("start") String startDate,
      @QueryParam("end") String endDate,
      @QueryParam("usage_point_id") String usagePointId,
      @HeaderParam("Authorization") String bearerToken
  );

  /**
   * Get daily consumption max power data from Enedis.
   *
   * @param startDate    The start date to fetch from, in YYYY-MM-DD format.
   * @param endDate      The end date to fetch to, in YYYY-MM-DD format.
   * @param usagePointId The usage point ID (Enedis PRM).
   * @param bearerToken  The bearer token to authenticate with.
   * @return The daily consumption max power data.
   */
  @GET
  @Path("metering_data_dcmp/v5/daily_consumption_max_power")
  @Produces(MediaType.APPLICATION_JSON)
  EnedisDailyConsumptionMaxPowerDTO getDailyConsumptionMaxPower(
      @QueryParam("start") String startDate,
      @QueryParam("end") String endDate,
      @QueryParam("usage_point_id") String usagePointId,
      @HeaderParam("Authorization") String bearerToken
  );

  /**
   * Get daily production data from Enedis.
   *
   * @param startDate    The start date to fetch from, in YYYY-MM-DD format.
   * @param endDate      The end date to fetch to, in YYYY-MM-DD format.
   * @param usagePointId The usage point ID (Enedis PRM).
   * @param bearerToken  The bearer token to authenticate with.
   * @return The daily consumption data.
   */
  @GET
  @Path("metering_data_dp/v5/daily_production")
  @Produces(MediaType.APPLICATION_JSON)
  EnedisDailyProductionDTO getDailyProduction(
      @QueryParam("start") String startDate,
      @QueryParam("end") String endDate,
      @QueryParam("usage_point_id") String usagePointId,
      @HeaderParam("Authorization") String bearerToken
  );

  /**
   * Get contract information available for a given usage point. Unfortunately, this endpoint returns
   * application/octet-stream or application/text, irrespectively of asking for JSON, so the user of
   * this client needs to manually parse the response to {@see EnedisContractDTO}.
   * @param usagePointId The usage point ID (Enedis PRM).
   * @param bearerToken The bearer token to authenticate with.
   * @return The contract information.
   */
  @GET
  @Path("customers_upc/v5/usage_points/contracts")
  @Produces(MediaType.APPLICATION_JSON)
  String getContracts(
      @QueryParam("usage_point_id") String usagePointId,
      @HeaderParam("Authorization") String bearerToken
  );

  /**
   * Get load curve consumption data from Enedis per 30 minutes.
   *
   * @param startDate    The start date to fetch from, in YYYY-MM-DD format.
   * @param endDate      The end date to fetch to, in YYYY-MM-DD format.
   * @param usagePointId The usage point ID (Enedis PRM).
   * @param bearerToken  The bearer token to authenticate with.
   * @return The average load curve consumption data for the given period.
   */
  @GET
  @Path("metering_data_clc/v5/consumption_load_curve")
  @Produces(MediaType.APPLICATION_JSON)
  EnedisConsumptionLoadCurveDTO getConsumptionLoadCurve(
          @QueryParam("start") String startDate,
          @QueryParam("end") String endDate,
          @QueryParam("usage_point_id") String usagePointId,
          @HeaderParam("Authorization") String bearerToken
  );

  /**
   * Get load curve production data from Enedis per 30 minutes.
   *
   * @param startDate    The start date to fetch from, in YYYY-MM-DD format.
   * @param endDate      The end date to fetch to, in YYYY-MM-DD format.
   * @param usagePointId The usage point ID (Enedis PRM).
   * @param bearerToken  The bearer token to authenticate with.
   * @return The average load curve production data for the given period.
   */
  @GET
  @Path("metering_data_plc/v5/production_load_curve")
  @Produces(MediaType.APPLICATION_JSON)
  EnedisProductionLoadCurveDTO getProductionLoadCurve(
          @QueryParam("start") String startDate,
          @QueryParam("end") String endDate,
          @QueryParam("usage_point_id") String usagePointId,
          @HeaderParam("Authorization") String bearerToken
  );
}
