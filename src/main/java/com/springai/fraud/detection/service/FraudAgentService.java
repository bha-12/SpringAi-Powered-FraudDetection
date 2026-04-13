package com.springai.fraud.detection.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

@Service
public class FraudAgentService {

    private final ChatClient agentClient;
    private final FraudDetectionTools tools;
    private final FraudRuleEngine fraudRuleEngine;

    public FraudAgentService(
            ChatClient.Builder builder,
            FraudDetectionTools tools,
            FraudRuleEngine fraudRuleEngine) {
        this.tools = tools;
        this.fraudRuleEngine = fraudRuleEngine;
        this.agentClient = builder
                .defaultSystem("""
                        You are a fraud detection AI assistant.
                        
                        CRITICAL RULES:
                        - NEVER write code or pseudocode
                        - ALWAYS call the actual tools provided
                        - ALWAYS pass ALL required parameters to tools
                        
                        WORKFLOW - follow exactly:
                        1. Call getCustomerDetails with customerId
                        2. Call analyzeTransaction with ALL details
                        3. Call alertCustomer if risk MEDIUM or above
                        4. Call addToReviewQueue with all findings
                        5. Report what actions were taken
                        """)
                .build();
    }

    @CircuitBreaker(name = "aiService",
            fallbackMethod = "investigateFallback")
    @RateLimiter(name = "aiService",
            fallbackMethod = "investigateFallback")
    public String investigate(String request) {
        return agentClient
                .prompt()
                .user(request)
                .tools(tools)
                .call()
                .content();
    }

    // Fallback when AI agent is down
    public String investigateFallback(
            String request, Exception ex) {
        return """
                ⚠️ AI Agent temporarily unavailable.
                Reason: %s
                
                Fallback action taken:
                - Transaction flagged for manual review
                - Please use /v2/fraud/analyze for immediate analysis
                - Human analyst should review manually
                """.formatted(ex.getMessage());
    }
}