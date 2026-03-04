package com.mohit.aireview.dto;

import java.util.Map;

public class OllamaRequest {

    private String model;
    private String prompt;
    private boolean stream;
    private Map<String, Object> options;

    public OllamaRequest(String model, String prompt, boolean stream, Map<String, Object> options) {
        this.model = model;
        this.prompt = prompt;
        this.stream = stream;
        this.options = options;
    }

    public String getModel() { return model; }
    public String getPrompt() { return prompt; }
    public boolean isStream() { return stream; }
    public Map<String, Object> getOptions() { return options; }
}