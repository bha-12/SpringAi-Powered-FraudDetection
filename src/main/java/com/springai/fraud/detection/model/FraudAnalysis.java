package com.springai.fraud.detection.model;

import com.springai.fraud.detection.config.FlexibleStringDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

@Data
public class FraudAnalysis {
    private String transactionId;
    private String riskLevel;

    @JsonDeserialize(using = FlexibleStringDeserializer.class)
    private String reason;

    private String recommendation;
    private double confidenceScore;
    private String additionalComments;
}