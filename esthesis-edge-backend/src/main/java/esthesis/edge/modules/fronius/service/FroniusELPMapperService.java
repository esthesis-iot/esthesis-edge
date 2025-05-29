package esthesis.edge.modules.fronius.service;

import esthesis.common.avro.ELPEntry;
import esthesis.edge.modules.fronius.config.FroniusProperties;
import esthesis.edge.modules.fronius.dto.FroniusPowerFlowRealtimeDataDTO;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service to map Fronius Solar API DTOs to ELP format.
 */

@ApplicationScoped
@RequiredArgsConstructor
public class FroniusELPMapperService {

    private final FroniusProperties froniusProperties;

    /**
     * Map FroniusPowerFlowRealtimeDataDTO Inverters to ELP format.
     *
     * @param inverter The inverter DTO to map.
     * @return The ELP formatted string.
     */
    public String toELP(FroniusPowerFlowRealtimeDataDTO.Inverter inverter, Instant timestamp) {
        List<ELPEntry> elpEntries = new ArrayList<>();

        elpEntries.add(ELPEntry.builder()
                .category(froniusProperties.fetchTypes().pfr().eday().category())
                .date(timestamp)
                .measurement(froniusProperties.fetchTypes().pfr().eday().measurement(), inverter.getEDay() + "i")
                .build());

        return elpEntries.stream()
                .map(ELPEntry::toString)
                .collect(Collectors.joining("\n"));
    }
}
