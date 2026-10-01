package esthesis.edge.modules.enedis.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.edge.modules.enedis.dto.datahub.EnedisMesureDTO;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

@QuarkusTest
class EnedisELPMapperServiceTest {

  @Inject
  EnedisELPMapperService enedisELPMapperService;

  @Inject
  ObjectMapper objectMapper;

  private EnedisMesureDTO read(String fixture) throws IOException {
    try (InputStream is = getClass().getResourceAsStream("/wiremock/enedis/__files/" + fixture)) {
      assertNotNull(is, "Missing fixture " + fixture);
      return objectMapper.readValue(is, EnedisMesureDTO.class);
    }
  }

  @Test
  void toElpDailyConsumption() throws IOException {
    EnedisMesureDTO dto = read("mesure-v2-consommation-quotidienne.json");

    String[] lines = enedisELPMapperService.toELP(dto, "energy", "dc").split("\n");
    assertEquals(5, lines.length);
    assertEquals("energy dc=10000i 2026-09-25T23:59:59Z", lines[0]);
  }

  @Test
  void toElpLoadCurveUsesPointTimestamp() throws IOException {
    EnedisMesureDTO dto = read("mesure-v2-cdc-consommation.json");

    String[] lines = enedisELPMapperService.toELP(dto, "energy", "clc").split("\n");
    assertEquals(3, lines.length);
    assertEquals("energy clc=120i 2026-09-28T00:30:00Z", lines[0]);
  }

  @Test
  void toElpSkipsUnusablePoints() throws IOException {
    EnedisMesureDTO dto = read("mesure-v2-pmax-pma.json");
    // A fixture point with an unparseable date; the "null" value point is already in the fixture.
    List<EnedisMesureDTO.Point> points =
        new ArrayList<>(dto.getGrandeur().get(0).getPoints());
    points.add(new EnedisMesureDTO.Point().setV("3300").setD("20XX-XX"));
    dto.getGrandeur().get(0).setPoints(points);

    String[] lines = enedisELPMapperService.toELP(dto, "energy", "dcmp").split("\n");
    assertEquals(4, lines.length);
    assertEquals("energy dcmp=3000i 2026-09-25T08:44:22Z", lines[0]);
  }

  @Test
  void toElpToutUsesPerPhaseFields() throws IOException {
    EnedisMesureDTO dto = read("mesure-v2-pmax-tout.json");

    String[] lines = enedisELPMapperService.toELP(dto, "energy", "dcmp").split("\n");
    assertEquals(8, lines.length);
    assertEquals(2, Arrays.stream(lines).filter(l -> l.startsWith("energy dcmp=")).count());
    assertEquals(2, Arrays.stream(lines).filter(l -> l.startsWith("energy dcmp_pma1=")).count());
    assertEquals(2, Arrays.stream(lines).filter(l -> l.startsWith("energy dcmp_pma2=")).count());
    assertEquals(2, Arrays.stream(lines).filter(l -> l.startsWith("energy dcmp_pma3=")).count());
  }

  @Test
  void toElpEmptyGrandeurIsEmptyString() {
    assertEquals("", enedisELPMapperService.toELP(
        new EnedisMesureDTO().setGrandeur(List.of()), "energy", "dc"));
    assertEquals("", enedisELPMapperService.toELP(new EnedisMesureDTO(), "energy", "dc"));
    assertTrue(enedisELPMapperService.toELP(
        new EnedisMesureDTO().setGrandeur(List.of(new EnedisMesureDTO.Grandeur())),
        "energy", "dc").isEmpty());
  }
}
