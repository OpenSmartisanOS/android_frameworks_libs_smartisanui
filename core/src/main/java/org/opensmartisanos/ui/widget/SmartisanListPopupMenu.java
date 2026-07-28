/* Ported from smartisanos.widget.support.SmartisanListPopupMenu in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.opensmartisanos.ui.R;

public class SmartisanListPopupMenu extends SmartisanPopupMenu {
    private final LinearLayout panel;
    private final TextView titleView;
    private final ListView listView;
    private final LinearLayout actionBar;
    private final ImageView leftAction;
    private final ImageView rightAction;
    private final Button leftButton;
    private final Button rightButton;
    private final TextView actionText;
    private boolean showingDividers = true;
    private boolean showPadding = true;

    public SmartisanListPopupMenu(Context context) { this(context, 0); }
    public SmartisanListPopupMenu(Context context, int width) {
        super(context);
        panel = new LinearLayout(context); panel.setOrientation(LinearLayout.VERTICAL);
        panel.setBackgroundResource(R.drawable.smartisan_rom_pop_up_menu_bg);
        int padding = dp(12); panel.setPadding(padding, padding, padding, padding);
        titleView = new TextView(context); titleView.setTextSize(12f); titleView.setTextColor(0x4c000000);
        titleView.setTypeface(titleView.getTypeface(), android.graphics.Typeface.BOLD); titleView.setGravity(Gravity.CENTER_VERTICAL);
        titleView.setVisibility(View.GONE); panel.addView(titleView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(36)));
        listView = new ListView(context); listView.setDivider(null);
        panel.addView(listView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        actionBar = new LinearLayout(context); actionBar.setGravity(Gravity.CENTER_VERTICAL); actionBar.setVisibility(View.GONE);
        leftAction = actionIcon(); rightAction = actionIcon(); leftButton = actionButton(); rightButton = actionButton();
        actionText = new TextView(context); actionText.setTextSize(10f); actionText.setTextColor(0x4c000000); actionText.setGravity(Gravity.CENTER);
        actionBar.addView(leftAction, new LinearLayout.LayoutParams(dp(36), dp(28)));
        actionBar.addView(leftButton, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(36)));
        actionBar.addView(actionText, new LinearLayout.LayoutParams(0, dp(36), 1f));
        actionBar.addView(rightButton, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(36)));
        actionBar.addView(rightAction, new LinearLayout.LayoutParams(dp(36), dp(28)));
        panel.addView(actionBar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));
        FrameLayout shadow = new FrameLayout(context);
        shadow.setBackgroundResource(R.drawable.smartisan_rom_popup_menu_bg_shadow);
        shadow.setPadding(dp(8), dp(8), dp(8), dp(8));
        shadow.addView(panel, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        menuPanelView = shadow;
        if (width > 0) setContentAreaWidth(width); else setContentAreaWidth(dp(240));
    }

    private ImageView actionIcon() { ImageView view = new ImageView(context); view.setScaleType(ImageView.ScaleType.CENTER); return view; }
    private Button actionButton() { Button button = new Button(context); button.setTextSize(10f); button.setVisibility(View.GONE); return button; }
    @Override protected void prepareShow() {
        int width = getPopupWindowWidth() - dp(40);
        int height = listView.getPaddingTop() + listView.getPaddingBottom();
        ListAdapter adapter = listView.getAdapter();
        View recycled = null;
        if (adapter != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                recycled = adapter.getView(i, recycled, listView);
                recycled.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                height += recycled.getMeasuredHeight();
            }
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = Math.min(height, dp(360));
        listView.setLayoutParams(params);
    }
    public void setAdapter(ListAdapter adapter) { listView.setAdapter(adapter); }
    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) { listView.setOnItemClickListener(listener); }
    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) { listView.setOnItemSelectedListener(listener); }
    public void setBottomActionBarVisible(boolean visible) { actionBar.setVisibility(visible ? View.VISIBLE : View.GONE); }
    public void setLeftButtonText(CharSequence text) { leftButton.setText(text); setTextOrIconViewVisibility(true, true); }
    public void setLeftImageViewResource(int resource) { leftAction.setImageResource(resource); setTextOrIconViewVisibility(true, false); }
    public void setLeftButtonOnClickListener(View.OnClickListener listener) { leftButton.setOnClickListener(listener); leftAction.setOnClickListener(listener); }
    public void setRightButtonText(CharSequence text) { rightButton.setText(text); setTextOrIconViewVisibility(false, true); }
    public void setRightImageViewResource(int resource) { rightAction.setImageResource(resource); setTextOrIconViewVisibility(false, false); }
    public void setRightButtonOnClickListener(View.OnClickListener listener) { rightButton.setOnClickListener(listener); rightAction.setOnClickListener(listener); }
    public void setLeftButtonVisible(boolean visible) { leftButton.setVisibility(visible ? View.VISIBLE : View.GONE); leftAction.setVisibility(visible ? View.VISIBLE : View.GONE); }
    public void setRightButtonVisible(boolean visible) { rightButton.setVisibility(visible ? View.VISIBLE : View.GONE); rightAction.setVisibility(visible ? View.VISIBLE : View.GONE); }
    public void setBottomText(CharSequence text) { actionText.setText(text); }
    public void setMenuListTitleVisible(boolean visible) { titleView.setVisibility(visible ? View.VISIBLE : View.GONE); }
    public void setMenuListTitle(CharSequence text) { titleView.setText(text); }
    public int getListViewHeight() { return listView.getHeight(); }
    public int getBottomActionBarHeight() { return actionBar.getVisibility() == View.VISIBLE ? actionBar.getHeight() : 0; }
    public int getMenuListTitleHeight() { return titleView.getVisibility() == View.VISIBLE ? titleView.getHeight() : 0; }
    public void setTextOrIconViewVisibility(boolean left, boolean useText) {
        (left ? leftButton : rightButton).setVisibility(useText ? View.VISIBLE : View.GONE);
        (left ? leftAction : rightAction).setVisibility(useText ? View.GONE : View.VISIBLE);
    }
    public boolean isShowingDividers() { return showingDividers; }
    public void setShowingDividers(boolean show) { showingDividers = show; listView.setDividerHeight(show ? 1 : 0); }
    public boolean isShowPadding() { return showPadding; }
    public void setShowPadding(boolean show) { showPadding = show; int p = show ? dp(12) : 0; panel.setPadding(p, p, p, p); }
    public ImageView getLeftActionView() { return leftAction; }
    public ImageView getRightActionView() { return rightAction; }
    public Button getLeftButton() { return leftButton; }
    public Button getRightButton() { return rightButton; }
    public ListView getListView() { return listView; }
}
