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
import org.opensmartisanos.ui.widget.SmartisanShadowButton;
import org.opensmartisanos.ui.widget.SmartisanSwitch;

public final class CatalogActivity extends Activity {
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
        addButtonRow(content, SmartisanButton.STYLE_NORMAL, R.string.smartisan_catalog_normal,
                SmartisanButton.STYLE_BACK, R.string.smartisan_catalog_back);
        addButtonRow(content, SmartisanButton.STYLE_HIGHLIGHT_BLUE,
                R.string.smartisan_catalog_confirm, SmartisanButton.STYLE_HIGHLIGHT_RED,
                R.string.smartisan_catalog_delete);
        addIconButtonRow(content);
        addDisabledButtonRow(content);

        addSection(content, R.string.smartisan_catalog_shadow_button);
        addLongButtons(content, false);
        addLongButtons(content, true);

        addSection(content, R.string.smartisan_catalog_switch);
        addSwitchRow(content, false, false);
        addSwitchRow(content, true, false);
        addSwitchRow(content, false, true);

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
        row.addView(smartisanSwitch);
        addRow(parent, row);
    }

    private void addRow(LinearLayout parent, View row) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(10);
        parent.addView(row, params);
    }
}
