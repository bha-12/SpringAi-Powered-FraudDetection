package com.springai.fraud.detection.service;

import com.springai.fraud.detection.model.Transaction;
import org.springframework.stereotype.Component;

@Component
public class FraudRuleEngine {

    public RuleResult evaluate(Transaction transaction) {

        // Rule 1 — Extreme amount (50x average) → instant BLOCK
        if (transaction.getAmount() > transaction.getCustomerAverageSpend() * 50) {
            return RuleResult.block("Amount exceeds 50x customer average — instant block");
        }

        // Rule 2 — Known safe merchant + normal amount → instant ALLOW
        if (isSafeMerchant(transaction) &&
            transaction.getAmount() <= transaction.getCustomerAverageSpend() * 1.5) {
            return RuleResult.allow("Known safe merchant within normal spend range");
        }

        // Rule 3 — International + night + unknown merchant → instant BLOCK
        if (transaction.isInternational() &&
            isNightTime(transaction.getTimestamp()) &&
            isUnknownMerchant(transaction.getMerchantName())) {
            return RuleResult.block("International + night time + unknown merchant");
        }

        // Everything else → send to AI
        return RuleResult.sendToAi("No definitive rule match — requires AI analysis");
    }

    private boolean isSafeMerchant(Transaction t) {
        String merchant = t.getMerchantName().toLowerCase();
        return merchant.contains("walmart") ||
               merchant.contains("target") ||
               merchant.contains("amazon") ||
               merchant.contains("costco") ||
               merchant.contains("kroger");
    }

    private boolean isUnknownMerchant(String merchantName) {
        return merchantName.toLowerCase().contains("unknown");
    }

    private boolean isNightTime(String timestamp) {
        try {
            int hour = java.time.LocalDateTime.parse(timestamp).getHour();
            return hour >= 22 || hour <= 6;
        } catch (Exception e) {
            return false;
        }
    }

    // ── Inner result class ──────────────────────────────────
    public static class RuleResult {
        public enum Decision { BLOCK, ALLOW, SEND_TO_AI }

        private final Decision decision;
        private final String reason;

        private RuleResult(Decision decision, String reason) {
            this.decision = decision;
            this.reason = reason;
        }

        public static RuleResult block(String reason) {
            return new RuleResult(Decision.BLOCK, reason);
        }

        public static RuleResult allow(String reason) {
            return new RuleResult(Decision.ALLOW, reason);
        }

        public static RuleResult sendToAi(String reason) {
            return new RuleResult(Decision.SEND_TO_AI, reason);
        }

        public Decision getDecision() { return decision; }
        public String getReason() { return reason; }
    }
}