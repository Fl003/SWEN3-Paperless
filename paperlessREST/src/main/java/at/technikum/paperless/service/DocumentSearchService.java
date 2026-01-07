package at.technikum.paperless.service;

import at.technikum.paperless.dto.DocumentSearchResultDTO;
import at.technikum.paperless.search.IndexedDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentSearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    private static final String INDEX_NAME = "documents";

    public List<DocumentSearchResultDTO> search(String queryText, Long ownerId, int page, int size) {
        if (queryText == null || queryText.isBlank()) {
            return List.of();
        }

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> {
                    // full-text match on the OCR text field
                    b.must(m -> m.match(mm -> mm
                            .field("text")
                            .query(queryText)
                    ));

                    //filter by document owner (current user)
                    b.filter(f -> f.term(t -> t
                            .field("ownerId")
                            .value(ownerId)
                            ));
                    return b;
                }))
                .withPageable(PageRequest.of(Math.max(page, 0), Math.max(size, 1)))
                .build();

        SearchHits<IndexedDocument> hits = elasticsearchOperations.search(
                query,
                IndexedDocument.class,
                IndexCoordinates.of(INDEX_NAME)
        );

        return hits.getSearchHits().stream()
                .map(this::toDto)
                .toList();
    }

    private DocumentSearchResultDTO toDto(SearchHit<IndexedDocument> hit) {
        IndexedDocument doc = hit.getContent();

        Double score = Double.valueOf(hit.getScore()); // hit.getScore() is float

        // IndexedDocument likely stores documentId as String -> convert to Long for DTO
        Long documentIdLong = null;
        try {
            if (doc != null && doc.getDocumentId() != null) {
                documentIdLong = Long.valueOf(doc.getDocumentId());
            }
        } catch (NumberFormatException ignored) {
            // keep null if it's not numeric
        }

        String snippet = makeSnippet(doc != null ? doc.getText() : null, 220);

        return DocumentSearchResultDTO.builder()
                .documentId(documentIdLong)
                .name(null) // not stored in ES currently (can be enriched from DB later)
                .contentType(doc != null ? doc.getContentType() : null)
                .status(doc != null ? doc.getStatus() : null)
                .tags(null) // not stored in ES currently (can be enriched from DB later)
                .score(score)
                .snippet(snippet)
                .build();
    }

    private String makeSnippet(String text, int maxLen) {
        if (text == null) return "";
        String t = text.replaceAll("\\s+", " ").trim();
        if (t.length() <= maxLen) return t;
        return t.substring(0, maxLen) + "...";
    }
}