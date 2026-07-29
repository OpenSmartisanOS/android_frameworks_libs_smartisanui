/* Ported from smartisanos.widget.editor.EditorRightIconWidget. */
package org.opensmartisanos.ui.internal;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.opensmartisanos.ui.R;

public class SmartisanEditorRightIconWidget extends LinearLayout {
    private final View divider;
    private final ImageView icon;
    private final TextView label;
    public SmartisanEditorRightIconWidget(Context context) { this(context, null); }
    public SmartisanEditorRightIconWidget(Context context, AttributeSet attrs) { this(context, attrs, 0); }
    public SmartisanEditorRightIconWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(HORIZONTAL); setGravity(Gravity.CENTER_VERTICAL);
        setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.smartisan_rom_editor_left_right_widget_min_height));
        LayoutInflater.from(context).inflate(R.layout.smartisan_rom_editor_right_icon_widget_layout, this, true);
        label = findViewById(R.id.smartisan_rom_label);
        icon = findViewById(R.id.smartisan_rom_icon);
        divider = findViewById(R.id.smartisan_rom_devide);
    }
    public void setText(CharSequence text) { label.setVisibility(text == null ? GONE : VISIBLE); label.setText(text); }
    public void setText(int resId) { setText(getResources().getString(resId)); }
    public void setIconDrawable(Drawable drawable) { icon.setVisibility(drawable == null ? GONE : VISIBLE); icon.setImageDrawable(drawable); }
    public void setIconResource(int resId) { icon.setVisibility(VISIBLE); icon.setImageResource(resId); }
    public void setIconContentDescription(String description) { icon.setContentDescription(description); }
    public void setWidth(int width) { ViewGroup.LayoutParams params = getLayoutParams(); if (params == null) params = new LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT); params.width = width; setLayoutParams(params); }
    public void setDevideVisible(boolean visible) {
        divider.setVisibility(visible ? VISIBLE : GONE);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) icon.getLayoutParams();
        params.leftMargin = visible ? 0 : getResources().getDimensionPixelOffset(R.dimen.smartisan_rom_editor_horizontal_padding);
        icon.setLayoutParams(params);
    }
    public TextView getLabelTextView() { return label; }
    public ImageView getIcon() { return icon; }
}
