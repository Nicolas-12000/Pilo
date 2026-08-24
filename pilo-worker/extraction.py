"""Structured extraction providers."""

from __future__ import annotations

import base64
import json
import os
import urllib.error
import urllib.request
from typing import Any


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
            "idNumber": "12345678X",
            "issueDate": "2026-01-01",
            "expirationDate": "2027-12-31",
            "address": "Calle Mayor 12, Madrid",
        },
    }


def _extract_gemini(
    content: bytes,
    mime_type: str,
    requirement_code: str,
    expected_document_type: str,
) -> dict[str, Any]:
    api_key = os.getenv("GEMINI_API_KEY", "")
    if not api_key:
        return _extract_mock(expected_document_type)

    model = os.getenv("GEMINI_MODEL", "gemini-2.0-flash")
    encoded = base64.b64encode(content).decode("ascii")
    prompt = (
        "Extract structured administrative document data. "
        "Return JSON with keys documentType, confidence, extractedFields. "
        "extractedFields may include personName, idNumber, issueDate, expirationDate, address. "
        f"Requirement code: {requirement_code}. Expected document type: {expected_document_type}."
    )
    payload = {
        "contents": [
            {
                "parts": [
                    {"text": prompt},
                    {"inline_data": {"mime_type": mime_type, "data": encoded}},
                ]
            }
        ],
        "generationConfig": {"responseMimeType": "application/json"},
    }
    url = (
        f"https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={api_key}"
    )
    request = urllib.request.Request(
        url,
        data=json.dumps(payload).encode("utf-8"),
        method="POST",
        headers={"Content-Type": "application/json"},
    )
    try:
        with urllib.request.urlopen(request, timeout=60) as response:
            body = json.loads(response.read().decode("utf-8"))
    except urllib.error.HTTPError as error:
        detail = error.read().decode("utf-8", errors="replace")
        raise RuntimeError(f"Gemini extraction failed ({error.code}): {detail}") from error

    json_text = body["candidates"][0]["content"]["parts"][0]["text"]
    parsed = json.loads(json_text)
    return {
        "documentType": parsed.get("documentType", "unknown"),
        "confidence": float(parsed.get("confidence", 0.0)),
        "extractedFields": parsed.get("extractedFields", {}),
    }
