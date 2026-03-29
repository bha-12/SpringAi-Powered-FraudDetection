package com.springai.fraud.detection.model;

import com.springai.fraud.detection.config.FlexibleStringDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

@Data
public class FraudAnalysis {
    private String transactionId;
    private String riskLevel;
    private String action;        // BLOCK, FLAG, MONITOR, ALLOW

    @JsonDeserialize(using = FlexibleStringDeserializer.class)
    private String reason;

    private String recommendation;
    private double confidenceScore;

    @JsonDeserialize(using = FlexibleStringDeserializer.class)
    private String additionalComments;

    private String analyzedAt;
    private long processingTimeMs;
}