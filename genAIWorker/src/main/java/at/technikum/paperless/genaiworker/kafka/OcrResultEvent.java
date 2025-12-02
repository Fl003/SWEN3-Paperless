package at.technikum.paperless.genaiworker.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OcrResultEvent {
    private String eventId;
    private String documentId;
    private String tenantId;
    private String contentType;
    private String status;
    private String text;
    private long durationMs;
    private String traceId;
}
