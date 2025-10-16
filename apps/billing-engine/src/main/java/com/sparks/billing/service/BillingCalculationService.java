package com.sparks.billing.service;

import com.sparks.billing.model.BillingRates;
import com.sparks.billing.model.BillingRecord;
import com.sparks.billing.model.MetricRecord;
import com.sparks.billing.repository.BillingRecordRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class BillingCalculationService {
    private final BillingRecordRepository billingRepo;
    private final BillingRates rates;

    public void calculateAndStoreBilling(MetricRecord m) {
        try {
            // Расчет использования ресурсов за час
            double cpuSec = m.getCpuCores() * 3600;           // CPU-секунды
            double memMbSec = m.getMemoryMb() * 3600;         // Memory-MB-секунды

            // Расчет стоимости
            double callsCost = m.getCalls() * rates.getPerCall();
            double latencyCost = m.getCalls() * m.getAvgLatencyMs() * rates.getPerMs();
            double memoryCost = memMbSec * rates.getPerMbSec();
            double coldStartCost = m.getColdStarts() * rates.getColdStartPenalty();

            double totalCost = callsCost + latencyCost + memoryCost + coldStartCost;

            BillingRecord billingRecord = new BillingRecord();
            billingRecord.setServiceName(m.getServiceName());
            billingRecord.setNamespace(m.getNamespace());
            billingRecord.setPeriodStart(m.getTimestamp().minus(1, ChronoUnit.HOURS));
            billingRecord.setPeriodEnd(m.getTimestamp());
            billingRecord.setCalls(m.getCalls());
            billingRecord.setTotalCpuSec(cpuSec);
            billingRecord.setTotalMemoryMbSec(memMbSec);
            billingRecord.setColdStartCount(m.getColdStarts());
            billingRecord.setTotalCostRub(totalCost);
            billingRecord.setPaid(false);

            billingRepo.save(billingRecord);

            log.info("Billing calculated for {}: ${} (calls: {}, memory: {}, coldStarts: {})",
                    m.getServiceName(), totalCost, callsCost, memoryCost, coldStartCost);

        } catch (Exception e) {
            log.error("Failed to calculate billing for service {}: {}",
                    m.getServiceName(), e.getMessage(), e);
        }
    }
}