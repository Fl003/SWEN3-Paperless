package at.technikum.paperless.api;

import at.technikum.paperless.domain.Document;
import at.technikum.paperless.service.DocumentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DocumentController.class)
class DocumentControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean DocumentService service;

    @Test void postCreates201() throws Exception {
        var now = OffsetDateTime.now();
        var entity = Document.builder()
                .id(1L).name("contract.pdf").contentType("application/pdf").sizeBytes(100L)
                .status("uploaded").createdAt(now).lastEdited(now).build();

        when(service.create(any(), any(), anyLong(), any())).thenReturn(entity);

        var req = Map.of("name","contract.pdf","contentType","application/pdf","sizeBytes",100);

        mvc.perform(post("/api/v1/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location","/api/v1/documents/1"));
    }
}
