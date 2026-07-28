/* Ported from smartisanos.widget.ShadowButton in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TextView;

import org.opensmartisanos.ui.R;
import org.opensmartisanos.ui.internal.SmartisanShadowDrawable;

public class SmartisanShadowButton extends Button {
    private static final int SMALL_BUTTON = 1;
    private static final int LONG_BUTTON = 2;
    private static final int SHRINK_LONG_BUTTON = 3;

    public enum LongButtonStyle { HIGH_LIGHT, RED, WHITE, GRAY }
    public enum SmallButtonStyle { HIGH_LIGHT, RED, STANDARD }

    public interface OnDisabledClickListener {
        void onDisabledClick();
    }

    private int buttonStyle;
    private boolean enableTextShadow = true;
    private boolean backgroundShadowEnabled = true;
    private boolean shadowShouldProject = true;
    private ColorStateList shadowColors;
    private float shadowDx;
    private float shadowDy;
    private float shadowRadius;
    private OnDisabledClickListener disabledClickListener;

    public SmartisanShadowButton(Context context) {
        this(context, null);
    }

    public SmartisanShadowButton(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.buttonStyle);
    }

    public SmartisanShadowButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SmartisanShadowButton(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray values = context.obtainStyledAttributes(attrs,
                R.styleable.SmartisanShadowButton, defStyleAttr, defStyleRes);
        shadowColors = values.getColorStateList(R.styleable.SmartisanShadowButton_shadowColors);
        shadowDx = values.getFloat(R.styleable.SmartisanShadowButton_android_shadowDx, 0f);
        shadowDy = values.getFloat(R.styleable.SmartisanShadowButton_android_shadowDy, 0f);
        shadowRadius = values.getFloat(
                R.styleable.SmartisanShadowButton_android_shadowRadius, 0f);
        backgroundShadowEnabled = values.getBoolean(
                R.styleable.SmartisanShadowButton_shadow_enabled, true);
        shadowShouldProject = values.getBoolean(
                R.styleable.SmartisanShadowButton_shadow_project, true);
        buttonStyle = values.getInt(R.styleable.SmartisanShadowButton_shadowButtonStyle, 0);
        values.recycle();

        updateTextShadow();
        if (getBackground() != null) {
            setBackground(getBackground());
        }
        limitMaxSizeIfNeeded();
    }

    @Override
    public void setText(CharSequence text, TextView.BufferType type) {
        super.setText(text, type);
        limitMaxSizeIfNeeded();
    }

    private void limitMaxSizeIfNeeded() {
        if (buttonStyle == 0 || buttonStyle == SMALL_BUTTON || getText() == null) {
            return;
        }
        setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.smartisan_shadow_button_text_size));
        float required = getPaint().measureText(getText().toString());
        int textMaxWidth = getResources().getDimensionPixelSize(
                R.dimen.smartisan_shadow_button_text_max_width);
        int maxWidth = getMaxWidth();
        if (required > textMaxWidth || (maxWidth > 0 && maxWidth < Integer.MAX_VALUE
                && required > maxWidth)) {
            setMaxTitleSize(getResources().getDimensionPixelSize(
                    R.dimen.smartisan_shadow_button_common_max_size));
        }
    }

    public void setMaxTitleSize(float maxTextSize) {
        if (getText() == null || getText().length() == 0) {
            return;
        }
        float size = getTextSize();
        float available = getMaxWidth() - getPaddingLeft() - getPaddingRight();
        if (available <= 0 || getMaxWidth() == Integer.MAX_VALUE) {
            available = getResources().getDimension(
                    R.dimen.smartisan_shadow_button_text_max_width);
        }
        while (size > maxTextSize && getPaint().measureText(getText().toString()) > available) {
            size -= 1f;
            setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        }
    }

    public void setShadowColors(ColorStateList colors, float radius, float dx, float dy) {
        shadowColors = colors;
        shadowRadius = radius;
        shadowDx = dx;
        shadowDy = dy;
        updateTextShadow();
    }

    public void setShadowEnable(boolean enabled) {
        enableTextShadow = enabled;
        refreshDrawableState();
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        updateTextShadow();
    }

    private void updateTextShadow() {
        int color = shadowColors == null ? 0
                : shadowColors.getColorForState(getDrawableState(), shadowColors.getDefaultColor());
        setShadowLayer(enableTextShadow ? shadowRadius : 0f,
                enableTextShadow ? shadowDx : 0f, enableTextShadow ? shadowDy : 0f, color);
    }

    public void updateBackgroundStyle(LongButtonStyle style) {
        if (style == null) {
            return;
        }
        boolean shrink = buttonStyle == SHRINK_LONG_BUTTON;
        int background;
        boolean normalText = false;
        boolean textShadow = true;
        switch (style) {
            case HIGH_LIGHT:
                background = shrink
                        ? R.drawable.smartisan_rom_shrink_long_btn_highlight_selector
                        : R.drawable.smartisan_rom_selector_long_btn_highlight;
                break;
            case RED:
                background = shrink
                        ? R.drawable.smartisan_rom_shrink_long_btn_red_selector
                        : R.drawable.smartisan_rom_selector_long_btn_red;
                break;
            case WHITE:
                background = shrink
                        ? R.drawable.smartisan_rom_shrink_long_btn_white_selector
                        : R.drawable.smartisan_rom_selector_long_btn_white;
                normalText = true;
                textShadow = false;
                break;
            case GRAY:
            default:
                background = shrink
                        ? R.drawable.smartisan_rom_shrink_long_btn_gray_selector
                        : R.drawable.smartisan_rom_selector_long_btn_gray;
                break;
        }
        setBackgroundResource(background);
        setTextColor(getResources().getColorStateList(normalText
                ? R.color.smartisan_button_normal_text
                : R.color.smartisan_button_highlight_text));
        setShadowEnable(textShadow);
    }

    public void updateBackgroundStyle(SmallButtonStyle style) {
        if (style == null) {
            return;
        }
        int background;
        boolean normalText = false;
        switch (style) {
            case HIGH_LIGHT:
                background = R.drawable.smartisan_rom_selector_small_btn_highlight;
                break;
            case RED:
                background = R.drawable.smartisan_rom_selector_small_btn_highlight_red;
                break;
            case STANDARD:
            default:
                background = R.drawable.smartisan_rom_selector_small_btn_standard;
                normalText = true;
                break;
        }
        setBackgroundResource(background);
        setTextColor(getResources().getColorStateList(normalText
                ? R.color.smartisan_button_normal_text
                : R.color.smartisan_button_highlight_text));
    }

    public void setOnDisabledClickListener(OnDisabledClickListener listener) {
        disabledClickListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (shouldHandleDisabledTouchEvent(event)) {
            if (event.getAction() == MotionEvent.ACTION_UP && disabledClickListener != null) {
                disabledClickListener.onDisabledClick();
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    private boolean shouldHandleDisabledTouchEvent(MotionEvent event) {
        if (isEnabled() || disabledClickListener == null) {
            return false;
        }
        Rect bounds = new Rect();
        getGlobalVisibleRect(bounds);
        return bounds.contains((int) event.getRawX(), (int) event.getRawY());
    }

    @Override
    public void setBackground(Drawable background) {
        Drawable current = getBackground();
        if (current instanceof SmartisanShadowDrawable
                && ((SmartisanShadowDrawable) current).getBackgroundDrawable() == background) {
            ((SmartisanShadowDrawable) current).setProjectBackwards(shadowShouldProject);
            return;
        }
        if (backgroundShadowEnabled && !(background instanceof SmartisanShadowDrawable)
                && (buttonStyle == LONG_BUTTON || buttonStyle == SHRINK_LONG_BUTTON)) {
            int shadow = buttonStyle == LONG_BUTTON
                    ? R.drawable.smartisan_rom_shadow_button_long_shadow_selector
                    : R.drawable.smartisan_rom_shadow_button_shrink_shadow_selector;
            SmartisanShadowDrawable.setViewBackground(this, background, shadow,
                    shadowShouldProject);
        } else {
            super.setBackground(background);
        }
    }

    public void setShadowEnabled(boolean enabled) {
        backgroundShadowEnabled = enabled;
    }

    public void setShadowShouldProjects(boolean shouldProject) {
        shadowShouldProject = shouldProject;
    }
}
