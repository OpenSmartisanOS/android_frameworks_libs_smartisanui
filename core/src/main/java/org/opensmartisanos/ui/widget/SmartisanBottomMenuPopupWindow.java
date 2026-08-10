package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.PopupWindow;

import org.opensmartisanos.ui.R;
import org.opensmartisanos.ui.internal.SmartisanMenuMath;

import java.util.ArrayList;
import java.util.List;

/** Public-SDK port of the original Smartisan bottom action menu. */
public class SmartisanBottomMenuPopupWindow extends PopupWindow implements View.OnClickListener {
    public static final int MENU_ITEM_COLUMN = 4;
    public static final int MENU_ITEM_COLUMN_LAND = 3;

    private Context context;
    protected GridView mGridView;
    private View parentPanel;
    private SmartisanDialogTitleBar titleBar;
    private SmartisanBottomMenuAdapter adapter;
    private Animation runningAnimation;
    private int requestedColumns;

    public SmartisanBottomMenuPopupWindow(Context context) {
        super(context);
        this.context = context;
        initLayout(context);
    }

    /** Original subclass hook retained with a public-SDK implementation. */
    protected void initLayout(Context context) {
        setBackgroundDrawable(new ColorDrawable(
                context.getColor(R.color.smartisan_rom_bottom_menu_scrim)));
        setFocusable(true);
        setTouchable(true);

        View content = LayoutInflater.from(context).inflate(
                R.layout.smartisan_rom_bottom_menu, null);
        View container = content.findViewById(R.id.smartisan_rom_bottom_menu_container);
        container.setOnClickListener(this);
        parentPanel = content.findViewById(R.id.smartisan_rom_bottom_menu_parent_panel);
        mGridView = content.findViewById(R.id.smartisan_rom_bottom_menu_grid);
        mGridView.setSelector(new ColorDrawable(0));

        titleBar = content.findViewById(R.id.smartisan_rom_bottom_menu_title_bar);
        if (titleBar != null) {
            titleBar.setTitle(R.string.smartisan_bottom_menu_title);
            titleBar.setLeftButtonVisibility(View.INVISIBLE);
            titleBar.addCancelImage(false);
            titleBar.setRightButtonVisibility(View.VISIBLE);
            titleBar.setOnRightButtonClickListener(this);
        }
        setContentView(content);
        content.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        content.setOnApplyWindowInsetsListener((view, insets) -> {
            applyWindowInsets(insets);
            return insets;
        });
    }

    public void setAdapter(SmartisanBottomMenuAdapter value) {
        adapter = value;
        updateColumnCount();
        mGridView.setAdapter(value);
    }

    public SmartisanBottomMenuAdapter getAdapter() {
        return adapter;
    }

    public void setNumColumns(int columns) {
        requestedColumns = columns;
        updateColumnCount();
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        mGridView.setOnItemClickListener(listener);
    }

    public SmartisanDialogTitleBar getTitleBar() {
        return titleBar;
    }

    public GridView getGridView() {
        return mGridView;
    }

    @Override
    public void onClick(View view) {
        dismissWithAnimation();
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        if (runningAnimation != null) return;
        updateColumnCount();
        super.showAtLocation(parent, gravity, x, y);
        getContentView().requestApplyInsets();
        Animation entrance = AnimationUtils.loadAnimation(
                context, R.anim.smartisan_bottom_menu_in);
        runningAnimation = entrance;
        entrance.setAnimationListener(new AnimationEndListener() {
            @Override public void onAnimationEnd(Animation animation) {
                clearAnimationIfCurrent(animation);
            }
        });
        parentPanel.startAnimation(entrance);
        // Some vendor View implementations finish an AnimationSet without delivering its end
        // listener. Use the exact resource duration as a guarded fallback so the popup cannot stay
        // permanently locked in its entrance state.
        parentPanel.postDelayed(() -> clearAnimationIfCurrent(entrance),
                entrance.computeDurationHint() + 32L);
    }

    public void showBrowserMenu(View parent, Menu menu, int childHeight,
            int popupWidth, int popupHeight) {
        showBrowserMenu(parent, menu, childHeight, popupWidth, popupHeight, 0, 0);
    }

    public void showBrowserMenu(View parent, Menu menu, int childHeight,
            int popupWidth, int popupHeight, int offsetX, int offsetY) {
        List<MenuItem> items = new ArrayList<>();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (item.isVisible()) items.add(item);
        }
        SmartisanBottomMenuAdapter value = new SmartisanBottomMenuAdapter(context, items);
        value.setChildHeight(childHeight);
        setAdapter(value);
        setWidth(popupWidth);
        setHeight(popupHeight);
        showAtLocation(parent, 0, offsetX, offsetY);
    }

    private void updateColumnCount() {
        int columns = requestedColumns;
        if (columns <= 0) {
            columns = SmartisanMenuMath.defaultBottomMenuColumns(
                    context.getResources().getConfiguration().orientation);
        }
        mGridView.setNumColumns(columns);
    }

    @SuppressWarnings("deprecation")
    private void applyWindowInsets(WindowInsets insets) {
        if (parentPanel == null) return;
        int left = insets.getSystemWindowInsetLeft();
        int top = insets.getSystemWindowInsetTop();
        int right = insets.getSystemWindowInsetRight();
        int bottom = insets.getSystemWindowInsetBottom();
        boolean landscape = context.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
        // Keep the dim/scrim layer full screen while moving the interactive surface clear of
        // gesture/navigation bars and display cutouts.
        if (landscape) {
            parentPanel.setPadding(left, top, right, bottom);
        } else {
            parentPanel.setPadding(left, 0, right, bottom);
        }
    }

    private void dismissWithAnimation() {
        if (runningAnimation != null) return;
        Animation exit = AnimationUtils.loadAnimation(context, R.anim.smartisan_bottom_menu_out);
        runningAnimation = exit;
        exit.setFillAfter(true);
        exit.setAnimationListener(new AnimationEndListener() {
            @Override public void onAnimationEnd(Animation animation) {
                finishDismissIfCurrent(animation);
            }
        });
        parentPanel.startAnimation(exit);
        parentPanel.postDelayed(() -> finishDismissIfCurrent(exit),
                exit.computeDurationHint() + 32L);
    }

    private void clearAnimationIfCurrent(Animation animation) {
        if (runningAnimation == animation) runningAnimation = null;
    }

    private void finishDismissIfCurrent(Animation animation) {
        if (runningAnimation != animation) return;
        runningAnimation = null;
        super.dismiss();
    }

    private abstract static class AnimationEndListener implements Animation.AnimationListener {
        @Override public void onAnimationStart(Animation animation) {}
        @Override public void onAnimationRepeat(Animation animation) {}
    }
}
