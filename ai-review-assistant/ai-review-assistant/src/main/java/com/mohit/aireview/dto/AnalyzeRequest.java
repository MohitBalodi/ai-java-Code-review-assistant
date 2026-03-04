package com.mohit.aireview.dto;

public class AnalyzeRequest {

    private String code;
    private String context; // NEW (optional)

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}