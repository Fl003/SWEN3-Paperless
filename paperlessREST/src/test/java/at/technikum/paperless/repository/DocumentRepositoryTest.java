package at.technikum.paperless.repository;

import at.technikum.paperless.domain.Document;
import at.technikum.paperless.domain.User;
import at.technikum.paperless.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DocumentRepositoryTest {

    @Autowired DocumentRepository docs;
    @Autowired
    UserRepository users;

    @Test
    void savesAndReads() {
        var now = OffsetDateTime.now();

        // 1) Create + persist an author (required because Document.author is @NotNull)
        var author = new User();
        author.setUsername("testuser");
        author.setPasswordDigest("pw");
        author = users.save(author);

        // 2) Create document WITH author
        var d = Document.builder()
                .name("contract.pdf")
                .contentType("application/pdf")
                .sizeBytes(1234L)
                .status("uploaded")
                .createdAt(now)
                .lastEdited(now)
                .author(author)
                .build();

        var saved = docs.save(d);

        assertThat(saved.getId()).isNotNull();
        assertThat(docs.findById(saved.getId())).isPresent();
    }
}