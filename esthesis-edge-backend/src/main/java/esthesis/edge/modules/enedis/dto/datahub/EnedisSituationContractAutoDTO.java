package esthesis.edge.modules.enedis.dto.datahub;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@ToString
@NoArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
public class EnedisSituationContractAutoDTO {
    @JsonProperty("usage_point_id")
    private String usagePointId;

    @JsonProperty("contract_start")
    private String contractStart;

    @JsonProperty("contract_type")
    private String contractType;

    private String contractor;

    @JsonProperty("balance_responsable_party")
    private String balanceResponsableParty;

    @JsonProperty("pricing_structure")
    private String pricingStructure;

    @JsonProperty("distribution_tariff")
    private String distributionTariff;

    @JsonProperty("supplier_tariff_profile")
    private List<TariffProfile> supplierTariffProfile;

    @JsonProperty("distribution_tariff_profile")
    private List<TariffProfile> distributionTariffProfile;

    @JsonProperty("supplier_mobile_peak")
    private String supplierMobilePeak;

    @JsonProperty("distribution_mobile_peak")
    private String distributionMobilePeak;

    @JsonProperty("subscribed_power")
    private Power subscribedPower;

    private String segment;

    private CustomerWrapper customer;

    @Data
    public static class TariffProfile {
        private String name;
        private Power power;
    }

    @Data
    public static class Power {
        private String unit;
        private String value;
    }

    @Data
    public static class CustomerWrapper {
        private Customer customer;
    }

    @Data
    public static class Customer {
        private Address adress;
        @JsonProperty("contact_data")
        private ContactData contactData;
        private Person person;
        private Organization organization;
    }

    @Data
    public static class Address {
        private String line1;
        private String line2;
        private String line3;
        private String line4;
        private String line5;
        private String line6;
        private String line7;
    }

    @Data
    public static class ContactData {
        private String email;
        private String landline;
        private String phone;
    }

    @Data
    public static class Person {
        private String title;
        private String lastname;
        private String firstname;
    }

    @Data
    public static class Organization {
        private String name;
        @JsonProperty("commercial_name")
        private String commercialName;
        @JsonProperty("business_code")
        private String businessCode;
        @JsonProperty("siret_number")
        private String siretNumber;
        @JsonProperty("siren_number")
        private String sirenNumber;
    }
}

