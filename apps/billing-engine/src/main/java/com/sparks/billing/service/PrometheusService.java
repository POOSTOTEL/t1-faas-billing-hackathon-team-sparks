package com.sparks.billing.service;

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
            // CPU seconds за период
            String cpuQuery = String.format(
                    "sum(container_cpu_usage_seconds_total{namespace=\"%s\"}[%s])",
                    namespace, timeRange
            );

            // Memory byte-seconds за период
            String memoryQuery = String.format(
                    "sum(container_memory_usage_bytes{namespace=\"%s\"}[%s])",
                    namespace, timeRange
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
        String url = baseUrl + "/api/v1/query?query=" +
                java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8);

        Map<String, Object> result = new HashMap<>();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode responseJson = objectMapper.readTree(response.getBody());
                result.put("status", "success");
                result.put("data", responseJson);
            } else {
                result.put("status", "error");
                result.put("error", "HTTP " + response.getStatusCode());
            }
        } catch (Exception e) {
            result.put("status", "error");
            result.put("error", e.getMessage());
            result.put("url", url);
        }

        return result;
    }
}