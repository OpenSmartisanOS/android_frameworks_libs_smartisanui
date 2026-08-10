package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;
import org.opensmartisanos.ui.R;
import org.opensmartisanos.ui.internal.SmartisanPickerMath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/* JADX INFO: loaded from: classes.dex */
@Deprecated
public class SmartisanNumberPicker extends LinearLayout {
    private static final boolean DBG = false;
    private static final int DEFAULT_TEXT_SIZE = 45;
    private static final int SELECTOR_ADJUSTMENT_DURATION_MILLIS = 800;
    private static final int SELECTOR_MAX_FLING_VELOCITY_ADJUSTMENT = 8;
    private static final int SELECTOR_MIDDLE_ITEM_INDEX = 2;
    private static final int SELECTOR_WHEEL_ITEM_COUNT = 5;
    private static final int SIZE_UNSPECIFIED = -1;
    private static final int SNAP_SCROLL_DURATION = 300;
    private static final String TAG = "SmartisanNumberPicker";
    private static final float TOP_AND_BOTTOM_FADING_EDGE_STRENGTH = 0.0f;
    private static final String UNSET_STRING = "--";
    public static final int UNSET_YEAR = 4;
    private static final float VOLUME = 0.0945f;
    private static SoundPool sPool;
    private static int sSoundId;
    private AccessibilityNodeProviderImpl mAccessibilityNodeProvider;
    private final Scroller mAdjustScroller;
    private int mBottomSelectionDividerBottom;
    private final boolean mComputeMaxWidth;
    private int mCurrentScrollOffset;
    private String[] mDisplayedValues;
    private final Scroller mFlingScroller;
    private Formatter mFormatter;
    private final boolean mHasSelectorWheel;
    private boolean mHasUnsetValue;
    private int mHighlightColor;
    private int mHighlightSize;
    private boolean mIngonreMoveEvents;
    private int mInitialScrollOffset;
    private long mLastDownEventTime;
    private float mLastDownEventY;
    private float mLastDownOrMoveEventY;
    private int mLastHoveredChildVirtualViewId;
    private final int mMaxHeight;
    private int mMaxValue;
    private int mMaxWidth;
    private int mMaximumFlingVelocity;
    private int mMiddleScrollOffset;
    private final int mMinHeight;
    private int mMinValue;
    private final int mMinWidth;
    private int mMinimumFlingVelocity;
    private int mNormalColor;
    private int mNormalSize;
    private int mOldTime;
    private OnScrollListener mOnScrollListener;
    private OnValueChangeListener mOnValueChangeListener;
    private int mPreviousScrollerY;
    private int mScrollState;
    private int mSelectorElementHeight;
    private final SparseArray<String> mSelectorIndexToStringCache;
    private final int[] mSelectorIndices;
    private int mSelectorTextGapHeight;
    private final Paint mSelectorWheelPaint;
    private boolean mShowSoftInputOnTap;
    private boolean mSoundEnable;
    private Runnable mSoundRunnable;
    private int mTopSelectionDividerTop;
    private int mTouchSlop;
    private int mValue;
    private VelocityTracker mVelocityTracker;
    private boolean mWrapSelectorWheel;
    private Context mcContext;
    private static final int DEFAUlT_TEXT_COLOR = Color.parseColor("#545454");

    @Override protected Parcelable onSaveInstanceState() {
        return new SavedState(super.onSaveInstanceState(), mValue);
    }

    @Override protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        setValue(savedState.value);
    }

    private static final class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override public SavedState createFromParcel(Parcel source) { return new SavedState(source); }
            @Override public SavedState[] newArray(int size) { return new SavedState[size]; }
        };
        final int value;
        SavedState(Parcelable superState, int value) { super(superState); this.value = value; }
        SavedState(Parcel source) { super(source); value = source.readInt(); }
        @Override public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(value);
        }
    }
    private static final TwoDigitFormatter sTwoDigitFormatter = new TwoDigitFormatter();

    public interface Formatter {
        String format(int i);
    }

    public interface OnScrollListener {
        public static final int SCROLL_STATE_FLING = 2;
        public static final int SCROLL_STATE_IDLE = 0;
        public static final int SCROLL_STATE_TOUCH_SCROLL = 1;

        void onScrollStateChange(SmartisanNumberPicker smartisanNumberPicker, int i);
    }

    public interface OnValueChangeListener {
        void onValueChange(SmartisanNumberPicker smartisanNumberPicker, int i, int i2);
    }

    private static class TwoDigitFormatter implements Formatter {
        java.util.Formatter mFmt;
        char mZeroDigit;
        final StringBuilder mBuilder = new StringBuilder();
        final Object[] mArgs = new Object[1];

        TwoDigitFormatter() {
            Locale locale = Locale.getDefault();
            init(locale);
        }

        private void init(Locale locale) {
            this.mFmt = createFormatter(locale);
            this.mZeroDigit = getZeroDigit(locale);
        }

        @Override // smartisanos.widget.SmartisanNumberPicker.Formatter
        public String format(int value) {
            Locale currentLocale = Locale.getDefault();
            if (this.mZeroDigit != getZeroDigit(currentLocale)) {
                init(currentLocale);
            }
            this.mArgs[0] = Integer.valueOf(value);
            StringBuilder sb = this.mBuilder;
            sb.delete(0, sb.length());
            this.mFmt.format("%02d", this.mArgs);
            return this.mFmt.toString();
        }

        private static char getZeroDigit(Locale locale) {
            return '0';
        }

        private java.util.Formatter createFormatter(Locale locale) {
            return new java.util.Formatter(this.mBuilder, locale);
        }
    }

    public static final Formatter getTwoDigitFormatter() {
        return sTwoDigitFormatter;
    }

    public void setSoundEnable(boolean enabled) {
        this.mSoundEnable = enabled;
    }

    public SmartisanNumberPicker(Context context) {
        this(context, null);
    }

    public SmartisanNumberPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmartisanNumberPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mSelectorIndexToStringCache = new SparseArray<>();
        this.mSelectorIndices = new int[5];
        this.mNormalSize = DEFAULT_TEXT_SIZE;
        this.mHighlightSize = DEFAULT_TEXT_SIZE;
        int i = DEFAUlT_TEXT_COLOR;
        this.mNormalColor = i;
        this.mHighlightColor = i;
        this.mInitialScrollOffset = Integer.MIN_VALUE;
        this.mScrollState = 0;
        this.mHasUnsetValue = false;
        this.mSoundEnable = true;
        this.mOldTime = -1;
        this.mSoundRunnable = new Runnable() { // from class: smartisanos.widget.SmartisanNumberPicker.1
            @Override // java.lang.Runnable
            public void run() {
                if (SmartisanNumberPicker.sPool != null && SmartisanNumberPicker.this.mSoundEnable) {
                    SmartisanNumberPicker.sPool.play(SmartisanNumberPicker.sSoundId, SmartisanNumberPicker.VOLUME, SmartisanNumberPicker.VOLUME, 0, 0, 1.0f);
                }
            }
        };
        this.mcContext = context;
        this.mHasSelectorWheel = true;
        this.mMinHeight = -1;
        this.mMaxHeight = -1;
        if (-1 != -1 && -1 != -1 && -1 > -1) {
            throw new IllegalArgumentException("minHeight > maxHeight");
        }
        this.mMinWidth = -1;
        this.mMaxWidth = -1;
        if (-1 == -1 || -1 == -1 || -1 <= -1) {
            this.mComputeMaxWidth = this.mMaxWidth == -1;
            setWillNotDraw(!this.mHasSelectorWheel);
            ViewConfiguration configuration = ViewConfiguration.get(context.getApplicationContext());
            this.mTouchSlop = configuration.getScaledTouchSlop();
            this.mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
            this.mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity() / 8;
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(this.mNormalSize);
            paint.setColor(this.mNormalColor);
            this.mSelectorWheelPaint = paint;
            this.mFlingScroller = new Scroller(getContext(), null, true);
            this.mAdjustScroller = new Scroller(getContext(), new DecelerateInterpolator(2.5f));
            if (getImportantForAccessibility() == 0) {
                setImportantForAccessibility(1);
            }
            return;
        }
        throw new IllegalArgumentException("minWidth > maxWidth");
    }

    @Override // android.widget.LinearLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (!this.mHasSelectorWheel) {
            super.onLayout(changed, left, top, right, bottom);
            return;
        }
        if (changed) {
            initializeSelectorWheel();
            initializeFadingEdges();
            int height = getHeight();
            int i = this.mSelectorElementHeight;
            int i2 = (height - i) / 2;
            this.mTopSelectionDividerTop = i2;
            this.mBottomSelectionDividerBottom = i2 + i;
        }
    }

    @Override // android.widget.LinearLayout, android.view.View
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!this.mHasSelectorWheel) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int newWidthMeasureSpec = makeMeasureSpec(widthMeasureSpec, this.mMaxWidth);
        int newHeightMeasureSpec = makeMeasureSpec(heightMeasureSpec, this.mMaxHeight);
        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec);
        int widthSize = resolveSizeAndStateRespectingMinSize(this.mMinWidth, getMeasuredWidth(), widthMeasureSpec);
        int heightSize = resolveSizeAndStateRespectingMinSize(this.mMinHeight, getMeasuredHeight(), heightMeasureSpec);
        setMeasuredDimension(widthSize, heightSize);
    }

    private boolean moveToFinalScrollerPosition(Scroller scroller) {
        scroller.forceFinished(true);
        int amountToScroll = scroller.getFinalY() - scroller.getCurrY();
        int futureScrollOffset = (this.mCurrentScrollOffset + amountToScroll) % this.mSelectorElementHeight;
        int overshootAdjustment = this.mInitialScrollOffset - futureScrollOffset;
        if (overshootAdjustment == 0) {
            return false;
        }
        int iAbs = Math.abs(overshootAdjustment);
        int i = this.mSelectorElementHeight;
        if (iAbs > i / 2) {
            if (overshootAdjustment > 0) {
                overshootAdjustment -= i;
            } else {
                overshootAdjustment += i;
            }
        }
        int amountToScroll2 = amountToScroll + overshootAdjustment;
        int maxScrollDelta = this.mSelectorElementHeight * 5;
        if (Math.abs(amountToScroll2) > maxScrollDelta) {
            amountToScroll2 = amountToScroll2 > 0 ? maxScrollDelta : -maxScrollDelta;
        }
        scrollBy(0, amountToScroll2);
        return true;
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!this.mHasSelectorWheel || !isEnabled()) {
            return false;
        }
        int action = event.getActionMasked();
        if (action != 0) {
            return false;
        }
        float y = event.getY();
        this.mLastDownEventY = y;
        this.mLastDownOrMoveEventY = y;
        this.mLastDownEventTime = event.getEventTime();
        this.mIngonreMoveEvents = false;
        this.mShowSoftInputOnTap = false;
        getParent().requestDisallowInterceptTouchEvent(true);
        if (!this.mFlingScroller.isFinished()) {
            this.mFlingScroller.forceFinished(true);
            this.mAdjustScroller.forceFinished(true);
            onScrollStateChange(0);
        } else if (!this.mAdjustScroller.isFinished()) {
            this.mFlingScroller.forceFinished(true);
            this.mAdjustScroller.forceFinished(true);
        } else {
            float f = this.mLastDownEventY;
            if (f >= this.mTopSelectionDividerTop && f <= this.mBottomSelectionDividerBottom) {
                this.mShowSoftInputOnTap = true;
            }
        }
        return true;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || !this.mHasSelectorWheel) {
            return false;
        }
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(event);
        int action = event.getActionMasked();
        if (action != 1) {
            if (action == 2 && !this.mIngonreMoveEvents) {
                float currentMoveY = event.getY();
                if (this.mScrollState != 1) {
                    int deltaDownY = (int) Math.abs(currentMoveY - this.mLastDownEventY);
                    if (deltaDownY > this.mTouchSlop) {
                        onScrollStateChange(1);
                    }
                } else {
                    int deltaMoveY = (int) (currentMoveY - this.mLastDownOrMoveEventY);
                    scrollBy(0, deltaMoveY);
                    invalidate();
                }
                this.mLastDownOrMoveEventY = currentMoveY;
            }
        } else {
            VelocityTracker velocityTracker = this.mVelocityTracker;
            velocityTracker.computeCurrentVelocity(1000, this.mMaximumFlingVelocity);
            int initialVelocity = (int) velocityTracker.getYVelocity();
            if (Math.abs(initialVelocity) > this.mMinimumFlingVelocity) {
                fling(initialVelocity);
                onScrollStateChange(2);
            } else {
                int eventY = (int) event.getY();
                int deltaMoveY2 = (int) Math.abs(eventY - this.mLastDownEventY);
                long deltaTime = event.getEventTime() - this.mLastDownEventTime;
                if (deltaMoveY2 <= this.mTouchSlop && deltaTime < ViewConfiguration.getTapTimeout()) {
                    if (!this.mShowSoftInputOnTap) {
                        int selectorIndexOffset = (eventY / this.mSelectorElementHeight) - 2;
                        if (selectorIndexOffset > 0) {
                            changeValueByOne(true);
                        } else if (selectorIndexOffset < 0) {
                            changeValueByOne(false);
                        }
                    } else {
                        this.mShowSoftInputOnTap = false;
                        performClick();
                    }
                } else {
                    ensureScrollWheelAdjusted();
                }
                onScrollStateChange(0);
            }
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
        return true;
    }

    @Override public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_DPAD_UP
                || keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
                && event.getAction() == KeyEvent.ACTION_DOWN
                && event.hasNoModifiers()
                && stepValueFromKeyboard(keyCode == KeyEvent.KEYCODE_DPAD_DOWN)) {
            requestFocus();
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Applies one logical wheel step without depending on a completed layout pass. This keeps
     * keyboard navigation usable when the picker has just received focus, while preserving the
     * same min/max, unset-year and wrapping order used by the scroll wheel.
     */
    private boolean stepValueFromKeyboard(boolean increment) {
        if (!isEnabled()) {
            return false;
        }
        int effectiveMax = maxValue();
        int logicalValue = hasUnset() && (mValue < mMinValue || mValue > mMaxValue)
                ? effectiveMax : mValue;
        int next;
        if (increment) {
            if (logicalValue < effectiveMax) {
                next = logicalValue + 1;
            } else if (mWrapSelectorWheel) {
                next = mMinValue;
            } else {
                return false;
            }
        } else if (logicalValue > mMinValue) {
            next = logicalValue - 1;
        } else if (mWrapSelectorWheel) {
            next = effectiveMax;
        } else {
            return false;
        }
        setValueInternal(next, true);
        return true;
    }

    @Override // android.view.View
    public void computeScroll() {
        Scroller scroller = this.mFlingScroller;
        if (scroller.isFinished()) {
            scroller = this.mAdjustScroller;
            if (scroller.isFinished()) {
                return;
            }
        }
        scroller.computeScrollOffset();
        int currentScrollerY = scroller.getCurrY();
        if (this.mPreviousScrollerY == 0) {
            this.mPreviousScrollerY = scroller.getStartY();
        }
        scrollBy(0, currentScrollerY - this.mPreviousScrollerY);
        this.mPreviousScrollerY = currentScrollerY;
        if (scroller.isFinished()) {
            onScrollerFinished(scroller);
        } else {
            invalidate();
        }
    }

    @Override // android.view.View
    public void scrollBy(int x, int y) {
        int[] selectorIndices = this.mSelectorIndices;
        if (!this.mWrapSelectorWheel && y > 0 && selectorIndices[2] <= this.mMinValue) {
            this.mCurrentScrollOffset = this.mInitialScrollOffset;
            return;
        }
        if (!this.mWrapSelectorWheel && y < 0 && selectorIndices[2] >= maxValue()) {
            this.mCurrentScrollOffset = this.mInitialScrollOffset;
            return;
        }
        this.mCurrentScrollOffset += y;
        while (true) {
            int i = this.mCurrentScrollOffset;
            if (i - this.mInitialScrollOffset <= this.mSelectorTextGapHeight) {
                break;
            }
            this.mCurrentScrollOffset = i - this.mSelectorElementHeight;
            decrementSelectorIndices(selectorIndices);
            setValueInternal(selectorIndices[2], true);
            if (!this.mWrapSelectorWheel && selectorIndices[2] <= this.mMinValue) {
                this.mCurrentScrollOffset = this.mInitialScrollOffset;
            }
        }
        while (true) {
            int i2 = this.mCurrentScrollOffset;
            if (i2 - this.mInitialScrollOffset < (-this.mSelectorTextGapHeight)) {
                this.mCurrentScrollOffset = i2 + this.mSelectorElementHeight;
                incrementSelectorIndices(selectorIndices);
                setValueInternal(selectorIndices[2], true);
                if (!this.mWrapSelectorWheel && selectorIndices[2] >= maxValue()) {
                    this.mCurrentScrollOffset = this.mInitialScrollOffset;
                }
            } else {
                return;
            }
        }
    }

    public void setOnValueChangedListener(OnValueChangeListener onValueChangedListener) {
        this.mOnValueChangeListener = onValueChangedListener;
    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.mOnScrollListener = onScrollListener;
    }

    public void setFormatter(Formatter formatter) {
        if (formatter == this.mFormatter) {
            return;
        }
        this.mFormatter = formatter;
        initializeSelectorWheelIndices();
    }

    public void setValue(int value) {
        setValueInternal(value, false);
    }

    private void tryComputeMaxWidth() {
        if (!this.mComputeMaxWidth) {
            return;
        }
        int maxTextWidth = 0;
        String[] strArr = this.mDisplayedValues;
        if (strArr == null) {
            float maxDigitWidth = TOP_AND_BOTTOM_FADING_EDGE_STRENGTH;
            for (int i = 0; i <= 9; i++) {
                float digitWidth = this.mSelectorWheelPaint.measureText(formatNumberWithLocale(i));
                if (digitWidth > maxDigitWidth) {
                    maxDigitWidth = digitWidth;
                }
            }
            int numberOfDigits = 0;
            for (int current = this.mMaxValue; current > 0; current /= 10) {
                numberOfDigits++;
            }
            return;
        }
        int valueCount = strArr.length;
        for (int i2 = 0; i2 < valueCount; i2++) {
            float textWidth = this.mSelectorWheelPaint.measureText(this.mDisplayedValues[i2]);
            if (textWidth > maxTextWidth) {
                maxTextWidth = (int) textWidth;
            }
        }
    }

    public boolean getWrapSelectorWheel() {
        return this.mWrapSelectorWheel;
    }

    public void setWrapSelectorWheel(boolean wrapSelectorWheel) {
        boolean wrappingAllowed = maxValue() - this.mMinValue >= this.mSelectorIndices.length;
        if ((!wrapSelectorWheel || wrappingAllowed) && wrapSelectorWheel != this.mWrapSelectorWheel) {
            this.mWrapSelectorWheel = wrapSelectorWheel;
        }
    }

    public int getValue() {
        return this.mValue;
    }

    public int getMinValue() {
        return this.mMinValue;
    }

    public void setMinValue(int minValue) {
        if (this.mMinValue == minValue) {
            return;
        }
        if (minValue < 0) {
            throw new IllegalArgumentException("minValue must be >= 0");
        }
        this.mMinValue = minValue;
        if (minValue > this.mValue) {
            this.mValue = minValue;
        }
        boolean wrapSelectorWheel = maxValue() - this.mMinValue > this.mSelectorIndices.length;
        setWrapSelectorWheel(wrapSelectorWheel);
        initializeSelectorWheelIndices();
        tryComputeMaxWidth();
        invalidate();
    }

    public int getMaxValue() {
        return this.mMaxValue;
    }

    public void setMaxValue(int maxValue, boolean unsetValue) {
        if (this.mMaxValue == maxValue) {
            return;
        }
        if (maxValue < 0) {
            throw new IllegalArgumentException("maxValue must be >= 0");
        }
        this.mMaxValue = maxValue;
        if (maxValue < this.mValue) {
            this.mValue = maxValue;
        }
        this.mHasUnsetValue = unsetValue;
        boolean wrapSelectorWheel = maxValue() - this.mMinValue > this.mSelectorIndices.length;
        setWrapSelectorWheel(wrapSelectorWheel);
        initializeSelectorWheelIndices();
        tryComputeMaxWidth();
        invalidate();
    }

    public void setMaxValue(int maxValue) {
        setMaxValue(maxValue, false);
    }

    private boolean hasUnset() {
        return this.mHasUnsetValue;
    }

    public void setTextSize(int normalSize, int highlightSize) {
        if (normalSize < 0 || highlightSize < 0) {
            throw new IllegalArgumentException("the text size mus be >= 0 ");
        }
        this.mNormalSize = normalSize;
        this.mHighlightSize = highlightSize;
    }

    private float getTextSizeByOffset(int offset) {
        int i = this.mMiddleScrollOffset;
        int i2 = this.mSelectorElementHeight;
        if (offset <= i - i2 || offset >= i + i2) {
            return this.mNormalSize;
        }
        if (offset < i) {
            int i3 = this.mNormalSize;
            return (float) (((double) i3) + (((((double) (offset - (i - i2))) * 1.0d) / ((double) i2)) * ((double) (this.mHighlightSize - i3))));
        }
        if (offset >= i) {
            int i4 = this.mNormalSize;
            return (float) (((double) i4) + (((((double) ((i + i2) - offset)) * 1.0d) / ((double) i2)) * ((double) (this.mHighlightSize - i4))));
        }
        return this.mNormalSize;
    }

    public void setTextColor(int normalColor, int highlightColor) {
        this.mNormalColor = normalColor;
        this.mHighlightColor = highlightColor;
    }

    private int getTextColorByOffset(int offset) {
        int i = this.mMiddleScrollOffset;
        int i2 = this.mSelectorElementHeight;
        if (offset <= i - i2 || offset >= i + i2) {
            int res_alpha = this.mNormalColor;
            return res_alpha;
        }
        if (offset == i) {
            return this.mHighlightColor;
        }
        float rate = 1.0f;
        if (offset < i) {
            rate = ((offset - (i - i2)) * 1.0f) / i2;
        }
        int i3 = this.mMiddleScrollOffset;
        if (offset >= i3) {
            int i4 = this.mSelectorElementHeight;
            rate = (((i3 + i4) - offset) * 1.0f) / i4;
        }
        int i5 = this.mNormalColor;
        int normal_color = i5 & 16777215;
        int i6 = this.mHighlightColor;
        int highlight_color = 16777215 & i6;
        int nb = normal_color & 255;
        int hb = highlight_color & 255;
        int rb = (int) (nb + ((hb - nb) * rate));
        int res_color = 0 | rb;
        int ng = (normal_color >> 8) & 255;
        int hg = (highlight_color >> 8) & 255;
        int rg = (int) (ng + ((hg - ng) * rate));
        int nr = (normal_color >> 16) & 255;
        int hr = (highlight_color >> 16) & 255;
        int rr = (int) (nr + ((hr - nr) * rate));
        int res_color2 = (rr << 16) | res_color | (rg << 8);
        int normal_alpha = i5 >> 24;
        int highlight_alpha = i6 >> 24;
        int rr2 = highlight_alpha - normal_alpha;
        int res_alpha2 = (int) (normal_alpha + (rr2 * rate));
        return (res_alpha2 << 24) | res_color2;
    }

    public String[] getDisplayedValues() {
        return this.mDisplayedValues;
    }

    public void setDisplayedValues(String[] displayedValues) {
        if (this.mDisplayedValues == displayedValues) {
            return;
        }
        this.mDisplayedValues = displayedValues;
        initializeSelectorWheelIndices();
    }

    @Override // android.view.View
    protected float getTopFadingEdgeStrength() {
        return TOP_AND_BOTTOM_FADING_EDGE_STRENGTH;
    }

    @Override // android.view.View
    protected float getBottomFadingEdgeStrength() {
        return TOP_AND_BOTTOM_FADING_EDGE_STRENGTH;
    }

    @Override // android.widget.LinearLayout, android.view.View
    protected void onDraw(Canvas canvas) {
        String scrollSelectorValue;
        if (!this.mHasSelectorWheel) {
            super.onDraw(canvas);
            return;
        }
        int y = this.mCurrentScrollOffset;
        int x = (getRight() - getLeft()) / 2;
        int[] selectorIndices = this.mSelectorIndices;
        for (int selectorIndex : selectorIndices) {
            if (looksUnset(selectorIndex)) {
                scrollSelectorValue = UNSET_STRING;
            } else {
                String scrollSelectorValue2 = this.mSelectorIndexToStringCache.get(selectorIndex);
                scrollSelectorValue = scrollSelectorValue2;
            }
            float curSize = getTextSizeByOffset(y);
            this.mSelectorWheelPaint.setTextSize(curSize);
            int curColor = getTextColorByOffset(y);
            this.mSelectorWheelPaint.setColor(curColor);
            canvas.drawText(scrollSelectorValue, x, y, this.mSelectorWheelPaint);
            y += this.mSelectorElementHeight;
        }
        int i = selectorIndices[2];
        if (isTimeChanged(i) && Math.abs(this.mFlingScroller.getFinalY() - this.mFlingScroller.getCurrY()) > 50) {
            this.mOldTime = selectorIndices[2];
            postDelayed(this.mSoundRunnable, 10L);
        } else if (isTimeChanged(selectorIndices[2]) && this.mFlingScroller.getFinalY() <= this.mFlingScroller.getCurrY()) {
            this.mOldTime = selectorIndices[2];
            postDelayed(this.mSoundRunnable, 30L);
        }
    }

    private boolean isTimeChanged(int newTime) {
        if (this.mOldTime == -1) {
            this.mOldTime = newTime;
        }
        if (this.mOldTime == newTime) {
            return false;
        }
        return true;
    }

    @Override // android.view.View
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(SmartisanNumberPicker.class.getName());
        event.setScrollable(true);
        event.setScrollY((this.mMinValue + this.mValue) * this.mSelectorElementHeight);
    }

    private int makeMeasureSpec(int measureSpec, int maxSize) {
        if (maxSize == -1) {
            return measureSpec;
        }
        int size = View.MeasureSpec.getSize(measureSpec);
        int mode = View.MeasureSpec.getMode(measureSpec);
        if (mode == Integer.MIN_VALUE) {
            return View.MeasureSpec.makeMeasureSpec(Math.min(size, maxSize), View.MeasureSpec.EXACTLY);
        }
        if (mode == 0) {
            return View.MeasureSpec.makeMeasureSpec(maxSize, View.MeasureSpec.EXACTLY);
        }
        if (mode == 1073741824) {
            return measureSpec;
        }
        throw new IllegalArgumentException("Unknown measure mode: " + mode);
    }

    private int resolveSizeAndStateRespectingMinSize(int minSize, int measuredSize, int measureSpec) {
        if (minSize != -1) {
            int desiredWidth = Math.max(minSize, measuredSize);
            return resolveSizeAndState(desiredWidth, measureSpec, 0);
        }
        return measuredSize;
    }

    private void initializeSelectorWheelIndices() {
        this.mSelectorIndexToStringCache.clear();
        int[] selectorIndices = this.mSelectorIndices;
        int current = getValue();
        if (looksUnset(current)) {
            selectorIndices[0] = this.mMaxValue - 1;
            ensureCachedScrollSelectorValue(selectorIndices[0]);
            selectorIndices[1] = this.mMaxValue;
            ensureCachedScrollSelectorValue(selectorIndices[1]);
            selectorIndices[2] = 4;
            ensureCachedScrollSelectorValue(selectorIndices[2]);
            selectorIndices[3] = this.mMinValue;
            ensureCachedScrollSelectorValue(selectorIndices[3]);
            selectorIndices[4] = this.mMinValue + 1;
            ensureCachedScrollSelectorValue(selectorIndices[4]);
            return;
        }
        for (int i = 0; i < this.mSelectorIndices.length; i++) {
            int selectorIndex = (i - 2) + current;
            if (this.mWrapSelectorWheel) {
                selectorIndex = getWrappedSelectorIndex(selectorIndex);
            }
            selectorIndices[i] = selectorIndex;
            ensureCachedScrollSelectorValue(selectorIndices[i]);
        }
    }

    private void setValueInternal(int current, boolean notifyChange) {
        int current2;
        if (this.mValue == current) {
            return;
        }
        if (looksUnset(current) && this.mValue == 4) {
            return;
        }
        if (this.mWrapSelectorWheel) {
            if (looksUnset(current)) {
                current2 = 4;
            } else {
                current2 = getWrappedSelectorIndex(current);
            }
        } else if (looksUnset(current)) {
            current2 = 4;
        } else {
            current2 = Math.min(Math.max(current, this.mMinValue), this.mMaxValue);
        }
        int previous = this.mValue;
        this.mValue = current2;
        if (notifyChange) {
            notifyChange(previous, current2);
            startVibrate();
        }
        initializeSelectorWheelIndices();
        invalidate();
    }

    private void startVibrate() {
        performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
    }

    private boolean looksUnset(int current) {
        return (current > this.mMaxValue || current < this.mMinValue) && hasUnset();
    }

    /* JADX INFO: Access modifiers changed from: private */
    private void changeValueByOne(boolean increment) {
        if (!this.mHasSelectorWheel) {
            if (increment) {
                setValueInternal(this.mValue + 1, true);
                return;
            } else {
                setValueInternal(this.mValue - 1, true);
                return;
            }
        }
        if (!moveToFinalScrollerPosition(this.mFlingScroller)) {
            moveToFinalScrollerPosition(this.mAdjustScroller);
        }
        this.mPreviousScrollerY = 0;
        if (increment) {
            this.mFlingScroller.startScroll(0, 0, 0, -this.mSelectorElementHeight, SNAP_SCROLL_DURATION);
        } else {
            this.mFlingScroller.startScroll(0, 0, 0, this.mSelectorElementHeight, SNAP_SCROLL_DURATION);
        }
        invalidate();
    }

    private void initializeSelectorWheel() {
        initializeSelectorWheelIndices();
        int[] selectorIndices = this.mSelectorIndices;
        int totalTextHeight = selectorIndices.length * this.mNormalSize;
        float totalTextGapHeight = (getBottom() - getTop()) - totalTextHeight;
        float textGapCount = selectorIndices.length;
        int i = (int) ((totalTextGapHeight / textGapCount) + 0.5f);
        this.mSelectorTextGapHeight = i;
        this.mSelectorElementHeight = this.mNormalSize + i;
        int editTextTextPosition = (getHeight() + this.mNormalSize) / 2;
        int i2 = this.mSelectorElementHeight;
        int i3 = editTextTextPosition - (i2 * 2);
        this.mInitialScrollOffset = i3;
        this.mMiddleScrollOffset = (i2 * 2) + i3;
        this.mCurrentScrollOffset = i3;
    }

    private void initializeFadingEdges() {
        setVerticalFadingEdgeEnabled(true);
        setFadingEdgeLength(((getBottom() - getTop()) - this.mNormalSize) / 2);
    }

    private void onScrollerFinished(Scroller scroller) {
        if (scroller == this.mFlingScroller) {
            ensureScrollWheelAdjusted();
            onScrollStateChange(0);
        }
    }

    private void onScrollStateChange(int scrollState) {
        if (this.mScrollState == scrollState) {
            return;
        }
        this.mScrollState = scrollState;
        OnScrollListener onScrollListener = this.mOnScrollListener;
        if (onScrollListener != null) {
            onScrollListener.onScrollStateChange(this, scrollState);
        }
    }

    private void fling(int velocityY) {
        this.mPreviousScrollerY = 0;
        if (velocityY > 0) {
            this.mFlingScroller.fling(0, 0, 0, velocityY, 0, 0, 0, Integer.MAX_VALUE);
        } else {
            this.mFlingScroller.fling(0, Integer.MAX_VALUE, 0, velocityY, 0, 0, 0, Integer.MAX_VALUE);
        }
        invalidate();
    }

    /* JADX INFO: Access modifiers changed from: private */
    private int getWrappedSelectorIndex(int selectorIndex) {
        return SmartisanPickerMath.wrapSelectorIndex(
                selectorIndex, this.mMinValue, maxValue());
    }

    private int maxValue() {
        return this.mMaxValue + (hasUnset() ? 1 : 0);
    }

    private void incrementSelectorIndices(int[] selectorIndices) {
        for (int i = 0; i < selectorIndices.length - 1; i++) {
            selectorIndices[i] = selectorIndices[i + 1];
        }
        int i2 = selectorIndices.length;
        int nextScrollSelectorIndex = selectorIndices[i2 - 2] + 1;
        if (this.mWrapSelectorWheel && nextScrollSelectorIndex > maxValue()) {
            nextScrollSelectorIndex = this.mMinValue;
        }
        selectorIndices[selectorIndices.length - 1] = nextScrollSelectorIndex;
        ensureCachedScrollSelectorValue(nextScrollSelectorIndex);
    }

    private void decrementSelectorIndices(int[] selectorIndices) {
        for (int i = selectorIndices.length - 1; i > 0; i--) {
            selectorIndices[i] = selectorIndices[i - 1];
        }
        int i2 = selectorIndices[1];
        int nextScrollSelectorIndex = i2 - 1;
        if (this.mWrapSelectorWheel && nextScrollSelectorIndex < this.mMinValue) {
            nextScrollSelectorIndex = maxValue();
        }
        selectorIndices[0] = nextScrollSelectorIndex;
        ensureCachedScrollSelectorValue(nextScrollSelectorIndex);
    }

    private void ensureCachedScrollSelectorValue(int selectorIndex) {
        String scrollSelectorValue;
        SparseArray<String> cache = this.mSelectorIndexToStringCache;
        String scrollSelectorValue2 = cache.get(selectorIndex);
        if (scrollSelectorValue2 != null) {
            return;
        }
        int i = this.mMinValue;
        if (selectorIndex < i || selectorIndex > this.mMaxValue) {
            if (hasUnset()) {
                return;
            } else {
                scrollSelectorValue = "";
            }
        } else {
            String[] strArr = this.mDisplayedValues;
            if (strArr != null) {
                int displayedValueIndex = selectorIndex - i;
                scrollSelectorValue = strArr[displayedValueIndex];
            } else {
                scrollSelectorValue = formatNumber(selectorIndex);
            }
        }
        cache.put(selectorIndex, scrollSelectorValue);
    }

    /* JADX INFO: Access modifiers changed from: private */
    private String formatNumber(int value) {
        Formatter formatter = this.mFormatter;
        return formatter != null ? formatter.format(value) : formatNumberWithLocale(value);
    }

    private void notifyChange(int previous, int current) {
        OnValueChangeListener onValueChangeListener = this.mOnValueChangeListener;
        if (onValueChangeListener != null) {
            onValueChangeListener.onValueChange(this, previous, this.mValue);
        }
    }

    private boolean ensureScrollWheelAdjusted() {
        int deltaY = this.mInitialScrollOffset - this.mCurrentScrollOffset;
        if (deltaY == 0) {
            return false;
        }
        this.mPreviousScrollerY = 0;
        int iAbs = Math.abs(deltaY);
        int i = this.mSelectorElementHeight;
        if (iAbs > i / 2) {
            if (deltaY > 0) {
                i = -i;
            }
            deltaY += i;
        }
        this.mAdjustScroller.startScroll(0, 0, 0, deltaY, SELECTOR_ADJUSTMENT_DURATION_MILLIS);
        invalidate();
        return true;
    }

    private static String formatNumberWithLocale(int value) {
        return String.format(Locale.getDefault(), "%d", Integer.valueOf(value));
    }

    private static void log(String msg) {
        Log.d(TAG, msg);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (sPool == null) {
            SoundPool soundPool = new SoundPool(6, 1, 0);
            sPool = soundPool;
            sSoundId = soundPool.load(getContext().getApplicationContext(), R.raw.smartisan_rom_time_picker, 1);
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        SoundPool soundPool = sPool;
        if (soundPool != null) {
            soundPool.release();
            sPool = null;
        }
    }

    @Override // android.view.View
    public AccessibilityNodeProvider getAccessibilityNodeProvider() {
        if (!this.mHasSelectorWheel) {
            return super.getAccessibilityNodeProvider();
        }
        if (this.mAccessibilityNodeProvider == null) {
            this.mAccessibilityNodeProvider = new AccessibilityNodeProviderImpl();
        }
        return this.mAccessibilityNodeProvider;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected boolean dispatchHoverEvent(MotionEvent event) {
        int hoveredVirtualViewId;
        if (!this.mHasSelectorWheel) {
            return super.dispatchHoverEvent(event);
        }
        if (isAccessibilityEnabled()) {
            int eventY = (int) event.getY();
            int hoveredVirtualViewId2 = this.mTopSelectionDividerTop;
            int i = this.mSelectorElementHeight;
            if (eventY < hoveredVirtualViewId2 - i) {
                hoveredVirtualViewId = 1;
            } else if (eventY < hoveredVirtualViewId2) {
                hoveredVirtualViewId = 2;
            } else {
                int hoveredVirtualViewId3 = this.mBottomSelectionDividerBottom;
                if (eventY > i + hoveredVirtualViewId3) {
                    hoveredVirtualViewId = 5;
                } else if (eventY > hoveredVirtualViewId3) {
                    hoveredVirtualViewId = 4;
                } else {
                    hoveredVirtualViewId = 3;
                }
            }
            int action = event.getActionMasked();
            AccessibilityNodeProviderImpl provider = (AccessibilityNodeProviderImpl) getAccessibilityNodeProvider();
            if (action == 7) {
                int i2 = this.mLastHoveredChildVirtualViewId;
                if (i2 == hoveredVirtualViewId || i2 == -1) {
                    return false;
                }
                provider.sendAccessibilityEventForVirtualView(i2, 256);
                provider.sendAccessibilityEventForVirtualView(hoveredVirtualViewId, 128);
                this.mLastHoveredChildVirtualViewId = hoveredVirtualViewId;
                provider.performAction(hoveredVirtualViewId, 64, null);
                return false;
            }
            if (action == 9) {
                provider.sendAccessibilityEventForVirtualView(hoveredVirtualViewId, 128);
                this.mLastHoveredChildVirtualViewId = hoveredVirtualViewId;
                provider.performAction(hoveredVirtualViewId, 64, null);
                return false;
            }
            if (action != 10) {
                return false;
            }
            provider.sendAccessibilityEventForVirtualView(hoveredVirtualViewId, 256);
            this.mLastHoveredChildVirtualViewId = -1;
            return false;
        }
        return false;
    }

    private boolean isAccessibilityEnabled() {
        AccessibilityManager manager = (AccessibilityManager) getContext().getSystemService(
                Context.ACCESSIBILITY_SERVICE);
        return manager != null && manager.isEnabled();
    }

    private class AccessibilityNodeProviderImpl extends AccessibilityNodeProvider {
        private static final int UNDEFINED = Integer.MIN_VALUE;
        private static final int VIRTUAL_VIEW_ID_CURRENT_VALUE = 3;
        private static final int VIRTUAL_VIEW_ID_DECREMENT = 2;
        private static final int VIRTUAL_VIEW_ID_DECREMENT_SECOND = 1;
        private static final int VIRTUAL_VIEW_ID_INCREMENT = 4;
        private static final int VIRTUAL_VIEW_ID_INCREMENT_SECOND = 5;
        private int mAccessibilityFocusedView;
        private final int[] mTempArray;
        private final Rect mTempRect;

        private AccessibilityNodeProviderImpl() {
            this.mTempRect = new Rect();
            this.mTempArray = new int[2];
            this.mAccessibilityFocusedView = Integer.MIN_VALUE;
        }

        @Override // android.view.accessibility.AccessibilityNodeProvider
        public AccessibilityNodeInfo createAccessibilityNodeInfo(int virtualViewId) {
            if (virtualViewId == -1) {
                return createAccessibilityNodeInfoForNumberPicker(SmartisanNumberPicker.this.getScrollX(), SmartisanNumberPicker.this.getScrollY(), SmartisanNumberPicker.this.getScrollX() + (SmartisanNumberPicker.this.getWidth()), SmartisanNumberPicker.this.getScrollY() + (SmartisanNumberPicker.this.getHeight()));
            }
            if (virtualViewId == 1) {
                return createAccessibilityNodeInfoForVirtualButton(1, getSecondVirtualDecrementButtonText(), SmartisanNumberPicker.this.getScrollX(), SmartisanNumberPicker.this.getScrollY(), SmartisanNumberPicker.this.getScrollX() + (SmartisanNumberPicker.this.getWidth()), mTopSelectionDividerTop - SmartisanNumberPicker.this.mSelectorElementHeight);
            }
            if (virtualViewId == 2) {
                return createAccessibilityNodeInfoForVirtualButton(2, getVirtualDecrementButtonText(), SmartisanNumberPicker.this.getScrollX(), SmartisanNumberPicker.this.getScrollY() + SmartisanNumberPicker.this.mSelectorElementHeight, SmartisanNumberPicker.this.getScrollX() + (SmartisanNumberPicker.this.getWidth()), mTopSelectionDividerTop);
            }
            if (virtualViewId == 3) {
                return createAccessibilityNodeInfoForVirtualButton(3, getCurrentValueVirtualButtonText(), SmartisanNumberPicker.this.getScrollX(), mTopSelectionDividerTop, SmartisanNumberPicker.this.getScrollX() + (SmartisanNumberPicker.this.getWidth()), SmartisanNumberPicker.this.mBottomSelectionDividerBottom);
            }
            if (virtualViewId == 4) {
                return createAccessibilityNodeInfoForVirtualButton(4, getVirtualIncrementButtonText(), SmartisanNumberPicker.this.getScrollX(), SmartisanNumberPicker.this.mBottomSelectionDividerBottom, SmartisanNumberPicker.this.getScrollX() + (SmartisanNumberPicker.this.getWidth()), (SmartisanNumberPicker.this.getScrollY() + (SmartisanNumberPicker.this.getHeight())) - SmartisanNumberPicker.this.mSelectorElementHeight);
            }
            if (virtualViewId == 5) {
                return createAccessibilityNodeInfoForVirtualButton(5, getSecondVirtualIncrementButtonText(), SmartisanNumberPicker.this.getScrollX(), SmartisanNumberPicker.this.mBottomSelectionDividerBottom + SmartisanNumberPicker.this.mSelectorElementHeight, SmartisanNumberPicker.this.getScrollX() + (SmartisanNumberPicker.this.getWidth()), SmartisanNumberPicker.this.getScrollY() + (SmartisanNumberPicker.this.getHeight()));
            }
            return super.createAccessibilityNodeInfo(virtualViewId);
        }

        @Override // android.view.accessibility.AccessibilityNodeProvider
        public List<AccessibilityNodeInfo> findAccessibilityNodeInfosByText(String searched, int virtualViewId) {
            if (TextUtils.isEmpty(searched)) {
                return Collections.emptyList();
            }
            String searchedLowerCase = searched.toLowerCase(Locale.getDefault());
            List<AccessibilityNodeInfo> result = new ArrayList<>();
            if (virtualViewId == -1) {
                findAccessibilityNodeInfosByTextInChild(searchedLowerCase, 1, result);
                findAccessibilityNodeInfosByTextInChild(searchedLowerCase, 2, result);
                findAccessibilityNodeInfosByTextInChild(searchedLowerCase, 3, result);
                findAccessibilityNodeInfosByTextInChild(searchedLowerCase, 4, result);
                findAccessibilityNodeInfosByTextInChild(searchedLowerCase, 5, result);
                return result;
            }
            if (virtualViewId == 1 || virtualViewId == 2 || virtualViewId == 3 || virtualViewId == 4 || virtualViewId == 5) {
                findAccessibilityNodeInfosByTextInChild(searchedLowerCase, virtualViewId, result);
                return result;
            }
            return super.findAccessibilityNodeInfosByText(searched, virtualViewId);
        }

        // This is the implementation of a focus action requested by the accessibility service,
        // not application-driven focus stealing.
        @android.annotation.SuppressLint("AccessibilityFocus")
        @Override // android.view.accessibility.AccessibilityNodeProvider
        public boolean performAction(int virtualViewId, int action, Bundle arguments) {
            if (virtualViewId != -1) {
                if (virtualViewId == 1 || virtualViewId == 2) {
                    if (action == 16) {
                        if (!SmartisanNumberPicker.this.isEnabled()) {
                            return false;
                        }
                        SmartisanNumberPicker.this.changeValueByOne(false);
                        sendAccessibilityEventForVirtualView(virtualViewId, 1);
                        return true;
                    }
                    if (action == 64) {
                        if (this.mAccessibilityFocusedView == virtualViewId) {
                            return false;
                        }
                        this.mAccessibilityFocusedView = virtualViewId;
                        sendAccessibilityEventForVirtualView(virtualViewId, 32768);
                        SmartisanNumberPicker smartisanNumberPicker = SmartisanNumberPicker.this;
                        smartisanNumberPicker.invalidate(0, 0, smartisanNumberPicker.getWidth(), mTopSelectionDividerTop);
                        return true;
                    }
                    if (action != 128 || this.mAccessibilityFocusedView != virtualViewId) {
                        return false;
                    }
                    this.mAccessibilityFocusedView = Integer.MIN_VALUE;
                    sendAccessibilityEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED);
                    SmartisanNumberPicker smartisanNumberPicker2 = SmartisanNumberPicker.this;
                    smartisanNumberPicker2.invalidate(0, 0, smartisanNumberPicker2.getWidth(), mTopSelectionDividerTop);
                    return true;
                }
                if (virtualViewId == 3) {
                    if (action == 16) {
                        if (!SmartisanNumberPicker.this.isEnabled()) {
                            return false;
                        }
                        SmartisanNumberPicker.this.performClick();
                        sendAccessibilityEventForVirtualView(virtualViewId, 1);
                        return true;
                    }
                    if (action == 64) {
                        if (this.mAccessibilityFocusedView == virtualViewId) {
                            return false;
                        }
                        this.mAccessibilityFocusedView = virtualViewId;
                        sendAccessibilityEventForVirtualView(virtualViewId, 32768);
                        SmartisanNumberPicker smartisanNumberPicker3 = SmartisanNumberPicker.this;
                        smartisanNumberPicker3.invalidate(0, smartisanNumberPicker3.mTopSelectionDividerTop, SmartisanNumberPicker.this.getWidth(), SmartisanNumberPicker.this.mBottomSelectionDividerBottom);
                        return true;
                    }
                    if (action != 128 || this.mAccessibilityFocusedView != virtualViewId) {
                        return false;
                    }
                    this.mAccessibilityFocusedView = Integer.MIN_VALUE;
                    sendAccessibilityEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED);
                    SmartisanNumberPicker smartisanNumberPicker4 = SmartisanNumberPicker.this;
                    smartisanNumberPicker4.invalidate(0, smartisanNumberPicker4.mTopSelectionDividerTop, SmartisanNumberPicker.this.getWidth(), SmartisanNumberPicker.this.mBottomSelectionDividerBottom);
                    return true;
                }
                if (virtualViewId == 4 || virtualViewId == 5) {
                    if (action == 16) {
                        if (!SmartisanNumberPicker.this.isEnabled()) {
                            return false;
                        }
                        SmartisanNumberPicker.this.changeValueByOne(true);
                        sendAccessibilityEventForVirtualView(virtualViewId, 1);
                        return true;
                    }
                    if (action == 64) {
                        if (this.mAccessibilityFocusedView == virtualViewId) {
                            return false;
                        }
                        this.mAccessibilityFocusedView = virtualViewId;
                        sendAccessibilityEventForVirtualView(virtualViewId, 32768);
                        SmartisanNumberPicker smartisanNumberPicker5 = SmartisanNumberPicker.this;
                        smartisanNumberPicker5.invalidate(0, smartisanNumberPicker5.mBottomSelectionDividerBottom, SmartisanNumberPicker.this.getWidth(), SmartisanNumberPicker.this.getHeight());
                        return true;
                    }
                    if (action != 128 || this.mAccessibilityFocusedView != virtualViewId) {
                        return false;
                    }
                    this.mAccessibilityFocusedView = Integer.MIN_VALUE;
                    sendAccessibilityEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED);
                    SmartisanNumberPicker smartisanNumberPicker6 = SmartisanNumberPicker.this;
                    smartisanNumberPicker6.invalidate(0, smartisanNumberPicker6.mBottomSelectionDividerBottom, SmartisanNumberPicker.this.getWidth(), SmartisanNumberPicker.this.getHeight());
                    return true;
                }
            } else {
                if (action == 64) {
                    if (this.mAccessibilityFocusedView == virtualViewId) {
                        return false;
                    }
                    if (SmartisanNumberPicker.this.performAccessibilityAction(
                            AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, arguments)) {
                        this.mAccessibilityFocusedView = virtualViewId;
                        return true;
                    }
                    return false;
                }
                if (action == 128) {
                    if (this.mAccessibilityFocusedView != virtualViewId) {
                        return false;
                    }
                    if (SmartisanNumberPicker.this.performAccessibilityAction(
                            AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS, arguments)) {
                        this.mAccessibilityFocusedView = Integer.MIN_VALUE;
                        return true;
                    }
                    return false;
                }
                if (action == 4096) {
                    if (!SmartisanNumberPicker.this.isEnabled() || (!SmartisanNumberPicker.this.getWrapSelectorWheel() && SmartisanNumberPicker.this.getValue() >= SmartisanNumberPicker.this.getMaxValue())) {
                        return false;
                    }
                    SmartisanNumberPicker.this.changeValueByOne(true);
                    return true;
                }
                if (action == 8192) {
                    if (!SmartisanNumberPicker.this.isEnabled() || (!SmartisanNumberPicker.this.getWrapSelectorWheel() && SmartisanNumberPicker.this.getValue() <= SmartisanNumberPicker.this.getMinValue())) {
                        return false;
                    }
                    SmartisanNumberPicker.this.changeValueByOne(false);
                    return true;
                }
            }
            return super.performAction(virtualViewId, action, arguments);
        }

        /* JADX INFO: Access modifiers changed from: private */
        private void sendAccessibilityEventForVirtualView(int virtualViewId, int eventType) {
            if (virtualViewId == 1) {
                if (hasSecondVirtualDecrementButton()) {
                    sendAccessibilityEventForVirtualButton(virtualViewId, eventType, getSecondVirtualDecrementButtonText());
                    return;
                }
                return;
            }
            if (virtualViewId == 2) {
                if (hasVirtualDecrementButton()) {
                    sendAccessibilityEventForVirtualButton(virtualViewId, eventType, getVirtualDecrementButtonText());
                }
            } else {
                if (virtualViewId == 3) {
                    sendAccessibilityEventForVirtualButton(virtualViewId, eventType, getCurrentValueVirtualButtonText());
                    return;
                }
                if (virtualViewId == 4) {
                    if (hasVirtualIncrementButton()) {
                        sendAccessibilityEventForVirtualButton(virtualViewId, eventType, getVirtualIncrementButtonText());
                    }
                } else if (virtualViewId == 5 && hasSecondVirtualIncrementButton()) {
                    sendAccessibilityEventForVirtualButton(virtualViewId, eventType, getSecondVirtualIncrementButtonText());
                }
            }
        }

        private void sendAccessibilityEventForVirtualButton(int virtualViewId, int eventType, String text) {
            if (SmartisanNumberPicker.this.isAccessibilityEnabled()) {
                AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
                event.setClassName(Button.class.getName());
                event.setPackageName(SmartisanNumberPicker.this.getContext().getPackageName());
                event.getText().add(text);
                event.setEnabled(SmartisanNumberPicker.this.isEnabled());
                event.setSource(SmartisanNumberPicker.this, virtualViewId);
                SmartisanNumberPicker smartisanNumberPicker = SmartisanNumberPicker.this;
                smartisanNumberPicker.requestSendAccessibilityEvent(smartisanNumberPicker, event);
            }
        }

        private void findAccessibilityNodeInfosByTextInChild(String searchedLowerCase, int virtualViewId, List<AccessibilityNodeInfo> outResult) {
            String virtualButtonText;
            if (virtualViewId == 1) {
                virtualButtonText = getSecondVirtualDecrementButtonText();
            } else if (virtualViewId == 2) {
                virtualButtonText = getVirtualDecrementButtonText();
            } else if (virtualViewId == 3) {
                virtualButtonText = getCurrentValueVirtualButtonText();
            } else if (virtualViewId == 4) {
                virtualButtonText = getVirtualIncrementButtonText();
            } else if (virtualViewId == 5) {
                virtualButtonText = getSecondVirtualIncrementButtonText();
            } else {
                return;
            }
            if (!TextUtils.isEmpty(virtualButtonText)
                    && virtualButtonText.toLowerCase(Locale.getDefault())
                            .contains(searchedLowerCase)) {
                outResult.add(createAccessibilityNodeInfo(virtualViewId));
            }
        }

        private AccessibilityNodeInfo createAccessibilityNodeInfoForVirtualButton(int virtualViewId, String text, int left, int top, int right, int bottom) {
            boolean clickable = virtualViewId != 3;
            AccessibilityNodeInfo info = AccessibilityNodeInfo.obtain();
            info.setClassName((clickable ? Button.class : TextView.class).getName());
            info.setPackageName(SmartisanNumberPicker.this.getContext().getPackageName());
            info.setSource(SmartisanNumberPicker.this, virtualViewId);
            info.setParent(SmartisanNumberPicker.this);
            info.setText(text);
            info.setClickable(clickable);
            info.setLongClickable(clickable);
            info.setEnabled(SmartisanNumberPicker.this.isEnabled());
            info.setAccessibilityFocused(this.mAccessibilityFocusedView == virtualViewId);
            Rect boundsInParent = this.mTempRect;
            boundsInParent.set(left, top, right, bottom);
            info.setVisibleToUser(isVisibleToUser(boundsInParent));
            info.setBoundsInParent(boundsInParent);
            int[] locationOnScreen = this.mTempArray;
            SmartisanNumberPicker.this.getLocationOnScreen(locationOnScreen);
            boundsInParent.offset(locationOnScreen[0], locationOnScreen[1]);
            info.setBoundsInScreen(boundsInParent);
            if (this.mAccessibilityFocusedView != virtualViewId) {
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_ACCESSIBILITY_FOCUS);
            }
            if (this.mAccessibilityFocusedView == virtualViewId) {
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
            }
            if (SmartisanNumberPicker.this.isEnabled()) {
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK);
            }
            return info;
        }

        private AccessibilityNodeInfo createAccessibilityNodeInfoForNumberPicker(int left, int top, int right, int bottom) {
            AccessibilityNodeInfo info = AccessibilityNodeInfo.obtain();
            info.setClassName(SmartisanNumberPicker.class.getName());
            info.setPackageName(SmartisanNumberPicker.this.getContext().getPackageName());
            info.setSource(SmartisanNumberPicker.this);
            if (hasSecondVirtualDecrementButton()) {
                info.addChild(SmartisanNumberPicker.this, 1);
            }
            if (hasVirtualDecrementButton()) {
                info.addChild(SmartisanNumberPicker.this, 2);
            }
            info.addChild(SmartisanNumberPicker.this, 3);
            if (hasVirtualIncrementButton()) {
                info.addChild(SmartisanNumberPicker.this, 4);
            }
            if (hasSecondVirtualIncrementButton()) {
                info.addChild(SmartisanNumberPicker.this, 5);
            }
            info.setParent((View) SmartisanNumberPicker.this.getParentForAccessibility());
            info.setEnabled(SmartisanNumberPicker.this.isEnabled());
            info.setScrollable(true);
            info.setAccessibilityFocused(this.mAccessibilityFocusedView == -1);

            Rect boundsInParent = this.mTempRect;
            boundsInParent.set(left, top, right, bottom);

            info.setBoundsInParent(boundsInParent);
            info.setVisibleToUser(isVisibleToUser(boundsInParent));
            int[] locationOnScreen = this.mTempArray;
            SmartisanNumberPicker.this.getLocationOnScreen(locationOnScreen);
            boundsInParent.offset(locationOnScreen[0], locationOnScreen[1]);

            info.setBoundsInScreen(boundsInParent);
            if (this.mAccessibilityFocusedView != -1) {
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_ACCESSIBILITY_FOCUS);
            }
            if (this.mAccessibilityFocusedView == -1) {
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
            }
            if (SmartisanNumberPicker.this.isEnabled()) {
                if (SmartisanNumberPicker.this.getWrapSelectorWheel() || SmartisanNumberPicker.this.getValue() < SmartisanNumberPicker.this.getMaxValue()) {
                    info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);
                }
                if (SmartisanNumberPicker.this.getWrapSelectorWheel() || SmartisanNumberPicker.this.getValue() > SmartisanNumberPicker.this.getMinValue()) {
                    info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);
                }
            }
            return info;
        }

        private boolean isVisibleToUser(Rect localBounds) {
            if (!SmartisanNumberPicker.this.isShown() || localBounds.isEmpty()) {
                return false;
            }
            Rect visibleBounds = new Rect();
            return SmartisanNumberPicker.this.getLocalVisibleRect(visibleBounds)
                    && Rect.intersects(localBounds, visibleBounds);
        }

        private boolean hasVirtualDecrementButton() {
            return SmartisanNumberPicker.this.getWrapSelectorWheel() || SmartisanNumberPicker.this.getValue() > SmartisanNumberPicker.this.getMinValue();
        }

        private boolean hasVirtualIncrementButton() {
            return SmartisanNumberPicker.this.getWrapSelectorWheel() || SmartisanNumberPicker.this.getValue() < SmartisanNumberPicker.this.getMaxValue();
        }

        private boolean hasSecondVirtualIncrementButton() {
            return SmartisanNumberPicker.this.getWrapSelectorWheel() || SmartisanNumberPicker.this.getValue() + 1 < SmartisanNumberPicker.this.getMaxValue();
        }

        private boolean hasSecondVirtualDecrementButton() {
            return SmartisanNumberPicker.this.getWrapSelectorWheel() || SmartisanNumberPicker.this.getValue() - 1 > SmartisanNumberPicker.this.getMinValue();
        }

        private String getVirtualDecrementButtonText() {
            return getVirtualButtonTextAt(SmartisanNumberPicker.this.mValue - 1);
        }

        private String getVirtualIncrementButtonText() {
            return getVirtualButtonTextAt(SmartisanNumberPicker.this.mValue + 1);
        }

        private String getSecondVirtualDecrementButtonText() {
            return getVirtualButtonTextAt(SmartisanNumberPicker.this.mValue - 2);
        }

        private String getSecondVirtualIncrementButtonText() {
            return getVirtualButtonTextAt(SmartisanNumberPicker.this.mValue + 2);
        }

        private String getCurrentValueVirtualButtonText() {
            return getVirtualButtonTextAt(SmartisanNumberPicker.this.mValue);
        }

        private String getVirtualButtonTextAt(int value) {
            if (SmartisanNumberPicker.this.mWrapSelectorWheel) {
                value = SmartisanNumberPicker.this.getWrappedSelectorIndex(value);
            }
            if (value > SmartisanNumberPicker.this.mMaxValue || value < SmartisanNumberPicker.this.mMinValue) {
                return null;
            }
            return SmartisanNumberPicker.this.mDisplayedValues == null ? SmartisanNumberPicker.this.formatNumber(value) : SmartisanNumberPicker.this.mDisplayedValues[value - SmartisanNumberPicker.this.mMinValue];
        }
    }
}
