#!/usr/bin/env python3
"""Verify the complete Core resource closure against manifests and a pinned legacy base."""

from __future__ import annotations

import argparse
import hashlib
import json
import re
import subprocess
from pathlib import Path


SECTIONS = (
    "resources",
    "transformed_resources",
    "project_adaptation_resources",
    "tracked_aggregate_resources",
)
SHA256 = re.compile(r"^[0-9a-f]{64}$")


def checked_relative(value: object, label: str) -> str:
    if not isinstance(value, str) or not value:
        raise SystemExit(f"missing {label}")
    path = Path(value)
    if path.is_absolute() or ".." in path.parts:
        raise SystemExit(f"invalid {label}: {value}")
    return value


def checked_hash(value: object, label: str) -> str:
    if not isinstance(value, str) or SHA256.fullmatch(value) is None:
        raise SystemExit(f"invalid SHA-256 for {label}")
    return value


def sha256(path: Path) -> str:
    checksum = hashlib.sha256()
    with path.open("rb") as source:
        for block in iter(lambda: source.read(1024 * 1024), b""):
            checksum.update(block)
    return checksum.hexdigest()


def git(repository: Path, *arguments: str) -> str:
    return subprocess.check_output(["git", *arguments], cwd=repository, text=True)


def baseline_blobs(repository: Path, ref: str, resource_relative: Path) -> dict[str, str]:
    result: dict[str, str] = {}
    output = git(repository, "ls-tree", "-r", ref, "--", str(resource_relative))
    prefix = str(resource_relative) + "/"
    for line in output.splitlines():
        metadata, path = line.split("\t", 1)
        object_name = metadata.split()[2]
        if path.startswith(prefix):
            result[path[len(prefix):]] = object_name
    return result


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--manifest", action="append", required=True, type=Path)
    parser.add_argument("--resource-root", required=True, type=Path)
    parser.add_argument(
        "--baseline-ref",
        required=True,
        help="immutable commit whose byte-identical legacy resources are explicitly exempt",
    )
    args = parser.parse_args()

    represented: dict[str, str] = {}
    for manifest_path in args.manifest:
        manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
        manifest_destinations: set[str] = set()
        artifacts = manifest.get("artifacts", {})
        if not isinstance(artifacts, dict):
            raise SystemExit(f"artifacts must be an object: {manifest_path}")
        for artifact, expected in artifacts.items():
            checked_hash(expected, f"artifact {artifact}")
        recipes = manifest.get("transformation_recipes", {})
        if not isinstance(recipes, dict):
            raise SystemExit(f"transformation_recipes must be an object: {manifest_path}")
        for recipe_name, recipe in recipes.items():
            if not isinstance(recipe, dict) or not recipe.get("description"):
                raise SystemExit(f"invalid transformation recipe: {recipe_name}")
        for section in SECTIONS:
            for entry in manifest.get(section, []):
                destination = checked_relative(
                    entry.get("destination"), "provenance destination")
                expected = checked_hash(entry.get("sha256"), destination)
                if section in {"resources", "transformed_resources"}:
                    source_reference = entry.get("source")
                    if not isinstance(source_reference, str) or not source_reference:
                        raise SystemExit(f"missing provenance source for {destination}")
                    source = checked_relative(
                        source_reference.split("#", 1)[0], "provenance source")
                    artifact = entry.get("source_artifact", "default")
                    if artifact != "default" and artifact not in artifacts:
                        raise SystemExit(
                            f"undeclared source artifact for {destination}: {artifact}")
                    if section == "transformed_resources":
                        recipe = entry.get("match")
                        if recipe not in recipes:
                            raise SystemExit(
                                f"unknown transformation recipe for {destination}: {recipe}")
                        checked_hash(entry.get("source_sha256"), f"source {source}")
                        if "#" in source_reference:
                            if not entry.get("source_symbols") or not entry.get("target_symbols"):
                                raise SystemExit(
                                    f"fragment transform lacks named symbols: {destination}")
                            checked_hash(
                                entry.get("fragment_sha256"),
                                f"fragment contract {destination}")
                elif not entry.get("reason"):
                    raise SystemExit(f"project/aggregate resource lacks reason: {destination}")
                previous = represented.setdefault(destination, expected)
                manifest_destinations.add(destination)
                if previous != expected:
                    raise SystemExit(
                        f"conflicting provenance hashes for {destination}: "
                        f"{previous} != {expected}"
                    )
        complete_destinations = manifest.get("complete_fragment_destinations", [])
        if not isinstance(complete_destinations, list):
            raise SystemExit(f"complete_fragment_destinations must be a list: {manifest_path}")
        if len(complete_destinations) != len(set(complete_destinations)):
            raise SystemExit(f"duplicate complete fragment destination: {manifest_path}")
        for destination in complete_destinations:
            checked_relative(destination, "complete fragment destination")
            if destination not in manifest_destinations:
                raise SystemExit(
                    f"complete fragment destination lacks provenance entry: {destination}")

    resource_root = args.resource_root.resolve()
    repository = Path(git(resource_root, "rev-parse", "--show-toplevel").strip())
    resource_relative = resource_root.relative_to(repository)
    # Resolve now so a moving branch name cannot silently broaden the exemption.
    baseline_commit = git(repository, "rev-parse", f"{args.baseline_ref}^{{commit}}").strip()
    if not re.fullmatch(r"[0-9a-f]{40}", args.baseline_ref) \
            or baseline_commit != args.baseline_ref:
        raise SystemExit("--baseline-ref must be the exact immutable 40-character commit ID")
    baseline = baseline_blobs(repository, baseline_commit, resource_relative)

    current = {
        str(path.relative_to(resource_root))
        for path in resource_root.rglob("*")
        if path.is_file()
    }
    stale = sorted(set(represented) - current)
    if stale:
        raise SystemExit("provenance contains missing destinations:\n  " + "\n  ".join(stale))

    bad_hashes = []
    for destination, expected in represented.items():
        actual = sha256(resource_root / destination)
        if actual != expected:
            bad_hashes.append(f"{destination}: expected {expected}, got {actual}")
    if bad_hashes:
        raise SystemExit("provenance destination hash mismatch:\n  " + "\n  ".join(bad_hashes))

    unchanged_legacy = set()
    for relative in current - set(represented):
        expected_blob = baseline.get(relative)
        if expected_blob is None:
            continue
        actual_blob = git(repository, "hash-object", str(resource_root / relative)).strip()
        if actual_blob == expected_blob:
            unchanged_legacy.add(relative)

    missing = sorted(current - set(represented) - unchanged_legacy)
    if missing:
        raise SystemExit(
            "Core resources are neither provenance-tracked nor byte-identical to the pinned "
            f"legacy base {baseline_commit}:\n  " + "\n  ".join(missing)
        )
    print(
        f"Resource provenance closes all {len(current)} Core resource files: "
        f"{len(represented)} manifest-tracked, {len(unchanged_legacy)} unchanged legacy "
        f"from {baseline_commit}."
    )


if __name__ == "__main__":
    main()
