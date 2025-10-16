package com.sparks.billing.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
@Table(name = "billing_records")
public class BillingRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "namespace")
    private String namespace;

    @Column(name = "period_start")
    private Instant periodStart;

    @Column(name = "period_end")
    private Instant periodEnd;

    @Column(name = "calls")
    private double calls;

    @Column(name = "total_cpu_sec")
    private double totalCpuSec;

    @Column(name = "total_memory_mb_sec")
    private double totalMemoryMbSec;

    @Column(name = "cold_start_count")
    private int coldStartCount;

    @Column(name = "total_cost_rub")
    private double totalCostRub;

    @Column(name = "is_paid")
    private boolean isPaid;
}