package org.opensmartisanos.ui.catalog;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.SparseArray;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.opensmartisanos.ui.R;
import org.opensmartisanos.ui.internal.RomPageIndicator;
import org.opensmartisanos.ui.internal.RomPagedView;
import org.opensmartisanos.ui.app.SmartisanAlertDialog;
import org.opensmartisanos.ui.app.SmartisanMenuDialog;
import org.opensmartisanos.ui.app.SmartisanMenuDialogListAdapter;
import org.opensmartisanos.ui.app.SmartisanProgressDialog;
import org.opensmartisanos.ui.widget.SmartisanBottomMenuPopupWindow;
import org.opensmartisanos.ui.widget.SmartisanBottomMenuAdapter;
import org.opensmartisanos.ui.widget.SmartisanAbsMenuItem;
import org.opensmartisanos.ui.widget.SmartisanDatePicker;
import org.opensmartisanos.ui.widget.SmartisanDatePickerEx;
import org.opensmartisanos.ui.widget.SmartisanDateTimePicker;
import org.opensmartisanos.ui.widget.SmartisanDialogTitleBar;
import org.opensmartisanos.ui.widget.SmartisanGridIconPopupMenu;
import org.opensmartisanos.ui.widget.SmartisanGroupMenuAdapter;
import org.opensmartisanos.ui.widget.SmartisanListPopupMenu;
import org.opensmartisanos.ui.widget.SmartisanListPopupMenuStandardAdapter;
import org.opensmartisanos.ui.widget.SmartisanMenuItem;
import org.opensmartisanos.ui.widget.SmartisanNumberPicker;
import org.opensmartisanos.ui.widget.SmartisanNumberPickerEx;
import org.opensmartisanos.ui.widget.SmartisanPopupMenu;
import org.opensmartisanos.ui.widget.SmartisanPopupMenuLongPressedListItem;
import org.opensmartisanos.ui.widget.SmartisanPopupMenuRemovableListItem;
import org.opensmartisanos.ui.widget.SmartisanPopupMenuStandardListItem;
import org.opensmartisanos.ui.widget.SmartisanSpinnerView;
import org.opensmartisanos.ui.widget.SmartisanTimePicker;
import org.opensmartisanos.ui.widget.SmartisanTimePickerEx;
import org.opensmartisanos.ui.widget.SmartisanWheelTextView;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.Locale;

/** Platform-only instrumentation contract suite; intentionally has no AndroidX dependency. */
public final class CatalogPopupInstrumentation extends Instrumentation {
    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        start();
    }

    @Override
    public void onStart() {
        Bundle result = new Bundle();
        Activity activity = null;
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN)
                    .setClassName(getTargetContext(), CatalogActivity.class.getName())
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity = startActivitySync(intent);
            waitForIdleSync();
            verifyBottomMenu(activity);
            verifyLandscapeResource(activity);
            verifyGridInteraction(activity);
            verifyPopupCoordinates(activity);
            verifyPopupOptionRows(activity);
            verifySpinnerView(activity);
            verifyAlertMessageAndCustomView(activity);
            verifyAlertOriginalContracts(activity);
            verifyAlertInputMethodAndButtonTouch(activity);
            verifyAlertChoiceStateRoundTrip(activity);
            verifyMenuNavigationCallback(activity);
            verifyMenuCenterAndLongList(activity);
            verifyProgressDialogWindow(activity);
            verifyDialogNightRtlAndExternalHierarchy(activity);
            verifyPickerStateRestoration(activity);
            verifyDismissOnlyMenuButton(activity);
            result.putString("stream", "\nSmartisan popup instrumentation: OK\n");
            finish(Activity.RESULT_OK, result);
        } catch (Throwable failure) {
            StringWriter trace = new StringWriter();
            failure.printStackTrace(new PrintWriter(trace));
            result.putString("stream", "\nSmartisan popup instrumentation: FAILED\n" + trace);
            finish(Activity.RESULT_CANCELED, result);
        } finally {
            if (activity != null) {
                Activity finalActivity = activity;
                runOnMainSync(finalActivity::finish);
            }
        }
    }

    private void verifyBottomMenu(Activity activity) {
        AtomicReference<SmartisanBottomMenuPopupWindow> reference = new AtomicReference<>();
        runOnMainSync(() -> {
            View root = activity.getWindow().getDecorView();
            PopupMenu menuModel = new PopupMenu(activity, root);
            Menu menu = menuModel.getMenu();
            menu.add("可用").setIcon(android.R.drawable.ic_menu_add);
            menu.add("禁用").setEnabled(false).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
            menu.add("隐藏").setVisible(false);
            SmartisanBottomMenuPopupWindow popup = new SmartisanBottomMenuPopupWindow(activity);
            int itemHeight = activity.getResources().getConfiguration().orientation
                    == Configuration.ORIENTATION_LANDSCAPE ? dp(activity, 90) : dp(activity, 74);
            popup.showBrowserMenu(root, menu, itemHeight,
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            reference.set(popup);
        });
        waitForIdleSync();
        SmartisanBottomMenuPopupWindow popup = reference.get();
        equal(ViewGroup.LayoutParams.MATCH_PARENT, popup.getWidth(), "bottom menu width");
        equal(ViewGroup.LayoutParams.MATCH_PARENT, popup.getHeight(), "bottom menu height");
        equal(2, popup.getAdapter().getCount(), "visible item filtering");
        if (!popup.getAdapter().isEnabled(0) || popup.getAdapter().isEnabled(1)) {
            throw new AssertionError("bottom menu disabled state was not preserved");
        }
        runOnMainSync(() -> {
            View firstItem = popup.getAdapter().getView(0, null, popup.getGridView());
            int expectedHeight = activity.getResources().getConfiguration().orientation
                    == Configuration.ORIENTATION_LANDSCAPE ? dp(activity, 90) : dp(activity, 74);
            equal(expectedHeight, firstItem.getLayoutParams().height,
                    "bottom menu orientation item height");
        });
        int expectedColumns = activity.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE ? 3 : 4;
        equal(expectedColumns, popup.getGridView().getNumColumns(), "bottom menu columns");
        View content = popup.getContentView();
        View panel = content.findViewById(R.id.smartisan_rom_bottom_menu_parent_panel);
        int[] contentLocation = new int[2];
        int[] panelLocation = new int[2];
        content.getLocationOnScreen(contentLocation);
        panel.getLocationOnScreen(panelLocation);
        if (expectedColumns == 4) {
            equal(contentLocation[1] + content.getHeight(), panelLocation[1] + panel.getHeight(),
                    "portrait bottom menu edge");
        } else {
            equal(contentLocation[0] + content.getWidth(), panelLocation[0] + panel.getWidth(),
                    "landscape bottom menu edge");
        }
        if (content.getRootWindowInsets() != null) {
            @SuppressWarnings("deprecation")
            int bottomInset = content.getRootWindowInsets().getSystemWindowInsetBottom();
            if (panel.getPaddingBottom() < bottomInset) {
                throw new AssertionError("bottom menu did not consume the bottom system inset");
            }
        }
        // R2 intentionally ignores dismiss requests while an entrance/exit animation owns the
        // panel. Animation callbacks depend on draw frames (not Looper idleness), so retry the
        // outside click until the 150 ms entrance has actually completed, then allow the 100 ms
        // exit to finish. This validates eventual dismissal without racing the device compositor.
        for (int attempt = 0; attempt < 15 && popup.isShowing(); attempt++) {
            runOnMainSync(() -> popup.onClick(content));
            SystemClock.sleep(150L);
            waitForIdleSync();
        }
        if (popup.isShowing()) throw new AssertionError("bottom menu leaked its window");

        runOnMainSync(() -> {
            View root = activity.getWindow().getDecorView();
            PopupMenu model = new PopupMenu(activity, root);
            ArrayList<android.view.MenuItem> live = new ArrayList<>();
            live.add(model.getMenu().add("第一项"));
            SmartisanBottomMenuAdapter adapter = new SmartisanBottomMenuAdapter(activity, live);
            equal(1, adapter.getCount(), "bottom menu live list initial size");
            live.add(model.getMenu().add("第二项"));
            adapter.notifyDataSetChanged();
            equal(2, adapter.getCount(), "bottom menu live list updated size");

            Configuration largeFontConfiguration = new Configuration(
                    activity.getResources().getConfiguration());
            largeFontConfiguration.fontScale = 2f;
            Context largeFontContext = activity.createConfigurationContext(
                    largeFontConfiguration);
            PopupMenu largeFontModel = new PopupMenu(largeFontContext,
                    new View(largeFontContext));
            ArrayList<android.view.MenuItem> titledItems = new ArrayList<>();
            titledItems.add(largeFontModel.getMenu().add("这是一个超过六个字的菜单标题"));
            titledItems.add(largeFontModel.getMenu().add("短标题"));
            SmartisanBottomMenuAdapter titledAdapter = new SmartisanBottomMenuAdapter(
                    largeFontContext, titledItems);
            GridView parent = new GridView(largeFontContext);
            View longTitleRow = titledAdapter.getView(0, null, parent);
            TextView title = longTitleRow.findViewById(R.id.smartisan_rom_bottom_menu_text);
            if (title.getTextSize() > dp(largeFontContext, 12) + 0.5f) {
                throw new AssertionError("bottom menu long title exceeded the R2 12dp cap");
            }
            title.setTextColor(Color.MAGENTA);
            View rebound = titledAdapter.getView(1, longTitleRow, parent);
            TextView reboundTitle = rebound.findViewById(R.id.smartisan_rom_bottom_menu_text);
            equal(largeFontContext.getColor(R.color.smartisan_rom_bottom_menu_text),
                    reboundTitle.getCurrentTextColor(), "bottom menu rebound text color");
        });
    }

    private void verifyLandscapeResource(Activity activity) {
        Configuration configuration = new Configuration(
                activity.getResources().getConfiguration());
        configuration.orientation = Configuration.ORIENTATION_LANDSCAPE;
        Context landscape = activity.createConfigurationContext(configuration);
        View root = View.inflate(landscape, R.layout.smartisan_rom_bottom_menu, null);
        View panel = root.findViewById(R.id.smartisan_rom_bottom_menu_parent_panel);
        ViewGroup.LayoutParams raw = panel.getLayoutParams();
        equal(dp(activity, 270), raw.width, "landscape panel width");
        equal(ViewGroup.LayoutParams.MATCH_PARENT, raw.height, "landscape panel height");
        equal(Gravity.END, ((android.widget.FrameLayout.LayoutParams) raw).gravity,
                "landscape panel gravity");
        android.widget.FrameLayout itemParent = new android.widget.FrameLayout(landscape);
        View item = LayoutInflater.from(landscape).inflate(
                R.layout.smartisan_rom_bottom_menu_item, itemParent, false);
        equal(dp(activity, 90), item.getLayoutParams().height,
                "landscape bottom-menu item height");
    }

    private void verifyGridInteraction(Activity activity) {
        AtomicReference<SmartisanGridIconPopupMenu> reference = new AtomicReference<>();
        AtomicInteger clicked = new AtomicInteger(-1);
        runOnMainSync(() -> {
            SmartisanGridIconPopupMenu popup = new SmartisanGridIconPopupMenu(activity, 4);
            ArrayList<Drawable> icons = new ArrayList<>();
            for (int i = 0; i < 13; i++) {
                icons.add(activity.getDrawable(android.R.drawable.ic_menu_add));
            }
            popup.setIcons(icons);
            popup.setOnMenuItemClickListener(clicked::set);
            popup.setAnchorView(activity.getWindow().getDecorView());
            popup.showCenter();
            reference.set(popup);
        });
        waitForIdleSync();
        SmartisanGridIconPopupMenu popup = reference.get();
        equal(dp(activity, 336) + popup.getLeftRightShadowWidth() * 2,
                popup.getPopupWindowWidth(), "grid menu width");
        runOnMainSync(() -> {
            View content = popup.getPopupWindow().getContentView();
            ViewGroup pager = content.findViewById(R.id.smartisan_rom_grid_menu_pager);
            equal(dp(activity, 336), pager.getWidth(), "grid menu content width");
            GridView grid = (GridView) pager.getChildAt(0);
            View item = grid.getChildAt(0);
            if (item == null) throw new AssertionError("grid menu first item missing");
            float x = item.getLeft() + item.getWidth() / 2f;
            float y = item.getTop() + item.getHeight() / 2f;
            long now = SystemClock.uptimeMillis();
            MotionEvent down = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, x, y, 0);
            MotionEvent up = MotionEvent.obtain(now, now + 16, MotionEvent.ACTION_UP, x, y, 0);
            try {
                grid.dispatchTouchEvent(down);
                grid.dispatchTouchEvent(up);
            } finally {
                down.recycle();
                up.recycle();
            }
        });
        waitForIdleSync();
        SystemClock.sleep(ViewConfiguration.getPressedStateDuration() + 50L);
        waitForIdleSync();
        equal(0, clicked.get(), "grid first-page item callback");

        AtomicReference<RomPagedView> pagerReference = new AtomicReference<>();
        AtomicInteger dragScroll = new AtomicInteger();
        runOnMainSync(() -> {
            RomPagedView pager = popup.getPopupWindow().getContentView().findViewById(
                    R.id.smartisan_rom_grid_menu_pager);
            pagerReference.set(pager);
            float startX = pager.getWidth() * 0.8f;
            float endX = pager.getWidth() * 0.2f;
            float y = pager.getHeight() / 2f;
            long now = SystemClock.uptimeMillis();
            MotionEvent down = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, startX, y, 0);
            MotionEvent move = MotionEvent.obtain(now, now + 250, MotionEvent.ACTION_MOVE,
                    endX, y, 0);
            MotionEvent up = MotionEvent.obtain(now, now + 500, MotionEvent.ACTION_UP, endX, y, 0);
            try {
                pager.dispatchTouchEvent(down);
                pager.dispatchTouchEvent(move);
                dragScroll.set(pager.getScrollX());
                pager.dispatchTouchEvent(up);
            } finally {
                down.recycle();
                move.recycle();
                up.recycle();
            }
        });
        SystemClock.sleep(300L);
        waitForIdleSync();
        RomPagedView pager = pagerReference.get();
        if (pager.getDisplayedChild() != 1) {
            throw new AssertionError("grid slow-drag second page: page="
                    + pager.getDisplayedChild() + ", moveScroll=" + dragScroll.get()
                    + ", width=" + pager.getWidth() + ", children=" + pager.getChildCount()
                    + ", direction=" + pager.getLayoutDirection());
        }
        runOnMainSync(() -> {
            GridView secondPage = (GridView) pager.getChildAt(1);
            View first = secondPage.getChildAt(0);
            if (first == null || !secondPage.performItemClick(first, 0, 0L)) {
                throw new AssertionError("grid second-page item was not clickable");
            }
        });
        equal(12, clicked.get(), "grid second-page absolute item callback");

        runOnMainSync(() -> {
            pager.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            pager.setCurrentPage(1, false);
            if (!pager.onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT,
                    new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT))) {
                throw new AssertionError("grid keyboard previous-page action was not handled");
            }
            equal(0, pager.getDisplayedChild(), "grid keyboard previous page");
            if (!pager.performAccessibilityAction(
                    AccessibilityNodeInfo.ACTION_SCROLL_FORWARD, null)) {
                throw new AssertionError("grid accessibility next-page action was not handled");
            }
            equal(1, pager.getDisplayedChild(), "grid accessibility next page");

            pager.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            pager.setCurrentPage(0, false);
            if (!pager.onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT,
                    new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT))) {
                throw new AssertionError("grid RTL physical-left action was not handled");
            }
            equal(1, pager.getDisplayedChild(), "grid RTL physical-left page");

            pager.setId(0x100201);
            SparseArray<Parcelable> state = new SparseArray<>();
            pager.saveHierarchyState(state);
            RomPagedView restored = new RomPagedView(activity);
            restored.setId(pager.getId());
            restored.addView(new View(activity));
            restored.addView(new View(activity));
            restored.restoreHierarchyState(roundTripState(state));
            restored.measure(View.MeasureSpec.makeMeasureSpec(dp(activity, 336),
                            View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(dp(activity, 200),
                            View.MeasureSpec.AT_MOST));
            restored.layout(0, 0, restored.getMeasuredWidth(), restored.getMeasuredHeight());
            equal(1, restored.getDisplayedChild(), "grid pager restored page");

            RomPageIndicator indicator = new RomPageIndicator(activity);
            indicator.setId(0x100202);
            indicator.setState(2, 1);
            SparseArray<Parcelable> indicatorState = new SparseArray<>();
            indicator.saveHierarchyState(indicatorState);
            RomPageIndicator restoredIndicator = new RomPageIndicator(activity);
            restoredIndicator.setId(indicator.getId());
            restoredIndicator.restoreHierarchyState(roundTripState(indicatorState));
            if (!String.valueOf(indicator.getContentDescription()).contentEquals(
                    restoredIndicator.getContentDescription())) {
                throw new AssertionError("grid indicator lost restored page description");
            }

            RomPageIndicator singlePageIndicator = new RomPageIndicator(activity);
            singlePageIndicator.setState(1, 0);
            singlePageIndicator.measure(View.MeasureSpec.makeMeasureSpec(0,
                            View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            int expectedIndicatorHeight = Math.round(activity.getResources().getDimension(
                    R.dimen.smartisan_rom_grid_indicator_radius) * 4f + 1f);
            equal(expectedIndicatorHeight, singlePageIndicator.getMeasuredHeight(),
                    "single-page indicator reserved height");

            RomPagedView firstPageSizedPager = new RomPagedView(activity);
            View shortFirstPage = new View(activity);
            shortFirstPage.setMinimumHeight(dp(activity, 40));
            View tallSecondPage = new View(activity);
            tallSecondPage.setMinimumHeight(dp(activity, 80));
            firstPageSizedPager.addView(shortFirstPage);
            firstPageSizedPager.addView(tallSecondPage);
            firstPageSizedPager.measure(View.MeasureSpec.makeMeasureSpec(dp(activity, 336),
                            View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(dp(activity, 200),
                            View.MeasureSpec.AT_MOST));
            equal(dp(activity, 40), firstPageSizedPager.getMeasuredHeight(),
                    "grid pager first-page wrap height");
            equal(dp(activity, 80), tallSecondPage.getMeasuredHeight(),
                    "grid pager still measures later pages");
        });
        runOnMainSync(popup::dismiss);

        AtomicReference<SmartisanGridIconPopupMenu> livePopupReference = new AtomicReference<>();
        Drawable replacement = activity.getDrawable(android.R.drawable.ic_menu_camera);
        runOnMainSync(() -> {
            ArrayList<Drawable> liveIcons = new ArrayList<>();
            liveIcons.add(activity.getDrawable(android.R.drawable.ic_menu_add));
            liveIcons.add(activity.getDrawable(android.R.drawable.ic_menu_close_clear_cancel));
            SmartisanGridIconPopupMenu livePopup = new SmartisanGridIconPopupMenu(activity, 4);
            livePopup.setIcons(liveIcons);
            // R2 snapshots the count but retains the caller's list for same-slot replacements.
            liveIcons.remove(1);
            liveIcons.set(0, replacement);
            liveIcons.add(activity.getDrawable(android.R.drawable.ic_menu_edit));
            liveIcons.add(activity.getDrawable(android.R.drawable.ic_menu_share));
            livePopup.setAnchorView(activity.getWindow().getDecorView());
            livePopup.showCenter();
            livePopupReference.set(livePopup);
        });
        waitForIdleSync();
        SmartisanGridIconPopupMenu livePopup = livePopupReference.get();
        runOnMainSync(() -> {
            ViewGroup livePager = livePopup.getPopupWindow().getContentView().findViewById(
                    R.id.smartisan_rom_grid_menu_pager);
            equal(1, livePager.getChildCount(), "grid snapshot page count");
            GridView liveGrid = (GridView) livePager.getChildAt(0);
            equal(2, liveGrid.getAdapter().getCount(), "grid snapshot icon count");
            View first = liveGrid.getChildAt(0);
            if (first == null) throw new AssertionError("grid replacement row missing");
            ImageView icon = first.findViewById(R.id.smartisan_rom_grid_menu_icon);
            if (icon.getDrawable() != replacement) {
                throw new AssertionError("grid did not retain caller drawable replacement");
            }
        });
        runOnMainSync(livePopup::dismiss);
    }

    private void verifyPopupCoordinates(Activity activity) {
        AtomicReference<SmartisanListPopupMenu> reference = new AtomicReference<>();
        runOnMainSync(() -> {
            SmartisanListPopupMenu popup = new SmartisanListPopupMenu(activity);
            popup.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1,
                    new String[] {"坐标"}));
            View anchor = activity.getWindow().getDecorView();
            popup.setAnchorView(anchor);
            popup.setClipToScreenEnabled(true);
            popup.show(SmartisanPopupMenu.ARROW_BOTTOM, dp(activity, 40), dp(activity, 160),
                    0, 0);
            reference.set(popup);
        });
        waitForIdleSync();
        SmartisanListPopupMenu popup = reference.get();
        int[] anchorLocation = new int[2];
        int[] popupLocation = new int[2];
        popup.getAnchorView().getLocationOnScreen(anchorLocation);
        popup.getPopupWindow().getContentView().getLocationOnScreen(popupLocation);
        within(anchorLocation[0] + dp(activity, 40), popupLocation[0], 1,
                "popup original x offset");
        within(anchorLocation[1] + dp(activity, 160), popupLocation[1], 1,
                "bottom-arrow must not move popup coordinates");
        runOnMainSync(popup::dismiss);

        runOnMainSync(() -> {
            SmartisanListPopupMenu above = new SmartisanListPopupMenu(activity);
            above.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1,
                    new String[] {"上方"}));
            View anchor = activity.getWindow().getDecorView();
            above.setAnchorView(anchor);
            above.setClipToScreenEnabled(true);
            above.setMenuPopup(true);
            above.show(SmartisanPopupMenu.ARROW_BOTTOM, dp(activity, 40),
                    anchor.getHeight() + dp(activity, 200), 0, 0);
            reference.set(above);
        });
        waitForIdleSync();
        SmartisanListPopupMenu above = reference.get();
        above.getAnchorView().getLocationOnScreen(anchorLocation);
        above.getPopupWindow().getContentView().getLocationOnScreen(popupLocation);
        View abovePanel = (View) above.getPopupWindow().getContentView()
                .findViewById(R.id.smartisan_rom_menu_list).getParent();
        int expectedY = anchorLocation[1] + dp(activity, 200)
                - abovePanel.getMeasuredHeight();
        within(expectedY, popupLocation[1], 1, "menu-popup above-anchor coordinate");
        runOnMainSync(above::dismiss);

        runOnMainSync(() -> {
            SmartisanListPopupMenu negativeX = new SmartisanListPopupMenu(activity);
            negativeX.setAdapter(new ArrayAdapter<>(activity,
                    android.R.layout.simple_list_item_1, new String[] {"负横向偏移"}));
            negativeX.setAnchorView(activity.getWindow().getDecorView());
            negativeX.setArrowVisible(true);
            int radius = negativeX.getMenuPanelBgRoundCornerRadius();
            int arrowOffset = -radius * 2;
            negativeX.show(SmartisanPopupMenu.ARROW_TOP, 0, dp(activity, 20),
                    arrowOffset, 0);
            reference.set(negativeX);
        });
        waitForIdleSync();
        SmartisanListPopupMenu negativeX = reference.get();
        View horizontalArrow = negativeX.getPopupWindow().getContentView().findViewById(
                R.id.smartisan_rom_arrow);
        near(negativeX.getLeftRightShadowWidth()
                        - negativeX.getMenuPanelBgRoundCornerRadius(),
                horizontalArrow.getX(), "negative horizontal arrow offset");
        runOnMainSync(negativeX::dismiss);

        runOnMainSync(() -> {
            SmartisanListPopupMenu negativeY = new SmartisanListPopupMenu(activity);
            negativeY.setAdapter(new ArrayAdapter<>(activity,
                    android.R.layout.simple_list_item_1, new String[] {"负纵向偏移"}));
            negativeY.setAnchorView(activity.getWindow().getDecorView());
            negativeY.setArrowVisible(true);
            int radius = negativeY.getMenuPanelBgRoundCornerRadius();
            negativeY.show(SmartisanPopupMenu.ARROW_LEFT, dp(activity, 20), 0,
                    0, -radius * 2);
            reference.set(negativeY);
        });
        waitForIdleSync();
        SmartisanListPopupMenu negativeY = reference.get();
        View verticalArrow = negativeY.getPopupWindow().getContentView().findViewById(
                R.id.smartisan_rom_arrow);
        int topBottomShadow = activity.getResources().getDimensionPixelSize(
                R.dimen.smartisan_rom_popup_bg_top_bottom_shadow_height);
        near(topBottomShadow - negativeY.getMenuPanelBgRoundCornerRadius(),
                verticalArrow.getY(), "negative vertical arrow offset");
        runOnMainSync(negativeY::dismiss);

        AtomicReference<int[]> groupedExpectedLocation = new AtomicReference<>();
        runOnMainSync(() -> {
            View anchor = activity.getWindow().getDecorView();
            PopupMenu model = new PopupMenu(activity, anchor);
            model.getMenu().add(1, 401, 0, "边缘分组项");
            SmartisanListPopupMenu groupedPopup = new SmartisanListPopupMenu(activity);
            groupedPopup.setAnchorView(anchor);
            groupedPopup.setArrowVisible(true);
            groupedPopup.setAdapter(new SmartisanGroupMenuAdapter(model.getMenu()));
            Rect visibleFrame = new Rect();
            anchor.getWindowVisibleDisplayFrame(visibleFrame);
            int[] rootScreen = new int[2];
            int[] anchorWindow = new int[2];
            anchor.getRootView().getLocationOnScreen(rootScreen);
            anchor.getLocationInWindow(anchorWindow);
            int visibleRight = visibleFrame.right - rootScreen[0];
            int contentWidth = groupedPopup.getPopupWindowWidth()
                    - groupedPopup.getLeftRightShadowWidth() * 2;
            int requestedX = visibleRight - groupedPopup.getPopupWindowWidth() / 2;
            int rawX = anchorWindow[0] + requestedX - groupedPopup.getLeftRightShadowWidth();
            groupedExpectedLocation.set(new int[] {rootScreen[0] + rawX - contentWidth});
            groupedPopup.show(SmartisanPopupMenu.ARROW_TOP, requestedX,
                    anchor.getHeight(), 0, 0);
            reference.set(groupedPopup);
        });
        waitForIdleSync();
        SmartisanListPopupMenu groupedPopup = reference.get();
        View groupedContent = groupedPopup.getPopupWindow().getContentView();
        View groupedArrow = groupedContent.findViewById(R.id.smartisan_rom_arrow);
        equal(View.GONE, groupedArrow.getVisibility(), "grouped edge arrow visibility");
        int[] groupedLocation = new int[2];
        groupedContent.getLocationOnScreen(groupedLocation);
        within(groupedExpectedLocation.get()[0], groupedLocation[0], 2,
                "grouped edge horizontal correction");
        runOnMainSync(groupedPopup::dismiss);
    }

    private void verifyPopupOptionRows(Activity activity) {
        AtomicReference<View[]> reference = new AtomicReference<>();
        runOnMainSync(() -> {
            SmartisanPopupMenuStandardListItem standard =
                    new SmartisanPopupMenuStandardListItem(activity);
            standard.setMenuIcon(android.R.drawable.ic_menu_search);
            standard.setMenuTitle("无线网络");
            standard.setMenuSubtitle("已连接");
            standard.setChecked(true);
            SmartisanPopupMenuLongPressedListItem longPressed =
                    new SmartisanPopupMenuLongPressedListItem(activity);
            SmartisanPopupMenuRemovableListItem removable =
                    new SmartisanPopupMenuRemovableListItem(activity);
            reference.set(new View[] {standard, longPressed, removable});
        });

        View[] rows = reference.get();
        View standardIcon = rows[0].findViewById(R.id.smartisan_rom_menu_icon);
        View selected = rows[0].findViewById(R.id.smartisan_rom_menu_selected);
        TextView subtitle = rows[0].findViewById(R.id.smartisan_rom_subtitle);
        equal(dp(activity, 44), standardIcon.getLayoutParams().width,
                "standard option icon slot");
        equal(dp(activity, 27), selected.getLayoutParams().width,
                "standard option selected width");
        equal(View.VISIBLE, selected.getVisibility(), "standard option checked state");
        near(12f * activity.getResources().getDisplayMetrics().scaledDensity,
                subtitle.getTextSize(), "standard option subtitle size");

        if (!(rows[1] instanceof RelativeLayout)) {
            throw new AssertionError("long-press option parent is not RelativeLayout");
        }
        View longIcon = rows[1].findViewById(R.id.smartisan_rom_menu_icon);
        equal(dp(activity, 36), longIcon.getLayoutParams().width,
                "long-press option icon width");

        View close = rows[2].findViewById(R.id.smartisan_rom_menu_closed);
        equal(dp(activity, 36), close.getLayoutParams().width,
                "removable option close width");

        runOnMainSync(() -> {
            PopupMenu popupModel = new PopupMenu(activity, activity.getWindow().getDecorView());
            Menu menu = popupModel.getMenu();
            menu.add(1, 101, 0, "第一组");
            menu.add(2, 201, 1, "第二组");
            SmartisanGroupMenuAdapter grouped = new SmartisanGroupMenuAdapter(menu);
            equal(3, grouped.getCount(), "group menu divider count");
            equal(1, grouped.getItem(0).getGroupId(), "group menu ascending group order");
            if (!"Divider".contentEquals(grouped.getItem(1).getTitle())) {
                throw new AssertionError("group menu divider sentinel missing");
            }
            equal(0, (int) grouped.getItemId(0), "group menu first position ID");
            equal(1, (int) grouped.getItemId(1), "group menu divider position ID");
            equal(2, (int) grouped.getItemId(2), "group menu final position ID");

            SmartisanMenuItem withoutIcon = new SmartisanAbsMenuItem() {
                @Override public String getTitle() { return "无图标"; }
                @Override public boolean hasMenuIcon() { return false; }
            };
            Drawable restoredIcon = activity.getDrawable(android.R.drawable.ic_menu_view);
            SmartisanMenuItem withIcon = new SmartisanAbsMenuItem() {
                @Override public String getTitle() { return "有图标"; }
                @Override public void setMenuIcon(ImageView imageView) {
                    // Deliberately do not set visibility: the adapter owns recycled row state.
                    imageView.setImageDrawable(restoredIcon);
                }
            };
            ArrayList<SmartisanMenuItem> iconItems = new ArrayList<>();
            iconItems.add(withoutIcon);
            iconItems.add(withIcon);
            SmartisanListPopupMenuStandardAdapter standardAdapter =
                    new SmartisanListPopupMenuStandardAdapter(activity, iconItems);
            ListView iconParent = new ListView(activity);
            View withoutIconRow = standardAdapter.getView(0, null, iconParent);
            equal(View.GONE, withoutIconRow.findViewById(
                    R.id.smartisan_rom_menu_icon).getVisibility(), "iconless popup row");
            View withIconRow = standardAdapter.getView(1, withoutIconRow, iconParent);
            equal(View.VISIBLE, withIconRow.findViewById(
                    R.id.smartisan_rom_menu_icon).getVisibility(), "reused popup row icon");

            PopupMenu duplicateModel = new PopupMenu(activity,
                    activity.getWindow().getDecorView());
            android.view.MenuItem firstDuplicate = duplicateModel.getMenu().add("重复一");
            android.view.MenuItem secondDuplicate = duplicateModel.getMenu().add("重复二");
            if (firstDuplicate.getItemId() != secondDuplicate.getItemId()) {
                throw new AssertionError("platform duplicate-ID test setup changed");
            }
            SmartisanGroupMenuAdapter duplicateAdapter = new SmartisanGroupMenuAdapter(
                    duplicateModel.getMenu());
            AtomicReference<android.view.MenuItem> exactItem = new AtomicReference<>();
            duplicateAdapter.setOnMenuItemActionListener(exactItem::set);
            duplicateAdapter.onItemClick(null, null, 1, 1L);
            if (exactItem.get() != secondDuplicate) {
                throw new AssertionError("group exact-item callback lost duplicate-ID selection");
            }
        });
    }

    private void verifySpinnerView(Activity activity) {
        AtomicBoolean dropClicked = new AtomicBoolean();
        AtomicReference<View[]> reference = new AtomicReference<>();
        runOnMainSync(() -> {
            SmartisanSpinnerView drop = new SmartisanSpinnerView(activity);
            drop.setSpinnerPickText(null, "第一项");
            drop.setSpinnerStyle(SmartisanSpinnerView.SPINNER_STYLE_DROP);
            equal(2, drop.getChildCount(), "drop-down child count after style change");
            drop.setDropDownClickListener(() -> dropClicked.set(true));
            if (drop.getDropdownIconView() == null) {
                throw new AssertionError("drop-down arrow missing");
            }
            drop.getDropdownIconView().performClick();

            SmartisanSpinnerView range = new SmartisanSpinnerView(activity);
            range.setSpinnerPickText(new String[] {"按日", "按周", "按月"}, null);
            range.setSpinnerStyle(SmartisanSpinnerView.SPINNER_STYLE_RANGE);
            equal(3, range.getChildCount(), "range child count after style change");
            if (range.getRangeLeftIcon() == null || range.getRangeRightIcon() == null) {
                throw new AssertionError("range arrows missing");
            }

            SmartisanWheelTextView wheel = new SmartisanWheelTextView(activity);
            wheel.setDisplayedValues("按日", "按周", "按月");
            wheel.setIsNeedRotate(true);
            wheel.setValue(-1);
            reference.set(new View[] {drop, range, wheel});
        });
        if (!dropClicked.get()) throw new AssertionError("drop-down callback not delivered");
        SmartisanWheelTextView wheel = (SmartisanWheelTextView) reference.get()[2];
        equal(2, wheel.getValue(), "wheel wrapped value");
    }

    private void verifyAlertMessageAndCustomView(Activity activity) {
        AtomicReference<SmartisanAlertDialog> reference = new AtomicReference<>();
        AtomicReference<TextView> customReference = new AtomicReference<>();
        runOnMainSync(() -> {
            TextView custom = new TextView(activity);
            custom.setText("自定义内容");
            SmartisanAlertDialog dialog = new SmartisanAlertDialog.Builder(activity)
                    .setMessage("说明文字")
                    .setView(custom)
                    .setPositiveButton("确定", null)
                    .create();
            dialog.show();
            reference.set(dialog);
            customReference.set(custom);
        });
        waitForIdleSync();
        SmartisanAlertDialog dialog = reference.get();
        if (dialog.getCustomView() != customReference.get()) {
            throw new AssertionError("alert custom view was cleared by setMessage/setView");
        }
        TextView message = dialog.findViewById(R.id.smartisan_rom_alert_message);
        if (message == null || !"说明文字".contentEquals(message.getText())) {
            throw new AssertionError("alert message was cleared by custom view");
        }
        runOnMainSync(dialog::dismiss);
    }

    private void verifyAlertOriginalContracts(Activity activity) {
        runOnMainSync(() -> {
            SmartisanAlertDialog.Builder themed = new SmartisanAlertDialog.Builder(
                    activity, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
            if (!(themed.getContext() instanceof ContextThemeWrapper)) {
                throw new AssertionError("alert Builder did not expose its themed context");
            }

            TextView custom = new TextView(themed.getContext());
            custom.setText("列表附加内容");
            SmartisanAlertDialog dialog = themed
                    .setItems(new CharSequence[] {"无回调选项"}, null)
                    .setView(custom)
                    .setPositiveButton("关闭", null)
                    .create();
            dialog.show();
            if (dialog.getButton(0) != null) {
                throw new AssertionError("alert returned a button for an unknown identifier");
            }

            ListView list = dialog.getListView();
            if (list == null || list.getOnItemClickListener() != null) {
                throw new AssertionError("alert installed a null-listener dismiss path");
            }
            View customPanel = dialog.findViewById(R.id.smartisan_rom_alert_custom_panel);
            if (!(customPanel.getLayoutParams() instanceof LinearLayout.LayoutParams)
                    || ((LinearLayout.LayoutParams) customPanel.getLayoutParams()).weight != 0f) {
                throw new AssertionError("alert list/custom panel retained competing weight");
            }
            if (list.performItemClick(null, 0, 0L) || !dialog.isShowing()) {
                throw new AssertionError("alert null-listener item dismissed the dialog");
            }
            dialog.dismiss();
        });
    }

    private void verifyAlertInputMethodAndButtonTouch(Activity activity) {
        AtomicReference<SmartisanAlertDialog> inputReference = new AtomicReference<>();
        runOnMainSync(() -> {
            EditText input = new EditText(activity);
            input.setSingleLine(true);
            SmartisanAlertDialog dialog = new SmartisanAlertDialog.Builder(activity)
                    .setTitle("输入")
                    .setView(input)
                    .setPositiveButton("确定", null)
                    .create();
            dialog.show();
            inputReference.set(dialog);
        });
        waitForIdleSync();
        runOnMainSync(() -> {
            SmartisanAlertDialog dialog = inputReference.get();
            int flags = dialog.getWindow().getAttributes().flags;
            if ((flags & WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM) != 0) {
                throw new AssertionError("alert input view was blocked from the input method");
            }
            dialog.dismiss();
        });

        AtomicInteger clickCount = new AtomicInteger();
        AtomicReference<SmartisanAlertDialog> clickReference = new AtomicReference<>();
        runOnMainSync(() -> {
            SmartisanAlertDialog dialog = new SmartisanAlertDialog.Builder(activity)
                    .setMessage("按钮触摸")
                    .setPositiveButton("确定", (ignored, which) -> clickCount.incrementAndGet())
                    .create();
            dialog.show();
            clickReference.set(dialog);
        });
        waitForIdleSync();
        runOnMainSync(() -> {
            SmartisanAlertDialog dialog = clickReference.get();
            Button button = dialog.getButton(SmartisanAlertDialog.BUTTON_POSITIVE);
            if (button == null || button.getWidth() <= 0 || button.getHeight() <= 0) {
                throw new AssertionError("alert action button was not laid out");
            }
            long now = SystemClock.uptimeMillis();
            MotionEvent down = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN,
                    button.getWidth() / 2f, button.getHeight() / 2f, 0);
            MotionEvent up = MotionEvent.obtain(now, now + 16, MotionEvent.ACTION_UP,
                    button.getWidth() / 2f, button.getHeight() / 2f, 0);
            try {
                if (!button.dispatchTouchEvent(down) || !button.dispatchTouchEvent(up)) {
                    throw new AssertionError("alert scale listener consumed the button path");
                }
            } finally {
                down.recycle();
                up.recycle();
            }
            equal(1, clickCount.get(), "alert DOWN/UP callback count");
        });
        waitForIdleSync();
        if (clickReference.get().isShowing()) {
            throw new AssertionError("alert DOWN/UP path did not dismiss after callback");
        }

        AtomicInteger cancelClickCount = new AtomicInteger();
        AtomicReference<SmartisanAlertDialog> cancelReference = new AtomicReference<>();
        runOnMainSync(() -> {
            SmartisanAlertDialog dialog = new SmartisanAlertDialog.Builder(activity)
                    .setMessage("取消触摸")
                    .setPositiveButton("确定",
                            (ignored, which) -> cancelClickCount.incrementAndGet())
                    .create();
            dialog.show();
            cancelReference.set(dialog);
        });
        waitForIdleSync();
        runOnMainSync(() -> {
            SmartisanAlertDialog dialog = cancelReference.get();
            Button button = dialog.getButton(SmartisanAlertDialog.BUTTON_POSITIVE);
            long now = SystemClock.uptimeMillis();
            MotionEvent down = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN,
                    button.getWidth() / 2f, button.getHeight() / 2f, 0);
            MotionEvent cancel = MotionEvent.obtain(now, now + 16, MotionEvent.ACTION_CANCEL,
                    button.getWidth() / 2f, button.getHeight() / 2f, 0);
            try {
                button.dispatchTouchEvent(down);
                button.dispatchTouchEvent(cancel);
            } finally {
                down.recycle();
                cancel.recycle();
            }
            equal(0, cancelClickCount.get(), "alert CANCEL callback count");
            if (!dialog.isShowing()) {
                throw new AssertionError("alert CANCEL path dismissed the dialog");
            }
            dialog.dismiss();
        });
    }

    private void verifyAlertChoiceStateRoundTrip(Activity activity) {
        runOnMainSync(() -> {
            CharSequence[] choices = {"一", "二", "三"};
            SmartisanAlertDialog single = new SmartisanAlertDialog.Builder(activity)
                    .setSingleChoiceItems(choices, 0, (ignored, which) -> { })
                    .setPositiveButton("关闭", null)
                    .create();
            single.show();
            single.getListView().setItemChecked(2, true);
            Bundle singleState = roundTripBundle(single.onSaveInstanceState());
            single.dismiss();

            SmartisanAlertDialog restoredSingle = new SmartisanAlertDialog.Builder(activity)
                    .setSingleChoiceItems(choices, 0, (ignored, which) -> { })
                    .setPositiveButton("关闭", null)
                    .create();
            restoredSingle.show();
            restoredSingle.onRestoreInstanceState(singleState);
            equal(2, restoredSingle.getListView().getCheckedItemPosition(),
                    "restored alert single choice");
            restoredSingle.dismiss();

            boolean[] checked = {true, false, false};
            SmartisanAlertDialog multi = new SmartisanAlertDialog.Builder(activity)
                    .setMultiChoiceItems(choices, checked, (ignored, which, value) -> { })
                    .setPositiveButton("关闭", null)
                    .create();
            multi.show();
            multi.getListView().setItemChecked(0, false);
            multi.getListView().setItemChecked(1, true);
            Bundle multiState = roundTripBundle(multi.onSaveInstanceState());
            multi.dismiss();

            SmartisanAlertDialog restoredMulti = new SmartisanAlertDialog.Builder(activity)
                    .setMultiChoiceItems(choices, new boolean[] {false, false, false},
                            (ignored, which, value) -> { })
                    .setPositiveButton("关闭", null)
                    .create();
            restoredMulti.show();
            restoredMulti.onRestoreInstanceState(multiState);
            if (restoredMulti.getListView().isItemChecked(0)
                    || !restoredMulti.getListView().isItemChecked(1)) {
                throw new AssertionError("restored alert multi-choice state differs");
            }
            restoredMulti.dismiss();
        });
    }

    private void verifyMenuNavigationCallback(Activity activity) {
        runOnMainSync(() -> {
            SmartisanMenuDialog dialog = new SmartisanMenuDialog(activity);
            View panel = dialog.findViewById(R.id.smartisan_rom_menu_dialog_content_panel);
            dialog.onApplyNavigationBarStatusChange(false);
            equal(activity.getResources().getDimensionPixelOffset(
                            R.dimen.smartisan_rom_menu_dialog_hidden_nav_space),
                    panel.getPaddingBottom(), "menu hidden-navigation spacing");
            dialog.onApplyNavigationBarStatusChange(true);
            equal(0, panel.getPaddingBottom(), "menu visible-navigation spacing");
        });
    }

    private void verifyMenuCenterAndLongList(Activity activity) {
        runOnMainSync(() -> {
            ArrayList<String> labels = new ArrayList<>();
            ArrayList<View.OnClickListener> listeners = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                labels.add("菜单 " + i);
                listeners.add(view -> { });
            }
            SmartisanMenuDialog dialog = new SmartisanMenuDialog(
                    activity, SmartisanMenuDialog.LOCATION_APP_CENTER);
            dialog.setAdapter(new SmartisanMenuDialogListAdapter(
                    activity, labels, listeners));
            dialog.show();
            equal(Gravity.CENTER, dialog.getWindow().getAttributes().gravity,
                    "center menu gravity");
            equal(activity.getResources().getDimensionPixelOffset(
                            R.dimen.smartisan_rom_menu_dialog_long_list_height),
                    dialog.getListView().getLayoutParams().height,
                    "five-item menu height");
            dialog.dismiss();
        });
    }

    private void verifyProgressDialogWindow(Activity activity) {
        AtomicReference<SmartisanProgressDialog> reference = new AtomicReference<>();
        runOnMainSync(() -> {
            SmartisanProgressDialog dialog = SmartisanProgressDialog.show(
                    activity, "处理中", "请稍候");
            reference.set(dialog);
        });
        waitForIdleSync();
        runOnMainSync(() -> {
            SmartisanProgressDialog dialog = reference.get();
            int flags = dialog.getWindow().getAttributes().flags;
            if ((flags & WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM) == 0) {
                throw new AssertionError("progress dialog lost ALT_FOCUSABLE_IM");
            }
            dialog.dismiss();
        });
    }

    private void verifyDialogNightRtlAndExternalHierarchy(Activity activity) {
        Configuration configuration = new Configuration(
                activity.getResources().getConfiguration());
        configuration.uiMode = (configuration.uiMode & ~Configuration.UI_MODE_NIGHT_MASK)
                | Configuration.UI_MODE_NIGHT_YES;
        configuration.setLocale(new Locale("ar"));
        Context nightRtl = activity.createConfigurationContext(configuration);
        equal(View.LAYOUT_DIRECTION_RTL,
                nightRtl.getResources().getConfiguration().getLayoutDirection(),
                "dialog RTL configuration");
        equal(0x9affffff, nightRtl.getColor(R.color.smartisan_rom_dialog_title_text),
                "night alert title color");

        View alert = LayoutInflater.from(nightRtl).inflate(
                R.layout.smartisan_rom_alert_dialog, null);
        TextView message = alert.findViewById(R.id.smartisan_rom_alert_message);
        if ((message.getGravity() & Gravity.RELATIVE_LAYOUT_DIRECTION) == 0) {
            throw new AssertionError("alert message gravity is not RTL-relative");
        }

        View menu = LayoutInflater.from(nightRtl).inflate(
                R.layout.smartisan_rom_menu_dialog, null);
        View menuContent = menu.findViewById(R.id.smartisan_rom_menu_dialog_content_panel);
        if (!(menuContent.getBackground() instanceof ColorDrawable)
                || ((ColorDrawable) menuContent.getBackground()).getColor() != 0xf5f5f5f5) {
            throw new AssertionError("night menu no longer keeps the original light surface");
        }

        SmartisanDialogTitleBar titleBar = new SmartisanDialogTitleBar(nightRtl);
        titleBar.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        View divider = titleBar.findViewById(R.id.smartisan_rom_shadow_divider);
        if (titleBar.getTitleBarContainer().getParent() != titleBar || divider == null
                || divider.getParent() != titleBar.getTitleBarContainer()) {
            throw new AssertionError("external menu title hierarchy differs");
        }
        titleBar.getTitleBarContainer().setBackgroundColor(Color.TRANSPARENT);
        titleBar.setBackgroundResource(R.drawable.smartisan_rom_revone_dialog_bg_title);
        divider.setVisibility(View.GONE);
        if (!(titleBar.getTitleBarContainer().getBackground() instanceof ColorDrawable)
                || ((ColorDrawable) titleBar.getTitleBarContainer().getBackground()).getColor()
                != Color.TRANSPARENT || titleBar.getBackground() == null
                || divider.getVisibility() != View.GONE) {
            throw new AssertionError("external menu title styling cannot reproduce R2 hierarchy");
        }
    }

    private void verifyPickerStateRestoration(Activity activity) {
        runOnMainSync(() -> {
            SmartisanDatePicker date = new SmartisanDatePicker(activity);
            date.setId(0x100101);
            date.init(SmartisanDatePicker.DatePickerType.EVENT,
                    2024, java.util.Calendar.FEBRUARY, 29, null);
            SparseArray<Parcelable> dateState = new SparseArray<>();
            date.saveHierarchyState(dateState);
            SmartisanDatePicker restoredDate = new SmartisanDatePicker(activity);
            restoredDate.setId(date.getId());
            restoredDate.init(SmartisanDatePicker.DatePickerType.EVENT,
                    2024, java.util.Calendar.FEBRUARY, 29, null);
            java.util.Calendar customMaximum = java.util.Calendar.getInstance();
            customMaximum.clear();
            customMaximum.set(2024, java.util.Calendar.DECEMBER, 31);
            restoredDate.setMaxDate(customMaximum.getTimeInMillis());
            restoredDate.restoreHierarchyState(roundTripState(dateState));
            equal(2024, restoredDate.getYear(), "restored picker year");
            equal(java.util.Calendar.FEBRUARY, restoredDate.getMonth(), "restored picker month");
            equal(29, restoredDate.getDayOfMonth(), "restored picker day");
            restoredDate.updateDate(2030, java.util.Calendar.JANUARY, 1);
            equal(2024, restoredDate.getYear(), "restored picker custom maximum year");
            equal(java.util.Calendar.DECEMBER, restoredDate.getMonth(),
                    "restored picker custom maximum month");

            SmartisanDatePickerEx dateEx = new SmartisanDatePickerEx(activity);
            dateEx.setId(0x100103);
            dateEx.init(SmartisanDatePickerEx.DatePickerType.BIRTHDAY,
                    1990, java.util.Calendar.DECEMBER, 31, null);
            SparseArray<Parcelable> dateExState = new SparseArray<>();
            dateEx.saveHierarchyState(dateExState);
            SmartisanDatePickerEx restoredDateEx = new SmartisanDatePickerEx(activity);
            restoredDateEx.setId(dateEx.getId());
            restoredDateEx.init(SmartisanDatePickerEx.DatePickerType.BIRTHDAY,
                    1990, java.util.Calendar.DECEMBER, 31, null);
            restoredDateEx.restoreHierarchyState(roundTripState(dateExState));
            equal(1990, restoredDateEx.getYear(), "restored extended picker year");
            equal(java.util.Calendar.DECEMBER, restoredDateEx.getMonth(),
                    "restored extended picker month");
            equal(31, restoredDateEx.getDayOfMonth(), "restored extended picker day");

            int lunarYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
            SmartisanDatePicker lunarDate = new SmartisanDatePicker(activity);
            lunarDate.setId(0x10010c);
            lunarDate.init(SmartisanDatePicker.DatePickerType.BIRTHDAY_LUNAR,
                    lunarYear, java.util.Calendar.JANUARY, 10, null);
            SparseArray<Parcelable> lunarDateState = new SparseArray<>();
            lunarDate.saveHierarchyState(lunarDateState);
            AtomicReference<SmartisanDatePicker.DatePickerType> lunarType =
                    new AtomicReference<>();
            SmartisanDatePicker restoredLunarDate = new SmartisanDatePicker(activity);
            restoredLunarDate.setId(lunarDate.getId());
            restoredLunarDate.init(SmartisanDatePicker.DatePickerType.EVENT,
                    lunarYear, java.util.Calendar.JANUARY, 10,
                    (picker, year, month, day, type) -> lunarType.set(type));
            restoredLunarDate.restoreHierarchyState(roundTripState(lunarDateState));
            restoredLunarDate.updateDate(lunarYear, java.util.Calendar.JANUARY, 11);
            if (lunarType.get() != SmartisanDatePicker.DatePickerType.BIRTHDAY_LUNAR) {
                throw new AssertionError("ordinary lunar-birthday mode was not restored");
            }

            SmartisanDatePickerEx lunarDateEx = new SmartisanDatePickerEx(activity);
            lunarDateEx.setId(0x10010d);
            lunarDateEx.init(SmartisanDatePickerEx.DatePickerType.BIRTHDAY_LUNAR,
                    lunarYear, java.util.Calendar.JANUARY, 10, null);
            SparseArray<Parcelable> lunarDateExState = new SparseArray<>();
            lunarDateEx.saveHierarchyState(lunarDateExState);
            AtomicReference<SmartisanDatePickerEx.DatePickerType> lunarTypeEx =
                    new AtomicReference<>();
            SmartisanDatePickerEx restoredLunarDateEx = new SmartisanDatePickerEx(activity);
            restoredLunarDateEx.setId(lunarDateEx.getId());
            restoredLunarDateEx.init(SmartisanDatePickerEx.DatePickerType.EVENT,
                    lunarYear, java.util.Calendar.JANUARY, 10,
                    (picker, year, month, day, type) -> lunarTypeEx.set(type));
            restoredLunarDateEx.restoreHierarchyState(roundTripState(lunarDateExState));
            restoredLunarDateEx.updateDate(lunarYear, java.util.Calendar.JANUARY, 11);
            if (lunarTypeEx.get() != SmartisanDatePickerEx.DatePickerType.BIRTHDAY_LUNAR) {
                throw new AssertionError("extended lunar-birthday mode was not restored");
            }

            SmartisanTimePicker time = new SmartisanTimePicker(activity);
            time.setId(0x100102);
            time.setIs24HourView(false);
            time.setCurrentHour(23);
            time.setCurrentMinute(47);
            SparseArray<Parcelable> timeState = new SparseArray<>();
            time.saveHierarchyState(timeState);
            SmartisanTimePicker restoredTime = new SmartisanTimePicker(activity);
            restoredTime.setId(time.getId());
            restoredTime.setIs24HourView(false);
            restoredTime.restoreHierarchyState(roundTripState(timeState));
            if (restoredTime.is24HourView()) {
                throw new AssertionError("restored picker unexpectedly changed to 24-hour mode");
            }
            equal(23, restoredTime.getCurrentHour(), "restored picker hour");
            equal(47, restoredTime.getCurrentMinute(), "restored picker minute");

            SmartisanTimePickerEx timeEx = new SmartisanTimePickerEx(activity);
            timeEx.setId(0x10010a);
            timeEx.setIs24HourView(false);
            timeEx.setCurrentHour(23);
            timeEx.setCurrentMinute(58);
            SparseArray<Parcelable> timeExState = new SparseArray<>();
            timeEx.saveHierarchyState(timeExState);
            SmartisanTimePickerEx restoredTimeEx = new SmartisanTimePickerEx(activity);
            restoredTimeEx.setId(timeEx.getId());
            restoredTimeEx.setIs24HourView(false);
            restoredTimeEx.restoreHierarchyState(roundTripState(timeExState));
            if (restoredTimeEx.is24HourView()) {
                throw new AssertionError("restored extended picker lost 12-hour mode");
            }
            equal(23, restoredTimeEx.getCurrentHour(), "restored extended picker hour");
            equal(58, restoredTimeEx.getCurrentMinute(), "restored extended picker minute");
            restoredTimeEx.setIs24HourView(true);
            if (!restoredTimeEx.is24HourView()) {
                throw new AssertionError("extended picker did not switch to 24-hour mode");
            }
            equal(23, restoredTimeEx.getCurrentHour(),
                    "extended picker hour changed across 12/24-hour switch");

            java.util.Calendar minimum = java.util.Calendar.getInstance();
            minimum.clear();
            minimum.set(2025, java.util.Calendar.JANUARY, 2, 8, 15, 0);
            java.util.Calendar current = java.util.Calendar.getInstance();
            current.clear();
            current.set(2025, java.util.Calendar.JANUARY, 3, 13, 30, 0);
            java.util.Calendar maximum = java.util.Calendar.getInstance();
            maximum.clear();
            maximum.set(2025, java.util.Calendar.JANUARY, 5, 20, 45, 0);
            SmartisanDateTimePicker dateTime = new SmartisanDateTimePicker(activity);
            dateTime.setId(0x10010b);
            dateTime.init(current.getTimeInMillis(), minimum.getTimeInMillis(),
                    maximum.getTimeInMillis(), null);
            SparseArray<Parcelable> dateTimeState = new SparseArray<>();
            dateTime.saveHierarchyState(dateTimeState);
            SmartisanDateTimePicker restoredDateTime = new SmartisanDateTimePicker(activity);
            restoredDateTime.setId(dateTime.getId());
            restoredDateTime.restoreHierarchyState(roundTripState(dateTimeState));
            equal(current.getTimeInMillis(), restoredDateTime.getCurrentMills(),
                    "restored date-time value");
            equal(minimum.getTimeInMillis(), restoredDateTime.getMinDate(),
                    "restored date-time minimum");
            equal(maximum.getTimeInMillis(), restoredDateTime.getMaxDate(),
                    "restored date-time maximum");

            SmartisanNumberPicker number = new SmartisanNumberPicker(activity);
            number.setId(0x100104);
            number.setMinValue(0);
            number.setMaxValue(10);
            number.setValue(7);
            SparseArray<Parcelable> numberState = new SparseArray<>();
            number.saveHierarchyState(numberState);
            SmartisanNumberPicker restoredNumber = new SmartisanNumberPicker(activity);
            restoredNumber.setId(number.getId());
            restoredNumber.setMinValue(0);
            restoredNumber.setMaxValue(10);
            restoredNumber.restoreHierarchyState(roundTripState(numberState));
            equal(7, restoredNumber.getValue(), "restored number-picker value");
            if (!restoredNumber.onKeyDown(KeyEvent.KEYCODE_DPAD_DOWN,
                    new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN))) {
                throw new AssertionError("number picker did not handle DPAD down");
            }
            equal(8, restoredNumber.getValue(), "number-picker DPAD increment");
            if (!restoredNumber.onKeyDown(KeyEvent.KEYCODE_DPAD_UP,
                    new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP))) {
                throw new AssertionError("number picker did not handle DPAD up");
            }
            equal(7, restoredNumber.getValue(), "number-picker DPAD decrement");
            restoredNumber.setWrapSelectorWheel(false);
            restoredNumber.setValue(restoredNumber.getMinValue());
            if (restoredNumber.onKeyDown(KeyEvent.KEYCODE_DPAD_UP,
                    new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP))) {
                throw new AssertionError("number picker consumed DPAD beyond minimum");
            }
            equal(restoredNumber.getMinValue(), restoredNumber.getValue(),
                    "number-picker minimum boundary");
            restoredNumber.setEnabled(false);
            if (restoredNumber.onKeyDown(KeyEvent.KEYCODE_DPAD_DOWN,
                    new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN))) {
                throw new AssertionError("disabled number picker consumed DPAD");
            }
            equal(restoredNumber.getMinValue(), restoredNumber.getValue(),
                    "disabled number-picker value");
            restoredNumber.setEnabled(true);

            SmartisanNumberPickerEx numberEx = new SmartisanNumberPickerEx(activity);
            numberEx.setId(0x100105);
            numberEx.setMinValue(0);
            numberEx.setMaxValue(10);
            numberEx.setValue(8);
            SparseArray<Parcelable> numberExState = new SparseArray<>();
            numberEx.saveHierarchyState(numberExState);
            SmartisanNumberPickerEx restoredNumberEx = new SmartisanNumberPickerEx(activity);
            restoredNumberEx.setId(numberEx.getId());
            restoredNumberEx.setMinValue(0);
            restoredNumberEx.setMaxValue(10);
            restoredNumberEx.restoreHierarchyState(roundTripState(numberExState));
            equal(8, restoredNumberEx.getValue(), "restored extended number-picker value");
            restoredNumberEx.setValue(restoredNumberEx.getMaxValue());
            if (!restoredNumberEx.onKeyDown(KeyEvent.KEYCODE_DPAD_DOWN,
                    new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN))) {
                throw new AssertionError("extended number picker did not wrap with DPAD");
            }
            equal(restoredNumberEx.getMinValue(), restoredNumberEx.getValue(),
                    "extended number-picker DPAD wrap");
            if (!restoredNumberEx.onKeyDown(KeyEvent.KEYCODE_DPAD_UP,
                    new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP))) {
                throw new AssertionError("extended number picker did not reverse-wrap with DPAD");
            }
            equal(restoredNumberEx.getMaxValue(), restoredNumberEx.getValue(),
                    "extended number-picker reverse DPAD wrap");

            SmartisanWheelTextView wheel = new SmartisanWheelTextView(activity);
            wheel.setId(0x100106);
            wheel.setDisplayedValues("One", "Two", "Three");
            wheel.setIsNeedRotate(true);
            wheel.setValue(2);
            SparseArray<Parcelable> wheelState = new SparseArray<>();
            wheel.saveHierarchyState(wheelState);
            SmartisanWheelTextView restoredWheel = new SmartisanWheelTextView(activity);
            restoredWheel.setId(wheel.getId());
            restoredWheel.restoreHierarchyState(roundTripState(wheelState));
            equal(2, restoredWheel.getValue(), "restored wheel value");
            if (!restoredWheel.performAccessibilityAction(
                    AccessibilityNodeInfo.ACTION_SCROLL_FORWARD, null)) {
                throw new AssertionError("restored wheel accessibility scroll was not handled");
            }
            equal(0, restoredWheel.getValue(), "restored rotating wheel accessibility value");

            SmartisanSpinnerView spinner = new SmartisanSpinnerView(activity);
            spinner.setId(org.opensmartisanos.ui.catalog.R.id.catalog_java_spinner_range);
            spinner.setSpinnerPickText(new String[] {"One", "Two", "Three"}, null);
            spinner.setSpinnerStyle(SmartisanSpinnerView.SPINNER_STYLE_RANGE);
            spinner.setIsNeedVerticalScroll(true);
            SmartisanWheelTextView spinnerWheel = spinner.findViewById(
                    R.id.smartisan_rom_spinner_text);
            spinnerWheel.setValue(2);
            SparseArray<Parcelable> spinnerState = new SparseArray<>();
            spinner.saveHierarchyState(spinnerState);
            SmartisanSpinnerView restoredSpinner = new SmartisanSpinnerView(activity);
            restoredSpinner.setId(spinner.getId());
            restoredSpinner.restoreHierarchyState(roundTripState(spinnerState));
            equal(SmartisanSpinnerView.SPINNER_STYLE_RANGE,
                    restoredSpinner.getSpinnerStyle(), "restored spinner style");
            SmartisanWheelTextView restoredSpinnerWheel = restoredSpinner.findViewById(
                    R.id.smartisan_rom_spinner_text);
            if (restoredSpinnerWheel == null) {
                throw new AssertionError("restored spinner wheel missing");
            }
            equal(2, restoredSpinnerWheel.getValue(), "restored spinner wheel value");
            if (!restoredSpinnerWheel.performAccessibilityAction(
                    AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD, null)) {
                throw new AssertionError("restored spinner accessibility scroll was not handled");
            }
            equal(1, restoredSpinnerWheel.getValue(),
                    "restored spinner accessibility value");

            LinearLayout spinnerHost = new LinearLayout(activity);
            SmartisanSpinnerView firstSpinner = new SmartisanSpinnerView(activity);
            firstSpinner.setId(0x100108);
            firstSpinner.setSpinnerPickText(new String[] {"一", "二", "三"}, null);
            firstSpinner.setSpinnerStyle(SmartisanSpinnerView.SPINNER_STYLE_RANGE);
            ((SmartisanWheelTextView) firstSpinner.findViewById(
                    R.id.smartisan_rom_spinner_text)).setValue(1);
            SmartisanSpinnerView secondSpinner = new SmartisanSpinnerView(activity);
            secondSpinner.setId(0x100109);
            secondSpinner.setSpinnerPickText(new String[] {"甲", "乙", "丙"}, null);
            secondSpinner.setSpinnerStyle(SmartisanSpinnerView.SPINNER_STYLE_RANGE);
            ((SmartisanWheelTextView) secondSpinner.findViewById(
                    R.id.smartisan_rom_spinner_text)).setValue(2);
            spinnerHost.addView(firstSpinner);
            spinnerHost.addView(secondSpinner);
            SparseArray<Parcelable> pairedState = new SparseArray<>();
            spinnerHost.saveHierarchyState(pairedState);

            LinearLayout restoredHost = new LinearLayout(activity);
            SmartisanSpinnerView restoredFirst = new SmartisanSpinnerView(activity);
            restoredFirst.setId(firstSpinner.getId());
            SmartisanSpinnerView restoredSecond = new SmartisanSpinnerView(activity);
            restoredSecond.setId(secondSpinner.getId());
            restoredHost.addView(restoredFirst);
            restoredHost.addView(restoredSecond);
            restoredHost.restoreHierarchyState(roundTripState(pairedState));
            equal(1, ((SmartisanWheelTextView) restoredFirst.findViewById(
                    R.id.smartisan_rom_spinner_text)).getValue(),
                    "first spinner retained independent child state");
            equal(2, ((SmartisanWheelTextView) restoredSecond.findViewById(
                    R.id.smartisan_rom_spinner_text)).getValue(),
                    "second spinner retained independent child state");
        });
    }

    private void verifyDismissOnlyMenuButton(Activity activity) {
        AtomicReference<SmartisanMenuDialog> reference = new AtomicReference<>();
        runOnMainSync(() -> {
            SmartisanMenuDialog dialog = new SmartisanMenuDialog(activity);
            dialog.setPositiveButton("确定", null);
            dialog.show();
            View button = dialog.findViewById(R.id.smartisan_rom_menu_dialog_ok);
            if (button == null) throw new AssertionError("positive button missing");
            button.performClick();
            reference.set(dialog);
        });
        waitForIdleSync();
        if (reference.get().isShowing()) {
            throw new AssertionError("dismiss-only positive button left dialog showing");
        }
    }

    private static void equal(int expected, int actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }

    private static void equal(long expected, long actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }

    private static void near(float expected, float actual, String label) {
        if (Math.abs(expected - actual) > 0.5f) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }

    private static void within(int expected, int actual, int tolerance, String label) {
        if (Math.abs(expected - actual) > tolerance) {
            throw new AssertionError(label + ": expected " + expected + " ± " + tolerance
                    + ", got " + actual);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static SparseArray<Parcelable> roundTripState(SparseArray<Parcelable> state) {
        Parcel parcel = Parcel.obtain();
        try {
            parcel.writeSparseArray(state);
            parcel.setDataPosition(0);
            return (SparseArray<Parcelable>) (SparseArray) parcel.readSparseArray(
                    CatalogPopupInstrumentation.class.getClassLoader());
        } finally {
            parcel.recycle();
        }
    }

    private static Bundle roundTripBundle(Bundle state) {
        Parcel parcel = Parcel.obtain();
        try {
            parcel.writeBundle(state);
            parcel.setDataPosition(0);
            Bundle restored = parcel.readBundle(
                    CatalogPopupInstrumentation.class.getClassLoader());
            if (restored == null) throw new AssertionError("dialog state parcel was empty");
            return restored;
        } finally {
            parcel.recycle();
        }
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
