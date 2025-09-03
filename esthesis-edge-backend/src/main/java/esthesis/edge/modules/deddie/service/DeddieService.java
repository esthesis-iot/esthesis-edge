package esthesis.edge.modules.deddie.service;

import esthesis.common.exception.QProcessingException;
import esthesis.edge.dto.DeviceDTO;
import esthesis.edge.dto.TemplateDTO;
import esthesis.edge.modules.deddie.client.DeddieClient;
import esthesis.edge.modules.deddie.config.DeddieConstants;
import esthesis.edge.modules.deddie.config.DeddieProperties;
import esthesis.edge.modules.deddie.templates.DeddieTemplates;
import esthesis.edge.services.DeviceService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;


@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class DeddieService {

    @Inject
    @RestClient
    DeddieClient deddieClient;

    private final DeddieProperties deddieProperties;
    private final DeviceService deviceService;
    private final DeddieFetchService deddieFetchService;


    /**
     * Generate a unique hardware ID for the device based on the tax number and supply number.
     *
     * @param taxNumber    The tax number associated with the device.
     * @param supplyNumber The supply number associated with the device.
     * @return A unique hardware ID string.
     */
    private String generateHardwareId(String taxNumber, String supplyNumber) {
        return "DEDDIE-" + taxNumber + "-" + supplyNumber;
    }

    /**
     * Create devices for the given tax number and supply numbers.
     *
     * @param accessToken           The access token created in the Deddie's portal to be associated with the devices.
     * @param taxNumber             The tax number associated with the devices.
     * @param selectedSupplyNumbers A list of supply numbers to filter the devices. If null or empty, all supply numbers will be used.
     */
    @Transactional
    public void createDevices(String accessToken, String taxNumber, List<String> selectedSupplyNumbers) {
        // Retrieve all supply numbers for the given tax number.
        List<String> supplyNumbers;
        try {
            supplyNumbers = deddieClient.retrieveSuppliesList(taxNumber,
                            accessToken,
                            DeddieConstants.API_SCOPE)
                    .stream()
                    .map(result -> result.split(" ")[0].trim())
                    .toList();

        } catch (Exception e) {
            throw new QProcessingException("Failed to retrieve supply numbers for the given token {} and tax number '{}'",
                    accessToken, taxNumber, e);
        }

        // If the user has selected specific supply numbers, filter the list.
        if (selectedSupplyNumbers != null && !selectedSupplyNumbers.isEmpty()) {

            // Ensure no leading/trailing spaces in the selected supply numbers.
            selectedSupplyNumbers = selectedSupplyNumbers.stream()
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .toList();

            // Filter the supply numbers based on the selected ones.
            if(!selectedSupplyNumbers.isEmpty()) {
                supplyNumbers = supplyNumbers.stream()
                        .filter(selectedSupplyNumbers::contains)
                        .toList();
            }

        }

        if (supplyNumbers.isEmpty()) {
            throw new QProcessingException("No valid supply numbers found for the given tax number '{}' and for the provided supply numbers '{}'"
                    , taxNumber, selectedSupplyNumbers);
        }

        // Create a device for each supply number.
        supplyNumbers.forEach(supplyNumber -> {
            String hardwareId = generateHardwareId(taxNumber, supplyNumber);

            DeviceDTO deviceDTO = DeviceDTO.builder()
                    .hardwareId(hardwareId)
                    .enabled(true)
                    .moduleName(DeddieConstants.MODULE_NAME)
                    .config(DeddieConstants.CONFIG_SUPPLY_NUMBER, supplyNumber)
                    .config(DeddieConstants.CONFIG_ACCESS_TOKEN, accessToken)
                    .config(DeddieConstants.CONFIG_TAX_NUMBER, taxNumber)
                    .build();

            deviceService.createDevice(deviceDTO);
        });


    }

    /**
     * Fetch new data from DEDDIE API.
     */
    @Scheduled(cron = "{esthesis.edge.modules.deddie.cron}")
    public void fetchData() {
        if (!deddieProperties.enabled()) {
            return;
        }

        log.debug("Fetching data from DEDDIE...");

        // Get all Deddie devices.
        List<DeviceDTO> devices = deviceService.listActiveDevices(DeddieConstants.MODULE_NAME);
        log.debug("Found '{}' active devices to fetch data for.", devices.size());
        if (devices.isEmpty()) {
            return;
        }

        for (DeviceDTO device : devices) {
            // Get device access token.
            String accessToken = deviceService.getDeviceConfigValueAsString(device.getHardwareId(),
                            DeddieConstants.CONFIG_ACCESS_TOKEN)
                    .orElseThrow(() -> new QProcessingException("Device '{}' does not have an access token configured.",
                            device.getHardwareId()));

            // Get device tax number.
            String taxNumber = deviceService.getDeviceConfigValueAsString(device.getHardwareId(),
                            DeddieConstants.CONFIG_TAX_NUMBER)
                    .orElseThrow(() -> new QProcessingException("Device '{}' does not have a tax number configured.",
                            device.getHardwareId()));

            // Get device supply number.
            String supplyNumber = deviceService.getDeviceConfigValueAsString(device.getHardwareId(),
                            DeddieConstants.CONFIG_SUPPLY_NUMBER)
                    .orElseThrow(() -> new QProcessingException("Device '{}' does not have a supply number configured.",
                            device.getHardwareId()));

            // Fetch Curve Active Consumption Data.
            int cacErrors = deviceService.getDeviceConfigValueAsString(device.getHardwareId(),
                    DeddieConstants.CONFIG_CAC_ERRORS).map(Integer::parseInt).orElse(0);

            if (deddieProperties.fetchTypes().cac().enabled() &&
                    cacErrors < deddieProperties.fetchTypes().cac().errorsThreshold()) {
                int queuedItems = deddieFetchService.fetchCacData(device.getHardwareId(),
                        accessToken,
                        taxNumber,
                        supplyNumber);
                log.debug("Queued {} items from  curve active consumption API ", queuedItems);
            }

            // Fetch Curve Reactive Power Data.
            int crpErrors = deviceService.getDeviceConfigValueAsString(device.getHardwareId(),
                    DeddieConstants.CONFIG_CRP_ERRORS).map(Integer::parseInt).orElse(0);
            if (deddieProperties.fetchTypes().crp().enabled() &&
                    crpErrors < deddieProperties.fetchTypes().crp().errorsThreshold()) {
                int queuedItems = deddieFetchService.fetchCrpData(device.getHardwareId(),
                        accessToken,
                        taxNumber,
                        supplyNumber);
                log.debug("Queued {} items from  curve reactive power API ", queuedItems);
            }

            // Fetch Curve Energy Produced Data.
            int cepErrors = deviceService.getDeviceConfigValueAsString(device.getHardwareId(),
                    DeddieConstants.CONFIG_CEP_ERRORS).map(Integer::parseInt).orElse(0);
            if (deddieProperties.fetchTypes().cep().enabled() &&
                    cepErrors < deddieProperties.fetchTypes().cep().errorsThreshold()) {
                int queuedItems = deddieFetchService.fetchCepData(device.getHardwareId(),
                        accessToken,
                        taxNumber,
                        supplyNumber);
                log.debug("Queued {} items from  curve energy produced API ", queuedItems);
            }

            // Fetch Curve energy Injected Data.
            int ceiErrors = deviceService.getDeviceConfigValueAsString(device.getHardwareId(),
                    DeddieConstants.CONFIG_CEI_ERRORS).map(Integer::parseInt).orElse(0);
            if (deddieProperties.fetchTypes().cei().enabled() &&
                    ceiErrors < deddieProperties.fetchTypes().cei().errorsThreshold()) {
                int queuedItems = deddieFetchService.fetchCeiData(device.getHardwareId(),
                        accessToken,
                        taxNumber,
                        supplyNumber);
                log.debug("Queued {} items from  curve energy injected API ", queuedItems);
            }

        }
    }

    /**
     * Count the number of devices created by the Deddie module.
     *
     * @return The number of devices.
     */
    public long countDevices() {
        return deviceService.countDevices(DeddieConstants.MODULE_NAME);
    }

    /**
     * Get the self-registration page for Deddie.
     *
     * @return The rendered self-registration page HTML string.
     */
    public String getSelfRegistrationPage() {
        return new TemplateDTO(DeddieTemplates.SELF_REGISTRATION)
                .data("title", deddieProperties.selfRegistration().page().registration().title())
                .data("message", deddieProperties.selfRegistration().page().registration().message())
                .data("logo1", deddieProperties.selfRegistration().page().logo1Url().orElse(""))
                .data("logo1Alt", deddieProperties.selfRegistration().page().logo1Alt().orElse(""))
                .data("logo2", deddieProperties.selfRegistration().page().logo2Url().orElse(""))
                .data("logo2Alt", deddieProperties.selfRegistration().page().logo2Alt().orElse(""))
                .data("logo3", deddieProperties.selfRegistration().page().logo3Url().orElse(""))
                .data("logo3Alt", deddieProperties.selfRegistration().page().logo3Alt().orElse(""))
                .data("placeholderTaxNumber", deddieProperties.selfRegistration().page().registration().placeholderTaxNumber())
                .data("placeholderAccessToken", deddieProperties.selfRegistration().page().registration().placeholderAccessToken())
                .data("placeholderSupplyNumber", deddieProperties.selfRegistration().page().registration().placeholderSupplyNumber())
                .render();
    }

    /* * Get the registration error page for Deddie.
     *
     * @param message The error message to display. If null or empty, a default message is used.
     * @return The rendered registration error page HTML string.
     */
    public String getRegistrationErrorPage(String message) {
        if (StringUtils.isBlank(message)) {
            message = deddieProperties.selfRegistration().page().error().message();
        }
        return new TemplateDTO(DeddieTemplates.REGISTRATION_ERROR)
                .data("title", deddieProperties.selfRegistration().page().error().title())
                .data("message", message)
                .data("logo1", deddieProperties.selfRegistration().page().logo1Url().orElse(""))
                .data("logo1Alt", deddieProperties.selfRegistration().page().logo1Alt().orElse(""))
                .data("logo2", deddieProperties.selfRegistration().page().logo2Url().orElse(""))
                .data("logo2Alt", deddieProperties.selfRegistration().page().logo2Alt().orElse(""))
                .data("logo3", deddieProperties.selfRegistration().page().logo3Url().orElse(""))
                .data("logo3Alt", deddieProperties.selfRegistration().page().logo3Alt().orElse(""))
                .render();
    }

    /**
     * Get the registration successful page for Deddie.
     *
     * @return The rendered registration successful page HTML string.
     */
    public String getRegistrationSuccessfulPage() {
        return new TemplateDTO(DeddieTemplates.REGISTRATION_SUCCESS)
                .data("title", deddieProperties.selfRegistration().page().success().title())
                .data("message", deddieProperties.selfRegistration().page().success().message())
                .data("logo1", deddieProperties.selfRegistration().page().logo1Url().orElse(""))
                .data("logo1Alt", deddieProperties.selfRegistration().page().logo1Alt().orElse(""))
                .data("logo2", deddieProperties.selfRegistration().page().logo2Url().orElse(""))
                .data("logo2Alt", deddieProperties.selfRegistration().page().logo2Alt().orElse(""))
                .data("logo3", deddieProperties.selfRegistration().page().logo3Url().orElse(""))
                .data("logo3Alt", deddieProperties.selfRegistration().page().logo3Alt().orElse(""))
                .render();
    }
}
