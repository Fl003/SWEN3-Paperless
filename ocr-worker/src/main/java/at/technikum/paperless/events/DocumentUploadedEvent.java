package at.technikum.paperless.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

//Represents the event published to Kafka after a document is uploaded
//Contains metadata needed by downstream services (OCR worker)

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentUploadedEvent {
    private String eventId;
    private Instant occurredAt;
    private String documentId;
    private String originalFilename;
    private String contentType;
    private String storagePath;
    private String uploadedBy;
    private Long ownerId;
    private String tenantId;
    private String traceId;
}
