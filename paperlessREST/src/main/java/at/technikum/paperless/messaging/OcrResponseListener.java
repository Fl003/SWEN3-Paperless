package at.technikum.paperless.messaging;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class OcrResponseListener {
    private static final Logger log = LoggerFactory.getLogger(OcrResponseListener.class);

    @KafkaListener(topics = "${paperless.kafka.in-topic}")
    public void handleOcrResponse(OcrResultEvent event) {
        log.info("OCR RESPONSE: docId={} status={} text={}",
                event.getDocumentId(),
                event.getStatus(),
                event.getText());
    }
}



