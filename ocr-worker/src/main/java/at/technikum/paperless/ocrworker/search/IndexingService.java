package at.technikum.paperless.ocrworker.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Log4j2
public class IndexingService {

    private final IndexedDocumentRepository repo;

    public void indexDone(String documentId,
                          Long ownerId,
                          String tenantId,
                          String contentType,
                          long durationMs,
                          String text) {
        log.error("INDEXING to ES documentId={} tenantId={} ownerId={}", documentId, tenantId, ownerId);
        repo.save(IndexedDocument.builder()
                .documentId(documentId)
                .ownerId(ownerId)
                .tenantId(tenantId)
                .contentType(contentType)
                .status("DONE")
                .durationMs(durationMs)
                .text(text)
                .indexedAt(Instant.now())
                .build());
    }

    public void indexError(String documentId,
                           Long ownerId,
                           String tenantId,
                           String contentType,
                           String errorMessage) {

        repo.save(IndexedDocument.builder()
                .documentId(documentId)
                        .ownerId(ownerId)
                .tenantId(tenantId)
                .contentType(contentType)
                .status("ERROR")
                .text(errorMessage)
                .indexedAt(Instant.now())
                .build());
    }
}