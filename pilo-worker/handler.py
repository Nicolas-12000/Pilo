"""AWS Lambda handler for S3 document uploads."""

from __future__ import annotations

import json
import logging
import os
import re
import urllib.parse
from typing import Any

import boto3

from backend_client import BackendClient
from extraction import extract

LOGGER = logging.getLogger(__name__)
LOGGER.setLevel(logging.INFO)

DOCUMENT_ID_PATTERN = re.compile(
    r"^cases/[0-9a-f-]{36}/documents/([0-9a-f-]{36})/.+$"
)


def handler(event: dict[str, Any], _context: Any) -> dict[str, Any]:
    backend = BackendClient(
        os.environ["PILO_BACKEND_URL"],
        os.environ["PILO_INTERNAL_API_KEY"],
    )
    s3 = boto3.client("s3")
    processed = 0

    for record in event.get("Records", []):
        bucket = record["s3"]["bucket"]["name"]
        key = urllib.parse.unquote_plus(record["s3"]["object"]["key"])
        document_id = _parse_document_id(key)
        LOGGER.info("Processing s3://%s/%s for document %s", bucket, key, document_id)

        backend.upload_completed(document_id)
        context = backend.processing_started(document_id)

        obj = s3.get_object(Bucket=bucket, Key=key)
        content = obj["Body"].read()
        mime_type = obj.get("ContentType") or context.get("mimeType") or "application/octet-stream"

        result = extract(
            content,
            mime_type,
            context.get("requirementCode", ""),
            context.get("expectedDocumentType", ""),
        )
        backend.submit_extraction(
            document_id,
            result["documentType"],
            float(result["confidence"]),
            result["extractedFields"],
        )
        processed += 1

    return {"processed": processed}


def _parse_document_id(storage_key: str) -> str:
    match = DOCUMENT_ID_PATTERN.match(storage_key)
    if not match:
        raise ValueError(f"INVALID_STORAGE_KEY: {storage_key}")
    return match.group(1)
