# Event-Driven Order Processing System

A distributed microservices system built with Apache Kafka, Spring Boot, PostgreSQL, Docker, Prometheus, and Grafana.

## Architecture

- **Orders Service** (port 8081) — accepts order requests, publishes to Kafka
- **Payment Service** (port 8082) — consumes orders, processes payments, publishes results
- **Notification Service** (port 8083) — consumes payment results, dispatches notifications

## Tech Stack

- Java 21, Spring Boot 4.0.5
- Apache Kafka 4.x (event broker)
- PostgreSQL 15 (database per service)
- Micrometer + Prometheus + Grafana (observability)
- Docker + Docker Compose (infrastructure)

## Running Locally

### Prerequisites
- Java 21
- Docker Engine
- Maven

### Start infrastructure
\```bash
cd infrastructure
docker compose up -d
\```

### Start services
Run each service from IntelliJ or:
\```bash
cd orders-service && mvn spring-boot:run
cd payment-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
\```

### Test the system
\```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerEmail": "test@example.com",
    "productName": "MacBook Pro",
    "amount": 150001,
    "idempotencyKey": "unique-key-001"
  }'
\```

## Observability
- Prometheus: http://localhost:9091
- Grafana: http://localhost:3000 (admin/admin)
