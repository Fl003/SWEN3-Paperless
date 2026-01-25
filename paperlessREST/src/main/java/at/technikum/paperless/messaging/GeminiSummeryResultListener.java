package at.technikum.paperless.messaging;

import at.technikum.paperless.service.DocumentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Profile("!test")
@Component
public class GeminiSummeryResultListener {
    private final ObjectMapper objectMapper;
    private final DocumentService documentService;

    @KafkaListener(topics = "${paperless.kafka.summary-topic}", groupId = "paperless-summary-group")
    public void handleSummeryResult(GeminiSummaryResultEvent event) {
        log.info("Received summary for document {}", event.getDocumentId());
        documentService.saveSummary(event.getDocumentId(), event.getSummary());
    }

}
