#!/usr/bin/env bash
# Switch the document processor Lambda from mock extraction to Gemini (merges env vars; does not wipe others).
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
export AWS_PROFILE="${AWS_PROFILE:-NicolasAlejandroGarcia}"
export AWS_DEFAULT_REGION="${AWS_DEFAULT_REGION:-eu-west-1}"
export AWS_REGION="${AWS_REGION:-eu-west-1}"
export AWS_PAGER=""

LAMBDA_NAME="${LAMBDA_NAME:-pilo-document-processor-dev}"
MODEL="${GEMINI_MODEL:-gemini-3.6-flash}"

read_key_from() {
  local file="$1"
  [[ -f "$file" ]] || return 1
  local line
  line="$(grep -E '^GEMINI_API_KEY=' "$file" | tail -1 || true)"
  [[ -n "$line" ]] || return 1
  local val="${line#GEMINI_API_KEY=}"
  val="${val#\"}"
  val="${val%\"}"
  val="${val#\'}"
  val="${val%\'}"
  [[ -n "$val" ]] || return 1
  printf '%s' "$val"
}

GEMINI_API_KEY="${GEMINI_API_KEY:-}"
if [[ -z "$GEMINI_API_KEY" ]]; then
  GEMINI_API_KEY="$(read_key_from "$ROOT/.env" || true)"
fi
if [[ -z "$GEMINI_API_KEY" ]]; then
  GEMINI_API_KEY="$(read_key_from "$ROOT/pilo-backend/.env" || true)"
fi
if [[ -z "$GEMINI_API_KEY" ]]; then
  echo "Falta GEMINI_API_KEY: exportala o ponela en .env (raíz o pilo-backend/)." >&2
  exit 1
fi

python3 - "$LAMBDA_NAME" "$GEMINI_API_KEY" "$MODEL" <<'PY'
import json, os, subprocess, sys

name, api_key, model = sys.argv[1:4]
result = subprocess.run(
    ["aws", "lambda", "get-function-configuration", "--function-name", name, "--query", "Environment.Variables", "--output", "json"],
    capture_output=True,
    text=True,
    check=True,
)
vars = json.loads(result.stdout or "{}")
vars["PILO_AI_PROVIDER"] = "gemini"
vars["GEMINI_API_KEY"] = api_key
vars["GEMINI_MODEL"] = model

import tempfile

with tempfile.NamedTemporaryFile("w", suffix=".json", delete=False) as tmp:
    json.dump({"Variables": vars}, tmp)
    path = tmp.name
subprocess.run(
    [
        "aws",
        "lambda",
        "update-function-configuration",
        "--function-name",
        name,
        "--environment",
        f"file://{path}",
        "--query",
        "LastUpdateStatus",
        "--output",
        "text",
    ],
    check=True,
    stdout=subprocess.DEVNULL,
)
os.unlink(path)
print(f"Lambda {name}: PILO_AI_PROVIDER=gemini, GEMINI_MODEL={model} (key set, not shown).")
PY
