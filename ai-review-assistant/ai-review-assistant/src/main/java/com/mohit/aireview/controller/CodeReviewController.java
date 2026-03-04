package com.mohit.aireview.controller;

import com.mohit.aireview.dto.*;
import com.mohit.aireview.service.CodeReviewService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api")
public class CodeReviewController {

    private final CodeReviewService service;

    public CodeReviewController(CodeReviewService service) {
        this.service = service;
    }

    @PostMapping("/analyze")
    public Mono<AnalyzeResponse> analyze(@RequestBody AnalyzeRequest request) {
        return service.analyze(
                request.getCode(),
                request.getContext()
        );
    }

    @PostMapping("/optimize")
    public Mono<OptimizeResponse> optimize(@RequestBody OptimizeRequest request) {
        return service.optimize(
                request.getCode(),
                request.getContext(),
                request.getCategory()
        );
    }
}