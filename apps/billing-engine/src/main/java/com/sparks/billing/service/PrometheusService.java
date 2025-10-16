package com.sparks.billing.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.juli.logging.Log;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

@Service
@Slf4j
public class PrometheusService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${prometheus.enabled:true}")
    private boolean prometheusEnabled;

    // Список возможных адресов Prometheus
    private final List<String> prometheusUrls = Arrays.asList(
            "http://knative-kube-prometheus-st-prometheus.observability.svc.cluster.local:9090",
            "http://prometheus-operated.observability.svc.cluster.local:9090",
            "http://knative-kube-prometheus-st-prometheus.observability:9090",
            "http://prometheus-operated.observability:9090"
    );

    public PrometheusService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }


    /**
     * Автоматический поиск работающего Prometheus
     */
    public String discoverPrometheusUrl() {
        for (String url : prometheusUrls) {
            try {
                String testUrl = url + "/api/v1/query?query=up";
                ResponseEntity<String> response = restTemplate.getForEntity(testUrl, String.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    return url;
                }
            } catch (Exception e) {
                // Продолжаем пробовать следующий URL
                continue;
            }
        }
        return null;
    }

    public Map<String, Object> getBillingMetrics(String namespace, String timeRange) {
        Map<String, Object> billingMetrics = new HashMap<>();
        String workingUrl = discoverPrometheusUrl();

        billingMetrics.put("prometheus_url", workingUrl);
        billingMetrics.put("prometheus_available", true);

        try {
            // Правильные PromQL запросы:

            // 1. CPU seconds за период - используем increase для counter метрики
            String cpuQuery = String.format(
                    "sum(increase(container_cpu_usage_seconds_total{namespace=\"%s\", container!=\"POD\"}[%s]))",
                    namespace, timeRange
            );

            // 2. Memory byte-seconds за период - используем avg_over_time и умножаем на секунды
            String memoryQuery = String.format(
                    "sum(avg_over_time(container_memory_working_set_bytes{namespace=\"%s\", container!=\"POD\"}[%s])) * %d",
                    namespace, timeRange, convertToSeconds(timeRange)
            );

            billingMetrics.put("cpu_seconds", executePrometheusQuery(cpuQuery, workingUrl));
            billingMetrics.put("memory_byte_seconds", executePrometheusQuery(memoryQuery, workingUrl));

        } catch (Exception e) {
            billingMetrics.put("prometheus_available", false);
            billingMetrics.put("prometheus_error", e.getMessage());
        }

        return billingMetrics;
    }

    /**
     * Конвертирует период в секунды
     */
    private long convertToSeconds(String timeRange) {
        if (timeRange.endsWith("h")) {
            int hours = Integer.parseInt(timeRange.substring(0, timeRange.length() - 1));
            return hours * 3600L;
        } else if (timeRange.endsWith("m")) {
            int minutes = Integer.parseInt(timeRange.substring(0, timeRange.length() - 1));
            return minutes * 60L;
        } else if (timeRange.endsWith("s")) {
            return Long.parseLong(timeRange.substring(0, timeRange.length() - 1));
        } else if (timeRange.endsWith("d")) {
            int days = Integer.parseInt(timeRange.substring(0, timeRange.length() - 1));
            return days * 86400L;
        } else {
            // По умолчанию 1 час
            return 3600L;
        }
    }


    /**
     * Проверка доступности Prometheus
     */
    public Map<String, Object> checkPrometheusHealth() {
        Map<String, Object> result = new HashMap<>();
        String workingUrl = discoverPrometheusUrl();

        if (workingUrl != null) {
            result.put("status", "connected");
            result.put("prometheus_url", workingUrl);
            result.put("available_urls", prometheusUrls);
        } else {
            result.put("status", "error");
            result.put("available_urls", prometheusUrls);
            result.put("error", "Cannot connect to any Prometheus instance");
            result.put("suggested_actions", List.of(
                    "Check if Prometheus pods are running: kubectl get pods -n observability",
                    "Check Prometheus services: kubectl get svc -n observability",
                    "Verify network policies",
                    "Check if Prometheus is exposed on different port"
            ));
        }

        return result;
    }

    /**
     * Общий метод выполнения запросов к Prometheus
     */
    private Map<String, Object> executePrometheusQuery(String query, String baseUrl) {
        Map<String, Object> result = new HashMap<>();

        try {
            // Просто создаем URL с параметром query - RestTemplate сам закодирует
            String url = baseUrl + "/api/v1/query?query={query}";

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class,
                    query  // Передаем query как параметр
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode responseJson = objectMapper.readTree(response.getBody());
                result.put("status", "success");
                result.put("data", responseJson);
            } else {
                result.put("status", "error");
                result.put("error", "HTTP " + response.getStatusCode());
                result.put("response_body", response.getBody());
            }

        } catch (Exception e) {
            result.put("status", "error");
            result.put("error", e.getMessage());
            result.put("query", query);
        }

        return result;
    }
}