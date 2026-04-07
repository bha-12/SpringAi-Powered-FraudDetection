package com.springai.fraud.detection.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class FraudAgentService {

    private final ChatClient agentClient;
    private final FraudDetectionTools tools;

    public FraudAgentService(
            ChatClient.Builder builder,
            FraudDetectionTools tools) {
        this.tools = tools;
        this.agentClient = builder
                .defaultSystem("""
                        You are a fraud detection AI assistant.
                        
                        CRITICAL RULES:
                        - NEVER write code or pseudocode
                        - NEVER describe what you would do
                        - ALWAYS call the actual tools provided
                        - Call tools directly and immediately
                        
                        WORKFLOW - follow exactly in this order:
                        1. Call getCustomerDetails tool
                        2. Call analyzeTransaction tool
                        3. Call alertCustomer tool if risk MEDIUM or above
                        4. Call addToReviewQueue tool with findings
                        5. Report what you did
                        """)
                .build();
    }

    public String investigate(String request) {
        return agentClient
                .prompt()
                .user(request)
                .tools(tools)
                .call()
                .content();
    }
}