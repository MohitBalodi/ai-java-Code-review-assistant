package com.mohit.aireview.client;

import com.mohit.aireview.dto.OllamaRequest;
import com.mohit.aireview.dto.OllamaResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Component
public class OllamaClient {

    private final WebClient webClient;

    public OllamaClient() {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:11434")
                .build();
    }

    public Mono<String> generate(String model, String prompt) {

        double temperature = model.contains("r1") ? 0.2 : 0.1;

        OllamaRequest request = new OllamaRequest(
                model,
                prompt,
                false,
                Map.of(
                        "temperature", temperature,
                        "top_p", 0.9
                )
        );

        return webClient.post()
                .uri("/api/generate")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OllamaResponse.class)
                .timeout(Duration.ofSeconds(240))  // 14B may need more time
                .doOnSubscribe(sub -> System.out.println("LLM call started: " + model))
                .doOnError(err -> System.out.println("LLM ERROR: " + err.getMessage()))
                .map(response -> {
                    if (response == null || response.getResponse() == null) {
                        throw new RuntimeException("Empty response from Ollama");
                    }
                    return response.getResponse();
                });
    }
}