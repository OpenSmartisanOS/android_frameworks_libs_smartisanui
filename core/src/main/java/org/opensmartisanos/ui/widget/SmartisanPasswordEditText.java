/* Ported from smartisanos.widget.PasswordEditText in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeProvider;
import android.widget.EditText;

import org.opensmartisanos.ui.R;
import org.opensmartisanos.ui.internal.SmartisanVirtualIconAccessibility;

public class SmartisanPasswordEditText extends EditText {
    private static final int EYE_DRAWABLE_PADDING = 48;
    private static final int INPUT_TYPE_CLEAR_VARIATION_MASK = -4096;
    private static final int INPUT_TYPE_VARIATION_MASK = 4095;
    private static final int VISIBLE_VARIATION_FLAG = 0x90;
    private final EyeAnimator eyeAnimator;
    private final SmartisanVirtualIconAccessibility accessibility;
    private int eyePaddingLeft;
    private int eyePaddingRight = EYE_DRAWABLE_PADDING;
    private int eyePaddingTop;
    private int invisibleVariation;
    private int visibleVariation;
    private boolean visible;
    private Runnable pendingSetInputType;

    public SmartisanPasswordEditText(Context context) { this(context, null); }
    public SmartisanPasswordEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }
    public SmartisanPasswordEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!updateVisibleStatus(getInputType())) {
            super.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                    | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            updateVisibleStatus(getInputType());
        }
        eyeAnimator = new EyeAnimator((AnimationDrawable) context.getDrawable(
                R.drawable.smartisan_rom_pwd_eye_open_close_anim));
        accessibility = new SmartisanVirtualIconAccessibility(this, 0,
                new SmartisanVirtualIconAccessibility.Callback() {
                    @Override public boolean isVisible() { return getEyeDrawable() != null; }
                    @Override public Rect getBounds() { return getEyeDrawable().getBounds(); }
                    @Override public CharSequence getDescription() {
                        return getContext().getString(visible
                                ? R.string.smartisan_rom_hide_password
                                : R.string.smartisan_rom_show_password);
                    }
                    @Override public boolean performClick() { togglePasswordVisibility(); return true; }
                });
    }

    private Drawable getEyeDrawable() { return eyeAnimator.getCurrentDrawable(); }
    public void setEyeAnimator(Drawable drawable) { eyeAnimator.setDrawable((AnimationDrawable) drawable); }
    public void setEyePaddingRight(int padding) { eyePaddingRight = padding; }
    public void setEyePaddingLeft(int padding) { eyePaddingLeft = padding; }
    public void setEyePaddingTop(int padding) { eyePaddingTop = padding; }
    public boolean isPasswordVisible() { return visible; }

    private boolean updateVisibleStatus(int inputType) {
        int variation = inputType & INPUT_TYPE_VARIATION_MASK;
        if (variation == 129 || variation == 225 || variation == 18) {
            invisibleVariation = variation;
            visibleVariation = variation | VISIBLE_VARIATION_FLAG;
            visible = false;
            return true;
        }
        if ((variation & VISIBLE_VARIATION_FLAG) == 0) return false;
        int original = variation - VISIBLE_VARIATION_FLAG;
        invisibleVariation = original;
        if (original == 1) invisibleVariation = original | 128;
        else if (original == 2) invisibleVariation = original | 16;
        visibleVariation = variation;
        visible = true;
        return true;
    }

    @Override public void setInputType(int type) { setInputTypeInternal(type, true); }
    private void setInputTypeInternal(int type, boolean reset) {
        super.setInputType(type);
        updateVisibleStatus(type);
        if (reset && eyeAnimator != null) eyeAnimator.resetCurrent();
    }
    @Override public int getCompoundPaddingRight() {
        int padding = super.getCompoundPaddingRight();
        Drawable drawable = getEyeDrawable();
        return drawable == null ? padding : drawable.getIntrinsicWidth()
                + Math.max(padding, eyePaddingRight + eyePaddingLeft);
    }
    public int getHorizontalOffsetForDrawables() {
        Drawable drawable = getEyeDrawable();
        return drawable == null ? 0 : drawable.getIntrinsicWidth();
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Drawable drawable = getEyeDrawable();
        if (drawable == null) return;
        int marginRight = 0;
        if (getParent() instanceof View) {
            ViewGroup.LayoutParams params = ((View) getParent()).getLayoutParams();
            if (params instanceof ViewGroup.MarginLayoutParams) {
                marginRight = ((ViewGroup.MarginLayoutParams) params).rightMargin;
            }
        }
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        int top = (getHeight() - height) / 2 + eyePaddingTop;
        int left = getScrollX() + getWidth() - width - (eyePaddingRight - marginRight);
        drawable.setBounds(left, top, left + width, top + height);
        drawable.draw(canvas);
    }
    @Override public boolean onTouchEvent(MotionEvent event) {
        Drawable drawable = getEyeDrawable();
        if (drawable != null && (int) event.getX() + getScrollX() >= drawable.getBounds().left) {
            if (event.getActionMasked() == MotionEvent.ACTION_UP) togglePasswordVisibility();
            return true;
        }
        return super.onTouchEvent(event);
    }
    public void togglePasswordVisibility() {
        eyeAnimator.setDirection();
        eyeAnimator.start();
        if (pendingSetInputType == null) {
            pendingSetInputType = () -> {
                int base = getInputType() & INPUT_TYPE_CLEAR_VARIATION_MASK;
                int selection = Math.max(0, getSelectionEnd());
                eyeAnimator.setDirection();
                setInputTypeInternal(base | (visible ? invisibleVariation : visibleVariation), false);
                setSelection(Math.min(selection, length()));
                accessibility.invalidateRoot();
            };
        }
        removeCallbacks(pendingSetInputType);
        postDelayed(pendingSetInputType, eyeAnimator.getAnimationDuration() / 2L);
        accessibility.sendClickEvent();
    }
    @Override protected boolean dispatchHoverEvent(MotionEvent event) {
        return accessibility.dispatchHoverEvent(event) || super.dispatchHoverEvent(event);
    }
    @Override public AccessibilityNodeProvider getAccessibilityNodeProvider() { return accessibility; }
    @Override protected void onDetachedFromWindow() {
        eyeAnimator.stop();
        if (pendingSetInputType != null) removeCallbacks(pendingSetInputType);
        super.onDetachedFromWindow();
    }

    private final class EyeAnimator extends Handler {
        private AnimationDrawable frames;
        private int current;
        private int direction;

        EyeAnimator(AnimationDrawable drawable) {
            super(Looper.getMainLooper());
            setDrawable(drawable);
        }
        Drawable getCurrentDrawable() { return frames.getFrame(current); }
        void setDrawable(AnimationDrawable drawable) { frames = drawable; resetCurrent(); }
        void resetCurrent() { current = visible ? 0 : frames.getNumberOfFrames() - 1; invalidate(); }
        void setDirection() { direction = visible ? 1 : -1; }
        int getAnimationDuration() {
            int duration = frames.getDuration(current);
            if (direction > 0) return (frames.getNumberOfFrames() - current) * duration;
            if (direction < 0) return current * duration;
            return 0;
        }
        void start() { removeCallbacksAndMessages(null); post(this::tick); }
        void stop() { removeCallbacksAndMessages(null); }
        private void tick() {
            int next = current + direction;
            if (next >= 0 && next < frames.getNumberOfFrames()) {
                int duration = frames.getDuration(current);
                current = next;
                postDelayed(this::tick, duration);
            }
            invalidate();
        }
    }
}
