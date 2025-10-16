package com.sparks.billing.controller;

import com.sparks.billing.model.MetricRecord;
import com.sparks.billing.model.dto.ClientMetricsDto;
import com.sparks.billing.model.dto.FinancialSummaryDto;
import com.sparks.billing.repository.BillingRecordRepository;
import com.sparks.billing.repository.MetricRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class MetricsController {

    private final MetricRecordRepository metricRepo;
    private final BillingRecordRepository billingRepo;

    @GetMapping("/client/metrics")
    public ClientMetricsDto getClientMetrics(@RequestParam String service) {
        Instant now = Instant.now();
        Instant from = now.minus(1, ChronoUnit.HOURS);
        List<MetricRecord> records = metricRepo.findByServiceNameAndTimestampBetween(service, from, now);

        double totalCalls = records.stream().mapToDouble(MetricRecord::getCalls).sum();
        double avgLatency = records.stream().mapToDouble(MetricRecord::getAvgLatencyMs).average().orElse(0);
        double p95Latency = records.stream().mapToDouble(MetricRecord::getP95LatencyMs).max().orElse(0);
        int coldStarts = records.stream().mapToInt(MetricRecord::getColdStarts).sum();
        double avgMemory = records.stream().mapToDouble(MetricRecord::getMemoryMb).average().orElse(0);

        return new ClientMetricsDto(totalCalls, avgLatency, p95Latency, coldStarts, avgMemory);
    }

    @GetMapping("/client/details")
    public List<MetricRecord> getDetails(@RequestParam String service, @RequestParam String period) {
        Instant to = Instant.now();
        Instant from = switch (period) {
            case "1h" -> to.minus(1, ChronoUnit.HOURS);
            case "6h" -> to.minus(6, ChronoUnit.HOURS);
            case "24h" -> to.minus(1, ChronoUnit.DAYS);
            case "7d" -> to.minus(7, ChronoUnit.DAYS);
            default -> to.minus(1, ChronoUnit.HOURS);
        };
        return metricRepo.findByServiceNameAndTimestampBetween(service, from, to);
    }

    @GetMapping("/client/finance")
    public FinancialSummaryDto getFinance(@RequestParam String service) {
        Instant now = Instant.now();
        Instant todayStart = now.truncatedTo(ChronoUnit.DAYS);
        Instant last24h = now.minus(1, ChronoUnit.DAYS);

        Double todayCost = billingRepo.sumCostSince(service, todayStart);
        Double last24hCost = billingRepo.sumCostSince(service, last24h);
        Double debt = billingRepo.sumUnpaidCost(service);

        return new FinancialSummaryDto(
                last24hCost != null ? last24hCost : 0.0,
                todayCost != null ? todayCost : 0.0,
                debt != null ? debt : 0.0
        );
    }
}