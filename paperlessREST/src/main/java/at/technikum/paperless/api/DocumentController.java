package at.technikum.paperless.api;

import at.technikum.paperless.domain.Document;
import at.technikum.paperless.domain.Tag;
import at.technikum.paperless.domain.User;
import at.technikum.paperless.dto.DocumentDTO;
import at.technikum.paperless.dto.DocumentSearchResultDTO;
import at.technikum.paperless.mapper.DocumentMapper;
import at.technikum.paperless.service.DocumentSearchService;
import at.technikum.paperless.service.DocumentService;
import at.technikum.paperless.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentService service;
    private final DocumentSearchService searchService;
    @Autowired
    private DocumentMapper mapper;
    @Autowired
    private UserUtils userUtils;

    // GET /api/v1/documents  -> list all documents
    @GetMapping
    public List<DocumentDTO> list() {
        User currentUser = userUtils.getCurrentUser();
        var documents =  currentUser.getDocuments();
        return documents.stream()
                .map(mapper::map)
                .toList();
    }

    // elastic search
    @GetMapping("/search")
    public List<DocumentSearchResultDTO> search(
            @RequestParam(name = "q") String query,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        User currentUser = userUtils.getCurrentUser();

        String tenantId = currentUser.getUsername();

        return searchService.search(query, tenantId, page, size);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentDTO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "tags", required = false) List<String> tags
    ) {
        log.info("Acceptet request for Post");
        log.info("File name: {}", file.getOriginalFilename());
        // Überprüfe die Dateigröße (10MB in Bytes)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "Datei ist zu groß. Maximale Größe ist 10MB"
            );
        }
        User currentUser = userUtils.getCurrentUser();
        log.info("User {}", currentUser.getUsername());
        var document = service.uploadFile(file, tags, currentUser);
        currentUser.addDocument(document);

        return ResponseEntity
                .created(URI.create("/api/v1/documents/" + document.getId()))
                .body(mapper.map(document));
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public DocumentDTO get(@PathVariable long id) {
        var document = service.get(id);
        return mapper.map(document);
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