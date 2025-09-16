package at.technikum.paperless.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity @Table(name="documents")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Document {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private String name;
    @Column(nullable = false) private String contentType;
    @Column(nullable = false) private long   sizeBytes;
    @Column(nullable = false) private String status;      // uploaded|ocrdone|indexed
    @Column(nullable = false) private OffsetDateTime createdAt;
    @Column(nullable = false) private OffsetDateTime lastEdited;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name="document_tags",
            joinColumns=@JoinColumn(name="document_id"),
            inverseJoinColumns=@JoinColumn(name="tag_id"))
    private Set<Tag> tags = new HashSet<>();
}
