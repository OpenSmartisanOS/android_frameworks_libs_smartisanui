#!/usr/bin/env python3
"""Extract a hash-pinned Smartisan resource closure into the Core AAR."""

from __future__ import annotations

import argparse
import hashlib
import json
import shutil
import tempfile
import xml.etree.ElementTree as ET
from pathlib import Path

ANDROID_NS = "http://schemas.android.com/apk/res/android"


def digest(path: Path) -> str:
    checksum = hashlib.sha256()
    with path.open("rb") as source:
        for block in iter(lambda: source.read(1024 * 1024), b""):
            checksum.update(block)
    return checksum.hexdigest()


def safe_child(root: Path, relative: str) -> Path:
    candidate = (root / relative).resolve()
    if candidate != root.resolve() and root.resolve() not in candidate.parents:
        raise ValueError(f"path escapes root: {relative}")
    return candidate


def transformed_xml(source: Path, target: Path) -> None:
    ET.register_namespace("android", ANDROID_NS)
    tree = ET.parse(source)
    for element in tree.iter():
        for key, value in tuple(element.attrib.items()):
            if value.startswith("@drawable/"):
                element.set(key, value.replace("@drawable/", "@drawable/smartisan_rom_", 1))
    ET.indent(tree, space="    ")
    tree.write(target, encoding="utf-8", xml_declaration=True)


def materialize(source: Path, target: Path, transform: str | None) -> None:
    target.parent.mkdir(parents=True, exist_ok=True)
    if transform == "prefix_drawable_references":
        transformed_xml(source, target)
    elif transform is None:
        shutil.copyfile(source, target)
    else:
        raise ValueError(f"unknown transform: {transform}")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--decoded-res", required=True, type=Path)
    parser.add_argument("--manifest", required=True, type=Path)
    parser.add_argument("--output", required=True, type=Path)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()

    entries = json.loads(args.manifest.read_text(encoding="utf-8"))["resources"]
    with tempfile.TemporaryDirectory(prefix="smartisan-res-") as directory:
        temporary_root = Path(directory)
        for entry in entries:
            source = safe_child(args.decoded_res, entry["source"])
            if digest(source) != entry["sha256"]:
                raise RuntimeError(f"source hash mismatch: {entry['source']}")
            destination = safe_child(args.output, entry["destination"])
            generated = safe_child(temporary_root, entry["destination"])
            materialize(source, generated, entry.get("transform"))
            if args.check:
                if not destination.is_file() or digest(destination) != digest(generated):
                    raise RuntimeError(f"generated resource differs: {entry['destination']}")
            else:
                destination.parent.mkdir(parents=True, exist_ok=True)
                shutil.copyfile(generated, destination)
    action = "verified" if args.check else "extracted"
    print(f"{action} {len(entries)} Smartisan ROM resources")


if __name__ == "__main__":
    main()
