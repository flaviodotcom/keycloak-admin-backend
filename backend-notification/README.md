# Backend Notification

Generic notification service responsible for sending e-mails from REST requests
or Kafka commands and emitting notification result events.

This service is intentionally independent from Keycloak. It sends e-mails by
SMTP through `quarkus-mailer` and publishes the result to `notification.events`.

## Responsibilities

- Consume `notification.commands`.
- Persist valid commands in a PostgreSQL-backed e-mail outbox before executing
  SMTP delivery.
- Send generic e-mails to `to`, `cc` and `bcc` recipients.
- Support text body, HTML body and base64 attachments.
- Require e-mail commands to include `schemaVersion=1`.
- Require and propagate `correlationId` for cross-service troubleshooting.
- Persist processed `commandId` values in PostgreSQL to avoid enqueueing or
  resending e-mails when Kafka reprocesses a command already queued, in
  progress or sent.
- Publish `notification.email.sent` after a successful send.
- Retry SMTP delivery automatically when it fails.
- Publish `notification.email.failed` when SMTP delivery exhausts all retry
  attempts, mark the outbox entry as failed and propagate the original error to
  the scheduler logs.
- Serve a dedicated static OpenAPI contract at `/openapi`.
- Expose liveness and readiness checks. Readiness validates PostgreSQL, Kafka
  and SMTP connectivity. When `quarkus.mailer.mock=true`, SMTP readiness reports
  the mock mode instead of opening a network socket.

Kafka payload examples are documented in
[event contracts](../docs/events/README.md).

## Architecture

The service follows the same layered direction as `backend-keycloak`:

```text
resources/              HTTP entry points
service/                Application service contracts
service/impl/           Use case orchestration
domain/gateway/         Ports used by application services
domain/factory/         Domain event creation
domain/model/           Domain enums and value types
domain/validation/      Contract and business validation
infrastructure/kafka/   Kafka consumers and publishers
infrastructure/mailer/  Quarkus Mailer adapter
infrastructure/scheduler/ Background outbox trigger
repository/             Panache repositories
entities/               JPA persistence entities
dto/                    Public request/response/event contracts
health/                 Health checks
```

Application services depend on domain ports instead of depending directly on
Kafka emitters or Quarkus Mailer. Infrastructure adapters implement those ports.

## Local Run

From the repository root:

```shell
./mvnw -pl backend-notification quarkus:dev
```

With Docker Compose:

```shell
./mvnw -pl backend-notification package
docker compose --profile notification up backend-notification
```

The service listens on `http://localhost:8083/api`.

For local SMTP inspection, open Mailpit at `http://localhost:8025`.

## Integration Tests

Kafka integration tests use Testcontainers and are disabled by default:

```shell
./mvnw -pl backend-notification -DskipITs=false verify
```

## Idempotency And Outbox

The notification flow is split into two steps:

1. A REST request or Kafka message validates the command and stores both the
   command idempotency state and the e-mail outbox row in the same database
   transaction.
2. A scheduled worker claims pending outbox rows with `FOR UPDATE SKIP LOCKED`,
   sends the e-mail, updates the persisted state and publishes the notification
   result event.

This means Kafka reprocessing does not resend an e-mail that is already queued,
being processed or sent. Invalid command payloads still fail before being stored
and continue to use Kafka DLQ behavior.

Outbox processing is configured with:

```properties
notification.outbox.batch-size=10
notification.outbox.processing-interval=2s
notification.outbox.max-attempts=3
notification.outbox.retry-delay=30s
```

When an SMTP attempt fails before `max-attempts`, the outbox row returns to
`PENDING` with `next_attempt_at` set from `retry-delay`. The scheduler still
logs and propagates the original error for visibility. When attempts are
exhausted, the row becomes `FAILED` and the service publishes
`notification.email.failed`.
