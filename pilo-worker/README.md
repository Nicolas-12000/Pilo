# PILO document processor (AWS Lambda)

Python worker triggered by S3 `ObjectCreated` events. Downloads the uploaded file, runs structured extraction, and callbacks the Spring Boot internal API.

## Environment variables

| Variable | Required | Description |
|----------|----------|-------------|
| `PILO_BACKEND_URL` | yes | Public backend base URL |
| `PILO_INTERNAL_API_KEY` | yes | Same as backend `PILO_INTERNAL_API_KEY` |
| `PILO_AI_PROVIDER` | no | `mock` (default) or `gemini` |
| `GEMINI_API_KEY` | if gemini | Google AI Studio key |
| `GEMINI_MODEL` | no | Default `gemini-2.0-flash` |

## Build deployment package

```bash
chmod +x scripts/build.sh
./scripts/build.sh
```

Upload `dist/pilo-document-processor.zip` to Lambda (handler: `handler.handler`).

## Expected S3 key format

```
cases/{caseId}/documents/{documentId}/{fileName}
```

See `docs/aws-operations.md` for AWS setup steps.
