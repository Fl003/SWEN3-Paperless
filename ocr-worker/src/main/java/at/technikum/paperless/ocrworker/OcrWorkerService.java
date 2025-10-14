package at.technikum.paperless.ocrworker;

import at.technikum.paperless.events.DocumentUploadedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OcrWorkerService {

    private final KafkaTemplate<String, Map<String, Object>> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OcrWorkerService(KafkaTemplate<String, Map<String, Object>> kafkaTemplate,
                            ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "paperless.documents", groupId = "ocr-worker-group")
    public void handleDocumentUploaded(DocumentUploadedEvent event) {
        try {
            System.out.println("Received new document event: " + event);

            Map<String, Object> response = Map.of(
                    "documentId", event.getDocumentId(),
                    "status", "done",
                    "text", "Dummy OCR result"
            );

            kafkaTemplate.send("ocr.responses", response);
            System.out.println("Sent OCR response: " + response);

        } catch (Exception e) {
            System.err.println("Failed to process event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
