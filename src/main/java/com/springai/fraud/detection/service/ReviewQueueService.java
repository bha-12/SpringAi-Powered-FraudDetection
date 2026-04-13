package com.springai.fraud.detection.service;

import com.springai.fraud.detection.model.FraudReviewCase;
import com.springai.fraud.detection.repository.FraudReviewCaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ReviewQueueService {

    private static final Logger log =
            LoggerFactory.getLogger(ReviewQueueService.class);

    private final FraudReviewCaseRepository repository;

    public ReviewQueueService(FraudReviewCaseRepository repository) {
        this.repository = repository;
    }

    public FraudReviewCase addToQueue(FraudReviewCase reviewCase) {
        String caseId = "CASE-" + UUID.randomUUID()
                .toString().substring(0, 8).toUpperCase();
        reviewCase.setCaseId(caseId);
        reviewCase.setStatus("PENDING");
        reviewCase.setCreatedAt(LocalDateTime.now().toString());

        // Save to MongoDB instead of HashMap
        FraudReviewCase saved = repository.save(reviewCase);

        log.info("📋 Added to MongoDB queue: {} [{}]",
                caseId, reviewCase.getRiskLevel());
        return saved;
    }

    public List<FraudReviewCase> getPendingCases() {
        // MongoDB query — no manual filtering needed!
        return repository.findByStatusOrderByRiskLevelDesc("PENDING");
    }

    public FraudReviewCase approveCase(
            String caseId, String reviewedBy) {

        FraudReviewCase reviewCase = repository
                .findById(caseId)
                .orElse(null);

        if (reviewCase == null) return null;

        reviewCase.setStatus("APPROVED");
        reviewCase.setReviewedBy(reviewedBy);
        reviewCase.setReviewedAt(LocalDateTime.now().toString());

        // Save updated case back to MongoDB
        FraudReviewCase saved = repository.save(reviewCase);

        log.info("✅ Case approved: {} by {}", caseId, reviewedBy);
        return saved;
    }

    public FraudReviewCase rejectCase(
            String caseId, String reviewedBy) {

        FraudReviewCase reviewCase = repository
                .findById(caseId)
                .orElse(null);

        if (reviewCase == null) return null;

        reviewCase.setStatus("REJECTED");
        reviewCase.setReviewedBy(reviewedBy);
        reviewCase.setReviewedAt(LocalDateTime.now().toString());

        FraudReviewCase saved = repository.save(reviewCase);

        log.info("❌ Case rejected: {} by {}", caseId, reviewedBy);
        return saved;
    }

    public int getPendingCount() {
        return repository.countByStatus("PENDING");
    }
}