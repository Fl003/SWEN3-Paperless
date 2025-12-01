package at.technikum.paperless.genaiworker.kafka;

import at.technikum.paperless.genaiworker.service.GeminiException;
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
            long start = System.currentTimeMillis();
            log.info("Received DocumentUploadedEvent: {}", event);

        try {
            // 1. ask genai for summary
            String summary = genAiService.handleDocumentUploadedEvent(event);
            long duration = System.currentTimeMillis() - start;

            // 2. build success event
            SummaryResultEvent result = SummaryResultEvent.builder()
                    .eventId(event.getEventId())
                    .documentId(event.getDocumentId())
                    .traceId(event.getTraceId())
                    .summary(summary)
                    .status("DONE")
                    .durationMs(duration)
                    .errorMessage(null)
                    .build();

            log.info("GenAI summary DONE for documentId={} (duration={} ms)",
                    event.getDocumentId(), duration);

            sendResult(result);

        } catch (GeminiException e) {
            long duration = System.currentTimeMillis() - start;

            // 3) build error event for genai
            SummaryResultEvent result = SummaryResultEvent.builder()
                    .eventId(event.getEventId())
                    .documentId(event.getDocumentId())
                    .traceId(event.getTraceId())
                    .summary(null)
                    .status("ERROR")
                    .durationMs(duration)
                    .errorMessage(e.getMessage())
                    .build();

            log.error("GenAI summary ERROR for documentId={} (duration={} ms)",
                    event.getDocumentId(), duration, e);

            sendResult(result);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;

            // 4. build error event for  unexpected problem
            SummaryResultEvent result = SummaryResultEvent.builder()
                    .eventId(event.getEventId())
                    .documentId(event.getDocumentId())
                    .traceId(event.getTraceId())
                    .summary(null)
                    .status("ERROR")
                    .durationMs(duration)
                    .errorMessage("Unexpected error: " + e.getMessage())
                    .build();

            log.error("Unexpected error while handling DocumentUploadedEvent for documentId={} (duration={} ms)",
                    event.getDocumentId(), duration, e);

            sendResult(result);
        }
    }

    private void sendResult(SummaryResultEvent result) {
        try {
            String payload = objectMapper.writeValueAsString(result);
            kafkaTemplate.send(outTopic, payload);
            log.info("SummaryResultEvent sent to topic {} for documentId={} with status={}",
                    outTopic, result.getDocumentId(), result.getStatus());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize SummaryResultEvent for documentId={}",
                    result.getDocumentId(), e);
        }
    }
}
