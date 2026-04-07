package com.springai.fraud.detection.model;

import lombok.Data;

@Data
public class Customer {
    private String customerId;
    private String name;
    private String email;
    private String phone;
    private String homeLocation;
    private double averageMonthlySpend;
    private String riskCategory;    // LOW, MEDIUM, HIGH
    private boolean cardBlocked;
    private int totalTransactions;
}