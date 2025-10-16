package com.sparks.billing;

import com.sparks.billing.model.BillingRates;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(BillingRates.class)
public class BillingEngineApplication {
    public static void main(String[] args) {
        SpringApplication.run(BillingEngineApplication.class, args);
    }
}