package com.personal.storagegate.controller;

import com.personal.storagegate.service.LocalLlmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class TestAiController {

    private final LocalLlmService llmService;

    public TestAiController(LocalLlmService llmService) {
        this.llmService = llmService;
    }

    @GetMapping("/api/test-llm")
    public String testLlm(@RequestParam(defaultValue = "Say hello in 5 words") String prompt) {
        log.info("Prompt: {}", prompt);
        return llmService.generateText(prompt);
    }

    @GetMapping("/api/models")
    public List<String> listModels() {
        return llmService.getAvailableModelIds();
    }
}