package com.springai.fraud.detection.controller;

import com.springai.fraud.detection.model.FraudReviewCase;
import com.springai.fraud.detection.service.FraudAgentService;
import com.springai.fraud.detection.service.ReviewQueueService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agent")
public class FraudAgentController {

    private final FraudAgentService fraudAgentService;
    private final ReviewQueueService reviewQueueService;

    public FraudAgentController(
            FraudAgentService fraudAgentService,
            ReviewQueueService reviewQueueService) {
        this.fraudAgentService = fraudAgentService;
        this.reviewQueueService = reviewQueueService;
    }

    @PostMapping("/investigate")
    public String investigate(@RequestBody String request) {
        return fraudAgentService.investigate(request);
    }

    @GetMapping("/queue")
    public List<FraudReviewCase> getPendingCases() {
        return reviewQueueService.getPendingCases();
    }

    @GetMapping("/queue/count")
    public int getPendingCount() {
        return reviewQueueService.getPendingCount();
    }

    @PostMapping("/queue/{caseId}/approve")
    public FraudReviewCase approveCase(
            @PathVariable String caseId,
            @RequestParam String reviewedBy) {
        return reviewQueueService.approveCase(caseId, reviewedBy);
    }

    @PostMapping("/queue/{caseId}/reject")
    public FraudReviewCase rejectCase(
            @PathVariable String caseId,
            @RequestParam String reviewedBy) {
        return reviewQueueService.rejectCase(caseId, reviewedBy);
    }
}