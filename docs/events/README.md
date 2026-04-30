# Event Contracts

This directory documents the Kafka contracts used by the platform services.
Contracts are versioned with the `schemaVersion` field. Consumers reject
messages without `schemaVersion` or with an unsupported version.

## Topics

| Topic | Producer | Consumer | Purpose |
| --- | --- | --- | --- |
| `identity.events` | `backend-keycloak` | `backend-audit` | Identity operation facts. |
| `notification.commands` | `backend-keycloak` or any trusted service | `backend-notification` | Generic e-mail delivery commands. |
| `notification.events` | `backend-notification` | `backend-audit` | Notification delivery facts. |

## Dead Letter Topics

| Source Topic | DLQ Topic |
| --- | --- |
| `identity.events` | `identity.events.audit.dlq` |
| `notification.commands` | `notification.commands.dlq` |
| `notification.events` | `notification.events.audit.dlq` |

## Required Contract Rules

- `schemaVersion` is required and must be `1`.
- `eventId` is required for identity and notification events.
- `commandId` is required for notification commands and notification events.
- `correlationId` is required for notification commands and is propagated to
  identity and notification events for cross-service tracing.
- `eventType` is required for all events.
- `source` identifies the producer service.
- `actor.id` identifies who requested the operation when available.
- Consumers do not apply alternate compatibility paths. Invalid messages fail
  and are handled by Kafka retry/DLQ behavior.
- `backend-notification` stores processed command ids and e-mail outbox rows in
  PostgreSQL. Commands already marked as `QUEUED`, `PROCESSING` or `SENT` are
  acknowledged without enqueueing or sending the e-mail again.
- SMTP delivery failures are retried by the outbox worker until
  `notification.outbox.max-attempts` is reached. `notification.email.failed` is
  published only after retry attempts are exhausted.

## Current Payloads

- [identity.user.created.v1.json](identity.user.created.v1.json)
- [notification.command.email.v1.json](notification.command.email.v1.json)
- [notification.email.sent.v1.json](notification.email.sent.v1.json)
- [notification.email.failed.v1.json](notification.email.failed.v1.json)

## Local Validation

Run unit tests and compile all modules:

```shell
./mvnw test
```

Run Kafka/Testcontainers integration tests when Docker is available:

```shell
./mvnw -DskipITs=false verify
```

OpenAPI endpoints:

```text
backend-keycloak:      http://localhost:8081/openapi
backend-audit:         http://localhost:8082/openapi
backend-notification:  http://localhost:8083/openapi
```
