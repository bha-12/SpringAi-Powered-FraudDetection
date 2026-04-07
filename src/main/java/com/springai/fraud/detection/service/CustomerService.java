package com.springai.fraud.detection.service;

import com.springai.fraud.detection.model.Customer;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CustomerService {

    private final Map<String, Customer> customerDatabase = new HashMap<>();

    public CustomerService() {
        Customer cust1 = new Customer();
        cust1.setCustomerId("CUST-001");
        cust1.setName("John Smith");
        cust1.setEmail("john.smith@email.com");
        cust1.setPhone("+1-214-555-0101");
        cust1.setHomeLocation("Dallas, Texas");
        cust1.setAverageMonthlySpend(1500.00);
        cust1.setRiskCategory("LOW");
        cust1.setCardBlocked(false);
        cust1.setTotalTransactions(245);
        customerDatabase.put("CUST-001", cust1);

        Customer cust2 = new Customer();
        cust2.setCustomerId("CUST-002");
        cust2.setName("Sarah Johnson");
        cust2.setEmail("sarah.j@email.com");
        cust2.setPhone("+1-972-555-0202");
        cust2.setHomeLocation("Irving, Texas");
        cust2.setAverageMonthlySpend(3200.00);
        cust2.setRiskCategory("MEDIUM");
        cust2.setCardBlocked(false);
        cust2.setTotalTransactions(892);
        customerDatabase.put("CUST-002", cust2);

        Customer cust3 = new Customer();
        cust3.setCustomerId("CUST-003");
        cust3.setName("Mike Davis");
        cust3.setEmail("mike.davis@email.com");
        cust3.setPhone("+1-469-555-0303");
        cust3.setHomeLocation("Plano, Texas");
        cust3.setAverageMonthlySpend(800.00);
        cust3.setRiskCategory("HIGH");
        cust3.setCardBlocked(false);
        cust3.setTotalTransactions(67);
        customerDatabase.put("CUST-003", cust3);
    }

    public Customer getCustomerById(String customerId) {
        Customer customer = customerDatabase.get(customerId);
        if (customer == null) {
            Customer unknown = new Customer();
            unknown.setCustomerId(customerId);
            unknown.setName("Unknown Customer");
            unknown.setRiskCategory("UNKNOWN");
            unknown.setCardBlocked(false);
            return unknown;
        }
        return customer;
    }

    public boolean blockCard(String customerId) {
        Customer customer = customerDatabase.get(customerId);
        if (customer != null) {
            customer.setCardBlocked(true);
            return true;
        }
        return false;
    }

    public boolean isCardBlocked(String customerId) {
        Customer customer = customerDatabase.get(customerId);
        return customer != null && customer.isCardBlocked();
    }
}