package com.springai.fraud.detection.repository;

import com.springai.fraud.detection.model.FraudReviewCase;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FraudReviewCaseRepository
        extends MongoRepository<FraudReviewCase, String> {

    // Spring Data generates these queries automatically!
    List<FraudReviewCase> findByStatus(String status);

    List<FraudReviewCase> findByStatusOrderByRiskLevelDesc(String status);

    int countByStatus(String status);

    List<FraudReviewCase> findByCustomerId(String customerId);

    List<FraudReviewCase> findByRiskLevel(String riskLevel);
}