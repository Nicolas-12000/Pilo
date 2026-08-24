ALTER TABLE documents DROP CONSTRAINT ck_documents_status;

ALTER TABLE documents
ADD CONSTRAINT ck_documents_status CHECK (
    status IN (
        'PENDING_UPLOAD',
        'UPLOADED',
        'PROCESSING',
        'VALIDATED',
        'REJECTED',
        'PROCESSING_FAILED'
    )
);
