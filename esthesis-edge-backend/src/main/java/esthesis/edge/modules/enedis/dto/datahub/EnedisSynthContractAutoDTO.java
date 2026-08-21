package esthesis.edge.modules.enedis.dto.datahub;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * A DTO representing the contractual summary (synthese contractuelle) of a usage point, received
 * from the Enedis synth_contrat_auto ITC API. All fields are optional.
 */
@Data
@ToString
@NoArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
public class EnedisSynthContractAutoDTO {

    private List<Segment> segments;

    @JsonProperty("generation_last_activation_date")
    private String generationLastActivationDate;

    @JsonProperty("consumption_last_activation_date")
    private String consumptionLastActivationDate;

    @JsonProperty("last_subscribed_power_change_date")
    private String lastSubscribedPowerChangeDate;

    // 0 = non-communicating meter, 1 = communicating Linky meter, 2 = communicating meter open to
    // metering services.
    @JsonProperty("services_level")
    private String servicesLevel;

    @Data
    public static class Segment {

        private String segment;
    }
}
