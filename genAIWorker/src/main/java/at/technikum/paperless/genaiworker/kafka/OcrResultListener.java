package at.technikum.paperless.genaiworker.kafka;

import at.technikum.paperless.genaiworker.service.GenAiService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OcrResultListener {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final GenAiService genAiService;

    @Value("вваваа")
    private String outTopic;
    //to-do should not listen for oce, but for file ulpload
//    @KafkaListener(topics = "${paperless.kafka.in-topic}", groupId = "genai-worker")
//    public void handleOcrResponse(String message) {
//        log.info("handleOcrResponse");
//        try{
//            OcrResultEvent event = objectMapper.readValue(message, OcrResultEvent.class);
//            String summery = genAiService.generateSummery(event.getText());
//            SummaryResultEvent resultEvent = new SummaryResultEvent(
//                event.getDocumentId(),
//                summery
//            );
//            log.info("handleOcrResponse got result from genAiService: " + summery);
//            kafkaTemplate.send(outTopic, objectMapper.writeValueAsString(resultEvent));
//        } catch (JsonProcessingException ex) {
//            log.error("Failed to parse OCR message into OcrResultEvent. Raw message={}", message, ex);
//        }catch (Exception ex) {
//            log.error("Unexpected error during OCR → GenAI processing. message={}, error={}",
//                    message, ex.getMessage(), ex);
//        }
//    }
}
