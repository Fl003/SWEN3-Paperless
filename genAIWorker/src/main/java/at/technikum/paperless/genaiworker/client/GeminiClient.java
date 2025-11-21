package at.technikum.paperless.genaiworker.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class GeminiClient {
    private final WebClient webClient;
    private final String apiKey = System.getenv("GEMINI_API_KEY");
    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=%s";

    public String requestSummery(String text) {

        /*
        String url = API_URL.formatted(apiKey);

        String requestJson = """
        {
          "contents": [{
            "parts": [{ "text": "%s" }]
          }]
        }
        """.formatted(text.replace("\"", "\\\""));

        return webClient.post()
                .uri(url)
                .header("Content-Type", "application/json")
                .bodyValue(requestJson)
                .retrieve()//if 2xx
                .bodyToMono(String.class)
                .block();

         */
        return "summery";
    }


}
