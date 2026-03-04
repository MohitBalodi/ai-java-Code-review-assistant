package com.mohit.aireview.dto;

public class OptimizeRequest {

    private String code;
    private String context;
    private String category; // NEW

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}