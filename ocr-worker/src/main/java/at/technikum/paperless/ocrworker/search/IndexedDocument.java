package at.technikum.paperless.ocrworker.search;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "#{@indexNameProvider.indexName}")
public class IndexedDocument {

    @Id
    private String documentId;
    private Long ownerId;

    private String tenantId;
    private String contentType;

    private String status;     // DONE / ERROR
    private Long durationMs;

    //full OCR text. ES will index this for full-text search
    private String text;

    private Instant indexedAt;
}