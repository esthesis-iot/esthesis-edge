package esthesis.edge.modules.enedis.service;

// Temporary mock data until the API is back available.
public class MockData {

  public static final String CQD = "{\n"
      + "  \"meter_reading\": {\n"
      + "    \"usage_point_id\": \"16401220101758\",\n"
      + "    \"start\": \"2019-05-06\",\n"
      + "    \"end\": \"2019-05-12\",\n"
      + "    \"quality\": \"BRUT\",\n"
      + "    \"reading_type\": {\n"
      + "      \"measurement_kind\": \"energy\",\n"
      + "      \"measuring_period\": \"P1D\",\n"
      + "      \"unit\": \"Wh\",\n"
      + "      \"aggregate\": \"sum\"\n"
      + "    },\n"
      + "    \"interval_reading\": [\n"
      + "      {\n"
      + "        \"value\": \"540\",\n"
      + "        \"date\": \"2019-05-06\"\n"
      + "      }\n"
      + "    ]\n"
      + "  }\n"
      + "}";
}
