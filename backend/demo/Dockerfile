# ==========================================================
# RESTAURANT MANAGEMENT - BACKEND
# Spring Boot 3.2.5 / Java 21
# ==========================================================

FROM eclipse-temurin:21-jdk-alpine AS runtime

WORKDIR /app

# Create a non-root user
RUN addgroup -S spring && adduser -S spring -G spring

COPY target/restaurant-management.jar app.jar

RUN chown spring:spring app.jar

USER spring:spring

# Spring Boot
EXPOSE 8080

# Container-aware JVM configuration
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["java", "-jar", "app.jar"]