package com.springai.fraud.detection.model;

import lombok.Data;

@Data
public class Transaction {
    private String transactionId;
    private String customerId;
    private double amount;
    private String merchantName;
    private String merchantCategory;
    private String transactionLocation;
    private String customerHomeLocation;
    private String timestamp;
    private double customerAverageSpend;
    private boolean isInternational;
}