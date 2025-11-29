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
    public String generateSummery(String text) {
        return geminiClient.requestSummery(text);
    }

    public String handleDocumentUploadedEvent(DocumentUploadedEvent event) {
        //1. takes file from minio
        byte[] fileData = storage.load(event.getStoragePath());
        String contentType = storage.getContentType(event.getStoragePath());

        log.info("Loaded file from MiniO: key={}, type={}, size={} bytes", event.getStoragePath(), contentType, fileData.length);

        //2. Sends request to gemini
        String summery = geminiClient.requestSummary(fileData, contentType);
        return summery;
    }
}
