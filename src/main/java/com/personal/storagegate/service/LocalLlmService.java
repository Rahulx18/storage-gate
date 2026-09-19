package com.personal.storagegate.service;

import com.personal.storagegate.dto.llm.ChatCompletionRequest;
import com.personal.storagegate.dto.llm.ChatCompletionResponse;
import com.personal.storagegate.dto.llm.ModelListResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Service
public class LocalLlmService {

    private final RestClient restClient;
    private final String modelName;

    public LocalLlmService(
            @Value("${local-llm.base-url}") String baseUrl,
            @Value("${local-llm.model}") String modelName,
            @Value("${local-llm.api-key}") String apiKey) {

        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1) // force 1.1 for lm studio handshake hang
                .connectTimeout(Duration.ofSeconds(25))
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(15));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .requestInterceptor((request, body, execution) -> {
                    System.out.println("Calling: " + request.getMethod() + " " + request.getURI());
                    return execution.execute(request, body);
                })
                .build();
        this.modelName = modelName;
    }

    public String generateText(String prompt) {
        ChatCompletionRequest request = new ChatCompletionRequest(this.modelName, prompt);

        ChatCompletionResponse response = restClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(ChatCompletionResponse.class);

        if (response != null && !response.choices().isEmpty()) {
            return response.choices().getFirst().message().content();
        }
        return "";
    }

    public List<String> getAvailableModelIds() {
        System.out.println("Getting available model IDs...");
        ModelListResponse response = restClient.get()
                .uri("/models")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(ModelListResponse.class);

        System.out.println("Response: " + response);

        if (response != null && response.data() != null) {
            return response.data().stream()
                    .map(ModelListResponse.ModelData::id)
                    .toList();
        }
        return Collections.emptyList();
    }
}