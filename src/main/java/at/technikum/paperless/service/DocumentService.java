package at.technikum.paperless.service;

import at.technikum.paperless.domain.Document;
import at.technikum.paperless.domain.Tag;
import at.technikum.paperless.repository.DocumentRepository;
import at.technikum.paperless.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Collection;

@Service @RequiredArgsConstructor
public class DocumentService {
    private final DocumentRepository docs;
    private final TagRepository tags;

    @Transactional
    public Document create(String name, String contentType, long sizeBytes, Collection<String> tagNames) {
        var now = OffsetDateTime.now();
        var d = Document.builder()
                .name(name).contentType(contentType).sizeBytes(sizeBytes)
                .status("uploaded").createdAt(now).lastEdited(now).build();

        if (tagNames != null) {
            for (var tn : tagNames) {
                var tag = tags.findByNameIgnoreCase(tn)
                        .orElseGet(() -> tags.save(Tag.builder().name(tn.trim()).build()));
                d.getTags().add(tag);
            }
        }
        return docs.save(d);
    }

    @Transactional(readOnly = true) public Document get(long id){ return docs.findById(id).orElseThrow(); }

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