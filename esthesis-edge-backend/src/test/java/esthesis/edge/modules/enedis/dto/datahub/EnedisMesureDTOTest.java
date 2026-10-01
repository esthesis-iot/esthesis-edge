package esthesis.edge.modules.enedis.dto.datahub;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

/**
 * The five mesure_synchrone_auto v2 sub-resources share one response shape.
 */
class EnedisMesureDTOTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  private EnedisMesureDTO read(String fixture) throws IOException {
    try (InputStream is = getClass().getResourceAsStream("/wiremock/enedis/__files/" + fixture)) {
      assertNotNull(is, "Missing fixture " + fixture);
      return objectMapper.readValue(is, EnedisMesureDTO.class);
    }
  }

  @Test
  void deserializesDailyConsumption() throws IOException {
    EnedisMesureDTO dto = read("mesure-v2-consommation-quotidienne.json");

    assertEquals("99999999999999", dto.getIdPrm());
    assertEquals("BRUT", dto.getEtapeMetier());
    assertEquals("2026-09-25", dto.getPeriode().getDateDebut());
    assertEquals("GLOBALE", dto.getTypeValeur());
    assertEquals("DIFF.INDEX", dto.getModeCalcul());
    assertEquals("P1D", dto.getPas());
    assertEquals(1, dto.getGrandeur().size());
    assertEquals("CONS", dto.getGrandeur().get(0).getGrandeurMetier());
    assertEquals("EA", dto.getGrandeur().get(0).getGrandeurPhysique());
    assertEquals("Wh", dto.getGrandeur().get(0).getUnite());
    assertEquals(5, dto.getGrandeur().get(0).getPoints().size());
    assertEquals("2026-09-25", dto.getGrandeur().get(0).getPoints().get(0).getD());
  }

  @Test
  void deserializesDailyProduction() throws IOException {
    EnedisMesureDTO dto = read("mesure-v2-production-quotidienne.json");

    assertEquals("PROD", dto.getGrandeur().get(0).getGrandeurMetier());
    assertEquals(5, dto.getGrandeur().get(0).getPoints().size());
  }

  @Test
  void deserializesPmaxPma() throws IOException {
    EnedisMesureDTO dto = read("mesure-v2-pmax-pma.json");

    assertEquals(1, dto.getGrandeur().size());
    assertEquals("PMA", dto.getGrandeur().get(0).getGrandeurPhysique());
    assertEquals(5, dto.getGrandeur().get(0).getPoints().size());
    assertEquals("null", dto.getGrandeur().get(0).getPoints().get(2).getV());
  }

  @Test
  void deserializesPmaxTout() throws IOException {
    EnedisMesureDTO dto = read("mesure-v2-pmax-tout.json");

    assertEquals(4, dto.getGrandeur().size());
    assertEquals("PMA3", dto.getGrandeur().get(3).getGrandeurPhysique());
  }

  @Test
  void deserializesLoadCurve() throws IOException {
    EnedisMesureDTO dto = read("mesure-v2-cdc-consommation.json");

    assertEquals("PA", dto.getGrandeur().get(0).getGrandeurPhysique());
    assertEquals(3, dto.getGrandeur().get(0).getPoints().size());
    EnedisMesureDTO.Point first = dto.getGrandeur().get(0).getPoints().get(0);
    assertEquals("2026-09-28 00:30:00", first.getD());
    assertEquals("PT30M", first.getP());
    assertEquals("B", first.getN());
    assertEquals("0", first.getIv());
    assertEquals("0", first.getEc());

    assertEquals("PROD", read("mesure-v2-cdc-production.json").getGrandeur().get(0).getGrandeurMetier());
  }

  @Test
  void ignoresUnknownFields() throws IOException {
    EnedisMesureDTO dto = objectMapper.readValue("""
        {
          "idPrm": "99999999999999",
          "unknownTopLevel": "x",
          "grandeur": [
            {
              "grandeurMetier": "CONS",
              "points": [ { "v": "1", "d": "2026-09-25", "unknownPoint": "y" } ],
              "calendrier": [ { "anything": 1 } ]
            }
          ],
          "contexte": [ { "anything": 2 } ]
        }
        """, EnedisMesureDTO.class);

    assertEquals("99999999999999", dto.getIdPrm());
    assertEquals("1", dto.getGrandeur().get(0).getPoints().get(0).getV());
  }
}
