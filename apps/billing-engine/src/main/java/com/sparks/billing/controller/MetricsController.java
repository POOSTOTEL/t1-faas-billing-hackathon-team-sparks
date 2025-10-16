package com.sparks.billing.controller;

import com.sparks.billing.service.PrometheusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/billing")
public class MetricsController {

    private final PrometheusService prometheusService;

    public MetricsController(PrometheusService prometheusService) {
        this.prometheusService = prometheusService;
    }

    @GetMapping("/summary/{namespace}")
    public ResponseEntity<Map<String, Object>> getBillingSummary(
            @PathVariable String namespace,
            @RequestParam(defaultValue = "1h") String period) {

        return ResponseEntity.ok(prometheusService.getBillingMetrics(namespace, period));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "OK", "service", "Billing Engine"));
    }

    @GetMapping("/ready")
    public ResponseEntity<Map<String, String>> readiness() {
        // Можно добавить проверку подключения к Prometheus
        return ResponseEntity.ok(Map.of("status", "READY"));
    }
    @GetMapping("/prometheus/test")
    public ResponseEntity<Map<String, Object>> testPrometheusQuery() {
        // Простой тестовый запрос к Prometheus
        String testQuery = "up";
        Map<String, Object> result = new HashMap<>();

        try {
            String url = "http://knative-kube-prometheus-st-prometheus.observability.svc.cluster.local:9090/api/v1/query?query=" +
                    java.net.URLEncoder.encode(testQuery, java.nio.charset.StandardCharsets.UTF_8);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            result.put("status", "success");
            result.put("http_status", response.getStatusCodeValue());
            result.put("response_length", response.getBody().length());
            result.put("url", url);

        } catch (Exception e) {
            result.put("status", "error");
            result.put("error", e.getMessage());
            result.put("suggested_urls", List.of(
                    "http://knative-kube-prometheus-st-prometheus.observability.svc.cluster.local:9090",
                    "http://prometheus-operated.observability.svc.cluster.local:9090"
            ));
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/prometheus/health")
    public ResponseEntity<Map<String, Object>> checkPrometheus() {
        return ResponseEntity.ok(prometheusService.checkPrometheusHealth());
    }

    @GetMapping("/test/metrics")
    public ResponseEntity<Map<String, Object>> testMetrics() {
        // Тестовый endpoint с mock данными
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Test metrics endpoint");
        result.put("timestamp", System.currentTimeMillis());
        result.put("sample_data", Map.of(
                "cpu_usage", "0.75",
                "memory_usage", "512Mi",
                "requests", "1500"
        ));
        return ResponseEntity.ok(result);
    }
}