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
    "org/opensmartisanos/ui/app/SmartisanMenuDialog.class",
    "org/opensmartisanos/ui/widget/SmartisanDatePicker.class",
    "org/opensmartisanos/ui/widget/SmartisanDatePickerDialog.class",
    "org/opensmartisanos/ui/widget/SmartisanTimePicker.class",
    "org/opensmartisanos/ui/widget/SmartisanTimePickerDialog.class",
    "org/opensmartisanos/ui/widget/SmartisanDateTimePicker.class",
    "org/opensmartisanos/ui/widget/SmartisanDateTimePickerDialog.class",
    "org/opensmartisanos/ui/widget/SmartisanGridIconPopupMenu.class",
    "org/opensmartisanos/ui/widget/SmartisanSpinnerView.class",
    "org/opensmartisanos/ui/widget/SmartisanWheelTextView.class",
    "org/opensmartisanos/ui/widget/SmartisanListPopupMenu.class",
    "org/opensmartisanos/ui/widget/SmartisanListPopupMenuStandardAdapter.class",
    "org/opensmartisanos/ui/widget/SmartisanPopupMenuStandardListItem.class",
    "org/opensmartisanos/ui/widget/SmartisanPopupMenuLongPressedListItem.class",
    "org/opensmartisanos/ui/widget/SmartisanPopupMenuRemovableListItem.class",
    "org/opensmartisanos/ui/widget/SmartisanGroupMenuAdapter.class",
    "org/opensmartisanos/ui/widget/SmartisanBottomMenuAdapter.class",
    "org/opensmartisanos/ui/widget/SmartisanBottomMenuPopupWindow.class",
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
    "res/drawable/smartisan_rom_dialog_button_right.xml",
    "res/drawable-xxhdpi-v4/smartisan_rom_dialog_full.9.png",
    "res/drawable-xxhdpi-v4/smartisan_rom_time_picker_widget_lens.9.png",
    "res/drawable/smartisan_rom_menu_dialog_item_selector.xml",
    "res/layout/smartisan_rom_bottom_menu.xml",
    "res/layout-land/smartisan_rom_bottom_menu.xml",
    "res/layout/smartisan_rom_bottom_menu_item.xml",
    "res/layout-land/smartisan_rom_bottom_menu_item.xml",
    "res/layout/smartisan_rom_grid_menu.xml",
    "res/layout/smartisan_rom_grid_menu_page.xml",
    "res/layout/smartisan_rom_grid_menu_item.xml",
    "res/layout/smartisan_rom_popup_menu_standard_list_item.xml",
    "res/layout/smartisan_rom_popup_menu_long_pressed_list_item.xml",
    "res/layout/smartisan_rom_popup_menu_removable_list_item.xml",
    "res/drawable/smartisan_rom_menu_item_close.xml",
    "res/drawable/smartisan_rom_menu_list_group_selector.xml",
    "res/drawable/smartisan_rom_revone_menu_list_selector.xml",
    "res/drawable-xxhdpi-v4/smartisan_rom_popup_menu_item_divider.png",
    "res/drawable/smartisan_rom_selector_dropdown_arrow.xml",
    "res/drawable/smartisan_rom_selector_previous_arrow.xml",
    "res/drawable/smartisan_rom_selector_next_arrow.xml",
    "res/drawable-xxhdpi-v4/smartisan_rom_drop_down_arrow.png",
    "res/drawable-xxhdpi-v4/smartisan_rom_pre_page_arrow.png",
    "res/drawable-xxhdpi-v4/smartisan_rom_next_page_arrow.png",
    "res/drawable-xxhdpi-v4/smartisan_rom_action_menu_grid_bg.9.png",
    "res/drawable-xxhdpi-v4/smartisan_rom_grid_wide_bg.png",
    "res/anim/smartisan_bottom_menu_in.xml",
    "res/anim-land/smartisan_bottom_menu_in.xml",
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
