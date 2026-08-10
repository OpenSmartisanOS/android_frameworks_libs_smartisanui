#!/usr/bin/env python3
"""Reproduce and verify the hash-pinned Smartisan OS R2 Java source ledger."""

from __future__ import annotations

import argparse
import hashlib
import json
import re
import subprocess
import tempfile
from pathlib import Path
from typing import Any


HEX_SHA256 = re.compile(r"[0-9a-f]{64}")
JAVA_NAME = re.compile(r"[A-Za-z_$][A-Za-z0-9_$]*(?:\.[A-Za-z_$][A-Za-z0-9_$]*)+")
TOP_LEVEL_KEYS = {
    "schema_version", "rom", "decompiler", "artifacts", "source_roots", "coverage",
    "classes",
}
COVERAGE_GROUPS = {
    "legacy", "alert", "menu_progress", "popup_menu", "picker", "dialog_pattern",
}
MAPPING_KINDS = {"public", "internal", "public-sdk-adaptation"}


class ProvenanceError(RuntimeError):
    """A malformed ledger or non-reproducible input."""


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def require_object(value: Any, label: str) -> dict[str, Any]:
    if not isinstance(value, dict):
        raise ProvenanceError(f"{label} must be an object")
    return value


def require_list(value: Any, label: str) -> list[Any]:
    if not isinstance(value, list):
        raise ProvenanceError(f"{label} must be an array")
    return value


def exact_keys(value: dict[str, Any], expected: set[str], label: str) -> None:
    missing = expected - value.keys()
    unknown = value.keys() - expected
    if missing:
        raise ProvenanceError(f"{label} is missing keys: {', '.join(sorted(missing))}")
    if unknown:
        raise ProvenanceError(f"{label} has unknown keys: {', '.join(sorted(unknown))}")


def validate_sha(value: Any, label: str) -> str:
    if not isinstance(value, str) or HEX_SHA256.fullmatch(value) is None:
        raise ProvenanceError(f"{label} is not a lowercase SHA-256")
    return value


def validate_name(value: Any, label: str) -> str:
    if not isinstance(value, str) or JAVA_NAME.fullmatch(value) is None:
        raise ProvenanceError(f"{label} is not a Java binary/source name: {value!r}")
    return value


def validate_manifest(document: dict[str, Any]) -> tuple[
        dict[str, dict[str, Any]], dict[str, dict[str, Any]], list[dict[str, Any]]]:
    exact_keys(document, TOP_LEVEL_KEYS, "manifest")
    if document["schema_version"] != 2:
        raise ProvenanceError("unsupported source provenance schema")
    if not isinstance(document["rom"], str) or not document["rom"]:
        raise ProvenanceError("manifest rom must be a non-empty string")

    decompiler = require_object(document["decompiler"], "decompiler")
    exact_keys(decompiler, {"name", "version", "engine_file", "engine_sha256"}, "decompiler")
    if decompiler["name"] != "jadx" or not isinstance(decompiler["version"], str):
        raise ProvenanceError("only a pinned JADX decompiler is supported")
    if not isinstance(decompiler["engine_file"], str) or "/" in decompiler["engine_file"]:
        raise ProvenanceError("decompiler engine_file must be a file name")
    validate_sha(decompiler["engine_sha256"], "decompiler.engine_sha256")

    artifacts: dict[str, dict[str, Any]] = {}
    for index, raw in enumerate(require_list(document["artifacts"], "artifacts")):
        artifact = require_object(raw, f"artifacts[{index}]")
        exact_keys(artifact, {"id", "file", "sha256"}, f"artifacts[{index}]")
        artifact_id = artifact["id"]
        if not isinstance(artifact_id, str) or not artifact_id:
            raise ProvenanceError(f"artifacts[{index}].id must be non-empty")
        if artifact_id in artifacts:
            raise ProvenanceError(f"duplicate artifact id: {artifact_id}")
        if not isinstance(artifact["file"], str) or "/" in artifact["file"]:
            raise ProvenanceError(f"artifacts[{index}].file must be a file name")
        validate_sha(artifact["sha256"], f"artifacts[{index}].sha256")
        artifacts[artifact_id] = artifact

    roots: dict[str, dict[str, Any]] = {}
    for index, raw in enumerate(require_list(document["source_roots"], "source_roots")):
        root = require_object(raw, f"source_roots[{index}]")
        exact_keys(root, {"id", "artifact", "relative_source_dir", "generation"},
                   f"source_roots[{index}]")
        root_id = root["id"]
        if not isinstance(root_id, str) or not root_id:
            raise ProvenanceError(f"source_roots[{index}].id must be non-empty")
        if root_id in roots:
            raise ProvenanceError(f"duplicate source root id: {root_id}")
        if root["artifact"] not in artifacts:
            raise ProvenanceError(f"unknown artifact for source root {root_id}: {root['artifact']}")
        source_dir = Path(root["relative_source_dir"])
        if source_dir.is_absolute() or ".." in source_dir.parts:
            raise ProvenanceError(f"unsafe relative_source_dir for {root_id}")
        generation = require_object(root["generation"], f"source root {root_id} generation")
        strategy = generation.get("strategy")
        if strategy == "archive":
            exact_keys(generation, {"strategy", "arguments"},
                       f"source root {root_id} generation")
        elif strategy == "single-class":
            exact_keys(generation, {"strategy", "arguments", "per_source_arguments"},
                       f"source root {root_id} generation")
            source_arguments = require_object(
                generation["per_source_arguments"],
                f"source root {root_id} per_source_arguments")
            for source, arguments in source_arguments.items():
                validate_name(source, f"source root {root_id} per-source key")
                if not isinstance(arguments, list) or not all(
                        isinstance(argument, str) for argument in arguments):
                    raise ProvenanceError(
                        f"source root {root_id} arguments for {source} must be strings")
        else:
            raise ProvenanceError(f"unknown generation strategy for {root_id}: {strategy!r}")
        if not isinstance(generation["arguments"], list) or not all(
                isinstance(argument, str) for argument in generation["arguments"]):
            raise ProvenanceError(f"source root {root_id} arguments must be strings")
        roots[root_id] = root

    coverage = require_object(document["coverage"], "coverage")
    exact_keys(coverage, COVERAGE_GROUPS, "coverage")
    covered_sources: list[str] = []
    for group in sorted(COVERAGE_GROUPS):
        values = require_list(coverage[group], f"coverage.{group}")
        if not values:
            raise ProvenanceError(f"coverage.{group} must not be empty")
        covered_sources.extend(validate_name(value, f"coverage.{group}") for value in values)
    duplicates = sorted({source for source in covered_sources if covered_sources.count(source) > 1})
    if duplicates:
        raise ProvenanceError(f"sources appear in multiple coverage groups: {', '.join(duplicates)}")

    classes: list[dict[str, Any]] = []
    class_keys: set[tuple[str, str]] = set()
    source_names: list[str] = []
    for index, raw in enumerate(require_list(document["classes"], "classes")):
        entry = require_object(raw, f"classes[{index}]")
        exact_keys(entry, {"source_root", "source", "sha256", "mappings"},
                   f"classes[{index}]")
        root_id = entry["source_root"]
        if root_id not in roots:
            raise ProvenanceError(f"classes[{index}] has unknown source root: {root_id!r}")
        source = validate_name(entry["source"], f"classes[{index}].source")
        key = (root_id, source)
        if key in class_keys:
            raise ProvenanceError(f"duplicate class entry: {root_id}:{source}")
        class_keys.add(key)
        source_names.append(source)
        validate_sha(entry["sha256"], f"classes[{index}].sha256")
        mappings = require_list(entry["mappings"], f"classes[{index}].mappings")
        if not mappings:
            raise ProvenanceError(f"classes[{index}].mappings must not be empty")
        for mapping_index, raw_mapping in enumerate(mappings):
            mapping = require_object(raw_mapping,
                                     f"classes[{index}].mappings[{mapping_index}]")
            exact_keys(mapping, {"kind", "target"},
                       f"classes[{index}].mappings[{mapping_index}]")
            if mapping["kind"] not in MAPPING_KINDS:
                raise ProvenanceError(
                    f"unknown mapping kind for {source}: {mapping['kind']!r}")
            target = validate_name(mapping["target"],
                                   f"classes[{index}].mappings[{mapping_index}].target")
        classes.append(entry)

    if set(source_names) != set(covered_sources) or len(source_names) != len(covered_sources):
        missing = sorted(set(covered_sources) - set(source_names))
        unknown = sorted(set(source_names) - set(covered_sources))
        details = []
        if missing:
            details.append(f"missing entries: {', '.join(missing)}")
        if unknown:
            details.append(f"unknown entries: {', '.join(unknown)}")
        raise ProvenanceError("class ledger does not match coverage: " + "; ".join(details))

    for root_id, root in roots.items():
        root_sources = {entry["source"] for entry in classes if entry["source_root"] == root_id}
        if not root_sources:
            raise ProvenanceError(f"unused source root: {root_id}")
        generation = root["generation"]
        if generation["strategy"] == "single-class":
            configured = set(generation["per_source_arguments"])
            if configured != root_sources:
                missing = sorted(root_sources - configured)
                unknown = sorted(configured - root_sources)
                raise ProvenanceError(
                    f"single-class generation set mismatch for {root_id}; "
                    f"missing={missing}, unknown={unknown}")
    used_artifacts = {root["artifact"] for root in roots.values()}
    if used_artifacts != set(artifacts):
        raise ProvenanceError(
            f"unused artifact entries: {', '.join(sorted(set(artifacts) - used_artifacts))}")
    return artifacts, roots, classes


def resolve_jadx_engine(jadx: Path, engine_file: str) -> Path:
    candidates = [
        jadx.resolve().parent.parent / "lib" / engine_file,
        jadx.resolve().parent / "lib" / engine_file,
    ]
    for candidate in candidates:
        if candidate.is_file():
            return candidate
    raise ProvenanceError(f"cannot locate {engine_file} next to JADX executable {jadx}")


def run(command: list[str]) -> None:
    completed = subprocess.run(command, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                               text=True, check=False)
    if completed.returncode != 0:
        raise ProvenanceError(
            f"source generation failed ({completed.returncode}): {' '.join(command)}\n"
            f"{completed.stdout[-4000:]}")


def generate_sources(canonical_root: Path, jadx: Path, artifacts: dict[str, Path],
                     roots: dict[str, dict[str, Any]], classes: list[dict[str, Any]]) -> None:
    canonical_root.mkdir(parents=True, exist_ok=True)
    for root_id, root in roots.items():
        output_root = canonical_root / root_id
        generation = root["generation"]
        artifact = artifacts[root["artifact"]]
        if generation["strategy"] == "archive":
            run([str(jadx), *generation["arguments"], "--output-dir", str(output_root),
                 str(artifact)])
            continue
        source_root = canonical_root / root["relative_source_dir"]
        for entry in classes:
            if entry["source_root"] != root_id:
                continue
            source = entry["source"]
            output = source_root / Path(*source.split(".")).with_suffix(".java")
            output.parent.mkdir(parents=True, exist_ok=True)
            run([str(jadx), *generation["arguments"],
                 *generation["per_source_arguments"][source],
                 "--single-class", source, "--single-class-output", str(output),
                 str(artifact)])


def verify_sources(canonical_root: Path, roots: dict[str, dict[str, Any]],
                   classes: list[dict[str, Any]]) -> None:
    verified = 0
    for entry in classes:
        source_root = canonical_root / roots[entry["source_root"]]["relative_source_dir"]
        source_path = source_root / Path(*entry["source"].split(".")).with_suffix(".java")
        if not source_path.is_file():
            raise ProvenanceError(f"required decompiled source is missing: {entry['source']}")
        actual = sha256(source_path)
        if actual != entry["sha256"]:
            raise ProvenanceError(
                f"source hash mismatch: {entry['source']} ({actual} != {entry['sha256']})")
        verified += 1
    print(f"Verified {verified} required Smartisan OS R2 decompiled source files.")


def verify_mapped_targets(repository: Path, classes: list[dict[str, Any]]) -> None:
    java_root = repository / "core/src/main/java"
    verified: set[str] = set()
    for entry in classes:
        for mapping in entry["mappings"]:
            target = mapping["target"]
            parts = target.split(".")
            # A target may name an inner class, so accept the longest enclosing Java file.
            for length in range(len(parts), 0, -1):
                candidate = java_root.joinpath(*parts[:length]).with_suffix(".java")
                if candidate.is_file():
                    verified.add(target)
                    break
            else:
                raise ProvenanceError(
                    f"mapped SDK source target does not exist in Core: {target}")
    print(f"Verified {len(verified)} mapped Core source targets.")


def verify(args: argparse.Namespace, canonical_root: Path) -> None:
    document = require_object(json.loads(args.manifest.read_text(encoding="utf-8")), "manifest")
    artifacts, roots, classes = validate_manifest(document)
    repository = Path(__file__).resolve().parents[2]
    verify_mapped_targets(repository, classes)

    jadx = args.jadx.resolve()
    if not jadx.is_file():
        raise ProvenanceError(f"JADX executable not found: {jadx}")
    version = subprocess.run([str(jadx), "--version"], capture_output=True, text=True,
                             check=True).stdout.strip()
    if version != document["decompiler"]["version"]:
        raise ProvenanceError(
            f"JADX version mismatch: {version!r} != {document['decompiler']['version']!r}")
    engine = resolve_jadx_engine(jadx, document["decompiler"]["engine_file"])
    actual_engine_hash = sha256(engine)
    if actual_engine_hash != document["decompiler"]["engine_sha256"]:
        raise ProvenanceError(
            f"JADX engine hash mismatch: {actual_engine_hash} != "
            f"{document['decompiler']['engine_sha256']}")

    supplied_artifacts = {
        "smartisanos": args.smartisanos_jar.resolve(),
        "framework": args.framework_jar.resolve(),
    }
    if set(supplied_artifacts) != set(artifacts):
        raise ProvenanceError("command-line artifact set does not match the ledger")
    for artifact_id, path in supplied_artifacts.items():
        if not path.is_file():
            raise ProvenanceError(f"required artifact is missing: {path}")
        if path.name != artifacts[artifact_id]["file"]:
            raise ProvenanceError(
                f"artifact file mismatch for {artifact_id}: {path.name} != "
                f"{artifacts[artifact_id]['file']}")
        actual = sha256(path)
        if actual != artifacts[artifact_id]["sha256"]:
            raise ProvenanceError(
                f"artifact hash mismatch for {artifact_id}: {actual} != "
                f"{artifacts[artifact_id]['sha256']}")

    generate_sources(canonical_root, jadx, supplied_artifacts, roots, classes)
    verify_sources(canonical_root, roots, classes)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--manifest", required=True, type=Path)
    parser.add_argument("--smartisanos-jar", required=True, type=Path)
    parser.add_argument("--framework-jar", required=True, type=Path)
    parser.add_argument("--jadx", required=True, type=Path)
    parser.add_argument(
        "--canonical-root", type=Path,
        help="keep the generated canonical tree here; the directory must be empty")
    args = parser.parse_args()

    if args.canonical_root is not None:
        canonical_root = args.canonical_root.resolve()
        if canonical_root.exists() and any(canonical_root.iterdir()):
            raise ProvenanceError(f"canonical root is not empty: {canonical_root}")
        verify(args, canonical_root)
        print(f"Canonical source tree retained at {canonical_root}")
        return
    with tempfile.TemporaryDirectory(prefix="smartisanui-r2-source-provenance-") as directory:
        verify(args, Path(directory))


if __name__ == "__main__":
    main()
