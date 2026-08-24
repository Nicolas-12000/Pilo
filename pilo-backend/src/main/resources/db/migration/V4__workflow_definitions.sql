ALTER TABLE workflow_tasks
    ADD COLUMN task_code VARCHAR(64),
    ADD COLUMN sort_order INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN completion_rule VARCHAR(64);

UPDATE workflow_tasks
SET task_code = CASE task_name
        WHEN 'Apertura del expediente' THEN 'OPEN_CASE'
        WHEN 'Recopilar documentación' THEN 'COLLECT_DOCUMENTS'
        WHEN 'Validar documentación' THEN 'VALIDATE_DOCUMENTS'
        WHEN 'Revisión final' THEN 'FINAL_REVIEW'
        ELSE 'LEGACY_' || replace(id::text, '-', '')
    END,
    sort_order = CASE task_name
        WHEN 'Apertura del expediente' THEN 10
        WHEN 'Recopilar documentación' THEN 20
        WHEN 'Validar documentación' THEN 30
        WHEN 'Revisión final' THEN 40
        ELSE 100
    END,
    completion_rule = CASE task_name
        WHEN 'Apertura del expediente' THEN 'ON_CREATE'
        WHEN 'Recopilar documentación' THEN 'ALL_MANDATORY_PRESENT'
        WHEN 'Validar documentación' THEN 'ALL_MANDATORY_VALIDATED'
        WHEN 'Revisión final' THEN 'MANUAL'
        ELSE 'MANUAL'
    END;

ALTER TABLE workflow_tasks
    ALTER COLUMN task_code SET NOT NULL,
    ALTER COLUMN completion_rule SET NOT NULL;

ALTER TABLE workflow_tasks
    ADD CONSTRAINT uk_workflow_tasks_case_code UNIQUE (case_id, task_code),
    ADD CONSTRAINT ck_workflow_tasks_completion_rule CHECK (
        completion_rule IN (
            'ON_CREATE',
            'ALL_MANDATORY_PRESENT',
            'ALL_MANDATORY_VALIDATED',
            'MANUAL'
        )
    );

CREATE TABLE workflow_step_definitions (
    id UUID PRIMARY KEY,
    procedure_type_id UUID NOT NULL REFERENCES procedure_types (id) ON DELETE CASCADE,
    task_code VARCHAR(64) NOT NULL,
    display_name VARCHAR(128) NOT NULL,
    assigned_role VARCHAR(32) NOT NULL,
    sort_order INTEGER NOT NULL,
    depends_on_task_code VARCHAR(64),
    completion_rule VARCHAR(64) NOT NULL,
    CONSTRAINT uk_workflow_step_definitions_type_code UNIQUE (procedure_type_id, task_code),
    CONSTRAINT uk_workflow_step_definitions_type_order UNIQUE (procedure_type_id, sort_order),
    CONSTRAINT ck_workflow_step_definitions_role CHECK (assigned_role IN ('USER', 'REVIEWER', 'ADMIN')),
    CONSTRAINT ck_workflow_step_definitions_rule CHECK (
        completion_rule IN (
            'ON_CREATE',
            'ALL_MANDATORY_PRESENT',
            'ALL_MANDATORY_VALIDATED',
            'MANUAL'
        )
    )
);

CREATE INDEX idx_workflow_step_definitions_procedure_type_id
    ON workflow_step_definitions (procedure_type_id);
