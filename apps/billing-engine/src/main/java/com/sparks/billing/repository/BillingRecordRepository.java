package com.sparks.billing.repository;


import com.sparks.billing.model.BillingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface BillingRecordRepository extends JpaRepository<BillingRecord, Long> {
    List<BillingRecord> findByServiceNameAndPeriodStartAfter(String service, Instant from);

    @Query("SELECT SUM(b.totalCostRub) FROM BillingRecord b WHERE b.serviceName = :service AND b.periodEnd >= :since")
    Double sumCostSince(@Param("service") String service, @Param("since") Instant since);

    @Query("SELECT SUM(b.totalCostRub) FROM BillingRecord b WHERE b.serviceName = :service AND b.isPaid = false")
    Double sumUnpaidCost(@Param("service") String service);
}