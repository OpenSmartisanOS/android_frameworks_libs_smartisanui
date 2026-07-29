/* Ported from smartisanos.app.SmartisanProgressDialog in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.app;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
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

    @SuppressLint("InflateParams")
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(context).inflate(
                R.layout.smartisan_rom_smartisan_progress_dialog, null);
        titleView = view.findViewById(R.id.smartisan_progress_dialog_title);
        progressBar = view.findViewById(R.id.smartisan_progress_dialog_progress);
        messageView = view.findViewById(R.id.smartisan_progress_dialog_message);
        content = view.findViewById(R.id.smartisan_progress_dialog_content);
        progressBar.setIndeterminate(true);
        setContentView(view);
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
        if (hideProgressBar) {
            progressBar.setIndeterminate(false);
            progressBar.setIndeterminateDrawable(null);
            progressBar.setVisibility(View.GONE);
        } else {
            Drawable drawable = indeterminateDrawable != null
                    ? indeterminateDrawable
                    : context.getDrawable(darkTheme
                            ? R.drawable.smartisan_rom_progress_medium_smartisanos_dark
                            : R.drawable.smartisan_rom_progress_medium_smartisanos_light);
            updateDrawableBounds(drawable, progressBar.getWidth(), progressBar.getHeight());
            progressBar.setIndeterminateDrawable(drawable);
            progressBar.setIndeterminate(true);
            progressBar.setVisibility(View.VISIBLE);
        }
        content.getRootView().setSystemUiVisibility(systemUiVisibility);
    }

    private void updateDrawableBounds(Drawable drawable, int width, int height) {
        if (drawable == null || width <= 0 || height <= 0) return;
        Rect padding = new Rect();
        drawable.getPadding(padding);
        int availableWidth = width - padding.left - padding.right;
        int availableHeight = height - padding.top - padding.bottom;
        int left = 0;
        int top = 0;
        int right = availableWidth;
        int bottom = availableHeight;
        if (!(drawable instanceof AnimationDrawable)) {
            int intrinsicWidth = drawable.getIntrinsicWidth();
            int intrinsicHeight = drawable.getIntrinsicHeight();
            if (intrinsicWidth > 0 && intrinsicHeight > 0) {
                float intrinsicAspect = (float) intrinsicWidth / intrinsicHeight;
                float boundAspect = (float) availableWidth / availableHeight;
                if (boundAspect > intrinsicAspect) {
                    int scaledWidth = Math.round(availableHeight * intrinsicAspect);
                    left = (availableWidth - scaledWidth) / 2;
                    right = left + scaledWidth;
                } else if (boundAspect < intrinsicAspect) {
                    int scaledHeight = Math.round(availableWidth / intrinsicAspect);
                    top = (availableHeight - scaledHeight) / 2;
                    bottom = top + scaledHeight;
                }
            }
        }
        drawable.setBounds(left, top, right, bottom);
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
