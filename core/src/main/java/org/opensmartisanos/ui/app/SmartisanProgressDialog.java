/* Ported from smartisanos.app.SmartisanProgressDialog in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.app;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.opensmartisanos.ui.R;

public class SmartisanProgressDialog extends Dialog {
    private final Context context;
    private CharSequence dialogTitle;
    private CharSequence message;
    private Drawable background;
    private Drawable indeterminateDrawable;
    private boolean hideProgressBar;
    private boolean darkTheme;
    private boolean customTitleColor;
    private boolean customMessageColor;
    private int titleColor = 0x9c000000;
    private int messageColor = 0x9c000000;
    private int systemUiVisibility;
    private LinearLayout content;
    private TextView titleView;
    private TextView messageView;
    private ProgressBar progressBar;

    public SmartisanProgressDialog(Context context) { super(context); this.context = context; }

    public static SmartisanProgressDialog show(Context context, CharSequence title, CharSequence message) {
        SmartisanProgressDialog dialog = new SmartisanProgressDialog(context);
        dialog.setTitle(title); dialog.setMessage(message); dialog.show(); return dialog;
    }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER);
        content.setPadding(dp(30), dp(8), dp(30), dp(8));

        titleView = new TextView(context);
        titleView.setTextSize(18f); titleView.setGravity(Gravity.CENTER); titleView.setSingleLine(true);
        content.addView(titleView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(dp(48), dp(48));
        progressParams.topMargin = dp(2); content.addView(progressBar, progressParams);
        messageView = new TextView(context);
        messageView.setTextSize(13f); messageView.setGravity(Gravity.CENTER); messageView.setSingleLine(true);
        LinearLayout.LayoutParams messageParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        messageParams.topMargin = dp(8); messageParams.bottomMargin = dp(8); content.addView(messageView, messageParams);
        setContentView(content, new ViewGroup.LayoutParams(dp(246), ViewGroup.LayoutParams.WRAP_CONTENT));
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams params = window.getAttributes(); params.width = dp(246); window.setAttributes(params);
        }
        applyState();
    }

    @Override protected void onStart() { super.onStart(); applyState(); }

    private void applyState() {
        if (content == null) return;
        titleView.setText(dialogTitle); titleView.setTextColor(titleColor); titleView.setVisibility(dialogTitle == null ? View.GONE : View.VISIBLE);
        messageView.setText(message); messageView.setTextColor(messageColor); messageView.setVisibility(message == null ? View.GONE : View.VISIBLE);
        content.setBackground(background != null ? background : context.getDrawable(darkTheme
                ? R.drawable.smartisan_rom_smartisan_progress_dialog_bg_dark
                : R.drawable.smartisan_rom_smartisan_progress_dialog_bg));
        if (indeterminateDrawable != null) progressBar.setIndeterminateDrawable(indeterminateDrawable);
        progressBar.setVisibility(hideProgressBar ? View.GONE : View.VISIBLE);
        content.getRootView().setSystemUiVisibility(systemUiVisibility);
    }

    private int dp(float value) { return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f); }
    public void setDarkTheme(boolean dark) {
        darkTheme = dark;
        if (!customTitleColor) titleColor = dark ? 0xffffffff : 0x9c000000;
        if (!customMessageColor) messageColor = dark ? 0xffffffff : 0x9c000000;
        applyState();
    }
    @Override public void setTitle(int resource) { setTitle(context.getText(resource)); }
    @Override public void setTitle(CharSequence title) { dialogTitle = title; applyState(); }
    public void setMessage(int resource) { setMessage(context.getText(resource)); }
    public void setMessage(CharSequence value) { message = value; applyState(); }
    public void setIndeterminateDrawableResource(int resource) { setIndeterminateDrawable(context.getDrawable(resource)); }
    public void setIndeterminateDrawable(Drawable drawable) { indeterminateDrawable = drawable; applyState(); }
    public void setHideProgressBar(boolean hide) { hideProgressBar = hide; applyState(); }
    public void setBackgroundResource(int resource) { setBackground(context.getDrawable(resource)); }
    public void setBackground(Drawable drawable) { background = drawable; applyState(); }
    public void setSystemUiVisibility(int visibility) { systemUiVisibility = visibility; applyState(); }
    public void setTitleColor(int color) { customTitleColor = true; titleColor = color; applyState(); }
    public void setMessageColor(int color) { customMessageColor = true; messageColor = color; applyState(); }
}
