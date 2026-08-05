# Munchy

Food-delivery monorepo.

## Services

- `api-gateway` (`8080`): Google OAuth entry point, access-JWT validation, cookies, and routing.
- `account-service` (`8082`): PostgreSQL users, identities, roles, sessions, refresh rotation, addresses, and consented location.
- `order-service` (`8081`): order-domain service (currently a starter endpoint).

## Account database

The local database is `munchy_account`, owned by `munchy_account_app`. Setup and verification scripts are in `database/account`. Flyway migrations live in `account-service/src/main/resources/db/migration`.

Required secrets are supplied through environment variables and must never be committed:

```text
GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET
MUNCHY_JWT_SECRET
MUNCHY_ACCOUNT_DB_PASSWORD
```

See `docs/oauth2-authentication-flow.md` for the complete browser-to-Google-to-gateway-to-account-service flow.
