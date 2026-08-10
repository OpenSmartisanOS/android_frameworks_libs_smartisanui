#!/usr/bin/env python3
"""Host-side contract checks for the public-SDK dialog, picker and popup family."""

from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "core/src/main/java/org/opensmartisanos/ui"
RES = ROOT / "core/src/main/res"


def source(path: str) -> str:
    return (JAVA / path).read_text(encoding="utf-8")


def require(text: str, pattern: str, message: str) -> None:
    if re.search(pattern, text, re.MULTILINE | re.DOTALL) is None:
        raise AssertionError(message)


date = source("widget/SmartisanDatePicker.java")
date_ex = source("widget/SmartisanDatePickerEx.java")
time = source("widget/SmartisanTimePicker.java")
number = source("widget/SmartisanNumberPicker.java")
number_ex = source("widget/SmartisanNumberPickerEx.java")
date_time = source("widget/SmartisanDateTimePicker.java")
date_dialog = source("widget/SmartisanDatePickerDialog.java")
date_ex_dialog = source("widget/SmartisanDatePickerExDialog.java")
time_dialog = source("widget/SmartisanTimePickerDialog.java")
time_ex_dialog = source("widget/SmartisanTimePickerExDialog.java")
date_time_dialog = source("widget/SmartisanDateTimePickerDialog.java")
picker_dialog_support = source("widget/SmartisanPickerDialogSupport.java")
grid = source("widget/SmartisanGridIconPopupMenu.java")
paged = source("internal/RomPagedView.java")
bottom = source("widget/SmartisanBottomMenuPopupWindow.java")
group = source("widget/SmartisanGroupMenuAdapter.java")
list_popup = source("widget/SmartisanListPopupMenu.java")
standard_adapter = source("widget/SmartisanListPopupMenuStandardAdapter.java")
standard_item = source("widget/SmartisanPopupMenuStandardListItem.java")
long_pressed_item = source("widget/SmartisanPopupMenuLongPressedListItem.java")
removable_item = source("widget/SmartisanPopupMenuRemovableListItem.java")
spinner_view = source("widget/SmartisanSpinnerView.java")
wheel_text = source("widget/SmartisanWheelTextView.java")
alert = source("app/SmartisanAlertController.java")
alert_button_scale = source("app/SmartisanAlertButtonScaleHelper.java")
alert_dialog = source("app/SmartisanAlertDialog.java")
menu = source("app/SmartisanMenuDialog.java")
progress_dialog = source("app/SmartisanProgressDialog.java")
menu_adapter = source("app/SmartisanMenuDialogListAdapter.java")
menu_legacy_adapter = source("app/SmartisanMenuDialogMultiAdapter.java")
menu_title = source("widget/SmartisanDialogTitleBar.java")
pattern_app = source("widget/SmartisanDialogPatternAppInfoLayout.java")
pattern_section = source("widget/SmartisanDialogPatternSectionGroup.java")
pattern_choice = source("widget/SmartisanDialogPatternTwoLineSingleChoice.java")
catalog = (ROOT / "catalog/src/main/java/org/opensmartisanos/ui/catalog/CatalogActivity.java").read_text(
        encoding="utf-8")
styles = (RES / "values/styles.xml").read_text(encoding="utf-8")

for picker in (date, date_ex):
    require(picker, r"enum DatePickerType\s*\{\s*EVENT,\s*BIRTHDAY,\s*BIRTHDAY_LUNAR",
            "DatePickerType order changed")
    require(picker, r"DEFAULT_BIRTHDAY_START_YEAR\s*=\s*1800", "birthday range changed")
    require(picker, r"setDate\(4,\s*monthOfYear,\s*dayOfMonth\)", "unset-year value lost")
    require(picker, r"setMinDate\(", "minimum date API lost")
    require(picker, r"setMaxDate\(", "maximum date API lost")

require(time, r"DateFormat\.is24HourFormat\(context\)", "public 12/24-hour mapping lost")
require(number, r"SELECTOR_WHEEL_ITEM_COUNT\s*=\s*5", "standard five-row wheel changed")
require(number_ex, r"SELECTOR_WHEEL_ITEM_COUNT\s*=\s*9", "extended curved-wheel slots changed")
for wheel in (number, number_ex):
    require(wheel, r"setWrapSelectorWheel", "wheel looping API lost")
    require(wheel, r"ensureScrollWheelAdjusted", "wheel snapping behavior lost")
    require(wheel, r"fling\(", "wheel inertia behavior lost")
    require(wheel, r"SavedState", "wheel state restoration lost")
    require(wheel, r"onKeyDown\(int keyCode, KeyEvent event\)",
            "wheel DPAD navigation lost")
    require(wheel, r"KEYCODE_DPAD_UP.*?KEYCODE_DPAD_DOWN.*?stepValueFromKeyboard",
            "wheel DPAD step mapping changed")

require(date_time, r"onSaveInstanceState", "DateTime state saving lost")
require(date_time, r"onRestoreInstanceState", "DateTime state restoration lost")

# Picker dialogs do not share one approximate window policy in the ROM. Ordinary Date and
# extended Time are always bottom-aligned; ordinary Time, extended Date and DateTime use the
# external-display center variant. Ordinary Date also retains the API 26+ measured-height path.
for dialog in (date_dialog, time_ex_dialog):
    require(dialog, r"SmartisanPickerDialogSupport\.configure\(this, false\)",
            "always-bottom picker dialog changed")
for dialog in (date_ex_dialog, time_dialog, date_time_dialog):
    require(dialog, r"SmartisanPickerDialogSupport\.configure\(this, true\)",
            "external-display picker dialog changed")
require(date_dialog, r"fitDatePickerWindowAfterMeasure\(this, titleBar, datePicker,\s*"
                     r"R\.drawable\.smartisan_rom_time_picker_widget_bottom\)",
        "DatePicker API 26+ exact-height path lost")
require(picker_dialog_support,
        r"titleBar\.getMeasuredHeight\(\) \+ picker\.getMeasuredHeight\(\)\s*"
        r"\+ bottom\.getIntrinsicHeight\(\)",
        "DatePicker measured-height formula changed")
require(time_dialog,
        r"externalDisplay\s*\?\s*R\.layout\.smartisan_rom_revone_time_picker_dialog\s*"
        r":\s*R\.layout\.smartisan_rom_time_picker_dialog",
        "TimePicker external-display layout mapping lost")
for dialog in (date_ex_dialog, time_dialog, date_time_dialog):
    require(dialog, r"smartisan_rom_revone_dialog_bottom_bg",
            "external-display picker bottom resource lost")
require(date_time_dialog,
        r"calendar\.set\(2037, Calendar\.DECEMBER, 31, 0, 0, 0\)",
        "DateTime original maximum instant changed")
if "smartisan_rom_select_time" in time_dialog or "smartisan_rom_select_time" in time_ex_dialog:
    raise AssertionError("Time dialogs added a non-ROM default title")

picker_dialog_layouts = (
        "smartisan_rom_date_picker_dialog.xml",
        "smartisan_rom_date_picker_ex_dialog.xml",
        "smartisan_rom_time_picker_dialog.xml",
        "smartisan_rom_time_picker_ex_dialog.xml",
        "smartisan_rom_date_time_picker_dialog.xml",
        "smartisan_rom_revone_time_picker_dialog.xml",
)
for layout_name in picker_dialog_layouts:
    layout = (RES / f"layout/{layout_name}").read_text(encoding="utf-8")
    require(layout, r'smartisanShowDivider="false"',
            f"Picker title divider mapping changed: {layout_name}")
require((RES / "layout/smartisan_rom_revone_time_picker_dialog.xml").read_text(
        encoding="utf-8"), r'smartisan_rom_picker_container.*?SmartisanTimePicker.*?'
        r'smartisan_rom_time_picker_widget_bottom',
        "External TimePicker hierarchy changed")
if not (RES / "drawable-xxhdpi/smartisan_rom_revone_dialog_bottom_bg.9.png").is_file():
    raise AssertionError("External picker ROM bottom NinePatch is missing")
sw411_picker_values = (RES / "values-sw411dp/smartisan_rom_menu_dialog.xml").read_text(
        encoding="utf-8")
sw432_picker_values = (RES / "values-sw432dp/smartisan_rom_picker.xml").read_text(
        encoding="utf-8")
require(sw411_picker_values,
        r'name="smartisan_rom_time_picker_width">411\.43dp</dimen>',
        "R2 sw411 TimePicker full-width override lost")
require(sw432_picker_values,
        r'name="smartisan_rom_time_picker_width">432dp</dimen>',
        "R2 sw432 TimePicker full-width override lost")

# Catalog exposes the ROM contracts instead of masking them with demo-only ranges.
picker_catalog = catalog[catalog.index("private void addPickers("):
        catalog.index("private void addDialogPatterns(")]
for forbidden in ("TimeUnit.DAYS", ".setMinDate(", ".setMaxDate("):
    if forbidden in picker_catalog:
        raise AssertionError(f"Catalog overrides the picker ROM range: {forbidden}")
for required in (
        "SmartisanDatePicker.DatePickerType.EVENT",
        "SmartisanDatePicker.DatePickerType.BIRTHDAY",
        "SmartisanDatePicker.DatePickerType.BIRTHDAY_LUNAR",
        "SmartisanDatePickerEx.DatePickerType.EVENT",
        "SmartisanDatePickerEx.DatePickerType.BIRTHDAY",
        "SmartisanDatePickerEx.DatePickerType.BIRTHDAY_LUNAR",
        "now.get(Calendar.MINUTE), true",
        "now.get(Calendar.MINUTE), false",
        "now.getTimeInMillis()).show()",
):
    if required not in picker_catalog:
        raise AssertionError(f"Catalog lost picker coverage: {required}")
require(catalog, r"range\.setId\(R\.id\.catalog_java_spinner_range\)",
        "Catalog range spinner lost its stable resource ID")
require(group, r"getGroupId", "group sorting lost")
require(group, r"dividerMenuItem\s*=\s*menu\.add\(DIVIDER_MENU\)",
        "R2 group divider sentinel lost")
require(group, r"menuItems\.add\(tail, dividerMenuItem\)",
        "group divider insertion lost")
require(group, r"getItemId\(int position\).*?return position",
        "R2 group adapter position IDs lost")
require(group, r"R\.drawable\.smartisan_rom_popup_menu_item_divider",
        "group menu is not using the original divider asset")
require(list_popup, r"R\.drawable\.smartisan_rom_menu_list_group_selector",
        "group menu selector mapping lost")
require(list_popup, r"R\.drawable\.smartisan_rom_revone_menu_list_selector",
        "external-display popup selector mapping lost")
require(list_popup, r"R\.drawable\.smartisan_rom_revone_list_popup_menu_separator",
        "external-display popup divider mapping lost")

# Standard, removable and long-press options must inflate the original remapped hierarchy.
for item_source, layout_name in (
        (standard_item, "smartisan_rom_popup_menu_standard_list_item"),
        (long_pressed_item, "smartisan_rom_popup_menu_long_pressed_list_item"),
        (removable_item, "smartisan_rom_popup_menu_removable_list_item")):
    require(item_source, rf"inflate\(\s*R\.layout\.{layout_name}",
            f"popup option no longer inflates {layout_name}")
    for constructor in ("new ImageView(", "new TextView(", "new LinearLayout("):
        if constructor in item_source:
            raise AssertionError(
                    f"popup option hierarchy is being recreated in Java: {constructor}")
require(long_pressed_item, r"extends RelativeLayout",
        "long-press option parent differs from the original RelativeLayout")
require(standard_adapter, r"STYLE_SELECTED\s*=\s*1", "selected option style changed")
require(standard_adapter, r"STYLE_REMOVABLE\s*=\s*2", "removable option style changed")
require(standard_adapter, r"SmartisanLocaleUtils\.isExternalDisplay",
        "external-display hover behavior lost")

standard_item_layout = (RES / "layout/smartisan_rom_popup_menu_standard_list_item.xml").read_text(
        encoding="utf-8")
long_item_layout = (RES / "layout/smartisan_rom_popup_menu_long_pressed_list_item.xml").read_text(
        encoding="utf-8")
removable_item_layout = (RES / "layout/smartisan_rom_popup_menu_removable_list_item.xml").read_text(
        encoding="utf-8")
for exact_value in (
        'android:layout_width="44dp"', 'android:layout_width="27dp"',
        'android:layout_height="27dp"', 'android:layout_marginRight="6dp"',
        'android:textSize="15sp"', 'android:textSize="12sp"'):
    if exact_value not in standard_item_layout:
        raise AssertionError(f"standard popup option value changed: {exact_value}")
for exact_value in ('style="@style/smartisan_rom_StandardIconStyle"',
                    'android:textSize="14sp"'):
    if exact_value not in long_item_layout:
        raise AssertionError(f"long-press popup option value changed: {exact_value}")
for exact_value in ('android:layout_width="44dp"', 'android:layout_width="36dp"',
                    'android:src="@drawable/smartisan_rom_menu_item_close"'):
    if exact_value not in removable_item_layout:
        raise AssertionError(f"removable popup option value changed: {exact_value}")
for asset in (
        "drawable-xxhdpi/smartisan_rom_menu_group_list_item_pressed.png",
        "drawable-xxhdpi/smartisan_rom_menu_item_close_icon.png",
        "drawable-xxhdpi/smartisan_rom_menu_item_close_icon_pressed.png",
        "drawable-xxhdpi/smartisan_rom_popup_menu_item_divider.png",
        "drawable-xxhdpi/smartisan_rom_revone_list_popup_menu_pressed.9.png",
        "drawable-xxhdpi/smartisan_rom_revone_list_popup_menu_separator.png"):
    if not (RES / asset).is_file():
        raise AssertionError(f"popup option ROM asset is missing: {asset}")

anchor_catalog = catalog[catalog.index("private void addAnchorMenus("):
        catalog.index("private void addGridMenus(")]
if "android.R.layout.simple_list_item_1" in anchor_catalog:
    raise AssertionError("Catalog bypasses the Smartisan popup option adapter")
for required in (
        "SmartisanListPopupMenuStandardAdapter", "STYLE_REMOVABLE",
        "SmartisanGroupMenuAdapter", "getSubtitle()", "isSelected()"):
    if required not in anchor_catalog:
        raise AssertionError(f"Catalog lost popup option coverage: {required}")

# The Catalog selector is the original compound Smartisan spinner, not a platform Spinner skin.
require(spinner_view, r"extends RelativeLayout", "SmartisanSpinnerView parent changed")
for required in (
        "SPINNER_STYLE_NORMAL = 0", "SPINNER_STYLE_DROP = 1",
        "SPINNER_STYLE_RANGE = 2", "SmartisanWheelTextView",
        "smartisan_rom_selector_dropdown_arrow", "smartisan_rom_selector_previous_arrow",
        "smartisan_rom_selector_next_arrow", "setDropDownClickListener",
        "setRangeClickListener", "setRangeWheelTextChangeListener"):
    if required not in spinner_view:
        raise AssertionError(f"SmartisanSpinnerView reverse contract lost: {required}")
require(wheel_text, r"extends View", "SmartisanWheelTextView parent changed")
require(wheel_text, r"new Scroller\(getContext\(\), new DecelerateInterpolator\(1f\)\)",
        "wheel adjustment interpolator changed")
require(wheel_text, r"SELECTOR_ADJUSTMENT_DURATION_MILLIS\s*=\s*150",
        "wheel snap duration changed")
require(wheel_text, r"canvas\.drawText", "wheel is no longer self-drawn")
spinner_catalog = catalog[catalog.index("private void addSdkSpinnerControls("):
        catalog.index("private void addRow(")]
for forbidden in ("new SmartisanSpinner(", "simple_spinner_item",
                  "simple_spinner_dropdown_item"):
    if forbidden in spinner_catalog:
        raise AssertionError(f"Catalog still uses the platform Spinner approximation: {forbidden}")
for required in ("SmartisanSpinnerView", "SPINNER_STYLE_DROP", "SPINNER_STYLE_RANGE",
                 "showSpinnerPopup"):
    if required not in spinner_catalog:
        raise AssertionError(f"Catalog lost Smartisan selector coverage: {required}")
spinner_xml = (ROOT / "catalog/src/main/res/layout/catalog_sdk_spinner.xml").read_text(
        encoding="utf-8")
for required in ("SmartisanSpinnerView", 'smartisanSpinnerViewStyle="drop"'):
    if required not in spinner_xml:
        raise AssertionError(f"Catalog XML selector is not using the original control: {required}")
for asset in (
        "drawable-xxhdpi/smartisan_rom_drop_down_arrow.png",
        "drawable-xxhdpi/smartisan_rom_drop_down_arrow_pressed.png",
        "drawable-xxhdpi/smartisan_rom_pre_page_arrow.png",
        "drawable-xxhdpi/smartisan_rom_pre_page_arrow_pressed.png",
        "drawable-xxhdpi/smartisan_rom_next_page_arrow.png",
        "drawable-xxhdpi/smartisan_rom_next_page_arrow_pressed.png"):
    if not (RES / asset).is_file():
        raise AssertionError(f"Smartisan selector asset is missing: {asset}")
require(grid, r"MAX_ROWS\s*=\s*3", "grid page row count changed")
require(grid, r"setIcons\(ArrayList<Drawable>", "Drawable icon API lost")
require(grid, r"inflate\(\s*R\.layout\.smartisan_rom_grid_menu, null\)",
        "Grid menu no longer inflates the remapped ROM hierarchy")
require(grid, r"R\.dimen\.smartisan_rom_popup_grid_menu_default_width",
        "Grid menu original fixed width changed")
require(grid, r"iconSize\s*=\s*values\.size\(\)",
        "Grid menu no longer snapshots the original Drawable-list size")
require(grid, r"SmartisanMenuMath\.gridPageCount\(iconCount, columns\)",
        "Grid menu page calculation is not using the tested package logic")
require(bottom, r"showBrowserMenu\(View parent, Menu menu, int childHeight,\s*int popupWidth, int popupHeight",
        "BottomMenu parameter contract changed")
require(bottom, r"setWidth\(popupWidth\).*setHeight\(popupHeight\)",
        "BottomMenu width/height mapping changed")
require(bottom, r"inflate\(\s*R\.layout\.smartisan_rom_bottom_menu, null\)",
        "BottomMenu no longer inflates the remapped ROM hierarchy")
require(bottom, r"if \(item\.isVisible\(\)\) items\.add\(item\)",
        "BottomMenu visible-item filtering changed")
for forbidden_constructor in ("new LinearLayout(", "new GridView("):
    if forbidden_constructor in bottom:
        raise AssertionError(
                f"BottomMenu visual hierarchy is being recreated in Java: {forbidden_constructor}")

# Alert must bind the remapped ROM hierarchy instead of rebuilding a visual approximation in Java.
require(alert, r"setContentView\(R\.layout\.smartisan_rom_alert_dialog\)",
        "Alert no longer inflates the remapped ROM layout")
for layout_name in (
        "smartisan_rom_select_dialog", "smartisan_rom_select_dialog_item",
        "smartisan_rom_select_dialog_singlechoice",
        "smartisan_rom_select_dialog_multichoice"):
    if layout_name not in alert:
        raise AssertionError(f"Alert lost remapped layout binding: {layout_name}")
for background in ("full", "top", "top_no_divider", "middle", "bottom"):
    if f"smartisan_rom_dialog_{background}" not in alert:
        raise AssertionError(f"Alert panel background mapping lost: {background}")
for forbidden_constructor in (
        "new LinearLayout(context)", "new TextView(context)", "new Button(context)",
        "new ListView(context)", "new CheckedTextView(context)"):
    if forbidden_constructor in alert:
        raise AssertionError(f"Alert visual hierarchy is being recreated in Java: {forbidden_constructor}")

alert_layout = (RES / "layout/smartisan_rom_alert_dialog.xml").read_text(encoding="utf-8")
for exact_value in (
        'android:minHeight="96dp"', 'android:paddingStart="20dp"',
        'android:paddingTop="18dp"', 'android:paddingEnd="18dp"',
        'android:paddingBottom="18dp"', 'android:textSize="13.5dp"'):
    if exact_value not in alert_layout:
        raise AssertionError(f"Alert ROM layout value changed: {exact_value}")

select_list = (RES / "layout/smartisan_rom_select_dialog.xml").read_text(encoding="utf-8")
single_choice = (RES / "layout/smartisan_rom_select_dialog_singlechoice.xml").read_text(
        encoding="utf-8")
multi_choice = (RES / "layout/smartisan_rom_select_dialog_multichoice.xml").read_text(
        encoding="utf-8")
require(select_list, r'android:divider="@drawable/smartisan_rom_alert_list_divider"',
        "Alert list lost the ROM divider")
require(select_list, r'android:listSelector="@drawable/smartisan_rom_alert_list_selector"',
        "Alert list lost the ROM pressed/focused selector")
require(single_choice,
        r'android:checkMark="@drawable/smartisan_rom_alert_single_choice_selector"',
        "Alert single-choice row is not using its dedicated ROM selector")
require(multi_choice,
        r'android:checkMark="@drawable/smartisan_rom_alert_multi_choice_selector"',
        "Alert multi-choice row is not using its dedicated ROM selector")
for choice_layout in (single_choice, multi_choice):
    require(choice_layout, r'android:checkMarkTint="@null"',
            "Alert choice row lets the host theme tint the original ROM check mark")
for generic_selector in ("smartisan_rom_selector_radio_choice",
                         "smartisan_rom_selector_check_box"):
    if generic_selector in single_choice or generic_selector in multi_choice:
        raise AssertionError("Alert choice rows reused generic SDK control resources")
require(alert, r'class CheckedItemAdapter.*hasStableIds\(\).*return true',
        "Alert ordinary/single list stable IDs changed")
require(alert, r'setSelectionFromTop\(params\.checkedItem,\s*0\)',
        "Alert checked-item scroll positioning changed")
require(alert_dialog, r'new ContextThemeWrapper\(context,\s*resolvedTheme\)',
        "Alert Builder no longer exposes the themed inflation context")
require(alert, r'if \(params\.itemListener != null\).*?setOnItemClickListener.*?'
               r'else if \(params\.multiChoiceListener != null\)',
        "Alert installs a click/dismiss path when the caller supplied no list listener")
require(alert, r'params\.hasListContent\(\).*?LinearLayout\.LayoutParams.*?weight\s*=\s*0f',
        "Alert list/custom panel weight coupling changed")
for key_contract in (
        r'boolean onKeyDown\(int keyCode,.*?highlightedButton\.onKeyDown',
        r'boolean onKeyUp\(int keyCode,.*?highlightedButton\.onKeyUp',
        r'KEYCODE_ENTER', r'KEYCODE_NUMPAD_ENTER'):
    require(alert + alert_dialog, key_contract,
            "Alert Enter/numpad forwarding contract changed")

for xlarge_name, check_mark in (
        ("smartisan_rom_select_dialog_item.xml", None),
        ("smartisan_rom_select_dialog_singlechoice.xml",
         "smartisan_rom_alert_single_choice_selector"),
        ("smartisan_rom_select_dialog_multichoice.xml",
         "smartisan_rom_alert_multi_choice_selector")):
    xlarge = (RES / f"layout-xlarge/{xlarge_name}").read_text(encoding="utf-8")
    for exact_value in ('android:minHeight="64dp"', 'android:paddingStart="16dp"',
                        'android:paddingEnd="16dp"', 'android:textSize="15sp"'):
        if exact_value not in xlarge:
            raise AssertionError(f"Alert xlarge row value changed: {xlarge_name}: {exact_value}")
    if check_mark is not None and check_mark not in xlarge:
        raise AssertionError(f"Alert xlarge choice selector changed: {xlarge_name}")

# The ROM button bitmaps must be rendered without Material tint/elevation/state animation, then
# receive the original scale/alpha touch feedback through the package-private SDK helper.
if alert_layout.count('style="@style/Widget.SmartisanUi.AlertDialog.Button"') != 3:
    raise AssertionError("Alert action buttons are not all using the dedicated neutral style")
button_style = re.search(
        r'<style name="Widget\.SmartisanUi\.AlertDialog\.Button"[^>]*>(.*?)</style>',
        styles, re.MULTILINE | re.DOTALL)
if button_style is None:
    raise AssertionError("Alert action-button neutral style is missing")
for neutral_item in (
        r'<item name="android:backgroundTint">@null</item>',
        r'<item name="android:stateListAnimator">@null</item>',
        r'<item name="android:elevation">0dp</item>'):
    require(button_style.group(1), neutral_item,
            "Alert action-button style no longer neutralizes Material rendering")
require(alert, r'setOnTouchListener\(new SmartisanAlertButtonScaleHelper\(button\)\)',
        "Alert action buttons lost the original scale/alpha touch helper")
for helper_contract in (r'0\.99f', r'0\.9f', r'ACTION_DOWN', r'ACTION_UP', r'ACTION_CANCEL'):
    require(alert_button_scale, helper_contract,
            "Alert action-button scale/alpha feedback contract changed")

alert_choices = re.search(
        r'private void addAlertChoices\(LinearLayout parent\) \{(.*?)\n  \}',
        catalog, re.MULTILINE | re.DOTALL)
if alert_choices is None:
    raise AssertionError("Catalog Alert choice demo is missing")
if alert_choices.group(1).count("button(SmartisanButton.STYLE_NORMAL") != 2 \
        or "STYLE_HIGHLIGHT" in alert_choices.group(1):
    raise AssertionError("Catalog single/multi-choice launch buttons are not consistently neutral")

# Custom content follows the original AlertController contract: resource IDs are inflated only
# after the custom panel exists, and text-editor detection uses the actual inflated hierarchy.
require(alert_dialog, r'setView\(int layoutResourceId\).*customViewLayoutResId\s*=\s*layoutResourceId',
        "Alert layout-resource custom View is still inflated eagerly")
require(alert, r'customViewLayoutResId,\s*customContainer,\s*false',
        "Alert custom layout is not inflated against the ROM custom container")
require(alert, r'addView\(customView, new FrameLayout\.LayoutParams\(\s*'
               r'ViewGroup\.LayoutParams\.MATCH_PARENT,\s*'
               r'ViewGroup\.LayoutParams\.MATCH_PARENT\)',
        "Alert custom View layout parameters differ from the original controller")
require(alert, r'canTextInput\(customView\)',
        "Alert input-method detection does not inspect the inflated custom View")
if re.search(r'setMessage\(CharSequence message\)\s*\{\s*params\.clear', alert_dialog):
    raise AssertionError("Alert setMessage still clears independent custom/list content")
for set_view in re.findall(r'public Builder setView\([^}]+\}', alert_dialog, re.DOTALL):
    if "clearContent" in set_view or "clearListContent" in set_view:
        raise AssertionError("Alert setView still clears independent message/list content")
require(alert_dialog, r'setItems\(CharSequence\[\] items.*?params\.clearListContent\(\)',
        "Alert list overloads no longer isolate their list-specific state")

# MenuDialog follows the original dedicated layout/controller path. Choice lists remain Alert
# semantics and must not be used by Catalog to approximate the menu-dialog rows.
require(menu,
        r'setContentView\(externalDisplay\s*\?\s*'
        r'R\.layout\.smartisan_rom_revone_menu_dialog\s*:\s*'
        r'R\.layout\.smartisan_rom_menu_dialog\)',
        "MenuDialog no longer inflates the remapped ROM layouts")
for forbidden_constructor in ("new LinearLayout(context)", "new ListView(context)"):
    if forbidden_constructor in menu:
        raise AssertionError(
                f"MenuDialog visual hierarchy is being recreated in Java: {forbidden_constructor}")
require(menu, r'FLAG_WATCH_OUTSIDE_TOUCH\s*\|\s*'
              r'WindowManager\.LayoutParams\.FLAG_ALT_FOCUSABLE_IM',
        "MenuDialog original public window flags changed")
require(menu, r'@interface DialogLocation\s*\{\s*int from\(\) default LOCATION_APP_BOTTOM;\s*'
              r'int to\(\) default LOCATION_DISPLAY_CENTER;',
        "MenuDialog DialogLocation bounds contract changed")
require(menu, r'public void onApplyNavigationBarStatusChange\(boolean shown\)',
        "MenuDialog public navigation-bar callback contract changed")
require(menu, r'adapter\.getCount\(\)\s*>=\s*5.*?'
              r'smartisan_rom_menu_dialog_long_list_height',
        "MenuDialog five-item long-list threshold changed")
require(menu, r'setAdaper\(SmartisanMenuDialogMultiAdapter.*?'
              r'smartisan_rom_menu_dialog_legacy_list_height.*?'
              r'smartisan_rom_menu_dialog_multi_list_bg',
        "MenuDialog legacy adapter geometry changed")
require(menu, r'bothVisible.*?buttonParams\.topMargin\s*=\s*bothVisible\s*\?\s*0\s*:\s*'
              r'buttonMarginView.*?listBottomPadding\s*=\s*bothVisible\s*\?\s*'
              r'buttonMarginView\s*:\s*buttonMarginEdge',
        "MenuDialog list/button margin coupling changed")
require(menu, r'DialogCallbackOrder\.dismissThenRun\(\s*this::dismiss,\s*'
              r'listener\s*==\s*null\s*\?\s*null\s*:',
        "MenuDialog must dismiss before an optional positive callback")
require(menu_adapter, r'smartisan_rom_menu_dialog_list_item',
        "MenuDialog action adapter lost the original row layout")
require(menu_adapter, r'dialog\.dismiss\(\).*?popupWindow\.dismiss\(\).*?'
                      r'listeners\.get\(position\)\.onClick',
        "MenuDialog action callback order changed")
for resource in (
        "smartisan_rom_menu_dialog_list_multi_item",
        "smartisan_rom_menu_dialog_last_item_selector",
        "smartisan_rom_recent_call_multi_item_selector",
        "smartisan_rom_menu_dialog_multi_item_selector"):
    if resource not in menu_legacy_adapter:
        raise AssertionError(f"MenuDialog legacy adapter lost ROM binding: {resource}")
require(menu_title, r'smartisan_rom_menu_dialog_title_bar',
        "MenuDialog title bar no longer inflates the original hierarchy")
require(menu_title, r'titleView\.setTextSize\(android\.util\.TypedValue\.COMPLEX_UNIT_SP,\s*'
                    r'titleTextSizeSp\)',
        "MenuDialog title-size restoration changed")
require(menu_title, r'shadow\.addRule\(RelativeLayout\.ALIGN_PARENT_TOP\).*?'
                    r'divider\.addRule\(RelativeLayout\.ALIGN_PARENT_TOP\).*?'
                    r'shadowView\.setTranslationY\(view\.getMeasuredHeight\(\)\).*?'
                    r'dividerView\.setTranslationY\(view\.getMeasuredHeight\(\)\)',
        "MenuDialog title overlays can expand the wrap-content title bar")
require(menu_title, r'pressed\s*\?\s*1\.33f\s*:\s*1f.*?'
                    r'view\.animate\(\)\.scaleX\(scale\)\.scaleY\(scale\)'
                    r'\.setDuration\(200L\)',
        "MenuDialog title icon press animation changed")
if "SpringAnimation" in menu_title or "SmartisanBarsHelper" in menu_title:
    raise AssertionError("MenuDialog title icon is not using the original 200ms animator")

menu_layout = (RES / "layout/smartisan_rom_menu_dialog.xml").read_text(encoding="utf-8")
revone_menu_layout = (RES / "layout/smartisan_rom_revone_menu_dialog.xml").read_text(
        encoding="utf-8")
menu_title_layout = (RES / "layout/smartisan_rom_menu_dialog_title_bar.xml").read_text(
        encoding="utf-8")
menu_item_selector = (RES / "drawable/smartisan_rom_menu_dialog_item_selector.xml").read_text(
        encoding="utf-8")
for exact_value in (
        'android:background="#f5f5f5f5"', 'android:paddingTop="24dp"',
        'android:paddingBottom="24dp"', 'android:dividerHeight="18dp"',
        'android:visibility="gone"'):
    if exact_value not in menu_layout:
        raise AssertionError(f"MenuDialog ROM layout value changed: {exact_value}")
for exact_value in (
        'android:paddingTop="6dp"', 'android:paddingBottom="6dp"',
        'android:layout_marginTop="18dp"',
        'android:background="@drawable/smartisan_rom_revone_dialog_bg_main"'):
    if exact_value not in revone_menu_layout:
        raise AssertionError(f"External MenuDialog ROM layout value changed: {exact_value}")
for exact_value in (
        'android:minHeight="48dp"', 'android:textSize="13.5sp"',
        '@drawable/smartisan_rom_standard_icon_cancel_selector'):
    if exact_value not in menu_title_layout:
        raise AssertionError(f"MenuDialog title-bar value changed: {exact_value}")
require(menu_item_selector,
        r'state_enabled="false".*?state_pressed="true".*?'
        r'<item android:drawable="@drawable/smartisan_rom_menu_dialog_item"',
        "MenuDialog item selector state order changed")
if "state_focused" in menu_item_selector:
    raise AssertionError("MenuDialog item selector added a non-ROM persistent focus state")
if "SmartisanMenuDialogListAdapter" not in catalog:
    raise AssertionError("Catalog is not exercising the original MenuDialog adapter")
for forbidden_helper in (
        "SmartisanMenuDialog.singleChoiceAdapter",
        "SmartisanMenuDialog.multiChoiceAdapter"):
    if forbidden_helper in catalog:
        raise AssertionError("Catalog is presenting Alert choice semantics as MenuDialog")

require(progress_dialog,
        r'setFlags\(WindowManager\.LayoutParams\.FLAG_ALT_FOCUSABLE_IM,\s*'
        r'WindowManager\.LayoutParams\.FLAG_ALT_FOCUSABLE_IM\)',
        "ProgressDialog input-method window flag changed")
require(progress_dialog, r'@Override public void onStart\(\)',
        "ProgressDialog narrowed the original public onStart contract")

# Dialog content patterns retain the original classes' small controller surface and inflate the
# original resource hierarchy. They must not drift into Java-built lookalikes.
for pattern, layout_name in (
        (pattern_app, "smartisan_rom_dialog_pattern_app_info"),
        (pattern_section, "smartisan_rom_dialog_pattern_section_group"),
        (pattern_choice, "smartisan_rom_dialog_pattern_two_line_single_choice")):
    require(pattern, rf"inflate\(.*?R\.layout\.{layout_name}",
            f"Dialog content pattern no longer inflates {layout_name}")
    for forbidden_constructor in ("new ImageView(", "new TextView(", "new LinearLayout(",
                                  "new RelativeLayout("):
        if forbidden_constructor in pattern:
            raise AssertionError(
                    f"Dialog content pattern is being recreated in Java: {forbidden_constructor}")

for pattern in (pattern_app, pattern_section):
    require(pattern, r"obtainStyledAttributes\(attrs,\s*R\.styleable\.",
            "Dialog content pattern lost XML attribute support")
    require(pattern, r"values\.getString\(",
            "Dialog content pattern attribute access differs from the original String contract")
for pattern in (pattern_app, pattern_section, pattern_choice):
    require(pattern, r"getResources\(\)\.getString\(resourceId\)",
            "Dialog content pattern resource setter differs from the original String contract")
require(pattern_section, r"TextUtils\.isEmpty\(value\)\s*\?\s*View\.GONE\s*:\s*View\.VISIBLE",
        "SectionGroup empty-field visibility behavior changed")
require(pattern_choice, r"TextUtils\.isEmpty\(summary\)\s*\?\s*View\.GONE\s*:\s*View\.VISIBLE",
        "Two-line choice empty-summary behavior changed")
require(pattern_choice, r"checked\s*\?\s*View\.VISIBLE\s*:\s*View\.INVISIBLE",
        "Two-line choice checked-state visibility changed")

app_pattern_layout = (RES / "layout/smartisan_rom_dialog_pattern_app_info.xml").read_text(
        encoding="utf-8")
section_pattern_layout = (RES / "layout/smartisan_rom_dialog_pattern_section_group.xml").read_text(
        encoding="utf-8")
choice_pattern_layout = (
        RES / "layout/smartisan_rom_dialog_pattern_two_line_single_choice.xml").read_text(
                encoding="utf-8")
pattern_values = (RES / "values/smartisan_rom_dialog_patterns.xml").read_text(
        encoding="utf-8")
choice_summary = (RES / "color/smartisan_rom_dialog_single_choice_summary.xml").read_text(
        encoding="utf-8")
for exact_value in (
        'android:layout_width="36dp"', 'android:layout_height="36dp"',
        'android:layout_marginRight="12dp"', 'android:duplicateParentState="true"',
        'android:layout_weight="1"', 'android:textStyle="normal"'):
    if exact_value not in app_pattern_layout:
        raise AssertionError(f"App-info dialog pattern value changed: {exact_value}")
for style_name in (
        "Widget.SmartisanUi.DialogPattern.SectionTitle",
        "Widget.SmartisanUi.DialogPattern.SectionSubtitle",
        "Widget.SmartisanUi.DialogPattern.SectionMessage"):
    if style_name not in section_pattern_layout:
        raise AssertionError(f"SectionGroup lost original style mapping: {style_name}")
for exact_value in (
        'android:layout_gravity="center_vertical"',
        'android:duplicateParentState="true"',
        'android:minHeight="?android:listPreferredItemHeightSmall"',
        'android:paddingStart="20dp"', 'android:paddingEnd="6dp"',
        'android:layout_toLeftOf="@id/smartisan_rom_item_check"',
        'android:layout_alignParentRight="true"',
        'android:textSize="12.5sp"',
        'android:textColor="@color/smartisan_rom_dialog_single_choice_summary"',
        'android:src="@drawable/smartisan_rom_dialog_pattern_choice_selected"'):
    if exact_value not in choice_pattern_layout:
        raise AssertionError(f"Two-line choice dialog pattern value changed: {exact_value}")
for exact_value in (
        '>20dp</dimen>', '>18dp</dimen>', '>60dp</dimen>',
        '>#66000000</color>', '>#4cffffff</color>',
        '<item name="android:textSize">15sp</item>',
        '<item name="android:textSize">12.5sp</item>',
        '<item name="android:textColor">#9a000000</item>',
        '<item name="android:paddingLeft">@dimen/smartisan_rom_dialog_pattern_padding_start</item>',
        '<item name="android:paddingRight">@dimen/smartisan_rom_dialog_pattern_padding_end</item>'):
    if exact_value not in pattern_values:
        raise AssertionError(f"Dialog content pattern value mapping changed: {exact_value}")
require(choice_summary,
        r'state_pressed="true".*?smartisan_rom_dialog_single_choice_summary_pressed_text.*?'
        r'smartisan_rom_dialog_pattern_secondary_text',
        "Two-line choice pressed/default summary color order changed")
choice_indicator = (
        RES / "drawable/smartisan_rom_dialog_pattern_choice_selected.xml").read_text(
                encoding="utf-8")
require(choice_indicator,
        r'state_enabled="true".*?state_pressed="true".*?'
        r'smartisan_rom_alert_choice_selected_on_pressed.*?'
        r'state_enabled="false".*?smartisan_rom_alert_choice_selected_on_disabled.*?'
        r'smartisan_rom_alert_choice_selected_on',
        "Two-line choice selected-marker state order changed")
if "state_checked" in choice_indicator or "smartisan_rom_selector_radio_choice" in choice_pattern_layout:
    raise AssertionError("Two-line choice reused the generic RadioButton state contract")
for public_pattern in (
        "SmartisanDialogPatternAppInfoLayout", "SmartisanDialogPatternSectionGroup",
        "SmartisanDialogPatternTwoLineSingleChoice"):
    if public_pattern not in catalog:
        raise AssertionError(f"Catalog is not exercising dialog content pattern: {public_pattern}")

input_layout = (RES / "layout/smartisan_rom_dialog_pattern_edit_text.xml").read_text(
        encoding="utf-8")
input_styles = (RES / "values/smartisan_rom_dialog_pattern_input.xml").read_text(
        encoding="utf-8")
for exact_value in (
        'android:paddingTop="@dimen/smartisan_rom_dialog_pattern_section_vertical_space"',
        'android:paddingBottom="@dimen/smartisan_rom_dialog_pattern_section_vertical_space"',
        'style="@style/Widget.SmartisanUi.DialogPattern.EditText"',
        'org.opensmartisanos.ui.widget.SmartisanTipsBar'):
    if exact_value not in input_layout:
        raise AssertionError(f"Dialog-pattern input layout changed: {exact_value}")
for exact_value in (
        '<item name="android:textSize">15sp</item>',
        '<item name="android:textColor">#cc000000</item>',
        '<item name="android:textColorHint">#26000000</item>',
        '@drawable/smartisan_rom_dialog_pattern_edit_text_bg',
        '>18dp</dimen>', '>10dp</dimen>'):
    if exact_value not in input_styles:
        raise AssertionError(f"DialogPatternEditText value changed: {exact_value}")
if not (RES / "drawable-xxhdpi/smartisan_rom_dialog_pattern_edit_text_bg.9.png").is_file():
    raise AssertionError("DialogPatternEditText lost its direct ROM NinePatch")

enter = (RES / "anim/smartisan_bottom_menu_in.xml").read_text(encoding="utf-8")
exit_ = (RES / "anim/smartisan_bottom_menu_out.xml").read_text(encoding="utf-8")
require(enter, r'duration="150"', "BottomMenu entry duration changed")
require(exit_, r'duration="100"', "BottomMenu exit duration changed")

bottom_portrait = (RES / "layout/smartisan_rom_bottom_menu.xml").read_text(encoding="utf-8")
bottom_landscape = (RES / "layout-land/smartisan_rom_bottom_menu.xml").read_text(
        encoding="utf-8")
bottom_item = (RES / "layout/smartisan_rom_bottom_menu_item.xml").read_text(encoding="utf-8")
bottom_land_item = (RES / "layout-land/smartisan_rom_bottom_menu_item.xml").read_text(
        encoding="utf-8")
grid_layout = (RES / "layout/smartisan_rom_grid_menu.xml").read_text(encoding="utf-8")
grid_page = (RES / "layout/smartisan_rom_grid_menu_page.xml").read_text(encoding="utf-8")
for value in ('android:layout_gravity="bottom"',
              'android:id="@id/smartisan_rom_bottom_menu_title_bar"'):
    if value not in bottom_portrait:
        raise AssertionError(f"BottomMenu portrait hierarchy changed: {value}")
for value in ('android:layout_width="270dp"', 'android:layout_gravity="end"'):
    if value not in bottom_landscape:
        raise AssertionError(f"BottomMenu landscape hierarchy changed: {value}")
if 'android:layout_height="74dp"' not in bottom_item:
    raise AssertionError("BottomMenu portrait item height changed")
if 'android:layout_height="90dp"' not in bottom_land_item:
    raise AssertionError("BottomMenu landscape item height changed")
for value in ('android:paddingTop="18dp"',
              'android:layout_marginTop="6dp"'):
    if value not in grid_layout:
        raise AssertionError(f"Grid menu hierarchy changed: {value}")
for value in ('android:listSelector="@android:color/transparent"',
              'android:verticalSpacing="6dp"'):
    if value not in grid_page:
        raise AssertionError(f"Grid menu page geometry changed: {value}")
require(grid, r'grid\.setOnItemClickListener',
        "Grid menu lost its original item callback")
require(grid, r'pages\.setOnPageChangeListener',
        "Grid indicator is not synchronized with the public-SDK pager")
for pager_contract in (
        "onInterceptTouchEvent", "settleAfterDrag", "getScaledPagingTouchSlop",
        "onKeyDown", "performAccessibilityAction", "LAYOUT_DIRECTION_RTL",
        "onSaveInstanceState", "onRestoreInstanceState"):
    if pager_contract not in paged:
        raise AssertionError(f"Public-SDK grid pager lost {pager_contract}")
require(spinner_view, r'removeStyleViews\(\).*?setTitleStyle\(\)',
        "Spinner style changes do not remove obsolete accessory views")
require(spinner_view, r'if \(textPicker == null\).*?new SmartisanWheelTextView',
        "Spinner recreates its text picker on every style change")

for approximate in (
        "drawable/smartisan_rom_bottom_menu_surface.xml",
        "drawable/smartisan_rom_grid_menu_surface.xml",
        "drawable/smartisan_rom_grid_menu_item_selector.xml"):
    if (RES / approximate).exists():
        raise AssertionError(f"Handwritten approximate popup resource remains: {approximate}")

api = (ROOT / "core/api/current.txt").read_text(encoding="utf-8")
require(api, r"API_VERSION = 3", "API level is not 3")
for public_type in (
        "SmartisanNumberPicker", "SmartisanNumberPickerEx", "SmartisanNumberPickerExtended",
        "SmartisanDatePickerExDialog", "SmartisanTimePickerExDialog",
        "SmartisanGroupMenuAdapter", "SmartisanDialogPatternAppInfoLayout"):
    if public_type not in api:
        raise AssertionError(f"Missing public API baseline entry: {public_type}")

print("Dialog, picker and popup host contract checks passed.")
