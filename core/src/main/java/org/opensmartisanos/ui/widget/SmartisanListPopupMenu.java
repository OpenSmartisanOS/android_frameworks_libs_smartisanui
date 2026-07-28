/* Ported from smartisanos.widget.support.SmartisanListPopupMenu in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.opensmartisanos.ui.R;

public class SmartisanListPopupMenu extends SmartisanPopupMenu {
    private final LinearLayout titleContainer;
    private final ListView listView;
    private final TextView titleView;
    private final View bottomActionBar;
    private final View bottomDivider;
    private final ImageView leftAction;
    private final ImageView rightAction;
    private final Button leftButton;
    private final Button rightButton;
    private final TextView actionText;
    private final int listVerticalPadding;

    private ListAdapter adapter;
    private AdapterView.OnItemClickListener itemClickListener;
    private AdapterView.OnItemSelectedListener itemSelectedListener;
    private View.OnClickListener leftClickListener;
    private View.OnClickListener rightClickListener;
    private boolean showBottomActionBar;
    private boolean showMenuListTitle;
    private boolean showingDividers = true;
    private boolean showPadding;

    public SmartisanListPopupMenu(Context context) {
        super(context);
        contentAreaWidth = context.getResources().getDimensionPixelSize(
                R.dimen.smartisan_rom_popup_list_menu_default_width);
        menuPanelView = LayoutInflater.from(context).inflate(
                R.layout.smartisan_rom_menu_popupwindow_layout, null);
        titleContainer = menuPanelView.findViewById(R.id.smartisan_rom_menu_title_container);
        listView = menuPanelView.findViewById(R.id.smartisan_rom_menu_list);
        titleView = menuPanelView.findViewById(R.id.smartisan_rom_menu_title);
        bottomActionBar = menuPanelView.findViewById(R.id.smartisan_rom_bottom_action_bar);
        bottomDivider = menuPanelView.findViewById(R.id.smartisan_rom_menu_list_bottom_divider);
        leftAction = menuPanelView.findViewById(R.id.smartisan_rom_left_icon);
        rightAction = menuPanelView.findViewById(R.id.smartisan_rom_right_icon);
        leftButton = menuPanelView.findViewById(R.id.smartisan_rom_left_btn);
        rightButton = menuPanelView.findViewById(R.id.smartisan_rom_right_btn);
        actionText = menuPanelView.findViewById(R.id.smartisan_rom_action_text);
        listVerticalPadding = context.getResources().getDimensionPixelOffset(
                R.dimen.smartisan_rom_popup_list_menu_padding_vertical);
    }

    /** Width follows the ROM API and includes the left and right shadow areas. */
    public SmartisanListPopupMenu(Context context, int width) {
        this(context);
        contentAreaWidth = width - bgLeftRightShadowWidth * 2;
        if (contentAreaWidth <= 0) throw new IllegalArgumentException("width is smaller than popup shadows");
    }

    @Override protected void prepareShow() {
        bottomActionBar.setVisibility(showBottomActionBar ? View.VISIBLE : View.GONE);
        bottomDivider.setVisibility(showBottomActionBar ? View.VISIBLE : View.GONE);
        titleContainer.setVisibility(showMenuListTitle ? View.VISIBLE : View.GONE);
        View left = leftButton.getVisibility() == View.VISIBLE ? leftButton : leftAction;
        View right = rightButton.getVisibility() == View.VISIBLE ? rightButton : rightAction;
        left.setOnClickListener(leftClickListener);
        right.setOnClickListener(rightClickListener);
        buildList();

        // PopupWindow measures weighted children against available height. Give the ROM's 0dp
        // list track its intrinsic height first so the same XML also works in ordinary AAR apps.
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = getListViewHeight();
        listView.setLayoutParams(params);
    }

    private void buildList() {
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(itemClickListener);
        listView.setOnItemSelectedListener(itemSelectedListener);
        listView.setFocusable(true);
        listView.setDivider(showingDividers
                ? context.getDrawable(R.drawable.smartisan_rom_list_divider_drawable) : null);
        listView.setSelector(R.drawable.smartisan_rom_menu_list_selector);
        int horizontalLeft = listView.getPaddingLeft();
        int horizontalRight = listView.getPaddingRight();
        int vertical = showPadding ? listVerticalPadding : 0;
        listView.setPadding(horizontalLeft, vertical, horizontalRight, vertical);
        listView.setClipToPadding(!showPadding);
        listView.setFocusableInTouchMode(true);
    }

    public void setAdapter(ListAdapter adapter) {
        this.adapter = adapter;
        listView.setAdapter(adapter);
    }
    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) { itemClickListener = listener; }
    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) { itemSelectedListener = listener; }
    public void setBottomActionBarVisible(boolean visible) { showBottomActionBar = visible; }
    public void setLeftButtonText(CharSequence text) { leftButton.setText(text); }
    public void setLeftImageViewRes(int resource) { leftAction.setImageResource(resource); }
    public void setLeftImageViewResource(int resource) { setLeftImageViewRes(resource); }
    public void setLeftButtonOnClickListener(View.OnClickListener listener) { leftClickListener = listener; }
    public void setRightButtonText(CharSequence text) { rightButton.setText(text); }
    public void setRightImageViewRes(int resource) { rightAction.setImageResource(resource); }
    public void setRightImageViewResource(int resource) { setRightImageViewRes(resource); }
    public void setRightButtonOnClickListener(View.OnClickListener listener) { rightClickListener = listener; }
    public void setLeftButtonVisible(boolean visible) { leftAction.setVisibility(visible ? View.VISIBLE : View.GONE); }
    public void setRightButtonVisible(boolean visible) { rightAction.setVisibility(visible ? View.VISIBLE : View.GONE); }
    public void setBottomText(CharSequence text) { actionText.setText(text); }
    public void setMenuListTitleVisible(boolean visible) { showMenuListTitle = visible; }
    public void setMenuListTitle(CharSequence text) { titleView.setText(text); }

    public int getListViewHeight() {
        if (adapter == null) return showPadding ? listVerticalPadding * 2 : 0;
        int height = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View item = adapter.getView(i, null, listView);
            item.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            height += item.getMeasuredHeight();
            if (showingDividers) height += listView.getDividerHeight();
        }
        return height + (showPadding ? listVerticalPadding * 2 : 0);
    }

    public int getBottomActionBarHeight() {
        if (bottomActionBar.getVisibility() == View.GONE) return 0;
        bottomActionBar.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        return bottomActionBar.getMeasuredHeight();
    }

    public int getMenuListTitleHeight() {
        return titleView.getVisibility() == View.GONE ? 0
                : context.getResources().getDimensionPixelSize(
                        R.dimen.smartisan_rom_popup_list_menu_title_height);
    }

    public void setTextOrIconViewVisibility(boolean left, boolean useText) {
        View button = left ? leftButton : rightButton;
        View icon = left ? leftAction : rightAction;
        int rule = left ? RelativeLayout.RIGHT_OF : RelativeLayout.LEFT_OF;
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) actionText.getLayoutParams();
        if (useText) {
            button.setVisibility(View.VISIBLE);
            icon.setVisibility(View.GONE);
            params.addRule(rule, button.getId());
        } else {
            icon.setVisibility(View.VISIBLE);
            button.setVisibility(View.GONE);
            params.addRule(rule, icon.getId());
        }
        actionText.setLayoutParams(params);
    }

    public boolean isShowingDividers() { return showingDividers; }
    public void setShowingDividers(boolean show) { showingDividers = show; }
    public boolean isShowPadding() { return showPadding; }
    public void setShowPadding(boolean show) { showPadding = show; }
    public ImageView getLeftActionView() { return leftAction; }
    public ImageView getRightActionView() { return rightAction; }
    public Button getLeftButton() { return leftButton; }
    public Button getRightButton() { return rightButton; }
    public ListView getListView() { return listView; }
}
