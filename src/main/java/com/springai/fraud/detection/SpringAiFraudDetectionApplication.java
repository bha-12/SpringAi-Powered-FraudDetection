package com.springai.fraud.detection;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringAiFraudDetectionApplication {


	public static void main(String[] args) {
		SpringApplication.run(SpringAiFraudDetectionApplication.class, args);
	}

}
