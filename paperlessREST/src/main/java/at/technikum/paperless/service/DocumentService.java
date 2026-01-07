package at.technikum.paperless.service;

import at.technikum.paperless.domain.Document;
import at.technikum.paperless.domain.Tag;
import at.technikum.paperless.domain.User;
import at.technikum.paperless.repository.DocumentRepository;
import at.technikum.paperless.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.*;

import at.technikum.paperless.messaging.DocumentEventsProducer;
import at.technikum.paperless.messaging.DocumentUploadedEvent;
import at.technikum.paperless.exception.DocumentNotFoundException;
import at.technikum.paperless.exception.FileStorageException;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service @RequiredArgsConstructor
public class DocumentService {
    private final DocumentRepository docs;
    private final TagRepository tags;
    private final FileStorageService fileStorage;
    private final DocumentEventsProducer eventsProducer;

    @Transactional
    public Document uploadFile(MultipartFile file, Collection<String> tagNames, User author) {
        // upload the file to MinIO via S3StorageService (returns the S3 object key, e.g. "docs/abc.pdf")
        final String s3Key;
        try {
            s3Key = fileStorage.store(file);
        } catch (Exception ex) {
            // if FileStorageService still throws Exception, translate it here
            throw new FileStorageException("Fehler beim Hochladen der Datei", ex);
        }

        // create aggregate
        var document = Document.builder()
                .name(file.getOriginalFilename())
                .author(author)
                .contentType(file.getContentType())
                .sizeBytes(file.getSize())
                .storageBucket("paperless")
                .storageKey(s3Key)
                .status("uploaded")
                .createdAt(OffsetDateTime.now())
                .lastEdited(OffsetDateTime.now())
                .build();

        // attach tags (create on demand)
        if (tagNames != null) {
            for (var tn : tagNames) {
                var tag = tags.findByNameIgnoreCase(tn)
                        .orElseGet(() -> tags.save(Tag.builder().name(tn.trim()).build()));
                document.getTags().add(tag);
            }
        }

        log.info("Author before save: {}", document.getAuthor());
        // persist
        var saved = docs.save(document);

        // publish event
        publishUploadedEvent(saved, s3Key);

        return saved;
    }

    @Transactional(readOnly = true)
    public List<Document> findAll() { return docs.findAll(); }

    @Transactional(readOnly = true)
    public Document get(long id) {
        return docs.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException(id));
    }

    @Transactional
    public Document update(long id, String name, String ct, Long size, String status, Collection<String> tagNames){
        var d = get(id);
        if (name != null) d.setName(name);
        if (ct != null) d.setContentType(ct);
        if (size != null) d.setSizeBytes(size);
        if (status != null) d.setStatus(status);
        if (tagNames != null) {
            d.getTags().clear();
            for (var tn : tagNames) {
                var tag = tags.findByNameIgnoreCase(tn)
                        .orElseGet(() -> tags.save(Tag.builder().name(tn.trim()).build()));
                d.getTags().add(tag);
            }
        }
        d.setLastEdited(OffsetDateTime.now());
        return d;
    }

    @Transactional
    public void delete(long id) {
        // ensure existence first to surface 404
        if (!docs.existsById(id)) {
            throw new DocumentNotFoundException(id);
        }
        docs.deleteById(id);
    }

    @Transactional
    public void saveSummary(String documentId, String summery) {
        try {
            Long id = Long.parseLong(documentId);
            docs.saveSummaryById(id, summery);
        } catch (NumberFormatException e) {
            log.error("Invalid documentId received from Kafka: {}", documentId);
            throw e;
        }
    }

    private void publishUploadedEvent(Document saved, String storagePath) {
        var event = DocumentUploadedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .occurredAt(Instant.now())
                .documentId(String.valueOf(saved.getId()))
                .originalFilename(saved.getName())
                .contentType(saved.getContentType())
                .storagePath(storagePath)
                .uploadedBy("system")       // TODO later: replace with authenticated user
                .tenantId("default")       // TODO later: tenant logic if needed
                .ownerId(saved.getAuthor().getId())
                .traceId(UUID.randomUUID().toString())
                .build();

        eventsProducer.publish(event);
        log.info("Published DocumentUploadedEvent for docId={} -> {}", saved.getId(), storagePath);
    }
}