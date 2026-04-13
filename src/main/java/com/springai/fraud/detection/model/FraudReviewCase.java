package com.springai.fraud.detection.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = "fraud_cases")  // ← maps to MongoDB collection
public class FraudReviewCase {

    @Id  // ← maps to MongoDB _id field
    private String caseId;

    private String transactionId;
    private String customerId;
    private String riskLevel;
    private String action;
    private Double confidenceScore;
    private String aiReason;
    private String aiRecommendation;
    private String status;
    private String reviewedBy;
    private String createdAt;
    private String reviewedAt;
    private Transaction transaction;
    private Customer customer;
}