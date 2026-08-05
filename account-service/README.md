# Account Service

Owns Munchy users, Google identities, roles, authentication sessions, rotating
refresh-token hashes, saved delivery addresses, and consented current location.

## Local configuration

Required:

```text
MUNCHY_JWT_SECRET
MUNCHY_ACCOUNT_DB_PASSWORD
```

Defaults:

```text
MUNCHY_ACCOUNT_DB_HOST=localhost
MUNCHY_ACCOUNT_DB_PORT=5432
MUNCHY_ACCOUNT_DB_NAME=munchy_account
MUNCHY_ACCOUNT_DB_USERNAME=munchy_account_app
MUNCHY_ACCOUNT_PORT=8082
MUNCHY_ACCOUNT_BIND_ADDRESS=127.0.0.1
```

## Layers

HTTP controllers accept validated DTOs, service interfaces define use cases,
service implementations enforce business rules and transaction boundaries,
repositories own R2DBC persistence, and dedicated mapper components wrap the
shared strict `ModelMapper` configuration. Entities are never returned directly.

## Address endpoints

All browser requests go through the API Gateway:

```text
GET    /api/v1/users/me/addresses
POST   /api/v1/users/me/addresses
PUT    /api/v1/users/me/addresses/{addressId}
PUT    /api/v1/users/me/addresses/{addressId}/default
DELETE /api/v1/users/me/addresses/{addressId}
GET    /api/v1/users/me/current-location
PUT    /api/v1/users/me/current-location
```

The gateway derives user and session ownership from the validated access JWT;
clients cannot select a different `userId` in request bodies.
