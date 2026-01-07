package at.technikum.paperless.ocrworker;

import at.technikum.paperless.events.DocumentUploadedEvent;
import at.technikum.paperless.ocrworker.search.IndexingService;
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
    private final IndexingService indexingService;

    private final ObjectMapper om = new ObjectMapper();

    @Value("${app.kafka.out-topic:ocr.results}")
    private String outTopic;

    @KafkaListener(topics = "${app.kafka.in-topic}", groupId = "${app.kafka.group:ocr-worker}")
    public void handleDocumentUploaded(DocumentUploadedEvent e) {
        onDocumentUploaded(e);
    }

    public void onDocumentUploaded(DocumentUploadedEvent e) {
        long t0 = System.currentTimeMillis();
        String ct = null;

        try {
            String key = e.getStoragePath();

            // Content-Type from event or S3 HEAD fallback
            ct = e.getContentType();
            if (ct == null || ct.isBlank()) {
                try { ct = storage.getContentType(key); } catch (Exception ignore) { ct = null; }
            }
            String safeCt = (ct == null ? "unknown" : ct);

            byte[] bytes = storage.load(key);
            String text = ocr.ocr(bytes, ct, e.getOriginalFilename());

            long duration = System.currentTimeMillis() - t0;

            // Index into Elasticsearch
            indexingService.indexDone(
                    e.getDocumentId(),
                    e.getTenantId(),
                    safeCt,
                    duration,
                    text
            );

            // publish OCR result to Kafka (existing)
            var payload = Map.of(
                    "eventId", e.getEventId(),
                    "documentId", e.getDocumentId(),
                    "traceId", e.getTraceId(),
                    "tenantId", e.getTenantId(),
                    "status", "DONE",
                    "durationMs", duration,
                    "contentType", safeCt,
                    "text", text
            );

            kafkaTemplate.send(outTopic, e.getDocumentId(), om.writeValueAsString(payload));
            log.info("OCR done docId={} ms={}", e.getDocumentId(), duration);

        } catch (Exception ex) {
            String safeCt = (ct == null ? "unknown" : ct);

            // Index into Elasticsearch (ERROR)
            try {
                indexingService.indexError(
                        e.getDocumentId(),
                        e.getTenantId(),
                        safeCt,
                        ex.getMessage()
                );
            } catch (Exception idxEx) {
                log.error("Failed to index ERROR state into Elasticsearch. docId={}", e.getDocumentId(), idxEx);
            }

            // existing Kafka error publish
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

            log.error("OCR failed docId={} error={}", e.getDocumentId(), ex.getMessage(), ex);
        }
    }
}