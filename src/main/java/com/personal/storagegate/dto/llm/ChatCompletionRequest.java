package com.personal.storagegate.dto.llm;

import java.util.List;

public class ChatCompletionRequest {
    public String model;
    public List<Message> messages;
    public double temperature = 0.7;

    public record Message(String role, String content) {}

    public ChatCompletionRequest(String model, String userPrompt) {
        this.model = model;
        this.messages = List.of(new Message("user", userPrompt));
    }
}