package com.springai.fraud.detection.service;

import com.springai.fraud.detection.model.FraudAnalysis;
import com.springai.fraud.detection.model.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FraudDetectionService {

    private final ChatClient chatClient;

    private final ObjectMapper objectMapper;

    public FraudDetectionService(ChatClient.Builder builder, ObjectMapper objectMapper, ObjectMapper objectMapper1) {
        this.chatClient = builder
                .defaultSystem("""
                        You are an expert fraud detection AI for a major US credit card company.
                        You analyze transactions and identify suspicious activity.
                        
                        Use these STRICT risk rules:
                        
                                        CRITICAL: ANY of these alone = CRITICAL
                                                                            - Amount is 10x or more above customer average
                                                                            - International transaction + unusual hour (10PM-6AM)
                                                                            - Unknown merchant + high amount + international
                        
                                        HIGH: ALL of these together = HIGH risk
                                          - Amount is 5x-9x above customer average (not 10x+)
                                          - Unusual hour (10PM-6AM)
                                          - Merchant category mismatch with history
                        
                        MEDIUM: ONE of these = MEDIUM risk
                          - Amount is 2x-5x above customer average
                          - New merchant never seen before
                          - Different city from home location
                        
                        LOW: Normal transaction patterns
                        
                        Action rules — you MUST follow these exactly:
                          CRITICAL → action = BLOCK
                          HIGH     → action = FLAG
                          MEDIUM   → action = MONITOR
                          LOW      → action = ALLOW
                        
                        Always respond with ONLY a valid JSON object — no explanation,
                        no markdown, no extra text. Just raw JSON.
                        
                        JSON structure:
                        {
                            "transactionId": "string",
                            "riskLevel": "LOW or MEDIUM or HIGH or CRITICAL",
                            "action": "BLOCK or FLAG or MONITOR or ALLOW",
                            "reason": "list every suspicious signal detected",
                            "recommendation": "specific action the bank should take",
                            "confidenceScore": 0.0 to 1.0,
                            "additionalComments": "extra context or pattern observations"
                        }
                        """)
                .build();
        this.objectMapper = objectMapper1;
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
            // Clean response in case AI adds markdown
            String cleaned = aiResponse
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            return objectMapper.readValue(cleaned, FraudAnalysis.class);

        } catch (Exception e) {
            // Fallback if parsing fails
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