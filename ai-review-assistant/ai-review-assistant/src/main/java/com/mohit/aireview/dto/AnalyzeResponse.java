package com.mohit.aireview.dto;

import java.util.List;

public class AnalyzeResponse {

    private String summary;
    private String timeComplexity;
    private String spaceComplexity;
    private String category; // NEW
    private List<String> issues;

    // getters and setters

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getTimeComplexity() { return timeComplexity; }
    public void setTimeComplexity(String timeComplexity) { this.timeComplexity = timeComplexity; }

    public String getSpaceComplexity() { return spaceComplexity; }
    public void setSpaceComplexity(String spaceComplexity) { this.spaceComplexity = spaceComplexity; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<String> getIssues() { return issues; }
    public void setIssues(List<String> issues) { this.issues = issues; }
}