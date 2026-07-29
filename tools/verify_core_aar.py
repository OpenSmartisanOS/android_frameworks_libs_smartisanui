#!/usr/bin/env python3
"""Verify that the published Core AAR is a self-contained public-SDK artifact."""

from __future__ import annotations

import argparse
import io
import zipfile
from pathlib import Path


REQUIRED_CLASSES = (
    "org/opensmartisanos/ui/widget/SmartisanQuickDeleteEditText.class",
    "org/opensmartisanos/ui/widget/SmartisanPasswordEditText.class",
    "org/opensmartisanos/ui/widget/SmartisanEditor.class",
    "org/opensmartisanos/ui/widget/SmartisanSimpleEditor.class",
    "org/opensmartisanos/ui/widget/SmartisanLabelEditor.class",
)
REQUIRED_RESOURCES = (
    "res/layout/smartisan_rom_abs_editor_layout.xml",
    "res/drawable/smartisan_rom_pwd_eye_open_close_anim.xml",
    "res/drawable-xxhdpi-v4/smartisan_rom_editor_bg_single.9.png",
)
FORBIDDEN_BYTECODE_REFERENCES = (
    b"com/android/internal/",
    b"com/smartisanos/internal/",
    b"smartisanos/widget/",
)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("aar", type=Path)
    args = parser.parse_args()
    with zipfile.ZipFile(args.aar) as aar:
        aar_names = set(aar.namelist())
        for resource in REQUIRED_RESOURCES:
            if resource not in aar_names:
                raise RuntimeError(f"missing AAR resource: {resource}")
        with zipfile.ZipFile(io.BytesIO(aar.read("classes.jar"))) as classes:
            class_names = set(classes.namelist())
            for name in REQUIRED_CLASSES:
                if name not in class_names:
                    raise RuntimeError(f"missing public class: {name}")
            for name in class_names:
                if not name.endswith(".class"):
                    continue
                bytecode = classes.read(name)
                for forbidden in FORBIDDEN_BYTECODE_REFERENCES:
                    if forbidden in bytecode:
                        raise RuntimeError(f"private API reference {forbidden!r} in {name}")
    print("verified self-contained Smartisan Core AAR")


if __name__ == "__main__":
    main()
