package at.technikum.paperless.genaiworker.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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



    public String requestSummery(String text) {
        String url = API_URL.formatted(apiKey);

        String requestJson = """
                {
                  "contents": [{
                    "parts": [{
                      "text": "Summarize the following text in the same language as the original.\\\\nDo not change the language.\\\\n\\\\nText:\\\\n%s"
                    }]
                  }],
                  "generationConfig": {
                    "maxOutputTokens": 300
                  }
                }
        """.formatted(text.replace("\"", "\\\""));
        log.info("Sending request to Gemini: {} with payload length {}", url, requestJson.length());
        try {
            String rawResponse = webClient.post()
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
            log.info("Received response from Gemini ({} chars)", rawResponse != null ? rawResponse.length() : 0);
            GeminiApiResponse response = objectMapper.readValue(rawResponse, GeminiApiResponse.class);
            return response.getCandidates().getFirst().getContent().getParts().getFirst().getText();
        }catch(WebClientResponseException e) {
            log.error("Gemini API error: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        }catch (JsonProcessingException e) {
            log.error("Failed to parse Gemini response", e);
            return "no gemini today blat";
        } catch(Exception e) {
            log.error("Failed to call Gemini API", e);
            throw e;
        }
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

        try {
            String rawResponse = webClient.post()
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
            log.info("Received response from Gemini ({} chars)", rawResponse != null ? rawResponse.length() : 0);
            GeminiApiResponse response = objectMapper.readValue(rawResponse, GeminiApiResponse.class);
            return response.getCandidates().getFirst().getContent().getParts().getFirst().getText();
        }catch(WebClientResponseException e) {
            log.error("Gemini API error: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        }catch (JsonProcessingException e) {
            log.error("Failed to parse Gemini response", e);
            return "no gemini today blat";
        } catch(Exception e) {
            log.error("Failed to call Gemini API", e);
            throw e;
        }
    }


}
