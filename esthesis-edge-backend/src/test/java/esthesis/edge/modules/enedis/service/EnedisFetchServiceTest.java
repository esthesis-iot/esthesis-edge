package esthesis.edge.modules.enedis.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.edge.TestUtils;
import esthesis.edge.modules.enedis.client.EnedisClient;
import esthesis.edge.modules.enedis.dto.datahub.EnedisConsumptionLoadCurveDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisDailyConsumptionDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisDailyConsumptionMaxPowerDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisDailyProductionDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisProductionLoadCurveDTO;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.io.IOException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.wildfly.common.Assert;

@QuarkusTest
class EnedisFetchServiceTest {

  @Inject
  TestUtils testUtils;

  @Inject
  EnedisFetchService enedisFetchService;

  @InjectMock
  @RestClient
  EnedisClient enedisRestClient;

  @Inject
  ObjectMapper objectMapper;

  @Test
  void fetchDailyConsumption() throws IOException {
    String hardwareId = "test1";
    testUtils.createDevice(hardwareId);

    EnedisDailyConsumptionDTO dto = objectMapper.readValue("""
        {
          "meter_reading": {
            "usage_point_id": "16401220101758",
            "start": "2019-05-06",
            "end": "2019-05-12",
            "quality": "BRUT",
            "reading_type": {
              "measurement_kind": "energy",
              "measuring_period": "P1D",
              "unit": "Wh",
              "aggregate": "sum"
            },
            "interval_reading": [
              {
                "value": "540",
                "date": "2019-05-06"
              }
            ]
          }
        }""", EnedisDailyConsumptionDTO.class);
    when(enedisRestClient.getDailyConsumption(any(String.class), any(String.class),
        any(String.class), any(String.class)))
        .thenReturn(dto);
    Assert.assertTrue(enedisFetchService.fetchDailyConsumption(hardwareId, "test", "test") == 1);
  }

  @Test
  void fetchDailyConsumptionMaxPower() throws JsonProcessingException {
    String hardwareId = "test2";
    testUtils.createDevice(hardwareId);

    // Note, ENEDIS official API docs show this date as being in the format
    // of "2019-05-06T03:00:00+02:00", however the actual API response uses
    // "2019-05-06 03:00:00", which is the format used here.
    EnedisDailyConsumptionMaxPowerDTO dto = objectMapper.readValue("""
        {
          "meter_reading": {
            "usage_point_id": "16401220101758",
            "start": "2019-05-06",
            "end": "2019-05-12",
            "quality": "BRUT",
            "reading_type": {
              "measurement_kind": "power",
              "measuring_period": "string",
              "unit": "VA",
              "aggregate": "maximum"
            },
            "interval_reading": [
              {
                "value": "540",
                "date": "2019-05-06 03:00:00"
              }
            ]
          }
        }""", EnedisDailyConsumptionMaxPowerDTO.class);
    when(enedisRestClient.getDailyConsumptionMaxPower(any(String.class), any(String.class),
        any(String.class), any(String.class)))
        .thenReturn(dto);
    Assert.assertTrue(
        enedisFetchService.fetchDailyConsumptionMaxPower(hardwareId, "test", "test") == 1);
  }

  @Test
  void fetchDailyProduction() throws JsonProcessingException {
    String hardwareId = "test3";
    testUtils.createDevice(hardwareId);

    EnedisDailyProductionDTO dto = objectMapper.readValue("""
        {
          "meter_reading": {
            "usage_point_id": "16401220101758",
            "start": "2019-05-06",
            "end": "2019-05-12",
            "quality": "BRUT",
            "reading_type": {
              "measurement_kind": "energy",
              "measuring_period": "P1D",
              "unit": "Wh",
              "aggregate": "sum"
            },
            "interval_reading": [
              {
                "value": "540",
                "date": "2019-05-06"
              }
            ]
          }
        }""", EnedisDailyProductionDTO.class);
    when(enedisRestClient.getDailyProduction(any(String.class), any(String.class),
        any(String.class), any(String.class)))
        .thenReturn(dto);
    Assert.assertTrue(
        enedisFetchService.fetchDailyProduction(hardwareId, "test", "test") == 1);
  }

  @Test
  void fetchConsumptionLoadCurve() throws IOException {
    String hardwareId = "test4";
    testUtils.createDevice(hardwareId);

    EnedisConsumptionLoadCurveDTO dto = objectMapper.readValue("""
            {
                "meter_reading": {
                    "usage_point_id": "16401220101758",
                    "start": "2025-03-12",
                    "end": "2025-03-13",
                    "quality": "BRUT",
                    "reading_type": {
                        "unit": "W",
                        "measurement_kind": "power",
                        "aggregate": "average"
                    },
                    "interval_reading": [
                        {
                            "value": "2249",
                            "date": "2025-03-12 00:30:00",
                            "interval_length": "PT30M",
                            "measure_type": "B"
                        },
                        {
                            "value": "1032",
                            "date": "2025-03-12 01:00:00",
                            "interval_length": "PT30M",
                            "measure_type": "B"
                        }
                    ]
                }
            }""", EnedisConsumptionLoadCurveDTO.class);
    when(enedisRestClient.getConsumptionLoadCurve(any(String.class), any(String.class),
            any(String.class), any(String.class)))
            .thenReturn(dto);
    Assert.assertTrue(enedisFetchService.fetchConsumptionLoadCurve(hardwareId, "test", "test") == 1);
  }

  @Test
  void fetchProductionLoadCurve() throws IOException {
      String hardwareId = "test5";
      testUtils.createDevice(hardwareId);

      EnedisProductionLoadCurveDTO dto = objectMapper.readValue("""
              {
                  "meter_reading": {
                      "usage_point_id": "16401220101758",
                      "start": "2025-03-12",
                      "end": "2025-03-13",
                      "quality": "BRUT",
                      "reading_type": {
                          "unit": "W",
                          "measurement_kind": "power",
                          "aggregate": "average"
                      },
                      "interval_reading": [
                          {
                              "value": "338",
                              "date": "2025-03-12 00:30:00",
                              "interval_length": "PT30M",
                              "measure_type": "B"
                          },
                          {
                              "value": "512",
                              "date": "2025-03-12 01:00:00",
                              "interval_length": "PT30M",
                              "measure_type": "B"
                          }           \s
                      ]
                  }
              }""", EnedisProductionLoadCurveDTO.class);
      when(enedisRestClient.getProductionLoadCurve(any(String.class), any(String.class),
              any(String.class), any(String.class)))
              .thenReturn(dto);
      Assert.assertTrue(enedisFetchService.fetchProductionLoadCurve(hardwareId, "test", "test") == 1);
  }

}
