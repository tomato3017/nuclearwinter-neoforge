#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PACK_DIR="$ROOT_DIR/resourcepack-template"
OUT_DIR="$ROOT_DIR/build/resourcepacks"
OUT_FILE="$OUT_DIR/nuclearwinter-resourcepack-template.zip"

if [ ! -d "$PACK_DIR" ]; then
  echo "Missing resource pack template at $PACK_DIR" >&2
  exit 1
fi

mkdir -p "$OUT_DIR"

python3 - "$PACK_DIR" "$OUT_FILE" <<'PY'
import sys
from pathlib import Path
from zipfile import ZIP_DEFLATED, ZipFile

pack_dir = Path(sys.argv[1])
out_file = Path(sys.argv[2])

with ZipFile(out_file, "w", compression=ZIP_DEFLATED) as zf:
    for path in sorted(pack_dir.rglob("*")):
        if path.is_file():
            zf.write(path, path.relative_to(pack_dir))

print(out_file)
PY
