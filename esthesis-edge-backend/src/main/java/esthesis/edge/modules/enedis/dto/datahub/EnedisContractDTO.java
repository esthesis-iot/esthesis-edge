package esthesis.edge.modules.enedis.dto.datahub;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * A DTO representing the contract information received from Enedis.
 * @deprecated This class is deprecated and will be removed in future versions.
 * Use the updated contract DTOs instead.
 */

@Data
@ToString
@NoArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
@Deprecated(since = "2026-01", forRemoval = true)
public class EnedisContractDTO {

    private Customer customer;

    @Data
    public static class Customer {
        @JsonProperty("customer_id")
        private String customerId;
        @JsonProperty("usage_points")
        private UsagePointWrapper[] usagePoints;
    }

    @Data
    public static class UsagePointWrapper {
        @JsonProperty("usage_point")
        private UsagePoint usagePoint;
        private Contracts contracts;
    }

    @Data
    public static class UsagePoint {
        @JsonProperty("usage_point_id")
        private String usagePointId;

        @JsonProperty("usage_point_status")
        private String usagePointStatus;

        @JsonProperty("meter_type")
        private String meterType;
    }

    @Data
    public static class Contracts {
        private String segment;

        @JsonProperty("subscribed_power")
        private String subscribedPower;

        @JsonProperty("last_activation_date")
        private String lastActivationDate;

        @JsonProperty("distribution_tariff")
        private String distributionTariff;

        @JsonProperty("offpeak_hours")
        private String offpeakHours;

        @JsonProperty("contract_type")
        private String contractType;

        @JsonProperty("contract_status")
        private String contractStatus;

        @JsonProperty("last_distribution_tariff_change_date")
        private String lastDistributionTariffChangeDate;
    }
}
