"""HTTP client for PILO internal API."""

from __future__ import annotations

import json
import urllib.error
import urllib.request
from typing import Any


class BackendClient:
    def __init__(self, base_url: str, api_key: str) -> None:
        self._base_url = base_url.rstrip("/")
        self._api_key = api_key

    def upload_completed(self, document_id: str) -> None:
        self._post(f"/internal/documents/{document_id}/upload-completed")

    def processing_started(self, document_id: str) -> dict[str, Any]:
        return self._post(f"/internal/documents/{document_id}/processing-started")

    def submit_extraction(
        self,
        document_id: str,
        document_type: str,
        confidence: float,
        extracted_fields: dict[str, Any],
    ) -> None:
        payload = {
            "documentType": document_type,
            "confidence": confidence,
            "extractedFields": extracted_fields,
        }
        self._post(
            f"/internal/documents/{document_id}/extractions",
            payload=payload,
        )

    def _post(self, path: str, payload: dict[str, Any] | None = None) -> dict[str, Any]:
        body = None if payload is None else json.dumps(payload).encode("utf-8")
        request = urllib.request.Request(
            f"{self._base_url}{path}",
            data=body,
            method="POST",
            headers={
                "X-Internal-Api-Key": self._api_key,
                "Content-Type": "application/json",
                "Accept": "application/json",
            },
        )
        try:
            with urllib.request.urlopen(request, timeout=30) as response:
                raw = response.read().decode("utf-8")
                if not raw:
                    return {}
                return json.loads(raw)
        except urllib.error.HTTPError as error:
            detail = error.read().decode("utf-8", errors="replace")
            raise RuntimeError(f"Backend call failed ({error.code}): {detail}") from error
