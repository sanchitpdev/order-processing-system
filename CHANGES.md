# What was fixed and improved

This document summarises every change made to the original project.

> Important context: the original code targets **Spring Boot 4.0.5** (released
> March 2026, built on Spring Framework 7 + Jackson 3). In Boot 4 several starters
> were renamed and reorganised, so a few things that *look* broken are actually
> valid Boot 4 artifacts. The real issues are listed below.

## Bugs fixed (these would break the build or the running system)

1. **Compile error in `OrderController`** — the log line accessed `request.customerEmail`
   as a field. Records expose their components as *methods*, so this does not compile.
   Changed to `request.customerEmail()`.

2. **Deprecated/at-risk Kafka JSON serialization** — the YAML used
   `org.springframework.kafka.support.serializer.JsonSerializer` / `JsonDeserializer`,
   which in Spring Kafka 4.0 are **deprecated for removal** and rely on Jackson 2, while
   Boot 4 ships Jackson 3. The poms also declared a **version-less**
   `com.fasterxml.jackson.core:jackson-databind` (Jackson 2), a fragile/unnecessary
   dependency. Migrated to the Jackson 3 equivalents
   `JacksonJsonSerializer` / `JacksonJsonDeserializer` (the `spring.json.*` property
   keys are unchanged) and removed the explicit Jackson 2 dependency.

3. **Docker start-up race** — `depends_on` listed only container names, so the services
   started before Kafka/Postgres were *ready* and crash-looped. Added health checks to
   Kafka and the Postgres databases and switched the application services to
   `depends_on: { condition: service_healthy }`.

## Cleanups

4. **Removed `@EnableRetry` + the `spring-retry` dependency** from payment-service.
   `@RetryableTopic` is Spring Kafka's own non-blocking retry mechanism and, in 4.0, its
   `@BackOff` annotation (`org.springframework.kafka.annotation.BackOff`, which the code
   already uses correctly) comes from Spring Kafka — spring-retry was unused.

5. **Removed the redundant `spring-kafka` dependency** from order-service
   (`spring-boot-starter-kafka` already pulls it in transitively).

6. **Standardised the three poms** on consistent Boot 4 starters
   (`spring-boot-starter-webmvc`, `spring-boot-starter-kafka`, and the matching
   per-module `*-test` starters) and removed empty boilerplate metadata blocks.

7. **Renamed `publicOrderPlaced` → `publishOrderPlaced`** (typo) in the order producer
   and its caller.

8. **Removed the unused `notifications-db`** from Compose (notification-service has no
   database) and renamed the Compose service `orders-service` → `order-service` to match
   the module/artifact name (Prometheus target updated to match).

9. **Corrected a misleading comment** in `PaymentService.simulatePayment` — it claimed an
   "80% success rate" but the logic is deterministic (amounts divisible by 10 fail).

## Feature improvements

10. **Request validation** — `OrderRequest` now uses Bean Validation
    (`@NotBlank`, `@Email`, `@NotNull`, `@Positive`) via `spring-boot-starter-validation`,
    and the controller validates with `@Valid`.

11. **Global exception handler** — new `GlobalExceptionHandler` returns clean, consistent
    JSON for validation failures (`400`) and unexpected errors (`500`) instead of leaking
    stack traces.

12. **Hardened Dockerfiles** — multi-stage build with dependency-layer caching, a
    **non-root** runtime user, a container `HEALTHCHECK` against `/actuator/health`,
    container-aware JVM memory (`-XX:MaxRAMPercentage=75`), and a `.dockerignore` per
    service.

13. **More reliable producers** — `acks=all` and `retries=3` on the Kafka producers.

14. **Kubernetes-style health probes** enabled (`management.endpoint.health.probes`).

## Verified against official docs (Boot 4 / Spring Kafka 4.0)

- `spring-boot-starter-webmvc` is the Boot 4 name for the old `spring-boot-starter-web`.
- `spring-boot-starter-kafka`, `spring-boot-starter-validation`, and the granular
  `*-test` starters all exist as real Boot 4 artifacts.
- `@RetryableTopic(backOff = @BackOff(...))` with
  `org.springframework.kafka.annotation.BackOff` is correct for Spring Kafka 4.0
  (it changed from `backoff = @Backoff` in 3.x), so that code was left as-is.

## Could not run here

This environment has no Docker daemon and no access to Maven Central, so the build was
**not executed** here. All fixes were made by analysis and verified against Spring's
official documentation. Build it on your machine with `docker compose up --build` from
`infrastructure/`.
