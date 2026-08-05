-- Run this while connected to the munchy_account database.
-- The database should be owned by the munchy_account_app login role.

CREATE SCHEMA IF NOT EXISTS account;
SET search_path TO account, public;

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(320) NOT NULL,
    name VARCHAR(150) NOT NULL,
    phone_number VARCHAR(30),
    picture_url TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMPTZ,
    CONSTRAINT users_status_check
        CHECK (status IN ('ACTIVE', 'BLOCKED', 'DELETED'))
);

CREATE UNIQUE INDEX users_email_unique_idx ON users (LOWER(email));

CREATE TABLE user_identities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    provider VARCHAR(30) NOT NULL,
    provider_subject VARCHAR(255) NOT NULL,
    provider_email VARCHAR(320),
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMPTZ,
    CONSTRAINT user_identities_user_fk
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT user_identities_provider_check
        CHECK (provider IN ('GOOGLE')),
    CONSTRAINT user_identities_provider_subject_unique
        UNIQUE (provider, provider_subject),
    CONSTRAINT user_identities_user_provider_unique
        UNIQUE (user_id, provider)
);

CREATE INDEX user_identities_user_id_idx ON user_identities (user_id);

CREATE TABLE roles (
    id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(40) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO roles (name, description)
VALUES
    ('CUSTOMER', 'Can browse restaurants and place orders'),
    ('RESTAURANT_OWNER', 'Can manage restaurants and menus'),
    ('DELIVERY_PARTNER', 'Can accept and deliver orders'),
    ('ADMIN', 'Can administer the Munchy platform')
ON CONFLICT (name) DO NOTHING;

CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id SMALLINT NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT user_roles_user_fk
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT user_roles_role_fk
        FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE RESTRICT
);

CREATE INDEX user_roles_role_id_idx ON user_roles (role_id);

-- One row represents one stable browser/device login session.
CREATE TABLE auth_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    identity_id UUID,
    provider VARCHAR(30) NOT NULL DEFAULT 'GOOGLE',
    ip_address INET,
    user_agent TEXT,
    device_name VARCHAR(150),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    revoke_reason VARCHAR(100),
    CONSTRAINT auth_sessions_user_fk
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT auth_sessions_identity_fk
        FOREIGN KEY (identity_id) REFERENCES user_identities (id) ON DELETE SET NULL,
    CONSTRAINT auth_sessions_provider_check
        CHECK (provider IN ('GOOGLE')),
    CONSTRAINT auth_sessions_expiry_check
        CHECK (expires_at > created_at)
);

CREATE INDEX auth_sessions_user_id_idx ON auth_sessions (user_id);
CREATE INDEX auth_sessions_active_user_idx
    ON auth_sessions (user_id, expires_at)
    WHERE revoked_at IS NULL;

-- A session remains stable while these refresh-token rows rotate.
-- Only a SHA-256 token hash is stored, never the raw refresh token.
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL,
    token_hash CHAR(64) NOT NULL UNIQUE,
    parent_token_id UUID,
    issued_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ,
    revoke_reason VARCHAR(100),
    CONSTRAINT refresh_tokens_session_fk
        FOREIGN KEY (session_id) REFERENCES auth_sessions (id) ON DELETE CASCADE,
    CONSTRAINT refresh_tokens_parent_fk
        FOREIGN KEY (parent_token_id) REFERENCES refresh_tokens (id) ON DELETE SET NULL,
    CONSTRAINT refresh_tokens_expiry_check
        CHECK (expires_at > issued_at)
);

CREATE INDEX refresh_tokens_session_id_idx ON refresh_tokens (session_id);
CREATE INDEX refresh_tokens_active_session_idx
    ON refresh_tokens (session_id, expires_at)
    WHERE used_at IS NULL AND revoked_at IS NULL;

-- Stores only the latest consented location for a stable login session.
CREATE TABLE session_locations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL UNIQUE,
    latitude NUMERIC(9, 6) NOT NULL,
    longitude NUMERIC(9, 6) NOT NULL,
    accuracy_meters NUMERIC(10, 2),
    source VARCHAR(30) NOT NULL,
    captured_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT session_locations_session_fk
        FOREIGN KEY (session_id) REFERENCES auth_sessions (id) ON DELETE CASCADE,
    CONSTRAINT session_locations_latitude_check
        CHECK (latitude BETWEEN -90 AND 90),
    CONSTRAINT session_locations_longitude_check
        CHECK (longitude BETWEEN -180 AND 180),
    CONSTRAINT session_locations_accuracy_check
        CHECK (accuracy_meters IS NULL OR accuracy_meters >= 0),
    CONSTRAINT session_locations_source_check
        CHECK (source IN ('BROWSER_GPS', 'IP_APPROXIMATION'))
);

CREATE TABLE user_addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    label VARCHAR(40) NOT NULL,
    recipient_name VARCHAR(150) NOT NULL,
    recipient_phone VARCHAR(30) NOT NULL,
    address_line_1 VARCHAR(255) NOT NULL,
    address_line_2 VARCHAR(255),
    landmark VARCHAR(255),
    locality VARCHAR(150) NOT NULL,
    city VARCHAR(120) NOT NULL,
    state VARCHAR(120) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country_code CHAR(2) NOT NULL DEFAULT 'IN',
    latitude NUMERIC(9, 6),
    longitude NUMERIC(9, 6),
    delivery_instructions VARCHAR(500),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT user_addresses_user_fk
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT user_addresses_country_code_check
        CHECK (country_code = UPPER(country_code)),
    CONSTRAINT user_addresses_coordinates_check
        CHECK (
            (latitude IS NULL AND longitude IS NULL)
            OR
            (latitude BETWEEN -90 AND 90 AND longitude BETWEEN -180 AND 180)
        ),
    CONSTRAINT user_addresses_active_state_check
        CHECK (NOT is_default OR (is_active AND deleted_at IS NULL))
);

CREATE INDEX user_addresses_active_user_idx
    ON user_addresses (user_id, updated_at DESC)
    WHERE is_active AND deleted_at IS NULL;

CREATE UNIQUE INDEX user_addresses_one_default_per_user_idx
    ON user_addresses (user_id)
    WHERE is_default AND is_active AND deleted_at IS NULL;

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;

CREATE TRIGGER users_set_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER session_locations_set_updated_at
BEFORE UPDATE ON session_locations
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER user_addresses_set_updated_at
BEFORE UPDATE ON user_addresses
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();
