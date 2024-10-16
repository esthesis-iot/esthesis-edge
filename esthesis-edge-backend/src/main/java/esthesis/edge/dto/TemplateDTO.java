package esthesis.edge.dto;

import io.quarkus.qute.Engine;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

public class TemplateDTO {

  @Getter
  @Setter
  private String body;
  private final Engine engine;

  public TemplateDTO() {
    this.engine = Engine.builder().addDefaults().build();
  }

  public TemplateDTO(String body) {
    this();
    this.body = body;
  }

  private final Map<String, Object> data = new HashMap<>();

  public TemplateDTO data(String key, Object value) {
    data.put(key, value);
    return this;
  }

  public String render() {
    return engine.parse(body).data(data).render();
  }
}
