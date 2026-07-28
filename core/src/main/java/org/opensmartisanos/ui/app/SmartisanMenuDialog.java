/* Ported from smartisanos.app.MenuDialog in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.app;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.opensmartisanos.ui.R;
import org.opensmartisanos.ui.widget.SmartisanDialogTitleBar;
import org.opensmartisanos.ui.widget.SmartisanShadowButton;

import java.util.List;

public class SmartisanMenuDialog extends Dialog implements DialogInterface.OnKeyListener {
    public static final int LOCATION_BOTTOM = 0;
    public static final int LOCATION_CENTER = 1;
    private final Context context;
    private final int location;
    private SmartisanDialogTitleBar titleBar;
    private ListView listView;
    private SmartisanShadowButton positiveButton;
    private SmartisanShadowButton negativeButton;
    private CharSequence title;
    private boolean titleSingleLine;
    private ListAdapter adapter;
    private AdapterView.OnItemClickListener itemClickListener;
    private CharSequence positiveText;
    private CharSequence negativeText;
    private View.OnClickListener positiveListener;
    private View.OnClickListener negativeListener;
    private SmartisanShadowButton.LongButtonStyle positiveStyle = SmartisanShadowButton.LongButtonStyle.HIGH_LIGHT;

    public SmartisanMenuDialog(Context context) { this(context, LOCATION_BOTTOM); }
    public SmartisanMenuDialog(Context context, int location) {
        super(context, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
        this.context = context; this.location = location; setOnKeyListener(this);
    }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xfff4f4f4);
        titleBar = new SmartisanDialogTitleBar(context);
        titleBar.setTitle(title); titleBar.setTitleSingleLine(titleSingleLine);
        titleBar.setLeftButtonVisibility(View.INVISIBLE);
        titleBar.setOnRightButtonClickListener(v -> dismiss());
        root.addView(titleBar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        listView = new ListView(context);
        listView.setDivider(null); listView.setAdapter(adapter); listView.setOnItemClickListener(itemClickListener);
        root.addView(listView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        LinearLayout actions = new LinearLayout(context);
        actions.setGravity(Gravity.CENTER); actions.setPadding(dp(8), 0, dp(8), dp(8));
        negativeButton = actionButton(negativeText, false);
        positiveButton = actionButton(positiveText, true);
        LinearLayout.LayoutParams actionParams = new LinearLayout.LayoutParams(0, dp(48), 1f);
        actionParams.leftMargin = dp(4); actionParams.rightMargin = dp(4);
        actions.addView(negativeButton, new LinearLayout.LayoutParams(actionParams));
        actions.addView(positiveButton, new LinearLayout.LayoutParams(actionParams));
        root.addView(actions, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56)));
        setContentView(root);
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND | WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setGravity(location == LOCATION_CENTER ? Gravity.CENTER : Gravity.BOTTOM);
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = location == LOCATION_CENTER ? dp(360) : WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT; window.setAttributes(params);
        }
    }

    private SmartisanShadowButton actionButton(CharSequence text, boolean positive) {
        SmartisanShadowButton button = new SmartisanShadowButton(context, null, 0,
                R.style.Widget_SmartisanUi_ShadowButton_Shrink);
        button.setText(text); button.setVisibility(text == null ? View.GONE : View.VISIBLE);
        button.updateBackgroundStyle(positive ? positiveStyle : SmartisanShadowButton.LongButtonStyle.GRAY);
        button.setOnClickListener(v -> {
            View.OnClickListener listener = positive ? positiveListener : negativeListener;
            if (listener != null) listener.onClick(v);
            if (positive) dismiss();
        });
        return button;
    }

    private int dp(float value) { return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f); }
    @Override public void setTitle(int resource) { setTitle(context.getText(resource)); }
    @Override public void setTitle(CharSequence value) { title = value; if (titleBar != null) titleBar.setTitle(value); }
    public void setTitleSingleLine(boolean value) { titleSingleLine = value; if (titleBar != null) titleBar.setTitleSingleLine(value); }
    public void setAdapter(ListAdapter value) { setAdapter(value, null); }
    public void setAdapter(ListAdapter value, AdapterView.OnItemClickListener listener) {
        adapter = value; itemClickListener = listener;
        if (listView != null) { listView.setAdapter(value); listView.setOnItemClickListener(listener); }
    }
    public ListView getListView() { return listView; }
    public SmartisanDialogTitleBar getTitleBar() { return titleBar; }
    public void setNegativeButton(int resource, View.OnClickListener listener) { setNegativeButton(context.getText(resource), listener); }
    public void setNegativeButton(CharSequence text, View.OnClickListener listener) {
        negativeText = text; negativeListener = listener;
        if (negativeButton != null) { negativeButton.setText(text); negativeButton.setVisibility(text == null ? View.GONE : View.VISIBLE); }
    }
    public void setNegativeImage(int resource, View.OnClickListener listener) { setNegativeButton(context.getText(android.R.string.cancel), listener); }
    public void setPositiveButton(int resource, View.OnClickListener listener) { setPositiveButton(context.getText(resource), listener); }
    public void setPositiveButton(CharSequence text, View.OnClickListener listener) {
        positiveText = text; positiveListener = listener;
        if (positiveButton != null) { positiveButton.setText(text); positiveButton.setVisibility(text == null ? View.GONE : View.VISIBLE); }
    }
    public void setPositiveButtonGone() { setPositiveButton((CharSequence) null, null); }
    public void setPositiveRedBackground(boolean red) { setPositiveBackgroundStyle(red ? SmartisanShadowButton.LongButtonStyle.RED : SmartisanShadowButton.LongButtonStyle.HIGH_LIGHT); }
    public void setPositiveBackgroundStyle(SmartisanShadowButton.LongButtonStyle style) {
        positiveStyle = style; if (positiveButton != null) positiveButton.updateBackgroundStyle(style);
    }
    @Override public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) { dismiss(); return true; }
        return false;
    }

    public static ListAdapter singleChoiceAdapter(Context context, List<? extends CharSequence> items) {
        return new TextAdapter(context, items, false);
    }
    public static ListAdapter multiChoiceAdapter(Context context, List<? extends CharSequence> items) {
        return new TextAdapter(context, items, true);
    }
    private static final class TextAdapter extends BaseAdapter {
        private final Context context; private final List<? extends CharSequence> items; private final boolean multi;
        TextAdapter(Context context, List<? extends CharSequence> items, boolean multi) { this.context = context; this.items = items; this.multi = multi; }
        @Override public int getCount() { return items.size(); }
        @Override public Object getItem(int position) { return items.get(position); }
        @Override public long getItemId(int position) { return position; }
        @Override public View getView(int position, View convertView, ViewGroup parent) {
            TextView view = convertView instanceof TextView ? (TextView) convertView : new TextView(context);
            view.setText(items.get(position)); view.setTextSize(17f); view.setTextColor(0xcc000000);
            view.setGravity(Gravity.CENTER_VERTICAL); view.setPadding(dp(context, 18), 0, dp(context, 18), 0);
            view.setMinHeight(dp(context, 48)); view.setCompoundDrawablePadding(dp(context, 8));
            if (multi) view.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.checkbox_on_background, 0);
            return view;
        }
        private static int dp(Context context, float value) { return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f); }
    }
}
