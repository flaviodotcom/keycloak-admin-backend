# Backend Audit

Kafka consumer responsible for persisting identity and notification audit
events.

## Responsibilities

- Consume `identity.events`.
- Consume `notification.events`.
- Persist the raw event payload and indexed audit fields in PostgreSQL.
- Require event payloads to include `schemaVersion`, `eventId` and `eventType`.
- Preserve idempotency by ignoring duplicated `eventId` values.
- Expose audit records through `GET /api/v1/audit-events`.

Kafka payload examples are documented in
[event contracts](../docs/events/README.md).

## Local Run

From the repository root:

```shell
./mvnw -pl backend-audit quarkus:dev
```

With Docker Compose:

```shell
docker compose --profile audit up backend-audit
```

The service listens on `http://localhost:8082/api`.

## Integration Tests

Kafka/PostgreSQL integration tests use Testcontainers and are disabled by
default:

```shell
./mvnw -pl backend-audit -DskipITs=false verify
```
