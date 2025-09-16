package at.technikum.paperless.repository;

import at.technikum.paperless.domain.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DocumentRepositoryTest {
    @Autowired DocumentRepository docs;

    @Test void savesAndReads() {
        var now = OffsetDateTime.now();
        var d = Document.builder()
                .name("contract.pdf").contentType("application/pdf").sizeBytes(1234)
                .status("uploaded").createdAt(now).lastEdited(now).build();

        var saved = docs.save(d);
        assertThat(saved.getId()).isNotNull();
        assertThat(docs.findById(saved.getId())).isPresent();
    }
}
