package com.sparks.billing.service;

import com.sparks.billing.client.PrometheusClient;
import com.sparks.billing.model.MetricRecord;
import com.sparks.billing.repository.MetricRecordRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MetricsCollectorService {

    private final PrometheusClient prometheus;
    private final MetricRecordRepository metricsRepo;
    private final BillingCalculationService billingService;

    @Scheduled(fixedRate = 60_000)
    public void collectAndBill() {
        String namespace = "default";
        Set<String> services = prometheus.getServices(namespace);
        Instant now = Instant.now();
        Instant from = now.minus(1, ChronoUnit.HOURS);

        for (String svc : services) {
            try {
                log.info("Collecting metrics for service: {}", svc);
                MetricRecord record = fetchMetrics(svc, namespace, from, now);
                metricsRepo.save(record);
                billingService.calculateAndStoreBilling(record);
                log.info("Successfully processed billing for service: {}", svc);
            } catch (Exception e) {
                log.error("Failed to collect metrics for service {}: {}", svc, e.getMessage(), e);
            }
        }
    }

    private MetricRecord fetchMetrics(String svc, String ns, Instant from, Instant to) {
        // 1. Memory usage (работающая метрика)
        double memoryBytes = prometheus.query(String.format(
                "avg(container_memory_usage_bytes{namespace=\"%s\", pod=~\"%s.*\"})", ns, svc));
        double memoryMb = memoryBytes / 1024 / 1024;

        // 2. CPU usage (работающая метрика)
        double cpuCores = prometheus.query(String.format(
                "avg(rate(container_cpu_usage_seconds_total{namespace=\"%s\", pod=~\"%s.*\"}[5m]))", ns, svc));

        // 3. Network traffic как proxy для количества вызовов
        double networkBytesPerSec = prometheus.query(String.format(
                "rate(container_network_receive_bytes_total{namespace=\"%s\", pod=~\"%s.*\"}[5m])", ns, svc));

        // Оцениваем вызовы: предполагаем ~1KB на запрос
        double estimatedCalls = (networkBytesPerSec * 3600) / 1024; // за час

        // 4. Cold starts - по времени старта подов
        double podStartCount = prometheus.query(String.format(
                "count(kube_pod_start_time{namespace=\"%s\", pod=~\"%s.*\"})", ns, svc));
        int coldStarts = (int) podStartCount;

        // 5. Latency - оцениваем через CPU utilization или используем константу
        // Если нет прямых метрик latency, используем эвристику
        double estimatedLatency = calculateEstimatedLatency(cpuCores, memoryMb);

        MetricRecord record = new MetricRecord();
        record.setServiceName(svc);
        record.setNamespace(ns);
        record.setTimestamp(to);
        record.setCalls(Math.max(0, estimatedCalls));
        record.setAvgLatencyMs(estimatedLatency);
        record.setP95LatencyMs(estimatedLatency * 1.5); // p95 обычно выше среднего
        record.setColdStarts(coldStarts);
        record.setCpuCores(cpuCores);
        record.setMemoryMb(memoryMb);

        log.debug("Collected metrics for {}: calls={}, memory={}MB, cpu={} cores, coldStarts={}",
                svc, estimatedCalls, memoryMb, cpuCores, coldStarts);

        return record;
    }

    private double calculateEstimatedLatency(double cpuCores, double memoryMb) {
        // Эвристика: чем выше утилизация CPU, тем выше latency
        // Базовое предположение: 50ms при низкой нагрузке, до 500ms при высокой
        double baseLatency = 50.0;
        double loadFactor = Math.min(cpuCores * 100, 90.0); // CPU utilization в %
        return baseLatency + (loadFactor * 5.0); // +5ms за каждый % утилизации
    }
}