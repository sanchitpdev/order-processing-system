# syntax=docker/dockerfile:1

# ── Stage 1 — Build the jar ───────────────────────────────────────────────
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Resolve dependencies first so this layer is cached unless pom.xml changes.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Now copy sources and build.
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ── Stage 2 — Run the jar ─────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# curl is used by the container HEALTHCHECK below.
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Run as a non-root user.
RUN groupadd --system app && useradd --system --gid app --create-home appuser

COPY --from=builder /app/target/*.jar app.jar
RUN chown appuser:app app.jar
USER appuser

EXPOSE 8081

HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
    CMD curl -fsS http://localhost:8081/actuator/health || exit 1

# MaxRAMPercentage keeps the JVM inside the container's memory limit.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
