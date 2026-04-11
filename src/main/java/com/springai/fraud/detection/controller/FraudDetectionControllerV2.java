package com.springai.fraud.detection.controller;

import com.springai.fraud.detection.model.FraudAnalysis;
import com.springai.fraud.detection.model.Transaction;
import com.springai.fraud.detection.service.FraudDetectionServiceV2;
import com.springai.fraud.detection.service.MetricsService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v2/fraud")
public class FraudDetectionControllerV2 {

    private final FraudDetectionServiceV2 fraudDetectionServiceV2;
    private final MetricsService metricsService;

    public FraudDetectionControllerV2(
            FraudDetectionServiceV2 fraudDetectionServiceV2,
            MetricsService metricsService) {
        this.fraudDetectionServiceV2 = fraudDetectionServiceV2;
        this.metricsService = metricsService;
    }

    @PostMapping("/analyze")
    public FraudAnalysis analyze(@RequestBody Transaction transaction) {
        FraudAnalysis result = fraudDetectionServiceV2
                .analyzeWithResilience(transaction);

        // Record metrics
        metricsService.recordAnalysis(
                result.getDetectionMethod(),
                result.getRiskLevel(),
                result.getProcessingTimeMs());

        return result;
    }

    @GetMapping("/metrics/summary")
    public Map<String, Object> getMetricsSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalTokensUsed", metricsService.getTotalTokensUsed());
        summary.put("estimatedCost",
                "$" + String.format("%.4f",
                        metricsService.getTotalTokensUsed() * 0.000002));
        summary.put("actuatorMetrics",
                "http://localhost:8080/actuator/metrics");
        summary.put("healthEndpoint",
                "http://localhost:8080/actuator/health");
        return summary;
    }
}