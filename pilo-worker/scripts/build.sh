#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DIST="$ROOT/dist"
BUILD="$DIST/build"

rm -rf "$DIST"
mkdir -p "$BUILD"
cp "$ROOT/handler.py" "$ROOT/backend_client.py" "$ROOT/extraction.py" "$BUILD/"

pip install -r "$ROOT/requirements.txt" -t "$BUILD" --quiet

if command -v zip >/dev/null 2>&1; then
  ( cd "$BUILD" && zip -qr "$DIST/pilo-document-processor.zip" . )
else
  python3 - "$BUILD" "$DIST/pilo-document-processor.zip" <<'PY'
import os, sys, zipfile
srcdir, dest = sys.argv[1], sys.argv[2]
with zipfile.ZipFile(dest, "w", compression=zipfile.ZIP_DEFLATED) as zf:
    for root, _, files in os.walk(srcdir):
        for name in files:
            path = os.path.join(root, name)
            arc = os.path.relpath(path, srcdir)
            zf.write(path, arc)
PY
fi

echo "Built $DIST/pilo-document-processor.zip"
