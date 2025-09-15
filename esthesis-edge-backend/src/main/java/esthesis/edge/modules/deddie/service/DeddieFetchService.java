package esthesis.edge.modules.deddie.service;

import esthesis.edge.dto.QueueItemDTO;
import esthesis.edge.modules.deddie.client.DeddieClient;
import esthesis.edge.modules.deddie.config.DeddieConstants;
import esthesis.edge.modules.deddie.config.DeddieProperties;
import esthesis.edge.modules.deddie.dto.DeddieCurvesActiveConsumptionDTO;
import esthesis.edge.modules.deddie.dto.DeddieCurvesEnergyInjectedDTO;
import esthesis.edge.modules.deddie.dto.DeddieCurvesEnergyProducedDTO;
import esthesis.edge.modules.deddie.dto.DeddieCurvesReactivePowerDTO;
import esthesis.edge.modules.deddie.dto.DeddieCurvesSearchParametersDTO;
import esthesis.edge.services.DeviceService;
import esthesis.edge.services.FetchHelperService;
import esthesis.edge.services.QueueService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class DeddieFetchService {

    @Inject
    @RestClient
    DeddieClient deddieClient;

    private final QueueService dataService;
    private final DeviceService deviceService;
    private final DeddieProperties deddieProperties;
    private final FetchHelperService fetchHelperService;
    private final DeddieELPMapperService deddieELPMapperService;


    /**
     * Fetches Curve Active Consumption data from DEDDIE API.
     *
     * @param hardwareId The hardware ID of the device.
     * @param accessToken The access token required by the DEDDIE API.
     * @param taxNumber The client tax number associated with the device.
     * @param supplyNumber The client supply number associated with the device.
     *
     * @return the number of items queued for processing/sync.
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public int fetchCacData(String hardwareId,
                            String accessToken,
                            String taxNumber,
                            String supplyNumber) {
        // Get last fetch date for the device.
        String lastFetch = deviceService.getDeviceConfigValueAsInstant(hardwareId,
                        DeddieConstants.CONFIG_CAC_LAST_FETCHED_AT)
                .map(Instant::toString)
                .orElse(Instant.now().minus(Duration.ofDays(deddieProperties.pastDaysInit())).toString());

        // Prepare search parameters for the DEDDIE API request.
        DeddieCurvesSearchParametersDTO searchParametersDTO = new DeddieCurvesSearchParametersDTO();
        searchParametersDTO.setFromDate(lastFetch)
                .setAnalysisType(DeddieConstants.CURVES_ANALYSIS_TYPE)
                .setClassType(DeddieConstants.CURVES_CLASS_TYPE_CAC)
                .setTaxNumber(taxNumber)
                .setSupplyNumber(supplyNumber)
                .setToDate(Instant.now().toString());

        log.debug("Fetching Curve Active Consumption data for hardwareId: {}, fromDate: {}, toDate: {}",
                hardwareId, searchParametersDTO.getFromDate(), searchParametersDTO.getToDate());
        DeddieCurvesActiveConsumptionDTO curvesDTO = null;

        // Perform the API call to fetch the data.

        try {
            curvesDTO = deddieClient.getCurvesCurvesActiveConsumption(searchParametersDTO,
                    accessToken,
                    DeddieConstants.API_SCOPE);

            fetchHelperService.resetErrors(hardwareId, DeddieConstants.CONFIG_CAC_ERRORS);
            log.debug("Fetched Curve Active Consumption data: '{}'.", curvesDTO);

        } catch (Exception e) {
            log.warn("Failed to fetch Curve Active Consumption for device '{}'.", hardwareId, e);
            fetchHelperService.increaseErrors(hardwareId, DeddieConstants.CONFIG_CAC_ERRORS);
        }

        // Queue the fetched data for processing/sync.
        int itemsQueued = 0;

        if (curvesDTO != null && curvesDTO.getCurves() != null && !curvesDTO.getCurves().isEmpty()) {
            log.debug("Queuing Curve Active Consumption data:\n{}", deddieELPMapperService.toELP(curvesDTO));
            dataService.queue(
                    QueueItemDTO.builder()
                            .id(UUID.randomUUID().toString())
                            .createdAt(Instant.now())
                            .hardwareId(hardwareId)
                            .dataObject(deddieELPMapperService.toELP(curvesDTO))
                            .build());


            // Update last fetched at, only if data was fetched. This is due to the fact that data might
            // not be available at the time of fetching, however it may become available later on.
            deviceService.updateDeviceConfig(hardwareId,
                    DeddieConstants.CONFIG_CAC_LAST_FETCHED_AT, Instant.now().toString());

            itemsQueued++;
        } else {
            log.debug("No Curve Active Consumption data found for hardwareId: {}", hardwareId);
        }


        return itemsQueued;
    }

    /**
     * Fetches Curve Reactive Power data from DEDDIE API.
     *
     * @param hardwareId The hardware ID of the device.
     * @param accessToken The access token required by the DEDDIE API.
     * @param taxNumber The client tax number associated with the device.
     * @param supplyNumber The client supply number associated with the device.
     *
     * @return the number of items queued for processing/sync.
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public int fetchCrpData(String hardwareId,
                            String accessToken,
                            String taxNumber,
                            String supplyNumber) {
        // Get last fetch date for the device.
        String lastFetch = deviceService.getDeviceConfigValueAsInstant(hardwareId,
                        DeddieConstants.CONFIG_CRP_LAST_FETCHED_AT)
                .map(Instant::toString)
                .orElse(Instant.now().minus(Duration.ofDays(deddieProperties.pastDaysInit())).toString());

        // Prepare search parameters for the DEDDIE API request.
        DeddieCurvesSearchParametersDTO searchParametersDTO = new DeddieCurvesSearchParametersDTO();
        searchParametersDTO.setFromDate(lastFetch)
                .setAnalysisType(DeddieConstants.CURVES_ANALYSIS_TYPE)
                .setClassType(DeddieConstants.CURVES_CLASS_TYPE_CRP)
                .setTaxNumber(taxNumber)
                .setSupplyNumber(supplyNumber)
                .setToDate(Instant.now().toString());

        log.debug("Fetching Curve Reactive Power data for hardwareId: {}, fromDate: {}, toDate: {}",
                hardwareId, searchParametersDTO.getFromDate(), searchParametersDTO.getToDate());
        DeddieCurvesReactivePowerDTO curvesDTO = null;

        // Perform the API call to fetch the data.
        try {
            curvesDTO = deddieClient.getCurvesReactivePower(searchParametersDTO,
                    accessToken,
                    DeddieConstants.API_SCOPE);

            fetchHelperService.resetErrors(hardwareId, DeddieConstants.CONFIG_CRP_ERRORS);
            log.debug("Fetched Curve Reactive Power data: '{}'.", curvesDTO);

        } catch (Exception e) {
            log.warn("Failed to fetch Curve Reactive Power for device '{}'.", hardwareId, e);
            fetchHelperService.increaseErrors(hardwareId, DeddieConstants.CONFIG_CRP_ERRORS);
        }

        // Queue the fetched data for processing/sync.
        int itemsQueued = 0;

        if (curvesDTO != null && curvesDTO.getCurves() != null && !curvesDTO.getCurves().isEmpty()) {
            log.debug("Queuing Curve Reactive Power data:\n{}", deddieELPMapperService.toELP(curvesDTO));
            dataService.queue(
                    QueueItemDTO.builder()
                            .id(UUID.randomUUID().toString())
                            .createdAt(Instant.now())
                            .hardwareId(hardwareId)
                            .dataObject(deddieELPMapperService.toELP(curvesDTO))
                            .build());
            // Update last fetched at, only if data was fetched. This is due to the fact that data might
            // not be available at the time of fetching, however it may become available later on.
            deviceService.updateDeviceConfig(hardwareId,
                    DeddieConstants.CONFIG_CRP_LAST_FETCHED_AT, Instant.now().toString());
            itemsQueued++;
        }

        return itemsQueued;
    }

    /**
     * Fetches Curve Energy Produced data from DEDDIE API.
     *
     * @param hardwareId The hardware ID of the device.
     * @param accessToken The access token required by the DEDDIE API.
     * @param taxNumber The client tax number associated with the device.
     * @param supplyNumber The client supply number associated with the device.
     *
     * @return the number of items queued for processing/sync.
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public int fetchCepData(String hardwareId,
                            String accessToken,
                            String taxNumber,
                            String supplyNumber) {
        // Get last fetch date for the device.
        String lastFetch = deviceService.getDeviceConfigValueAsInstant(hardwareId,
                        DeddieConstants.CONFIG_CEP_LAST_FETCHED_AT)
                .map(Instant::toString)
                .orElse(Instant.now().minus(Duration.ofDays(deddieProperties.pastDaysInit())).toString());

        // Prepare search parameters for the DEDDIE API request.
        DeddieCurvesSearchParametersDTO searchParametersDTO = new DeddieCurvesSearchParametersDTO();
        searchParametersDTO.setFromDate(lastFetch)
                .setAnalysisType(DeddieConstants.CURVES_ANALYSIS_TYPE)
                .setClassType(DeddieConstants.CURVES_CLASS_TYPE_CEP)
                .setTaxNumber(taxNumber)
                .setSupplyNumber(supplyNumber)
                .setToDate(Instant.now().toString());

        log.debug("Fetching Curve Energy Produced data for hardwareId: {}, fromDate: {}, toDate: {}",
                hardwareId, searchParametersDTO.getFromDate(), searchParametersDTO.getToDate());
        DeddieCurvesEnergyProducedDTO curvesDTO = null;

        // Perform the API call to fetch the data.
        try {
            curvesDTO = deddieClient.getCurvesEnergyProduced(searchParametersDTO,
                    accessToken,
                    DeddieConstants.API_SCOPE);
            fetchHelperService.resetErrors(hardwareId, DeddieConstants.CONFIG_CEP_ERRORS);
            log.debug("Fetched Curve Energy Produced data: '{}'.", curvesDTO);
        } catch (Exception e) {
            log.warn("Failed to fetch Curve Energy Produced for device '{}'.", hardwareId, e);
            fetchHelperService.increaseErrors(hardwareId, DeddieConstants.CONFIG_CEP_ERRORS);
        }

        // Queue the fetched data for processing/sync.
        int itemsQueued = 0;

        if (curvesDTO != null && curvesDTO.getCurves() != null && !curvesDTO.getCurves().isEmpty()) {
            log.debug("Queuing Curve Energy Produced data:\n{}", deddieELPMapperService.toELP(curvesDTO));
            dataService.queue(
                    QueueItemDTO.builder()
                            .id(UUID.randomUUID().toString())
                            .createdAt(Instant.now())
                            .hardwareId(hardwareId)
                            .dataObject(deddieELPMapperService.toELP(curvesDTO))
                            .build());

            // Update last fetched at, only if data was fetched. This is due to the fact that data might
            // not be available at the time of fetching, however it may become available later on.
            deviceService.updateDeviceConfig(hardwareId,
                    DeddieConstants.CONFIG_CEP_LAST_FETCHED_AT, Instant.now().toString());
            itemsQueued++;
        }

        return itemsQueued;
    }

    /**
     * Fetches Curve Energy Injected data from DEDDIE API.
     *
     * @param hardwareId The hardware ID of the device.
     * @param accessToken The access token required by the DEDDIE API.
     * @param taxNumber The client tax number associated with the device.
     * @param supplyNumber The client supply number associated with the device.
     *
     * @return the number of items queued for processing/sync.
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public int fetchCeiData(String hardwareId,
                            String accessToken,
                            String taxNumber,
                            String supplyNumber) {
        // Get last fetch date for the device.
        String lastFetch = deviceService.getDeviceConfigValueAsInstant(hardwareId,
                        DeddieConstants.CONFIG_CEI_LAST_FETCHED_AT)
                .map(Instant::toString)
                .orElse(Instant.now().minus(Duration.ofDays(deddieProperties.pastDaysInit())).toString());

        // Prepare search parameters for the DEDDIE API request.
        DeddieCurvesSearchParametersDTO searchParametersDTO = new DeddieCurvesSearchParametersDTO();
        searchParametersDTO.setFromDate(lastFetch)
                .setAnalysisType(DeddieConstants.CURVES_ANALYSIS_TYPE)
                .setClassType(DeddieConstants.CURVES_CLASS_TYPE_CEI)
                .setTaxNumber(taxNumber)
                .setSupplyNumber(supplyNumber)
                .setToDate(Instant.now().toString());

        log.debug("Fetching Curve Energy Injected data for hardwareId: {}, fromDate: {}, toDate: {}",
                hardwareId, searchParametersDTO.getFromDate(), searchParametersDTO.getToDate());
        DeddieCurvesEnergyInjectedDTO curvesDTO = null;

        // Perform the API call to fetch the data.
        try {
            curvesDTO = deddieClient.getCurvesEnergyInjected(searchParametersDTO,
                    accessToken,
                    DeddieConstants.API_SCOPE);

            fetchHelperService.resetErrors(hardwareId, DeddieConstants.CONFIG_CEI_ERRORS);
            log.debug("Fetched Curve Energy Injected data: '{}'.", curvesDTO);

        } catch (Exception e) {
            log.warn("Failed to fetch Curve Energy Injected for device '{}'.", hardwareId, e);
            fetchHelperService.increaseErrors(hardwareId, DeddieConstants.CONFIG_CEI_ERRORS);
        }

        // Queue the fetched data for processing/sync.
        int itemsQueued = 0;

        if (curvesDTO != null && curvesDTO.getCurves() != null && !curvesDTO.getCurves().isEmpty()) {
            log.debug("Queuing Curve Energy Injected data:\n{}", deddieELPMapperService.toELP(curvesDTO));
            dataService.queue(
                    QueueItemDTO.builder()
                            .id(UUID.randomUUID().toString())
                            .createdAt(Instant.now())
                            .hardwareId(hardwareId)
                            .dataObject(deddieELPMapperService.toELP(curvesDTO))
                            .build());

            // Update last fetched at, only if data was fetched. This is due to the fact that data might
            // not be available at the time of fetching, however it may become available later on.
            deviceService.updateDeviceConfig(hardwareId,
                    DeddieConstants.CONFIG_CEI_LAST_FETCHED_AT, Instant.now().toString());
            itemsQueued++;
        }
        return itemsQueued;
    }

}
