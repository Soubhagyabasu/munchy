SET search_path TO account, public;

ALTER TABLE refresh_tokens
    ADD COLUMN jwt_id UUID;

UPDATE refresh_tokens
SET jwt_id = id
WHERE jwt_id IS NULL;

ALTER TABLE refresh_tokens
    ALTER COLUMN jwt_id SET NOT NULL;

ALTER TABLE refresh_tokens
    ADD CONSTRAINT refresh_tokens_jwt_id_unique UNIQUE (jwt_id);
