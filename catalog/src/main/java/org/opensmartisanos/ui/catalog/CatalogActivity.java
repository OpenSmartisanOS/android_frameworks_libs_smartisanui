/*
 * Copyright (C) 2026 The OpenSmartisanOS Project
 * Licensed under the Apache License, Version 2.0.
 */
package org.opensmartisanos.ui.catalog;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.opensmartisanos.ui.widget.SmartisanButton;
import org.opensmartisanos.ui.widget.SmartisanListContentItem;
import org.opensmartisanos.ui.widget.SmartisanListContentItemCheck;
import org.opensmartisanos.ui.widget.SmartisanListContentItemSwitch;
import org.opensmartisanos.ui.widget.SmartisanListContentItemText;
import org.opensmartisanos.ui.widget.SmartisanShadowButton;
import org.opensmartisanos.ui.widget.SmartisanSettingItemCheck;
import org.opensmartisanos.ui.widget.SmartisanSettingItemSwitch;
import org.opensmartisanos.ui.widget.SmartisanSettingItemText;
import org.opensmartisanos.ui.widget.SmartisanSwitch;

public final class CatalogActivity extends Activity {
    private final java.util.List<SmartisanSwitch> switchSamples = new java.util.ArrayList<>();
    private boolean darkSwitchStyle;
    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(20), dp(16), dp(32));
        content.setBackgroundColor(getResources().getColor(
                org.opensmartisanos.ui.R.color.smartisan_catalog_background));
        scroll.addView(content, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        addHeading(content, R.string.smartisan_catalog_heading, 22);
        addSection(content, R.string.smartisan_catalog_smartisan_button);
        addSmartisanButtons(content);

        addSection(content, R.string.smartisan_catalog_shadow_button);
        addShadowButtons(content);

        addSection(content, R.string.smartisan_catalog_switch);
        addSwitchRow(content, false, false);
        addSwitchRow(content, true, false);
        addSwitchRow(content, false, true);
        addSwitchStyleControl(content);

        addSection(content, R.string.smartisan_catalog_list_item);
        addListItems(content);

        addSection(content, R.string.smartisan_catalog_setting_item);
        addSettingItems(content);

        setContentView(scroll);
    }

    private void addHeading(LinearLayout parent, int text, int textSize) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(Color.rgb(51, 51, 51));
        view.setTextSize(textSize);
        view.setGravity(Gravity.CENTER_VERTICAL);
        parent.addView(view, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));
    }

    private void addSection(LinearLayout parent, int text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(Color.rgb(102, 102, 102));
        view.setTextSize(14);
        view.setGravity(Gravity.BOTTOM);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(52));
        params.bottomMargin = dp(10);
        parent.addView(view, params);
    }

    private LinearLayout newRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        return row;
    }

    private void addButtonRow(LinearLayout parent, int leftStyle, int leftText,
            int rightStyle, int rightText) {
        LinearLayout row = newRow();
        SmartisanButton left = button(leftStyle, leftText);
        SmartisanButton right = button(rightStyle, rightText);
        row.addView(left);
        LinearLayout.LayoutParams rightParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rightParams.leftMargin = dp(12);
        row.addView(right, rightParams);
        addRow(parent, row);
    }

    private void addSmartisanButtons(LinearLayout parent) {
        addButtonRow(parent, SmartisanButton.STYLE_NORMAL, R.string.smartisan_catalog_normal,
                SmartisanButton.STYLE_BACK, R.string.smartisan_catalog_back);
        addButtonRow(parent, SmartisanButton.STYLE_HIGHLIGHT_BLUE,
                R.string.smartisan_catalog_confirm, SmartisanButton.STYLE_HIGHLIGHT_RED,
                R.string.smartisan_catalog_delete);
        addIconButtonRow(parent);

        LinearLayout iconRow = newRow();
        SmartisanButton iconButton = new SmartisanButton(this);
        iconButton.setButtonStyle(SmartisanButton.STYLE_NORMAL_WITH_ICON);
        iconButton.setButtonSourceBitmap(
                org.opensmartisanos.ui.R.drawable.smartisan_rom_icon_setting_btn_normal);
        iconRow.addView(iconButton, new LinearLayout.LayoutParams(dp(60), dp(48)));
        SmartisanButton iconArea = new SmartisanButton(this);
        iconArea.setButtonStyle(SmartisanButton.STYLE_AREA_ONLY_ICON);
        iconArea.setButtonSourceBitmap(
                org.opensmartisanos.ui.R.drawable.smartisan_rom_icon_search_btn_normal);
        LinearLayout.LayoutParams iconAreaParams = new LinearLayout.LayoutParams(dp(40), dp(48));
        iconAreaParams.leftMargin = dp(12);
        iconRow.addView(iconArea, iconAreaParams);
        SmartisanButton custom = new SmartisanButton(this);
        custom.setButtonStyle(SmartisanButton.STYLE_CUSTOM_BACKGROUND);
        custom.setBackgroundResource(
                org.opensmartisanos.ui.R.drawable.smartisan_rom_selector_small_btn_standard);
        LinearLayout.LayoutParams customParams = new LinearLayout.LayoutParams(dp(66), dp(48));
        customParams.leftMargin = dp(12);
        iconRow.addView(custom, customParams);
        addRow(parent, iconRow);
        addDisabledButtonRow(parent);
    }

    private SmartisanButton button(int style, int text) {
        SmartisanButton button = new SmartisanButton(this);
        button.setButtonStyle(style);
        button.setButtonText(text);
        return button;
    }

    private void addIconButtonRow(LinearLayout parent) {
        LinearLayout row = newRow();
        int[] styles = {SmartisanButton.STYLE_SETTING, SmartisanButton.STYLE_DELETE,
                SmartisanButton.STYLE_SEARCH};
        for (int style : styles) {
            SmartisanButton button = new SmartisanButton(this);
            button.setButtonStyle(style);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(60), dp(48));
            params.rightMargin = dp(8);
            row.addView(button, params);
        }
        addRow(parent, row);
    }

    private void addDisabledButtonRow(LinearLayout parent) {
        LinearLayout row = newRow();
        SmartisanButton normal = button(SmartisanButton.STYLE_NORMAL,
                R.string.smartisan_catalog_disabled);
        normal.setEnabled(false);
        normal.setEnabledStyle(false);
        SmartisanButton highlight = button(SmartisanButton.STYLE_HIGHLIGHT_BLUE,
                R.string.smartisan_catalog_disabled);
        highlight.setEnabled(false);
        highlight.setEnabledStyle(false);
        row.addView(normal);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = dp(12);
        row.addView(highlight, params);
        addRow(parent, row);
    }

    private void addLongButtons(LinearLayout parent, boolean shrink) {
        LinearLayout row = newRow();
        int style = shrink ? org.opensmartisanos.ui.R.style.Widget_SmartisanUi_ShadowButton_Shrink
                : org.opensmartisanos.ui.R.style.Widget_SmartisanUi_ShadowButton_Long;
        SmartisanShadowButton primary = new SmartisanShadowButton(this, null, 0, style);
        primary.setText(shrink ? R.string.smartisan_catalog_shrink : R.string.smartisan_catalog_long);
        primary.updateBackgroundStyle(SmartisanShadowButton.LongButtonStyle.HIGH_LIGHT);
        SmartisanShadowButton destructive = new SmartisanShadowButton(this, null, 0, style);
        destructive.setText(R.string.smartisan_catalog_delete);
        destructive.updateBackgroundStyle(SmartisanShadowButton.LongButtonStyle.RED);
        LinearLayout.LayoutParams item = new LinearLayout.LayoutParams(0, dp(shrink ? 48 : 60), 1f);
        item.leftMargin = dp(4);
        item.rightMargin = dp(4);
        row.addView(primary, new LinearLayout.LayoutParams(item));
        row.addView(destructive, new LinearLayout.LayoutParams(item));
        addRow(parent, row);
    }

    private void addShadowButtons(LinearLayout parent) {
        addLongButtons(parent, false);
        addLongButtons(parent, true);
        addLongButtonPair(parent, SmartisanShadowButton.LongButtonStyle.WHITE,
                R.string.smartisan_catalog_white,
                SmartisanShadowButton.LongButtonStyle.GRAY, R.string.smartisan_catalog_gray);
        LinearLayout row = newRow();
        int style = org.opensmartisanos.ui.R.style.Widget_SmartisanUi_ShadowButton_Small;
        SmartisanShadowButton standard = new SmartisanShadowButton(this, null, 0, style);
        standard.setText(R.string.smartisan_catalog_normal);
        standard.updateBackgroundStyle(SmartisanShadowButton.SmallButtonStyle.STANDARD);
        SmartisanShadowButton blue = new SmartisanShadowButton(this, null, 0, style);
        blue.setText(R.string.smartisan_catalog_confirm);
        blue.updateBackgroundStyle(SmartisanShadowButton.SmallButtonStyle.HIGH_LIGHT);
        SmartisanShadowButton red = new SmartisanShadowButton(this, null, 0, style);
        red.setText(R.string.smartisan_catalog_delete);
        red.updateBackgroundStyle(SmartisanShadowButton.SmallButtonStyle.RED);
        row.addView(standard);
        row.addView(blue, spacedWrapParams());
        row.addView(red, spacedWrapParams());
        addRow(parent, row);
    }

    private void addLongButtonPair(LinearLayout parent,
            SmartisanShadowButton.LongButtonStyle leftStyle, int leftText,
            SmartisanShadowButton.LongButtonStyle rightStyle, int rightText) {
        LinearLayout row = newRow();
        int style = org.opensmartisanos.ui.R.style.Widget_SmartisanUi_ShadowButton_Shrink;
        SmartisanShadowButton left = new SmartisanShadowButton(this, null, 0, style);
        left.setText(leftText);
        left.updateBackgroundStyle(leftStyle);
        SmartisanShadowButton right = new SmartisanShadowButton(this, null, 0, style);
        right.setText(rightText);
        right.updateBackgroundStyle(rightStyle);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(48), 1f);
        params.leftMargin = dp(4);
        params.rightMargin = dp(4);
        row.addView(left, new LinearLayout.LayoutParams(params));
        row.addView(right, new LinearLayout.LayoutParams(params));
        addRow(parent, row);
    }

    private LinearLayout.LayoutParams spacedWrapParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = dp(8);
        return params;
    }

    private void addSwitchRow(LinearLayout parent, boolean checked, boolean disabled) {
        LinearLayout row = newRow();
        TextView label = new TextView(this);
        label.setText(disabled ? R.string.smartisan_catalog_disabled
                : checked ? R.string.smartisan_catalog_checked : R.string.smartisan_catalog_unchecked);
        label.setTextColor(Color.rgb(51, 51, 51));
        label.setTextSize(16);
        row.addView(label, new LinearLayout.LayoutParams(0, dp(52), 1f));
        SmartisanSwitch smartisanSwitch = new SmartisanSwitch(this);
        smartisanSwitch.setChecked(checked);
        smartisanSwitch.setEnabled(!disabled);
        switchSamples.add(smartisanSwitch);
        row.addView(smartisanSwitch);
        addRow(parent, row);
    }

    private void addSwitchStyleControl(LinearLayout parent) {
        SmartisanButton toggle = button(SmartisanButton.STYLE_NORMAL,
                R.string.smartisan_catalog_dark_switch);
        toggle.setOnClickListener(view -> {
            darkSwitchStyle = !darkSwitchStyle;
            for (SmartisanSwitch sample : switchSamples) {
                sample.setSwitchDrawableStyle(darkSwitchStyle
                        ? SmartisanSwitch.STYLE_DARK : SmartisanSwitch.STYLE_LIGHT);
            }
            toggle.setButtonText(darkSwitchStyle ? R.string.smartisan_catalog_light_switch
                    : R.string.smartisan_catalog_dark_switch);
        });
        addRow(parent, toggle);
    }

    private void addListItems(LinearLayout parent) {
        SmartisanListContentItemText single = new SmartisanListContentItemText(this);
        single.setTitle(R.string.smartisan_catalog_wifi);
        single.setSummary(R.string.smartisan_catalog_wifi_summary);
        single.setSubtitle(R.string.smartisan_catalog_connected);
        single.setBackgroundStyle(SmartisanListContentItem.BG_STYLE_SINGLE);
        single.setClickable(true);
        addListItem(parent, single, 10);

        SmartisanListContentItemText top = new SmartisanListContentItemText(this);
        top.setTitle(R.string.smartisan_catalog_notifications);
        top.setSubtitle(R.string.smartisan_catalog_enabled);
        top.setBackgroundStyle(SmartisanListContentItem.BG_STYLE_TOP);
        top.setClickable(true);
        addListItem(parent, top, 0);

        SmartisanListContentItemSwitch middle = new SmartisanListContentItemSwitch(this);
        middle.setTitle(R.string.smartisan_catalog_mobile_data);
        middle.setSummary(R.string.smartisan_catalog_mobile_data_summary);
        middle.setChecked(true);
        middle.setBackgroundStyle(SmartisanListContentItem.BG_STYLE_MIDDLE);
        addListItem(parent, middle, 0);

        SmartisanListContentItemCheck bottom = new SmartisanListContentItemCheck(this);
        bottom.setTitle(R.string.smartisan_catalog_selected_item);
        bottom.setChecked(true);
        bottom.setBackgroundStyle(SmartisanListContentItem.BG_STYLE_BOTTOM);
        bottom.setClickable(true);
        addListItem(parent, bottom, 10);

        SmartisanListContentItemCheck unchecked = new SmartisanListContentItemCheck(this);
        unchecked.setTitle(R.string.smartisan_catalog_unselected_item);
        unchecked.setChecked(false);
        unchecked.setBackgroundStyle(SmartisanListContentItem.BG_STYLE_SINGLE);
        addListItem(parent, unchecked, 10);

        SmartisanListContentItemText noArrow = new SmartisanListContentItemText(this);
        noArrow.setTitle(R.string.smartisan_catalog_no_arrow);
        noArrow.setSubtitle(R.string.smartisan_catalog_value);
        noArrow.setArrowVisible(false);
        noArrow.setBackgroundStyle(SmartisanListContentItem.BG_STYLE_SINGLE);
        addListItem(parent, noArrow, 10);

        SmartisanListContentItemSwitch disabled = new SmartisanListContentItemSwitch(this);
        disabled.setTitle(R.string.smartisan_catalog_disabled_switch);
        disabled.setChecked(false);
        disabled.setBackgroundStyle(SmartisanListContentItem.BG_STYLE_SINGLE);
        disabled.setEnabled(false);
        addListItem(parent, disabled, 10);
    }

    private void addSettingItems(LinearLayout parent) {
        SmartisanSettingItemText text = new SmartisanSettingItemText(this);
        text.setTitle(R.string.smartisan_catalog_account);
        text.setSummary(R.string.smartisan_catalog_account_summary);
        text.setSubtitle(R.string.smartisan_catalog_signed_in);
        text.setupInfoButton(true, view -> { });
        text.setBackgroundStyle(SmartisanListContentItem.BG_STYLE_TOP);
        addListItem(parent, text, 0);

        SmartisanSettingItemSwitch switchItem = new SmartisanSettingItemSwitch(this);
        switchItem.setTitle(R.string.smartisan_catalog_backup);
        switchItem.setSummary(R.string.smartisan_catalog_backup_summary);
        switchItem.setChecked(true);
        switchItem.setBackgroundStyle(SmartisanListContentItem.BG_STYLE_MIDDLE);
        addListItem(parent, switchItem, 0);

        SmartisanSettingItemCheck check = new SmartisanSettingItemCheck(this);
        check.setTitle(R.string.smartisan_catalog_sync);
        check.setSummary(R.string.smartisan_catalog_sync_summary);
        check.setChecked(true);
        check.setBackgroundStyle(SmartisanListContentItem.BG_STYLE_BOTTOM);
        addListItem(parent, check, 10);
    }

    private void addListItem(LinearLayout parent, View item, int bottomMargin) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(bottomMargin);
        parent.addView(item, params);
    }

    private void addRow(LinearLayout parent, View row) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(10);
        parent.addView(row, params);
    }
}
