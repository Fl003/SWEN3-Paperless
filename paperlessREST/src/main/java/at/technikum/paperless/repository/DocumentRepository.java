package at.technikum.paperless.repository;

import at.technikum.paperless.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {}
