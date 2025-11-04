package at.technikum.paperless.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class OcrResponseListener {

    private static final Logger log = LoggerFactory.getLogger(OcrResponseListener.class);

    @KafkaListener(topics = "ocr.responses", groupId = "rest-server-group")
    public void handleOcrResponse(Map<String, Object> response) {
        String docId = String.valueOf(response.get("documentId"));
        String status = String.valueOf(response.get("status"));
        String text = String.valueOf(response.get("text"));

        log.info("Received OCR response for document {} → [{}] {}", docId, status, text);

        // TODO: hier kannst du später z.B. den OCR-Text in der Datenbank speichern
    }
}
