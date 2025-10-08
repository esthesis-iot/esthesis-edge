package esthesis.edge.modules.enedis.dto.datahub;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;


/**
 * A DTO representing the subscribed services response received from Enedis.
 */

@Data
@ToString
@NoArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
public class EnedisSubscribedServicesResponseDTO {
    private int nbTotalServices;
    private List<ServiceSouscritDTO> serviceSouscrit;

    @Data
    public static class ServiceSouscritDTO {
        private String id;
        private boolean injection;
        private boolean soutirage;
        private String serviceCode;
        private String dateDebut;
        private String dateFin;
        private String etatCode;
        private String etatLibelle;
        private String pointId;
        private String sirenTitulaire;
        private String sirenBeneficiaire;
        private String mesuresTypeCode;
        private String mesuresPas;
        private String periodiciteTransmission;
        private boolean mesuresCorrigees;
        private boolean espaceDynamique;
        private boolean publicationDonnees;
        private AutorisationDTO autorisation;
    }

    @Data
    public static class AutorisationDTO {
        private long autorisationId;
        private String autorisationLibelle;
        private String autorisationType;
    }
}
