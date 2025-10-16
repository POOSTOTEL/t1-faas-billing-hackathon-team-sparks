package com.sparks.billing.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Data
@Table(name = "metric_records")
@EntityListeners(AuditingEntityListener.class)
public class MetricRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(name = "namespace", nullable = false)
    private String namespace;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "calls", nullable = false)
    private double calls;

    @Column(name = "avg_latency_ms", nullable = false)
    private double avgLatencyMs;

    @Column(name = "p95_latency_ms", nullable = false)
    private double p95LatencyMs;

    @Column(name = "cold_starts", nullable = false)
    private int coldStarts;

    @Column(name = "cpu_cores", nullable = false)
    private double cpuCores;

    @Column(name = "memory_mb", nullable = false)
    private double memoryMb;
}