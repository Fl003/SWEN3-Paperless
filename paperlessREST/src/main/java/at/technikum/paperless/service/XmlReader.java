package at.technikum.paperless.service;

import at.technikum.paperless.dto.DocumentDTO;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class XmlReader {
    public List<DocumentDTO> parseXmlFiles(String folderPath) {
        List<DocumentDTO> documents = new ArrayList<>();
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            throw new IllegalArgumentException("Invalid folder path: " + folderPath);
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".xml"));
        if (files == null) return documents;

        for (File file : files) {
            try {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document document = builder.parse(file);

                DocumentDTO dto = new DocumentDTO();
                dto.setDocumentId(getLongValue(document, "documentId"));
                dto.setAuthorId(getLongValue(document, "authorId"));
                dto.setName(getStringValue(document, "name"));
                dto.setContentType(getStringValue(document, "contentType"));
                dto.setStatus(getStringValue(document, "status"));
                dto.setCreatedAt(OffsetDateTime.parse(getStringValue(document, "createdAt")));
                dto.setLastEdited(OffsetDateTime.parse(getStringValue(document, "lastEdited")));
                dto.setTags(getListValue(document, "tags"));

                documents.add(dto);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return documents;
    }

    private String getStringValue(Document doc, String tagName) {
        NodeList nodes = doc.getElementsByTagName(tagName);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent() : null;
    }

    private Long getLongValue(Document doc, String tagName) {
        String value = getStringValue(doc, tagName);
        return value != null ? Long.parseLong(value) : null;
    }

    private List<String> getListValue(Document doc, String tagName) {
        NodeList nodes = doc.getElementsByTagName(tagName);
        List<String> tags = new ArrayList<>();
        if (nodes.getLength() > 0) {
            NodeList childNodes = nodes.item(0).getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node item = childNodes.item(i);
                if (item.getNodeType() == Node.ELEMENT_NODE) {
                    tags.add(item.getTextContent());
                }
            }
        }
        return tags;
    }
}