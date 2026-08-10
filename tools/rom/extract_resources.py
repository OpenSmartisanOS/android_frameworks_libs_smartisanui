#!/usr/bin/env python3
"""Extract replayable resources and audit hash-pinned Smartisan SDK adaptations."""

from __future__ import annotations

import argparse
import hashlib
import json
import re
import shutil
import tempfile
import xml.etree.ElementTree as ET
from pathlib import Path

ANDROID_NS = "http://schemas.android.com/apk/res/android"
SMARTISAN_NS = "http://schemas.android.com/apk/res/smartisanos"
AUTO_NS = "http://schemas.android.com/apk/res-auto"
PRIVATE_FRAMEWORK_DRAWABLES = {
    "@android:drawable/btn_star_off_pressed_holo_light",
    "@android:drawable/btn_star_on_disabled_focused_holo_dark",
    "@android:drawable/btn_star_on_disabled_focused_holo_light",
}
CLASS_RENAMES = {
    "smartisanos.widget.PasswordEditText":
        "org.opensmartisanos.ui.widget.SmartisanPasswordEditText",
    "smartisanos.widget.QuickDeleteEditText":
        "org.opensmartisanos.ui.widget.SmartisanQuickDeleteEditText",
    "smartisanos.widget.editor.EditorLeftLabelWidget":
        "org.opensmartisanos.ui.internal.SmartisanEditorLeftLabelWidget",
    "smartisanos.widget.editor.EditorRightIconWidget":
        "org.opensmartisanos.ui.internal.SmartisanEditorRightIconWidget",
    "smartisanos.widget.SmoothSeekBar":
        "org.opensmartisanos.ui.widget.SmartisanSmoothSeekBar",
    "smartisanos.widget.MenuDialogTitleBar":
        "org.opensmartisanos.ui.widget.SmartisanDialogTitleBar",
    "smartisanos.widget.SmartisanNumberPicker":
        "org.opensmartisanos.ui.widget.SmartisanNumberPicker",
    "smartisanos.widget.SmartisanNumberPickerEx":
        "org.opensmartisanos.ui.widget.SmartisanNumberPickerEx",
    "smartisanos.widget.SmartisanDatePicker":
        "org.opensmartisanos.ui.widget.SmartisanDatePicker",
    "smartisanos.widget.SmartisanDatePickerEx":
        "org.opensmartisanos.ui.widget.SmartisanDatePickerEx",
    "smartisanos.widget.SmartisanTimePicker":
        "org.opensmartisanos.ui.widget.SmartisanTimePicker",
    "smartisanos.widget.SmartisanTimePickerEx":
        "org.opensmartisanos.ui.widget.SmartisanTimePickerEx",
    "smartisanos.widget.SmartisanDateTimePicker":
        "org.opensmartisanos.ui.widget.SmartisanDateTimePicker",
    "smartisanos.widget.ShadowButton":
        "org.opensmartisanos.ui.widget.SmartisanShadowButton",
}
SHA256 = re.compile(r"^[0-9a-f]{64}$")
DIRECT_MATCHES = {"exact", "decoded-nine-patch", "decoded-nine-patch-name"}
PUBLIC_SDK_ATTRIBUTE_RENAMES = {
    # SDK target attribute -> private Smartisan source attribute.
    "smartisanShowDivider": "show_divider",
}
PUBLIC_SDK_CLASS_RENAMES = {
    **CLASS_RENAMES,
    "smartisanos.widget.FontFitTextView":
        "org.opensmartisanos.ui.internal.RomFontFitTextView",
    "smartisanos.widget.TipsView":
        "org.opensmartisanos.ui.widget.SmartisanTipsBar",
    "smartisanos.widget.support.WrapContentViewPager":
        "org.opensmartisanos.ui.internal.RomPagedView",
    "smartisanos.app.IndicatorView":
        "org.opensmartisanos.ui.internal.RomPageIndicator",
    "com.android.internal.widget.DialogTitle": "TextView",
}


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


def decoded_source_path(root: Path, relative: str) -> Path:
    """Resolve an APK path against apktool output without weakening path checks.

    APK ZIP entries commonly retain the redundant ``-v4`` qualifier, while apktool removes it
    because every supported Android release satisfies v4. The decoded bytes are otherwise the
    canonical source used for NinePatch and XML verification.
    """
    direct = safe_child(root, relative)
    if direct.exists():
        return direct
    parts = [re.sub(r"-v4$", "", part) for part in Path(relative).parts]
    return safe_child(root, str(Path(*parts)))


def require_hash(value: object, label: str) -> str:
    if not isinstance(value, str) or SHA256.fullmatch(value) is None:
        raise RuntimeError(f"invalid SHA-256 for {label}")
    return value


def transformed_xml(source: Path, target: Path, all_references: bool = False) -> None:
    ET.register_namespace("android", ANDROID_NS)
    ET.register_namespace("app", AUTO_NS)
    tree = ET.parse(source)
    for element in tree.iter():
        if all_references and element.tag in CLASS_RENAMES:
            element.tag = CLASS_RENAMES[element.tag]
        elif all_references and element.tag == "smartisanos.widget.RoundedRectLinearLayout":
            element.tag = "org.opensmartisanos.ui.internal.RomRoundedRectLinearLayout"
        elif all_references and element.tag == "smartisanos.widget.DividerListView":
            element.tag = "org.opensmartisanos.ui.internal.RomDividerListView"
        for key, value in tuple(element.attrib.items()):
            if all_references and key.startswith(f"{{{SMARTISAN_NS}}}"):
                del element.attrib[key]
                key = key.replace(f"{{{SMARTISAN_NS}}}", f"{{{AUTO_NS}}}", 1)
                element.set(key, value)
            if value.startswith("@drawable/"):
                element.set(key, value.replace("@drawable/", "@drawable/smartisan_rom_", 1))
            elif value in PRIVATE_FRAMEWORK_DRAWABLES:
                element.set(key, value.replace("@android:drawable/", "@drawable/smartisan_rom_", 1))
            elif all_references and value.startswith("@") and not value.startswith("@android:"):
                marker, separator, name = value.partition("/")
                if separator and marker in {"@color", "@dimen", "@id", "@integer", "@layout", "@string", "@style"}:
                    element.set(key, f"{marker}/smartisan_rom_{name}")
        if all_references and element.tag == "smartisanos.widget.SwitchEx":
            element.tag = "org.opensmartisanos.ui.widget.SmartisanSwitch"
    ET.indent(tree, space="    ")
    tree.write(target, encoding="utf-8", xml_declaration=True)


def materialize(source: Path, target: Path, transform: str | None) -> None:
    target.parent.mkdir(parents=True, exist_ok=True)
    if transform == "prefix_drawable_references":
        transformed_xml(source, target)
    elif transform == "prefix_resource_references":
        transformed_xml(source, target, all_references=True)
    elif transform is None:
        shutil.copyfile(source, target)
    else:
        raise ValueError(f"unknown transform: {transform}")


def local_name(value: str) -> str:
    return value.split("}", 1)[-1]


def semantic_attribute(value: str) -> tuple[str, str]:
    namespace = ""
    if value.startswith("{"):
        namespace, _, value = value[1:].partition("}")
    if namespace == ANDROID_NS:
        namespace = "android"
    elif namespace in {SMARTISAN_NS, AUTO_NS}:
        namespace = "app"
    value = PUBLIC_SDK_ATTRIBUTE_RENAMES.get(value, value)
    return namespace, value


def normalize_literal(value: str) -> str:
    """Normalize apktool's harmless integral rendering without erasing semantics."""
    return re.sub(r"(?<![\d.])(-?\d+)\.0+(?=dp|sp|px|%|%p|$)", r"\1", value)


def resource_reference(value: str) -> tuple[str, str, str, str] | None:
    if value in {"@null", "@empty"} or not value.startswith(("@", "?")):
        return None
    marker = value[0]
    body = value[1:]
    if body.startswith("+"):
        body = body[1:]
    package = ""
    if ":" in body.split("/", 1)[0]:
        package, body = body.split(":", 1)
    resource_type, separator, name = body.partition("/")
    if not separator:
        return None
    return marker, package, resource_type, name


def deterministic_prefixed_reference(source: tuple[str, str, str, str],
                                     target: tuple[str, str, str, str]) -> bool:
    source_marker, source_package, source_type, source_name = source
    target_marker, target_package, target_type, target_name = target
    if source_marker != target_marker or source_type != target_type:
        return False
    if target_name.startswith("smartisan_rom_"):
        target_name = target_name[len("smartisan_rom_"):]
    elif target_name.startswith("smartisan_"):
        target_name = target_name[len("smartisan_"):]
    if source_name != target_name:
        return False
    # References local to framework-res become explicit @android references in a public AAR.
    return (source_package == target_package
            or (source_package in {"", "smartisanos"} and target_package in {"", "android"}))


def verify_xml_structure(source: Path, target: Path, recipe: dict[str, object],
                         label: str) -> tuple[int, int]:
    """Verify an XML transform without reducing references to resource types.

    Deterministic ``smartisan_``/``smartisan_rom_`` prefixing is reversed first. Remaining
    reference aliases must form a bijection, and all structural/literal changes must be declared
    by the selected transformation recipe.
    """
    source_elements = list(ET.parse(source).getroot().iter())
    target_elements = list(ET.parse(target).getroot().iter())
    if len(source_elements) != len(target_elements):
        raise RuntimeError(
            f"XML element-count mismatch for {label}: "
            f"{len(source_elements)} != {len(target_elements)}")

    allowed_removed = set(recipe.get("removed_attributes", []))
    allowed_added = set(recipe.get("added_attributes", []))
    literal_maps = {
        tuple(item) for item in recipe.get("literal_value_maps", [])
        if isinstance(item, list) and len(item) == 3
    }
    source_to_target: dict[tuple[str, str, str, str], tuple[str, str, str, str]] = {}
    target_to_source: dict[tuple[str, str, str, str], tuple[str, str, str, str]] = {}
    aliases = 0

    reverse_classes = {target_name: source_name
                       for source_name, target_name in PUBLIC_SDK_CLASS_RENAMES.items()}
    for position, (source_element, target_element) in enumerate(
            zip(source_elements, target_elements)):
        if len(source_element) != len(target_element):
            raise RuntimeError(
                f"XML hierarchy mismatch for {label} at element {position}: "
                f"{len(source_element)} children != {len(target_element)}")
        source_tag = local_name(source_element.tag)
        raw_target_tag = local_name(target_element.tag)
        target_tag = (source_tag if source_tag == raw_target_tag
                      else reverse_classes.get(raw_target_tag, raw_target_tag))
        if source_tag != target_tag:
            tag_maps = {tuple(item) for item in recipe.get("tag_maps", [])
                        if isinstance(item, list) and len(item) == 2}
            if (source_tag, local_name(target_element.tag)) not in tag_maps:
                raise RuntimeError(
                    f"undeclared tag remap for {label} at element {position}: "
                    f"{source_tag} -> {local_name(target_element.tag)}")

        source_attributes = {semantic_attribute(key): value
                             for key, value in source_element.attrib.items()}
        target_attributes = {semantic_attribute(key): value
                             for key, value in target_element.attrib.items()}
        if len(source_attributes) != len(source_element.attrib) \
                or len(target_attributes) != len(target_element.attrib):
            raise RuntimeError(f"attribute remap collision for {label} at element {position}")
        removed = set(source_attributes) - set(target_attributes)
        added = set(target_attributes) - set(source_attributes)
        removed_names = {name for _, name in removed}
        added_names = {name for _, name in added}
        if not removed_names <= allowed_removed or not added_names <= allowed_added:
            raise RuntimeError(
                f"undeclared attribute remap for {label} at element {position}: "
                f"removed={sorted(removed)}, added={sorted(added)}")

        for attribute in sorted(set(source_attributes) & set(target_attributes)):
            attribute_name = attribute[1]
            source_value = normalize_literal(source_attributes[attribute])
            target_value = normalize_literal(target_attributes[attribute])
            if source_value == target_value:
                continue
            source_ref = resource_reference(source_value)
            target_ref = resource_reference(target_value)
            if source_ref is not None and target_ref is not None:
                if deterministic_prefixed_reference(source_ref, target_ref):
                    continue
                if not recipe.get("allow_reference_aliases"):
                    raise RuntimeError(
                        f"recipe does not allow reference aliases for {label}: "
                        f"{source_value} -> {target_value}")
                previous_target = source_to_target.setdefault(source_ref, target_ref)
                previous_source = target_to_source.setdefault(target_ref, source_ref)
                if previous_target != target_ref or previous_source != source_ref:
                    raise RuntimeError(
                        f"non-bijective resource remap for {label}: "
                        f"{source_value} -> {target_value}")
                aliases += 1
                continue
            if (attribute_name, source_value, target_value) not in literal_maps:
                raise RuntimeError(
                    f"undeclared literal remap for {label} at element {position}, "
                    f"{attribute_name}: {source_value!r} -> {target_value!r}")

        source_text = normalize_literal((source_element.text or "").strip())
        target_text = normalize_literal((target_element.text or "").strip())
        if source_text != target_text:
            source_ref = resource_reference(source_text)
            target_ref = resource_reference(target_text)
            if source_ref is not None and target_ref is not None:
                if deterministic_prefixed_reference(source_ref, target_ref):
                    continue
                if not recipe.get("allow_reference_aliases"):
                    raise RuntimeError(
                        f"recipe does not allow text reference aliases for {label}")
                previous_target = source_to_target.setdefault(source_ref, target_ref)
                previous_source = target_to_source.setdefault(target_ref, source_ref)
                if previous_target != target_ref or previous_source != source_ref:
                    raise RuntimeError(f"non-bijective text resource remap for {label}")
                aliases += 1
            elif ("#text", source_text, target_text) not in literal_maps:
                raise RuntimeError(
                    f"undeclared text remap for {label} at element {position}: "
                    f"{source_text!r} -> {target_text!r}")
    return len(source_to_target), aliases


def named_resource(root: ET.Element, selector: str, label: str) -> ET.Element:
    resource_type, separator, name = selector.partition(":")
    if not separator or not resource_type or not name:
        raise RuntimeError(f"invalid resource selector for {label}: {selector}")
    matches = [element for element in root
               if local_name(element.tag) == resource_type and element.get("name") == name]
    if len(matches) != 1:
        raise RuntimeError(
            f"resource selector for {label} matched {len(matches)} nodes: {selector}")
    return matches[0]


def fragment_digest(source: Path, target: Path, source_symbols: list[str],
                    target_symbols: list[str]) -> str:
    source_root = ET.parse(source).getroot()
    target_root = ET.parse(target).getroot()
    checksum = hashlib.sha256()
    for side, root, symbols in (("source", source_root, source_symbols),
                                ("target", target_root, target_symbols)):
        checksum.update(side.encode("ascii"))
        for selector in symbols:
            element = named_resource(root, selector, str(source if side == "source" else target))
            checksum.update(selector.encode("utf-8"))
            canonical = ET.canonicalize(
                ET.tostring(element, encoding="unicode"), strip_text=True)
            checksum.update(canonical.encode("utf-8"))
    return checksum.hexdigest()


def verify_density_fragment(source: Path, target: Path, source_symbols: list[str],
                            target_symbols: list[str], density_dpi: object,
                            label: str) -> None:
    if not isinstance(density_dpi, int) or density_dpi <= 0:
        raise RuntimeError(f"density-safe recipe lacks positive source_density_dpi: {label}")
    if len(source_symbols) != 1 or len(target_symbols) != 1:
        raise RuntimeError(f"density-safe recipe requires one source and target value: {label}")
    source_element = named_resource(
        ET.parse(source).getroot(), source_symbols[0], label)
    target_element = named_resource(
        ET.parse(target).getroot(), target_symbols[0], label)
    source_match = re.fullmatch(r"(-?[\d.]+)\.0*px|(-?[\d.]+)px",
                                (source_element.text or "").strip())
    target_match = re.fullmatch(r"(-?[\d.]+)dp", (target_element.text or "").strip())
    if source_match is None or target_match is None:
        raise RuntimeError(f"density-safe values have unexpected units: {label}")
    source_px = float(source_match.group(1) or source_match.group(2))
    expected_dp = source_px * 160.0 / density_dpi
    if abs(expected_dp - float(target_match.group(1))) > 0.011:
        raise RuntimeError(
            f"density-safe conversion mismatch for {label}: "
            f"{source_px}px at {density_dpi}dpi != {target_match.group(1)}dp")


def verify_required_fragment_values(source: Path, target: Path, source_symbols: list[str],
                                    target_symbols: list[str], mappings: object,
                                    label: str) -> None:
    if mappings is None:
        return
    if not isinstance(mappings, list) or not mappings:
        raise RuntimeError(f"invalid required_value_maps for {label}")
    source_root = ET.parse(source).getroot()
    target_root = ET.parse(target).getroot()
    source_fragment = named_resource(source_root, source_symbols[0], label)
    target_fragment = named_resource(target_root, target_symbols[0], label)
    for mapping in mappings:
        if not isinstance(mapping, list) or len(mapping) != 3:
            raise RuntimeError(f"invalid required value mapping for {label}: {mapping}")
        source_name, target_name, expected = mapping

        def item_value(fragment: ET.Element, name: object) -> str:
            values = [(element.text or "").strip() for element in fragment
                      if local_name(element.tag) == "item" and element.get("name") == name]
            if len(values) != 1:
                raise RuntimeError(
                    f"required item {name!r} matched {len(values)} nodes for {label}")
            return normalize_literal(values[0])

        source_value = item_value(source_fragment, source_name)
        target_value = item_value(target_fragment, target_name)
        if source_value != expected or target_value != expected:
            raise RuntimeError(
                f"required value mapping mismatch for {label}: "
                f"{source_name}={source_value!r}, {target_name}={target_value!r}, "
                f"expected {expected!r}")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--decoded-res", type=Path)
    parser.add_argument("--decoded-framework-res", type=Path)
    parser.add_argument("--decoded-smartisanos-res", type=Path)
    parser.add_argument("--decoded-artifact", action="append", default=[],
                        metavar="NAME=PATH")
    parser.add_argument("--artifact", action="append", default=[], metavar="NAME=PATH")
    parser.add_argument("--manifest", required=True, type=Path)
    parser.add_argument("--output", required=True, type=Path)
    parser.add_argument("--check", action="store_true")
    parser.add_argument("--require-sources", action="store_true")
    args = parser.parse_args()

    def named_paths(values: list[str]) -> dict[str, Path]:
        result: dict[str, Path] = {}
        for value in values:
            name, separator, path = value.partition("=")
            if not separator or not name or not path:
                parser.error(f"expected NAME=PATH, got: {value}")
            result[name] = Path(path)
        return result

    decoded_roots = {
        "framework-res.apk": args.decoded_framework_res,
        "framework-smartisanos-res.apk": args.decoded_smartisanos_res,
    }
    decoded_roots = {name: root for name, root in decoded_roots.items() if root is not None}
    decoded_roots.update(named_paths(args.decoded_artifact))
    if args.decoded_res is not None:
        decoded_roots["default"] = args.decoded_res
    manifest = json.loads(args.manifest.read_text(encoding="utf-8"))
    artifacts = manifest.get("artifacts", {})
    if not isinstance(artifacts, dict):
        raise RuntimeError("artifacts must be an object")
    for name, expected in artifacts.items():
        require_hash(expected, f"artifact {name}")
    artifact_paths = named_paths(args.artifact)
    if args.require_sources:
        missing_artifacts = sorted(set(artifacts) - set(artifact_paths))
        if missing_artifacts:
            raise RuntimeError("strict audit is missing source artifacts: "
                               + ", ".join(missing_artifacts))
    for name, path in artifact_paths.items():
        expected = artifacts.get(name)
        if expected is None:
            raise RuntimeError(f"artifact is not declared by manifest: {name}")
        if not path.is_file() or digest(path) != expected:
            raise RuntimeError(f"artifact hash mismatch: {name}")

    direct_entries = manifest.get("resources", [])
    transformed_entries = manifest.get("transformed_resources", [])
    adaptation_entries = manifest.get("project_adaptation_resources", [])
    aggregate_entries = manifest.get("tracked_aggregate_resources", [])
    recipes = manifest.get("transformation_recipes", {})
    if transformed_entries and not isinstance(recipes, dict):
        raise RuntimeError("transformation_recipes must be an object")
    for name, recipe in recipes.items():
        if not isinstance(recipe, dict) or not recipe.get("description"):
            raise RuntimeError(f"transformation recipe must be a described object: {name}")
    entries = direct_entries + transformed_entries
    non_replayable = [entry["destination"] for entry in transformed_entries
                      if entry.get("transform") is None]
    if not args.check and non_replayable:
        preview = ", ".join(non_replayable[:3])
        if len(non_replayable) > 3:
            preview += f", and {len(non_replayable) - 3} more"
        parser.error(
            "cannot extract non-replayable public-SDK adaptations; their named semantic "
            f"contracts can only be audited with --check: {preview}")
    if not args.check and not decoded_roots:
        parser.error("provide a decoded resource root for extraction")

    decoded_source_files = manifest.get("decoded_source_files", {})
    for source_key, expected in decoded_source_files.items():
        require_hash(expected, f"decoded source {source_key}")
    destination_hashes: dict[str, str] = {}
    fragment_targets: dict[str, set[str]] = {}
    replayed_transforms = 0
    structural_transforms = 0
    fragment_transforms = 0
    reference_aliases = 0
    deferred_transforms = 0
    for entry in adaptation_entries + aggregate_entries:
        if not entry.get("reason"):
            raise RuntimeError(f"missing reason for project resource: {entry.get('destination')}")
        destination = safe_child(args.output, entry["destination"])
        expected = require_hash(entry.get("sha256"), entry["destination"])
        if not destination.is_file() or digest(destination) != expected:
            raise RuntimeError(f"project adaptation resource differs: {entry['destination']}")
    with tempfile.TemporaryDirectory(prefix="smartisan-res-") as directory:
        temporary_root = Path(directory)
        for index, entry in enumerate(entries):
            transformed = index >= len(direct_entries)
            artifact = entry.get("source_artifact", "default")
            if artifact != "default" and artifact not in artifacts:
                raise RuntimeError(f"undeclared source artifact: {artifact}")
            decoded_root = decoded_roots.get(artifact) or decoded_roots.get("default")
            source_reference = entry["source"]
            source_relative = source_reference.split("#", 1)[0]
            source_key = f"{artifact}:{source_relative}"
            declared_source = entry.get("source_sha256")
            recorded_source = decoded_source_files.get(source_key)
            if declared_source is not None and recorded_source is not None \
                    and declared_source != recorded_source:
                raise RuntimeError(f"conflicting source hashes: {source_reference}")
            match = entry.get("match")
            if transformed:
                if match not in recipes:
                    raise RuntimeError(
                            f"unknown transformation recipe for {entry['destination']}: {match}")
                require_hash(entry.get("source_sha256"), source_reference)
                if entry.get("transform") is None:
                    if not source_relative.endswith(".xml") \
                            or not entry["destination"].endswith(".xml"):
                        raise RuntimeError(
                            f"non-replayable transform is not XML: {entry['destination']}")
                    if "#" in source_reference:
                        recipe = recipes[match]
                        source_symbols = entry.get("source_symbols")
                        target_symbols = entry.get("target_symbols")
                        if not isinstance(source_symbols, list) or not source_symbols:
                            raise RuntimeError(
                                f"fragment transform lacks source_symbols: {source_reference}")
                        if not isinstance(target_symbols, list) or not target_symbols:
                            raise RuntimeError(
                                f"fragment transform lacks target_symbols: {entry['destination']}")
                        require_hash(
                            entry.get("fragment_sha256"),
                            f"fragment contract {source_reference} -> {entry['destination']}")
                        if (len(source_symbols) != len(target_symbols)
                                and not recipe.get("allow_fragment_count_mismatch")):
                            raise RuntimeError(
                                f"fragment symbol count mismatch without recipe approval: "
                                f"{source_reference} -> {entry['destination']}")
                        known_targets = fragment_targets.setdefault(entry["destination"], set())
                        duplicate_targets = known_targets.intersection(target_symbols)
                        if duplicate_targets:
                            raise RuntimeError(
                                f"duplicate target fragment ownership in {entry['destination']}: "
                                + ", ".join(sorted(duplicate_targets)))
                        known_targets.update(target_symbols)
            elif match not in DIRECT_MATCHES:
                raise RuntimeError(f"unknown direct-resource match mode: {match}")
            if decoded_root is None and args.require_sources:
                raise RuntimeError(f"missing decoded root for {artifact}")
            destination = safe_child(args.output, entry["destination"])
            expected_destination = require_hash(entry.get("sha256"), entry["destination"])
            previous_hash = destination_hashes.setdefault(
                    entry["destination"], expected_destination)
            if previous_hash != expected_destination:
                raise RuntimeError(
                        f"conflicting destination hashes: {entry['destination']}")

            if decoded_root is not None:
                source = decoded_source_path(decoded_root, source_relative)
                expected_source = recorded_source
                if "#" in source_reference and expected_source is None:
                    raise RuntimeError(f"missing decoded source-file hash: {source_key}")
                if expected_source is None:
                    expected_source = declared_source or expected_destination
                require_hash(expected_source, source_reference)
                if not source.is_file() or digest(source) != expected_source:
                    raise RuntimeError(f"source hash mismatch: {source_reference}")

                # Every transformed entry is either deterministically replayed, structurally
                # checked, or bound to exact named source/target fragments. There is deliberately
                # no hash-only/review-only success path.
                if transformed and entry.get("transform") is not None:
                    generated = safe_child(temporary_root, f"replay-{index}/"
                                            + entry["destination"])
                    materialize(source, generated, entry.get("transform"))
                    if digest(generated) != expected_destination:
                        raise RuntimeError(
                                f"transformation replay mismatch: {entry['destination']}")
                    replayed_transforms += 1
                elif transformed:
                    recipe = recipes[match]
                    source_symbols = entry.get("source_symbols")
                    target_symbols = entry.get("target_symbols")
                    if "#" in source_reference:
                        if not isinstance(source_symbols, list) or not source_symbols:
                            raise RuntimeError(
                                f"fragment transform lacks source_symbols: {source_reference}")
                        if not isinstance(target_symbols, list) or not target_symbols:
                            raise RuntimeError(
                                f"fragment transform lacks target_symbols: {entry['destination']}")
                        expected_fragment = require_hash(
                            entry.get("fragment_sha256"),
                            f"fragment contract {source_reference} -> {entry['destination']}")
                        actual_fragment = fragment_digest(
                            source, destination, source_symbols, target_symbols)
                        if actual_fragment != expected_fragment:
                            raise RuntimeError(
                                f"fragment contract mismatch: {source_reference} -> "
                                f"{entry['destination']}")
                        if match == "density-safe-value-remap":
                            verify_density_fragment(
                                source, destination, source_symbols, target_symbols,
                                entry.get("source_density_dpi"), entry["destination"])
                        verify_required_fragment_values(
                            source, destination, source_symbols, target_symbols,
                            entry.get("required_value_maps"), entry["destination"])
                        fragment_transforms += 1
                    else:
                        _, aliases = verify_xml_structure(
                            source, destination, recipe, entry["destination"])
                        reference_aliases += aliases
                        structural_transforms += 1
            elif transformed:
                deferred_transforms += 1

            if args.check:
                if not destination.is_file() or digest(destination) != expected_destination:
                    raise RuntimeError(f"vendored resource differs: {entry['destination']}")
            else:
                generated = safe_child(temporary_root, entry["destination"])
                materialize(source, generated, entry.get("transform"))
                destination.parent.mkdir(parents=True, exist_ok=True)
                shutil.copyfile(generated, destination)

    for relative in manifest.get("complete_fragment_destinations", []):
        destination = safe_child(args.output, relative)
        if not destination.is_file():
            raise RuntimeError(f"complete fragment destination is missing: {relative}")
        root = ET.parse(destination).getroot()
        actual_symbols = {
            f"{local_name(element.tag)}:{element.get('name')}"
            for element in root if element.get("name")
        }
        declared_symbols = fragment_targets.get(relative, set())
        if actual_symbols != declared_symbols:
            raise RuntimeError(
                f"incomplete fragment provenance for {relative}: "
                f"missing={sorted(actual_symbols - declared_symbols)}, "
                f"stale={sorted(declared_symbols - actual_symbols)}")

    verified_transforms = replayed_transforms + structural_transforms + fragment_transforms
    if args.require_sources and verified_transforms != len(transformed_entries):
        raise RuntimeError(
            f"only {verified_transforms}/{len(transformed_entries)} transforms were verified")
    if verified_transforms + deferred_transforms != len(transformed_entries):
        raise RuntimeError(
            f"transform accounting mismatch: {verified_transforms} verified + "
            f"{deferred_transforms} deferred != {len(transformed_entries)}")
    action = "verified vendored" if args.check else "extracted"
    print(f"{action} {len(entries)} Smartisan ROM resources, "
          f"{len(adaptation_entries)} project adaptations and "
          f"{len(aggregate_entries)} tracked aggregates; "
          f"{verified_transforms} transforms source-verified: "
          f"{replayed_transforms} replayed, "
          f"{structural_transforms} XML transforms structurally verified, "
          f"{fragment_transforms} named-fragment contracts verified "
          f"({reference_aliases} one-to-one alias occurrences under explicit recipes); "
          f"{deferred_transforms} source audits deferred")


if __name__ == "__main__":
    main()
