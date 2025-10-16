package com.sparks.billing.repository;

import com.sparks.billing.model.MetricRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface MetricRecordRepository extends JpaRepository<MetricRecord, Long> {
    List<MetricRecord> findByServiceNameAndTimestampBetween(
            String service, Instant from, Instant to);
}