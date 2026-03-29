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

    public FraudDetectionService(
            ChatClient.Builder builder,
            ObjectMapper objectMapper,
            @Value("${fraud.detection.system.prompt}") String systemPrompt) {

        this.objectMapper = objectMapper;
        this.chatClient = builder
                .defaultSystem(systemPrompt)
                .build();
    }

    public FraudAnalysis analyze(Transaction transaction) {
        long startTime = System.currentTimeMillis();
        String prompt = buildPrompt(transaction);

        String aiResponse = chatClient
                .prompt()
                .user(prompt)
                .call()
                .content();

        FraudAnalysis result = parseResponse(aiResponse, transaction.getTransactionId());
        result.setAnalyzedAt(java.time.LocalDateTime.now().toString());
        result.setProcessingTimeMs(System.currentTimeMillis() - startTime);
        return result;
    }

    public List<FraudAnalysis> analyzeBatch(List<Transaction> transactions) {
        return transactions.parallelStream()
                .map(this::analyze)
                .collect(java.util.stream.Collectors.toList());
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