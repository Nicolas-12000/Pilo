CREATE TABLE procedure_types (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    target_days INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_procedure_types_target_days CHECK (target_days > 0)
);

CREATE TABLE requirements (
    id UUID PRIMARY KEY,
    procedure_type_id UUID NOT NULL REFERENCES procedure_types (id) ON DELETE CASCADE,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    is_mandatory BOOLEAN NOT NULL,
    validation_rules JSONB NOT NULL DEFAULT '{}'::jsonb,
    CONSTRAINT uk_requirements_procedure_type_code UNIQUE (procedure_type_id, code)
);

CREATE INDEX idx_requirements_procedure_type_id ON requirements (procedure_type_id);
