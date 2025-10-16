package com.sparks.billing.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Slf4j
@Component
public class PrometheusClient {

    @Value("${prometheus.url:http://prometheus-operated.observability.svc:9090}")
    private String prometheusUrl;

    /**
     * Получает список сервисов - УПРОЩЕННАЯ ВЕРСИЯ
     */
    public Set<String> getServices(String namespace) {
        log.info("🚀 START getServices for namespace: {}", namespace);

        try {
            // ПРОСТОЙ запрос который точно должен работать
            String query = "up";
            log.info("📊 Trying basic query: {}", query);

            String response = executeSimplePromQuery(query);
            log.info("✅ Got response from Prometheus, length: {}", response.length());

            // Просто возвращаем тестовые сервисы для начала
            Set<String> services = Set.of("test-app-1", "test-app-2", "test-app-3");
            log.info("🎯 Returning test services: {}", services);
            return services;

        } catch (Exception e) {
            log.error("💥 CRITICAL ERROR in getServices: {}", e.getMessage(), e);
            log.info("🔄 Using fallback services");
            return Set.of("fallback-service-1", "fallback-service-2");
        } finally {
            log.info("🏁 END getServices");
        }
    }

    /**
     * Выполняет запрос к Prometheus - УПРОЩЕННАЯ ВЕРСИЯ
     */
    public double query(String promQL) {
        log.info("🔍 START query: {}", promQL);

        try {
            String response = executeSimplePromQuery(promQL);
            log.info("📄 Response received, length: {}", response.length());

            // Простой парсинг
            double value = parseSimpleValue(response);
            log.info("📈 Parsed value: {}", value);

            return value;

        } catch (Exception e) {
            log.error("❌ Query failed: {}", e.getMessage());
            log.info("🔄 Returning default value 0.0");
            return 0.0;
        } finally {
            log.info("🏁 END query");
        }
    }

    /**
     * ПРОСТОЙ HTTP запрос через HttpURLConnection
     */
    private String executeSimplePromQuery(String query) throws Exception {
        log.info("🌐 START executeSimplePromQuery: {}", query);

        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String urlString = prometheusUrl + "/api/v1/query?query=" + encodedQuery;

        log.info("🔗 URL: {}", urlString);

        URL url = new URL(urlString);
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000); // 10 seconds
            connection.setReadTimeout(10000);    // 10 seconds

            log.info("📡 Connecting to Prometheus...");
            int responseCode = connection.getResponseCode();
            log.info("📋 Response Code: {}", responseCode);

            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String result = response.toString();
                log.info("✅ SUCCESS - Response length: {}", result.length());
                return result;
            } else {
                // Читаем error stream
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                String errorLine;
                StringBuilder errorResponse = new StringBuilder();

                while ((errorLine = errorReader.readLine()) != null) {
                    errorResponse.append(errorLine);
                }
                errorReader.close();

                log.error("🚨 HTTP ERROR {}: {}", responseCode, errorResponse.toString());
                throw new RuntimeException("HTTP " + responseCode + ": " + errorResponse.toString());
            }

        } catch (Exception e) {
            log.error("💥 CONNECTION ERROR: {}", e.getMessage());
            throw e;
        } finally {
            if (connection != null) {
                connection.disconnect();
                log.info("🔌 Connection disconnected");
            }
        }
    }

    /**
     * ПРОСТОЙ парсинг значения из ответа
     */
    private double parseSimpleValue(String response) {
        log.info("🔧 START parseSimpleValue, response length: {}", response.length());

        try {
            // Простой поиск значения в JSON
            // Ищем паттерн: "value":["timestamp","number"]
            int valueIndex = response.indexOf("\"value\"");
            if (valueIndex == -1) {
                log.warn("⚠️ No 'value' field found in response");
                return 0.0;
            }

            int bracketStart = response.indexOf("[", valueIndex);
            int bracketEnd = response.indexOf("]", bracketStart);

            if (bracketStart == -1 || bracketEnd == -1) {
                log.warn("⚠️ No array found in value field");
                return 0.0;
            }

            String arrayContent = response.substring(bracketStart + 1, bracketEnd);
            log.info("📦 Array content: {}", arrayContent);

            // Разделяем по запятой - должно быть [timestamp, value]
            String[] parts = arrayContent.split(",");
            if (parts.length < 2) {
                log.warn("⚠️ Not enough parts in array");
                return 0.0;
            }

            // Берем второе значение (число), убираем кавычки
            String valueStr = parts[1].replace("\"", "").trim();
            log.info("🔢 Value string: '{}'", valueStr);

            double value = Double.parseDouble(valueStr);
            log.info("✅ Successfully parsed value: {}", value);
            return value;

        } catch (Exception e) {
            log.error("❌ Parse error: {}", e.getMessage());
            return 0.0;
        } finally {
            log.info("🏁 END parseSimpleValue");
        }
    }

    /**
     * Тестовый метод для проверки подключения
     */
    public String testConnection() {
        log.info("🧪 START testConnection");

        try {
            String response = executeSimplePromQuery("up");
            log.info("✅ Connection test SUCCESS");
            return "SUCCESS: Connected to Prometheus";

        } catch (Exception e) {
            log.error("❌ Connection test FAILED: {}", e.getMessage());
            return "FAILED: " + e.getMessage();
        } finally {
            log.info("🏁 END testConnection");
        }
    }
}