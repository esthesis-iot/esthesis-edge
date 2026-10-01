package esthesis.edge.modules.enedis.service;

import esthesis.common.avro.ELPEntry;
import esthesis.edge.modules.enedis.EnedisUtil;
import esthesis.edge.modules.enedis.dto.datahub.EnedisMesureDTO;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service to map Enedis DTOs to ELP format.
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class EnedisELPMapperService {

  // Per-phase maximum power grandeurs (PMA1, PMA2, PMA3) returned for grandeurPhysique=TOUT.
  private static final Pattern PER_PHASE_PMA = Pattern.compile("PMA\\d");

  /**
   * Map an Enedis mesure_synchrone_auto v2 response to ELP format. All five sub-resources
   * share the same response shape, so a single mapper serves them all.
   * <p>
   * Each grandeur maps to the given measurement, except per-phase grandeurs (grandeurPhysique
   * matching PMA followed by a digit) which map to measurement_pmaN. Points with a missing,
   * literal "null" or unparseable value/date are skipped (the Enedis sandbox returns such
   * points), logging a single warning per response.
   *
   * @param dto         The DTO to map.
   * @param category    The ELP category.
   * @param measurement The ELP measurement (field) name.
   * @return The ELP formatted string, or an empty string when there are no usable points.
   */
  public String toELP(EnedisMesureDTO dto, String category, String measurement) {
    if (dto == null || dto.getGrandeur() == null) {
      return "";
    }

    List<String> lines = new ArrayList<>();
    int skipped = 0;
    for (EnedisMesureDTO.Grandeur grandeur : dto.getGrandeur()) {
      if (grandeur == null || grandeur.getPoints() == null) {
        continue;
      }
      String field = fieldName(grandeur, measurement);
      for (EnedisMesureDTO.Point point : grandeur.getPoints()) {
        Instant date = point != null && isUsable(point.getV()) && isUsable(point.getD())
            ? parseDate(point.getD()) : null;
        if (date == null) {
          skipped++;
          continue;
        }
        lines.add(ELPEntry.builder()
            .category(category)
            .date(date)
            .measurement(field, point.getV() + "i")
            .build().toString());
      }
    }
    if (skipped > 0) {
      log.warn("Skipped {} Enedis mesure point(s) with a missing, null or unparseable "
          + "value/date for measurement '{}'.", skipped, measurement);
    }

    return String.join("\n", lines);
  }

  private static boolean isUsable(String value) {
    return value != null && !value.isBlank() && !"null".equals(value);
  }

  private static String fieldName(EnedisMesureDTO.Grandeur grandeur, String measurement) {
    String physique = grandeur.getGrandeurPhysique();
    if (physique != null && PER_PHASE_PMA.matcher(physique).matches()) {
      return measurement + "_" + physique.toLowerCase();
    }
    return measurement;
  }

  private static Instant parseDate(String date) {
    try {
      return EnedisUtil.mesureDateToInstant(date);
    } catch (DateTimeParseException e) {
      return null;
    }
  }
}
