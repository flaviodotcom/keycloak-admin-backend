# Backend Notification

Generic notification service responsible for sending e-mails from REST requests
or Kafka commands and emitting notification result events.

This service is intentionally independent from Keycloak. It sends e-mails by
SMTP through `quarkus-mailer` and publishes the result to `notification.events`.

## Responsibilities

- Consume `notification.commands`.
- Send generic e-mails to `to`, `cc` and `bcc` recipients.
- Support text body, HTML body and base64 attachments.
- Require e-mail commands to include `schemaVersion=1`.
- Require and propagate `correlationId` for cross-service troubleshooting.
- Persist processed `commandId` values in PostgreSQL to avoid resending e-mails
  when Kafka reprocesses a command already marked as sent or in progress.
- Publish `notification.email.sent` after a successful send.
- Publish `notification.email.failed` when SMTP delivery fails, then propagate
  the original error so Kafka retry/DLQ can handle the failed command.
- Serve a dedicated static OpenAPI contract at `/openapi`.

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
