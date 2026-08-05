SET search_path TO account, public;

DROP INDEX IF EXISTS refresh_tokens_active_session_idx;

CREATE UNIQUE INDEX refresh_tokens_one_active_per_session_idx
    ON refresh_tokens (session_id)
    WHERE used_at IS NULL AND revoked_at IS NULL;
