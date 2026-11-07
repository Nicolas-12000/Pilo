import os
import sys
import unittest

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from extraction import extract, normalize_model_output  # noqa: E402


class NormalizeModelOutputTest(unittest.TestCase):
    def test_keeps_fields_backed_by_evidence(self):
        result = normalize_model_output(
            {
                "documentType": "identity_document",
                "confidence": 0.9,
                "fields": {
                    "personName": {"value": "Ana García", "evidence": "NOMBRE ANA APELLIDOS GARCÍA"},
                    "idNumber": {"value": "12345678Z", "evidence": "DNI 12345678-Z"},
                },
            }
        )

        self.assertEqual(result["documentType"], "identity_document")
        self.assertEqual(result["extractedFields"], {"personName": "Ana García", "idNumber": "12345678Z"})
        self.assertEqual(result["confidence"], 0.9)

    def test_drops_unjustified_fields_and_lowers_confidence(self):
        result = normalize_model_output(
            {
                "documentType": "identity_document",
                "confidence": 0.9,
                "fields": {
                    "personName": {"value": "Ana García", "evidence": "ANA GARCÍA"},
                    "idNumber": {"value": "12345678Z", "evidence": "DNI 87654321X"},
                    "address": {"value": "Calle Mayor 1", "evidence": ""},
                },
            }
        )

        self.assertEqual(result["extractedFields"], {"personName": "Ana García"})
        self.assertAlmostEqual(result["confidence"], 0.3, places=3)

    def test_empty_document_has_zero_confidence(self):
        result = normalize_model_output({"documentType": "other", "confidence": 0.99, "fields": {}})

        self.assertEqual(result["confidence"], 0.0)
        self.assertEqual(result["extractedFields"], {})

    def test_unknown_document_type_becomes_other(self):
        result = normalize_model_output({"documentType": "passport_of_mars", "confidence": 1, "fields": None})

        self.assertEqual(result["documentType"], "other")

    def test_gemini_without_key_fails_instead_of_mocking(self):
        previous = {k: os.environ.get(k) for k in ("PILO_AI_PROVIDER", "GEMINI_API_KEY")}
        os.environ["PILO_AI_PROVIDER"] = "gemini"
        os.environ.pop("GEMINI_API_KEY", None)
        try:
            with self.assertRaises(RuntimeError):
                extract(b"%PDF", "application/pdf", "IDENTITY_DOCUMENT", "identity_document")
        finally:
            for key, value in previous.items():
                if value is None:
                    os.environ.pop(key, None)
                else:
                    os.environ[key] = value


if __name__ == "__main__":
    unittest.main()
