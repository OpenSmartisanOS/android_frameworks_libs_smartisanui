/*
 * Copyright (C) 2026 The OpenSmartisanOS Project
 * Licensed under the Apache License, Version 2.0.
 */
package org.opensmartisanos.ui.catalog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Arrays;
import org.opensmartisanos.ui.app.SmartisanMenuDialog;
import org.opensmartisanos.ui.app.SmartisanProgressDialog;
import org.opensmartisanos.ui.widget.SmartisanActionButtonGroup;
import org.opensmartisanos.ui.widget.SmartisanBottomBar;
import org.opensmartisanos.ui.widget.SmartisanButton;
import org.opensmartisanos.ui.widget.SmartisanButtonGroup;
import org.opensmartisanos.ui.widget.SmartisanCircleProgressPopup;
import org.opensmartisanos.ui.widget.SmartisanCircleProgressView;
import org.opensmartisanos.ui.widget.SmartisanDialogTitleBar;
import org.opensmartisanos.ui.widget.SmartisanDownloadProgressView;
import org.opensmartisanos.ui.widget.SmartisanEmptyView;
import org.opensmartisanos.ui.widget.SmartisanIconBottomBar;
import org.opensmartisanos.ui.widget.SmartisanLabelEditor;
import org.opensmartisanos.ui.widget.SmartisanListContentItem;
import org.opensmartisanos.ui.widget.SmartisanListContentItemCheck;
import org.opensmartisanos.ui.widget.SmartisanListContentItemSwitch;
import org.opensmartisanos.ui.widget.SmartisanListContentItemText;
import org.opensmartisanos.ui.widget.SmartisanListPopupMenu;
import org.opensmartisanos.ui.widget.SmartisanMixBottomBar;
import org.opensmartisanos.ui.widget.SmartisanPasswordEditText;
import org.opensmartisanos.ui.widget.SmartisanQuickDeleteEditText;
import org.opensmartisanos.ui.widget.SmartisanSearchBar;
import org.opensmartisanos.ui.widget.SmartisanSearchEditText;
import org.opensmartisanos.ui.widget.SmartisanSegmentedControl;
import org.opensmartisanos.ui.widget.SmartisanSettingItemCheck;
import org.opensmartisanos.ui.widget.SmartisanSettingItemSwitch;
import org.opensmartisanos.ui.widget.SmartisanSettingItemText;
import org.opensmartisanos.ui.widget.SmartisanShadowButton;
import org.opensmartisanos.ui.widget.SmartisanSimpleEditor;
import org.opensmartisanos.ui.widget.SmartisanSliderWithIcons;
import org.opensmartisanos.ui.widget.SmartisanSmoothSeekBar;
import org.opensmartisanos.ui.widget.SmartisanSnackbarWithButton;
import org.opensmartisanos.ui.widget.SmartisanSnackbarWithDrawable;
import org.opensmartisanos.ui.widget.SmartisanSwitch;
import org.opensmartisanos.ui.widget.SmartisanTabSwitcher;
import org.opensmartisanos.ui.widget.SmartisanTipsBar;
import org.opensmartisanos.ui.widget.SmartisanTitleBar;

public final class CatalogActivity extends Activity {
  private static final String EXTRA_COMPONENT = "component";

  private static final String COMPONENT_BUTTON = "button";
  private static final String COMPONENT_SHADOW_BUTTON = "shadow_button";
  private static final String COMPONENT_SWITCH = "switch";
  private static final String COMPONENT_SEGMENTED = "segmented";
  private static final String COMPONENT_LIST_ITEM = "list_item";
  private static final String COMPONENT_SETTING_ITEM = "setting_item";
  private static final String COMPONENT_SEARCH_BAR = "search_bar";
  private static final String COMPONENT_EDITORS = "editors";
  private static final String COMPONENT_TITLE_BAR = "title_bar";
  private static final String COMPONENT_BOTTOM_BARS = "bottom_bars";
  private static final String COMPONENT_BUTTON_GROUPS = "button_groups";
  private static final String COMPONENT_SLIDER_PROGRESS = "slider_progress";
  private static final String COMPONENT_SNACKBARS = "snackbars";
  private static final String COMPONENT_DIALOGS = "dialogs";
  private static final String COMPONENT_POPUP = "popup";
  private static final String COMPONENT_TIPS = "tips";
  private static final String COMPONENT_EMPTY_VIEW = "empty_view";

  private static final CatalogEntry[][] CATALOG_GROUPS = {
    {
      new CatalogEntry(COMPONENT_BUTTON, R.string.smartisan_catalog_smartisan_button),
      new CatalogEntry(COMPONENT_SHADOW_BUTTON, R.string.smartisan_catalog_shadow_button),
      new CatalogEntry(COMPONENT_SWITCH, R.string.smartisan_catalog_switch),
      new CatalogEntry(COMPONENT_SEGMENTED, R.string.smartisan_catalog_segmented)
    },
    {
      new CatalogEntry(COMPONENT_LIST_ITEM, R.string.smartisan_catalog_list_item),
      new CatalogEntry(COMPONENT_SETTING_ITEM, R.string.smartisan_catalog_setting_item),
      new CatalogEntry(COMPONENT_SEARCH_BAR, R.string.smartisan_catalog_search_bar),
      new CatalogEntry(COMPONENT_EDITORS, R.string.smartisan_catalog_editors)
    },
    {
      new CatalogEntry(COMPONENT_TITLE_BAR, R.string.smartisan_catalog_title_bar),
      new CatalogEntry(COMPONENT_BOTTOM_BARS, R.string.smartisan_catalog_bottom_bars),
      new CatalogEntry(COMPONENT_BUTTON_GROUPS, R.string.smartisan_catalog_button_groups),
      new CatalogEntry(COMPONENT_SLIDER_PROGRESS, R.string.smartisan_catalog_slider_progress),
      new CatalogEntry(COMPONENT_SNACKBARS, R.string.smartisan_catalog_snackbars),
      new CatalogEntry(COMPONENT_DIALOGS, R.string.smartisan_catalog_dialogs),
      new CatalogEntry(COMPONENT_POPUP, R.string.smartisan_catalog_popup),
      new CatalogEntry(COMPONENT_TIPS, R.string.smartisan_catalog_tips),
      new CatalogEntry(COMPONENT_EMPTY_VIEW, R.string.smartisan_catalog_empty_view)
    }
  };

  private static final int[] CATALOG_GROUP_TITLES = {
    R.string.smartisan_catalog_group_controls,
    R.string.smartisan_catalog_group_content,
    R.string.smartisan_catalog_group_navigation
  };

  private final java.util.List<SmartisanSwitch> switchSamples = new java.util.ArrayList<>();
  private boolean darkSwitchStyle;

  private static final class CatalogEntry {
    final String id;
    final int title;

    CatalogEntry(String id, int title) {
      this.id = id;
      this.title = title;
    }
  }

  private int dp(float value) {
    return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    String component = getIntent().getStringExtra(EXTRA_COMPONENT);
    if (component == null) {
      showCatalog();
    } else {
      showComponent(component);
    }
  }

  private void showCatalog() {
    LinearLayout root = createPageRoot();
    root.addView(createTitleBar(R.string.smartisan_catalog_name, false));

    LinearLayout content = createScrollContent(root, 12, 12, 12, 32);
    for (int group = 0; group < CATALOG_GROUPS.length; group++) {
      addCatalogGroup(content, CATALOG_GROUP_TITLES[group], CATALOG_GROUPS[group]);
    }
    setContentView(root);
  }

  private void showComponent(String component) {
    CatalogEntry entry = findEntry(component);
    if (entry == null) {
      finish();
      return;
    }

    LinearLayout root = createPageRoot();
    root.addView(createTitleBar(entry.title, true));
    LinearLayout content = createScrollContent(root, 16, 18, 16, 32);
    addComponentSamples(content, component);
    setContentView(root);
  }

  private LinearLayout createPageRoot() {
    LinearLayout root = new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setBackgroundColor(
        getResources().getColor(org.opensmartisanos.ui.R.color.smartisan_catalog_background));
    Window window = getWindow();
    window.setStatusBarColor(Color.WHITE);
    window.setNavigationBarColor(
        getResources().getColor(org.opensmartisanos.ui.R.color.smartisan_catalog_background));
    if (android.os.Build.VERSION.SDK_INT >= 26) {
      window.getDecorView().setSystemUiVisibility(
          View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
    } else if (android.os.Build.VERSION.SDK_INT >= 23) {
      window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }
    root.setOnApplyWindowInsetsListener(
        (view, insets) -> {
          view.setPadding(view.getPaddingLeft(), insets.getSystemWindowInsetTop(),
              view.getPaddingRight(), insets.getSystemWindowInsetBottom());
          return insets;
        });
    root.post(() -> root.requestApplyInsets());
    return root;
  }

  private SmartisanTitleBar createTitleBar(int title, boolean showBack) {
    SmartisanTitleBar titleBar = new SmartisanTitleBar(this);
    titleBar.setCenterText(title);
    if (showBack) {
      titleBar.addLeftImageView(SmartisanTitleBar.BACK_ICON_RES).setOnClickListener(view -> finish());
    }
    return titleBar;
  }

  private LinearLayout createScrollContent(
      LinearLayout root, int left, int top, int right, int bottom) {
    ScrollView scroll = new ScrollView(this);
    scroll.setFillViewport(true);
    scroll.setClipToPadding(false);
    LinearLayout content = new LinearLayout(this);
    content.setOrientation(LinearLayout.VERTICAL);
    content.setPadding(dp(left), dp(top), dp(right), dp(bottom));
    scroll.addView(
        content,
        new ScrollView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    root.addView(scroll, new LinearLayout.LayoutParams(0, 0, 1f));
    LinearLayout.LayoutParams scrollParams = (LinearLayout.LayoutParams) scroll.getLayoutParams();
    scrollParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
    scroll.setLayoutParams(scrollParams);
    return content;
  }

  private void addCatalogGroup(LinearLayout parent, int heading, CatalogEntry[] entries) {
    addSection(parent, heading);
    for (int i = 0; i < entries.length; i++) {
      CatalogEntry entry = entries[i];
      SmartisanListContentItemText item = new SmartisanListContentItemText(this);
      item.setTitle(entry.title);
      item.setBackgroundStyle(groupBackgroundStyle(i, entries.length));
      item.setClickable(true);
      item.setOnClickListener(view -> openComponent(entry.id));
      addListItem(parent, item, i == entries.length - 1 ? 12 : 0);
    }
  }

  private static int groupBackgroundStyle(int index, int size) {
    if (size == 1) return SmartisanListContentItem.BG_STYLE_SINGLE;
    if (index == 0) return SmartisanListContentItem.BG_STYLE_TOP;
    if (index == size - 1) return SmartisanListContentItem.BG_STYLE_BOTTOM;
    return SmartisanListContentItem.BG_STYLE_MIDDLE;
  }

  private void openComponent(String component) {
    Intent intent = new Intent(this, CatalogActivity.class);
    intent.putExtra(EXTRA_COMPONENT, component);
    startActivity(intent);
  }

  private CatalogEntry findEntry(String component) {
    for (CatalogEntry[] group : CATALOG_GROUPS) {
      for (CatalogEntry entry : group) {
        if (entry.id.equals(component)) return entry;
      }
    }
    return null;
  }

  private void addComponentSamples(LinearLayout content, String component) {
    switch (component) {
      case COMPONENT_BUTTON -> addSmartisanButtons(content);
      case COMPONENT_SHADOW_BUTTON -> addShadowButtons(content);
      case COMPONENT_SWITCH -> {
        addSwitchRow(content, false, false);
        addSwitchRow(content, true, false);
        addSwitchRow(content, false, true);
        addSwitchStyleControl(content);
      }
      case COMPONENT_SEGMENTED -> addSegmentedControls(content);
      case COMPONENT_LIST_ITEM -> addListItems(content);
      case COMPONENT_SETTING_ITEM -> addSettingItems(content);
      case COMPONENT_SEARCH_BAR -> addSearchBars(content);
      case COMPONENT_EDITORS -> addEditors(content);
      case COMPONENT_TITLE_BAR -> addTitleBars(content);
      case COMPONENT_BOTTOM_BARS -> addBottomBars(content);
      case COMPONENT_BUTTON_GROUPS -> addButtonGroups(content);
      case COMPONENT_SLIDER_PROGRESS -> addSliderAndProgress(content);
      case COMPONENT_SNACKBARS -> addSnackbars(content);
      case COMPONENT_DIALOGS -> addDialogs(content);
      case COMPONENT_POPUP -> addPopup(content);
      case COMPONENT_TIPS -> addTips(content);
      case COMPONENT_EMPTY_VIEW -> addEmptyView(content);
      default -> throw new IllegalArgumentException("Unknown component: " + component);
    }
  }

  private void addSection(LinearLayout parent, int text) {
    TextView view = new TextView(this);
    view.setText(text);
    view.setTextColor(Color.rgb(102, 102, 102));
    view.setTextSize(14);
    view.setGravity(Gravity.BOTTOM);
    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52));
    params.bottomMargin = dp(10);
    parent.addView(view, params);
  }

  private LinearLayout newRow() {
    LinearLayout row = new LinearLayout(this);
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setGravity(Gravity.CENTER_VERTICAL);
    return row;
  }

  private void addButtonRow(
      LinearLayout parent, int leftStyle, int leftText, int rightStyle, int rightText) {
    LinearLayout row = newRow();
    SmartisanButton left = button(leftStyle, leftText);
    SmartisanButton right = button(rightStyle, rightText);
    row.addView(left);
    LinearLayout.LayoutParams rightParams =
        new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    rightParams.leftMargin = dp(12);
    row.addView(right, rightParams);
    addRow(parent, row);
  }

  private void addSmartisanButtons(LinearLayout parent) {
    addButtonRow(
        parent,
        SmartisanButton.STYLE_NORMAL,
        R.string.smartisan_catalog_normal,
        SmartisanButton.STYLE_BACK,
        R.string.smartisan_catalog_back);
    addButtonRow(
        parent,
        SmartisanButton.STYLE_HIGHLIGHT_BLUE,
        R.string.smartisan_catalog_confirm,
        SmartisanButton.STYLE_HIGHLIGHT_RED,
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
    int[] styles = {
      SmartisanButton.STYLE_SETTING, SmartisanButton.STYLE_DELETE, SmartisanButton.STYLE_SEARCH
    };
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
    SmartisanButton normal =
        button(SmartisanButton.STYLE_NORMAL, R.string.smartisan_catalog_disabled);
    normal.setEnabled(false);
    normal.setEnabledStyle(false);
    SmartisanButton highlight =
        button(SmartisanButton.STYLE_HIGHLIGHT_BLUE, R.string.smartisan_catalog_disabled);
    highlight.setEnabled(false);
    highlight.setEnabledStyle(false);
    row.addView(normal);
    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    params.leftMargin = dp(12);
    row.addView(highlight, params);
    addRow(parent, row);
  }

  private void addLongButtons(LinearLayout parent, boolean shrink) {
    LinearLayout row = newRow();
    int style =
        shrink
            ? org.opensmartisanos.ui.R.style.Widget_SmartisanUi_ShadowButton_Shrink
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
    addLongButtonPair(
        parent,
        SmartisanShadowButton.LongButtonStyle.WHITE,
        R.string.smartisan_catalog_white,
        SmartisanShadowButton.LongButtonStyle.GRAY,
        R.string.smartisan_catalog_gray);
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

  private void addLongButtonPair(
      LinearLayout parent,
      SmartisanShadowButton.LongButtonStyle leftStyle,
      int leftText,
      SmartisanShadowButton.LongButtonStyle rightStyle,
      int rightText) {
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
    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    params.leftMargin = dp(8);
    return params;
  }

  private void addSwitchRow(LinearLayout parent, boolean checked, boolean disabled) {
    LinearLayout row = newRow();
    TextView label = new TextView(this);
    label.setText(
        disabled
            ? R.string.smartisan_catalog_disabled
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
    SmartisanButton toggle =
        button(SmartisanButton.STYLE_NORMAL, R.string.smartisan_catalog_dark_switch);
    toggle.setOnClickListener(
        view -> {
          darkSwitchStyle = !darkSwitchStyle;
          for (SmartisanSwitch sample : switchSamples) {
            sample.setSwitchDrawableStyle(
                darkSwitchStyle ? SmartisanSwitch.STYLE_DARK : SmartisanSwitch.STYLE_LIGHT);
          }
          toggle.setButtonText(
              darkSwitchStyle
                  ? R.string.smartisan_catalog_light_switch
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

    View custom =
        LayoutInflater.from(this).inflate(R.layout.catalog_custom_list_item, parent, false);
    ((SmartisanListContentItem) custom)
        .setBackgroundStyle(SmartisanListContentItem.BG_STYLE_SINGLE);
    custom.setClickable(true);
    addListItem(parent, custom, 10);
  }

  private void addSettingItems(LinearLayout parent) {
    SmartisanSettingItemText text = new SmartisanSettingItemText(this);
    text.setTitle(R.string.smartisan_catalog_account);
    text.setSummary(R.string.smartisan_catalog_account_summary);
    text.setSubtitle(R.string.smartisan_catalog_signed_in);
    text.setupInfoButton(true, view -> {});
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
    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    params.bottomMargin = dp(bottomMargin);
    parent.addView(item, params);
  }

  private void addTitleBars(LinearLayout parent) {
    SmartisanTitleBar titleBar = new SmartisanTitleBar(this);
    titleBar.setCenterText(R.string.smartisan_catalog_page_title);
    titleBar.addLeftImageView(SmartisanTitleBar.BACK_ICON_RES);
    titleBar.addRightButton(
        SmartisanButton.STYLE_HIGHLIGHT_BLUE, R.string.smartisan_catalog_confirm);
    addRow(parent, titleBar);

    SmartisanDialogTitleBar dialogTitle = new SmartisanDialogTitleBar(this);
    dialogTitle.setTitle(R.string.smartisan_catalog_page_title);
    dialogTitle.addCancelImage(true);
    dialogTitle.addCompleteImage(false);
    dialogTitle.setLeftButtonVisibility(View.VISIBLE);
    addRow(parent, dialogTitle);
  }

  private void addSearchBars(LinearLayout parent) {
    SmartisanSearchBar standard = new SmartisanSearchBar(this);
    standard.setHint(getText(R.string.smartisan_catalog_search_hint));
    standard.addRightImageView(
        org.opensmartisanos.ui.R.drawable.smartisan_rom_sorting_icon_selector);
    addSearchBar(parent, standard);

    SmartisanSearchBar filter = new SmartisanSearchBar(this);
    filter.setHint(getText(R.string.smartisan_catalog_search_hint));
    filter.setSecondaryFilterText(R.string.smartisan_catalog_filter);
    filter.setSecondaryFilterVisibility(View.VISIBLE);
    addSearchBar(parent, filter);

    SmartisanSearchBar active = new SmartisanSearchBar(this);
    active.setWithAnimation(false);
    active.onClickSearchEditor(false);
    active.setQuery(getText(R.string.smartisan_catalog_search_long_query));
    addSearchBar(parent, active);

    SmartisanSearchEditText editor = new SmartisanSearchEditText(this);
    editor.setSingleLine(true);
    editor.setHint(R.string.smartisan_catalog_search_editor_hint);
    editor.setTextSize(15);
    editor.setTextColor(Color.rgb(51, 51, 51));
    editor.setHintTextColor(Color.rgb(153, 153, 153));
    editor.setGravity(Gravity.CENTER_VERTICAL);
    editor.setPadding(dp(14), 0, dp(14), 0);
    editor.setBackgroundResource(
        org.opensmartisanos.ui.R.drawable.smartisan_rom_search_bar_edit_bg_selector);
    LinearLayout.LayoutParams editorParams =
        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(44));
    editorParams.bottomMargin = dp(10);
    parent.addView(editor, editorParams);
  }

  private void addSearchBar(LinearLayout parent, SmartisanSearchBar searchBar) {
    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
    params.bottomMargin = dp(10);
    parent.addView(searchBar, params);
  }

  private void addEditors(LinearLayout parent) {
    SmartisanQuickDeleteEditText quickDelete = new SmartisanQuickDeleteEditText(this);
    quickDelete.setSingleLine(true);
    quickDelete.setText(R.string.smartisan_catalog_editor_value);
    quickDelete.setHint(R.string.smartisan_catalog_editor_hint);
    quickDelete.setTextSize(15);
    quickDelete.setBackgroundResource(
        org.opensmartisanos.ui.R.drawable.smartisan_rom_editor_bg_single);
    quickDelete.setPadding(dp(12), 0, dp(6), 0);
    LinearLayout.LayoutParams quickParams =
        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(44));
    quickParams.bottomMargin = dp(10);
    parent.addView(quickDelete, quickParams);

    SmartisanPasswordEditText password = new SmartisanPasswordEditText(this);
    password.setSingleLine(true);
    password.setText(R.string.smartisan_catalog_password_value);
    password.setHint(R.string.smartisan_catalog_password_hint);
    password.setTextSize(15);
    password.setBackgroundResource(
        org.opensmartisanos.ui.R.drawable.smartisan_rom_editor_bg_single);
    password.setPadding(dp(12), 0, dp(6), 0);
    LinearLayout.LayoutParams passwordParams =
        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(44));
    passwordParams.bottomMargin = dp(10);
    parent.addView(password, passwordParams);

    SmartisanLabelEditor top =
        labelEditor(
            R.string.smartisan_catalog_editor_account,
            R.string.smartisan_catalog_editor_hint,
            SmartisanLabelEditor.BG_STYLE_TOP);
    addRow(parent, top);
    SmartisanLabelEditor middle =
        labelEditor(
            R.string.smartisan_catalog_editor_server,
            R.string.smartisan_catalog_editor_server_hint,
            SmartisanLabelEditor.BG_STYLE_MIDDLE);
    middle.setRightLabel(R.string.smartisan_catalog_editor_optional);
    addRow(parent, middle);
    SmartisanLabelEditor bottom =
        labelEditor(
            R.string.smartisan_catalog_editor_note,
            R.string.smartisan_catalog_editor_note_hint,
            SmartisanLabelEditor.BG_STYLE_BOTTOM);
    bottom.setParagraphMode(true);
    addRow(parent, bottom);

    SmartisanSimpleEditor simple = new SmartisanSimpleEditor(this);
    simple.setBackgroundStyle(SmartisanSimpleEditor.BG_STYLE_SINGLE);
    simple.getEditor().setHint(R.string.smartisan_catalog_editor_simple_hint);
    addRow(parent, simple);
  }

  private SmartisanLabelEditor labelEditor(int label, int hint, int background) {
    SmartisanLabelEditor editor = new SmartisanLabelEditor(this);
    editor.setBackgroundStyle(background);
    editor.setLeftLabel(label);
    editor.getEditor().setHint(hint);
    return editor;
  }

  private void addBottomBars(LinearLayout parent) {
    SmartisanBottomBar tabs = new SmartisanBottomBar(this);
    tabs.addBarItem(
        View.generateViewId(),
        getString(R.string.smartisan_catalog_world_clock),
        org.opensmartisanos.ui.R.drawable.smartisan_rom_clock_tab_worldclock,
        org.opensmartisanos.ui.R.color.smartisan_rom_clock_bottom_bar_icon);
    tabs.addBarItem(
        View.generateViewId(),
        getString(R.string.smartisan_catalog_alarm),
        org.opensmartisanos.ui.R.drawable.smartisan_rom_clock_tab_alarm,
        org.opensmartisanos.ui.R.color.smartisan_rom_clock_bottom_bar_icon);
    tabs.addBarItem(
        View.generateViewId(),
        getString(R.string.smartisan_catalog_stopwatch),
        org.opensmartisanos.ui.R.drawable.smartisan_rom_clock_tab_stopwatch,
        org.opensmartisanos.ui.R.color.smartisan_rom_clock_bottom_bar_icon);
    tabs.addBarItem(
        View.generateViewId(),
        getString(R.string.smartisan_catalog_timer),
        org.opensmartisanos.ui.R.drawable.smartisan_rom_clock_tab_timer,
        org.opensmartisanos.ui.R.color.smartisan_rom_clock_bottom_bar_icon);
    tabs.setup(true);
    addRow(parent, tabs);

    int clockId = View.generateViewId();
    int alarmId = View.generateViewId();
    int stopwatchId = View.generateViewId();
    int timerId = View.generateViewId();
    SmartisanTabSwitcher editableTabs = new SmartisanTabSwitcher(this);
    editableTabs.setTabs(
        Arrays.asList(
            new SmartisanTabSwitcher.Tab(
                clockId,
                getString(R.string.smartisan_catalog_world_clock),
                org.opensmartisanos.ui.R.drawable.smartisan_rom_clock_tab_worldclock,
                org.opensmartisanos.ui.R.drawable.smartisan_rom_clock_tab_worldclock,
                true),
            new SmartisanTabSwitcher.Tab(
                alarmId,
                getString(R.string.smartisan_catalog_alarm),
                org.opensmartisanos.ui.R.drawable.smartisan_rom_clock_tab_alarm,
                org.opensmartisanos.ui.R.drawable.smartisan_rom_clock_tab_alarm,
                true),
            new SmartisanTabSwitcher.Tab(
                timerId,
                getString(R.string.smartisan_catalog_timer),
                org.opensmartisanos.ui.R.drawable.smartisan_rom_clock_tab_timer,
                org.opensmartisanos.ui.R.drawable.smartisan_rom_clock_tab_timer,
                false)),
        Arrays.asList(
            new SmartisanTabSwitcher.Tab(
                stopwatchId,
                getString(R.string.smartisan_catalog_stopwatch),
                org.opensmartisanos.ui.R.drawable.smartisan_rom_clock_tab_stopwatch,
                org.opensmartisanos.ui.R.drawable.smartisan_rom_clock_tab_stopwatch,
                true)));
    editableTabs.setSelectedTabId(clockId);
    addRow(parent, editableTabs);

    SmartisanIconBottomBar icons = new SmartisanIconBottomBar(this);
    icons.setIconRefArray(
        new int[] {
          org.opensmartisanos.ui.R.drawable.smartisan_rom_qsb_tab_t9_voice_selector,
          org.opensmartisanos.ui.R.drawable.smartisan_rom_qsb_tab_softkey_selector
        });
    FrameLayout iconBarHost = new FrameLayout(this);
    iconBarHost.setClipChildren(false);
    int iconBarHeight =
        getResources()
            .getDimensionPixelSize(org.opensmartisanos.ui.R.dimen.smartisan_button_fixed_height);
    iconBarHost.addView(
        icons,
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, iconBarHeight));
    LinearLayout.LayoutParams iconBarHostParams =
        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, iconBarHeight);
    iconBarHostParams.bottomMargin = dp(10);
    parent.addView(iconBarHost, iconBarHostParams);

    SmartisanMixBottomBar mix = new SmartisanMixBottomBar(this);
    addRow(parent, mix);
  }

  private void addButtonGroups(LinearLayout parent) {
    SmartisanButtonGroup joined = new SmartisanButtonGroup(this);
    joined.setButtonCount(3, false);
    joined.setButtonText(0, R.string.smartisan_catalog_item_one);
    joined.setButtonText(1, R.string.smartisan_catalog_item_two);
    joined.setButtonText(2, R.string.smartisan_catalog_item_three);
    joined.setButtonActivated(0);
    bindButtonGroup(joined, false);
    addRow(parent, joined);

    SmartisanButtonGroup separated = new SmartisanButtonGroup(this);
    separated.setButtonCount(3, true);
    separated.setButtonText(0, R.string.smartisan_catalog_cancel);
    separated.setButtonText(1, R.string.smartisan_catalog_confirm);
    separated.setButtonText(2, R.string.smartisan_catalog_delete);
    bindButtonGroup(separated, true);
    addRow(parent, separated);

    SmartisanActionButtonGroup actions = new SmartisanActionButtonGroup(this);
    actions
        .getLeftActionButton()
        .setImageResource(
            org.opensmartisanos.ui.R.drawable.smartisan_rom_standard_icon_back_selector);
    for (int i = 0; i < actions.getButtonCount(); i++) {
      actions.setButtonText(
          i,
          i % 3 == 0
              ? R.string.smartisan_catalog_item_one
              : i % 3 == 1
                  ? R.string.smartisan_catalog_item_two
                  : R.string.smartisan_catalog_item_three);
      final int index = i;
      actions.getButton(i).setOnClickListener(view -> actions.setButtonActivated(index));
    }
    actions.setButtonActivated(0);
    actions.getLeftActionButton().setOnClickListener(view -> actions.setButtonActivated(-1));
    addRow(parent, actions);
  }

  private void bindButtonGroup(SmartisanButtonGroup group, boolean highlightSelected) {
    for (int i = 0; i < group.getButtonCount(); i++) {
      final int selected = i;
      group.getButton(i).setOnClickListener(
          view -> {
            group.setButtonActivated(selected);
            if (highlightSelected) {
              for (int button = 0; button < group.getButtonCount(); button++) {
                group
                    .getButton(button)
                    .updateBackgroundStyle(
                        button == selected
                            ? SmartisanShadowButton.SmallButtonStyle.HIGH_LIGHT
                            : SmartisanShadowButton.SmallButtonStyle.STANDARD);
              }
            }
          });
    }
  }

  private void addSliderAndProgress(LinearLayout parent) {
    SmartisanSliderWithIcons slider = new SmartisanSliderWithIcons(this);
    slider.setLeftIconVisible(true);
    slider.setRightIconVisible(true);
    slider.setLeftIcon(
        org.opensmartisanos.ui.R.drawable.smartisan_rom_standard_icon_cancel_selector);
    slider.setRightIcon(
        org.opensmartisanos.ui.R.drawable.smartisan_rom_standard_icon_complete_selector);
    slider.setMax(100);
    slider.setProgress(42);
    slider.setPadding(dp(8), dp(8), dp(8), dp(8));
    addRow(parent, slider);

    SmartisanSmoothSeekBar smooth = new SmartisanSmoothSeekBar(this);
    smooth.setMax(100);
    smooth.setProgress(20);
    addRow(parent, smooth);

    LinearLayout controls = newRow();
    SmartisanButton animate =
        button(SmartisanButton.STYLE_NORMAL, R.string.smartisan_catalog_smooth_seek);
    animate.setOnClickListener(
        view -> smooth.setProgressSmooth(smooth.getProgress() >= 50 ? 15 : 85));
    SmartisanButton showPopup =
        button(SmartisanButton.STYLE_HIGHLIGHT_BLUE, R.string.smartisan_catalog_circle_popup);
    showPopup.setOnClickListener(
        view -> {
          SmartisanCircleProgressPopup popup = new SmartisanCircleProgressPopup(this);
          popup.setCircleAnimDuration(900);
          popup.setCircleProgressListener(
              new SmartisanCircleProgressPopup.CircleProgressListenerAdapter() {
                @Override
                public void complete() {
                  popup.dismiss();
                }
              });
          popup.show(view, view.getWidth() / 2, view.getHeight() / 2, true);
        });
    controls.addView(animate);
    controls.addView(showPopup, spacedWrapParams());
    addRow(parent, controls);

    LinearLayout row = newRow();
    SmartisanCircleProgressView circle = new SmartisanCircleProgressView(this);
    circle.setSweepAngle(230);
    row.addView(circle);
    int[] states = {
      SmartisanDownloadProgressView.STATE_PAUSED,
      SmartisanDownloadProgressView.STATE_RESUMED,
      SmartisanDownloadProgressView.STATE_RETRY,
      SmartisanDownloadProgressView.STATE_PROCESSING
    };
    for (int state : states) {
      SmartisanDownloadProgressView progress = new SmartisanDownloadProgressView(this);
      progress.setCurrentState(state);
      progress.setProgress(64);
      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(44), dp(44));
      params.leftMargin = dp(14);
      row.addView(progress, params);
    }
    addRow(parent, row);
  }

  private void addSnackbars(LinearLayout parent) {
    SmartisanSnackbarWithButton button = new SmartisanSnackbarWithButton(this);
    button.setMessage(R.string.smartisan_catalog_snackbar_message);
    button.setActionText(R.string.smartisan_catalog_retry);
    button.setBackgroundColor(Color.rgb(255, 248, 231));
    addRow(parent, button);

    SmartisanSnackbarWithDrawable drawable = new SmartisanSnackbarWithDrawable(this);
    drawable.setMessage(R.string.smartisan_catalog_snackbar_message);
    drawable.setImageResource(org.opensmartisanos.ui.R.drawable.smartisan_rom_toast_action_dismiss);
    drawable.setBackgroundColor(Color.rgb(255, 248, 231));
    addRow(parent, drawable);
  }

  private void addDialogs(LinearLayout parent) {
    LinearLayout row = newRow();
    SmartisanButton menu =
        button(SmartisanButton.STYLE_NORMAL, R.string.smartisan_catalog_menu_dialog);
    menu.setOnClickListener(
        v -> {
          SmartisanMenuDialog dialog = new SmartisanMenuDialog(this);
          dialog.setTitle(R.string.smartisan_catalog_page_title);
          ArrayAdapter<String> adapter =
              new ArrayAdapter<>(
                  this,
                  android.R.layout.simple_list_item_1,
                  Arrays.asList(
                      getString(R.string.smartisan_catalog_item_one),
                      getString(R.string.smartisan_catalog_item_two),
                      getString(R.string.smartisan_catalog_item_three)));
          dialog.setAdapter(adapter, (parentView, item, position, id) -> dialog.dismiss());
          dialog.setNegativeButton(R.string.smartisan_catalog_cancel, v2 -> dialog.dismiss());
          dialog.setPositiveButton(R.string.smartisan_catalog_confirm, v2 -> {});
          dialog.show();
        });
    SmartisanButton progress =
        button(SmartisanButton.STYLE_HIGHLIGHT_BLUE, R.string.smartisan_catalog_progress_dialog);
    progress.setOnClickListener(
        v -> {
          SmartisanProgressDialog dialog =
              SmartisanProgressDialog.show(
                  this,
                  getText(R.string.smartisan_catalog_loading),
                  getText(R.string.smartisan_catalog_loading_message));
          dialog.setCanceledOnTouchOutside(true);
        });
    row.addView(menu);
    row.addView(progress, spacedWrapParams());
    addRow(parent, row);
  }

  private void addPopup(LinearLayout parent) {
    SmartisanButton anchor =
        button(SmartisanButton.STYLE_NORMAL, R.string.smartisan_catalog_popup_open);
    anchor.setOnClickListener(
        v -> {
          SmartisanListPopupMenu popup = new SmartisanListPopupMenu(this);
          popup.setAnchorView(v);
          popup.setMenuListTitleVisible(true);
          popup.setMenuListTitle(getText(R.string.smartisan_catalog_page_title));
          popup.setAdapter(
              new ArrayAdapter<>(
                  this,
                  android.R.layout.simple_list_item_1,
                  Arrays.asList(
                      getString(R.string.smartisan_catalog_item_one),
                      getString(R.string.smartisan_catalog_item_two),
                      getString(R.string.smartisan_catalog_item_three))));
          popup.setOnItemClickListener((parentView, item, position, id) -> popup.dismiss());
          popup.setBottomActionBarVisible(true);
          popup.setBottomText(getText(R.string.smartisan_catalog_filter));
          popup.showCenter(v.getHeight(), 0);
        });
    addRow(parent, anchor);
  }

  private void addTips(LinearLayout parent) {
    SmartisanTipsBar shortTip = new SmartisanTipsBar(this);
    shortTip.setText(R.string.smartisan_catalog_tips_short);
    addRow(parent, shortTip);
    SmartisanTipsBar longTip = new SmartisanTipsBar(this);
    longTip.setText(R.string.smartisan_catalog_tips_long);
    addRow(parent, longTip);
  }

  private void addEmptyView(LinearLayout parent) {
    SmartisanEmptyView empty = new SmartisanEmptyView(this);
    empty.setEmptyStyle(SmartisanEmptyView.STYLE_WITH_ACTION);
    empty.setPrimaryHint(getText(R.string.smartisan_catalog_empty_primary));
    empty.setSecondaryHint(getText(R.string.smartisan_catalog_empty_secondary));
    addRow(parent, empty);
  }

  private void addSegmentedControls(LinearLayout parent) {
    SmartisanSegmentedControl joined = new SmartisanSegmentedControl(this);
    joined.setItems(
        Arrays.asList(
            getString(R.string.smartisan_catalog_item_one),
            getString(R.string.smartisan_catalog_item_two),
            getString(R.string.smartisan_catalog_item_three)));
    joined.setSelectedIndex(0);
    joined.setItemEnabled(2, false);
    addRow(parent, joined);

    SmartisanSegmentedControl separated = new SmartisanSegmentedControl(this);
    separated.setItems(
        Arrays.asList(
            getString(R.string.smartisan_catalog_item_one),
            getString(R.string.smartisan_catalog_item_two)),
        true);
    separated.setSelectedIndex(1);
    addRow(parent, separated);
  }

  private void addRow(LinearLayout parent, View row) {
    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    params.bottomMargin = dp(10);
    parent.addView(row, params);
  }
}
