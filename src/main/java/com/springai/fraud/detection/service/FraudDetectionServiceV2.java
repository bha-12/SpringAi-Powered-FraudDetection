package com.springai.fraud.detection.service;

import com.springai.fraud.detection.model.FraudAnalysis;
import com.springai.fraud.detection.model.Transaction;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FraudDetectionServiceV2 {

    private static final Logger log = 
            LoggerFactory.getLogger(FraudDetectionServiceV2.class);

    private final FraudDetectionService fraudDetectionService;
    private final FraudRuleEngine fraudRuleEngine;

    public FraudDetectionServiceV2(
            FraudDetectionService fraudDetectionService,
            FraudRuleEngine fraudRuleEngine) {
        this.fraudDetectionService = fraudDetectionService;
        this.fraudRuleEngine = fraudRuleEngine;
    }

    @CircuitBreaker(name = "aiService", fallbackMethod = "fallbackAnalysis")
    @RateLimiter(name = "aiService", fallbackMethod = "rateLimitFallback")
    public FraudAnalysis analyzeWithResilience(Transaction transaction) {
        log.info("🔍 Analyzing transaction: {} with AI",
                transaction.getTransactionId());

        long startTime = System.currentTimeMillis();
        FraudAnalysis result = fraudDetectionService.analyze(transaction);
        long duration = System.currentTimeMillis() - startTime;

        log.info("✅ Analysis complete: {} ms, risk: {}",
                duration, result.getRiskLevel());

        return result;
    }

    // Circuit breaker fallback
    // Called when AI is down or too slow
    public FraudAnalysis fallbackAnalysis(
            Transaction transaction, Exception ex) {

        log.warn("⚠️ Circuit breaker triggered for: {} — Reason: {}",
                transaction.getTransactionId(), ex.getMessage());

        FraudRuleEngine.RuleResult ruleResult =
                fraudRuleEngine.evaluate(transaction);

        // Map Decision to risk level
        String riskLevel = switch (ruleResult.getDecision()) {
            case BLOCK -> "CRITICAL";
            case SEND_TO_AI -> "MEDIUM";
            case ALLOW -> "LOW";
        };

        String action = switch (ruleResult.getDecision()) {
            case BLOCK -> "BLOCK";
            case SEND_TO_AI -> "FLAG";
            case ALLOW -> "ALLOW";
        };

        FraudAnalysis fallback = new FraudAnalysis();
        fallback.setTransactionId(transaction.getTransactionId());
        fallback.setRiskLevel(riskLevel);
        fallback.setAction(action);
        fallback.setDetectionMethod("RULE_ENGINE_FALLBACK");
        fallback.setReason("AI unavailable — rule engine fallback: "
                + ruleResult.getReason());
        fallback.setConfidenceScore(0.6);
        fallback.setAnalyzedAt(LocalDateTime.now().toString());
        fallback.setProcessingTimeMs(0L);

        return fallback;
    }

    // Rate limiter fallback
    // Called when too many requests per minute
    public FraudAnalysis rateLimitFallback(
            Transaction transaction, Exception ex) {

        log.warn("🚦 Rate limit exceeded for: {}",
                transaction.getTransactionId());

        FraudAnalysis fallback = new FraudAnalysis();
        fallback.setTransactionId(transaction.getTransactionId());
        fallback.setRiskLevel("MEDIUM");
        fallback.setAction("MONITOR");
        fallback.setDetectionMethod("RATE_LIMITED");
        fallback.setReason("Rate limit exceeded — " +
                "transaction queued for later analysis");
        fallback.setConfidenceScore(0.5);
        fallback.setAnalyzedAt(LocalDateTime.now().toString());
        fallback.setProcessingTimeMs(0L);

        return fallback;
    }
}