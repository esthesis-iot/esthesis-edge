package esthesis.edge.modules.enedis.dto.datahub;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
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
    private Boolean soutirage;
    private Boolean injection;
    private Integer page;
    private Boolean comptage;
    private Long autorisationId;
    private Boolean autorisation;
}

