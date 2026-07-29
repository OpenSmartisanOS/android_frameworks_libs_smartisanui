/* Ported from smartisanos.widget.QuickDeleteEditText in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeProvider;
import android.widget.EditText;

import org.opensmartisanos.ui.R;
import org.opensmartisanos.ui.internal.SmartisanVirtualIconAccessibility;

public class SmartisanQuickDeleteEditText extends EditText {
    private static final String TAG = "SmartisanQuickDelete";
    private static final int DELETE_BUTTON_VIRTUAL_ID = 256;
    private OnDeleteIconClickListener deleteListener;
    private boolean drawIcon;
    private boolean iconVisible = true;
    private int iconPaddingRight = 54;
    private int iconPaddingLeft;
    private int iconPaddingTop;
    private Drawable quickDeleteDrawable;
    private final SmartisanVirtualIconAccessibility accessibility;

    public interface OnDeleteIconClickListener { void onDeleteIconClick(); }

    public SmartisanQuickDeleteEditText(Context context) { this(context, null); }
    public SmartisanQuickDeleteEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }
    public SmartisanQuickDeleteEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }
    public SmartisanQuickDeleteEditText(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        quickDeleteDrawable = context.getDrawable(R.drawable.smartisan_rom_quick_icon_delete);
        accessibility = new SmartisanVirtualIconAccessibility(this, DELETE_BUTTON_VIRTUAL_ID,
                new SmartisanVirtualIconAccessibility.Callback() {
                    @Override public boolean isVisible() { return shouldDrawIcon(); }
                    @Override public Rect getBounds() { return quickDeleteDrawable.getBounds(); }
                    @Override public CharSequence getDescription() {
                        return getContext().getString(R.string.smartisan_rom_quick_delete);
                    }
                    @Override public boolean performClick() { clearFromIcon(); return true; }
                });
    }

    public void setDrawable(int id) {
        try { quickDeleteDrawable = getContext().getDrawable(id); }
        catch (Resources.NotFoundException exception) { Log.e(TAG, "setDrawable failed", exception); }
    }
    public void setIconPaddingRight(int padding) { iconPaddingRight = padding; }
    public void setIconPaddingLeft(int padding) { iconPaddingLeft = padding; }
    public void setIconPaddingTop(int padding) { iconPaddingTop = padding; }
    public void setIconVisible(boolean visible) { iconVisible = visible; invalidate(); accessibility.invalidateRoot(); }
    public void setOnDeleteIconClickListener(OnDeleteIconClickListener listener) { deleteListener = listener; }

    @Override protected void onTextChanged(CharSequence text, int start, int before, int count) {
        super.onTextChanged(text, start, before, count);
        updateDrawableVisibility(getText().length() > 0 && isFocused());
    }
    @Override protected void onFocusChanged(boolean focused, int direction, Rect previous) {
        super.onFocusChanged(focused, direction, previous);
        updateDrawableVisibility(getText().length() > 0 && isFocused());
    }
    public void updateDrawableVisibility(boolean visible) {
        if (quickDeleteDrawable != null && drawIcon != visible) {
            drawIcon = visible;
            accessibility.invalidateRoot();
            invalidate();
        }
    }
    @Override public int getCompoundPaddingRight() {
        int padding = super.getCompoundPaddingRight();
        return shouldDrawIcon() ? quickDeleteDrawable.getIntrinsicWidth()
                + Math.max(padding, iconPaddingRight + iconPaddingLeft) : padding;
    }
    public int getHorizontalOffsetForDrawables() {
        return shouldDrawIcon() ? quickDeleteDrawable.getIntrinsicWidth() : 0;
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!shouldDrawIcon()) return;
        int marginRight = 0;
        if (getParent() instanceof View) {
            ViewGroup.LayoutParams params = ((View) getParent()).getLayoutParams();
            if (params instanceof ViewGroup.MarginLayoutParams) {
                marginRight = ((ViewGroup.MarginLayoutParams) params).rightMargin;
            }
        }
        int width = quickDeleteDrawable.getIntrinsicWidth();
        int height = quickDeleteDrawable.getIntrinsicHeight();
        int top = (getHeight() - height) / 2 + iconPaddingTop;
        int left = getScrollX() + getWidth() - width - (iconPaddingRight - marginRight);
        quickDeleteDrawable.setBounds(left, top, left + width, top + height);
        quickDeleteDrawable.draw(canvas);
    }
    private boolean shouldDrawIcon() { return iconVisible && drawIcon && quickDeleteDrawable != null; }
    @Override public boolean onTouchEvent(MotionEvent event) {
        if (shouldDrawIcon() && (int) event.getX() + getScrollX() >= quickDeleteDrawable.getBounds().left) {
            setPressed(false);
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN
                    || event.getActionMasked() == MotionEvent.ACTION_MOVE) {
                quickDeleteDrawable.setState(new int[]{android.R.attr.state_pressed});
                invalidate();
            } else {
                quickDeleteDrawable.setState(getDrawableState());
                if (event.getActionMasked() == MotionEvent.ACTION_UP) clearFromIcon();
            }
            return true;
        }
        return super.onTouchEvent(event);
    }
    private void clearFromIcon() {
        setText("");
        if (deleteListener != null) deleteListener.onDeleteIconClick();
        accessibility.sendClickEvent();
    }
    @Override protected boolean dispatchHoverEvent(MotionEvent event) {
        return accessibility.dispatchHoverEvent(event) || super.dispatchHoverEvent(event);
    }
    @Override public AccessibilityNodeProvider getAccessibilityNodeProvider() { return accessibility; }
}
