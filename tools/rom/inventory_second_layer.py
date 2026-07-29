#!/usr/bin/env python3
"""Inventory real second-layer Smartisan widget use across decoded ROM apps."""

from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path


COMPONENTS = (
    ("editor", "smartisanos.widget.QuickDeleteEditText", "SmartisanQuickDeleteEditText"),
    ("editor", "smartisanos.widget.PasswordEditText", "SmartisanPasswordEditText"),
    ("editor", "smartisanos.widget.editor.AbsEditor", "SmartisanEditor"),
    ("editor", "smartisanos.widget.editor.SimpleEditor", "SmartisanSimpleEditor"),
    ("editor", "smartisanos.widget.editor.LabelEditor", "SmartisanLabelEditor"),
    ("bottom_bar", "smartisanos.widget.BottomBar", "SmartisanBottomBar"),
    ("bottom_bar", "smartisanos.widget.BottomBarItemView", "SmartisanBottomBarItemView"),
    ("bottom_bar", "smartisanos.widget.SmartisanBottomBar", "SmartisanIconBottomBar"),
    ("bottom_bar", "smartisanos.widget.MixBottomBar", "SmartisanMixBottomBar"),
    ("button_group", "smartisanos.widget.ButtonGroup", "SmartisanButtonGroup"),
    ("button_group", "smartisanos.widget.ActionButtonGroup", "SmartisanActionButtonGroup"),
    ("slider", "smartisanos.widget.SmoothSeekBar", "SmartisanSmoothSeekBar"),
    ("slider", "smartisanos.widget.SliderWithIcons", "SmartisanSliderWithIcons"),
    ("progress", "smartisanos.widget.CircleProgressView", "SmartisanCircleProgressView"),
    ("progress", "smartisanos.widget.CircleProgressPopup", "SmartisanCircleProgressPopup"),
    ("progress", "smartisanos.widget.DownloadProgressView", "SmartisanDownloadProgressView"),
    ("snackbar", "smartisanos.widget.SnackbarWithButton", "SmartisanSnackbarWithButton"),
    ("snackbar", "smartisanos.widget.SnackbarWithDrawable", "SmartisanSnackbarWithDrawable"),
)


def sha256(path: Path) -> str | None:
    if not path.is_file():
        return None
    return hashlib.sha256(path.read_bytes()).hexdigest()


def scan_app(app: Path) -> set[str]:
    hits: set[str] = set()
    references = {
        source: (source, source.rsplit(".", 1)[-1])
        for _, source, _ in COMPONENTS
    }
    for root_name in ("sources", "resources"):
        root = app / root_name
        if not root.is_dir():
            continue
        for path in root.rglob("*"):
            if not path.is_file() or path.suffix not in {".java", ".xml"}:
                continue
            if path.name == "R.java" or path.name.startswith("R$"):
                continue
            try:
                content = path.read_text(encoding="utf-8", errors="ignore")
            except OSError:
                continue
            for source, (qualified, simple) in references.items():
                if path.name == f"{simple}.java":
                    continue
                if qualified in content or simple in content:
                    hits.add(source)
    return hits


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--apps", required=True, type=Path)
    parser.add_argument("--framework-sources", required=True, type=Path)
    parser.add_argument("--output", required=True, type=Path)
    args = parser.parse_args()

    apps = sorted(path for path in args.apps.iterdir() if path.is_dir())
    app_hits = {app.name: scan_app(app) for app in apps}
    result = []
    for family, source, public in COMPONENTS:
        names = [app.name for app in apps if source in app_hits[app.name]]
        source_path = args.framework_sources / Path(*source.split(".")).with_suffix(".java")
        result.append({
            "family": family,
            "romClass": source,
            "publicClass": f"org.opensmartisanos.ui.widget.{public}",
            "actualAppCount": len(names),
            "apps": names,
            "sourceSha256": sha256(source_path),
        })
    document = {
        "rom": "Smartisan OS 8.5.3 R2",
        "scanRules": [
            "scan decoded app Java and XML",
            "exclude the component's own ClassName.java",
            "exclude generated R.java and R$*.java",
        ],
        "decodedAppCount": len(apps),
        "components": result,
    }
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(document, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


if __name__ == "__main__":
    main()
