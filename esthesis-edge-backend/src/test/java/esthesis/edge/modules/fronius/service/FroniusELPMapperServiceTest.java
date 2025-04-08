package esthesis.edge.modules.fronius.service;

import esthesis.edge.modules.fronius.FroniusTestUtils;
import esthesis.edge.modules.fronius.dto.FroniusPowerFlowRealtimeDataDTO;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@QuarkusTest
class FroniusELPMapperServiceTest {

    @Inject
    FroniusELPMapperService froniusELPMapperService;

    @Inject
    FroniusTestUtils froniusTestUtils;

    @Test
    void toELP() {
        FroniusPowerFlowRealtimeDataDTO.Inverter inverter =
                froniusTestUtils.createFroniusPowerFlowRealtimeDataDTO()
                        .getBody()
                        .getData()
                        .getInverters()
                        .values()
                        .stream()
                        .findFirst()
                        .orElseThrow();

        assertNotNull(froniusELPMapperService.toELP(inverter, Instant.now()));

    }
}