package com.sparks.billing.controller;

import com.sparks.billing.client.PrometheusClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {

    private final PrometheusClient prometheusClient;

    @GetMapping("/test-prometheus")
    public String testPrometheus() {
        log.info("=== START PROMETHEUS CONNECTION TEST ===");

        String connectionResult = prometheusClient.testConnection();
        log.info("Connection result: {}", connectionResult);

        Set<String> services = prometheusClient.getServices("default");
        log.info("Discovered services: {}", services);

        // Тест простого запроса
        double memoryUsage = prometheusClient.query("container_memory_usage_bytes");
        log.info("Memory usage query result: {}", memoryUsage);

        log.info("=== END PROMETHEUS CONNECTION TEST ===");

        return String.format(
                "Connection: %s | Services: %s | Memory: %.2f",
                connectionResult, services, memoryUsage
        );
    }

    @GetMapping("/simple-query")
    public String simpleQuery() {
        try {
            double result = prometheusClient.query("up");
            return "Simple query result: " + result;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}