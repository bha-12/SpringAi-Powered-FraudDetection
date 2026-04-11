package com.springai.fraud.detection.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MetricsService {

    private static final Logger log =
            LoggerFactory.getLogger(MetricsService.class);

    // Counters
    private final Counter totalAnalysisCounter;
    private final Counter aiAnalysisCounter;
    private final Counter ruleEngineCounter;
    private final Counter fallbackCounter;
    private final Counter criticalRiskCounter;
    private final Counter highRiskCounter;
    private final Counter mediumRiskCounter;
    private final Counter lowRiskCounter;

    // Timer for AI response time
    private final Timer aiResponseTimer;

    // Token usage tracking
    private final AtomicInteger totalTokensUsed = new AtomicInteger(0);

    public MetricsService(MeterRegistry registry) {
        // Initialize counters
        totalAnalysisCounter = Counter.builder("fraud.analysis.total")
                .description("Total fraud analyses performed")
                .register(registry);

        aiAnalysisCounter = Counter.builder("fraud.analysis.ai")
                .description("Analyses performed by AI")
                .register(registry);

        ruleEngineCounter = Counter.builder("fraud.analysis.rule_engine")
                .description("Analyses performed by rule engine")
                .register(registry);

        fallbackCounter = Counter.builder("fraud.analysis.fallback")
                .description("Analyses using fallback")
                .register(registry);

        criticalRiskCounter = Counter.builder("fraud.risk.critical")
                .description("Critical risk transactions")
                .register(registry);

        highRiskCounter = Counter.builder("fraud.risk.high")
                .description("High risk transactions")
                .register(registry);

        mediumRiskCounter = Counter.builder("fraud.risk.medium")
                .description("Medium risk transactions")
                .register(registry);

        lowRiskCounter = Counter.builder("fraud.risk.low")
                .description("Low risk transactions")
                .register(registry);

        aiResponseTimer = Timer.builder("fraud.ai.response.time")
                .description("AI model response time")
                .register(registry);

        // Register token usage as gauge
        io.micrometer.core.instrument.Gauge
                .builder("fraud.tokens.used", totalTokensUsed,
                        AtomicInteger::get)
                .description("Total tokens used")
                .register(registry);
    }

    public void recordAnalysis(String detectionMethod, 
                               String riskLevel,
                               long processingTimeMs) {
        // Count total
        totalAnalysisCounter.increment();

        // Count by method
        switch (detectionMethod) {
            case "AI_ANALYSIS" -> aiAnalysisCounter.increment();
            case "RULE_ENGINE" -> ruleEngineCounter.increment();
            case "RULE_ENGINE_FALLBACK" -> fallbackCounter.increment();
        }

        // Count by risk level
        switch (riskLevel) {
            case "CRITICAL" -> criticalRiskCounter.increment();
            case "HIGH"     -> highRiskCounter.increment();
            case "MEDIUM"   -> mediumRiskCounter.increment();
            case "LOW"      -> lowRiskCounter.increment();
        }

        // Record response time
        aiResponseTimer.record(processingTimeMs,
                java.util.concurrent.TimeUnit.MILLISECONDS);

        // Estimate token usage (rough estimate)
        // Average prompt ~500 tokens, response ~200 tokens
        if ("AI_ANALYSIS".equals(detectionMethod)) {
            totalTokensUsed.addAndGet(700);
        }

        log.info("📊 Metrics recorded — method: {}, risk: {}, time: {}ms",
                detectionMethod, riskLevel, processingTimeMs);
    }

    public int getTotalTokensUsed() {
        return totalTokensUsed.get();
    }
}