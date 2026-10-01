package esthesis.edge.modules.enedis.dto.datahub;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * A DTO representing the response of any of the Enedis mesure_synchrone_auto v2 sub-resources
 * (daily consumption, daily production, daily maximum power, consumption load curve, production
 * load curve), which all share the same shape. Every value is received as a string; fields not
 * applicable to a given sub-resource (e.g. typeValeur and pas on load curves) are null.
 */
@Data
@NoArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnedisMesureDTO {

  private String idPrm;
  private String etapeMetier;
  private Periode periode;
  private String typeValeur;
  private String modeCalcul;
  private String pas;
  private List<Grandeur> grandeur;

  @Data
  @NoArgsConstructor
  @RegisterForReflection
  @Accessors(chain = true)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Periode {

    private String dateDebut;
    private String dateFin;
  }

  @Data
  @NoArgsConstructor
  @RegisterForReflection
  @Accessors(chain = true)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Grandeur {

    private String grandeurMetier;
    private String grandeurPhysique;
    private String unite;
    private List<Point> points;
  }

  @Data
  @NoArgsConstructor
  @RegisterForReflection
  @Accessors(chain = true)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Point {

    // Value.
    private String v;
    // Date, either YYYY-MM-DD or "YYYY-MM-DD HH:mm:ss".
    private String d;
    // Load curve step (e.g. PT30M).
    private String p;
    // Load curve nature.
    private String n;
    // Load curve validity indicator.
    private String iv;
    // Load curve calculation-completeness indicator.
    private String ec;
  }
}
