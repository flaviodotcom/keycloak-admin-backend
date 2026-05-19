# Local Kafka Testing Guide

This guide is for developers who want to run the full local platform and verify
the end-to-end Kafka flow:

1. `backend-keycloak` performs identity operations.
2. Identity events are published to Kafka.
3. `backend-audit` persists identity and notification events.
4. `backend-keycloak` publishes notification commands.
5. `backend-notification` sends e-mails through Mailpit and publishes
   notification result events.

---

## ⚠️ Important Concept

| Layer | Purpose |
|------|--------|
| Docker Compose `--profile kafka-enabled` | Starts Kafka + services |

---

## Start The Platform

Build the services and start the Kafka-en abled profile:

```shell
./mvnw package -DskipTests
docker compose --profile kafka-enabled up --build
```

If the stack was already running with an older configuration, restart it:

```shell
docker compose down --remove-orphans
docker compose --profile kafka-enabled up --build
```

If Docker reports `network <id> not found`, remove stale local containers and
the old project network, then start again:

```shell
docker compose down --remove-orphans
docker rm -f backend-audit backend-notification backend-keycloak-kafka keycloak-admin-platform keycloak-admin-platform-kafka keycloak-admin-platform-kafka-ui keycloak-admin-platform-mailpit keycloak-admin-platform-postgres-audit keycloak-admin-platform-postgres-notification
docker network rm backend-users_default
docker compose --profile kafka-enabled up --build
```

If the network does not exist, continue with the final `docker compose` command.

## Useful URLs

| Service | URL |
| --- | --- |
| Keycloak Admin Console | `http://localhost:8080` |
| Backend Keycloak API | `http://localhost:8081/api` |
| Backend Keycloak OpenAPI | `http://localhost:8081/openapi` |
| Backend Keycloak Swagger UI | `http://localhost:8081/api/q/swagger-ui` |
| Audit API | `http://localhost:8082/api` |
| Audit OpenAPI | `http://localhost:8082/openapi` |
| Notification API | `http://localhost:8083/api` |
| Notification OpenAPI | `http://localhost:8083/openapi` |
| Kafka UI | `http://localhost:8084` |
| Mailpit | `http://localhost:8025` |

Keycloak admin credentials:

```text
Username: myKeycloakAdminUser
Password: tops3cr3t
```

Example realm imported by Docker Compose:

```text
user-management
```

Example client values used by the local services:

```shell
KEYCLOAK_REALM=user-management
KEYCLOAK_ADMIN_CLIENT_ID=backend-keycloak-admin
KEYCLOAK_ADMIN_CLIENT_SECRET="X7NLxXxrI3EH4wP4gPqOqJ9E9bjgqD45"
KEYCLOAK_OIDC_CLIENT_ID=backend-keycloak
KEYCLOAK_OIDC_CLIENT_SECRET="X7NLxXxrI3EH4wP4gPqOqJ9E9bjgqD45"
```

These values are intentionally committed for local testing only.

## Check Service Health

```shell
curl http://localhost:8081/api/q/health/ready
curl http://localhost:8082/q/health/ready
curl http://localhost:8083/q/health/ready
```

All three services should report `UP`. If the Keycloak readiness check is down,
check whether the `user-management` realm was imported and whether the configured
client secrets match the example values above.

## Create A Group

```shell
curl -i -X POST http://localhost:8081/api/v1/groups \
  -H "Content-Type: application/json" \
  -H "X-Actor-Id: admin@example.com" \
  -d '{"name":"Financeiro"}'
```

Expected result:

```text
HTTP/1.1 201 Created
```

With Kafka enabled, this operation publishes an event to `identity.events`.

## Create A User

```shell
curl -i -X POST http://localhost:8081/api/v1/users \
  -H "Content-Type: application/json" \
  -H "X-Actor-Id: admin@example.com" \
  -d '{
    "username": "maria.teste",
    "email": "maria.teste@email.com",
    "firstName": "Maria",
    "lastName": "Teste",
    "enabled": true,
    "emailVerified": false,
    "attributes": {
      "cpf": ["12345678901"],
      "departamento": ["FINANCEIRO"]
    }
  }'
```

Expected result:

```text
HTTP/1.1 201 Created
Location: http://localhost:8081/api/v1/users/<USER_ID>
```

Save the created user id. You will use it to trigger the update-password e-mail.

## Search Users

```shell
curl "http://localhost:8081/api/v1/users?search=Maria" \
  -H "X-Actor-Id: admin@example.com"
```

The response should include `maria.teste`.

## Inspect Kafka

Open Kafka UI:

```text
http://localhost:8084
```

Expected topics:

```text
identity.events
notification.commands
notification.events
```

After creating the group and user, inspect `identity.events`. You should see
identity event payloads with:

- `schemaVersion`
- `eventId`
- `eventType`
- `correlationId`
- `actor`
- `subject`

## Send Update Password E-mail Through Kafka

Use the user id returned by the create-user response:

```shell
curl -i -X POST http://localhost:8081/api/v1/users/<USER_ID>/actions/update-password-email \
  -H "X-Actor-Id: admin@example.com"
```

Expected result:

```text
HTTP/1.1 204 No Content
```

In Kafka UI, inspect:

- `notification.commands`: command published by `backend-keycloak`.
- `notification.events`: result event published by `backend-notification`.

The `backend-notification` service uses an outbox table before sending e-mail.
If the command is processed successfully, the outbox status becomes `SENT`.

## Check Mailpit

Open:

```text
http://localhost:8025
```

You should see an e-mail with:

```text
To: maria.teste@email.com
Subject: Update your password
```

If the message does not appear immediately, refresh Mailpit and check the
`backend-notification` logs:

```shell
docker compose logs --tail=120 backend-notification
```

Look for:

```text
Notification command consumed
Notification command enqueued
Notification outbox sent
Notification event published
```

## Check Audit Events

Query audit events:

```shell
curl http://localhost:8082/api/v1/audit-events
```

You should see identity events and notification events persisted by
`backend-audit`.

You can also inspect the audit database directly:

```shell
docker compose exec postgres-audit psql -U audit -d audit -c 'select event_id, event_type, correlation_id, actor_id, subject_type, subject_id, topic from audit_events order by id desc limit 10;'
```

## Troubleshooting

### No E-mail In Mailpit

First check that `backend-keycloak` published the notification command:

```shell
docker compose logs --tail=120 backend-keycloak-kafka
```

Look for:

```text
Notification command published
```

Then check that `backend-notification` consumed and sent it:

```shell
docker compose logs --tail=160 backend-notification
```

If `backend-keycloak` published the command but `backend-notification` did not
consume it, recreate Kafka and the services that depend on it:

```shell
docker compose --profile kafka-enabled up -d --force-recreate kafka backend-audit backend-notification backend-keycloak-kafka
```

Kafka runs as a single local broker. The compose file sets replication-related
configuration to `1` so internal topics such as `__consumer_offsets` can be
created correctly.

### Verify The Notification Outbox

```shell
docker compose exec postgres-notification psql -U notification -d notification -c 'select command_id, status, attempts, error_message from notification_email_outbox order by id desc limit 10;'
```

Expected successful status:

```text
SENT
```

### Inspect Kafka Consumer Group

The broker image is minimal, so use the Apache Kafka CLI image:

```shell
docker run --rm --network backend-users_default apache/kafka:3.8.0 \
  /opt/kafka/bin/kafka-consumer-groups.sh \
  --bootstrap-server kafka:9092 \
  --describe \
  --group backend-notification
```

The `backend-notification` group should be assigned to
`notification.commands`.
