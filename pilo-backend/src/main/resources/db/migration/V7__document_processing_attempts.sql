-- Expand-only: cheap defaults, no table rewrite.
ALTER TABLE documents
    ADD COLUMN processing_attempts INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN last_error VARCHAR(512),
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();
