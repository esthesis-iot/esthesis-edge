package esthesis.edge.modules.enedis.dto.datahub;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * A DTO representing the subscribed services request to be sent to Enedis.
 */

@Data
@ToString
@NoArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
public class EnedisSubscribedServicesRequestDTO {
    private List<String> pointId;
    private List<String> siren;
    private String dateDebut;
    private String dateFin;
    private List<String> etatCode;
    private String serviceType;
    private List<String> mesureTypeCode;
    private boolean soutirage;
    private boolean injection;
    private int page;
    private boolean comptage;
    private long autorisationId;
    private boolean autorisation;
}

