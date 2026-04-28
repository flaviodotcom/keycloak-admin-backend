# Keycloak Admin Backend

Generic REST backend for managing a Keycloak realm.

This project exists to centralize identity administration behind a stable, clean
and maintainable API. Instead of coupling applications directly to Keycloak Admin
Client details, this service exposes common user, group, role and User Profile
attribute operations through a small REST surface.

The current implementation is designed as an open source foundation: the domain
contracts are separated from Keycloak infrastructure, errors are normalized, API
messages are internationalized, and the code is organized so future identity
features can be added without spreading Keycloak-specific logic across the
application.

## Features

- User CRUD: create, update, find by id, search and delete users.
- Group CRUD: create, update, find by id, search and delete groups.
- Role CRUD: create, update, find by id, search and delete realm roles.
- Nested group support when searching groups.
- Group membership lookup through `GET /v1/groups/{id}/members`.
- User creation with optional group assignment through `groupIds`.
- Optional group expansion in user responses with `includeGroups=true`.
- Paginated list responses with `page`, `size`, `sort` and `sortBy`.
- Keycloak update-password email action after user creation.
- Managed User Profile attribute creation.
- Case-insensitive and accent-insensitive search support for configured fields.
- Attribute search support using `attr.<name>` query parameters.
- Localized error responses using `Accept-Language`.
- Centralized translation of common Keycloak errors into clearer API problems.

## Tech Stack

- Java 25
- Quarkus 3.34.6
- Maven Wrapper
- Keycloak Admin REST Client
- Quarkus REST with Jackson
- Hibernate Validator
- SmallRye OpenAPI
- SmallRye Health
- Lombok
- JUnit, Mockito and REST Assured

## Architecture

The project follows a simple layered architecture:

```text
resources/                 HTTP resources and request entry points
resources/support/         URI/query parameter mapping
service/                   Application use cases
domain/identity/           Domain models, commands, criteria and gateway ports
domain/shared/             Shared domain helpers, such as text matching
infrastructure/keycloak/   Keycloak gateway implementations and mappers
dto/                       Public request and response DTOs
exceptions/                Problem response handling
i18n/                      Locale resolution and message bundle access
health/                    Liveness and readiness checks
```

Keycloak-specific code is intentionally kept in `infrastructure/keycloak`.
Application services depend on domain gateway interfaces, not on Keycloak Admin
Client classes. This keeps the HTTP/API layer readable and makes it easier to add
new use cases later, such as role assignments, password actions, or more User
Profile operations.

## Requirements

- JDK 21 or newer.
- A running Keycloak server.
- A Keycloak realm configured for this backend.
- A confidential Keycloak client with enough service account permissions to
  manage users, groups, roles and User Profile attributes.

The Maven Wrapper is included, so Maven does not need to be installed globally.

## Configuration

The API listens on port `8081` by default.

```properties
quarkus.http.port=8081
quarkus.default-locale=en
```

Keycloak configuration can be supplied through environment variables:

| Property | Environment variable | Local default |
| --- | --- | --- |
| `keycloak.server-url` | `KEYCLOAK_SERVER_URL` | `http://localhost:8080` |
| `keycloak.realm` | `KEYCLOAK_REALM` | `user-management` |
| `quarkus.keycloak.admin-client.client-id` | `KEYCLOAK_ADMIN_CLIENT_ID` | `backend-keycloak-admin` |
| `quarkus.keycloak.admin-client.client-secret` | `KEYCLOAK_ADMIN_CLIENT_SECRET` | local development secret |
| `quarkus.oidc.client-id` | `KEYCLOAK_OIDC_CLIENT_ID` | `backend-users` |
| `quarkus.oidc.credentials.secret` | `KEYCLOAK_OIDC_CLIENT_SECRET` | local development secret |

For real environments, provide secrets through environment variables or your
deployment secret manager. Do not rely on local development values.

## Running Locally

Start Keycloak first, then run the application:

```shell
./mvnw quarkus:dev
```

API base URL:

```text
http://localhost:8081
```

OpenAPI document:

```text
http://localhost:8081/openapi
```

Swagger UI:

```text
http://localhost:8081/q/swagger-ui
```

Health endpoints:

```text
http://localhost:8081/q/health
http://localhost:8081/q/health/live
http://localhost:8081/q/health/ready
```

The liveness check only reports whether the application process is alive. The
readiness check validates basic Keycloak connectivity by reading the configured
realm representation. If Keycloak is unavailable or the admin client cannot
access the realm, readiness fails and the pod should not receive traffic.

## Authentication

The project is integrated with Quarkus OIDC, so it can validate bearer tokens
issued by the configured Keycloak realm/client.

By default, the current REST endpoints are not protected with security
annotations. This keeps the open source base flexible for different deployment
models, but production applications should explicitly protect resources
according to their needs.

Use Quarkus security annotations such as:

- `@Authenticated` when any authenticated user may access the endpoint.
- `@RolesAllowed` when access depends on one or more roles.
- `@PermitAll` when an endpoint should intentionally remain public.

Example request when an endpoint is protected:

```http
Authorization: Bearer <access-token>
```

## API Overview

List endpoints return a paginated response:

```json
{
  "content": [],
  "page": 0,
  "size": 10,
  "totalElements": 0,
  "totalPages": 0,
  "first": true,
  "last": true
}
```

Pagination defaults:

- `page`: `0`
- `size`: `10`
- `sort`: `asc`
- `size` maximum: `100`

The API applies pagination after the final in-memory filtering and sorting step.
This is intentional: some filters are case-insensitive, accent-insensitive or
depend on data that Keycloak cannot page correctly by itself. Applying API
pagination before those filters could return incomplete pages.

### Users

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/v1/users` | Search users. |
| `GET` | `/v1/users/{id}` | Find a user by id. |
| `POST` | `/v1/users` | Create a user. |
| `PUT` | `/v1/users/{id}` | Replace a user. |
| `PATCH` | `/v1/users/{id}` | Partially update a user. |
| `DELETE` | `/v1/users/{id}` | Delete a user. |

Supported query parameters for `GET /v1/users`:

| Parameter | Description |
| --- | --- |
| `search` | Generic user search term. |
| `username` | Filters by username. |
| `email` | Filters by email. |
| `firstName` | Filters by first name. |
| `lastName` | Filters by last name. |
| `enabled` | Filters by enabled state. Accepts `true` or `false`. |
| `exact` | Controls exact matching. Accepts `true` or `false`. |
| `includeGroups` | When `true`, includes lightweight group objects in each user response. |
| `attr.<name>` | Filters by a User Profile attribute. |
| `page` | Zero-based page index. Defaults to `0`. |
| `size` | Page size. Defaults to `10`, maximum `100`. |
| `sort` | Sort direction. Accepts `asc` or `desc`. Defaults to `asc`. |
| `sortBy` | Sort field. Supports `id`, `username`, `email`, `firstName`, `lastName`, `enabled`, `createdTimestamp`. Defaults to `username`. |

Example:

```http
GET /v1/users?search=Maria&enabled=true&attr.departamento=RH&page=0&size=10&sortBy=username&sort=asc
```

Including groups:

```http
GET /v1/users?includeGroups=true
GET /v1/users/{id}?includeGroups=true
```

When `includeGroups=true` is used, each user may contain:

```json
{
  "content": [
    {
      "id": "user-id",
      "username": "maria.teste",
      "email": "maria.teste@email.com",
      "firstName": "Maria",
      "lastName": "Teste",
      "enabled": true,
      "emailVerified": false,
      "createdTimestamp": 1777039859679,
      "attributes": {
        "departamento": ["RH"]
      },
      "groups": [
        {
          "id": "group-id",
          "name": "Financeiro",
          "path": "/Financeiro"
        }
      ]
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

When `includeGroups` is not provided, `groups` is omitted from the response.

Create user:

```http
POST /v1/users
Content-Type: application/json
```

```json
{
  "username": "maria.teste",
  "email": "maria.teste@email.com",
  "firstName": "Maria",
  "lastName": "Teste",
  "enabled": true,
  "emailVerified": false,
  "attributes": {
    "cpf": ["12345678901"],
    "departamento": ["RH"]
  },
  "groupIds": ["group-id-1"]
}
```

Defaults:

- `enabled` defaults to `true`.
- `emailVerified` defaults to `false`.
- `attributes` defaults to an empty map.
- `groupIds` defaults to an empty list.

After the user is created, the backend assigns the informed groups and asks
Keycloak to send the `UPDATE_PASSWORD` required action email to the user's email.
SMTP must be configured in Keycloak for that email action to succeed.

Replace user:

```http
PUT /v1/users/{id}
Content-Type: application/json
```

```json
{
  "username": "maria.teste",
  "email": "maria.teste@email.com",
  "firstName": "Maria",
  "lastName": "Teste",
  "enabled": true,
  "emailVerified": true,
  "attributes": {
    "cpf": ["12345678901"],
    "departamento": ["TI"]
  }
}
```

`PUT /v1/users/{id}` has full-replace semantics. Fields that default in the
request DTO keep their defaults, and `attributes` defaults to an empty map. This
means a `PUT` request without `attributes` replaces the user with no attributes.

Partially update user:

```http
PATCH /v1/users/{id}
Content-Type: application/json
```

```json
{
  "firstName": "Maria Clara",
  "enabled": true
}
```

`PATCH /v1/users/{id}` only changes fields present in the request. Missing
fields keep their current Keycloak values. Attribute behavior is explicit:

- missing `attributes`: preserves current attributes;
- `attributes: {}`: clears attributes;
- `attributes` with values: replaces the full attribute map and rebuilds
  internal search attributes when needed.

### Groups

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/v1/groups` | Search groups, including nested groups. |
| `GET` | `/v1/groups/{id}` | Find a group by id. |
| `GET` | `/v1/groups/{id}/members` | List users that belong to a group. |
| `POST` | `/v1/groups` | Create a group. |
| `PUT` | `/v1/groups/{id}` | Update a group. |
| `DELETE` | `/v1/groups/{id}` | Delete a group. |

Supported query parameters for `GET /v1/groups`:

| Parameter | Description |
| --- | --- |
| `search` | Generic group search term. |
| `name` | Filters by group name. |
| `exact` | Controls exact matching. Accepts `true` or `false`. |
| `attr.<name>` | Filters by a group attribute. |
| `page` | Zero-based page index. Defaults to `0`. |
| `size` | Page size. Defaults to `10`, maximum `100`. |
| `sort` | Sort direction. Accepts `asc` or `desc`. Defaults to `asc`. |
| `sortBy` | Sort field. Supports `id`, `name`, `path`. Defaults to `name`. |

Create group:

```json
{
  "name": "Financeiro",
  "parentGroupId": "parent-group-id",
  "attributes": {
    "departmentCode": ["FIN"]
  }
}
```

If `parentGroupId` is provided, the new group is created as a subgroup. If it is
not provided, the group is created at the realm root.

Update group:

```json
{
  "name": "Financeiro",
  "attributes": {
    "departmentCode": ["FIN"]
  }
}
```

Group members:

```http
GET /v1/groups/{id}/members?page=0&size=10&sortBy=username&sort=asc
```

The response is paginated and contains users. It does not include each member's
groups unless a future endpoint explicitly supports that expansion.

Supported member sort fields are `id`, `username`, `email`, `firstName`,
`lastName`, `enabled` and `createdTimestamp`.

### Roles

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/v1/roles` | Search realm roles. |
| `GET` | `/v1/roles/{id}` | Find a realm role by id. |
| `POST` | `/v1/roles` | Create a realm role. |
| `PUT` | `/v1/roles/{id}` | Update a realm role. |
| `DELETE` | `/v1/roles/{id}` | Delete a realm role. |

Supported query parameters for `GET /v1/roles`:

| Parameter | Description |
| --- | --- |
| `name` | Filters by role name. |
| `exact` | Controls exact matching. Accepts `true` or `false`. |
| `page` | Zero-based page index. Defaults to `0`. |
| `size` | Page size. Defaults to `10`, maximum `100`. |
| `sort` | Sort direction. Accepts `asc` or `desc`. Defaults to `asc`. |
| `sortBy` | Sort field. Supports `id`, `name`, `description`. Defaults to `name`. |

Create role:

```json
{
  "name": "manager",
  "description": "Manager role"
}
```

Update role:

```json
{
  "name": "manager",
  "description": "Updated manager role"
}
```

### User Profile Attributes

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/v1/users/attributes` | Create a Keycloak User Profile attribute managed by this backend. |

Create attribute:

```json
{
  "name": "cpf",
  "displayName": {
    "en": "CPF",
    "pt-BR": "CPF"
  },
  "insensitive": false,
  "required": false,
  "multivalued": false
}
```

Fields:

| Field | Required | Description |
| --- | --- | --- |
| `name` | Yes | Public attribute name. Internal names prefixed with `__search_` are reserved. |
| `displayName` | Yes | Map of localized labels used by Keycloak User Profile. |
| `insensitive` | Yes | Enables case-insensitive and accent-insensitive search metadata for this attribute. |
| `required` | No | Whether Keycloak should require the attribute. Defaults to `false`. |
| `multivalued` | No | Whether Keycloak should allow multiple values. Defaults to `false`. |

When `insensitive=true`, the backend maintains an internal normalized attribute
named `__search_<attributeName>` on user create/update operations. That internal
attribute is used for searching and is reserved for backend use.

Existing Keycloak User Profile attributes can be used by users. If a user request
contains an attribute that is not configured in Keycloak User Profile, the API
rejects the request.

## Search Behavior

The backend keeps Keycloak filters where they are reliable and applies additional
matching where Keycloak does not provide the expected behavior.

For users, the `search` parameter is expanded across supported user fields so
searches like these work consistently:

```http
GET /v1/users?search=Conceição
GET /v1/users?firstName=José
GET /v1/users?firstName=jose
```

For text matching, fields configured as insensitive are matched without case or
accent differences. For example, `José`, `Jose`, `josé` and `jose` can match when
the field supports insensitive matching.

Attribute filters use the `attr.` prefix:

```http
GET /v1/users?attr.departamento=RH
GET /v1/groups?attr.departmentCode=FIN
```

## Query Parameter Rules

The API validates query parameters strictly:

- Unsupported query parameters return `400 Bad Request`.
- Repeated query parameters return `400 Bad Request`.
- Blank query parameter values return `400 Bad Request`.
- Boolean values accept only `true` or `false`, case-insensitive.
- `page` must be greater than or equal to `0`.
- `size` must be between `1` and `100`.
- `sort` accepts only `asc` or `desc`.
- `sortBy` accepts only fields supported by the requested resource.
- Attribute filter names cannot be blank.

Invalid examples:

```http
GET /v1/users?enabled=true&enabled=false
GET /v1/users?username=
GET /v1/users?unknown=value
GET /v1/users?attr.=value
GET /v1/users?page=-1
GET /v1/users?size=500
GET /v1/users?sort=random
```

Request DTO validation also rejects invalid emails, blank `groupIds`, blank
attribute names, empty attribute value lists and blank attribute values.
`attributes: {}` is valid and means no attributes for create/update operations
or explicit attribute clearing for `PATCH`.

## Internationalization

The API localizes error titles, details and validation messages using the
`Accept-Language` request header.

Examples:

```http
Accept-Language: en
Accept-Language: pt-BR
Accept-Language: *
```

Behavior:

- Missing `Accept-Language` uses the default locale, English.
- `Accept-Language: *` is treated as no explicit language preference and uses
  English.
- Unsupported locales use English.
- The JVM default locale is ignored.
- General problem messages are read from `messages*.properties`.
- Bean Validation messages are read from `ValidationMessages*.properties`.

### Adding a New Language

To support a new locale, add both bundle files:

```text
src/main/resources/messages_<locale>.properties
src/main/resources/ValidationMessages_<locale>.properties
```

Example for Spanish:

```text
src/main/resources/messages_es.properties
src/main/resources/ValidationMessages_es.properties
```

A locale is considered supported only when both files exist. If only
`messages_es.properties` is added, requests with `Accept-Language: es` will still
use English because validation messages would be incomplete.

This rule is enforced by `MessageBundleCatalog`.

## Error Response Format

Errors are returned as problem objects:

```json
{
  "status": 400,
  "title": "Invalid request data",
  "detail": "One or more fields are invalid.",
  "timestamp": "2026-04-27T16:24:21.0728487-03:00",
  "messages": [
    {
      "name": "username",
      "message": "username is required"
    }
  ]
}
```

Localized example:

```http
Accept-Language: pt-BR
```

```json
{
  "status": 400,
  "title": "Dados da requisição inválidos",
  "detail": "Um ou mais campos estão inválidos.",
  "messages": [
    {
      "name": "username",
      "message": "username é obrigatório"
    }
  ]
}
```

Common Keycloak errors translated by the API include:

- duplicate username or email during user creation
- missing required User Profile attributes
- unavailable update-password email because Keycloak SMTP is not configured
- missing or invalid Keycloak `Location` header after resource creation

Unknown Keycloak errors are propagated through the centralized exception handling
flow instead of being hidden in gateway code.

## Development

Run tests:

```shell
./mvnw test
```

Run verification with integration tests enabled:

```shell
./mvnw -DskipITs=false verify
```

Run the suite while simulating a Portuguese JVM locale:

```shell
./mvnw -Duser.language=pt -Duser.country=BR -DskipITs=false verify
```

That command is useful to validate that `Accept-Language` controls the API
language and that the JVM locale does not leak into responses.

## Packaging

Build the application:

```shell
./mvnw package
```

Run the generated application:

```shell
java -jar target/quarkus-app/quarkus-run.jar
```

Build an uber jar:

```shell
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

Build a native executable:

```shell
./mvnw package -Dnative
```

Build native using a container:

```shell
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

## Contributing

This project aims to stay simple, explicit and easy to maintain.

When contributing:

- Keep Keycloak-specific logic inside `infrastructure/keycloak`.
- Prefer domain gateway interfaces over direct Keycloak Admin Client usage in
  services or resources.
- Keep request parsing and query validation in `resources/support`.
- Add tests for new API behavior and error mapping.
- Add new error messages to `messages.properties` and all supported locale files.
- Add new validation messages to `ValidationMessages.properties` and all
  supported locale files.
- Do not introduce silent recovery paths for failed operations. Let failures
  propagate through the centralized exception handling flow.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
