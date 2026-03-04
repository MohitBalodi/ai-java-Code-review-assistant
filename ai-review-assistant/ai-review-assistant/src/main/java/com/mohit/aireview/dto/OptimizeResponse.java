package com.mohit.aireview.dto;

public class OptimizeResponse {

    private String optimizedCode;
    private String optimizationExplanation;

    public String getOptimizedCode() {
        return optimizedCode;
    }

    public void setOptimizedCode(String optimizedCode) {
        this.optimizedCode = optimizedCode;
    }

    public String getOptimizationExplanation() {
        return optimizationExplanation;
    }

    public void setOptimizationExplanation(String optimizationExplanation) {
        this.optimizationExplanation = optimizationExplanation;
    }
}