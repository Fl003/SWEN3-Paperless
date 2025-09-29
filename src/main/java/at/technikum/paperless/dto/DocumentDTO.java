package at.technikum.paperless.dto;

import at.technikum.paperless.domain.Tag;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Setter
@Getter
//show
public class DocumentDTO {
    private Long documentId;
    private String name;
    private String contentType;
    private long sizeBytes;
    private String status;      // uploaded|ocrdone|indexed
    private OffsetDateTime createdAt;
    private OffsetDateTime lastEdited;
    private List<String> tags;
}