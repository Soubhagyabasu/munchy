# Munchy account database setup with pgAdmin

These instructions create the local account database without committing a
database password to Git.

## 1. Register the local PostgreSQL server

Open pgAdmin 4 and expand `Servers`. If the local server is not already shown,
select `Register > Server` and use:

- Name: `Munchy Local PostgreSQL`
- Host: `localhost`
- Port: `5432`
- Maintenance database: `postgres`
- Username: `postgres`
- Password: the password chosen during PostgreSQL installation

Saving the password in pgAdmin is optional. Never put it in this repository.

## 2. Create the application login role

Under the registered server, right-click `Login/Group Roles` and select
`Create > Login/Group Role`.

- General > Name: `munchy_account_app`
- Definition > Password: choose a new local development password
- Privileges > Can login?: `Yes`
- Privileges > Superuser?: `No`
- Privileges > Create databases?: `No`
- Privileges > Create roles?: `No`

Save the role. Do not use the `postgres` superuser from the application.

## 3. Create the database

Right-click `Databases` and select `Create > Database`.

- Database: `munchy_account`
- Owner: `munchy_account_app`

Save the database.

## 4. Create the tables

Right-click `munchy_account` and open `Query Tool`. Confirm that the database
shown in the Query Tool header is `munchy_account`, not `postgres`.

Open `V1__create_account_schema.sql`, then execute the entire file. It creates
the `account` schema, tables, indexes, constraints, triggers, and four initial
roles.

If the Query Tool connection uses `postgres`, add this as the first line for
this local setup before executing the file:

```sql
SET ROLE munchy_account_app;
```

Do not add that local-only line to the version-controlled migration file.

## 5. Verify the result

Refresh `munchy_account > Schemas`. The `account` schema should contain:

- `auth_sessions`
- `refresh_tokens`
- `roles`
- `session_locations`
- `user_addresses`
- `user_identities`
- `user_roles`
- `users`

Run `verify_account_schema.sql` in the same Query Tool. It lists the tables,
seeded roles, and foreign-key relationships.

## Important boundaries

- Login IP and browser metadata belong to `auth_sessions`.
- Current consented GPS data belongs to `session_locations`.
- Confirmed reusable delivery addresses belong to `user_addresses`.
- A future order service must copy the chosen delivery address into its own
  immutable order-address snapshot.
- Raw refresh tokens and Google tokens must never be stored in these tables.
