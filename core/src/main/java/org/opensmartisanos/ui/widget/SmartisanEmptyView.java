/* Ported from smartisanos.widget.SmartisanBlankView in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.opensmartisanos.ui.R;

public class SmartisanEmptyView extends LinearLayout {
    public static final int STYLE_NORMAL = 0;
    public static final int STYLE_SMALL = 1;
    public static final int STYLE_WITH_ACTION = 2;
    private final ImageView imageView;
    private final TextView primaryHintView;
    private final TextView secondaryHintView;
    private final TextView actionButton;
    private int emptyStyle;

    public SmartisanEmptyView(Context context) { this(context, null); }
    public SmartisanEmptyView(Context context, AttributeSet attrs) { this(context, attrs, 0); }
    public SmartisanEmptyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL); setGravity(Gravity.CENTER_HORIZONTAL);
        imageView = new ImageView(context);
        LayoutParams imageParams = new LayoutParams(dp(120), dp(120)); imageParams.topMargin = dp(24); addView(imageView, imageParams);
        primaryHintView = new TextView(context); primaryHintView.setTextSize(20f); primaryHintView.setTypeface(primaryHintView.getTypeface(), android.graphics.Typeface.BOLD);
        primaryHintView.setTextColor(0x26000000); primaryHintView.setGravity(Gravity.CENTER); primaryHintView.setSingleLine(true);
        LayoutParams primaryParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); primaryParams.setMargins(dp(60), dp(18), dp(60), 0); addView(primaryHintView, primaryParams);
        secondaryHintView = new TextView(context); secondaryHintView.setTextSize(13.5f); secondaryHintView.setTextColor(0x26000000); secondaryHintView.setGravity(Gravity.CENTER);
        LayoutParams secondaryParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); secondaryParams.setMargins(dp(60), dp(5), dp(60), dp(24)); addView(secondaryHintView, secondaryParams);
        actionButton = new TextView(context); actionButton.setGravity(Gravity.CENTER); actionButton.setVisibility(GONE); actionButton.setClickable(true);
        actionButton.setBackgroundResource(R.drawable.smartisan_rom_blank_option_btn_selector);
        LayoutParams actionParams = new LayoutParams(dp(60), dp(60)); actionParams.topMargin = dp(12); addView(actionButton, actionParams);
        setEmptyStyle(STYLE_NORMAL);
    }
    private int dp(float value) { return (int) (value * getResources().getDisplayMetrics().density + 0.5f); }
    public void setEmptyStyle(int style) {
        if (style < STYLE_NORMAL || style > STYLE_WITH_ACTION) throw new IllegalArgumentException("Unknown empty style: " + style);
        emptyStyle = style; boolean small = style != STYLE_NORMAL;
        LayoutParams params = (LayoutParams) imageView.getLayoutParams(); params.width = params.height = dp(small ? 72 : 120); params.topMargin = style == STYLE_WITH_ACTION ? 0 : dp(24); imageView.setLayoutParams(params);
        imageView.setImageResource(small ? R.drawable.smartisan_rom_blank_icon_small : R.drawable.smartisan_rom_blank_icon_large);
        actionButton.setVisibility(style == STYLE_WITH_ACTION ? VISIBLE : GONE);
    }
    public int getEmptyStyle() { return emptyStyle; }
    public void setPrimaryHint(CharSequence text) { primaryHintView.setText(text); }
    public void setSecondaryHint(CharSequence text) { secondaryHintView.setText(text); }
    public void setActionText(CharSequence text) { actionButton.setText(text); }
    public ImageView getImageView() { return imageView; }
    public TextView getPrimaryHintView() { return primaryHintView; }
    public TextView getSecondaryHintView() { return secondaryHintView; }
    public TextView getActionButton() { return actionButton; }
}
