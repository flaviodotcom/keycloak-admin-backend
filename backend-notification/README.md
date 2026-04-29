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
- Publish `notification.email.sent` after a successful send.
- Publish `notification.email.failed` when SMTP delivery fails, then propagate
  the original error so Kafka retry/DLQ can handle the failed command.

Kafka payload examples are documented in
[event contracts](../docs/events/README.md).

## Local Run

From the repository root:

```shell
./mvnw -pl backend-notification quarkus:dev
```

With Docker Compose:

```shell
docker compose --profile notification up backend-notification
```

The service listens on `http://localhost:8083/api`.

For local SMTP inspection, open Mailpit at `http://localhost:8025`.

## Integration Tests

Kafka integration tests use Testcontainers and are disabled by default:

```shell
./mvnw -pl backend-notification -DskipITs=false verify
```
