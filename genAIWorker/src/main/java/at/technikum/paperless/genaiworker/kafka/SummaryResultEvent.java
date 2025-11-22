package at.technikum.paperless.genaiworker.kafka;

import lombok.Data;

@Data
public class SummaryResultEvent {
    private final String documentId;
    private final String summery;
}
