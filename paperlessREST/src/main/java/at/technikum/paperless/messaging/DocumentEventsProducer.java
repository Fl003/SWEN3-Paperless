package at.technikum.paperless.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class DocumentEventsProducer {
    private static final Logger log = LoggerFactory.getLogger(DocumentEventsProducer.class);

    private final KafkaTemplate<String, DocumentUploadedEvent> kafka;
    private final String topic;

    public DocumentEventsProducer(
            KafkaTemplate<String, DocumentUploadedEvent> kafka,
            @Value("${paperless.kafka.topic}") String topic) {
        this.kafka = kafka;
        this.topic = topic;
    }

    public void publish(DocumentUploadedEvent event) {
        String key = event.getDocumentId(); // good for partitioning by doc
        log.info("Publishing DocumentUploadedEvent docId={} eventId={} to topic={}", key, event.getEventId(), topic);

        kafka.send(topic, key, event).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish eventId={} docId={} : {}", event.getEventId(), key, ex.toString());
            } else {
                var md = result.getRecordMetadata();
                log.info("Published eventId={} to {}-{}@offset {}", event.getEventId(), md.topic(), md.partition(), md.offset());
            }
        });
    }
}
