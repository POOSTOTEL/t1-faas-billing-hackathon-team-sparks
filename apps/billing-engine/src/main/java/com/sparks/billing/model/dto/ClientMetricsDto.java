package com.sparks.billing.model.dto;


public record ClientMetricsDto(
        double totalCalls,
        double avgLatencyMs,
        double p95LatencyMs,
        int coldStarts,
        double avgMemoryMb
) {}