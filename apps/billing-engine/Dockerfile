# Stage 1: Сборка
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Копируем только POM для кэширования зависимостей
COPY pom.xml .

# Загружаем зависимости (ускоряет последующие сборки)
RUN mvn dependency:go-offline -B

# Копируем исходный код
COPY src ./src

# Собираем fat-jar (без запуска тестов)
RUN mvn clean package -DskipTests

# Stage 2: Финальный образ
FROM eclipse-temurin:17-jre-alpine

# Создаём непривилегированного пользователя
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

WORKDIR /app

# Копируем JAR из builder-стадии
COPY --from=builder /app/target/billing-engine-*.jar billing-engine.jar

# Меняем владельца
RUN chown appuser:appgroup billing-engine.jar

# Переключаемся на непривилегированного пользователя
USER appuser

# Экспонируем порт
EXPOSE 8081

# Запуск приложения
ENTRYPOINT ["java", "-jar", "billing-engine.jar"]