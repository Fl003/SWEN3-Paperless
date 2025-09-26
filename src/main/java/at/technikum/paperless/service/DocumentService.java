package at.technikum.paperless.service;

import at.technikum.paperless.domain.Document;
import at.technikum.paperless.domain.Tag;
import at.technikum.paperless.repository.DocumentRepository;
import at.technikum.paperless.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
@Service @RequiredArgsConstructor
public class DocumentService {
    private final DocumentRepository docs;
    private final TagRepository tags;
    private final FileStorageService fileStorage;

    @Transactional
    public Document uploadFile(MultipartFile file, Collection<String> tagNames) {
        try {
            log.info("tagnames: " + tagNames);

            // Speichere die Datei
            String storedFilePath = fileStorage.store(file);

            // Erstelle das Document-Objekt mit Daten aus der Datei
            var document = Document.builder()
                    .name(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .sizeBytes(file.getSize())
                    .status("uploaded")
                    .createdAt(OffsetDateTime.now())
                    .lastEdited(OffsetDateTime.now())
                    .build();

            // Falls Tags zugewiesen wurden
            if (tagNames != null) {
                for (var tn : tagNames) {
                    var tag = tags.findByNameIgnoreCase(tn)
                            .orElseGet(() -> tags.save(Tag.builder().name(tn.trim()).build()));
                    document.getTags().add(tag);
                }
            }

            // Speichere in der Datenbank
            return docs.save(document);

        } catch (IOException e) {
            throw new RuntimeException("Fehler beim Hochladen der Datei: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<Document> findAll() { return docs.findAll(); }

    @Transactional(readOnly = true) public Document get(long id){ return docs.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found: " + id));}

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

    @Transactional public void delete(long id){ docs.deleteById(id); }
}