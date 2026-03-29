package com.springai.fraud.detection.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springai.fraud.detection.model.FraudAnalysis;
import com.springai.fraud.detection.model.Transaction;
import com.springai.fraud.detection.service.FraudDetectionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@RestController
@RequestMapping("/fraud")
public class FraudDetectionController {

    private final FraudDetectionService fraudDetectionService;

    public FraudDetectionController(FraudDetectionService fraudDetectionService) {
        this.fraudDetectionService = fraudDetectionService;
    }

    @PostMapping("/analyze")
    public FraudAnalysis analyze(@RequestBody Transaction transaction) {
        return fraudDetectionService.analyze(transaction);
    }

    @PostMapping("/analyze/batch")
    public List<FraudAnalysis> analyzeBatch(@RequestBody List<Transaction> transactions) {
        return fraudDetectionService.analyzeBatch(transactions);
    }
}