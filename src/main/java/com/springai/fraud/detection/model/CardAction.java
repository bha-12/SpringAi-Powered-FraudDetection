package com.springai.fraud.detection.model;

import lombok.Data;

@Data
public class CardAction {
    private String customerId;
    private String action;          // BLOCKED, ALERTED, MONITORED
    private String reason;
    private String timestamp;
    private boolean success;
}