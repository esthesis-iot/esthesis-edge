package esthesis.edge.services;

import esthesis.common.avro.AvroUtils;
import esthesis.common.avro.PayloadData;
import esthesis.common.avro.ValueData;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class QueuePayloadSanitizer {

  private final AvroUtils avroUtils;

  SanitizedQueuePayload sanitize(String queueItemId, String dataObject) {
    List<SanitizedLine> validLines = new ArrayList<>();
    int droppedLineCount = 0;

    for (String line : dataObject.split("\n")) {
      if (line.isBlank()) {
        droppedLineCount++;
        continue;
      }

      try {
        PayloadData payloadData = avroUtils.parsePayload(line);
        for (ValueData valueData : payloadData.getValues()) {
          parseFieldValue(valueData);
        }
        validLines.add(new SanitizedLine(line, payloadData));
      } catch (Exception e) {
        droppedLineCount++;
        log.error("Skipping malformed line '{}' from queue item '{}'.", line, queueItemId, e);
      }
    }

    if (droppedLineCount > 0) {
      log.warn("Queue item '{}' dropped {} malformed line(s) during sanitization.",
          queueItemId, droppedLineCount);
    }

    return new SanitizedQueuePayload(validLines, droppedLineCount);
  }

  Object parseFieldValue(ValueData valueData) {
    return switch (valueData.getValueType()) {
      case STRING -> valueData.getValue();
      case BOOLEAN -> Boolean.parseBoolean(valueData.getValue());
      case BYTE -> Byte.parseByte(valueData.getValue());
      case SHORT -> Short.parseShort(valueData.getValue());
      case INTEGER -> Integer.parseInt(valueData.getValue());
      case LONG, BIG_INTEGER -> Long.parseLong(valueData.getValue());
      case FLOAT -> Float.parseFloat(valueData.getValue());
      case DOUBLE, BIG_DECIMAL -> Double.parseDouble(valueData.getValue());
      case UNKNOWN -> {
        log.warn("Unknown value type '{}' for field '{}'.", valueData.getValueType(),
            valueData.getName());
        yield null;
      }
    };
  }

  record SanitizedLine(String line, PayloadData payloadData) {
  }

  record SanitizedQueuePayload(List<SanitizedLine> lines, int droppedLineCount) {

    boolean hasValidLines() {
      return !lines.isEmpty();
    }

    String toDataObject() {
      return lines.stream().map(SanitizedLine::line).collect(Collectors.joining("\n"));
    }
  }
}