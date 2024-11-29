CREATE TABLE refresh_tokens (
    id         UUID PRIMARY KEY,
    usuario_id UUID        NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked    BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX ix_refresh_tokens_usuario ON refresh_tokens (usuario_id);
