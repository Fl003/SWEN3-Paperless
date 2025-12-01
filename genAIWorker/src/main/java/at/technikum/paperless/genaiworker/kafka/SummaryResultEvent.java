package at.technikum.paperless.genaiworker.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SummaryResultEvent {

    private String eventId;
    private String documentId;
    private String traceId;
    private String summary;

    private String status; // "DONE" or "ERROR"
    private Long durationMs; // nice for logging
    private String errorMessage;
}
