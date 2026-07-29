#!/usr/bin/env python3
"""Verify hash-pinned decompiled framework sources used by Core."""

from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--sources", required=True, type=Path)
    parser.add_argument("--manifest", required=True, type=Path)
    args = parser.parse_args()
    entries = json.loads(args.manifest.read_text(encoding="utf-8"))["classes"]
    verified = 0
    missing = []
    for entry in entries:
        path = args.sources / Path(*entry["source"].split(".")).with_suffix(".java")
        if not path.is_file():
            missing.append(entry["source"])
            continue
        actual = hashlib.sha256(path.read_bytes()).hexdigest()
        if actual != entry["sha256"]:
            raise RuntimeError(f"source hash mismatch: {entry['source']}")
        verified += 1
    print(f"verified {verified} Smartisan ROM source files; "
          f"{len(missing)} sources absent from this JADX tree")


if __name__ == "__main__":
    main()
