package com.springai.fraud.detection.model;

import lombok.Data;

@Data
public class FraudReviewCase {
    private String caseId;
    private String transactionId;
    private String customerId;
    private String riskLevel;
    private String action;
    private double confidenceScore;
    private String aiReason;
    private String aiRecommendation;
    private String status;        // PENDING, APPROVED, REJECTED
    private String reviewedBy;
    private String createdAt;
    private String reviewedAt;
    private Transaction transaction;
    private Customer customer;
}