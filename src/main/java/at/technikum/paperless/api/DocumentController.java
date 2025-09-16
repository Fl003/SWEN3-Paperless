package at.technikum.paperless.api;

import at.technikum.paperless.domain.Document;
import at.technikum.paperless.domain.Tag;
import at.technikum.paperless.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService service;

    @PostMapping
    public ResponseEntity<Map<String,Object>> add(@RequestBody Map<String,Object> body) {
        var d = service.create(
                (String) body.get("name"),
                (String) body.get("contentType"),
                ((Number) body.get("sizeBytes")).longValue(),
                (List<String>) body.get("tags"));
        return ResponseEntity.created(URI.create("/api/v1/documents/" + d.getId()))
                .body(toResponse(d));
    }

    @GetMapping("/{id}")
    public Map<String,Object> get(@PathVariable long id) {
        return toResponse(service.get(id));
    }

    @PutMapping("/{id}")
    public Map<String,Object> update(@PathVariable long id, @RequestBody Map<String,Object> body) {
        var d = service.update(
                id,
                (String) body.get("name"),
                (String) body.get("contentType"),
                body.get("sizeBytes") == null ? null : ((Number) body.get("sizeBytes")).longValue(),
                (String) body.get("status"),
                (List<String>) body.get("tags"));
        return toResponse(d);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private Map<String,Object> toResponse(Document d) {
        return Map.of(
                "documentId", d.getId(),
                "name", d.getName(),
                "contentType", d.getContentType(),
                "sizeBytes", d.getSizeBytes(),
                "status", d.getStatus(),
                "createdAt", d.getCreatedAt(),
                "lastEdited", d.getLastEdited(),
                "tags", d.getTags().stream().map(Tag::getName).toList()
        );
    }
}