FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the JAR file
COPY target/billing-engine-1.0.0.jar app.jar

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]