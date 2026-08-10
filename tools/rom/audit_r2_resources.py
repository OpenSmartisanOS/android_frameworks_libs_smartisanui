#!/usr/bin/env python3
"""Run the optional strict R2 artifact, decoded-source and vendored-resource audit."""

from __future__ import annotations

import argparse
import subprocess
import sys
from pathlib import Path


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--framework-res-apk", required=True, type=Path)
    parser.add_argument("--framework-smartisanos-res-apk", required=True, type=Path)
    parser.add_argument("--smartisanos-jar", required=True, type=Path)
    parser.add_argument(
        "--framework-jar", type=Path,
        help="framework.jar; defaults to the sibling of smartisanos.jar")
    parser.add_argument(
        "--jadx", type=Path,
        help="JADX 1.5.6 executable; defaults to ../reverse-tools/jadx/bin/jadx")
    parser.add_argument("--settings-apk", required=True, type=Path)
    parser.add_argument("--decoded-framework-res", required=True, type=Path)
    parser.add_argument("--decoded-smartisanos-res", required=True, type=Path)
    parser.add_argument("--decoded-settings", required=True, type=Path)
    args = parser.parse_args()

    repository = Path(__file__).resolve().parents[2]
    extractor = repository / "tools/rom/extract_resources.py"
    source_verifier = repository / "tools/rom/verify_source_provenance.py"
    output = repository / "core/src/main/res"
    framework_jar = args.framework_jar or args.smartisanos_jar.parent / "framework.jar"
    jadx = args.jadx or repository.parent / "reverse-tools/jadx/bin/jadx"
    decoded = [
        "--decoded-framework-res", str(args.decoded_framework_res),
        "--decoded-smartisanos-res", str(args.decoded_smartisanos_res),
    ]
    common_artifacts = [
        "--artifact", f"framework-res.apk={args.framework_res_apk}",
        "--artifact", f"framework-smartisanos-res.apk={args.framework_smartisanos_res_apk}",
        "--artifact", f"SettingsSmartisan.apk={args.settings_apk}",
    ]

    commands = [
        [
            sys.executable, str(source_verifier),
            "--manifest", str(repository / "core/provenance/r2-sources.json"),
            "--smartisanos-jar", str(args.smartisanos_jar),
            "--framework-jar", str(framework_jar),
            "--jadx", str(jadx),
        ],
        [
            sys.executable, str(extractor),
            "--manifest", str(repository / "core/provenance/r2-dialog-resources.json"),
            "--output", str(output), "--check", "--require-sources",
            *decoded, *common_artifacts,
            "--decoded-artifact", f"SettingsSmartisan.apk={args.decoded_settings}",
            "--artifact", f"smartisanos.jar={args.smartisanos_jar}",
        ],
        [
            sys.executable, str(extractor),
            "--manifest",
            str(repository / "core/provenance/r2-settings-components-resources.json"),
            "--output", str(output), "--check", "--require-sources",
            *decoded, *common_artifacts,
        ],
    ]
    for command in commands:
        subprocess.run(command, cwd=repository, check=True)
    print("Strict Smartisan OS R2 artifact/source/resource audit passed.")


if __name__ == "__main__":
    main()
