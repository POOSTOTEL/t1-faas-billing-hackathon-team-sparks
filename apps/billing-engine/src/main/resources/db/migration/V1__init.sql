CREATE TABLE metric_records (
    id BIGSERIAL PRIMARY KEY,
    service_name VARCHAR(100) NOT NULL,
    namespace VARCHAR(100) NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL,
    calls DOUBLE PRECISION NOT NULL,
    avg_latency_ms DOUBLE PRECISION NOT NULL,
    p95_latency_ms DOUBLE PRECISION NOT NULL,
    cold_starts INT NOT NULL,
    cpu_cores DOUBLE PRECISION NOT NULL,
    memory_mb DOUBLE PRECISION NOT NULL,
    UNIQUE(service_name, namespace, timestamp)
);

CREATE TABLE billing_records (
    id BIGSERIAL PRIMARY KEY,
    service_name VARCHAR(100) NOT NULL,
    namespace VARCHAR(100) NOT NULL,
    period_start TIMESTAMPTZ NOT NULL,
    period_end TIMESTAMPTZ NOT NULL,
    calls DOUBLE PRECISION NOT NULL,
    total_cpu_sec DOUBLE PRECISION NOT NULL,
    total_memory_mb_sec DOUBLE PRECISION NOT NULL,
    cold_start_count INT NOT NULL,
    total_cost_rub DOUBLE PRECISION NOT NULL,
    is_paid BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_metric_service_time ON metric_records(service_name, timestamp);
CREATE INDEX idx_billing_service_period ON billing_records(service_name, period_start);