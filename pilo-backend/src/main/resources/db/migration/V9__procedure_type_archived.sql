-- Expand-only: archived procedures leave the public catalogue but keep their existing cases.
ALTER TABLE procedure_types
    ADD COLUMN archived_at TIMESTAMPTZ NULL;
