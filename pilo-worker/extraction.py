"""Structured extraction providers.

The model only proposes values. Every field must come with the literal text it was read
from; fields without evidence are dropped, and the reported confidence is lowered by the
share of fields that could not be justified. The backend rules make the final decision.
"""

from __future__ import annotations

import base64
import json
import logging
import os
import random
import re
import time
import urllib.error
import urllib.request
from typing import Any

LOGGER = logging.getLogger(__name__)

DOCUMENT_TYPES = [
    "identity_document",
    "proof_of_address",
    "rental_contract",
    "home_insurance",
    "tax_certificate",
    "building_plan",
    "other",
]
FIELD_NAMES = ["personName", "idNumber", "issueDate", "expirationDate", "address"]

_FIELD_SCHEMA = {
    "type": ["object", "null"],
    "properties": {
        "value": {"type": "string"},
        "evidence": {
            "type": "string",
            "description": "Exact text copied from the document where the value appears.",
        },
    },
    "required": ["value", "evidence"],
}

RESPONSE_SCHEMA = {
    "type": "object",
    "properties": {
        "documentType": {"type": "string", "enum": DOCUMENT_TYPES},
        "confidence": {
            "type": "number",
            "minimum": 0,
            "maximum": 1,
            "description": "How legible and unambiguous the document is.",
        },
        "fields": {
            "type": "object",
            "properties": {name: _FIELD_SCHEMA for name in FIELD_NAMES},
        },
    },
    "required": ["documentType", "confidence", "fields"],
}

RETRYABLE_STATUS = {408, 429, 500, 502, 503, 504}
MAX_ATTEMPTS = 3
REQUEST_TIMEOUT_SECONDS = 35


def extract(content: bytes, mime_type: str, requirement_code: str, expected_document_type: str) -> dict[str, Any]:
    provider = os.getenv("PILO_AI_PROVIDER", "mock").lower()
    if provider == "gemini":
        return _extract_gemini(content, mime_type, requirement_code, expected_document_type)
    return _extract_mock(expected_document_type)


def _extract_mock(expected_document_type: str) -> dict[str, Any]:
    document_type = expected_document_type or "unknown_document"
    return {
        "documentType": document_type,
        "confidence": 0.95,
        "extractedFields": {
            "personName": "Ana Usuario",
            "idNumber": "12345678Z",
            "issueDate": "2026-01-01",
            "expirationDate": "2027-12-31",
            "address": "Calle Mayor 12, Madrid",
        },
    }


def build_prompt(requirement_code: str, expected_document_type: str) -> str:
    return (
        "You are reading a Spanish administrative document uploaded by a citizen.\n"
        "Classify it with one of the allowed documentType values; use \"other\" if none fits.\n"
        "For each field, return it only if it is clearly printed in the document, with "
        "\"evidence\" set to the exact text you read it from. If a field is not visible, set it to null. "
        "Never infer or complete values.\n"
        "Dates must be returned as YYYY-MM-DD. idNumber is a Spanish DNI/NIE without spaces.\n"
        f"Requirement code: {requirement_code}. Expected document type: {expected_document_type}."
    )


def _extract_gemini(
    content: bytes,
    mime_type: str,
    requirement_code: str,
    expected_document_type: str,
) -> dict[str, Any]:
    api_key = os.getenv("GEMINI_API_KEY", "")
    if not api_key:
        # Silently falling back to the mock would mark any upload as valid.
        raise RuntimeError("PILO_AI_PROVIDER=gemini but GEMINI_API_KEY is not set")

    model = os.getenv("GEMINI_MODEL", "gemini-3.6-flash")
    payload = {
        "contents": [
            {
                "parts": [
                    # Google recommends the file before the instruction for single documents.
                    {"inline_data": {"mime_type": mime_type, "data": base64.b64encode(content).decode("ascii")}},
                    {"text": build_prompt(requirement_code, expected_document_type)},
                ]
            }
        ],
        "generationConfig": {
            "responseMimeType": "application/json",
            "responseJsonSchema": RESPONSE_SCHEMA,
            "thinkingConfig": {"thinkingLevel": "low"},
        },
    }
    url = f"https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent"
    body = _post_with_retries(url, api_key, payload)
    json_text = body["candidates"][0]["content"]["parts"][0]["text"]
    return normalize_model_output(json.loads(json_text))


def _post_with_retries(url: str, api_key: str, payload: dict[str, Any]) -> dict[str, Any]:
    data = json.dumps(payload).encode("utf-8")
    for attempt in range(1, MAX_ATTEMPTS + 1):
        request = urllib.request.Request(
            url,
            data=data,
            method="POST",
            headers={"Content-Type": "application/json", "x-goog-api-key": api_key},
        )
        try:
            with urllib.request.urlopen(request, timeout=REQUEST_TIMEOUT_SECONDS) as response:
                return json.loads(response.read().decode("utf-8"))
        except urllib.error.HTTPError as error:
            detail = error.read().decode("utf-8", errors="replace")[:500]
            if error.code not in RETRYABLE_STATUS or attempt == MAX_ATTEMPTS:
                raise RuntimeError(f"Gemini extraction failed ({error.code}): {detail}") from error
            LOGGER.warning("Gemini returned %s, retrying (attempt %s)", error.code, attempt)
        except (urllib.error.URLError, TimeoutError) as error:
            if attempt == MAX_ATTEMPTS:
                raise RuntimeError(f"Gemini extraction failed: {error}") from error
            LOGGER.warning("Gemini request failed (%s), retrying (attempt %s)", error, attempt)
        time.sleep(2 ** (attempt - 1) + random.uniform(0, 0.5))
    raise RuntimeError("unreachable")


def _compact(text: str) -> str:
    return re.sub(r"[^0-9a-z]", "", text.lower())


def _has_evidence(name: str, value: str, evidence: str) -> bool:
    if not value.strip() or not evidence.strip():
        return False
    # Identifiers must literally appear in the quoted text; dates and names may be reformatted.
    if name == "idNumber":
        return _compact(value) in _compact(evidence)
    return True


def normalize_model_output(parsed: dict[str, Any]) -> dict[str, Any]:
    document_type = parsed.get("documentType") or "other"
    if document_type not in DOCUMENT_TYPES:
        document_type = "other"

    raw_fields = parsed.get("fields") or {}
    proposed = 0
    kept: dict[str, str] = {}
    for name in FIELD_NAMES:
        field = raw_fields.get(name)
        if not isinstance(field, dict):
            continue
        value = str(field.get("value") or "")
        if not value.strip():
            continue
        proposed += 1
        if _has_evidence(name, value, str(field.get("evidence") or "")):
            kept[name] = value.strip()

    try:
        model_confidence = min(max(float(parsed.get("confidence", 0.0)), 0.0), 1.0)
    except (TypeError, ValueError):
        model_confidence = 0.0
    evidence_ratio = len(kept) / proposed if proposed else 0.0
    return {
        "documentType": document_type,
        "confidence": round(model_confidence * evidence_ratio, 3),
        "extractedFields": kept,
    }
