package at.technikum.paperless.genaiworker.service;

import at.technikum.paperless.genaiworker.client.GeminiClient;
import at.technikum.paperless.genaiworker.kafka.DocumentUploadedEvent;
import at.technikum.paperless.genaiworker.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenAiService {
    private final GeminiClient geminiClient;
    private final StorageService storage;

    // simple text , summary help (eg for ocr text)
    public String generateSummery(String text) {
        try {
            log.info("Requesting Gemini text summary (length={} chars)", text != null ? text.length() : 0);
            String summary = geminiClient.requestSummery(text);
            log.info("Gemini text summary generated ({} chars)", summary != null ? summary.length() : 0);
            return summary;
        } catch (GeminiException e) {
            //already logged in GeminiClient, just add context
            log.error("GeminiException while summarizing plain text", e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while summarizing plain text", e);
            throw new GeminiException("Failed to generate summary from text", e);
        }
    }

    public String handleDocumentUploadedEvent(DocumentUploadedEvent event) {
        String storagePath = event.getStoragePath();
        String documentId = event.getDocumentId();

        try {
            log.info("Starting GenAI summary for documentId={} storagePath={}", documentId, storagePath);

            // 1. load bytes + content type from minio
            byte[] fileData = storage.load(storagePath);
            String contentType = storage.getContentType(storagePath);
            log.info("Loaded file from MinIO: key={}, type={}, size={} bytes",
                    storagePath, contentType, fileData != null ? fileData.length : -1);

            // 2. request summary from gemini
            String summary = geminiClient.requestSummary(fileData, contentType);
            log.info("Successfully generated summary for documentId={} ({} chars)",
                    documentId, summary != null ? summary.length() : 0);

            return summary;

        } catch (GeminiException e) {
            // gemini already logged cause we just add document context
            log.error("GeminiException while generating summary for documentId={}", documentId, e);
            throw e;

        } catch (Exception e) {
            log.error("Unexpected error while handling DocumentUploadedEvent for documentId={}", documentId, e);
            throw new GeminiException("Failed to generate summary for documentId=" + documentId, e);
        }
    }
}
