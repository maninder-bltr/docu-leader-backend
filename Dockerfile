# =============================================================================
# Stage 1: Build the application
# =============================================================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Install build dependencies
RUN apk add --no-cache gradle bash

# Copy build configuration first (for better layer caching)
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Make gradlew executable and download dependencies
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

# Copy source code
COPY src ./src

# Build the application (skip tests for faster CI/CD)
RUN ./gradlew bootJar -x test --no-daemon

# =============================================================================
# Stage 2: Runtime image
# =============================================================================
FROM eclipse-temurin:21-jre-alpine

# Labels for Render/metadata
LABEL org.opencontainers.image.source="https://github.com/maninder-bltr/docu-leader-backend"
LABEL org.opencontainers.image.description="DocuLeader - AI Document Intelligence Platform"
LABEL maintainer="maninder.bltr@gmail.com"

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder --chown=spring:spring /app/build/libs/*.jar app.jar

# Expose port (Render will override with $PORT env var)
EXPOSE 8088

# Health check endpoint (Spring Boot Actuator)
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8088/actuator/health || exit 1

# JVM optimizations for containerized environments
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -XX:+ParallelRefProcEnabled \
               -XX:MaxGCPauseMillis=200 \
               -XX:+UnlockExperimentalVMOptions \
               -XX:+DisableExplicitGC \
               -XX:+AlwaysPreTouch \
               -Djava.security.egd=file:/dev/./urandom"

# Spring profiles and server port (Render sets PORT env var)
ENV SPRING_PROFILES_ACTIVE=default \
    SERVER_PORT=8088

# Entry point: Let Render's $PORT override server.port via command line
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8088} -jar app.jar"]
