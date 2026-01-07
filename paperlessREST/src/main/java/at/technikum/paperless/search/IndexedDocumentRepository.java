package at.technikum.paperless.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface IndexedDocumentRepository extends ElasticsearchRepository<IndexedDocument, String> {
}