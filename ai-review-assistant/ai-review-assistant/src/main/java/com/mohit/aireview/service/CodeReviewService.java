package com.mohit.aireview.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.ParseResult;
import com.mohit.aireview.client.OllamaClient;
import com.mohit.aireview.dto.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import com.github.javaparser.JavaParser;

import com.github.javaparser.ast.CompilationUnit;


@Service
public class CodeReviewService {

    private static final String ANALYSIS_MODEL = "deepseek-r1:7b";
    private static final String OPTIMIZE_MODEL = "qwen2.5-coder:14b";

    private final OllamaClient ollamaClient;
    private final ObjectMapper objectMapper;

    public CodeReviewService(OllamaClient ollamaClient,
                             ObjectMapper objectMapper) {
        this.ollamaClient = ollamaClient;
        this.objectMapper = objectMapper;
    }

    // ============================
    // STEP 1 — ANALYZE
    // ============================

    private boolean isValidJavaFlexible(String code) {

        JavaParser parser = new JavaParser();

        // 1️⃣ Try full compilation unit
        try {
            ParseResult<CompilationUnit> result = parser.parse(code);
            if (result.isSuccessful() && result.getResult().isPresent()) {
                return true;
            }
        } catch (Exception ignored) {}

        // 2️⃣ Try as class body
        try {
            String wrappedClass = """
            public class TempWrapper {
        """ + code + """
            }
        """;

            ParseResult<CompilationUnit> result = parser.parse(wrappedClass);
            if (result.isSuccessful() && result.getResult().isPresent()) {
                return true;
            }
        } catch (Exception ignored) {}

        // 3️⃣ Try as method body
        try {
            String wrappedMethod = """
            public class TempWrapper {
                public void tempMethod() {
        """ + code + """
                }
            }
        """;

            ParseResult<CompilationUnit> result = parser.parse(wrappedMethod);
            if (result.isSuccessful() && result.getResult().isPresent()) {
                return true;
            }
        } catch (Exception ignored) {}

        return false;
    }

    public Mono<AnalyzeResponse> analyze(String code, String context) {

        if (code == null || code.trim().isEmpty()) {
            return Mono.just(fallbackAnalyze("No valid Java code provided"));
        }

        if (code.length() > 10000) {
            return Mono.just(fallbackAnalyze("Input too large. Please limit to 10,000 characters."));
        }

        if (!isValidJavaFlexible(code)) {
            return Mono.just(buildSyntaxErrorResponse());
        }

        String prompt = buildAnalyzePrompt(code, context);

        return ollamaClient.generate(ANALYSIS_MODEL, prompt)
                .map(raw -> {
                    System.out.println("RAW OPTIMIZE RESPONSE:");
                    System.out.println(raw);
                    return raw;
                })
                .map(this::sanitize)
                .map(this::parseAnalyzeJson)
                .onErrorResume(e ->
                        Mono.just(fallbackAnalyze("Model timeout or failure"))
                );
    }

    private AnalyzeResponse buildSyntaxErrorResponse() {
        AnalyzeResponse res = new AnalyzeResponse();
        res.setSummary("Invalid Java syntax");
        res.setTimeComplexity("N/A");
        res.setSpaceComplexity("N/A");
        res.setCategory("SYNTAX_INVALID");
        res.setIssues(List.of("Code contains syntax errors"));
        return res;
    }

    private AnalyzeResponse parseAnalyzeJson(String rawResponse) {
        try {
            String json = extractJson(rawResponse);
            AnalyzeResponse response =
                    objectMapper.readValue(json, AnalyzeResponse.class);

            validateAnalyzeResponse(response);
            return response;

        } catch (Exception e) {
            return fallbackAnalyze("Model returned invalid or malformed response");
        }
    }

    private void validateAnalyzeResponse(AnalyzeResponse response) {

        if (response.getSummary() == null ||
                response.getTimeComplexity() == null ||
                response.getSpaceComplexity() == null ||
                response.getCategory() == null ||
                response.getIssues() == null) {

            throw new RuntimeException("Invalid analyze response structure");
        }

        // Validate category
        try {
            AnalysisCategory.valueOf(response.getCategory());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid category returned by model");
        }

        // Enforce logic consistency
        if (response.getCategory().equals("CLEAN") && !response.getIssues().isEmpty()) {
            response.setCategory("INEFFICIENCY");
        }

        if (!response.getCategory().equals("CLEAN") && response.getIssues().isEmpty()) {
            response.setIssues(List.of("Unspecified issue detected"));
        }
    }

    private AnalyzeResponse fallbackAnalyze(String reason) {
        AnalyzeResponse fallback = new AnalyzeResponse();
        fallback.setSummary(reason);
        fallback.setTimeComplexity("N/A");
        fallback.setSpaceComplexity("N/A");
        fallback.setIssues(List.of("Model formatting failure"));
        return fallback;
    }
    private String buildAnalyzePrompt(String code, String context) {

        String contextBlock = "";

        if (context != null && !context.trim().isEmpty()) {
            contextBlock = """
Additional context:
""" + context + """

Use this only to understand intent.
Do NOT change the JSON structure.
""";
        }

        return """
You are a strict Java analyzer.

Return EXACTLY ONE JSON object.
Return ONLY JSON.
No markdown.
No backticks.
No explanations outside JSON.

You MUST assign exactly ONE category from:
BUG
INEFFICIENCY
SMELL
CLEAN

Category rules:

BUG:
- Logical errors
- Boundary errors
- Infinite loops
- Risk of runtime exception

INEFFICIENCY:
- Avoidable repeated work
- Avoidable higher complexity
- Recursive exponential patterns
- Repeated addition for multiplication

SMELL:
- Poor practice
- Redundant object creation
- Minor style inefficiencies

CLEAN:
- Correct and reasonably efficient

If multiple apply, choose the most severe:
BUG > INEFFICIENCY > SMELL > CLEAN

Required format:

{
  "summary": "short explanation",
  "timeComplexity": "O(...)",
  "spaceComplexity": "O(...)",
  "category": "BUG | INEFFICIENCY | SMELL | CLEAN",
  "issues": ["issue1", "issue2"]
}

""" + contextBlock + """

Analyze this Java code:

""" + code;
    }

    // ============================
    // STEP 2 — OPTIMIZE
    // ============================

    public Mono<OptimizeResponse> optimize(
            String code, String context, String category) {

        String prompt = buildOptimizePrompt(code, context, category);

        return ollamaClient.generate(OPTIMIZE_MODEL,prompt)
                .doOnSubscribe(sub -> System.out.println("Optimize request started"))
                .doOnError(err -> System.out.println("Optimize error: " + err.getMessage()))
                .map(raw -> {
                    System.out.println("RAW OPTIMIZE RESPONSE:");
                    System.out.println(raw);
                    return raw;
                })
                .map(this::parseOptimizeResponse)
                .onErrorResume(e -> {
                    System.out.println("FALLBACK triggered");
                    return Mono.just(fallbackOptimize());
                });
    }

    private OptimizeResponse parseOptimizeResponse(String raw) {

        try {
            String explanation = "";
            String optimizedCode = "";

            int explanationIndex = raw.indexOf("Explanation:");
            int improvedIndex = raw.indexOf("Improved Code:");

            if (improvedIndex != -1) {

                if (explanationIndex != -1) {
                    explanation = raw.substring(
                            explanationIndex + "Explanation:".length(),
                            improvedIndex
                    ).trim();
                }

                optimizedCode = raw.substring(
                        improvedIndex + "Improved Code:".length()
                ).trim();
            }

            if (optimizedCode.isEmpty()) {
                return fallbackOptimize();
            }

            OptimizeResponse response = new OptimizeResponse();
            response.setOptimizedCode(optimizedCode);
            response.setOptimizationExplanation(explanation);

            return response;

        } catch (Exception e) {
            return fallbackOptimize();
        }
    }

    private OptimizeResponse fallbackOptimize() {
        OptimizeResponse fallback = new OptimizeResponse();
        fallback.setOptimizedCode("");
        fallback.setOptimizationExplanation("Model failed to generate optimized version.");
        return fallback;
    }

    private String buildOptimizePrompt(String code,
                                       String context,
                                       String category) {

        String taskInstruction;

        switch (category) {
            case "BUG":
                taskInstruction = "Fix correctness issues in the code while preserving intent.";
                break;
            case "INEFFICIENCY":
                taskInstruction = "Improve performance without changing logic.";
                break;
            case "SMELL":
                taskInstruction = "Improve code quality and best practices without altering logic.";
                break;
            default:
                taskInstruction = "Improve the code.";
        }

        String contextBlock = "";

        if (context != null && !context.trim().isEmpty()) {
            contextBlock = """
Additional context:
""" + context + """
""";
        }

        return """
You are a senior Java engineer.

""" + taskInstruction + """

Respond EXACTLY in this format:

Explanation:
<short explanation>

Improved Code:
<only improved method(s)>

Do NOT use markdown.
Do NOT use backticks.
Do NOT include package or class declarations.
Do NOT rename methods.

""" + contextBlock + """

Java code:

""" + code;
    }

    // ============================
    // COMMON UTILITIES
    // ============================

    private String extractJson(String raw) {
        int first = raw.indexOf("{");
        if (first == -1) {
            throw new RuntimeException("No JSON start found");
        }

        int braces = 0;
        for (int i = first; i < raw.length(); i++) {
            if (raw.charAt(i) == '{') braces++;
            if (raw.charAt(i) == '}') braces--;

            if (braces == 0) {
                return raw.substring(first, i + 1);
            }
        }

        throw new RuntimeException("No complete JSON object found");
    }

    private String sanitize(String raw) {
        raw = raw
                .replace("```json", "")
                .replace("```java", "")
                .replace("```", "")
                .trim();

        int firstBrace = raw.indexOf("{");
        if (firstBrace > 0) {
            raw = raw.substring(firstBrace);
        }

        return raw.trim();
    }

    private String formatIssues(List<String> issues) {
        if (issues == null || issues.isEmpty()) return "None";

        StringBuilder sb = new StringBuilder();
        for (String issue : issues) {
            sb.append("- ").append(issue).append("\n");
        }
        return sb.toString();
    }

}