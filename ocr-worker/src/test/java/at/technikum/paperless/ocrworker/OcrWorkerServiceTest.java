package at.technikum.paperless.ocrworker;

import at.technikum.paperless.events.DocumentUploadedEvent;
import at.technikum.paperless.ocrworker.search.IndexingService;
import at.technikum.paperless.ocrworker.service.OcrEngine;
import at.technikum.paperless.ocrworker.service.S3StorageClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OcrWorkerServiceTest {

    @Mock S3StorageClient storage;
    @Mock OcrEngine ocr;
    @Mock KafkaTemplate<String, String> kafka;

    // ✅ THIS is what you're missing
    @Mock IndexingService indexingService;

    @Captor ArgumentCaptor<String> payloadCaptor;

    @InjectMocks OcrWorkerService service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(service, "outTopic", "ocr.results.test");
    }

    private DocumentUploadedEvent event(String ct) {
        DocumentUploadedEvent e = new DocumentUploadedEvent();
        e.setEventId("e-1");
        e.setDocumentId("doc-42");
        e.setOwnerId(1L);
        e.setTraceId("trace-123");
        e.setTenantId("default");
        e.setOriginalFilename("file.pdf");
        e.setStoragePath("docs/doc-42.pdf");
        e.setContentType(ct);
        return e;
    }

    @Test
    void happyPath_usesEventContentType_andPublishesDONE() throws Exception {
        var e = event("application/pdf");

        when(storage.load("docs/doc-42.pdf")).thenReturn("PDFBYTES".getBytes());
        when(ocr.ocr(any(), eq("application/pdf"), eq("file.pdf"))).thenReturn("EXTRACTED TEXT");

        service.onDocumentUploaded(e);

        verify(kafka).send(eq("ocr.results.test"), eq("doc-42"), payloadCaptor.capture());
        var json = payloadCaptor.getValue();
        assertThat(json).contains("\"status\":\"DONE\"");
        assertThat(json).contains("\"documentId\":\"doc-42\"");
        assertThat(json).contains("\"contentType\":\"application/pdf\"");
        assertThat(json).contains("EXTRACTED TEXT");

        // optional: also verify indexing happened
        verify(indexingService).indexDone(eq("doc-42"), eq(1L), anyString(), anyString(), anyLong(), anyString(), anyString());
    }

    @Test
    void missingContentType_headsFromS3() throws Exception {
        var e = event(null);

        when(storage.getContentType("docs/doc-42.pdf")).thenReturn("image/png");
        when(storage.load("docs/doc-42.pdf")).thenReturn(new byte[]{1,2,3});
        when(ocr.ocr(any(), eq("image/png"), eq("file.pdf"))).thenReturn("IMG TEXT");

        service.onDocumentUploaded(e);

        verify(storage).getContentType("docs/doc-42.pdf");
        verify(kafka).send(eq("ocr.results.test"), eq("doc-42"), payloadCaptor.capture());
        assertThat(payloadCaptor.getValue()).contains("\"status\":\"DONE\"");
        assertThat(payloadCaptor.getValue()).contains("\"contentType\":\"image/png\"");
    }

    @Test
    void onError_publishesERRORPayload() throws Exception {
        var e = event("application/pdf");

        when(storage.load(anyString())).thenReturn("PDF".getBytes());
        when(ocr.ocr(any(), any(), any())).thenThrow(new RuntimeException("boom"));

        service.onDocumentUploaded(e);

        verify(kafka).send(eq("ocr.results.test"), eq("doc-42"), payloadCaptor.capture());
        var json = payloadCaptor.getValue();
        assertThat(json).contains("\"status\":\"ERROR\"");
        assertThat(json).contains("\"error\":\"boom\"");
    }
}