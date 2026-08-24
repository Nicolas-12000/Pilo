#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DIST="$ROOT/dist"
BUILD="$DIST/build"

rm -rf "$DIST"
mkdir -p "$BUILD"
cp "$ROOT/handler.py" "$ROOT/backend_client.py" "$ROOT/extraction.py" "$BUILD/"

pip install -r "$ROOT/requirements.txt" -t "$BUILD" --quiet

(
  cd "$BUILD"
  zip -qr "$DIST/pilo-document-processor.zip" .
)

echo "Built $DIST/pilo-document-processor.zip"
