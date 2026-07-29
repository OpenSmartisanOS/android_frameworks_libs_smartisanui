/* Ported from smartisanos.widget.editor.EditorLeftLabelWidget. */
package org.opensmartisanos.ui.internal;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.opensmartisanos.ui.R;

public class SmartisanEditorLeftLabelWidget extends LinearLayout {
    private final ImageView arrow;
    private final View divider;
    private final ImageView icon;
    private final FrameLayout iconContainer;
    private final TextView label;
    private final LinearLayout rightSlot;
    private OnClickListener iconClickListener;
    private OnClickListener labelClickListener;

    public SmartisanEditorLeftLabelWidget(Context context) { this(context, null); }
    public SmartisanEditorLeftLabelWidget(Context context, AttributeSet attrs) { this(context, attrs, 0); }
    public SmartisanEditorLeftLabelWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setMinimumHeight(getResources().getDimensionPixelSize(
                R.dimen.smartisan_rom_editor_left_right_widget_min_height));
        LayoutInflater.from(context).inflate(R.layout.smartisan_rom_editor_left_label_layout, this, true);
        icon = findViewById(R.id.smartisan_rom_icon);
        label = findViewById(R.id.smartisan_rom_label);
        arrow = findViewById(R.id.smartisan_rom_arrow);
        rightSlot = findViewById(R.id.smartisan_rom_rightExpandView);
        iconContainer = findViewById(R.id.smartisan_rom_iconContainer);
        divider = findViewById(R.id.smartisan_rom_divider);
        icon.setOnClickListener(view -> { if (iconClickListener != null && icon.isShown()) iconClickListener.onClick(view); });
        label.setOnClickListener(view -> { if (labelClickListener != null) labelClickListener.onClick(view); });
    }
    public void setIconContainerBackground(int drawable) { iconContainer.setBackgroundResource(drawable); }
    public void setIconDrawable(Drawable drawable) {
        iconContainer.setVisibility(drawable == null ? GONE : VISIBLE);
        icon.setImageDrawable(drawable);
    }
    public void setIconResource(int resId) { iconContainer.setVisibility(VISIBLE); icon.setImageResource(resId); }
    public void setOnIconClickListener(OnClickListener listener) { iconClickListener = listener; }
    public void setOnLabelClickListener(OnClickListener listener) { labelClickListener = listener; }
    public void setArrowVisible(boolean visible) {
        arrow.setVisibility(visible ? VISIBLE : GONE);
        int margin = getResources().getDimensionPixelSize(R.dimen.smartisan_rom_editor_element_margin_left_right);
        setArrowRightMargin(visible ? margin : 0);
        setLabelRightMargin(visible ? 0 : margin);
    }
    public void setLabelRightMargin(int margin) { margin(label, false, margin); }
    public void setLabelLeftMargin(int margin) { margin(label, true, margin); }
    private void setArrowRightMargin(int margin) { margin(arrow, false, margin); }
    private void margin(View view, boolean left, int value) {
        LayoutParams params = (LayoutParams) view.getLayoutParams();
        if (left) params.leftMargin = value; else params.rightMargin = value;
        view.setLayoutParams(params);
    }
    public void setLabelGravity(int gravity) {
        int padding = gravity == Gravity.TOP ? 30 : 18;
        label.setPadding(0, padding, 0, padding);
        label.setGravity(gravity);
    }
    public void setText(CharSequence text) { label.setText(text); label.setVisibility(TextUtils.isEmpty(text) ? GONE : VISIBLE); }
    public void setText(int resId) { setText(getResources().getString(resId)); }
    public CharSequence getText() { return label.getText(); }
    public float getTextWidth() { return label.getPaint().measureText(label.getText().toString()); }
    public void setScale(int width, int height) {
        android.view.ViewGroup.LayoutParams params = getLayoutParams();
        if (params == null) params = new FrameLayout.LayoutParams(width, height);
        params.width = width; params.height = height; setLayoutParams(params);
    }
    public void setLabelWidth(int width) { LayoutParams params = (LayoutParams) label.getLayoutParams(); params.width = width; label.setLayoutParams(params); }
    public void setRightExpandView(View view) {
        rightSlot.removeAllViews();
        if (view == null) rightSlot.setVisibility(GONE);
        else { rightSlot.addView(view); rightSlot.setVisibility(VISIBLE); }
    }
    public View getIconView() { return icon; }
    public void setShowDivider(boolean show) { divider.setVisibility(show ? VISIBLE : GONE); }
}
