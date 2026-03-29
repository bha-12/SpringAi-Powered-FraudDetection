package com.springai.fraud.detection.service;

import com.springai.fraud.detection.model.FraudAnalysis;
import com.springai.fraud.detection.model.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FraudDetectionService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final FraudRuleEngine ruleEngine;

    public FraudDetectionService(
            ChatClient.Builder builder,
            ObjectMapper objectMapper,
            FraudRuleEngine ruleEngine,
            @Value("${fraud.detection.system.prompt}") String systemPrompt) {

        this.objectMapper = objectMapper;
        this.ruleEngine = ruleEngine;
        this.chatClient = builder
                .defaultSystem(systemPrompt)
                .build();
    }

    public FraudAnalysis analyze(Transaction transaction) {
        long startTime = System.currentTimeMillis();

        // Layer 1 — Rule Engine
        FraudRuleEngine.RuleResult ruleResult = ruleEngine.evaluate(transaction);

        if (ruleResult.getDecision() == FraudRuleEngine.RuleResult.Decision.BLOCK) {
            return buildRuleBasedResult(transaction, "CRITICAL", "BLOCK",
                    ruleResult.getReason(), startTime, "RULE_ENGINE");
        }

        if (ruleResult.getDecision() == FraudRuleEngine.RuleResult.Decision.ALLOW) {
            return buildRuleBasedResult(transaction, "LOW", "ALLOW",
                    ruleResult.getReason(), startTime, "RULE_ENGINE");
        }

        // Layer 2 — AI Analysis
        String aiResponse = chatClient
                .prompt()
                .user(buildPrompt(transaction))
                .call()
                .content();

        FraudAnalysis result = parseResponse(aiResponse, transaction.getTransactionId());
        result.setDetectionMethod("AI_ANALYSIS");
        result.setAnalyzedAt(java.time.LocalDateTime.now().toString());
        result.setProcessingTimeMs(System.currentTimeMillis() - startTime);
        return result;
    }

    public List<FraudAnalysis> analyzeBatch(List<Transaction> transactions) {
        return transactions.parallelStream()
                .map(this::analyze)
                .collect(java.util.stream.Collectors.toList());
    }

    private FraudAnalysis buildRuleBasedResult(Transaction transaction,
                                               String riskLevel, String action, String reason,
                                               long startTime, String method) {

        FraudAnalysis result = new FraudAnalysis();
        result.setTransactionId(transaction.getTransactionId());
        result.setRiskLevel(riskLevel);
        result.setAction(action);
        result.setDetectionMethod(method);
        result.setReason(reason);
        result.setRecommendation(
                action.equals("BLOCK")
                        ? "Block card immediately and alert customer"
                        : "Transaction approved — no action needed"
        );
        result.setConfidenceScore(1.0);
        result.setAdditionalComments("Decision made by rule engine — no AI call needed");
        result.setAnalyzedAt(java.time.LocalDateTime.now().toString());
        result.setProcessingTimeMs(System.currentTimeMillis() - startTime);
        return result;
    }

    private String buildPrompt(Transaction transaction) {
        return String.format("""
                Analyze this credit card transaction for fraud:
                
                Transaction ID:     %s
                Customer ID:        %s
                Amount:             $%.2f
                Merchant:           %s (%s)
                Transaction Location: %s
                Customer Home:      %s
                Customer Avg Spend: $%.2f
                International:      %s
                Timestamp:          %s
                
                Is this fraudulent? Respond in JSON only.
                """,
                transaction.getTransactionId(),
                transaction.getCustomerId(),
                transaction.getAmount(),
                transaction.getMerchantName(),
                transaction.getMerchantCategory(),
                transaction.getTransactionLocation(),
                transaction.getCustomerHomeLocation(),
                transaction.getCustomerAverageSpend(),
                transaction.isInternational() ? "Yes" : "No",
                transaction.getTimestamp()
        );
    }

    private FraudAnalysis parseResponse(String aiResponse, String transactionId) {
        try {
            String cleaned = aiResponse
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();
            return objectMapper.readValue(cleaned, FraudAnalysis.class);
        } catch (Exception e) {
            FraudAnalysis fallback = new FraudAnalysis();
            fallback.setTransactionId(transactionId);
            fallback.setRiskLevel("UNKNOWN");
            fallback.setReason("AI response could not be parsed: " + aiResponse);
            fallback.setRecommendation("Manual review required");
            fallback.setConfidenceScore(0.0);
            return fallback;
        }
    }
}