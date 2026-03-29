# 🚨 AI-Powered Fraud Detection System

A Spring Boot microservice that uses local LLM (Mistral via Ollama) to analyze 
credit card transactions and detect fraud in real-time.

## 🛠️ Tech Stack
- Java 21
- Spring Boot 3.5.x
- Spring AI 1.1.3
- Ollama (Local LLM — no API cost)
- Mistral 7B model
- Apple M5 Pro (Metal GPU acceleration)

## 🚀 Features
- Real-time fraud analysis via REST API
- 4 risk levels: LOW, MEDIUM, HIGH, CRITICAL
- Confidence scoring (0.0 - 1.0)
- Additional AI commentary on suspicious patterns
- Batch transaction analysis (coming soon)

## 📡 API Usage

### Analyze Single Transaction
POST /fraud/analyze

### Analyze Batch Transactions  
POST /fraud/analyze/batch

## 🏃 Running Locally
1. Install Ollama — ollama.com
2. Pull Mistral: ollama pull mistral
3. Run: ./mvnw spring-boot:run
4. Test: POST http://localhost:8080/fraud/analyze

## 👨‍💻 Author
Bharat Dronamraju — Java Backend Engineer transitioning to AI Engineer
