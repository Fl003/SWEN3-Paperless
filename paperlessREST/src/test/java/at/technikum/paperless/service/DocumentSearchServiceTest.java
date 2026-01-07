package at.technikum.paperless.service;

import at.technikum.paperless.dto.DocumentSearchResultDTO;
import at.technikum.paperless.search.IndexedDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DocumentSearchServiceTest {

    private ElasticsearchOperations elasticsearchOperations;
    private DocumentSearchService service;

    @BeforeEach
    void setUp() {
        elasticsearchOperations = mock(ElasticsearchOperations.class);
        service = new DocumentSearchService(elasticsearchOperations);
    }

    @Test
    void search_blankQuery_returnsEmptyList_andDoesNotCallElasticsearch() {
        List<DocumentSearchResultDTO> res1 = service.search("   ", 1L, 0, 10);
        List<DocumentSearchResultDTO> res2 = service.search(null, 1L, 0, 10);

        assertTrue(res1.isEmpty());
        assertTrue(res2.isEmpty());

        verifyNoInteractions(elasticsearchOperations);
    }

    @Test
    void search_callsElasticsearch_withIndexDocuments_andMapsResults() {
        // Arrange: fake SearchHit
        SearchHit<IndexedDocument> hit = mock(SearchHit.class);
        when(hit.getScore()).thenReturn(1.23f);

        IndexedDocument doc = IndexedDocument.builder()
                .documentId("6")
                .ownerId(42L)
                .contentType("application/pdf")
                .status("DONE")
                .text("Hello   world.\nThis is   OCR text.")
                .build();

        when(hit.getContent()).thenReturn(doc);

        //arrange: fake SearchHits
        SearchHits<IndexedDocument> hits = mock(SearchHits.class);
        when(hits.getSearchHits()).thenReturn(List.of(hit));

        when(elasticsearchOperations.search(any(NativeQuery.class), eq(IndexedDocument.class), any(IndexCoordinates.class)))
                .thenReturn(hits);

        // act
        List<DocumentSearchResultDTO> result = service.search("hello", 42L, 0, 10);

        // assert
        assertEquals(1, result.size());

        DocumentSearchResultDTO dto = result.get(0);
        assertEquals(6L, dto.getDocumentId());
        assertEquals("application/pdf", dto.getContentType());
        assertEquals("DONE", dto.getStatus());
        assertEquals(1.23, dto.getScore(), 0.0001);
        assertTrue(dto.getSnippet().contains("Hello world. This is OCR text."));
    }

    @Test
    void search_nonNumericDocumentId_setsNullDocumentIdInDto() {
        SearchHit<IndexedDocument> hit = mock(SearchHit.class);
        when(hit.getScore()).thenReturn(0.5f);

        IndexedDocument doc = IndexedDocument.builder()
                .documentId("abc") // not parseable
                .contentType("text/plain")
                .status("DONE")
                .text("some text")
                .build();
        when(hit.getContent()).thenReturn(doc);

        SearchHits<IndexedDocument> hits = mock(SearchHits.class);
        when(hits.getSearchHits()).thenReturn(List.of(hit));

        when(elasticsearchOperations.search(any(NativeQuery.class), eq(IndexedDocument.class), any(IndexCoordinates.class)))
                .thenReturn(hits);

        List<DocumentSearchResultDTO> result = service.search("some", 42L, 0, 10);

        assertEquals(1, result.size());
        assertNull(result.get(0).getDocumentId());
    }

    @Test
    void search_clampsNegativePageAndSize() {
        SearchHits<IndexedDocument> hits = mock(SearchHits.class);
        when(hits.getSearchHits()).thenReturn(List.of());

        ArgumentCaptor<NativeQuery> queryCaptor = ArgumentCaptor.forClass(NativeQuery.class);

        when(elasticsearchOperations.search(queryCaptor.capture(), eq(IndexedDocument.class), any(IndexCoordinates.class)))
                .thenReturn(hits);

        service.search("abc", 42L, -5, -10);

        NativeQuery captured = queryCaptor.getValue();
        assertNotNull(captured);

        // We can't easily assert the internal query DSL, but we CAN validate it built a pageable request
        assertNotNull(captured.getPageable());
        assertEquals(0, captured.getPageable().getPageNumber());
        assertEquals(1, captured.getPageable().getPageSize());
    }

    @Test
    void search_usesDocumentsIndex() {
        SearchHits<IndexedDocument> hits = mock(SearchHits.class);
        when(hits.getSearchHits()).thenReturn(List.of());

        ArgumentCaptor<IndexCoordinates> indexCaptor = ArgumentCaptor.forClass(IndexCoordinates.class);

        when(elasticsearchOperations.search(any(NativeQuery.class), eq(IndexedDocument.class), indexCaptor.capture()))
                .thenReturn(hits);

        service.search("abc", 42L, 0, 10);

        IndexCoordinates used = indexCaptor.getValue();
        assertNotNull(used);
        assertEquals("documents", used.getIndexName());
    }
}