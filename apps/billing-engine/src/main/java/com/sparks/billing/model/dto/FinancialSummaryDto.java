package com.sparks.billing.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public record FinancialSummaryDto(
        double last24hCost,
        double todayCost,
        double debt
) {}
