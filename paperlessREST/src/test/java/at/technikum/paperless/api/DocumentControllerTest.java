package at.technikum.paperless.api;

import at.technikum.paperless.domain.Document;
import at.technikum.paperless.domain.User;
import at.technikum.paperless.service.DocumentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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

        MockMultipartFile file = new MockMultipartFile(
                "file",           // parameter name
                "test.pdf",       // original filename
                "application/pdf",// content type
                "test data".getBytes() // content
        );

        when(service.uploadFile(any(MultipartFile.class), anyList(), any(User.class))).thenReturn(entity);

        mvc.perform(multipart("/api/v1/documents")
                        .file(file)
                        .param("tags", "document")
                        .param("tags", "important"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location","/api/v1/documents/1"));
    }
}
