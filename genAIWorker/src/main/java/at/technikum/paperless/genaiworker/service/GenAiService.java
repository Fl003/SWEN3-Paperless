package at.technikum.paperless.genaiworker.service;

import at.technikum.paperless.genaiworker.client.GeminiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class GenAiService {
    private final GeminiClient geminiClient;
    public String generateSummery(String text) {
        return geminiClient.requestSummery(text);
    }
}
