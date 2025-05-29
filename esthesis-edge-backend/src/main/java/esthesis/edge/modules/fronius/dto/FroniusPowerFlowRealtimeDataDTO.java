package esthesis.edge.modules.fronius.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * A DTO representing the real-time power flow data received from Fronius.
 */

@Data
@ToString
@NoArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
public class FroniusPowerFlowRealtimeDataDTO {

    @JsonProperty("Body")
    private Body body;

    @JsonProperty("Head")
    private Head head;

    @Data
    public static class Body {
        @JsonProperty("Data")
        private BodyData data;
    }

    @Data
    public static class Head {
        @JsonProperty("Status")
        private Status status;

        @JsonProperty("Timestamp")
        private String timeStamp;
    }

    @Data
    public static class BodyData {
        @JsonProperty("Inverters")
        private Map<String, Inverter> inverters;

        @JsonProperty("Site")
        private Site site;

        @JsonProperty("Version")
        private String version;
    }

    @Data
    public static class Site {
        @JsonProperty("Mode")
        private String mode;

        @JsonProperty("E_Day")
        private String eDay;

        @JsonProperty("E_Year")
        private String eYear;

        @JsonProperty("E_Total")
        private String eTotal;

        @JsonProperty("Meter_Location")
        private String meterLocation;

        @JsonProperty("P_Akku")
        private String pAkku;

        @JsonProperty("P_Grid")
        private String pGrid;

        @JsonProperty("P_Load")
        private String pLoad;

        @JsonProperty("P_PV")
        private String pPv;

        @JsonProperty("rel_Autonomy")
        private String relAutonomy;

        @JsonProperty("rel_SelfConsumption")
        private String relSelfConsumption;

    }

    @Data
    public static class Inverter {

        @JsonProperty("DT")
        private String dt;

        @JsonProperty("E_Day")
        private String eDay;

        @JsonProperty("E_Year")
        private String eYear;

        @JsonProperty("E_Total")
        private String eTotal;

        @JsonProperty("P")
        private String p;

    }

    @Data
    public static class Status {
        @JsonProperty("Code")
        private String code;

        @JsonProperty("Reason")
        private String reason;

        @JsonProperty("UserMessage")
        private String userMessage;
    }
}
