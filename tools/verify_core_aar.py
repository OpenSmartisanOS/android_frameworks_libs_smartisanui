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
    "org/opensmartisanos/ui/widget/SmartisanBottomBar.class",
    "org/opensmartisanos/ui/widget/SmartisanBottomBarItemView.class",
    "org/opensmartisanos/ui/widget/SmartisanTabSwitcher.class",
    "org/opensmartisanos/ui/widget/SmartisanIconBottomBar.class",
    "org/opensmartisanos/ui/widget/SmartisanMixBottomBar.class",
    "org/opensmartisanos/ui/widget/SmartisanButtonGroup.class",
    "org/opensmartisanos/ui/widget/SmartisanActionButtonGroup.class",
    "org/opensmartisanos/ui/widget/SmartisanSmoothSeekBar.class",
    "org/opensmartisanos/ui/widget/SmartisanSliderWithIcons.class",
    "org/opensmartisanos/ui/widget/SmartisanCircleProgressView.class",
    "org/opensmartisanos/ui/widget/SmartisanCircleProgressPopup.class",
    "org/opensmartisanos/ui/widget/SmartisanDownloadProgressView.class",
    "org/opensmartisanos/ui/widget/SmartisanSnackbarWithButton.class",
    "org/opensmartisanos/ui/widget/SmartisanSnackbarWithDrawable.class",
    "org/opensmartisanos/ui/app/SmartisanAlertDialog.class",
    "org/opensmartisanos/ui/app/SmartisanAlertDialog$Builder.class",
)
REQUIRED_RESOURCES = (
    "res/layout/smartisan_rom_abs_editor_layout.xml",
    "res/drawable/smartisan_rom_pwd_eye_open_close_anim.xml",
    "res/drawable-xxhdpi-v4/smartisan_rom_editor_bg_single.9.png",
    "res/layout/smartisan_rom_mix_bottom_bar.xml",
    "res/drawable/smartisan_rom_smartisan_bottom_tab_icon_scalable_selector.xml",
    "res/drawable-anydpi-v24/smartisan_rom_clock_tab_worldclock.xml",
    "res/color/smartisan_rom_clock_bottom_bar_icon.xml",
    "res/drawable/smartisan_rom_qsb_tab_t9_voice_selector.xml",
    "res/layout/smartisan_rom_button_group_layout.xml",
    "res/layout/smartisan_rom_slider_with_icons_layout.xml",
    "res/layout/smartisan_rom_snackbar_with_btn_layout.xml",
    "res/drawable/smartisan_alert_revone_root.xml",
    "res/drawable/smartisan_alert_revone_content.xml",
    "res/drawable/smartisan_alert_revone_button_right.xml",
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
