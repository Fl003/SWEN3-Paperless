package at.technikum.paperless.genaiworker.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeminiClient {
    private final WebClient webClient;
    private final String apiKey = System.getenv("GEMINI_API_KEY");
    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=%s";

    public String requestSummery(String text) {
        String url = API_URL.formatted(apiKey);

        String requestJson = """
        {
          "contents": [{
            "parts": [{ "text": "%s" }]
          }]
        }
        """.formatted(text.replace("\"", "\\\""));
        log.info("Sending request to Gemini: {} with payload length {}", url, requestJson.length());
        try {
            String response = webClient.post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestJson)
                    .retrieve()//if 2xx
                    .onStatus(status -> status.is4xxClientError(), r -> {
                        log.error("Gemini returned 4xx error");
                        return r.createException();
                    })
                    .onStatus(status -> status.is5xxServerError(), r -> {
                        log.error("Gemini returned 5xx error");
                        return r.createException();
                    })
                    .bodyToMono(String.class)
                    .block();
            log.info("Received response from Gemini ({} chars)", response != null ? response.length() : 0);
            return response;
        }catch(WebClientResponseException e) {
            log.error("Gemini API error: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
        catch(Exception e) {
            log.error("Failed to call Gemini API", e);
            throw e;
        }
    }


}
