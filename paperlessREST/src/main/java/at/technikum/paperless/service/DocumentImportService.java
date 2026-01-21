package at.technikum.paperless.service;

import at.technikum.paperless.domain.Document;
import at.technikum.paperless.domain.Tag;
import at.technikum.paperless.domain.User;
import at.technikum.paperless.dto.DocumentDTO;
import at.technikum.paperless.repository.DocumentRepository;
import at.technikum.paperless.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class DocumentImportService {

    private final XmlReader xmlReader;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private TagRepository tags;

    public DocumentImportService() {
        this.xmlReader = new XmlReader();
    }

    public void importAndSaveDocuments(String folderPath) {
        List<DocumentDTO> documents = xmlReader.parseXmlFiles(folderPath);
        if (documents.isEmpty()) {
            return;
        }

        saveDocuments(documents);
    }


    public void saveDocuments(List<DocumentDTO> documents) {
        for (DocumentDTO dto : documents) {
            User user = new User();
            user.setId(dto.getAuthorId());

            var document = Document.builder()
                    .name(dto.getName())
                    .author(user)
                    .contentType(dto.getContentType())
                    .status("uploaded")
                    .createdAt(OffsetDateTime.now())
                    .lastEdited(OffsetDateTime.now())
                    .build();

            if (dto.getTags() != null && !dto.getTags().isEmpty()) {
                for (String tagName : dto.getTags()) {
                    var tag = tags.findByNameIgnoreCase(tagName)
                            .orElseGet(() -> tags.save(Tag.builder().name(tagName.trim()).build()));
                    document.getTags().add(tag);
                }
            }

            documentRepository.save(document);
        }
    }
}
