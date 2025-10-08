package esthesis.edge.modules.enedis.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.common.agent.dto.AgentRegistrationRequest;
import esthesis.common.agent.dto.AgentRegistrationResponse;
import esthesis.edge.TestUtils;
import esthesis.edge.clients.EsthesisAgentServiceClient;
import esthesis.edge.model.DeviceEntity;
import esthesis.edge.modules.enedis.client.EnedisClient;
import esthesis.edge.modules.enedis.config.EnedisConstants;
import esthesis.edge.modules.enedis.dto.datahub.EnedisAuthTokenDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisSituationContractAutoDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisSubscribedServicesRequestDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisSubscribedServicesResponseDTO;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

import lombok.SneakyThrows;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class EnedisServiceTest {

    @InjectMock
    @RestClient
    EnedisClient enedisRestClient;

    @Inject
    EnedisService enedisService;

    @InjectMock
    @RestClient
    EsthesisAgentServiceClient esthesisAgentServiceClient;

    @InjectMock
    EnedisFetchService enedisFetchService;

    @Inject
    TestUtils testUtils;

    @Inject
    ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        // Mock the esthesis CORE registration.
        when(esthesisAgentServiceClient.register(any(AgentRegistrationRequest.class)))
                .thenReturn(new AgentRegistrationResponse());
    }

    @Test
    void refreshAuthToken() {
        EnedisAuthTokenDTO dto = new EnedisAuthTokenDTO();
        dto.setAccessToken("test");
        dto.setExpiresOn(1000);
        dto.setScope("test");
        dto.setTokenType("test");

        when(enedisRestClient.getAuthToken(any(String.class), any(String.class), any(String.class)))
                .thenReturn(dto);
        enedisRestClient.getAuthToken("test", "test", "test");
        assertDoesNotThrow(() -> enedisService.refreshAuthToken());
    }

    @SneakyThrows
    @Test
    void createDevice() {
        String situationContractAutoReponse = getSituationContractAutoReponse();
        when(enedisRestClient.getSituationContractAuto(any(String.class), any(String.class)))
                .thenReturn(objectMapper.readValue(situationContractAutoReponse, new TypeReference<>() {}));
        enedisService.createDevice("test");
        assertNotNull(DeviceEntity.findByHardwareId("test"));
    }

    @Test
    void getSelfRegistrationPage() {
        String state = UUID.randomUUID().toString();
        assertTrue(enedisService.getSelfRegistrationPage(state).contains(state));
    }

    @Test
    void getRegistrationSuccessfulPage() {
        assertNotNull(enedisService.getRegistrationSuccessfulPage());
    }

    @Test
    void getErrorPage() {
        assertNotNull(enedisService.getErrorPage());
    }

    @Test
    void countDevices() {
        String hardwareId = UUID.randomUUID().toString();
        testUtils.createDevice(hardwareId);
        assertTrue(enedisService.countDevices() >= 1);
    }

    @Test
    void getFetchErrors() {
        assertNotNull(enedisService.getFetchErrors());
    }

    @Test
    void resetFetchErrors() {
        String hardwareId = UUID.randomUUID().toString();
        testUtils.createDevice(hardwareId);
        assertNotNull(enedisService.resetFetchErrors(hardwareId));
    }

    @Test
    void fetchData() {
        String hardwareId = UUID.randomUUID().toString();
        testUtils.createDevice(hardwareId, EnedisConstants.MODULE_NAME);

        EnedisAuthTokenDTO accessToken = new EnedisAuthTokenDTO();
        accessToken.setAccessToken("test");
        accessToken.setExpiresOn(1000);
        accessToken.setScope("test");
        accessToken.setTokenType("test");
        when(enedisRestClient.getAuthToken(any(String.class), any(String.class), any(String.class)))
                .thenReturn(accessToken);
        testUtils.setDeviceConfig(hardwareId, EnedisConstants.CONFIG_PRM, "test");
        when(enedisFetchService.fetchDailyConsumption(any(String.class), any(String.class),
                any(String.class))).thenReturn(0);
        when(enedisFetchService.fetchDailyConsumptionMaxPower(any(String.class), any(String.class),
                any(String.class))).thenReturn(0);
        when(enedisFetchService.fetchDailyProduction(any(String.class), any(String.class),
                any(String.class))).thenReturn(0);

        assertDoesNotThrow(() -> enedisService.fetchData());
    }

    @Test
    void fetchUsagePointId() {
        Long authorizationId = 123456L;

        EnedisSubscribedServicesResponseDTO responseDTO = new EnedisSubscribedServicesResponseDTO();
        EnedisSubscribedServicesResponseDTO.ServiceSouscritDTO serviceSouscrit = new EnedisSubscribedServicesResponseDTO.ServiceSouscritDTO();
        serviceSouscrit.setPointId("99999999999999");
        responseDTO.setServiceSouscrit(List.of(serviceSouscrit));

        when(enedisRestClient.getSubscribedServices(any(EnedisSubscribedServicesRequestDTO.class), any(String.class)))
                .thenReturn(responseDTO);

        assertEquals("99999999999999", enedisService.fetchUsagePointId(authorizationId));
    }

    private String getSituationContractAutoReponse() {
        return """
                [
                  {
                    "usage_point_id": "3232947408",
                    "contract_start": "2021-01-01T00:00:00+0100",
                    "contract_type": "Contrat CARD-I",
                    "contractor": "RENNES METROPOLE",
                    "balance_responsable_party": "ACM_010",
                    "pricing_structure": "Nouvelle Offre",
                    "distribution_tariff": "Tarif HTA Courte Utilisation",
                    "supplier_tariff_profile": [
                      {
                        "name": "Heures Creuses Hiver/Saison Haute",
                        "power": {
                          "unit": "kW",
                          "value": "30"
                        }
                      }
                    ],
                    "distribution_tariff_profile": [
                      {
                        "name": "Heures Creuses Hiver/Saison Haute",
                        "power": {
                          "unit": "kW",
                          "value": "30"
                        }
                      }
                    ],
                    "supplier_mobile_peak": "string",
                    "distribution_mobile_peak": "string",
                    "subscribed_power": {
                      "unit": "kW",
                      "value": "30"
                    },
                    "segment": "C5",
                    "customer": {
                      "customer": {
                        "adress": {
                          "line1": "string",
                          "line2": "string",
                          "line3": "string",
                          "line4": "4 RUE HENRI FREVILLE",
                          "line5": "CS 20723",
                          "line6": "35207 RENNES",
                          "line7": "FR (France)"
                        },
                        "contact_data": {
                          "email": "m.belay@rennesmetropole.fr",
                          "landline": "624136309",
                          "phone": "223621058"
                        },
                        "person": {
                          "title": "M",
                          "lastname": "BELAY",
                          "firstname": "Martin"
                        },
                        "organization": {
                          "name": "RENNES METROPOLE",
                          "commercial_name": "string",
                          "business_code": "string",
                          "siret_number": "24350013900262",
                          "siren_number": "243500139"
                        }
                      }
                    }
                  }
                ]
                """;
    }

}
