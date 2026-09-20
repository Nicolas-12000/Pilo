-- Expand-only: cheap defaults, no table rewrite.
ALTER TABLE users
    ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN token_version INTEGER NOT NULL DEFAULT 0;
