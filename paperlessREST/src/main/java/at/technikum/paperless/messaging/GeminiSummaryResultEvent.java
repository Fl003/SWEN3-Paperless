package at.technikum.paperless.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GeminiSummaryResultEvent {
    private String documentId;
    private String summary;
}
