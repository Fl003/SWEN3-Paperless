package at.technikum.paperless.genaiworker.client;

import at.technikum.paperless.genaiworker.service.GeminiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Base64;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeminiClient {
    private final WebClient webClient;
    private final String apiKey = System.getenv("GEMINI_API_KEY");
    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=%s";
    private final ObjectMapper objectMapper;

    // helper for fallback +logging
    private String performRequest(String requestJson, String logContext) {
        String url = API_URL.formatted(apiKey);

        try {
            log.info("Sending {} request to Gemini (payload {} chars)", logContext, requestJson.length());

            String rawResponse = webClient.post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestJson)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, r -> {
                        log.error("Gemini returned 4xx: {}", r.statusCode());
                        return r.createException();
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, r -> {
                        log.error("Gemini returned 5xx: {}", r.statusCode());
                        return r.createException();
                    })
                    .bodyToMono(String.class)
                    .block();

            log.info("Received Gemini response for {} ({} chars)", logContext,
                    rawResponse != null ? rawResponse.length() : 0);

            if (rawResponse == null) {
                log.error("Gemini returned NULL response");
                throw new GeminiException("Gemini returned NULL response");
            }

            GeminiApiResponse response = objectMapper.readValue(rawResponse, GeminiApiResponse.class);

            // Defensive extraction
            if (response.getCandidates() == null ||
                    response.getCandidates().isEmpty() ||
                    response.getCandidates().get(0).getContent() == null ||
                    response.getCandidates().get(0).getContent().getParts() == null ||
                    response.getCandidates().get(0).getContent().getParts().isEmpty()) {

                log.error("Gemini returned empty structure: {}", rawResponse);
                throw new GeminiException("Gemini returned empty structure");
            }

            return response.getCandidates()
                    .get(0)
                    .getContent()
                    .getParts()
                    .get(0)
                    .getText();

        } catch (WebClientResponseException e) {
            // HTTP error
            log.error("Gemini HTTP error: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new GeminiException("Gemini HTTP error: " + e.getStatusCode(), e);

        } catch (JsonProcessingException e) {
            // Parsing error
            log.error("Failed to parse Gemini response", e);
            throw new GeminiException("Failed to parse Gemini response", e);

        } catch (Exception e) {
            // Any unexpected error
            log.error("Unexpected error calling Gemini", e);
            throw new GeminiException("Unexpected Gemini error", e);
        }
    }

    public String requestSummery(String text) {
        String requestJson = """
                {
                  "contents": [{
                    "parts": [{
                      "text": "Summarize the following text in the same language as the original.\\nDo not change the language.\\n\\nText:\\n%s"
                    }]
                  }],
                  "generationConfig": {
                    "maxOutputTokens": 300
                  }
                }
        """.formatted(text.replace("\"", "\\\""));

        return performRequest(requestJson, "text-summary");
    }

    //für unter 20 MB Datei, sends content in request
    public String requestSummary(byte[] content, String contentType) {
        String url = API_URL.formatted(apiKey);
        String base64 = Base64.getEncoder().encodeToString(content);
        String requestJson = """
                {
                     "contents": [{
                       "parts": [{
                         "text": "Summarize the following document in the same language as the original.\\\\nDo not change the language.\\\\n"
                       },
                       {
                         "inline_data": {
                            "mime_type": "%s",
                            "data": "%s"
                         }
                       }]
                     }],
                     "generationConfig": {
                       "maxOutputTokens": 300
                     }
                   }""".formatted(contentType, base64);

        return performRequest(requestJson, "file-summary");
    }


}
