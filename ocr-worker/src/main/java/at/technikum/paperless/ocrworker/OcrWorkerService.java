package at.technikum.paperless.ocrworker;

import at.technikum.paperless.events.DocumentUploadedEvent;
import at.technikum.paperless.ocrworker.service.OcrEngine;
import at.technikum.paperless.ocrworker.service.S3StorageClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OcrWorkerService {

    private final S3StorageClient storage;
    private final OcrEngine ocr;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper om = new ObjectMapper();

    @Value("${app.kafka.out-topic:ocr.results}")
    private String outTopic;

    @KafkaListener(topics = "${app.kafka.in-topic}", groupId = "${app.kafka.group}")
    public void onDocumentUploaded(DocumentUploadedEvent e) {
        long t0 = System.currentTimeMillis();
        try {
            String key = e.getStoragePath();
            // Content-Type aus Event oder per HEAD aus S3
            String ct = e.getContentType();
            if (ct == null || ct.isBlank()) {
                try { ct = storage.getContentType(key); } catch (Exception ignore) { ct = null; }
            }

            byte[] bytes = storage.load(key);
            String text = ocr.ocr(bytes, ct, e.getOriginalFilename());

            var payload = Map.of(
                    "eventId", e.getEventId(),
                    "documentId", e.getDocumentId(),
                    "traceId", e.getTraceId(),
                    "tenantId", e.getTenantId(),
                    "status", "DONE",
                    "durationMs", (System.currentTimeMillis() - t0),
                    "contentType", (ct == null ? "unknown" : ct),
                    "text", text
            );

            kafkaTemplate.send(outTopic, e.getDocumentId(), om.writeValueAsString(payload));
            log.info("OCR done docId={} ms={}", e.getDocumentId(), (System.currentTimeMillis()-t0));

        } catch (Exception ex) {
            try {
                var err = Map.of(
                        "eventId", e.getEventId(),
                        "documentId", e.getDocumentId(),
                        "traceId", e.getTraceId(),
                        "tenantId", e.getTenantId(),
                        "status", "ERROR",
                        "error", ex.getMessage()
                );
                kafkaTemplate.send(outTopic, e.getDocumentId(), om.writeValueAsString(err));
            } catch (Exception ignore) {}
        }
    }
}

