package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
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
public class SmartisanNumberPickerEx extends LinearLayout {
    private static final boolean DBG = false;
    private static final float DEFAULT_LINE_SPACE = 2.0f;
    private static final float DEFAULT_TEXT_SCALE = 1.05f;
    private static final int DEFAULT_TEXT_SIZE = 17;
    private static final int HIGHLIGHT_TEXT_SIZE = 60;
    private static final int SELECTOR_ADJUSTMENT_DURATION_MILLIS = 800;
    private static final int SELECTOR_MAX_FLING_VELOCITY_ADJUSTMENT = 8;
    private static final int SELECTOR_MIDDLE_ITEM_INDEX = 4;
    private static final int SELECTOR_WHEEL_ITEM_COUNT = 9;
    private static final int SIZE_UNSPECIFIED = -1;
    private static final int SNAP_SCROLL_DURATION = 300;
    private static final String TAG = "SmartisanNumberPickerEx";
    private static final int TEXT_WIDTH_OFFSET_IGNORE = 10;
    private static final float TOP_AND_BOTTOM_FADING_EDGE_STRENGTH = 0.0f;
    private static final String UNSET_STRING = "--";
    public static final int UNSET_YEAR = 4;
    private static final float VOLUME = 0.0945f;
    private static final int mHighlightItemSize = 146;
    private static SoundPool sPool;
    private static int sSoundId;

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
    private float CENTER_CONTENT_OFFSET;
    private AccessibilityNodeProviderImpl mAccessibilityNodeProvider;
    private final Scroller mAdjustScroller;
    private int mBottomSelectionDividerBottom;
    private final boolean mComputeMaxWidth;
    private int mCurrentScrollOffset;
    private String[] mDisplayedValues;
    private Drawable mDrawableBg;
    private int mFirstLineY;
    private final Scroller mFlingScroller;
    private Formatter mFormatter;
    private int mHalfCircumference;
    private final boolean mHasSelectorWheel;
    private boolean mHasUnsetValue;
    private int mHeight;
    private int mHighlightColor;
    private int mHighlightSize;
    private String mHightlightSuffix;
    private int mHightlightSuffixFontSize;
    private int mHightlightSuffixMargin;
    private boolean mIngonreMoveEvents;
    private int mInitialColor;
    private int mInitialScrollOffset;
    private int mInitialValue;
    private int mItemsVisibleCount;
    private long mLastDownEventTime;
    private float mLastDownEventY;
    private float mLastDownOrMoveEventY;
    private int mLastHoveredChildVirtualViewId;
    private float mLineSpacingMultiplier;
    private final int mMaxHeight;
    private int mMaxTextHeight;
    private int mMaxValue;
    private int mMaxWidth;
    private String mMaxWidthText;
    private int mMaximumFlingVelocity;
    private int mMidTextWidth;
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
    private Paint mPaintCenterText;
    private Paint mPaintIndicator;
    private Paint mPaintOuterText;
    private int mPreviousScrollerY;
    private int mRadius;
    private float mScaleX;
    private int mScrollState;
    private int mSecondLineY;
    private int mSelectorElementHeight;
    private final SparseArray<String> mSelectorIndexToStringCache;
    private final int[] mSelectorIndices;
    private int mSelectorTextGapHeight;
    private final Paint mSelectorWheelPaint;
    private boolean mShowSoftInputOnTap;
    private boolean mSoundEnable;
    private Runnable mSoundRunnable;
    private Rect mTempRect;
    private float mTextPadding;
    private int mTextSize;
    private int mTopSelectionDividerTop;
    private int mTouchSlop;
    private int mValue;
    private VelocityTracker mVelocityTracker;
    private int mWidth;
    private boolean mWrapSelectorWheel;
    private Context mcContext;
    private static final int DEFAUlT_TEXT_COLOR = Color.parseColor("#4c000000");
    private static final int INITIAL_TEXT_COLOR = Color.parseColor("#e65079d9");
    private static final int HIGHLIGHT_TEXT_COLOR = Color.parseColor("#9a000000");
    private static int mDefaultItemSize = 0;
    private static final TwoDigitFormatter sTwoDigitFormatter = new TwoDigitFormatter();

    public interface Formatter {
        String format(int i);
    }

    public interface OnScrollListener {
        public static final int SCROLL_STATE_FLING = 2;
        public static final int SCROLL_STATE_IDLE = 0;
        public static final int SCROLL_STATE_TOUCH_SCROLL = 1;

        void onScrollStateChange(SmartisanNumberPickerEx smartisanNumberPickerEx, int i);
    }

    public interface OnValueChangeListener {
        void onValueChange(SmartisanNumberPickerEx smartisanNumberPickerEx, int i, int i2);
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

        @Override // smartisanos.widget.SmartisanNumberPickerEx.Formatter
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

    public SmartisanNumberPickerEx(Context context) {
        this(context, null);
    }

    public SmartisanNumberPickerEx(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmartisanNumberPickerEx(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mHightlightSuffix = null;
        this.mSelectorIndexToStringCache = new SparseArray<>();
        this.mSelectorIndices = new int[9];
        this.mNormalSize = DEFAULT_TEXT_SIZE;
        this.mHighlightSize = 60;
        this.mNormalColor = DEFAUlT_TEXT_COLOR;
        this.mHighlightColor = HIGHLIGHT_TEXT_COLOR;
        this.mInitialColor = INITIAL_TEXT_COLOR;
        this.mInitialScrollOffset = Integer.MIN_VALUE;
        this.mScrollState = 0;
        this.mHasUnsetValue = false;
        this.mSoundEnable = true;
        this.mOldTime = -1;
        this.mLineSpacingMultiplier = DEFAULT_LINE_SPACE;
        this.mItemsVisibleCount = 9;
        this.mScaleX = DEFAULT_TEXT_SCALE;
        this.mTempRect = new Rect();
        this.mSoundRunnable = new Runnable() { // from class: smartisanos.widget.SmartisanNumberPickerEx.1
            @Override // java.lang.Runnable
            public void run() {
                if (SmartisanNumberPickerEx.sPool != null && SmartisanNumberPickerEx.this.mSoundEnable) {
                    SmartisanNumberPickerEx.sPool.play(SmartisanNumberPickerEx.sSoundId, SmartisanNumberPickerEx.VOLUME, SmartisanNumberPickerEx.VOLUME, 0, 0, 1.0f);
                }
            }
        };
        this.mcContext = context;
        DisplayMetrics dm = getResources().getDisplayMetrics();
        float density = dm.density;
        if (density <= 1.0f) {
            this.CENTER_CONTENT_OFFSET = 1.4f;
        } else if (1.0f < density && density < 1.3f) {
            this.CENTER_CONTENT_OFFSET = -2.0f;
        } else if (1.3f < density && density < DEFAULT_LINE_SPACE) {
            this.CENTER_CONTENT_OFFSET = 4.0f;
        } else if (DEFAULT_LINE_SPACE <= density && density < 2.3f) {
            this.CENTER_CONTENT_OFFSET = 7.0f;
        } else if (2.3f <= density && density < 3.0f) {
            this.CENTER_CONTENT_OFFSET = 4.0f;
        } else if (density >= 3.0f) {
            this.CENTER_CONTENT_OFFSET = density * 2.5f;
        }
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
            this.mFlingScroller = new Scroller(getContext(), null, true);
            if (getContext().getResources().getDisplayMetrics().density == 2.5f) {
                this.mFlingScroller.setFriction(0.0028f);
            } else {
                this.mFlingScroller.setFriction(0.0015f);
            }
            Scroller scroller = new Scroller(getContext(), new DecelerateInterpolator(2.5f));
            this.mAdjustScroller = scroller;
            scroller.setFriction(5.0E-4f);
            if (getImportantForAccessibility() == 0) {
                setImportantForAccessibility(1);
            }
            this.mHightlightSuffixFontSize = context.getResources().getDimensionPixelSize(R.dimen.smartisan_rom_numberpicker_highlight_suffix_font_size);
            this.mHightlightSuffixMargin = context.getResources().getDimensionPixelSize(R.dimen.smartisan_rom_numberpicker_highlight_suffix_margin);
            this.mInitialColor = context.getResources().getColor(R.color.smartisan_rom_calendar_date_pick_select_day_color);
            this.mTextSize = (int) (Resources.getSystem().getDisplayMetrics().density * 17.0f);
            this.mPaintOuterText = new Paint();
            this.mPaintCenterText = new Paint();
            this.mPaintIndicator = new Paint();
            this.mSelectorWheelPaint = new Paint();
            initPaints();
            return;
        }
        throw new IllegalArgumentException("minWidth > maxWidth");
    }

    private void initPaints() {
        this.mPaintOuterText.setColor(this.mNormalColor);
        this.mPaintOuterText.setAntiAlias(true);
        this.mPaintOuterText.setFakeBoldText(true);
        this.mPaintOuterText.setTextSize(this.mTextSize);
        this.mPaintCenterText.setColor(this.mHighlightColor);
        this.mPaintCenterText.setAntiAlias(true);
        this.mPaintCenterText.setTextScaleX(this.mScaleX);
        this.mPaintCenterText.setFakeBoldText(true);
        this.mPaintCenterText.setTextSize(this.mTextSize);
        this.mPaintIndicator.setColor(this.mNormalColor);
        this.mPaintIndicator.setAntiAlias(true);
        this.mSelectorWheelPaint.setTextSize(this.mHightlightSuffixFontSize);
        this.mSelectorWheelPaint.setColor(this.mHighlightColor);
        this.mSelectorWheelPaint.setFakeBoldText(true);
        this.mSelectorWheelPaint.setAntiAlias(true);
        this.mSelectorWheelPaint.setTextAlign(Paint.Align.CENTER);
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
        int maxScrollDelta = this.mSelectorElementHeight * 9;
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
                    if (this.mShowSoftInputOnTap) {
                        this.mShowSoftInputOnTap = false;
                        performClick();
                    } else {
                        int selectorIndexOffset = (eventY / this.mSelectorElementHeight) - 4;
                        if (selectorIndexOffset > 0) {
                            changeValueByOne(true);
                        } else if (selectorIndexOffset < 0) {
                            changeValueByOne(false);
                        }
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

    /** See {@link SmartisanNumberPicker#onKeyDown(int, KeyEvent)}. */
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
        if (!this.mWrapSelectorWheel && y > 0 && selectorIndices[4] <= this.mMinValue) {
            this.mCurrentScrollOffset = this.mInitialScrollOffset;
            return;
        }
        if (!this.mWrapSelectorWheel && y < 0 && selectorIndices[4] >= maxValue()) {
            this.mCurrentScrollOffset = this.mInitialScrollOffset;
            return;
        }
        this.mCurrentScrollOffset += y;
        while (true) {
            int i = this.mCurrentScrollOffset;
            int i2 = i - this.mInitialScrollOffset;
            int i3 = this.mSelectorElementHeight;
            if (i2 < i3) {
                break;
            }
            this.mCurrentScrollOffset = i - i3;
            decrementSelectorIndices(selectorIndices);
            setValueInternal(selectorIndices[4], true);
            if (!this.mWrapSelectorWheel && selectorIndices[4] <= this.mMinValue) {
                this.mCurrentScrollOffset = this.mInitialScrollOffset;
            }
        }
        while (true) {
            int i4 = this.mCurrentScrollOffset;
            int i5 = i4 - this.mInitialScrollOffset;
            int i6 = this.mSelectorElementHeight;
            if (i5 <= (-i6)) {
                this.mCurrentScrollOffset = i4 + i6;
                incrementSelectorIndices(selectorIndices);
                setValueInternal(selectorIndices[4], true);
                if (!this.mWrapSelectorWheel && selectorIndices[4] >= maxValue()) {
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

    public void setInitialValue(int value) {
        this.mInitialValue = value;
    }

    public void setHightlightSuffix(String hightlightSuffix) {
        this.mHightlightSuffix = hightlightSuffix;
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
                float digitWidth = this.mPaintCenterText.measureText(formatNumberWithLocale(i));
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
            float textWidth = this.mPaintCenterText.measureText(this.mDisplayedValues[i2]);
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

    public void setTextColor(int normalColor, int highlightColor) {
        this.mNormalColor = normalColor;
        this.mHighlightColor = highlightColor;
    }

    public void setTextSize(int textSize) {
        this.mTextSize = (int) (Resources.getSystem().getDisplayMetrics().density * textSize);
    }

    public void setTextScaleX(int scalex) {
        this.mScaleX = scalex;
    }

    public void setLineSpacemMultiplier(float lineSpacemMultiplier) {
        this.mLineSpacingMultiplier = lineSpacemMultiplier < 1.0f ? DEFAULT_LINE_SPACE : this.mLineSpacingMultiplier;
        initializeSelectorWheel();
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
        if (this.mDisplayedValues == null) {
            this.mMaxWidthText = null;
            return;
        }
        int len = displayedValues.length;
        for (int i = 0; i < len; i++) {
            if (this.mMaxWidthText != null) {
                this.mMaxWidthText = displayedValues[i].length() > this.mMaxWidthText.length() ? displayedValues[i] : this.mMaxWidthText;
            } else {
                this.mMaxWidthText = displayedValues[i];
            }
        }
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
        int[] selectorIndices = this.mSelectorIndices;
        initPaints();
        int j2 = (int) ((this.mCurrentScrollOffset - this.mInitialScrollOffset) % (this.mLineSpacingMultiplier * this.mMaxTextHeight));
        if (this.mDrawableBg != null) {
            canvas.save();
            canvas.translate(TOP_AND_BOTTOM_FADING_EDGE_STRENGTH, this.mFirstLineY);
            this.mDrawableBg.setBounds(0, 0, this.mWidth, this.mSecondLineY - this.mFirstLineY);
            this.mDrawableBg.draw(canvas);
            canvas.restore();
        } else {
            float f = getPaddingLeft();
            int i = this.mFirstLineY;
            canvas.drawLine(f, i, this.mWidth, i, this.mPaintIndicator);
            float f2 = getPaddingLeft();
            int i2 = this.mSecondLineY;
            canvas.drawLine(f2, i2, this.mWidth, i2, this.mPaintIndicator);
        }
        for (int i3 = 0; i3 < this.mItemsVisibleCount; i3++) {
            int selectorIndex = selectorIndices[i3];
            if (looksUnset(selectorIndex)) {
                scrollSelectorValue = UNSET_STRING;
            } else {
                String scrollSelectorValue2 = this.mSelectorIndexToStringCache.get(selectorIndex);
                scrollSelectorValue = scrollSelectorValue2;
            }
            if (selectorIndex == this.mInitialValue) {
                this.mPaintOuterText.setColor(this.mInitialColor);
                this.mPaintCenterText.setColor(this.mInitialColor);
            } else {
                this.mPaintOuterText.setColor(this.mNormalColor);
                this.mPaintCenterText.setColor(this.mHighlightColor);
            }
            canvas.save();
            float itemHeight = this.mMaxTextHeight * this.mLineSpacingMultiplier;
            String scrollSelectorValue3 = scrollSelectorValue;
            double radian = (((double) ((i3 * itemHeight) + j2)) * 3.141592653589793d) / ((double) this.mHalfCircumference);
            if (radian < Math.PI && radian > 0.0d) {
                int translateY = (int) ((((double) this.mRadius) - (Math.cos(radian) * ((double) this.mRadius))) - ((Math.sin(radian) * ((double) this.mMaxTextHeight)) / 2.0d));
                canvas.translate(TOP_AND_BOTTOM_FADING_EDGE_STRENGTH, translateY);
                canvas.scale(1.0f, (float) Math.sin(radian));
                int i4 = this.mMaxTextHeight;
                float baseline = i4 - this.CENTER_CONTENT_OFFSET;
                int i5 = this.mFirstLineY;
                if (translateY > i5 || i4 + translateY < i5) {
                    scrollSelectorValue3 = scrollSelectorValue3;
                    int i6 = this.mSecondLineY;
                    if (translateY <= i6 && this.mMaxTextHeight + translateY >= i6) {
                        canvas.save();
                        canvas.clipRect(0, 0, this.mWidth, this.mSecondLineY - translateY);
                        canvas.drawText(scrollSelectorValue3, getTextX(scrollSelectorValue3, this.mPaintCenterText, this.mTempRect), baseline, this.mPaintCenterText);
                        canvas.restore();
                        canvas.save();
                        canvas.clipRect(0, this.mSecondLineY - translateY, this.mWidth, (int) itemHeight);
                        canvas.drawText(scrollSelectorValue3, getTextX(scrollSelectorValue3, this.mPaintOuterText, this.mTempRect), baseline, this.mPaintOuterText);
                        canvas.restore();
                    } else if (translateY >= this.mFirstLineY && this.mMaxTextHeight + translateY <= this.mSecondLineY) {
                        canvas.clipRect(0, 0, this.mWidth, (int) itemHeight);
                        canvas.drawText(scrollSelectorValue3, getTextX(scrollSelectorValue3, this.mPaintCenterText, this.mTempRect), baseline, this.mPaintCenterText);
                    } else {
                        canvas.clipRect(0, 0, this.mWidth, (int) itemHeight);
                        canvas.drawText(scrollSelectorValue3, getTextX(scrollSelectorValue3, this.mPaintOuterText, this.mTempRect), baseline, this.mPaintOuterText);
                    }
                } else {
                    canvas.save();
                    canvas.clipRect(0, 0, this.mWidth, this.mFirstLineY - translateY);
                    scrollSelectorValue3 = scrollSelectorValue3;
                    canvas.drawText(scrollSelectorValue3, getTextX(scrollSelectorValue3, this.mPaintOuterText, this.mTempRect), baseline, this.mPaintOuterText);
                    canvas.restore();
                    canvas.save();
                    canvas.clipRect(0, this.mFirstLineY - translateY, this.mWidth, (int) itemHeight);
                    canvas.drawText(scrollSelectorValue3, getTextX(scrollSelectorValue3, this.mPaintCenterText, this.mTempRect), baseline, this.mPaintCenterText);
                    canvas.restore();
                }
                canvas.restore();
            } else {
                canvas.restore();
            }
            if (i3 == this.mItemsVisibleCount / 2 && !TextUtils.isEmpty(this.mHightlightSuffix)) {
                String text = scrollSelectorValue3;
                if (this.mFormatter == null) {
                    if (this.mDisplayedValues != null) {
                        String str = this.mMaxWidthText;
                        if (str == null) {
                            str = text;
                        }
                        text = str;
                    } else {
                        text = formatNumberWithLocale(this.mMaxValue);
                    }
                }
                this.mSelectorWheelPaint.getTextBounds(text, 0, text.length(), this.mTempRect);
                int textWidth = (int) (this.mTempRect.width() * this.mScaleX);
                if (Math.abs(textWidth - this.mMidTextWidth) > 10) {
                    this.mMidTextWidth = textWidth;
                }
                int textRight = getPaddingLeft() + (this.mMidTextWidth / 2) + (this.mWidth / 2);
                Paint.FontMetricsInt fontMetrics = this.mSelectorWheelPaint.getFontMetricsInt();
                int i7 = this.mSecondLineY;
                int selectorIndex2 = this.mFirstLineY;
                int centerY = ((i7 - selectorIndex2) / 2) + selectorIndex2;
                float y = (((fontMetrics.bottom - fontMetrics.top) / 2) + centerY) - fontMetrics.bottom;
                canvas.drawText(this.mHightlightSuffix, textRight + this.mTextPadding + this.mHightlightSuffixMargin, y, this.mSelectorWheelPaint);
            }
        }
        if (!isTimeChanged(selectorIndices[2]) || Math.abs(this.mFlingScroller.getFinalY() - this.mFlingScroller.getCurrY()) <= 50) {
            if (isTimeChanged(selectorIndices[2]) && this.mFlingScroller.getFinalY() <= this.mFlingScroller.getCurrY()) {
                this.mOldTime = selectorIndices[2];
                postDelayed(this.mSoundRunnable, 30L);
                return;
            }
            return;
        }
        this.mOldTime = selectorIndices[2];
        postDelayed(this.mSoundRunnable, 10L);
    }

    private int getTextX(String a, Paint paint, Rect rect) {
        paint.getTextBounds(a, 0, a.length(), rect);
        int textWidth = rect.width();
        return (((this.mWidth - getPaddingLeft()) - ((int) (textWidth * this.mScaleX))) / 2)
                + getPaddingLeft();
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
        event.setClassName(SmartisanNumberPickerEx.class.getName());
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
            int min = this.mMinValue;
            int max = this.mMaxValue;
            int i = 0;
            while (true) {
                int[] iArr = this.mSelectorIndices;
                if (i < iArr.length / 2) {
                    selectorIndices[i] = max - (((iArr.length / 2) - i) - 1);
                    ensureCachedScrollSelectorValue(selectorIndices[i]);
                    int j = (this.mSelectorIndices.length / 2) + i + 1;
                    selectorIndices[j] = min + i;
                    ensureCachedScrollSelectorValue(selectorIndices[j]);
                    i++;
                } else {
                    int i2 = iArr.length;
                    int mid = i2 / 2;
                    selectorIndices[mid] = 4;
                    ensureCachedScrollSelectorValue(selectorIndices[mid]);
                    return;
                }
            }
        } else {
            for (int i3 = 0; i3 < this.mSelectorIndices.length; i3++) {
                int selectorIndex = (i3 - 4) + current;
                if (this.mWrapSelectorWheel) {
                    selectorIndex = getWrappedSelectorIndex(selectorIndex);
                }
                selectorIndices[i3] = selectorIndex;
                ensureCachedScrollSelectorValue(selectorIndices[i3]);
            }
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
        if (!SmartisanLocaleUtils.isExternalDisplay(getContext())) {
            performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
        }
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
        int i = mDefaultItemSize;
        int i2 = (i / 2) + 6;
        this.mInitialScrollOffset = i2;
        this.mMiddleScrollOffset = (i * 4) + i2;
        this.mCurrentScrollOffset = i2;
        this.mTextPadding = getResources().getDimensionPixelOffset(R.dimen.smartisan_rom_number_picker_text_padding);
        this.mWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        this.mHeight = measuredHeight;
        int i3 = this.mWidth;
        if (i3 == 0 || measuredHeight == 0) {
            return;
        }
        this.mWidth = i3 - getPaddingRight();
        int i4 = this.mHeight;
        int i5 = (int) ((((double) i4) * 3.141592653589793d) / 2.0d);
        this.mHalfCircumference = i5;
        float f = this.mLineSpacingMultiplier;
        int i6 = (int) (i5 / ((this.mItemsVisibleCount - 1) * f));
        this.mMaxTextHeight = i6;
        this.mRadius = i4 / 2;
        this.mFirstLineY = (int) ((i4 - (i6 * f)) / DEFAULT_LINE_SPACE);
        this.mSecondLineY = (int) ((i4 + (i6 * f)) / DEFAULT_LINE_SPACE);
        this.mSelectorElementHeight = (int) ((f * i6) + 0.5f);
        this.mDrawableBg = getContext().getDrawable(R.drawable.smartisan_rom_time_picker_widget_lens);
    }

    private void initializeFadingEdges() {
        setVerticalFadingEdgeEnabled(true);
        setFadingEdgeLength(((getBottom() - getTop()) - 146) / 2);
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
                return createAccessibilityNodeInfoForNumberPicker(SmartisanNumberPickerEx.this.getScrollX(), SmartisanNumberPickerEx.this.getScrollY(), SmartisanNumberPickerEx.this.getScrollX() + (SmartisanNumberPickerEx.this.getWidth()), SmartisanNumberPickerEx.this.getScrollY() + (SmartisanNumberPickerEx.this.getHeight()));
            }
            if (virtualViewId == 1) {
                return createAccessibilityNodeInfoForVirtualButton(1, getSecondVirtualDecrementButtonText(), SmartisanNumberPickerEx.this.getScrollX(), SmartisanNumberPickerEx.this.getScrollY(), SmartisanNumberPickerEx.this.getScrollX() + (SmartisanNumberPickerEx.this.getWidth()), mTopSelectionDividerTop - SmartisanNumberPickerEx.this.mSelectorElementHeight);
            }
            if (virtualViewId == 2) {
                return createAccessibilityNodeInfoForVirtualButton(2, getVirtualDecrementButtonText(), SmartisanNumberPickerEx.this.getScrollX(), SmartisanNumberPickerEx.this.getScrollY() + SmartisanNumberPickerEx.this.mSelectorElementHeight, SmartisanNumberPickerEx.this.getScrollX() + (SmartisanNumberPickerEx.this.getWidth()), mTopSelectionDividerTop);
            }
            if (virtualViewId == 3) {
                return createAccessibilityNodeInfoForVirtualButton(3, getCurrentValueVirtualButtonText(), SmartisanNumberPickerEx.this.getScrollX(), mTopSelectionDividerTop, SmartisanNumberPickerEx.this.getScrollX() + (SmartisanNumberPickerEx.this.getWidth()), SmartisanNumberPickerEx.this.mBottomSelectionDividerBottom);
            }
            if (virtualViewId == 4) {
                return createAccessibilityNodeInfoForVirtualButton(4, getVirtualIncrementButtonText(), SmartisanNumberPickerEx.this.getScrollX(), SmartisanNumberPickerEx.this.mBottomSelectionDividerBottom, SmartisanNumberPickerEx.this.getScrollX() + (SmartisanNumberPickerEx.this.getWidth()), (SmartisanNumberPickerEx.this.getScrollY() + (SmartisanNumberPickerEx.this.getHeight())) - SmartisanNumberPickerEx.this.mSelectorElementHeight);
            }
            if (virtualViewId == 5) {
                return createAccessibilityNodeInfoForVirtualButton(5, getSecondVirtualIncrementButtonText(), SmartisanNumberPickerEx.this.getScrollX(), SmartisanNumberPickerEx.this.mBottomSelectionDividerBottom + SmartisanNumberPickerEx.this.mSelectorElementHeight, SmartisanNumberPickerEx.this.getScrollX() + (SmartisanNumberPickerEx.this.getWidth()), SmartisanNumberPickerEx.this.getScrollY() + (SmartisanNumberPickerEx.this.getHeight()));
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
                        if (!SmartisanNumberPickerEx.this.isEnabled()) {
                            return false;
                        }
                        SmartisanNumberPickerEx.this.changeValueByOne(false);
                        sendAccessibilityEventForVirtualView(virtualViewId, 1);
                        return true;
                    }
                    if (action == 64) {
                        if (this.mAccessibilityFocusedView == virtualViewId) {
                            return false;
                        }
                        this.mAccessibilityFocusedView = virtualViewId;
                        sendAccessibilityEventForVirtualView(virtualViewId, 32768);
                        SmartisanNumberPickerEx smartisanNumberPickerEx = SmartisanNumberPickerEx.this;
                        smartisanNumberPickerEx.invalidate(0, 0, smartisanNumberPickerEx.getWidth(), mTopSelectionDividerTop);
                        return true;
                    }
                    if (action != 128 || this.mAccessibilityFocusedView != virtualViewId) {
                        return false;
                    }
                    this.mAccessibilityFocusedView = Integer.MIN_VALUE;
                    sendAccessibilityEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED);
                    SmartisanNumberPickerEx smartisanNumberPickerEx2 = SmartisanNumberPickerEx.this;
                    smartisanNumberPickerEx2.invalidate(0, 0, smartisanNumberPickerEx2.getWidth(), mTopSelectionDividerTop);
                    return true;
                }
                if (virtualViewId == 3) {
                    if (action == 16) {
                        if (!SmartisanNumberPickerEx.this.isEnabled()) {
                            return false;
                        }
                        SmartisanNumberPickerEx.this.performClick();
                        sendAccessibilityEventForVirtualView(virtualViewId, 1);
                        return true;
                    }
                    if (action == 64) {
                        if (this.mAccessibilityFocusedView == virtualViewId) {
                            return false;
                        }
                        this.mAccessibilityFocusedView = virtualViewId;
                        sendAccessibilityEventForVirtualView(virtualViewId, 32768);
                        SmartisanNumberPickerEx smartisanNumberPickerEx3 = SmartisanNumberPickerEx.this;
                        smartisanNumberPickerEx3.invalidate(0, smartisanNumberPickerEx3.mTopSelectionDividerTop, SmartisanNumberPickerEx.this.getWidth(), SmartisanNumberPickerEx.this.mBottomSelectionDividerBottom);
                        return true;
                    }
                    if (action != 128 || this.mAccessibilityFocusedView != virtualViewId) {
                        return false;
                    }
                    this.mAccessibilityFocusedView = Integer.MIN_VALUE;
                    sendAccessibilityEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED);
                    SmartisanNumberPickerEx smartisanNumberPickerEx4 = SmartisanNumberPickerEx.this;
                    smartisanNumberPickerEx4.invalidate(0, smartisanNumberPickerEx4.mTopSelectionDividerTop, SmartisanNumberPickerEx.this.getWidth(), SmartisanNumberPickerEx.this.mBottomSelectionDividerBottom);
                    return true;
                }
                if (virtualViewId == 4 || virtualViewId == 5) {
                    if (action == 16) {
                        if (!SmartisanNumberPickerEx.this.isEnabled()) {
                            return false;
                        }
                        SmartisanNumberPickerEx.this.changeValueByOne(true);
                        sendAccessibilityEventForVirtualView(virtualViewId, 1);
                        return true;
                    }
                    if (action == 64) {
                        if (this.mAccessibilityFocusedView == virtualViewId) {
                            return false;
                        }
                        this.mAccessibilityFocusedView = virtualViewId;
                        sendAccessibilityEventForVirtualView(virtualViewId, 32768);
                        SmartisanNumberPickerEx smartisanNumberPickerEx5 = SmartisanNumberPickerEx.this;
                        smartisanNumberPickerEx5.invalidate(0, smartisanNumberPickerEx5.mBottomSelectionDividerBottom, SmartisanNumberPickerEx.this.getWidth(), SmartisanNumberPickerEx.this.getHeight());
                        return true;
                    }
                    if (action != 128 || this.mAccessibilityFocusedView != virtualViewId) {
                        return false;
                    }
                    this.mAccessibilityFocusedView = Integer.MIN_VALUE;
                    sendAccessibilityEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED);
                    SmartisanNumberPickerEx smartisanNumberPickerEx6 = SmartisanNumberPickerEx.this;
                    smartisanNumberPickerEx6.invalidate(0, smartisanNumberPickerEx6.mBottomSelectionDividerBottom, SmartisanNumberPickerEx.this.getWidth(), SmartisanNumberPickerEx.this.getHeight());
                    return true;
                }
            } else {
                if (action == 64) {
                    if (this.mAccessibilityFocusedView == virtualViewId) {
                        return false;
                    }
                    if (SmartisanNumberPickerEx.this.performAccessibilityAction(
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
                    if (SmartisanNumberPickerEx.this.performAccessibilityAction(
                            AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS, arguments)) {
                        this.mAccessibilityFocusedView = Integer.MIN_VALUE;
                        return true;
                    }
                    return false;
                }
                if (action == 4096) {
                    if (!SmartisanNumberPickerEx.this.isEnabled() || (!SmartisanNumberPickerEx.this.getWrapSelectorWheel() && SmartisanNumberPickerEx.this.getValue() >= SmartisanNumberPickerEx.this.getMaxValue())) {
                        return false;
                    }
                    SmartisanNumberPickerEx.this.changeValueByOne(true);
                    return true;
                }
                if (action == 8192) {
                    if (!SmartisanNumberPickerEx.this.isEnabled() || (!SmartisanNumberPickerEx.this.getWrapSelectorWheel() && SmartisanNumberPickerEx.this.getValue() <= SmartisanNumberPickerEx.this.getMinValue())) {
                        return false;
                    }
                    SmartisanNumberPickerEx.this.changeValueByOne(false);
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
            if (SmartisanNumberPickerEx.this.isAccessibilityEnabled()) {
                AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
                event.setClassName(Button.class.getName());
                event.setPackageName(SmartisanNumberPickerEx.this.getContext().getPackageName());
                event.getText().add(text);
                event.setEnabled(SmartisanNumberPickerEx.this.isEnabled());
                event.setSource(SmartisanNumberPickerEx.this, virtualViewId);
                SmartisanNumberPickerEx smartisanNumberPickerEx = SmartisanNumberPickerEx.this;
                smartisanNumberPickerEx.requestSendAccessibilityEvent(smartisanNumberPickerEx, event);
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
            char c;
            boolean clickable = virtualViewId != 3;
            AccessibilityNodeInfo info = AccessibilityNodeInfo.obtain();
            info.setClassName((clickable ? Button.class : TextView.class).getName());
            info.setPackageName(SmartisanNumberPickerEx.this.getContext().getPackageName());
            info.setSource(SmartisanNumberPickerEx.this, virtualViewId);
            info.setParent(SmartisanNumberPickerEx.this);
            info.setText(text);
            info.setClickable(clickable);
            info.setLongClickable(clickable);
            info.setEnabled(SmartisanNumberPickerEx.this.isEnabled());
            info.setAccessibilityFocused(this.mAccessibilityFocusedView == virtualViewId);
            Rect boundsInParent = this.mTempRect;
            boundsInParent.set(left, top, right, bottom);
            info.setVisibleToUser(isVisibleToUser(boundsInParent));
            info.setBoundsInParent(boundsInParent);
            int[] locationOnScreen = this.mTempArray;
            SmartisanNumberPickerEx.this.getLocationOnScreen(locationOnScreen);
            c = 1;
            boundsInParent.offset(locationOnScreen[0], locationOnScreen[c]);
            info.setBoundsInScreen(boundsInParent);
            if (this.mAccessibilityFocusedView != virtualViewId) {
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_ACCESSIBILITY_FOCUS);
            }
            if (this.mAccessibilityFocusedView == virtualViewId) {
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
            }
            if (SmartisanNumberPickerEx.this.isEnabled()) {
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK);
            }
            return info;
        }

        private AccessibilityNodeInfo createAccessibilityNodeInfoForNumberPicker(int left, int top, int right, int bottom) {
            AccessibilityNodeInfo info = AccessibilityNodeInfo.obtain();
            info.setClassName(SmartisanNumberPickerEx.class.getName());
            info.setPackageName(SmartisanNumberPickerEx.this.getContext().getPackageName());
            info.setSource(SmartisanNumberPickerEx.this);
            if (hasSecondVirtualDecrementButton()) {
                info.addChild(SmartisanNumberPickerEx.this, 1);
            }
            if (hasVirtualDecrementButton()) {
                info.addChild(SmartisanNumberPickerEx.this, 2);
            }
            info.addChild(SmartisanNumberPickerEx.this, 3);
            if (hasVirtualIncrementButton()) {
                info.addChild(SmartisanNumberPickerEx.this, 4);
            }
            if (hasSecondVirtualIncrementButton()) {
                info.addChild(SmartisanNumberPickerEx.this, 5);
            }
            info.setParent((View) SmartisanNumberPickerEx.this.getParentForAccessibility());
            info.setEnabled(SmartisanNumberPickerEx.this.isEnabled());
            info.setScrollable(true);
            info.setAccessibilityFocused(this.mAccessibilityFocusedView == -1);

            Rect boundsInParent = this.mTempRect;
            boundsInParent.set(left, top, right, bottom);

            info.setBoundsInParent(boundsInParent);
            info.setVisibleToUser(isVisibleToUser(boundsInParent));
            int[] locationOnScreen = this.mTempArray;
            SmartisanNumberPickerEx.this.getLocationOnScreen(locationOnScreen);
            boundsInParent.offset(locationOnScreen[0], locationOnScreen[1]);

            info.setBoundsInScreen(boundsInParent);
            if (this.mAccessibilityFocusedView != -1) {
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_ACCESSIBILITY_FOCUS);
            }
            if (this.mAccessibilityFocusedView == -1) {
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
            }
            if (SmartisanNumberPickerEx.this.isEnabled()) {
                if (SmartisanNumberPickerEx.this.getWrapSelectorWheel() || SmartisanNumberPickerEx.this.getValue() < SmartisanNumberPickerEx.this.getMaxValue()) {
                    info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);
                }
                if (SmartisanNumberPickerEx.this.getWrapSelectorWheel() || SmartisanNumberPickerEx.this.getValue() > SmartisanNumberPickerEx.this.getMinValue()) {
                    info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);
                }
            }
            return info;
        }

        private boolean isVisibleToUser(Rect localBounds) {
            if (!SmartisanNumberPickerEx.this.isShown() || localBounds.isEmpty()) {
                return false;
            }
            Rect visibleBounds = new Rect();
            return SmartisanNumberPickerEx.this.getLocalVisibleRect(visibleBounds)
                    && Rect.intersects(localBounds, visibleBounds);
        }

        private boolean hasVirtualDecrementButton() {
            return SmartisanNumberPickerEx.this.getWrapSelectorWheel() || SmartisanNumberPickerEx.this.getValue() > SmartisanNumberPickerEx.this.getMinValue();
        }

        private boolean hasVirtualIncrementButton() {
            return SmartisanNumberPickerEx.this.getWrapSelectorWheel() || SmartisanNumberPickerEx.this.getValue() < SmartisanNumberPickerEx.this.getMaxValue();
        }

        private boolean hasSecondVirtualIncrementButton() {
            return SmartisanNumberPickerEx.this.getWrapSelectorWheel() || SmartisanNumberPickerEx.this.getValue() + 1 < SmartisanNumberPickerEx.this.getMaxValue();
        }

        private boolean hasSecondVirtualDecrementButton() {
            return SmartisanNumberPickerEx.this.getWrapSelectorWheel() || SmartisanNumberPickerEx.this.getValue() - 1 > SmartisanNumberPickerEx.this.getMinValue();
        }

        private String getVirtualDecrementButtonText() {
            return getVirtualButtonTextAt(SmartisanNumberPickerEx.this.mValue - 1);
        }

        private String getVirtualIncrementButtonText() {
            return getVirtualButtonTextAt(SmartisanNumberPickerEx.this.mValue + 1);
        }

        private String getSecondVirtualDecrementButtonText() {
            return getVirtualButtonTextAt(SmartisanNumberPickerEx.this.mValue - 2);
        }

        private String getSecondVirtualIncrementButtonText() {
            return getVirtualButtonTextAt(SmartisanNumberPickerEx.this.mValue + 2);
        }

        private String getCurrentValueVirtualButtonText() {
            return getVirtualButtonTextAt(SmartisanNumberPickerEx.this.mValue);
        }

        private String getVirtualButtonTextAt(int value) {
            if (SmartisanNumberPickerEx.this.mWrapSelectorWheel) {
                value = SmartisanNumberPickerEx.this.getWrappedSelectorIndex(value);
            }
            if (value > SmartisanNumberPickerEx.this.mMaxValue || value < SmartisanNumberPickerEx.this.mMinValue) {
                return null;
            }
            return SmartisanNumberPickerEx.this.mDisplayedValues == null ? SmartisanNumberPickerEx.this.formatNumber(value) : SmartisanNumberPickerEx.this.mDisplayedValues[value - SmartisanNumberPickerEx.this.mMinValue];
        }
    }
}
