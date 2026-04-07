package com.springai.fraud.detection.service;

import com.springai.fraud.detection.model.CardAction;
import com.springai.fraud.detection.model.Customer;
import com.springai.fraud.detection.model.FraudAnalysis;
import com.springai.fraud.detection.model.FraudReviewCase;
import com.springai.fraud.detection.model.Transaction;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class FraudDetectionTools {

    private final FraudDetectionService fraudDetectionService;
    private final CustomerService customerService;
    private final ReviewQueueService reviewQueueService;

    public FraudDetectionTools(
            FraudDetectionService fraudDetectionService,
            CustomerService customerService,
            ReviewQueueService reviewQueueService) {
        this.fraudDetectionService = fraudDetectionService;
        this.customerService = customerService;
        this.reviewQueueService = reviewQueueService;
    }

    @Tool(name = "getCustomerDetails",
            description = "Get customer details and profile by customer ID")
    public Customer getCustomerDetails(String customerId) {
        System.out.println("🔧 Tool called: getCustomerDetails(" + customerId + ")");
        return customerService.getCustomerById(customerId);
    }

    @Tool(name = "analyzeTransaction",
            description = "Analyze a credit card transaction for fraud risk")
    public FraudAnalysis analyzeTransaction(
            String transactionId,
            String customerId,
            Double amount,
            String merchantName,
            String merchantCategory,
            String transactionLocation,
            String customerHomeLocation,
            Double customerAverageSpend,
            Boolean isInternational,
            String timestamp) {

        System.out.println("🔧 Tool called: analyzeTransaction(" + transactionId + ")");

        Transaction transaction = new Transaction();
        transaction.setTransactionId(transactionId);
        transaction.setCustomerId(customerId);
        transaction.setAmount(amount != null ? amount : 0.0);
        transaction.setMerchantName(merchantName != null ? merchantName : "");
        transaction.setMerchantCategory(merchantCategory != null ? merchantCategory : "");
        transaction.setTransactionLocation(transactionLocation != null ? transactionLocation : "");
        transaction.setCustomerHomeLocation(customerHomeLocation != null ? customerHomeLocation : "");
        transaction.setCustomerAverageSpend(customerAverageSpend != null ? customerAverageSpend : 0.0);
        transaction.setInternational(isInternational != null && isInternational);
        transaction.setTimestamp(timestamp != null ? timestamp : "");

        return fraudDetectionService.analyze(transaction);
    }

    @Tool(name = "addToReviewQueue",
            description = "Add fraud case to human review queue. Use instead of blocking directly.")
    public FraudReviewCase addToReviewQueue(
            String transactionId,
            String customerId,
            String riskLevel,
            String recommendedAction,
            String reason,
            double confidenceScore) {
        System.out.println("🔧 Tool called: addToReviewQueue(" + transactionId + ")");
        Customer customer = customerService.getCustomerById(customerId);
        FraudReviewCase reviewCase = new FraudReviewCase();
        reviewCase.setTransactionId(transactionId);
        reviewCase.setCustomerId(customerId);
        reviewCase.setRiskLevel(riskLevel);
        reviewCase.setAction(recommendedAction);
        reviewCase.setAiReason(reason);
        reviewCase.setAiRecommendation(
                "AI recommends: " + recommendedAction +
                        " with confidence " + confidenceScore);
        reviewCase.setConfidenceScore(confidenceScore);
        reviewCase.setCustomer(customer);
        return reviewQueueService.addToQueue(reviewCase);
    }

    @Tool(name = "alertCustomer",
            description = "Send fraud alert to customer via email and phone")
    public CardAction alertCustomer(
            String customerId, String alertMessage) {
        System.out.println("🔧 Tool called: alertCustomer(" + customerId + ")");
        Customer customer = customerService.getCustomerById(customerId);
        CardAction action = new CardAction();
        action.setCustomerId(customerId);
        action.setAction("ALERTED");
        action.setReason("Alert sent to " + customer.getEmail()
                + " and " + customer.getPhone()
                + ": " + alertMessage);
        action.setTimestamp(LocalDateTime.now().toString());
        action.setSuccess(true);
        return action;
    }

    @Tool(name = "isCardBlocked",
            description = "Check if a customer card is already blocked")
    public String isCardBlocked(String customerId) {
        System.out.println("🔧 Tool called: isCardBlocked(" + customerId + ")");
        boolean blocked = customerService.isCardBlocked(customerId);
        return blocked ? "Card is BLOCKED" : "Card is NOT blocked";
    }
}