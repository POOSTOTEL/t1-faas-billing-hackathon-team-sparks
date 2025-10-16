package com.sparks.billing.model;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "billing.rates")
@Validated
@Data
public class BillingRates {
    @DecimalMin("0.0")
    private double perCall = 0.001;

    @DecimalMin("0.0")
    private double perMs = 0.00001;

    @DecimalMin("0.0")
    private double perMbSec = 0.000001;

    @DecimalMin("0.0")
    private double coldStartPenalty = 0.1;
}
