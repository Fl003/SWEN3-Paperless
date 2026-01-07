package at.technikum.paperless.ocrworker.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class IndexingService {

    private final IndexedDocumentRepository repo;

    public void indexDone(String documentId,
                          String tenantId,
                          String contentType,
                          long durationMs,
                          String text) {

        repo.save(IndexedDocument.builder()
                .documentId(documentId)
                .tenantId(tenantId)
                .contentType(contentType)
                .status("DONE")
                .durationMs(durationMs)
                .text(text)
                .indexedAt(Instant.now())
                .build());
    }

    public void indexError(String documentId,
                           String tenantId,
                           String contentType,
                           String errorMessage) {

        repo.save(IndexedDocument.builder()
                .documentId(documentId)
                .tenantId(tenantId)
                .contentType(contentType)
                .status("ERROR")
                .text(errorMessage)
                .indexedAt(Instant.now())
                .build());
    }
}