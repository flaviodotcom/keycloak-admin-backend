# Keycloak Admin Platform

Open source identity administration platform built around Keycloak, Kafka and
Quarkus.

This repository is organized as a monorepo with three services:

- `backend-keycloak`: REST API that centralizes Keycloak realm
  administration.
- `backend-audit`: Kafka consumer that persists identity and notification audit
  events.
- `backend-notification`: generic notification service that consumes
  notification commands, sends e-mails and emits result events without depending
  on Keycloak.

## Repository Layout

```text
backend-keycloak/   Current Keycloak admin API
backend-audit/            Audit microservice
backend-notification/     Notification microservice
docs/plans/               Architecture and implementation plans
docs/architecture/        C4 architecture diagrams
docs/events/              Kafka event and command contracts
docker-compose.yml        Local platform dependencies and services
pom.xml                   Maven aggregator
```

## Current Status

The existing Keycloak administration backend lives in `backend-keycloak`.
The repository also includes the first versions of the audit and notification
services:

- `backend-audit` consumes identity and notification events from Kafka and
  persists them in PostgreSQL.
- `backend-notification` consumes generic e-mail commands, sends e-mails through
  SMTP and publishes notification result events.

## Build

This project targets Java 21.

Run the current admin backend tests from the repository root:

```shell
./mvnw -pl backend-keycloak test
```

Run the full current build:

```shell
./mvnw verify
```

Run Kafka/Testcontainers integration tests when Docker is available:

```shell
./mvnw -DskipITs=false verify
```

## Run Admin Backend In Dev Mode

```shell
./mvnw -pl backend-keycloak quarkus:dev
```

The admin API is exposed at:

```text
http://localhost:8081/api
```

OpenAPI remains at:

```text
http://localhost:8081/openapi
```

## Local Compose

Start the platform dependencies:

```shell
docker compose up -d keycloak kafka kafka-ui postgres-audit postgres-notification mailpit
```

The service containers use each module's JVM Dockerfile and expect packaged
Quarkus artifacts under `target/quarkus-app`. Build them before starting the
service profiles:

```shell
./mvnw package
```

Start the admin backend with Compose:

```shell
docker compose --profile admin up backend-keycloak
```

Start audit and notification services:

```shell
docker compose --profile audit --profile notification up backend-audit backend-notification
```

Start the whole local platform:

```shell
docker compose --profile admin --profile audit --profile notification up
```

Start the local platform with Kafka enabled in the admin backend:

```shell
docker compose --profile kafka-enabled up --build
```

This starts the admin backend with `IDENTITY_EVENTS_ENABLED=true` and
`NOTIFICATION_COMMANDS_ENABLED=true`, plus Kafka, audit, notification,
PostgreSQL, Mailpit and Keycloak. In this mode, identity mutations publish
`identity.events`, and update-password e-mail actions publish
`notification.commands`.

Mailpit is exposed at:

```text
http://localhost:8025
```

OpenAPI endpoints:

```text
backend-keycloak:      http://localhost:8081/openapi
backend-audit:         http://localhost:8082/openapi
backend-notification:  http://localhost:8083/openapi
```

## Event Architecture

The target architecture uses Kafka to decouple Keycloak administration from
auditing and notification delivery.

Topics:

- `identity.events`: facts emitted after identity operations.
- `notification.commands`: commands requesting generic e-mail delivery.
- `notification.events`: facts emitted after notification delivery succeeds or
  fails.

The Kafka payloads are documented in [event contracts](docs/events/README.md).
Every contract has a required `schemaVersion`; the current version is `1`.
Consumers reject invalid payloads and rely on Kafka retry/DLQ handling instead
of applying alternate compatibility paths.
`correlationId` is propagated through events and commands to connect HTTP
requests, Kafka messages and service logs.

Consumers:

- `backend-audit` consumes `identity.events` and `notification.events`.
- `backend-notification` consumes `notification.commands`.

When `IDENTITY_EVENTS_ENABLED=true`, `backend-keycloak` publishes identity
events after user, group and role create/update/delete operations. In that mode,
requests must include `X-Actor-Id` unless the API is protected by OIDC and the
authenticated principal can be used as the actor.

When `NOTIFICATION_COMMANDS_ENABLED=true`, the admin backend publishes generic
e-mail commands to `notification.commands` for supported notification actions.
The update-password email endpoint uses this mode instead of Keycloak SMTP.
`backend-notification` persists processed command ids and an e-mail outbox in
PostgreSQL so Kafka reprocessing does not enqueue or send e-mails for commands
already queued, in progress or sent. SMTP failures are retried by the outbox
worker until `notification.outbox.max-attempts` is reached; only then is
`notification.email.failed` published.

See [identity-events architecture](docs/plans/2026-04-29-identity-events-architecture.md)
for the detailed plan.

See [C4 architecture diagrams](docs/architecture/c4.md) for the system context
and container view.

## Service Documentation

- [Admin backend](backend-keycloak/README.md)
- [Audit service](backend-audit/README.md)
- [Notification service](backend-notification/README.md)
