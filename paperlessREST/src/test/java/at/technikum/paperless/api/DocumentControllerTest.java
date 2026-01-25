package at.technikum.paperless.api;

import at.technikum.paperless.domain.Document;
import at.technikum.paperless.domain.User;
import at.technikum.paperless.dto.DocumentDTO;
import at.technikum.paperless.mapper.DocumentMapper;
import at.technikum.paperless.service.DocumentSearchService;
import at.technikum.paperless.service.DocumentService;
import at.technikum.paperless.utils.UserUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DocumentController.class)
@AutoConfigureMockMvc(addFilters = false)
class DocumentControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    DocumentService service;

    @MockitoBean
    DocumentSearchService documentSearchService;

    @MockitoBean
    DocumentMapper mapper;

    @MockitoBean
    UserUtils userUtils;

    @Test
    void postCreates201() throws Exception {
        var now = OffsetDateTime.now();

        User currentUser = mock(User.class);
        when(currentUser.getId()).thenReturn(99L);
        when(currentUser.getUsername()).thenReturn("testuser");

        when(userUtils.getCurrentUser()).thenReturn(currentUser);

        var entity = Document.builder()
                .id(1L)
                .name("contract.pdf")
                .contentType("application/pdf")
                .sizeBytes(100L)
                .status("uploaded")
                .createdAt(now)
                .lastEdited(now)
                .build();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "test data".getBytes()
        );

        when(service.uploadFile(any(MultipartFile.class), anyList(), any(User.class)))
                .thenReturn(entity);

        // Controller returns mapper.map(document)
        when(mapper.map(any(Document.class))).thenReturn(new DocumentDTO());

        mvc.perform(multipart("/api/v1/documents")
                        .file(file)
                        .param("tags", "document")
                        .param("tags", "important"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/documents/1"));
    }
}