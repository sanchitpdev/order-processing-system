# Event-Driven Order Processing System

A distributed microservices system built with Apache Kafka, Spring Boot 4, PostgreSQL,
Docker, Prometheus, and Grafana.

## Architecture

```
   POST /api/orders                 order.placed              payment.processed
client ─────────────▶ order-service ───────────▶ payment-service ───────────▶ notification-service
                          │                          │                              │
                       orders-db                 payments-db                   (logs notifications)
```

- **order-service** (port 8081) — accepts order requests, persists them, publishes `order.placed` events.
- **payment-service** (port 8082) — consumes `order.placed`, processes payments (with non-blocking
  retries + a dead-letter topic), publishes `payment.processed` events.
- **notification-service** (port 8083) — consumes `payment.processed`, dispatches notifications.

Each event is keyed by `idempotencyKey`, and both order- and payment-service reject duplicate
keys, so the pipeline is safe to retry end-to-end.

## Tech Stack

- Java 21, Spring Boot 4.0.5 (Spring Framework 7, Jackson 3)
- Apache Kafka (Spring for Apache Kafka 4.x client)
- PostgreSQL 15 (database per stateful service)
- Micrometer + Prometheus + Grafana (observability)
- Docker + Docker Compose

## Running the whole stack with Docker

From the `infrastructure/` directory:

```bash
cd infrastructure
docker compose up --build
```

Compose builds each service image (multi-stage Maven build) and starts everything in
dependency order. Application services wait for Kafka and their database to report
**healthy** before they boot, so there is no start-up race. First build downloads
dependencies and can take a few minutes.

Stop and clean up (including volumes):

```bash
docker compose down -v
```

## Test the system

```bash
# Succeeds (amount not divisible by 10):
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerEmail": "test@example.com",
    "productName": "MacBook Pro",
    "amount": 150001,
    "idempotencyKey": "unique-key-001"
  }'

# Fails (amount divisible by 10 -> exercises retry + DLQ path):
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerEmail": "test@example.com",
    "productName": "Keyboard",
    "amount": 1500,
    "idempotencyKey": "unique-key-002"
  }'
```

Watch the flow with:

```bash
docker compose logs -f order-service payment-service notification-service
```

The payment outcome is a deterministic stand-in for a real gateway: **amounts divisible
by 10 fail; everything else succeeds.** Failed events are retried on suffixed retry topics
and finally land on a `.DLQ` topic.

### Request validation

`POST /api/orders` validates its body. A bad request returns `400` with details, e.g.:

```json
{ "status": 400, "error": "Bad Request", "message": "Validation failed",
  "details": { "customerEmail": "customerEmail must be a valid email address" } }
```

## Observability

- Health:     http://localhost:8081/actuator/health (and :8082, :8083)
- Prometheus: http://localhost:9091
- Grafana:    http://localhost:3000  (admin / admin) — add Prometheus
  (`http://prometheus:9090`) as a data source.

## Running a single service locally (without Docker)

Start only the infrastructure, then run a service from your IDE or Maven:

```bash
cd infrastructure && docker compose up -d kafka zookeeper orders-db payments-db
cd ../order-service && ./mvnw spring-boot:run
```

> Note: the bundled tests are `@SpringBootTest` context-load checks that need Kafka and
> Postgres running. The Docker image build runs `-DskipTests`; run `mvn test` locally only
> with the infrastructure up.

## Notes / next steps

- The broker runs in the classic ZooKeeper mode (Confluent `cp-kafka:7.5.0`) because it is
  battle-tested for local use. Migrating to **KRaft** (no ZooKeeper) is the recommended
  next step to match modern Kafka deployments.
