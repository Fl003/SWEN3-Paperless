package at.technikum.paperless.search;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "documents")
public class IndexedDocument {

    @Id
    private String documentId;

    @Field(type = FieldType.Keyword)
    private String tenantId;

    @Field(type = FieldType.Long)
    private Long ownerId;

    @Field(type = FieldType.Keyword)
    private String contentType;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Long)
    private Long durationMs;

    @Field(type = FieldType.Text)
    private String text;

    @Field(type = FieldType.Long)
    private Long indexedAt;
}
