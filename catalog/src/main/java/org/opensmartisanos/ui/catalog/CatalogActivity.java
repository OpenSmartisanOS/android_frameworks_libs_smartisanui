/*
 * Copyright (C) 2026 The OpenSmartisanOS Project
 * Licensed under the Apache License, Version 2.0.
 */
package org.opensmartisanos.ui.catalog;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import org.opensmartisanos.ui.app.SmartisanAlertDialog;
import org.opensmartisanos.ui.app.SmartisanMenuDialog;
import org.opensmartisanos.ui.app.SmartisanMenuDialogListAdapter;
import org.opensmartisanos.ui.app.SmartisanMenuDialogMultiAdapter;
import org.opensmartisanos.ui.app.SmartisanProgressDialog;
import org.opensmartisanos.ui.widget.SmartisanActionButtonGroup;
import org.opensmartisanos.ui.widget.SmartisanAbsMenuItem;
import org.opensmartisanos.ui.widget.SmartisanBottomBar;
import org.opensmartisanos.ui.widget.SmartisanBottomMenuPopupWindow;
import org.opensmartisanos.ui.widget.SmartisanButton;
import org.opensmartisanos.ui.widget.SmartisanButtonGroup;
import org.opensmartisanos.ui.widget.SmartisanCheckBox;
import org.opensmartisanos.ui.widget.SmartisanCircleProgressPopup;
import org.opensmartisanos.ui.widget.SmartisanCircleProgressView;
import org.opensmartisanos.ui.widget.SmartisanCircularProgressBar;
import org.opensmartisanos.ui.widget.SmartisanDialogTitleBar;
import org.opensmartisanos.ui.widget.SmartisanDownloadProgressView;
import org.opensmartisanos.ui.widget.SmartisanEmptyView;
import org.opensmartisanos.ui.widget.SmartisanIconBottomBar;
import org.opensmartisanos.ui.widget.SmartisanDatePicker;
import org.opensmartisanos.ui.widget.SmartisanDatePickerDialog;
import org.opensmartisanos.ui.widget.SmartisanDatePickerEx;
import org.opensmartisanos.ui.widget.SmartisanDatePickerExDialog;
import org.opensmartisanos.ui.widget.SmartisanDateTimePickerDialog;
import org.opensmartisanos.ui.widget.SmartisanDialogPatternAppInfoLayout;
import org.opensmartisanos.ui.widget.SmartisanDialogPatternSectionGroup;
import org.opensmartisanos.ui.widget.SmartisanDialogPatternTwoLineSingleChoice;
import org.opensmartisanos.ui.widget.SmartisanGridIconPopupMenu;
import org.opensmartisanos.ui.widget.SmartisanGroupMenuAdapter;
import org.opensmartisanos.ui.widget.SmartisanLabelEditor;
import org.opensmartisanos.ui.widget.SmartisanListContentItem;
import org.opensmartisanos.ui.widget.SmartisanListContentItemCheck;
import org.opensmartisanos.ui.widget.SmartisanListContentItemSwitch;
import org.opensmartisanos.ui.widget.SmartisanListContentItemText;
import org.opensmartisanos.ui.widget.SmartisanListPopupMenu;
import org.opensmartisanos.ui.widget.SmartisanListPopupMenuStandardAdapter;
import org.opensmartisanos.ui.widget.SmartisanMenuItem;
import org.opensmartisanos.ui.widget.SmartisanMixBottomBar;
import org.opensmartisanos.ui.widget.SmartisanPasswordEditText;
import org.opensmartisanos.ui.widget.SmartisanProgressBar;
import org.opensmartisanos.ui.widget.SmartisanQuickDeleteEditText;
import org.opensmartisanos.ui.widget.SmartisanRadioButton;
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
import org.opensmartisanos.ui.widget.SmartisanSpinnerView;
import org.opensmartisanos.ui.widget.SmartisanTabSwitcher;
import org.opensmartisanos.ui.widget.SmartisanTimePickerDialog;
import org.opensmartisanos.ui.widget.SmartisanTimePickerExDialog;
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
  private static final String COMPONENT_SLIDER = "slider";
  private static final String COMPONENT_SEEKBAR = "seekbar";
  private static final String COMPONENT_DRAWN_PROGRESS = "drawn_progress";
  private static final String COMPONENT_EDIT_TEXTS = "edit_texts";
  private static final String COMPONENT_SNACKBARS = "snackbars";
  private static final String COMPONENT_ALERT_CONFIRM = "alert_confirm";
  private static final String COMPONENT_ALERT_LIST = "alert_list";
  private static final String COMPONENT_ALERT_CHOICES = "alert_choices";
  private static final String COMPONENT_ALERT_INPUT = "alert_input";
  private static final String COMPONENT_MENU_DIALOG = "menu_dialog";
  private static final String COMPONENT_PICKERS = "pickers";
  private static final String COMPONENT_PROGRESS_DIALOG = "progress_dialog";
  private static final String COMPONENT_ANCHOR_MENU = "anchor_menu";
  private static final String COMPONENT_GRID_MENU = "grid_menu";
  private static final String COMPONENT_BOTTOM_MENU = "bottom_menu";
  private static final String COMPONENT_DIALOG_PATTERNS = "dialog_patterns";
  private static final String COMPONENT_TIPS = "tips";
  private static final String COMPONENT_EMPTY_VIEW = "empty_view";
  private static final String COMPONENT_SDK_SELECTION = "sdk_selection";
  private static final String COMPONENT_SDK_PROGRESS = "sdk_progress";
  private static final String COMPONENT_SDK_SPINNER = "sdk_spinner";

  private static final CatalogEntry[][] CATALOG_GROUPS = {
    {
      new CatalogEntry(COMPONENT_BUTTON, R.string.smartisan_catalog_smartisan_button,
          R.string.smartisan_catalog_impl_platform, R.string.smartisan_catalog_parent_button),
      new CatalogEntry(COMPONENT_SHADOW_BUTTON, R.string.smartisan_catalog_shadow_button,
          R.string.smartisan_catalog_impl_platform, R.string.smartisan_catalog_parent_button),
      new CatalogEntry(COMPONENT_SDK_SELECTION, R.string.smartisan_catalog_sdk_selection,
          R.string.smartisan_catalog_impl_platform, R.string.smartisan_catalog_parent_selection),
      new CatalogEntry(COMPONENT_SDK_PROGRESS, R.string.smartisan_catalog_sdk_progress,
          R.string.smartisan_catalog_impl_platform, R.string.smartisan_catalog_parent_progress),
      new CatalogEntry(COMPONENT_SEEKBAR, R.string.smartisan_catalog_seekbar,
          R.string.smartisan_catalog_impl_platform, R.string.smartisan_catalog_parent_seekbar),
      new CatalogEntry(COMPONENT_EDIT_TEXTS, R.string.smartisan_catalog_edit_texts,
          R.string.smartisan_catalog_impl_platform, R.string.smartisan_catalog_parent_edittext),
      new CatalogEntry(COMPONENT_TIPS, R.string.smartisan_catalog_tips,
          R.string.smartisan_catalog_impl_platform, R.string.smartisan_catalog_parent_textview)
    },
    {
      new CatalogEntry(COMPONENT_SDK_SPINNER, R.string.smartisan_catalog_sdk_spinner,
          R.string.smartisan_catalog_impl_compound,
          R.string.smartisan_catalog_parent_relative_layout),
      new CatalogEntry(COMPONENT_SEGMENTED, R.string.smartisan_catalog_segmented,
          R.string.smartisan_catalog_impl_compound, R.string.smartisan_catalog_parent_viewgroup),
      new CatalogEntry(COMPONENT_LIST_ITEM, R.string.smartisan_catalog_list_item,
          R.string.smartisan_catalog_impl_compound, R.string.smartisan_catalog_parent_viewgroup),
      new CatalogEntry(COMPONENT_SETTING_ITEM, R.string.smartisan_catalog_setting_item,
          R.string.smartisan_catalog_impl_compound, R.string.smartisan_catalog_parent_viewgroup),
      new CatalogEntry(COMPONENT_SEARCH_BAR, R.string.smartisan_catalog_search_bar,
          R.string.smartisan_catalog_impl_compound, R.string.smartisan_catalog_parent_viewgroup),
      new CatalogEntry(COMPONENT_EDITORS, R.string.smartisan_catalog_editors,
          R.string.smartisan_catalog_impl_compound, R.string.smartisan_catalog_parent_viewgroup),
      new CatalogEntry(COMPONENT_TITLE_BAR, R.string.smartisan_catalog_title_bar,
          R.string.smartisan_catalog_impl_compound, R.string.smartisan_catalog_parent_viewgroup),
      new CatalogEntry(COMPONENT_BOTTOM_BARS, R.string.smartisan_catalog_bottom_bars,
          R.string.smartisan_catalog_impl_compound, R.string.smartisan_catalog_parent_viewgroup),
      new CatalogEntry(COMPONENT_BUTTON_GROUPS, R.string.smartisan_catalog_button_groups,
          R.string.smartisan_catalog_impl_compound, R.string.smartisan_catalog_parent_viewgroup),
      new CatalogEntry(COMPONENT_SLIDER, R.string.smartisan_catalog_slider,
          R.string.smartisan_catalog_impl_compound, R.string.smartisan_catalog_parent_viewgroup),
      new CatalogEntry(COMPONENT_SNACKBARS, R.string.smartisan_catalog_snackbars,
          R.string.smartisan_catalog_impl_compound, R.string.smartisan_catalog_parent_viewgroup),
      new CatalogEntry(COMPONENT_EMPTY_VIEW, R.string.smartisan_catalog_empty_view,
          R.string.smartisan_catalog_impl_compound, R.string.smartisan_catalog_parent_viewgroup)
    },
    {
      new CatalogEntry(COMPONENT_SWITCH, R.string.smartisan_catalog_switch,
          R.string.smartisan_catalog_impl_custom, R.string.smartisan_catalog_parent_checkbox),
      new CatalogEntry(COMPONENT_DRAWN_PROGRESS, R.string.smartisan_catalog_drawn_progress,
          R.string.smartisan_catalog_impl_custom, R.string.smartisan_catalog_parent_view)
    },
    {
      new CatalogEntry(COMPONENT_ALERT_CONFIRM, R.string.smartisan_catalog_alert_confirm,
          R.string.smartisan_catalog_impl_overlay, R.string.smartisan_catalog_parent_dialog),
      new CatalogEntry(COMPONENT_ALERT_LIST, R.string.smartisan_catalog_alert_list,
          R.string.smartisan_catalog_impl_overlay, R.string.smartisan_catalog_parent_dialog),
      new CatalogEntry(COMPONENT_ALERT_CHOICES, R.string.smartisan_catalog_alert_choices,
          R.string.smartisan_catalog_impl_overlay, R.string.smartisan_catalog_parent_dialog),
      new CatalogEntry(COMPONENT_ALERT_INPUT, R.string.smartisan_catalog_alert_input,
          R.string.smartisan_catalog_impl_overlay, R.string.smartisan_catalog_parent_dialog),
      new CatalogEntry(COMPONENT_MENU_DIALOG, R.string.smartisan_catalog_menu_dialog_page,
          R.string.smartisan_catalog_impl_overlay, R.string.smartisan_catalog_parent_dialog),
      new CatalogEntry(COMPONENT_DIALOG_PATTERNS, R.string.smartisan_catalog_dialog_patterns,
          R.string.smartisan_catalog_impl_compound, R.string.smartisan_catalog_parent_viewgroup),
      new CatalogEntry(COMPONENT_PICKERS, R.string.smartisan_catalog_pickers,
          R.string.smartisan_catalog_impl_overlay, R.string.smartisan_catalog_parent_dialog),
      new CatalogEntry(COMPONENT_PROGRESS_DIALOG, R.string.smartisan_catalog_progress_page,
          R.string.smartisan_catalog_impl_overlay, R.string.smartisan_catalog_parent_dialog),
      new CatalogEntry(COMPONENT_ANCHOR_MENU, R.string.smartisan_catalog_anchor_menu,
          R.string.smartisan_catalog_impl_overlay, R.string.smartisan_catalog_parent_popup),
      new CatalogEntry(COMPONENT_GRID_MENU, R.string.smartisan_catalog_grid_menu,
          R.string.smartisan_catalog_impl_overlay, R.string.smartisan_catalog_parent_popup),
      new CatalogEntry(COMPONENT_BOTTOM_MENU, R.string.smartisan_catalog_bottom_menu,
          R.string.smartisan_catalog_impl_overlay, R.string.smartisan_catalog_parent_popup)
    }
  };

  private static final int[] CATALOG_GROUP_TITLES = {
    R.string.smartisan_catalog_group_sdk,
    R.string.smartisan_catalog_group_compound,
    R.string.smartisan_catalog_group_custom,
    R.string.smartisan_catalog_group_overlays
  };

  private final java.util.List<SmartisanSwitch> switchSamples = new java.util.ArrayList<>();
  private boolean darkSwitchStyle;

  private static final class CatalogEntry {
    final String id;
    final int title;
    final int implementation;
    final int platformSuperclass;

    CatalogEntry(String id, int title, int implementation, int platformSuperclass) {
      this.id = id;
      this.title = title;
      this.implementation = implementation;
      this.platformSuperclass = platformSuperclass;
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
    addComponentMetadata(content, entry);
    addComponentSamples(content, component);
    setContentView(root);
  }

  private LinearLayout createPageRoot() {
    LinearLayout root = new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setBackgroundResource(R.drawable.smartisan_catalog_settings_background);
    Window window = getWindow();
    window.setStatusBarColor(getColor(R.color.smartisan_catalog_system_bar));
    window.setNavigationBarColor(getColor(R.color.smartisan_catalog_page_background));
    if (!isNightMode()) {
      if (android.os.Build.VERSION.SDK_INT >= 26) {
        window.getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
      } else if (android.os.Build.VERSION.SDK_INT >= 23) {
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
      }
    } else {
      window.getDecorView().setSystemUiVisibility(0);
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
    if (isNightMode()) {
      titleBar.setBackgroundColor(getColor(R.color.smartisan_catalog_system_bar));
      titleBar.setCenterTextColor(getColor(R.color.smartisan_catalog_primary_text));
    }
    if (showBack) {
      android.widget.ImageView back = titleBar.addLeftImageView(SmartisanTitleBar.BACK_ICON_RES);
      if (isNightMode()) {
        back.setColorFilter(getColor(R.color.smartisan_catalog_primary_text));
      }
      back.setOnClickListener(view -> finish());
    }
    return titleBar;
  }

  private LinearLayout createScrollContent(
      LinearLayout root, int left, int top, int right, int bottom) {
    ScrollView scroll = new ScrollView(this);
    scroll.setFillViewport(true);
    scroll.setClipToPadding(false);
    scroll.setBackgroundResource(R.drawable.smartisan_catalog_settings_background);
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

  private boolean isNightMode() {
    return (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
        == Configuration.UI_MODE_NIGHT_YES;
  }

  private void addCatalogGroup(LinearLayout parent, int heading, CatalogEntry[] entries) {
    addSection(parent, heading);
    for (int i = 0; i < entries.length; i++) {
      CatalogEntry entry = entries[i];
      SmartisanListContentItemText item = new SmartisanListContentItemText(this);
      item.setTitle(entry.title);
      item.setBackgroundStyle(groupBackgroundStyle(i, entries.length));
      item.setShadowShouldProjects(false);
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
      case COMPONENT_SLIDER -> addSliderWithIcons(content);
      case COMPONENT_SEEKBAR -> addSmoothSeekBar(content);
      case COMPONENT_DRAWN_PROGRESS -> addDrawnProgress(content);
      case COMPONENT_EDIT_TEXTS -> addEditTexts(content);
      case COMPONENT_SNACKBARS -> addSnackbars(content);
      case COMPONENT_ALERT_CONFIRM -> addAlertConfirm(content);
      case COMPONENT_ALERT_LIST -> addAlertList(content);
      case COMPONENT_ALERT_CHOICES -> addAlertChoices(content);
      case COMPONENT_ALERT_INPUT -> addAlertInput(content);
      case COMPONENT_MENU_DIALOG -> addMenuDialogs(content);
      case COMPONENT_DIALOG_PATTERNS -> addDialogPatterns(content);
      case COMPONENT_PICKERS -> addPickers(content);
      case COMPONENT_PROGRESS_DIALOG -> addProgressDialogs(content);
      case COMPONENT_ANCHOR_MENU -> addAnchorMenus(content);
      case COMPONENT_GRID_MENU -> addGridMenus(content);
      case COMPONENT_BOTTOM_MENU -> addBottomMenus(content);
      case COMPONENT_TIPS -> addTips(content);
      case COMPONENT_EMPTY_VIEW -> addEmptyView(content);
      case COMPONENT_SDK_SELECTION -> addSdkSelectionControls(content);
      case COMPONENT_SDK_PROGRESS -> addSdkProgressControls(content);
      case COMPONENT_SDK_SPINNER -> addSdkSpinnerControls(content);
      default -> throw new IllegalArgumentException("Unknown component: " + component);
    }
  }

  private void addSection(LinearLayout parent, int text) {
    TextView view = new TextView(this);
    view.setText(text);
    view.setTextColor(getColor(R.color.smartisan_catalog_secondary_text));
    view.setTextSize(14);
    view.setGravity(Gravity.BOTTOM);
    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52));
    params.bottomMargin = dp(10);
    parent.addView(view, params);
  }

  private void addComponentMetadata(LinearLayout parent, CatalogEntry entry) {
    TextView metadata = new TextView(this);
    metadata.setText(
        getString(
            R.string.smartisan_catalog_component_metadata,
            getString(entry.implementation),
            getString(entry.platformSuperclass)));
    metadata.setTextColor(getColor(R.color.smartisan_catalog_secondary_text));
    metadata.setTextSize(14);
    metadata.setLineSpacing(0, 1.2f);
    metadata.setPadding(dp(12), dp(12), dp(12), dp(12));
    metadata.setBackgroundColor(getColor(R.color.smartisan_catalog_metadata_surface));
    addRow(parent, metadata);
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

  private void addEditTexts(LinearLayout parent) {
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
  }

  private void addEditors(LinearLayout parent) {
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

  private void addSliderWithIcons(LinearLayout parent) {
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
  }

  private void addSmoothSeekBar(LinearLayout parent) {
    SmartisanSmoothSeekBar smooth = new SmartisanSmoothSeekBar(this);
    smooth.setMax(100);
    smooth.setProgress(20);
    addRow(parent, smooth);

    LinearLayout controls = newRow();
    SmartisanButton animate =
        button(SmartisanButton.STYLE_NORMAL, R.string.smartisan_catalog_smooth_seek);
    animate.setOnClickListener(
        view -> smooth.setProgressSmooth(smooth.getProgress() >= 50 ? 15 : 85));
    controls.addView(animate);
    addRow(parent, controls);
  }

  private void addDrawnProgress(LinearLayout parent) {
    LinearLayout controls = newRow();
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
    controls.addView(showPopup);
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

  private CharSequence[] dialogItems() {
    return new CharSequence[] {
      getText(R.string.smartisan_catalog_item_one),
      getText(R.string.smartisan_catalog_item_two),
      getText(R.string.smartisan_catalog_item_three)
    };
  }

  private void addAlertConfirm(LinearLayout parent) {
    addSection(parent, R.string.smartisan_catalog_alert_basic);
    LinearLayout firstRow = newRow();
    SmartisanButton noTitle =
        button(SmartisanButton.STYLE_NORMAL, R.string.smartisan_catalog_alert_no_title);
    noTitle.setOnClickListener(view -> new SmartisanAlertDialog.Builder(this)
        .setMessage(R.string.smartisan_catalog_alert_short_message)
        .setPositiveButton(R.string.smartisan_catalog_got_it, null)
        .show());
    SmartisanButton twoButtons =
        button(SmartisanButton.STYLE_HIGHLIGHT_BLUE, R.string.smartisan_catalog_alert_two_buttons);
    twoButtons.setOnClickListener(view -> new SmartisanAlertDialog.Builder(this)
        .setTitle(R.string.smartisan_catalog_confirm_title)
        .setMessage(R.string.smartisan_catalog_confirm_message)
        .setNegativeButton(R.string.smartisan_catalog_cancel, null)
        .setPositiveButton(R.string.smartisan_catalog_confirm, null)
        .show());
    firstRow.addView(noTitle);
    firstRow.addView(twoButtons, spacedWrapParams());
    addRow(parent, firstRow);

    LinearLayout secondRow = newRow();
    SmartisanButton threeButtons =
        button(SmartisanButton.STYLE_NORMAL, R.string.smartisan_catalog_alert_three_buttons);
    threeButtons.setOnClickListener(view -> new SmartisanAlertDialog.Builder(this)
        .setTitle(R.string.smartisan_catalog_confirm_title)
        .setMessage(R.string.smartisan_catalog_alert_short_message)
        .setNeutralButton(R.string.smartisan_catalog_later, null)
        .setNegativeButton(R.string.smartisan_catalog_cancel, null)
        .setPositiveButton(R.string.smartisan_catalog_confirm, null)
        .show());
    SmartisanButton longText =
        button(SmartisanButton.STYLE_NORMAL, R.string.smartisan_catalog_alert_long_text);
    longText.setOnClickListener(view -> new SmartisanAlertDialog.Builder(this)
        .setTitle(R.string.smartisan_catalog_alert_long_text)
        .setMessage(R.string.smartisan_catalog_alert_long_message)
        .setNegativeButton(R.string.smartisan_catalog_cancel, null)
        .setPositiveButton(R.string.smartisan_catalog_confirm, null)
        .show());
    secondRow.addView(threeButtons);
    secondRow.addView(longText, spacedWrapParams());
    addRow(parent, secondRow);
  }

  private void addAlertList(LinearLayout parent) {
    SmartisanButton open =
        button(SmartisanButton.STYLE_NORMAL, R.string.smartisan_catalog_open_list);
    open.setOnClickListener(view -> new SmartisanAlertDialog.Builder(this)
        .setTitle(R.string.smartisan_catalog_alert_list)
        .setItems(dialogItems(), (dialog, which) -> showSelection(which))
        .show());
    addRow(parent, open);
  }

  private void addAlertChoices(LinearLayout parent) {
    LinearLayout row = newRow();
    SmartisanButton single =
        button(SmartisanButton.STYLE_NORMAL, R.string.smartisan_catalog_open_single_choice);
    single.setOnClickListener(view -> new SmartisanAlertDialog.Builder(this)
        .setTitle(R.string.smartisan_catalog_single_choice)
        .setSingleChoiceItems(dialogItems(), 1, (dialog, which) -> showSelection(which))
        .setNegativeButton(R.string.smartisan_catalog_cancel, null)
        .setPositiveButton(R.string.smartisan_catalog_confirm, null)
        .show());
    SmartisanButton multiple =
        button(SmartisanButton.STYLE_NORMAL, R.string.smartisan_catalog_open_multi_choice);
    multiple.setOnClickListener(view -> new SmartisanAlertDialog.Builder(this)
        .setTitle(R.string.smartisan_catalog_multi_choice)
        .setMultiChoiceItems(dialogItems(), new boolean[] {true, false, true},
            (dialog, which, checked) -> { })
        .setNegativeButton(R.string.smartisan_catalog_cancel, null)
        .setPositiveButton(R.string.smartisan_catalog_confirm, null)
        .show());
    row.addView(single);
    row.addView(multiple, spacedWrapParams());
    addRow(parent, row);
  }

  private void addAlertInput(LinearLayout parent) {
    SmartisanButton inputButton =
        button(SmartisanButton.STYLE_HIGHLIGHT_BLUE, R.string.smartisan_catalog_open_input);
    inputButton.setOnClickListener(view -> {
      View content = LayoutInflater.from(this).inflate(
          org.opensmartisanos.ui.R.layout.smartisan_rom_dialog_pattern_edit_text, null);
      EditText input = content.findViewById(
          org.opensmartisanos.ui.R.id.smartisan_rom_dialog_pattern_edit_text);
      input.setSingleLine(true);
      input.setHint(R.string.smartisan_catalog_input_hint);
      input.requestFocus();
      SmartisanAlertDialog dialog = new SmartisanAlertDialog.Builder(this)
          .setTitle(R.string.smartisan_catalog_input_title)
          .setView(content)
          .setNegativeButton(R.string.smartisan_catalog_cancel, null)
          .setPositiveButton(R.string.smartisan_catalog_confirm, null)
          .create();
      Window window = dialog.getWindow();
      if (window != null) {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
            | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
      }
      dialog.setOnShowListener(shown -> {
        android.widget.Button positive =
            dialog.getButton(SmartisanAlertDialog.BUTTON_POSITIVE);
        positive.setEnabled(!input.getText().toString().trim().isEmpty());
        input.addTextChangedListener(new TextWatcher() {
          @Override public void beforeTextChanged(
              CharSequence value, int start, int count, int after) { }

          @Override public void onTextChanged(
              CharSequence value, int start, int before, int count) { }

          @Override public void afterTextChanged(Editable value) {
            positive.setEnabled(!value.toString().trim().isEmpty());
          }
        });
        input.setSelection(0, input.length());
      });
      dialog.show();
    });
    addRow(parent, inputButton);
  }

  private void addMenuDialogs(LinearLayout parent) {
    addSection(parent, R.string.smartisan_catalog_menu_length);
    LinearLayout row = newRow();
    SmartisanButton shortMenu =
        button(SmartisanButton.STYLE_NORMAL, R.string.smartisan_catalog_short_menu);
    shortMenu.setOnClickListener(view -> showMenuDialog(
        SmartisanMenuDialog.LOCATION_APP_BOTTOM, 3,
        SmartisanShadowButton.LongButtonStyle.HIGH_LIGHT));
    SmartisanButton longMenu =
        button(SmartisanButton.STYLE_NORMAL, R.string.smartisan_catalog_long_menu);
    longMenu.setOnClickListener(view -> showMenuDialog(
        SmartisanMenuDialog.LOCATION_APP_CENTER, 8,
        SmartisanShadowButton.LongButtonStyle.HIGH_LIGHT));
    row.addView(shortMenu);
    row.addView(longMenu, spacedWrapParams());
    addRow(parent, row);

    LinearLayout styleRow = newRow();
    SmartisanButton red =
        button(SmartisanButton.STYLE_HIGHLIGHT_RED, R.string.smartisan_catalog_red_action);
    red.setOnClickListener(view -> showMenuDialog(
        SmartisanMenuDialog.LOCATION_APP_BOTTOM, 4,
        SmartisanShadowButton.LongButtonStyle.RED));
    SmartisanButton displayCenter =
        button(SmartisanButton.STYLE_NORMAL, R.string.smartisan_catalog_display_center);
    displayCenter.setOnClickListener(view -> showMenuDialog(
        SmartisanMenuDialog.LOCATION_DISPLAY_CENTER, 4,
        SmartisanShadowButton.LongButtonStyle.HIGH_LIGHT));
    styleRow.addView(red);
    styleRow.addView(displayCenter, spacedWrapParams());
    addRow(parent, styleRow);

    LinearLayout structureRow = newRow();
    SmartisanButton listOnly =
        button(SmartisanButton.STYLE_NORMAL, R.string.smartisan_catalog_list_only_menu);
    listOnly.setOnClickListener(view -> showOriginalMenuDialog(
        SmartisanMenuDialog.LOCATION_APP_BOTTOM, 3, null, false));
    SmartisanButton primaryOnly =
        button(SmartisanButton.STYLE_NORMAL, R.string.smartisan_catalog_primary_only_menu);
    primaryOnly.setOnClickListener(view -> showPrimaryOnlyMenuDialog());
    structureRow.addView(listOnly);
    structureRow.addView(primaryOnly, spacedWrapParams());
    addRow(parent, structureRow);

    LinearLayout legacyRow = newRow();
    SmartisanButton legacy =
        button(SmartisanButton.STYLE_NORMAL, R.string.smartisan_catalog_legacy_menu);
    legacy.setOnClickListener(view -> showLegacyMenuDialog());
    SmartisanButton recent =
        button(SmartisanButton.STYLE_NORMAL, R.string.smartisan_catalog_recent_menu);
    recent.setOnClickListener(view -> showOriginalMenuDialog(
        SmartisanMenuDialog.LOCATION_APP_BOTTOM, 4, null, true));
    legacyRow.addView(legacy);
    legacyRow.addView(recent, spacedWrapParams());
    addRow(parent, legacyRow);

    SmartisanButton gray =
        button(SmartisanButton.STYLE_NORMAL, R.string.smartisan_catalog_gray_action);
    gray.setOnClickListener(view -> showMenuDialog(
        SmartisanMenuDialog.LOCATION_APP_BOTTOM, 4,
        SmartisanShadowButton.LongButtonStyle.GRAY));
    addRow(parent, gray);
  }

  private void showMenuDialog(int location, int count,
      SmartisanShadowButton.LongButtonStyle style) {
    showOriginalMenuDialog(location, count, style, false);
  }

  private void showOriginalMenuDialog(int location, int count,
      SmartisanShadowButton.LongButtonStyle style, boolean recentCall) {
    List<String> values = new ArrayList<>();
    List<View.OnClickListener> listeners = new ArrayList<>();
    for (int i = 1; i <= count; i++) {
      values.add(getString(R.string.smartisan_catalog_numbered_item, i));
      int position = i - 1;
      listeners.add(view -> showSelection(position));
    }
    SmartisanMenuDialog dialog = new SmartisanMenuDialog(this, location);
    dialog.setTitle(R.string.smartisan_catalog_menu_dialog_page);
    dialog.setAdapter(new SmartisanMenuDialogListAdapter(
        this, values, listeners, recentCall));
    if (style != null) {
      dialog.setPositiveButton(style == SmartisanShadowButton.LongButtonStyle.RED
          ? R.string.smartisan_catalog_delete
          : R.string.smartisan_catalog_confirm, ignored -> { });
      dialog.setPositiveBgStyle(style);
    }
    dialog.show();
  }

  private void showPrimaryOnlyMenuDialog() {
    SmartisanMenuDialog dialog = new SmartisanMenuDialog(this);
    dialog.setTitle(R.string.smartisan_catalog_confirm_title);
    dialog.setPositiveButton(R.string.smartisan_catalog_delete, ignored -> { });
    dialog.setPositiveRedBg(true);
    dialog.show();
  }

  private void showLegacyMenuDialog() {
    List<String> values = new ArrayList<>();
    for (int i = 1; i <= 6; i++) {
      values.add(getString(R.string.smartisan_catalog_numbered_item, i));
    }
    SmartisanMenuDialog dialog = new SmartisanMenuDialog(this);
    dialog.setTitle(R.string.smartisan_catalog_legacy_menu);
    dialog.setAdaper(new SmartisanMenuDialogMultiAdapter(this, values),
        (parentView, item, position, id) -> {
          showSelection(position);
          dialog.dismiss();
        });
    dialog.show();
  }

  private void addPickers(LinearLayout parent) {
    Calendar now = Calendar.getInstance();

    addSection(parent, R.string.smartisan_catalog_standard_pickers);
    LinearLayout row = newRow();
    SmartisanButton eventDate =
        button(SmartisanButton.STYLE_NORMAL, R.string.smartisan_catalog_event_date);
    eventDate.setOnClickListener(view -> new SmartisanDatePickerDialog(this,
        (picker, year, month, day) -> Toast.makeText(this,
            getString(R.string.smartisan_catalog_date_result, year, month + 1, day),
            Toast.LENGTH_SHORT).show(), now.get(Calendar.YEAR), now.get(Calendar.MONTH),
        now.get(Calendar.DAY_OF_MONTH), SmartisanDatePicker.DatePickerType.EVENT).show());
    SmartisanButton birthday =
        button(SmartisanButton.STYLE_NORMAL, R.string.smartisan_catalog_birthday_without_year);
    birthday.setOnClickListener(view -> new SmartisanDatePickerDialog(this,
        (picker, year, month, day) -> Toast.makeText(this,
            getString(R.string.smartisan_catalog_birthday_result, month + 1, day),
            Toast.LENGTH_SHORT).show(), 4, now.get(Calendar.MONTH),
        now.get(Calendar.DAY_OF_MONTH), SmartisanDatePicker.DatePickerType.BIRTHDAY).show());
    row.addView(eventDate);
    row.addView(birthday, spacedWrapParams());
    addRow(parent, row);

    SmartisanButton lunarBirthday =
        button(SmartisanButton.STYLE_NORMAL, R.string.smartisan_catalog_lunar_birthday);
    lunarBirthday.setOnClickListener(view -> new SmartisanDatePickerDialog(this,
        (picker, year, month, day) -> Toast.makeText(this,
            getString(R.string.smartisan_catalog_date_result, year, month + 1, day),
            Toast.LENGTH_SHORT).show(), now.get(Calendar.YEAR), now.get(Calendar.MONTH),
        now.get(Calendar.DAY_OF_MONTH),
        SmartisanDatePicker.DatePickerType.BIRTHDAY_LUNAR).show());
    addRow(parent, lunarBirthday);

    LinearLayout timeRow = newRow();
    SmartisanButton time24 =
        button(SmartisanButton.STYLE_NORMAL, R.string.smartisan_catalog_time_24_hour);
    time24.setOnClickListener(view -> {
      SmartisanTimePickerDialog dialog = new SmartisanTimePickerDialog(this,
        (picker, hour, minute) -> Toast.makeText(this,
            getString(R.string.smartisan_catalog_time_result, hour, minute),
            Toast.LENGTH_SHORT).show(), now.get(Calendar.HOUR_OF_DAY),
        now.get(Calendar.MINUTE), true);
      dialog.setTitle(R.string.smartisan_catalog_open_time_picker);
      dialog.show();
    });
    SmartisanButton time12 =
        button(SmartisanButton.STYLE_NORMAL, R.string.smartisan_catalog_time_12_hour);
    time12.setOnClickListener(view -> {
      SmartisanTimePickerDialog dialog = new SmartisanTimePickerDialog(this,
          (picker, hour, minute) -> Toast.makeText(this,
              getString(R.string.smartisan_catalog_time_result, hour, minute),
              Toast.LENGTH_SHORT).show(), now.get(Calendar.HOUR_OF_DAY),
          now.get(Calendar.MINUTE), false);
      dialog.setTitle(R.string.smartisan_catalog_open_time_picker);
      dialog.show();
    });
    timeRow.addView(time24);
    timeRow.addView(time12, spacedWrapParams());
    addRow(parent, timeRow);

    addSection(parent, R.string.smartisan_catalog_extended_pickers);
    LinearLayout extendedRow = newRow();
    SmartisanButton extendedDate =
        button(SmartisanButton.STYLE_NORMAL, R.string.smartisan_catalog_extended_event_date);
    extendedDate.setOnClickListener(view -> new SmartisanDatePickerExDialog(this,
        (picker, year, month, day) -> Toast.makeText(this,
            getString(R.string.smartisan_catalog_date_result, year, month + 1, day),
            Toast.LENGTH_SHORT).show(), now.get(Calendar.YEAR), now.get(Calendar.MONTH),
        now.get(Calendar.DAY_OF_MONTH), SmartisanDatePickerEx.DatePickerType.EVENT).show());
    SmartisanButton extendedBirthday =
        button(SmartisanButton.STYLE_NORMAL,
            R.string.smartisan_catalog_extended_birthday_without_year);
    extendedBirthday.setOnClickListener(view -> new SmartisanDatePickerExDialog(this,
        (picker, year, month, day) -> Toast.makeText(this,
            getString(R.string.smartisan_catalog_birthday_result, month + 1, day),
            Toast.LENGTH_SHORT).show(), 4, now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH),
        SmartisanDatePickerEx.DatePickerType.BIRTHDAY).show());
    extendedRow.addView(extendedDate);
    extendedRow.addView(extendedBirthday, spacedWrapParams());
    addRow(parent, extendedRow);

    SmartisanButton extendedLunarBirthday =
        button(SmartisanButton.STYLE_NORMAL,
            R.string.smartisan_catalog_extended_lunar_birthday);
    extendedLunarBirthday.setOnClickListener(view -> new SmartisanDatePickerExDialog(this,
        (picker, year, month, day) -> Toast.makeText(this,
            getString(R.string.smartisan_catalog_date_result, year, month + 1, day),
            Toast.LENGTH_SHORT).show(), now.get(Calendar.YEAR), now.get(Calendar.MONTH),
        now.get(Calendar.DAY_OF_MONTH),
        SmartisanDatePickerEx.DatePickerType.BIRTHDAY_LUNAR).show());
    addRow(parent, extendedLunarBirthday);

    LinearLayout extendedTimeRow = newRow();
    SmartisanButton extendedTime24 =
        button(SmartisanButton.STYLE_NORMAL, R.string.smartisan_catalog_extended_time_24_hour);
    extendedTime24.setOnClickListener(view -> new SmartisanTimePickerExDialog(this,
        (picker, hour, minute) -> Toast.makeText(this,
            getString(R.string.smartisan_catalog_time_result, hour, minute), Toast.LENGTH_SHORT).show(),
        now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show());
    SmartisanButton extendedTime12 =
        button(SmartisanButton.STYLE_NORMAL, R.string.smartisan_catalog_extended_time_12_hour);
    extendedTime12.setOnClickListener(view -> new SmartisanTimePickerExDialog(this,
        (picker, hour, minute) -> Toast.makeText(this,
            getString(R.string.smartisan_catalog_time_result, hour, minute), Toast.LENGTH_SHORT).show(),
        now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false).show());
    extendedTimeRow.addView(extendedTime24);
    extendedTimeRow.addView(extendedTime12, spacedWrapParams());
    addRow(parent, extendedTimeRow);

    addSection(parent, R.string.smartisan_catalog_combined_picker);
    SmartisanButton dateTime =
        button(SmartisanButton.STYLE_HIGHLIGHT_BLUE,
            R.string.smartisan_catalog_current_to_2037);
    dateTime.setOnClickListener(view -> new SmartisanDateTimePickerDialog(this,
        value -> Toast.makeText(this,
            android.text.format.DateFormat.getDateFormat(this).format(value) + " "
                + android.text.format.DateFormat.getTimeFormat(this).format(value),
            Toast.LENGTH_SHORT).show(), now.getTimeInMillis()).show());
    addRow(parent, dateTime);
  }

  private void addDialogPatterns(LinearLayout parent) {
    SmartisanButton appInfo =
        button(SmartisanButton.STYLE_NORMAL, R.string.smartisan_catalog_pattern_app_info);
    appInfo.setOnClickListener(view -> {
      SmartisanDialogPatternAppInfoLayout content = new SmartisanDialogPatternAppInfoLayout(this);
      content.setIcon(android.R.drawable.sym_def_app_icon);
      content.setTitle(R.string.smartisan_catalog_pattern_app_title);
      content.setSummary(R.string.smartisan_catalog_pattern_app_summary);
      new SmartisanAlertDialog.Builder(this)
          .setTitle(R.string.smartisan_catalog_dialog_patterns)
          .setView(content)
          .setPositiveButton(R.string.smartisan_catalog_got_it, null)
          .show();
    });
    addRow(parent, appInfo);

    SmartisanButton section =
        button(SmartisanButton.STYLE_NORMAL, R.string.smartisan_catalog_pattern_section);
    section.setOnClickListener(view -> {
      SmartisanDialogPatternSectionGroup content = new SmartisanDialogPatternSectionGroup(this);
      content.setPrimaryTitle(R.string.smartisan_catalog_pattern_section_title);
      content.setSubtitle(R.string.smartisan_catalog_pattern_section_subtitle);
      content.setMessage(R.string.smartisan_catalog_pattern_section_message);
      new SmartisanAlertDialog.Builder(this).setView(content)
          .setNegativeButton(R.string.smartisan_catalog_cancel, null)
          .setPositiveButton(R.string.smartisan_catalog_confirm, null).show();
    });
    addRow(parent, section);

    SmartisanButton choice =
        button(SmartisanButton.STYLE_NORMAL, R.string.smartisan_catalog_pattern_two_line_choice);
    choice.setOnClickListener(view -> {
      SmartisanDialogPatternTwoLineSingleChoice content =
          new SmartisanDialogPatternTwoLineSingleChoice(this);
      content.setTitle(R.string.smartisan_catalog_item_one);
      content.setSummary(R.string.smartisan_catalog_pattern_choice_summary);
      content.setChecked(true);
      new SmartisanAlertDialog.Builder(this).setView(content)
          .setPositiveButton(R.string.smartisan_catalog_confirm, null).show();
    });
    addRow(parent, choice);
  }

  private void addProgressDialogs(LinearLayout parent) {
    SmartisanButton progress =
        button(SmartisanButton.STYLE_HIGHLIGHT_BLUE, R.string.smartisan_catalog_progress_dialog);
    progress.setOnClickListener(view -> {
      SmartisanProgressDialog dialog = new SmartisanProgressDialog(this);
      dialog.setTitle(R.string.smartisan_catalog_loading);
      dialog.setMessage(R.string.smartisan_catalog_loading_message);
      dialog.setDarkTheme((getResources().getConfiguration().uiMode
          & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
          == android.content.res.Configuration.UI_MODE_NIGHT_YES);
      dialog.show();
      dialog.setProgress(45);
      dialog.setCanceledOnTouchOutside(true);
    });
    addRow(parent, progress);
  }

  private void addAnchorMenus(LinearLayout parent) {
    addSection(parent, R.string.smartisan_catalog_four_directions);
    LinearLayout firstRow = newRow();
    firstRow.addView(anchorButton(R.string.smartisan_catalog_direction_top,
        org.opensmartisanos.ui.widget.SmartisanPopupMenu.ARROW_TOP));
    firstRow.addView(anchorButton(R.string.smartisan_catalog_direction_bottom,
        org.opensmartisanos.ui.widget.SmartisanPopupMenu.ARROW_BOTTOM), spacedWrapParams());
    addRow(parent, firstRow);
    LinearLayout secondRow = newRow();
    secondRow.addView(anchorButton(R.string.smartisan_catalog_direction_left,
        org.opensmartisanos.ui.widget.SmartisanPopupMenu.ARROW_LEFT));
    secondRow.addView(anchorButton(R.string.smartisan_catalog_direction_right,
        org.opensmartisanos.ui.widget.SmartisanPopupMenu.ARROW_RIGHT), spacedWrapParams());
    addRow(parent, secondRow);

    addSection(parent, R.string.smartisan_catalog_anchor_item_styles);
    SmartisanButton removable = button(SmartisanButton.STYLE_NORMAL,
        R.string.smartisan_catalog_anchor_removable);
    removable.setOnClickListener(this::showRemovableAnchorMenu);
    addRow(parent, removable);

    SmartisanButton grouped = button(SmartisanButton.STYLE_NORMAL,
        R.string.smartisan_catalog_anchor_grouped);
    grouped.setOnClickListener(this::showGroupedAnchorMenu);
    addRow(parent, grouped);
  }

  private SmartisanButton anchorButton(int text, int direction) {
    SmartisanButton anchor = button(SmartisanButton.STYLE_NORMAL, text);
    anchor.setOnClickListener(view -> showAnchorMenu(view, direction));
    return anchor;
  }

  private void showAnchorMenu(View anchor, int direction) {
    SmartisanListPopupMenu popup = new SmartisanListPopupMenu(this);
    popup.setAnchorView(anchor);
    popup.setMenuListTitleVisible(true);
    popup.setMenuListTitle(getText(R.string.smartisan_catalog_anchor_menu));
    SmartisanListPopupMenuStandardAdapter adapter =
        new SmartisanListPopupMenuStandardAdapter(this, popupMenuItems()) {
          @Override public boolean isEnabled(int position) {
            SmartisanMenuItem item = getItem(position);
            return !(item instanceof CatalogPopupItem) || ((CatalogPopupItem) item).enabled;
          }
        };
    popup.setAdapter(adapter);
    popup.setOnItemClickListener((parentView, item, position, id) -> popup.dismiss());
    popup.setArrowVisible(true);
    showAnchoredPopup(popup, anchor, direction);
  }

  private void showRemovableAnchorMenu(View anchor) {
    SmartisanListPopupMenu popup = new SmartisanListPopupMenu(this);
    popup.setAnchorView(anchor);
    ArrayList<SmartisanMenuItem> items = popupMenuItems();
    SmartisanListPopupMenuStandardAdapter adapter =
        new SmartisanListPopupMenuStandardAdapter(this, items);
    adapter.setMenuItemStyle(SmartisanListPopupMenuStandardAdapter.STYLE_REMOVABLE);
    adapter.setCloseIconClickListener(close -> {
      Object item = close.getTag();
      if (item instanceof SmartisanMenuItem) {
        adapter.remove((SmartisanMenuItem) item);
        if (adapter.isEmpty()) popup.dismiss();
      }
    });
    popup.setAdapter(adapter);
    popup.setOnItemClickListener((parentView, item, position, id) -> popup.dismiss());
    popup.setArrowVisible(true);
    showAnchoredPopup(popup, anchor,
        org.opensmartisanos.ui.widget.SmartisanPopupMenu.ARROW_TOP);
  }

  private void showGroupedAnchorMenu(View anchor) {
    PopupMenu menuModel = new PopupMenu(this, anchor);
    Menu menu = menuModel.getMenu();
    menu.add(1, 1, 0, R.string.smartisan_catalog_anchor_open)
        .setIcon(android.R.drawable.ic_menu_view);
    menu.add(1, 2, 1, R.string.smartisan_catalog_anchor_share)
        .setIcon(android.R.drawable.ic_menu_share);
    menu.add(2, 3, 2, R.string.smartisan_catalog_anchor_rename)
        .setIcon(android.R.drawable.ic_menu_edit);
    menu.add(2, 4, 3, R.string.smartisan_catalog_anchor_delete)
        .setIcon(android.R.drawable.ic_menu_delete);
    menu.add(2, 5, 4, R.string.smartisan_catalog_disabled)
        .setIcon(android.R.drawable.ic_menu_close_clear_cancel).setEnabled(false);

    SmartisanListPopupMenu popup = new SmartisanListPopupMenu(this);
    popup.setAnchorView(anchor);
    popup.setAdapter(new SmartisanGroupMenuAdapter(menu));
    popup.setContentAreaWidth(getResources().getDimensionPixelOffset(
        org.opensmartisanos.ui.R.dimen.smartisan_rom_popup_list_menu_long_press_width));
    menuModel.setOnMenuItemClickListener(item -> {
      Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
      popup.dismiss();
      return true;
    });
    showAnchoredPopup(popup, anchor,
        org.opensmartisanos.ui.widget.SmartisanPopupMenu.ARROW_TOP);
  }

  private ArrayList<SmartisanMenuItem> popupMenuItems() {
    ArrayList<SmartisanMenuItem> items = new ArrayList<>();
    items.add(new CatalogPopupItem(getString(R.string.smartisan_catalog_anchor_wifi),
        getString(R.string.smartisan_catalog_anchor_connected),
        android.R.drawable.presence_online, true, true));
    items.add(new CatalogPopupItem(getString(R.string.smartisan_catalog_anchor_bluetooth),
        getString(R.string.smartisan_catalog_anchor_available),
        android.R.drawable.stat_sys_data_bluetooth, false, true));
    items.add(new CatalogPopupItem(getString(R.string.smartisan_catalog_anchor_airplane),
        null, android.R.drawable.ic_menu_compass, false, false));
    return items;
  }

  private void showAnchoredPopup(SmartisanListPopupMenu popup, View anchor, int direction) {
    int x = anchor.getWidth() / 2 - popup.getPopupWindowWidth() / 2;
    int y = anchor.getHeight();
    int arrowX = popup.getPopupWindowWidth() / 2 - popup.getLeftRightShadowWidth()
        - popup.getMenuPanelBgRoundCornerRadius();
    if (direction == org.opensmartisanos.ui.widget.SmartisanPopupMenu.ARROW_LEFT) {
      x = anchor.getWidth();
      y = 0;
    } else if (direction == org.opensmartisanos.ui.widget.SmartisanPopupMenu.ARROW_RIGHT) {
      x = -popup.getPopupWindowWidth();
      y = 0;
    } else if (direction == org.opensmartisanos.ui.widget.SmartisanPopupMenu.ARROW_BOTTOM) {
      popup.setMenuPopup(true);
      y = anchor.getHeight();
    }
    popup.show(direction, x, y, arrowX, anchor.getHeight() / 2);
  }

  private static final class CatalogPopupItem extends SmartisanAbsMenuItem {
    private final String title;
    private final String subtitle;
    private final int iconResource;
    private final boolean selected;
    private final boolean enabled;

    CatalogPopupItem(String title, String subtitle, int iconResource,
        boolean selected, boolean enabled) {
      this.title = title;
      this.subtitle = subtitle;
      this.iconResource = iconResource;
      this.selected = selected;
      this.enabled = enabled;
    }

    @Override public String getTitle() { return title; }
    @Override public String getSubtitle() { return subtitle; }
    @Override public boolean isSelected() { return selected; }
    @Override public void setMenuIcon(ImageView imageView) {
      imageView.setImageResource(iconResource);
      imageView.setVisibility(View.VISIBLE);
    }
  }

  private void addGridMenus(LinearLayout parent) {
    SmartisanButton anchor =
        button(SmartisanButton.STYLE_NORMAL, R.string.smartisan_catalog_open_grid_menu);
    anchor.setOnClickListener(view -> {
      SmartisanGridIconPopupMenu popup = new SmartisanGridIconPopupMenu(this, 4);
      popup.setAnchorView(view);
      popup.setIcons(new int[] {
        android.R.drawable.ic_menu_camera, android.R.drawable.ic_menu_compass,
        android.R.drawable.ic_menu_crop, android.R.drawable.ic_menu_directions,
        android.R.drawable.ic_menu_edit, android.R.drawable.ic_menu_gallery,
        android.R.drawable.ic_menu_manage, android.R.drawable.ic_menu_mapmode,
        android.R.drawable.ic_menu_myplaces, android.R.drawable.ic_menu_search,
        android.R.drawable.ic_menu_send, android.R.drawable.ic_menu_share,
        android.R.drawable.ic_menu_slideshow
      });
      popup.setOnMenuItemClickListener(this::showSelection);
      popup.setArrowVisible(true);
      popup.showCenter(view.getHeight(), 0);
    });
    addRow(parent, anchor);
  }

  private void addBottomMenus(LinearLayout parent) {
    SmartisanButton open =
        button(SmartisanButton.STYLE_NORMAL, R.string.smartisan_catalog_open_bottom_menu);
    open.setOnClickListener(view -> {
      PopupMenu menuModel = new PopupMenu(this, view);
      Menu menu = menuModel.getMenu();
      menu.add(0, 1, 0, R.string.smartisan_catalog_share)
          .setIcon(android.R.drawable.ic_menu_share);
      menu.add(0, 2, 1, R.string.smartisan_catalog_edit)
          .setIcon(android.R.drawable.ic_menu_edit);
      menu.add(0, 3, 2, R.string.smartisan_catalog_save)
          .setIcon(android.R.drawable.ic_menu_save);
      menu.add(0, 4, 3, R.string.smartisan_catalog_delete)
          .setIcon(android.R.drawable.ic_menu_delete);
      menu.add(0, 5, 4, R.string.smartisan_catalog_disabled).setEnabled(false)
          .setIcon(android.R.drawable.ic_menu_close_clear_cancel);
      menuModel.setOnMenuItemClickListener(item -> {
        Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
        return true;
      });
      SmartisanBottomMenuPopupWindow popup = new SmartisanBottomMenuPopupWindow(this);
      if (popup.getTitleBar() != null) {
        popup.getTitleBar().setTitle(R.string.smartisan_catalog_bottom_menu);
      }
      int childHeight = getResources().getConfiguration().orientation
          == android.content.res.Configuration.ORIENTATION_LANDSCAPE ? dp(90) : dp(74);
      popup.showBrowserMenu(view.getRootView(), menu, childHeight,
          ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
      popup.setOnItemClickListener((parentView, itemView, position, id) -> {
        android.view.MenuItem item = popup.getAdapter().getItem(position);
        menu.performIdentifierAction(item.getItemId(), 0);
        popup.onClick(itemView);
      });
    });
    addRow(parent, open);
  }

  private void showSelection(int position) {
    Toast.makeText(this, getString(R.string.smartisan_catalog_selected_number, position + 1),
        Toast.LENGTH_SHORT).show();
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

  private void addSdkSelectionControls(LinearLayout parent) {
    addSection(parent, R.string.smartisan_catalog_xml_sample);
    addRow(
        parent,
        LayoutInflater.from(this).inflate(R.layout.catalog_sdk_selection, parent, false));

    addSection(parent, R.string.smartisan_catalog_java_sample);
    LinearLayout checkRow = newRow();
    SmartisanCheckBox checkBox = new SmartisanCheckBox(this);
    checkBox.setId(R.id.catalog_java_checkbox);
    checkBox.setText(R.string.smartisan_catalog_unchecked);
    checkRow.addView(checkBox);
    RadioGroup radioGroup = new RadioGroup(this);
    radioGroup.setOrientation(RadioGroup.HORIZONTAL);
    SmartisanRadioButton first = new SmartisanRadioButton(this);
    first.setId(R.id.catalog_java_radio_first);
    first.setText(R.string.smartisan_catalog_item_one);
    SmartisanRadioButton second = new SmartisanRadioButton(this);
    second.setId(R.id.catalog_java_radio_second);
    second.setText(R.string.smartisan_catalog_item_two);
    radioGroup.addView(first);
    radioGroup.addView(second);
    radioGroup.check(first.getId());
    checkRow.addView(radioGroup, spacedWrapParams());
    addRow(parent, checkRow);
  }

  private void addSdkProgressControls(LinearLayout parent) {
    addSection(parent, R.string.smartisan_catalog_xml_sample);
    addRow(
        parent,
        LayoutInflater.from(this).inflate(R.layout.catalog_sdk_progress, parent, false));

    addSection(parent, R.string.smartisan_catalog_java_sample);
    SmartisanProgressBar progress = new SmartisanProgressBar(this);
    progress.setMax(100);
    progress.setProgress(62);
    progress.setSecondaryProgress(82);
    progress.setProgressTintList(ColorStateList.valueOf(Color.rgb(122, 84, 161)));
    addRow(parent, progress);
    SmartisanCircularProgressBar circular = new SmartisanCircularProgressBar(this);
    addRow(parent, circular);
  }

  private void addSdkSpinnerControls(LinearLayout parent) {
    addSection(parent, R.string.smartisan_catalog_xml_sample);
    View xmlSample = LayoutInflater.from(this).inflate(R.layout.catalog_sdk_spinner, parent, false);
    SmartisanSpinnerView xmlSpinner = xmlSample.findViewById(R.id.catalog_sdk_spinner_dropdown);
    configureSpinnerDropDown(xmlSpinner);
    addRow(parent, xmlSample);

    addSection(parent, R.string.smartisan_catalog_java_sample);
    SmartisanSpinnerView spinner = new SmartisanSpinnerView(this);
    spinner.setId(R.id.catalog_java_spinner);
    spinner.setMinimumHeight(dp(56));
    spinner.setSpinnerStyle(SmartisanSpinnerView.SPINNER_STYLE_DROP);
    spinner.setSpinnerPickText(null, getString(R.string.smartisan_catalog_item_two));
    spinner.setSubContentText(getString(R.string.smartisan_catalog_spinner_current));
    configureSpinnerDropDown(spinner);
    addSpinnerRow(parent, spinner);

    addSection(parent, R.string.smartisan_catalog_spinner_range);
    SmartisanSpinnerView range = new SmartisanSpinnerView(this);
    range.setId(R.id.catalog_java_spinner_range);
    range.setMinimumHeight(dp(56));
    range.setIsNeedVerticalScroll(true);
    range.setSpinnerPickText(new String[] {
        getString(R.string.smartisan_catalog_spinner_range_day),
        getString(R.string.smartisan_catalog_spinner_range_week),
        getString(R.string.smartisan_catalog_spinner_range_month)
    }, null);
    range.setSpinnerStyle(SmartisanSpinnerView.SPINNER_STYLE_RANGE);
    range.setRangeClickListener(new SmartisanSpinnerView.SpinnerRangeClickListener() {
      @Override public void onRangeLeftClick() {
        Toast.makeText(CatalogActivity.this, R.string.smartisan_catalog_previous,
            Toast.LENGTH_SHORT).show();
      }

      @Override public void onRangeRightClick() {
        Toast.makeText(CatalogActivity.this, R.string.smartisan_catalog_next,
            Toast.LENGTH_SHORT).show();
      }
    });
    addSpinnerRow(parent, range);
  }

  private void configureSpinnerDropDown(SmartisanSpinnerView spinner) {
    spinner.setDropDownClickListener(() -> showSpinnerPopup(spinner));
    spinner.setOnClickListener(view -> showSpinnerPopup(spinner));
  }

  private void showSpinnerPopup(SmartisanSpinnerView spinner) {
    SmartisanListPopupMenu popup = new SmartisanListPopupMenu(this);
    popup.setAnchorView(spinner);
    ArrayList<SmartisanMenuItem> items = new ArrayList<>();
    items.add(new SpinnerPopupItem(getString(R.string.smartisan_catalog_item_one)));
    items.add(new SpinnerPopupItem(getString(R.string.smartisan_catalog_item_two)));
    items.add(new SpinnerPopupItem(getString(R.string.smartisan_catalog_item_three)));
    SmartisanListPopupMenuStandardAdapter adapter =
        new SmartisanListPopupMenuStandardAdapter(this, items);
    popup.setAdapter(adapter);
    popup.setOnItemClickListener((parentView, itemView, position, id) -> {
      SmartisanMenuItem selected = adapter.getItem(position);
      if (selected != null) spinner.setSpinnerPickText(null, selected.getTitle());
      popup.dismiss();
    });
    popup.setArrowVisible(true);
    showAnchoredPopup(popup, spinner,
        org.opensmartisanos.ui.widget.SmartisanPopupMenu.ARROW_TOP);
  }

  private static final class SpinnerPopupItem extends SmartisanAbsMenuItem {
    private final String title;

    SpinnerPopupItem(String title) { this.title = title; }

    @Override public String getTitle() { return title; }
    @Override public boolean hasMenuIcon() { return false; }
  }

  private void addSpinnerRow(LinearLayout parent, SmartisanSpinnerView spinner) {
    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56));
    params.bottomMargin = dp(10);
    parent.addView(spinner, params);
  }

  private void addRow(LinearLayout parent, View row) {
    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    params.bottomMargin = dp(10);
    parent.addView(row, params);
  }
}
