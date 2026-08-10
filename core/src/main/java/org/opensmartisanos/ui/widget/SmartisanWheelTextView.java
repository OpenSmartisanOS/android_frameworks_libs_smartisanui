/* Ported from Smartisan OS 8.5.3 R2 smartisanos.widget.SmartisanWheelTextView. */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;
import android.widget.Spinner;

import org.opensmartisanos.ui.R;

import java.util.List;

/** Self-drawn single-line or vertically rotating text used by SmartisanSpinnerView. */
public class SmartisanWheelTextView extends View {
    private static final String END_TEXT = "...";
    private static final float MAX_FONT_SCALE_WITH_SUBTITLE = 1.1f;
    private static final int SELECTOR_ADJUSTMENT_DURATION_MILLIS = 150;
    private static int maxNoActionOffset;
    private static int minActionOffset;

    /** Listener matching the original wheel value contract. */
    public interface OnValueChangeListener {
        void onValueChange(SmartisanWheelTextView view, int oldValue, int newValue);
    }

    private final Scroller adjustScroller;
    private int clickTimeout;
    private float contentPaddingTop;
    private int currentScrollOffset;
    private String[] displayedValues;
    private float downX;
    private float downY;
    private int drawSubtitleLength;
    private int drawTitleLength;
    @SuppressWarnings("unused") private int hostWidth;
    private boolean ignoreMoveEvents;
    private int initialScrollOffset = Integer.MIN_VALUE;
    private boolean needRotate;
    private float lastDownOrMoveEventY;
    @SuppressWarnings("unused") private int leftElementsWidth;
    @SuppressWarnings("unused") private View leftIconView;
    @SuppressWarnings("unused") private View leftView;
    private int maximumAvailableWidth;
    private float maximumSubtitleSize;
    private float maximumTitleSize = Integer.MAX_VALUE;
    private float maximumTitleSizeWithSubtitle;
    private OnValueChangeListener valueChangeListener;
    @SuppressWarnings("unused") private int paddingLeftOffset;
    private Paint titlePaint;
    private int previousScrollerY;
    @SuppressWarnings("unused") private int rightElementsWidth;
    @SuppressWarnings("unused") private View rightIconView;
    @SuppressWarnings("unused") private List<SmartisanButton> rightViewList;
    private int selectorElementHeight;
    private int[] selectorIndices;
    private String subContent;
    private Paint subtitlePaint;
    private float subtitleSize;
    private int textColor;
    @SuppressWarnings("unused") private boolean titleAlignCenter = true;
    private float titleSize;
    private int touchSlop;
    private int value;

    public SmartisanWheelTextView(Context context) {
        this(context, null);
    }

    public SmartisanWheelTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0, R.style.Widget_SmartisanUi_WheelTextView);
    }

    public SmartisanWheelTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs,
                isStyleResource(context, defStyleAttr) ? 0 : defStyleAttr,
                isStyleResource(context, defStyleAttr)
                        ? defStyleAttr : R.style.Widget_SmartisanUi_WheelTextView);
    }

    private SmartisanWheelTextView(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray attributes = context.obtainStyledAttributes(attrs,
                R.styleable.SmartisanWheelTextView, defStyleAttr, defStyleRes);
        textColor = attributes.getColor(
                R.styleable.SmartisanWheelTextView_android_textColor,
                getResources().getColor(R.color.smartisan_rom_spinner_title_text));
        titleSize = attributes.getDimensionPixelSize(
                R.styleable.SmartisanWheelTextView_android_textSize,
                getResources().getDimensionPixelSize(
                        R.dimen.smartisan_rom_spinner_title_text_size));
        attributes.recycle();

        maxNoActionOffset = (int) dpToPixels(6.6666665f);
        minActionOffset = (int) dpToPixels(16.666666f);
        initializeMaximumSizes();
        setWillNotDraw(false);
        setFocusable(true);
        initializeTitlePaint();
        initializeSubtitlePaint();
        contentPaddingTop = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6f,
                getResources().getDisplayMetrics());
        adjustScroller = new Scroller(getContext(), new DecelerateInterpolator(1f));
        clickTimeout = ViewConfiguration.getPressedStateDuration()
                + ViewConfiguration.getTapTimeout();
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    private static boolean isStyleResource(Context context, int resource) {
        if (resource == 0) return false;
        try {
            return "style".equals(context.getResources().getResourceTypeName(resource));
        } catch (Resources.NotFoundException ignored) {
            return false;
        }
    }

    private void initializeMaximumSizes() {
        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        maximumTitleSizeWithSubtitle = (resources.getDimensionPixelSize(
                R.dimen.smartisan_rom_spinner_title_text_size) / configuration.fontScale)
                * MAX_FONT_SCALE_WITH_SUBTITLE;
        maximumSubtitleSize = (resources.getDimensionPixelSize(
                R.dimen.smartisan_rom_spinner_subtitle_text_size) / configuration.fontScale)
                * MAX_FONT_SCALE_WITH_SUBTITLE;
    }

    private void initializeTitlePaint() {
        titlePaint = new Paint(33);
        titlePaint.setAntiAlias(true);
        titlePaint.setTextSize(titleSize);
        titlePaint.setColor(textColor);
    }

    private void initializeSubtitlePaint() {
        subtitlePaint = new Paint();
        subtitlePaint.setAntiAlias(true);
        subtitleSize = getResources().getDimensionPixelSize(
                R.dimen.smartisan_rom_spinner_subtitle_text_size);
        subtitlePaint.setTextSize(subtitleSize);
        subtitlePaint.setColor(getResources().getColor(
                R.color.smartisan_rom_spinner_subtitle_text));
    }

    public void setSubTextSize(float size) {
        subtitleSize = size;
        subtitlePaint.setTextSize(size);
        requestLayout();
        invalidate();
    }

    void setTitleColor(int colorValue) {
        titlePaint.setColor(colorValue);
    }

    void setSubtitleColor(int colorValue) {
        subtitlePaint.setColor(colorValue);
    }

    @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) initializeSelectorWheel();
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int contentWidth = handleTextByWidth();
        int desiredWidth = Math.max(getSuggestedMinimumWidth(),
                contentWidth + getPaddingLeft() + getPaddingRight());
        int desiredHeight = Math.max(getSuggestedMinimumHeight(),
                getDesiredTextHeight() + getPaddingTop() + getPaddingBottom());
        int width = resolveSizeAndState(desiredWidth, widthMeasureSpec, 0);
        int height = resolveSizeAndState(desiredHeight, heightMeasureSpec, 0);
        setMeasuredDimension(width, height);
        initializeSelectorWheel();
    }

    private int getDesiredTextHeight() {
        titlePaint.setTextSize(TextUtils.isEmpty(subContent)
                ? Math.min(titleSize, maximumTitleSize)
                : Math.min(titleSize, maximumTitleSizeWithSubtitle));
        Paint.FontMetricsInt titleMetrics = titlePaint.getFontMetricsInt();
        int titleHeight = titleMetrics.bottom - titleMetrics.top;
        if (TextUtils.isEmpty(subContent)) return titleHeight;
        subtitlePaint.setTextSize(Math.min(subtitleSize, maximumSubtitleSize));
        Paint.FontMetricsInt subtitleMetrics = subtitlePaint.getFontMetricsInt();
        int subtitleHeight = subtitleMetrics.bottom - subtitleMetrics.top;
        return Math.round(contentPaddingTop) + titleHeight + subtitleHeight;
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || !supportsRotation()) return super.onTouchEvent(event);
        int action = event.getActionMasked();
        float deltaX = Math.abs(event.getX() - downX);
        float deltaY = Math.abs(event.getY() - downY);
        if (action == MotionEvent.ACTION_DOWN) {
            downX = event.getX();
            downY = event.getY();
            lastDownOrMoveEventY = downY;
            ignoreMoveEvents = false;
            getParent().requestDisallowInterceptTouchEvent(true);
            if (!adjustScroller.isFinished()) adjustScroller.forceFinished(true);
        } else if (action == MotionEvent.ACTION_UP) {
            long time = event.getEventTime() - event.getDownTime();
            if (needRotate) ensureScrollWheelAdjusted();
            if (deltaY < touchSlop && deltaX < touchSlop && time < clickTimeout) performClick();
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (!ignoreMoveEvents) {
                float currentY = event.getY();
                int movement = (int) (currentY - lastDownOrMoveEventY);
                if (needRotate) {
                    scrollBy(0, movement);
                    invalidate();
                }
                lastDownOrMoveEventY = currentY;
            }
        } else if (action == MotionEvent.ACTION_CANCEL) {
            if (needRotate) ensureScrollWheelAdjusted();
            if (deltaY < touchSlop) performClick();
        }
        return true;
    }

    @Override public boolean performClick() {
        return super.performClick();
    }

    @Override public void computeScroll() {
        if (adjustScroller.isFinished()) return;
        adjustScroller.computeScrollOffset();
        int currentY = adjustScroller.getCurrY();
        if (previousScrollerY == Integer.MAX_VALUE) {
            previousScrollerY = adjustScroller.getStartY();
        }
        scrollBy(0, currentY - previousScrollerY);
        previousScrollerY = currentY;
        if (!adjustScroller.isFinished()) invalidate();
    }

    private float dpToPixels(float dp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
    }

    @Override public void scrollBy(int x, int y) {
        if (y > maxNoActionOffset) y = maxNoActionOffset;
        int[] indices = selectorIndices;
        currentScrollOffset += y;
        if (Math.abs(currentScrollOffset - initialScrollOffset) <= maxNoActionOffset) return;
        if (currentScrollOffset - initialScrollOffset > minActionOffset && !ignoreMoveEvents) {
            currentScrollOffset -= selectorElementHeight;
            decrementSelectorIndices(indices);
            setValueInternal(value - 1, true);
            ensureScrollWheelAdjusted();
            ignoreMoveEvents = true;
        } else if (currentScrollOffset - initialScrollOffset < -minActionOffset
                && !ignoreMoveEvents) {
            currentScrollOffset += selectorElementHeight;
            incrementSelectorIndices(indices);
            setValueInternal(value + 1, true);
            ensureScrollWheelAdjusted();
            ignoreMoveEvents = true;
        }
    }

    public void setAvailWidth(int maximum) {
        maximumAvailableWidth = Math.max(getMinimumWidth(), maximum);
        requestLayout();
    }

    public void setOnValueChangedListener(OnValueChangeListener listener) {
        valueChangeListener = listener;
    }

    public void setValue(int newValue) {
        setValueInternal(newValue, false);
    }

    public int getValue() {
        return value;
    }

    public void setDisplayedValues(String... values) {
        if (values == null || values.length == 0 || values.equals(displayedValues)) return;
        displayedValues = values;
        value = Math.floorMod(value, displayedValues.length);
        initializeSelectorWheelIndices();
        updateAccessibilityDescription();
        requestLayout();
    }

    public void setSubContentText(String text) {
        subContent = text;
        updateAccessibilityDescription();
        requestLayout();
    }

    @Override protected void onDraw(Canvas canvas) {
        if (displayedValues == null || displayedValues.length == 0) return;
        if (displayedValues.length == 1 || !needRotate) {
            drawSingleLineText(canvas, displayedValues[0]);
            return;
        }
        if (selectorIndices == null) return;
        titlePaint.setTextAlign(Paint.Align.CENTER);
        int y = currentScrollOffset;
        int centerX = getPaddingLeft()
                + (getWidth() - getPaddingLeft() - getPaddingRight()) / 2;
        for (int selectorIndex : selectorIndices) {
            int index = selectorIndex < 0
                    ? Math.abs(displayedValues.length + selectorIndex) % displayedValues.length
                    : selectorIndex % displayedValues.length;
            String text = displayedValues[index];
            if (drawTitleLength != 0 && drawTitleLength < text.length()) {
                text = text.substring(0, Math.max(0, drawTitleLength)) + END_TEXT;
            }
            canvas.drawText(text, centerX, y, titlePaint);
            y += selectorElementHeight;
        }
    }

    private void drawSingleLineText(Canvas canvas, String text) {
        if (TextUtils.isEmpty(text) && TextUtils.isEmpty(subContent)) return;
        titlePaint.setTextSize(TextUtils.isEmpty(subContent)
                ? Math.min(titleSize, maximumTitleSize)
                : Math.min(titleSize, maximumTitleSizeWithSubtitle));
        subtitlePaint.setTextSize(TextUtils.isEmpty(subContent)
                ? subtitleSize : Math.min(subtitleSize, maximumSubtitleSize));
        float titleBaseline = 0f;
        float subtitleBaseline = 0f;
        Paint.FontMetricsInt titleMetrics = titlePaint.getFontMetricsInt();
        int contentHeight = Math.max(0,
                getMeasuredHeight() - getPaddingTop() - getPaddingBottom());
        if (!TextUtils.isEmpty(text)) {
            titleBaseline = getPaddingTop()
                    + (contentHeight - (titleMetrics.bottom + titleMetrics.top)) / 2f;
        }
        if (!TextUtils.isEmpty(subContent)) {
            Paint.FontMetricsInt subtitleMetrics = subtitlePaint.getFontMetricsInt();
            if (!TextUtils.isEmpty(text)) {
                titleBaseline = getPaddingTop() + contentPaddingTop - titleMetrics.top;
                subtitleBaseline = titleBaseline + (subtitleMetrics.bottom - subtitleMetrics.top);
            } else {
                subtitleBaseline = getPaddingTop()
                        + (contentHeight - subtitleMetrics.bottom - subtitleMetrics.top) / 2f;
            }
        }
        int centerX = getPaddingLeft()
                + (getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) / 2;
        if (!TextUtils.isEmpty(text)) {
            if (drawTitleLength > 0 && drawTitleLength < text.length()) {
                text = text.substring(0, Math.max(0, drawTitleLength - 1)) + END_TEXT;
            }
            titlePaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(text, centerX, titleBaseline, titlePaint);
        }
        if (!TextUtils.isEmpty(subContent)) {
            String subtitle = drawSubtitleLength > 0 && drawSubtitleLength < subContent.length()
                    ? subContent.substring(0, drawSubtitleLength - 1) + END_TEXT : subContent;
            subtitlePaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(subtitle, centerX, subtitleBaseline, subtitlePaint);
        }
    }

    @Override public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(SmartisanWheelTextView.class.getName());
        event.setScrollable(true);
        event.setScrollY(value * selectorElementHeight);
    }

    @Override public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(Spinner.class.getName());
        boolean scrollable = supportsRotation() && displayedValues.length > 1;
        info.setScrollable(scrollable);
        if (scrollable) {
            info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);
            info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);
            info.setRangeInfo(AccessibilityNodeInfo.RangeInfo.obtain(
                    AccessibilityNodeInfo.RangeInfo.RANGE_TYPE_INT,
                    0, displayedValues.length - 1, value));
        }
        if (TextUtils.isEmpty(info.getContentDescription())) {
            info.setContentDescription(getAccessibilityText());
        }
    }

    @Override public boolean performAccessibilityAction(int action, Bundle arguments) {
        if (isEnabled() && supportsRotation() && displayedValues.length > 1) {
            if (action == AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) {
                setValueInternal(value + 1, true);
                return true;
            }
            if (action == AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) {
                setValueInternal(value - 1, true);
                return true;
            }
        }
        return super.performAccessibilityAction(action, arguments);
    }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isEnabled() && supportsRotation() && displayedValues.length > 1) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                setValueInternal(value + 1, true);
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                setValueInternal(value - 1, true);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initializeSelectorWheelIndices() {
        if (!supportsRotation()) return;
        selectorIndices = new int[displayedValues.length];
        int current = getValue();
        for (int i = 0; i < selectorIndices.length; i++) {
            selectorIndices[i] = i - displayedValues.length / 2 + current;
        }
    }

    @Override public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        super.onPopulateAccessibilityEvent(event);
        if (displayedValues != null && displayedValues.length > value) {
            event.getText().add(displayedValues[value]);
        }
        if (!TextUtils.isEmpty(subContent)) event.getText().add(subContent);
    }

    private void setValueInternal(int current, boolean notifyChange) {
        if (displayedValues == null || displayedValues.length == 0) {
            value = Math.max(0, current);
            return;
        }
        if (value == current) return;
        if (current < 0) current = Math.abs(displayedValues.length + current);
        current %= displayedValues.length;
        int previous = value;
        value = current;
        if (notifyChange) notifyChange(previous, current);
        initializeSelectorWheelIndices();
        updateAccessibilityDescription();
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
        invalidate();
    }

    private void initializeSelectorWheel() {
        if (!supportsRotation()) return;
        initializeSelectorWheelIndices();
        getTitleHeight(displayedValues[0]);
        Paint.FontMetricsInt metrics = titlePaint.getFontMetricsInt();
        int contentHeight = Math.max(1,
                getMeasuredHeight() - getPaddingTop() - getPaddingBottom());
        float baseline = getPaddingTop()
                + (contentHeight - (metrics.bottom + metrics.top)) / 2f;
        selectorElementHeight = contentHeight;
        int defaultVisibleIndex = displayedValues.length / 2;
        initialScrollOffset = (int) baseline - selectorElementHeight * defaultVisibleIndex;
        currentScrollOffset = initialScrollOffset;
    }

    private void incrementSelectorIndices(int[] indices) {
        for (int i = 0; i < indices.length - 1; i++) indices[i]++;
    }

    private void decrementSelectorIndices(int[] indices) {
        for (int i = indices.length - 1; i > 0; i--) indices[i]--;
    }

    private void notifyChange(int previous, int current) {
        if (valueChangeListener != null) {
            valueChangeListener.onValueChange(this, previous, value);
        }
    }

    private boolean ensureScrollWheelAdjusted() {
        int deltaY = initialScrollOffset - currentScrollOffset;
        if (deltaY == 0) return false;
        previousScrollerY = Integer.MAX_VALUE;
        adjustScroller.startScroll(0, currentScrollOffset, 0, deltaY,
                SELECTOR_ADJUSTMENT_DURATION_MILLIS);
        invalidate();
        return true;
    }

    public void setTextSize(float normalSize) {
        if (normalSize < 0f) throw new IllegalArgumentException("the text size mus be >= 0 ");
        if (titleSize != normalSize) {
            titleSize = normalSize;
            titlePaint.setTextSize(normalSize);
            requestLayout();
            invalidate();
        }
    }

    public void setTextMaxSize(float maximumSize) {
        maximumTitleSize = maximumSize;
        requestLayout();
    }

    public void setTextColor(int normalColor) {
        if (textColor != normalColor) {
            textColor = normalColor;
            titlePaint.setColor(normalColor);
            invalidate();
        }
    }

    public void setIsNeedRotate(boolean rotate) {
        if (needRotate != rotate) {
            needRotate = rotate;
            initializeSelectorWheelIndices();
            requestLayout();
            invalidate();
        }
    }

    private int availableDrawTitleLength(String text, int maximumWidth) {
        int textWidth = 0;
        int endWidth = (int) getTitleWidth(END_TEXT);
        int availableLength = 0;
        for (int i = 0; i < text.length(); i++) {
            textWidth = (int) (textWidth + getTitleWidth(String.valueOf(text.charAt(i))));
            if (textWidth + endWidth > maximumWidth) break;
            availableLength = i + 1;
        }
        return availableLength;
    }

    private int handleTextByWidth() {
        String longestTitle = null;
        if (displayedValues != null && displayedValues.length > 0) {
            longestTitle = displayedValues[displayMaximumIndex()];
        }
        float titleWidth = getTitleWidth(longestTitle);
        float subtitleWidth = getSubtitleWidth(subContent);
        int measuredTitleWidth;
        if (maximumAvailableWidth > 0 && titleWidth >= maximumAvailableWidth) {
            drawTitleLength = availableDrawTitleLength(longestTitle, maximumAvailableWidth);
            measuredTitleWidth = maximumAvailableWidth;
        } else {
            if (longestTitle != null) drawTitleLength = longestTitle.length();
            measuredTitleWidth = (int) titleWidth;
        }
        int measuredSubtitleWidth;
        if (maximumAvailableWidth > 0 && subtitleWidth > 0
                && subtitleWidth >= maximumAvailableWidth) {
            drawSubtitleLength = availableDrawTitleLength(subContent, maximumAvailableWidth);
            measuredSubtitleWidth = maximumAvailableWidth;
        } else {
            if (subtitleWidth > 0) drawSubtitleLength = subContent.length();
            measuredSubtitleWidth = (int) subtitleWidth;
        }
        return Math.max(measuredTitleWidth, measuredSubtitleWidth);
    }

    private float getSubtitleWidth(String text) {
        if (TextUtils.isEmpty(text)) return 0f;
        subtitlePaint.setTextSize(!TextUtils.isEmpty(subContent)
                ? Math.min(maximumSubtitleSize, subtitleSize) : subtitleSize);
        return subtitlePaint.measureText(text);
    }

    private float getTitleWidth(String text) {
        if (TextUtils.isEmpty(text)) return 0f;
        titlePaint.setTextSize(!TextUtils.isEmpty(subContent)
                ? Math.min(maximumTitleSizeWithSubtitle, titleSize) : titleSize);
        return titlePaint.measureText(text);
    }

    private int getTitleHeight(String text) {
        Rect bounds = new Rect();
        if (text != null) titlePaint.getTextBounds(text, 0, text.length(), bounds);
        return bounds.height();
    }

    private boolean supportsRotation() {
        return needRotate && displayedValues != null && displayedValues.length > 0;
    }

    CharSequence getAccessibilityText() {
        String title = null;
        if (displayedValues != null && displayedValues.length > 0) {
            title = displayedValues[Math.floorMod(value, displayedValues.length)];
        }
        if (TextUtils.isEmpty(title)) return subContent;
        if (TextUtils.isEmpty(subContent)) return title;
        return title + ", " + subContent;
    }

    private void updateAccessibilityDescription() {
        if (TextUtils.isEmpty(getContentDescription())) {
            // Keep the view's explicit contentDescription authoritative. The current value is
            // supplied from onInitializeAccessibilityNodeInfo when no explicit value exists.
            sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
        }
    }

    @Override protected Parcelable onSaveInstanceState() {
        return new SavedState(super.onSaveInstanceState(), value, needRotate,
                displayedValues, subContent);
    }

    @Override protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        displayedValues = savedState.displayedValues;
        subContent = savedState.subContent;
        needRotate = savedState.needRotate;
        if (displayedValues != null && displayedValues.length > 0) {
            value = Math.floorMod(savedState.value, displayedValues.length);
        } else {
            value = Math.max(0, savedState.value);
        }
        initializeSelectorWheelIndices();
        requestLayout();
        invalidate();
    }

    private static final class SavedState extends BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    @Override public SavedState createFromParcel(Parcel source) {
                        return new SavedState(source);
                    }

                    @Override public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };

        final int value;
        final boolean needRotate;
        final String[] displayedValues;
        final String subContent;

        SavedState(Parcelable superState, int value, boolean needRotate,
                String[] displayedValues, String subContent) {
            super(superState);
            this.value = value;
            this.needRotate = needRotate;
            this.displayedValues = displayedValues == null ? null : displayedValues.clone();
            this.subContent = subContent;
        }

        SavedState(Parcel source) {
            super(source);
            value = source.readInt();
            needRotate = source.readInt() != 0;
            displayedValues = source.createStringArray();
            subContent = source.readString();
        }

        @Override public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(value);
            out.writeInt(needRotate ? 1 : 0);
            out.writeStringArray(displayedValues);
            out.writeString(subContent);
        }
    }

    private int displayMaximumIndex() {
        if (displayedValues == null || displayedValues.length == 0) return 0;
        int maximumIndex = 0;
        float maximumLength = 0f;
        for (int i = 0; i < displayedValues.length; i++) {
            float length = getTitleWidth(displayedValues[i]);
            if (length > maximumLength) {
                maximumLength = length;
                maximumIndex = i;
            }
        }
        return maximumIndex;
    }
}
