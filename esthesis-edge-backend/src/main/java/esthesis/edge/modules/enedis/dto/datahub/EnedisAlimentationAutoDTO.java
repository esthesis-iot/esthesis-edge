package esthesis.edge.modules.enedis.dto.datahub;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * A DTO representing the supply situation (situation d'alimentation) of a usage point, received
 * from the Enedis alimentation_auto ITC API. All fields are optional.
 */
@Data
@ToString
@NoArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
public class EnedisAlimentationAutoDTO {

    @JsonProperty("serial_number")
    private Long serialNumber;

    // Whether the point is supplied, cut, limited, not supplied or not connected.
    @JsonProperty("connection_state")
    private String connectionState;

    @JsonProperty("voltage_level")
    private String voltageLevel;

    @JsonProperty("phase_count")
    private String phaseCount;

    @JsonProperty("consumption_connection_power")
    private Power consumptionConnectionPower;

    @JsonProperty("generation_connection_power")
    private Power generationConnectionPower;

    @JsonProperty("nominal_service_voltage")
    private Power nominalServiceVoltage;

    @Data
    public static class Power {

        private Double value;
        private String unit;
    }
}
