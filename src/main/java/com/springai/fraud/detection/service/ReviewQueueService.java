package com.springai.fraud.detection.service;

import com.springai.fraud.detection.model.CardAction;
import com.springai.fraud.detection.model.FraudReviewCase;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ReviewQueueService {

    private final Map<String, FraudReviewCase> queue
            = new ConcurrentHashMap<>();

    public FraudReviewCase addToQueue(FraudReviewCase reviewCase) {
        String caseId = "CASE-" + UUID.randomUUID()
                .toString().substring(0, 8).toUpperCase();
        reviewCase.setCaseId(caseId);
        reviewCase.setStatus("PENDING");
        reviewCase.setCreatedAt(LocalDateTime.now().toString());
        queue.put(caseId, reviewCase);

        System.out.println("📋 Added to review queue: "
                + caseId + " [" + reviewCase.getRiskLevel() + "]");
        return reviewCase;
    }

    public List<FraudReviewCase> getPendingCases() {
        return queue.values().stream()
                .filter(c -> "PENDING".equals(c.getStatus()))
                .sorted((a, b) -> {
                    int priorityA = getRiskPriority(a.getRiskLevel());
                    int priorityB = getRiskPriority(b.getRiskLevel());
                    return Integer.compare(priorityB, priorityA);
                })
                .toList();
    }

    public FraudReviewCase approveCase(
            String caseId, String reviewedBy) {

        FraudReviewCase reviewCase = queue.get(caseId);
        if (reviewCase == null) return null;

        reviewCase.setStatus("APPROVED");
        reviewCase.setReviewedBy(reviewedBy);
        reviewCase.setReviewedAt(LocalDateTime.now().toString());

        System.out.println("✅ Case approved by human: "
                + caseId + " → " + reviewCase.getAction());
        return reviewCase;
    }

    public FraudReviewCase rejectCase(
            String caseId, String reviewedBy) {

        FraudReviewCase reviewCase = queue.get(caseId);
        if (reviewCase == null) return null;

        reviewCase.setStatus("REJECTED");
        reviewCase.setReviewedBy(reviewedBy);
        reviewCase.setReviewedAt(LocalDateTime.now().toString());

        System.out.println("❌ Case rejected by human: "
                + caseId + " → No action taken");
        return reviewCase;
    }

    public FraudReviewCase getCaseById(String caseId) {
        return queue.get(caseId);
    }

    public int getPendingCount() {
        return (int) queue.values().stream()
                .filter(c -> "PENDING".equals(c.getStatus()))
                .count();
    }

    private int getRiskPriority(String riskLevel) {
        return switch (riskLevel) {
            case "CRITICAL" -> 4;
            case "HIGH"     -> 3;
            case "MEDIUM"   -> 2;
            case "LOW"      -> 1;
            default         -> 0;
        };
    }
}