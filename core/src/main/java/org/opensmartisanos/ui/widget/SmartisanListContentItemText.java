/* Ported from smartisanos.widget.ListContentItemText in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.opensmartisanos.ui.R;

public class SmartisanListContentItemText extends SmartisanListContentItem {
    private TextView subtitle;
    private ImageView arrow;
    private LinearLayout rightSlot;
    private boolean dirty;

    public SmartisanListContentItemText(Context context) { this(context, null); }
    public SmartisanListContentItemText(Context context, AttributeSet attrs) { this(context, attrs, 0); }
    public SmartisanListContentItemText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray values = context.obtainStyledAttributes(attrs,
                R.styleable.SmartisanListContentItemText, defStyleAttr, 0);
        boolean showArrow = values.getBoolean(
                R.styleable.SmartisanListContentItemText_show_arrow, true);
        String text = values.getString(R.styleable.SmartisanListContentItemText_subTitle);
        values.recycle();
        setArrowVisible(showArrow);
        setSubtitle(text);
    }

    @Override protected int getDefaultRightLayout() {
        return R.layout.smartisan_rom_list_content_right_subtitle_arrow;
    }
    @Override protected void initRightWidget() {
        subtitle = findViewById(R.id.smartisan_rom_subtitle);
        arrow = findViewById(R.id.smartisan_rom_arrow);
        rightSlot = findViewById(R.id.smartisan_rom_rightExpandView);
    }
    @Override protected boolean rightContainerHasFixedWidth() { return false; }
    @Override public void setTitle(CharSequence value) { if (!customMidView) super.setTitle(value); }
    @Override public void setSummary(CharSequence value) { if (!customMidView) super.setSummary(value); }

    public void setSubtitle(CharSequence value) {
        if (!customRightView && subtitle != null) {
            subtitle.setVisibility(TextUtils.isEmpty(value) ? GONE : VISIBLE);
            subtitle.setText(value);
        }
    }
    public void setSubtitle(int resId) { setSubtitle(getContext().getString(resId)); }
    public CharSequence getSubTitle() { return customRightView ? null : subtitle.getText(); }
    public TextView getSubTitleView() { return subtitle; }
    public ImageView getArrowImageView() { return arrow; }

    public void setArrowVisible(boolean visible) {
        if (!customRightView) {
            arrow.setVisibility(visible ? VISIBLE : GONE);
            setSubtitleRightPadding(visible ? 0 : getResources().getDimensionPixelSize(
                    R.dimen.smartisan_rom_list_right_text_right_padding));
        }
    }

    public void setSubtitleRightPadding(int padding) {
        subtitle.setPadding(subtitle.getPaddingLeft(), subtitle.getPaddingTop(), padding,
                subtitle.getPaddingBottom());
    }

    @Override protected float getRightContentWidth() {
        if (customRightView) return rightContainer.getMeasuredWidth();
        float width = textWidth(subtitle) + subtitle.getPaddingRight();
        if (arrow.getVisibility() != GONE) width += arrow.getDrawable().getIntrinsicWidth();
        if (hasRightExpandView()) width += rightSlot.getMeasuredWidth();
        return width + getResources().getDimensionPixelSize(R.dimen.smartisan_rom_right_container_margin)
                + getResources().getDimensionPixelSize(R.dimen.smartisan_rom_flexible_space);
    }

    private void limitContainerWidth() {
        if (customMidView || customRightView || rightContainerHasFixedWidth() || getMeasuredWidth() <= 0) return;
        float left = getLeftContentWidth();
        float mid = getMidContentWidth();
        float right = getRightContentWidth();
        if (left + mid + right > getMeasuredWidth()) {
            int midLimit = getResources().getDimensionPixelSize(
                    R.dimen.smartisan_rom_item_text_mid_width_limit);
            if (left == 0f) midLimit += getResources().getDimensionPixelSize(
                    R.dimen.smartisan_rom_left_icon_area_width);
            float rightMax;
            if (mid > midLimit) {
                setMidContainerWidth(midLimit);
                rightMax = getMeasuredWidth() - left - midLimit;
                dirty = true;
            } else {
                rightMax = getMeasuredWidth() - left - mid;
            }
            if (rightMax > 0) {
                subtitle.setMaxWidth((int) (rightMax - (right - textWidth(subtitle)
                        - subtitle.getPaddingRight())));
                dirty = true;
            }
        } else if (subtitle.getMaxWidth() != Integer.MAX_VALUE) {
            subtitle.setMaxWidth(Integer.MAX_VALUE);
            dirty = true;
        }
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        limitContainerWidth();
        if (dirty) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            dirty = false;
        }
    }

    public void setRightExpandView(View view) {
        if (customRightView) return;
        rightSlot.removeAllViews();
        if (view == null) rightSlot.setVisibility(GONE);
        else { rightSlot.addView(view); rightSlot.setVisibility(VISIBLE); }
    }
    public boolean hasRightExpandView() {
        for (int i = 0; i < rightSlot.getChildCount(); i++) {
            if (rightSlot.getChildAt(i).getVisibility() != GONE) return true;
        }
        return false;
    }
}
