package at.technikum.paperless.genaiworker.kafka;

import at.technikum.paperless.genaiworker.service.GenAiService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentUploadListener {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final GenAiService genAiService;

    @Value("${APP_KAFKA_OUT_TOPIC:genai.results}")
    private String outTopic;

    @KafkaListener(topics = "${app.kafka.in-topic}", groupId = "${app.kafka.group}")
    public void onDocumentUploaded(DocumentUploadedEvent event) {
            log.info("Received DocumentUploadedEvent: {}", event);
            try {
                String summary = genAiService.handleDocumentUploadedEvent(event);
                SummaryResultEvent summaryResultEvent = new SummaryResultEvent(
                        event.getDocumentId(),
                        summary
                );
                log.info("onDocumentUploaded got result from genAiService: " + summary);
                kafkaTemplate.send(outTopic, objectMapper.writeValueAsString(summaryResultEvent));
            }catch (JsonProcessingException ex) {
                log.error("Failed to parse summaryResultEvent in String. error={}", ex.getMessage(), ex);
            }catch (Exception ex) {
                log.error("Unexpected error during OCR → GenAI processing. error={}",
                        ex.getMessage(), ex);
            }
        }
}
