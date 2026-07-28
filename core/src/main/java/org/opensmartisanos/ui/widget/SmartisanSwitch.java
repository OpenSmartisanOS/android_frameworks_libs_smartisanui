/*
 * Ported from smartisanos.widget.SwitchEx in Smartisan OS 8.5.3.
 * Hidden Smartisan resource and vibrator APIs are replaced with public Android APIs.
 */
package org.opensmartisanos.ui.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Pair;
import android.util.SparseIntArray;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import org.opensmartisanos.ui.R;

import java.lang.ref.WeakReference;

public class SmartisanSwitch extends CheckBox {
    public static final int STYLE_LIGHT = 1;
    public static final int STYLE_DARK = 2;
    public static final int SWITCH_BOTTOM = 0;
    public static final int SWITCH_UNPRESSED = 1;
    public static final int SWITCH_FRAME = 2;
    public static final int SWITCH_MASK = 3;
    public static final int SWITCH_PRESSED = 4;
    public static final int SWITCH_FRAME_PRESSED = 5;

    private static final int CLICK_TIMEOUT = 300;
    private static final int MESSAGE_SET_CHECKED = 1;
    private static final int ENABLED_ALPHA = 255;
    private static final int DISABLED_ALPHA = 191;
    private static final float FRAME_DURATION_MS = 16f;
    private static final float VELOCITY_DP_PER_SECOND = 350f;

    private static final int[] LIGHT_DRAWABLES = {
            R.drawable.smartisan_rom_switch_ex_bottom,
            R.drawable.smartisan_rom_switch_ex_unpressed,
            R.drawable.smartisan_rom_switch_ex_frame,
            R.drawable.smartisan_rom_switch_ex_mask,
            R.drawable.smartisan_rom_switch_ex_pressed,
            R.drawable.smartisan_rom_switch_ex_frame_pressed
    };
    private static final int[] DARK_DRAWABLES = {
            R.drawable.smartisan_rom_switch_ex_bottom_dark,
            R.drawable.smartisan_rom_switch_ex_unpressed_dark,
            R.drawable.smartisan_rom_switch_ex_frame_dark,
            R.drawable.smartisan_rom_switch_ex_mask_dark,
            R.drawable.smartisan_rom_switch_ex_pressed_dark,
            R.drawable.smartisan_rom_switch_ex_frame_pressed_dark
    };
    private static int[] drawableIds = LIGHT_DRAWABLES;
    private static int style = STYLE_LIGHT;

    private static final Pair<Float, Float> BOTTOM_BASE = Pair.create(286f, 144f);
    private static final Pair<Float, Float> THUMB_BASE = Pair.create(286f, 144f);
    private static final Pair<Float, Float> FRAME_BASE = Pair.create(198f, 144f);
    private static final Pair<Float, Float> MASK_BASE = Pair.create(198f, 144f);
    private static final Pair<Float, Float> PRESSED_BASE = Pair.create(286f, 144f);
    private static final Pair<Float, Float> PRESSED_FRAME_BASE = Pair.create(198f, 144f);

    private static final Canvas BITMAP_CANVAS = new Canvas();
    private static final Paint BITMAP_PAINT = new Paint(Paint.FILTER_BITMAP_FLAG);
    private static final PorterDuffXfermode MASK_XFERMODE =
            new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
    private static final RectF MASK_RECT = new RectF();
    private static final RectF BOTTOM_RECT = new RectF();
    private static final RectF FRAME_RECT = new RectF();
    private static final RectF PRESSED_FRAME_RECT = new RectF();
    private static final RectF PRESSED_RECT = new RectF();

    private static Bitmap bottom;
    private static Bitmap normalThumb;
    private static Bitmap pressedThumb;
    private static Bitmap frame;
    private static Bitmap pressedFrame;
    private static Bitmap mask;
    private static Bitmap currentThumb;
    private static Bitmap disabledOnBitmap;
    private static Bitmap disabledOffBitmap;
    private static Bitmap onBitmap;
    private static Bitmap onPressedBitmap;
    private static Bitmap offBitmap;
    private static Bitmap offPressedBitmap;
    private static Pair<Float, Float> bottomScale;
    private static Pair<Float, Float> normalScale;
    private static Pair<Float, Float> frameScale;
    private static Pair<Float, Float> maskScale;
    private static Pair<Float, Float> pressedScale;
    private static Pair<Float, Float> pressedFrameScale;
    private static float thumbWidth;
    private static float maskWidth;
    private static float maskHeight;
    private static float onPosition;
    private static float offPosition;
    private static int cachedDensityDpi;
    private static boolean cacheCleared;

    private final Handler handler = new CheckHandler(this);
    private final Resources resources;
    private int alpha = ENABLED_ALPHA;
    private int pressedShadowAlpha;
    private int touchSlop;
    private float velocity;
    private float extendedOffsetY;
    private float firstDownX;
    private float firstDownY;
    private float initialButtonPosition;
    private float buttonPosition;
    private float realPosition;
    private float animatedVelocity;
    private float animationPosition;
    private boolean checked;
    private boolean touching;
    private boolean animating;
    private boolean broadcasting;
    private boolean restoring;
    private boolean vibrateFeedback;
    private CompoundButton.OnCheckedChangeListener checkedChangeListener;
    private CompoundButton.OnCheckedChangeListener widgetCheckedChangeListener;
    private View.OnClickListener clickListener;
    private ViewParent claimedParent;
    private PerformClick performClick;
    private ValueAnimator shadowAlphaAnimator;
    private int bottomResourceId;

    public SmartisanSwitch(Context context) {
        this(context, null);
    }

    public SmartisanSwitch(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.checkboxStyle);
    }

    public SmartisanSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        resources = context.getResources();
        setButtonDrawable(null);
        setBackground(null);
        setPadding(0, 0, 0, 0);
        setMinWidth(0);
        setMinHeight(0);
        setVibrateFeedbackEnabled(true);
        initSwitchResources();
        initView(context);
    }

    private static final class CheckHandler extends Handler {
        private final WeakReference<SmartisanSwitch> switchReference;

        CheckHandler(SmartisanSwitch view) {
            super(Looper.getMainLooper());
            switchReference = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message message) {
            SmartisanSwitch view = switchReference.get();
            if (message.what == MESSAGE_SET_CHECKED && view != null) {
                view.setChecked((Boolean) message.obj, false, true);
            }
        }
    }

    private void initSwitchResources() {
        applySwitchStyle();
        initSwitchBitmap(resources);
    }

    private static synchronized void initSwitchBitmap(Resources resources) {
        if (cachedDensityDpi != resources.getConfiguration().densityDpi) {
            clearSwitchBitmap(false);
        }
        if (bottom == null || normalThumb == null || frame == null || mask == null
                || pressedThumb == null || pressedFrame == null) {
            bottom = bitmap(resources, drawableIds[SWITCH_BOTTOM]);
            normalThumb = bitmap(resources, drawableIds[SWITCH_UNPRESSED]);
            frame = bitmap(resources, drawableIds[SWITCH_FRAME]);
            mask = bitmap(resources, drawableIds[SWITCH_MASK]);
            pressedThumb = bitmap(resources, drawableIds[SWITCH_PRESSED]);
            pressedFrame = bitmap(resources, drawableIds[SWITCH_FRAME_PRESSED]);
            currentThumb = normalThumb;
            initDrawableScale(resources);
        }
        BITMAP_PAINT.setColor(0xffffffff);
        thumbWidth = normalScale.first;
        maskWidth = maskScale.first;
        maskHeight = maskScale.second;
        onPosition = thumbWidth / 2f;
        offPosition = maskWidth - thumbWidth / 2f;
        initComposedBitmaps();
    }

    private static Bitmap bitmap(Resources resources, int id) {
        return ((BitmapDrawable) resources.getDrawable(id)).getBitmap();
    }

    private static void initDrawableScale(Resources resources) {
        int densityDpi = resources.getConfiguration().densityDpi;
        bottomScale = scaled(BOTTOM_BASE, densityDpi);
        normalScale = scaled(THUMB_BASE, densityDpi);
        frameScale = scaled(FRAME_BASE, densityDpi);
        maskScale = scaled(MASK_BASE, densityDpi);
        pressedScale = scaled(PRESSED_BASE, densityDpi);
        pressedFrameScale = scaled(PRESSED_FRAME_BASE, densityDpi);
        cachedDensityDpi = densityDpi;
    }

    private static Pair<Float, Float> scaled(Pair<Float, Float> base, int densityDpi) {
        if (densityDpi == 480) {
            return base;
        }
        return Pair.create(densityDpi * base.first / 480f, densityDpi * base.second / 480f);
    }

    public static void clearSwitchBitmap() {
        clearSwitchBitmap(true);
    }

    private static synchronized void clearSwitchBitmap(boolean resetStyle) {
        bottom = null;
        normalThumb = null;
        pressedThumb = null;
        frame = null;
        pressedFrame = null;
        mask = null;
        disabledOnBitmap = null;
        disabledOffBitmap = null;
        onBitmap = null;
        onPressedBitmap = null;
        offBitmap = null;
        offPressedBitmap = null;
        cacheCleared = true;
        if (resetStyle) {
            style = STYLE_LIGHT;
            applySwitchStyle();
        }
    }

    private static void applySwitchStyle() {
        if (style == STYLE_DARK) {
            drawableIds = DARK_DRAWABLES;
        } else if (style == STYLE_LIGHT || drawableIds == null) {
            drawableIds = LIGHT_DRAWABLES;
        }
    }

    private void initView(Context context) {
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        buttonPosition = checked ? onPosition : offPosition;
        realPosition = realPosition(buttonPosition);
        float density = resources.getDisplayMetrics().density;
        velocity = (int) (VELOCITY_DP_PER_SECOND * density + 0.5f);
        extendedOffsetY = (int) (2f * density + 0.5f);
    }

    private static void initComposedBitmaps() {
        cacheCleared = false;
        if (disabledOnBitmap == null || disabledOffBitmap == null || onBitmap == null
                || offBitmap == null || onPressedBitmap == null || offPressedBitmap == null) {
            disabledOnBitmap = composeBitmap(DISABLED_ALPHA, realPosition(onPosition), 0);
            disabledOffBitmap = composeBitmap(DISABLED_ALPHA, realPosition(offPosition), 0);
            onBitmap = composeBitmap(ENABLED_ALPHA, realPosition(onPosition), 0);
            onPressedBitmap = composeBitmap(ENABLED_ALPHA, realPosition(onPosition), 255);
            offBitmap = composeBitmap(ENABLED_ALPHA, realPosition(offPosition), 0);
            offPressedBitmap = composeBitmap(ENABLED_ALPHA, realPosition(offPosition), 255);
        }
    }

    @Deprecated
    public void setSwitchDrawable(int type, Bitmap ignored) {
        setSwitchDrawableStyle(STYLE_DARK);
    }

    public void setSwitchDrawableStyle(int newStyle) {
        if (style != newStyle) {
            style = newStyle;
            clearSwitchBitmap(false);
            initSwitchResources();
            invalidate();
        }
    }

    public void setSwitchDrawables(SparseIntArray drawables) {
        drawableIds = new int[] {
                drawables.get(SWITCH_BOTTOM), drawables.get(SWITCH_UNPRESSED),
                drawables.get(SWITCH_FRAME), drawables.get(SWITCH_MASK),
                drawables.get(SWITCH_PRESSED), drawables.get(SWITCH_FRAME_PRESSED)
        };
        clearSwitchBitmap(false);
        initSwitchBitmap(resources);
        invalidate();
    }

    public void setBottomDrawable(int resId) {
        if (bottomResourceId != resId) {
            bottomResourceId = resId;
            bottom = bitmap(resources, resId);
            resetComposedBitmaps();
        }
    }

    public void setFrameDrawable(int resId) {
        frame = bitmap(resources, resId == -1
                ? R.drawable.smartisan_rom_switch_ex_frame : resId);
        resetComposedBitmaps();
    }

    private void resetComposedBitmaps() {
        disabledOnBitmap = null;
        disabledOffBitmap = null;
        onBitmap = null;
        onPressedBitmap = null;
        offBitmap = null;
        offPressedBitmap = null;
        initComposedBitmaps();
        invalidate();
    }

    @Override
    public void setEnabled(boolean enabled) {
        alpha = enabled ? ENABLED_ALPHA : DISABLED_ALPHA;
        super.setEnabled(enabled);
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
    public void toggle() {
        setChecked(!checked);
    }

    private void setCheckedFromUser(boolean value) {
        handler.removeMessages(MESSAGE_SET_CHECKED);
        handler.sendMessageDelayed(handler.obtainMessage(MESSAGE_SET_CHECKED, value), 20L);
    }

    @Override
    public void setChecked(boolean value) {
        setChecked(value, true, false);
    }

    private void setChecked(boolean value, boolean needsInvalidate, boolean fromUser) {
        if (checked == value) {
            return;
        }
        checked = value;
        if (needsInvalidate && mask != null) {
            buttonPosition = value ? onPosition : offPosition;
            realPosition = realPosition(buttonPosition);
            invalidate();
        }
        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
        if (broadcasting || restoring) {
            return;
        }
        broadcasting = true;
        if (checkedChangeListener != null) {
            checkedChangeListener.onCheckedChanged(this, checked);
        }
        if (widgetCheckedChangeListener != null) {
            widgetCheckedChangeListener.onCheckedChanged(this, checked);
        }
        if (fromUser && vibrateFeedback) {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        }
        broadcasting = false;
    }

    public void setVibrateFeedbackEnabled(boolean enabled) {
        vibrateFeedback = enabled;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        restoring = true;
        super.onRestoreInstanceState(state);
        restoring = false;
    }

    @Override
    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        checkedChangeListener = listener;
    }

    void setOnCheckedChangeWidgetListener(CompoundButton.OnCheckedChangeListener listener) {
        widgetCheckedChangeListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (animating || !isEnabled()) {
            return true;
        }
        int action = event.getActionMasked();
        float x = event.getX();
        float y = event.getY();
        float deltaX = Math.abs(x - firstDownX);
        float deltaY = Math.abs(y - firstDownY);
        if (action == MotionEvent.ACTION_DOWN) {
            attemptClaimDrag();
            touching = true;
            cancelShadowAlphaAnimation();
            pressedShadowAlpha = 255;
            firstDownX = x;
            firstDownY = y;
            initialButtonPosition = checked ? onPosition : offPosition;
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_MOVE
                || action == MotionEvent.ACTION_CANCEL) {
            buttonPosition = initialButtonPosition + x - firstDownX;
            if (buttonPosition >= onPosition) {
                buttonPosition = onPosition;
            }
            if (buttonPosition <= offPosition) {
                buttonPosition = offPosition;
            }
            realPosition = realPosition(buttonPosition);
            if (action != MotionEvent.ACTION_MOVE) {
                touching = false;
                startShadowAlphaAnimation();
                float elapsed = event.getEventTime() - event.getDownTime();
                if (deltaY < touchSlop && deltaX < touchSlop && elapsed < CLICK_TIMEOUT) {
                    if (performClick == null) {
                        performClick = new PerformClick();
                    }
                    if (!post(performClick)) {
                        performClick();
                    }
                } else {
                    boolean turningOn = buttonPosition
                            > offPosition + (onPosition - offPosition) / 2f;
                    startAnimation(turningOn);
                    setCheckedFromUser(turningOn);
                }
            }
        }
        invalidate();
        return true;
    }

    private final class PerformClick implements Runnable {
        @Override
        public void run() {
            performClick();
        }
    }

    @Override
    public boolean performClick() {
        if (clickListener != null) {
            clickListener.onClick(this);
        }
        startAnimation(!checked);
        setCheckedFromUser(!checked);
        return true;
    }

    @Override
    public void setOnClickListener(View.OnClickListener listener) {
        if (!isClickable()) {
            setClickable(true);
        }
        clickListener = listener;
    }

    private void attemptClaimDrag() {
        claimedParent = getParent();
        if (claimedParent != null) {
            claimedParent.requestDisallowInterceptTouchEvent(true);
        }
    }

    private static float realPosition(float position) {
        return position - thumbWidth / 2f;
    }

    private Bitmap composeBitmap() {
        return composeBitmap(alpha, realPosition, pressedShadowAlpha);
    }

    private static synchronized Bitmap composeBitmap(int alpha, float position,
            int shadowAlpha) {
        int width = Math.round(maskScale.first);
        int height = Math.round(maskScale.second);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        BITMAP_CANVAS.setBitmap(bitmap);
        BITMAP_PAINT.setAlpha(alpha);
        MASK_RECT.set(0f, 0f, maskScale.first, maskScale.second);
        BITMAP_CANVAS.drawBitmap(mask, null, MASK_RECT, BITMAP_PAINT);
        BITMAP_PAINT.setXfermode(MASK_XFERMODE);
        BOTTOM_RECT.set(position, 0f, bottomScale.first + position, bottomScale.second);
        BITMAP_CANVAS.drawBitmap(bottom, null, BOTTOM_RECT, BITMAP_PAINT);
        BITMAP_PAINT.setXfermode(null);
        FRAME_RECT.set(0f, 0f, frameScale.first, frameScale.second);
        BITMAP_CANVAS.drawBitmap(frame, null, FRAME_RECT, BITMAP_PAINT);
        if (shadowAlpha > 0) {
            int previousAlpha = BITMAP_PAINT.getAlpha();
            BITMAP_PAINT.setAlpha(shadowAlpha);
            PRESSED_FRAME_RECT.set(0f, 0f, pressedFrameScale.first, pressedFrameScale.second);
            BITMAP_CANVAS.drawBitmap(pressedFrame, null, PRESSED_FRAME_RECT, BITMAP_PAINT);
            BITMAP_PAINT.setAlpha(previousAlpha);
        }
        PRESSED_RECT.set(position, 0f, pressedScale.first + position, pressedScale.second);
        BITMAP_CANVAS.drawBitmap(shadowAlpha == 255 ? pressedThumb : currentThumb,
                null, PRESSED_RECT, BITMAP_PAINT);
        BITMAP_CANVAS.setBitmap(null);
        return bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (cacheCleared || cachedDensityDpi != resources.getConfiguration().densityDpi) {
            initSwitchBitmap(resources);
        }
        float on = realPosition(onPosition);
        float off = realPosition(offPosition);
        Bitmap bitmap;
        if (alpha != ENABLED_ALPHA) {
            bitmap = realPosition == off ? disabledOffBitmap
                    : realPosition == on ? disabledOnBitmap : composeBitmap();
        } else if (realPosition == off && !isShadowAlphaAnimationRunning()) {
            bitmap = touching ? offPressedBitmap : offBitmap;
        } else if (realPosition == on && !isShadowAlphaAnimationRunning()) {
            bitmap = touching ? onPressedBitmap : onBitmap;
        } else {
            bitmap = composeBitmap();
        }
        BITMAP_PAINT.setAlpha(alpha);
        canvas.drawBitmap(bitmap, 0f, extendedOffsetY, BITMAP_PAINT);
        if (realPosition <= off) {
            stopAnimation();
            animationPosition = offPosition;
        } else if (realPosition >= on) {
            stopAnimation();
            animationPosition = onPosition;
        } else if (animating) {
            doAnimation();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((int) maskWidth, (int) (maskHeight + extendedOffsetY * 2f));
    }

    private void startAnimation(boolean turnOn) {
        animatedVelocity = turnOn ? velocity : -velocity;
        animationPosition = buttonPosition;
        animating = true;
        doAnimation();
    }

    private void stopAnimation() {
        animating = false;
    }

    private void doAnimation() {
        animationPosition += animatedVelocity * FRAME_DURATION_MS / 1000f;
        if (animationPosition <= offPosition) {
            stopAnimation();
            animationPosition = offPosition;
        } else if (animationPosition >= onPosition) {
            stopAnimation();
            animationPosition = onPosition;
        }
        moveView(animationPosition);
    }

    private void moveView(float position) {
        buttonPosition = position;
        realPosition = realPosition(position);
        invalidate();
    }

    private void startShadowAlphaAnimation() {
        if (shadowAlphaAnimator == null) {
            shadowAlphaAnimator = ValueAnimator.ofInt(255, 0);
            shadowAlphaAnimator.addUpdateListener(animation -> {
                pressedShadowAlpha = (Integer) animation.getAnimatedValue();
                invalidate();
            });
            shadowAlphaAnimator.setDuration(200L);
        }
        shadowAlphaAnimator.start();
    }

    private boolean isShadowAlphaAnimationRunning() {
        return shadowAlphaAnimator != null && shadowAlphaAnimator.isRunning();
    }

    private void cancelShadowAlphaAnimation() {
        if (isShadowAlphaAnimationRunning()) {
            shadowAlphaAnimator.cancel();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelShadowAlphaAnimation();
        handler.removeCallbacksAndMessages(null);
        if (claimedParent != null) {
            claimedParent.requestDisallowInterceptTouchEvent(false);
            claimedParent = null;
        }
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(SmartisanSwitch.class.getName());
        event.setChecked(checked);
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(SmartisanSwitch.class.getName());
        info.setCheckable(true);
        info.setChecked(checked);
    }
}
