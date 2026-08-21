package esthesis.edge.modules.enedis.dto.datahub;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * A DTO representing the general data (non-normalised installation address of the PRM) received
 * from the Enedis donnees_generales_auto ITC API. All fields are optional.
 */
@Data
@ToString
@NoArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
public class EnedisDonneesGeneralesAutoDTO {

    private Address address;

    @Data
    public static class Address {

        @JsonProperty("staircase_floor_apartment")
        private String staircaseFloorApartment;

        private String building;

        @JsonProperty("number_street_name")
        private String numberStreetName;

        private String locality;

        @JsonProperty("postal_code_city")
        private String postalCodeCity;

        @JsonProperty("insee_code")
        private String inseeCode;
    }
}
