package esthesis.edge.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
class QueuePayloadSanitizerTest {

  @Inject
  QueuePayloadSanitizer queuePayloadSanitizer;

  @Test
  void sanitizeDropsMalformedNumericLines() {
    QueuePayloadSanitizer.SanitizedQueuePayload sanitizedQueuePayload =
        queuePayloadSanitizer.sanitize("queue-1",
            "energy active=0.137f 2025-10-29T01:45:00Z\n"
                + "energy active=nullf 2025-10-29T02:45:00Z\n"
                + "energy active=0.121f 2025-10-29T03:15:00Z");

    assertTrue(sanitizedQueuePayload.hasValidLines());
    assertEquals(1, sanitizedQueuePayload.droppedLineCount());
    assertEquals("energy active=0.137f 2025-10-29T01:45:00Z\n"
            + "energy active=0.121f 2025-10-29T03:15:00Z",
        sanitizedQueuePayload.toDataObject());
    assertFalse(sanitizedQueuePayload.toDataObject().contains("nullf"));
  }

  @Test
  void sanitizeReturnsEmptyPayloadWhenAllLinesAreInvalid() {
    QueuePayloadSanitizer.SanitizedQueuePayload sanitizedQueuePayload =
        queuePayloadSanitizer.sanitize("queue-2",
            "energy active=nullf 2025-10-29T02:45:00Z\n"
                + "energy active=bad-valuef 2025-10-29T03:15:00Z");

    assertFalse(sanitizedQueuePayload.hasValidLines());
    assertEquals(2, sanitizedQueuePayload.droppedLineCount());
    assertEquals("", sanitizedQueuePayload.toDataObject());
  }
}