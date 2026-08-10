#!/usr/bin/env python3
"""Check API 3 against immutable v1 and the actual R2 DEX contract.

The human-readable current API baseline protects every SDK addition.  This
checker independently maps public/protected R2 DEX classes and members to the
public-SDK port, so updating ``current.txt`` cannot approve an incomplete port.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import re
from pathlib import Path


CLASS = re.compile(r"^public (?:final |abstract )?(?:class|interface) ([^ <{]+)")
DEX_CLASS = re.compile(
        r"^class (\S+) access=0x([0-9a-f]+) super=(\S*)(?: interfaces=(.*))?$")
DEX_MEMBER = re.compile(
        r"^  (field|method) ([^ :(()]+)(?::([^ ]+)|\((.*?)\)(\S+)) "
        r"access=0x([0-9a-f]+)$")
JAVAP_CLASS = re.compile(
        r"^public (?:(?:final|abstract) )?(class|interface) ([^ <{]+)"
        r"(?: extends ([^ {]+?))?(?: implements ([^ {]+?))? \{$")

ACC_PUBLIC = 0x1
ACC_PROTECTED = 0x4
ACC_INTERFACE = 0x200


def api_blocks(path: Path) -> dict[str, set[str]]:
    result: dict[str, set[str]] = {}
    lines = path.read_text(encoding="utf-8").splitlines()
    current_class: str | None = None
    current_lines: set[str] = set()
    for line in lines:
        match = CLASS.match(line)
        if match:
            if current_class is not None:
                result[current_class] = current_lines
            current_class = match.group(1)
            current_lines = {line.strip()}
        elif current_class is not None and not line.startswith("Compiled from "):
            current_lines.add(line.strip())
    if current_class is not None:
        result[current_class] = current_lines
    return result


def block_digest(lines: set[str]) -> str:
    canonical = "\n".join(sorted(lines)) + "\n"
    return hashlib.sha256(canonical.encode("utf-8")).hexdigest()


def descriptor_for_class(class_name: str) -> str:
    return "L" + class_name.replace(".", "/") + ";"


def sdk_descriptor(original: str, mapping: dict) -> str:
    substitutions = mapping.get("r2_descriptor_substitutions", {})
    if original in substitutions:
        return substitutions[original]
    roots = sorted(mapping["r2_class_mappings"],
                   key=lambda entry: len(entry["original"]), reverse=True)
    for entry in roots:
        source = entry["original"]
        prefix = source[:-1]
        if original == source or original.startswith(prefix + "$"):
            target = descriptor_for_class(entry["sdk"])
            return target[:-1] + original[len(prefix):-1] + ";"
    return original


def map_descriptor(descriptor: str, mapping: dict) -> str:
    return re.sub(r"L[^;]+;",
                  lambda match: sdk_descriptor(match.group(0), mapping),
                  descriptor)


def java_name(descriptor: str) -> str:
    if not descriptor.startswith("L") or not descriptor.endswith(";"):
        raise ValueError(f"not a class descriptor: {descriptor}")
    return descriptor[1:-1].replace("/", ".")


def split_java_types(value: str | None) -> set[str]:
    if not value:
        return set()
    result: set[str] = set()
    depth = 0
    start = 0
    for index, character in enumerate(value + ","):
        if character == "<":
            depth += 1
        elif character == ">":
            depth -= 1
        elif character == "," and depth == 0:
            item = value[start:index].strip()
            if item:
                result.add(re.sub(r"<.*>", "", item))
            start = index + 1
    return result


def javap_access(path: Path) -> tuple[dict[str, dict], dict[str, set[tuple[str, str, str]]]]:
    """Parse javap -protected -s output into class and member contracts."""
    classes: dict[str, dict] = {}
    members: dict[str, set[tuple[str, str, str]]] = {}
    current_class: str | None = None
    pending: tuple[str, str] | None = None
    for raw in path.read_text(encoding="utf-8").splitlines():
        header = JAVAP_CLASS.match(raw)
        if header:
            kind, current_class, extends, implements = header.groups()
            interfaces = split_java_types(implements)
            superclass = (re.sub(r"<.*>", "", extends.strip())
                          if extends and kind == "class" else None)
            if kind == "interface":
                interfaces |= split_java_types(extends)
            classes[current_class] = {
                "interface": kind == "interface",
                "superclass": superclass,
                "interfaces": interfaces,
            }
            members.setdefault(current_class, set())
            pending = None
            continue
        if current_class is not None and re.match(r"  (?:public|protected) ", raw):
            declaration = raw.strip()
            pending = (declaration.split()[0], declaration)
            continue
        if current_class is None or pending is None:
            continue
        stripped = raw.strip()
        if not stripped.startswith("descriptor: "):
            continue
        visibility, declaration = pending
        pending = None
        descriptor = stripped.split(": ", 1)[1]
        prefix = (declaration.split("(", 1)[0] if "(" in declaration
                  else declaration.split("=", 1)[0].rstrip(";").rstrip())
        token = prefix.split()[-1]
        simple_class = current_class.rsplit(".", 1)[-1]
        name = "<init>" if token in (current_class, simple_class) else token.rsplit(".", 1)[-1]
        members[current_class].add((name, descriptor, visibility))
    return classes, members


def r2_access(path: Path) -> tuple[dict[str, dict], dict[str, set[tuple[str, str, int]]]]:
    classes: dict[str, dict] = {}
    members: dict[str, set[tuple[str, str, int]]] = {}
    current_class: str | None = None
    for raw in path.read_text(encoding="utf-8").splitlines():
        header = DEX_CLASS.match(raw)
        if header:
            current_class, access_hex, superclass, interfaces = header.groups()
            access = int(access_hex, 16)
            classes[current_class] = {
                "access": access,
                "interface": bool(access & ACC_INTERFACE),
                "superclass": superclass or None,
                "interfaces": set(filter(None, (interfaces or "").split(","))),
            }
            members.setdefault(current_class, set())
            continue
        member = DEX_MEMBER.match(raw)
        if current_class is None or member is None:
            continue
        kind, name, field_type, parameters, returns, access_hex = member.groups()
        access = int(access_hex, 16)
        if not access & (ACC_PUBLIC | ACC_PROTECTED):
            continue
        descriptor = (field_type if kind == "field"
                      else f"({parameters or ''}){returns}")
        members[current_class].add((name, descriptor, access))
    return classes, members


def check_r2_contract(mapping: dict, dex_classes: dict, dex_members: dict,
                      sdk_classes: dict, sdk_members: dict) -> None:
    ignored_interfaces = set(mapping.get("ignored_r2_interfaces", []))
    failures: list[str] = []
    for original, original_class in dex_classes.items():
        if not original_class["access"] & (ACC_PUBLIC | ACC_PROTECTED):
            continue
        mapped_descriptor = sdk_descriptor(original, mapping)
        if mapped_descriptor == original:
            failures.append(f"unmapped public/protected R2 class: {original}")
            continue
        target = java_name(mapped_descriptor)
        target_class = sdk_classes.get(target)
        if target_class is None:
            failures.append(f"missing mapped R2 class: {original} -> {target}")
            continue
        if original_class["interface"] != target_class["interface"]:
            failures.append(f"class kind changed: {original} -> {target}")

        original_super = original_class["superclass"]
        if original_super and original_super != "Ljava/lang/Object;":
            expected_super = java_name(map_descriptor(original_super, mapping))
            if target_class["superclass"] != expected_super:
                failures.append(
                        f"superclass changed: {original} expected {expected_super}, "
                        f"found {target_class['superclass']}")
        expected_interfaces = {
            java_name(map_descriptor(item, mapping))
            for item in original_class["interfaces"]
            if item not in ignored_interfaces
        }
        missing_interfaces = expected_interfaces - target_class["interfaces"]
        if missing_interfaces:
            failures.append(
                    f"interfaces missing from {target}: {sorted(missing_interfaces)}")

        available = sdk_members.get(target, set())
        for name, descriptor, access in dex_members.get(original, set()):
            expected_descriptor = map_descriptor(descriptor, mapping)
            found = any(
                    candidate_name == name
                    and candidate_descriptor == expected_descriptor
                    and (visibility == "public" if access & ACC_PUBLIC
                         else visibility in ("public", "protected"))
                    for candidate_name, candidate_descriptor, visibility in available)
            if not found:
                required = "public" if access & ACC_PUBLIC else "protected"
                failures.append(
                        f"{required} R2 member missing: {original} {name}"
                        f"{descriptor} -> {target} {name}{expected_descriptor}")
    if failures:
        raise SystemExit("R2 DEX API contract mismatch:\n  " + "\n  ".join(failures))


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--v1", required=True, type=Path)
    parser.add_argument("--current", required=True, type=Path)
    parser.add_argument("--current-access", required=True, type=Path)
    parser.add_argument("--mapping", required=True, type=Path)
    parser.add_argument("--r2-access", required=True, type=Path)
    parser.add_argument("--update-mapped-hashes", action="store_true")
    args = parser.parse_args()

    v1 = api_blocks(args.v1)
    current = api_blocks(args.current)
    mapping = json.loads(args.mapping.read_text(encoding="utf-8"))
    access_bytes = args.r2_access.read_bytes()
    expected_access_hash = mapping["r2_dex_access_baseline"]["sha256"]
    if hashlib.sha256(access_bytes).hexdigest() != expected_access_hash:
        raise SystemExit("R2 DEX access baseline hash changed")
    access_text = access_bytes.decode("utf-8")
    for source, target in mapping.get("r2_descriptor_substitutions", {}).items():
        if source not in access_text:
            raise SystemExit(f"unused R2 descriptor substitution: {source}")
        if target.startswith("Lsmartisanos/") or target.startswith("Lcom/android/internal/"):
            raise SystemExit(f"descriptor substitution still uses a private API: {target}")
    for descriptor in mapping.get("ignored_r2_interfaces", []):
        if descriptor not in access_text or not descriptor.startswith("Lsmartisanos/"):
            raise SystemExit(f"invalid ignored private R2 interface: {descriptor}")
    for descriptor in mapping["r2_dex_access_baseline"]["root_classes"]:
        if re.search(rf"^class {re.escape(descriptor)} access=", access_text,
                     re.MULTILINE) is None:
            raise SystemExit(f"R2 DEX root class missing from access baseline: {descriptor}")
    class_mappings = mapping["r2_class_mappings"]
    mapped_roots = {entry["original"] for entry in class_mappings}
    expected_roots = set(mapping["r2_dex_access_baseline"]["root_classes"])
    if mapped_roots != expected_roots:
        raise SystemExit("R2 original-to-SDK class mapping is incomplete")
    mapped_sdk_classes = {entry["sdk"] for entry in class_mappings}
    mapped_sdk_classes.update(entry["sdk"] for entry in mapping["framework_source_mappings"])
    if mapped_sdk_classes != set(mapping["mapped_public_classes"]):
        raise SystemExit("mapped_public_classes differs from the source-to-SDK mapping")

    dex_classes, dex_members = r2_access(args.r2_access)
    sdk_classes, sdk_members = javap_access(args.current_access)
    check_r2_contract(mapping, dex_classes, dex_members, sdk_classes, sdk_members)
    removed = set(mapping["allowed_removed_v1_classes"])
    changed = {name: set(lines) for name, lines
               in mapping["allowed_changed_v1_members"].items()}

    for class_name, members in v1.items():
        if class_name in removed:
            if class_name in current:
                raise SystemExit(f"approved removed API unexpectedly returned: {class_name}")
            continue
        if class_name not in current:
            raise SystemExit(f"v1 public class removed: {class_name}")
        missing = members - current[class_name] - changed.get(class_name, set())
        missing.discard("}")
        if missing:
            raise SystemExit(f"v1 API changed in {class_name}: {sorted(missing)}")

    for class_name in mapping["mapped_public_classes"]:
        if class_name not in current:
            raise SystemExit(f"mapped R2 public class is missing: {class_name}")

    approved_api3 = set(mapping["approved_api3_classes"])
    actual_api3 = set(current) - set(v1)
    if actual_api3 != approved_api3:
        raise SystemExit(
                "API 3 class approval mismatch; unexpected="
                f"{sorted(actual_api3 - approved_api3)}, "
                f"missing={sorted(approved_api3 - actual_api3)}")

    hashed_classes = mapped_sdk_classes | approved_api3
    missing_hash_classes = hashed_classes - set(current)
    if missing_hash_classes:
        raise SystemExit(f"cannot hash missing mapped API classes: {sorted(missing_hash_classes)}")
    actual_hashes = {
        class_name: block_digest(current[class_name])
        for class_name in sorted(hashed_classes)
    }
    if args.update_mapped_hashes:
        mapping["mapped_sdk_api_sha256"] = actual_hashes
        args.mapping.write_text(json.dumps(mapping, indent=2) + "\n", encoding="utf-8")
    else:
        recorded_hashes = mapping.get("mapped_sdk_api_sha256", {})
        if set(recorded_hashes) != hashed_classes:
            raise SystemExit(
                    "mapped SDK API hash coverage mismatch; unexpected="
                    f"{sorted(set(recorded_hashes) - hashed_classes)}, "
                    f"missing={sorted(hashed_classes - set(recorded_hashes))}")
        mismatched_hashes = [
            name for name, digest in actual_hashes.items()
            if recorded_hashes[name] != digest
        ]
        if mismatched_hashes:
            raise SystemExit(
                    "mapped SDK API hashes changed: " + ", ".join(mismatched_hashes))

    for class_name, signature in mapping["private_dex_helpers"]:
        if signature in current.get(class_name, set()):
            raise SystemExit(f"private R2 helper leaked into public API: {class_name} {signature}")

    ui_members = current.get("org.opensmartisanos.ui.SmartisanUi", set())
    if "public static final int API_VERSION = 3;" not in ui_members:
        raise SystemExit("SmartisanUi.API_VERSION must remain 3")
    action = "updated" if args.update_mapped_hashes else "verified"
    print(f"v1 compatibility, R2 DEX access and mapped API hashes {action}.")


if __name__ == "__main__":
    main()
