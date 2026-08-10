/* Ported from Smartisan OS 8.5.3 R2 smartisanos.widget.SmartisanSpinnerView. */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.opensmartisanos.ui.R;

/** Smartisan title selector with normal, drop-down and range modes. */
public class SmartisanSpinnerView extends RelativeLayout {
    public static final int SPINNER_STYLE_NORMAL = 0;
    public static final int SPINNER_STYLE_DROP = 1;
    public static final int SPINNER_STYLE_RANGE = 2;

    /** Called when the original drop-down arrow is pressed. */
    public interface SpinnerDropDownClickListener {
        void onDropDownClick();
    }

    /** Called when either range arrow is pressed. */
    public interface SpinnerRangeClickListener {
        void onRangeLeftClick();
        void onRangeRightClick();
    }

    /** Receives vertical wheel changes in range mode. */
    public interface SpinnerRangeWheelTextChangeListener {
        void onWheelTextChangeListener(SmartisanWheelTextView view, int oldValue, int newValue);
    }

    private int basicBlankSpacingWidth;
    private SpinnerDropDownClickListener dropDownListener;
    private ImageButton dropIcon;
    private int iconWidth;
    private boolean verticalScroll;
    private SpinnerRangeClickListener rangeClickListener;
    private ImageButton rangeLeftIcon;
    private ImageButton rangeRightIcon;
    private SpinnerRangeWheelTextChangeListener rangeWheelTextChangeListener;
    private int spinnerStyle;
    private ImageView textLeftIcon;
    private int textLeftIconResource;
    private SmartisanWheelTextView textPicker;
    private OnClickListener textClickListener;
    private String[] titleArray;
    private String titleText;

    public SmartisanSpinnerView(Context context) {
        this(context, null);
    }

    public SmartisanSpinnerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmartisanSpinnerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray attributes = context.obtainStyledAttributes(
                attrs, R.styleable.SmartisanSpinnerView, defStyleAttr, 0);
        spinnerStyle = attributes.getInt(
                R.styleable.SmartisanSpinnerView_smartisanSpinnerViewStyle,
                SPINNER_STYLE_NORMAL);
        int titleResource = attributes.getResourceId(
                R.styleable.SmartisanSpinnerView_smartisanSpinnerText, -1);
        if (titleResource < 0) {
            titleText = attributes.getString(
                    R.styleable.SmartisanSpinnerView_smartisanSpinnerText);
        } else {
            try {
                titleText = getResources().getString(titleResource);
            } catch (Resources.NotFoundException ignored) {
                titleArray = getResources().getStringArray(titleResource);
            }
        }
        textLeftIconResource = attributes.getResourceId(
                R.styleable.SmartisanSpinnerView_smartisanSpinnerLeftIcon, -1);
        verticalScroll = attributes.getBoolean(
                R.styleable.SmartisanSpinnerView_smartisanSpinnerVerticalScroll, false);
        attributes.recycle();

        basicBlankSpacingWidth = getResources().getDimensionPixelOffset(
                R.dimen.smartisan_rom_spinner_small_blank_spacing_width);
        iconWidth = getResources().getDimensionPixelOffset(
                R.dimen.smartisan_rom_spinner_small_icon_width);
        if (spinnerStyle >= 0) {
            setSpinnerStyle(spinnerStyle);
        }
    }

    public ImageView getTextLeftIcon() {
        return textLeftIcon;
    }

    public void setIsNeedVerticalScroll(boolean needScroll) {
        verticalScroll = needScroll;
        if (textPicker != null) {
            textPicker.setIsNeedRotate(needScroll);
        }
    }

    public void setCenterLeftIcon(int leftIconResource) {
        textLeftIconResource = leftIconResource;
        if (spinnerStyle == SPINNER_STYLE_DROP) setSpinnerStyle(spinnerStyle);
    }

    public void setSpinnerPickText(String[] textArray, String text) {
        titleArray = textArray;
        titleText = text;
        if (textPicker != null) {
            setPickerText();
            updateIconContentDescriptions();
        }
    }

    private int measureViewWidth(View view) {
        if (view == null) return 0;
        view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        return view.getMeasuredWidth();
    }

    public void setPickerAvailWidth(int maximumWidth) {
        if (spinnerStyle == SPINNER_STYLE_DROP) {
            maximumWidth -= measureViewWidth(dropIcon) * 2;
        } else if (spinnerStyle == SPINNER_STYLE_RANGE) {
            maximumWidth -= iconWidth * 2;
        }
        if (textPicker != null) {
            textPicker.setAvailWidth(maximumWidth);
        }
    }

    public void setSubContentText(CharSequence text) {
        setSubContentText(text == null ? null : text.toString());
    }

    public void setSubContentText(String text) {
        if (textPicker != null) {
            textPicker.setSubContentText(text);
            updateIconContentDescriptions();
        }
    }

    public void setSpinnerStyle(int style) {
        spinnerStyle = style;
        removeStyleViews();
        setTitleStyle();
        textPicker.setMinimumWidth(0);
        textPicker.setOnValueChangedListener(null);
        if (style == SPINNER_STYLE_DROP) {
            setLeftIconStyle();
            setDropIconStyle();
            adjustTextPickerLayoutParams();
        } else if (style == SPINNER_STYLE_RANGE) {
            setRangeIconStyle();
            textPicker.setIsNeedRotate(verticalScroll);
            textPicker.setOnValueChangedListener((picker, oldValue, newValue) -> {
                if (rangeWheelTextChangeListener != null) {
                    rangeWheelTextChangeListener.onWheelTextChangeListener(
                            picker, oldValue, newValue);
                }
                updateIconContentDescriptions();
            });
        }
        applyRtlToDirectionalIcons();
        updateIconContentDescriptions();
        setEnabled(isEnabled());
    }

    public void setTitleColor(int colorValue) {
        textPicker.setTitleColor(colorValue);
    }

    public void setTitleSize(float size) {
        textPicker.setTextSize(size);
    }

    public void setTitleMaxSize(float maximumSize) {
        textPicker.setTextMaxSize(maximumSize);
    }

    public void setSubtitleColor(int colorValue) {
        textPicker.setSubtitleColor(colorValue);
    }

    public void setSubtitleTextSize(float size) {
        textPicker.setSubTextSize(size);
    }

    private void setTitleStyle() {
        if (textPicker == null) {
            textPicker = new SmartisanWheelTextView(getContext());
            textPicker.setId(R.id.smartisan_rom_spinner_text);
            textPicker.setEnabled(true);
            if (textClickListener != null) textPicker.setOnClickListener(textClickListener);
        }
        setPickerText();
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(CENTER_IN_PARENT);
        textPicker.setLayoutParams(params);
        if (textPicker.getParent() == null) addView(textPicker);
    }

    private void removeStyleViews() {
        if (textLeftIcon != null) removeView(textLeftIcon);
        if (dropIcon != null) removeView(dropIcon);
        if (rangeLeftIcon != null) removeView(rangeLeftIcon);
        if (rangeRightIcon != null) removeView(rangeRightIcon);
        textLeftIcon = null;
        dropIcon = null;
        rangeLeftIcon = null;
        rangeRightIcon = null;
    }

    private void setLeftIconStyle() {
        if (textLeftIconResource <= 0) return;
        textLeftIcon = new ImageView(getContext());
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(CENTER_VERTICAL);
        params.addRule(START_OF, textPicker.getId());
        params.setMarginEnd(basicBlankSpacingWidth);
        addView(textLeftIcon, params);
        textLeftIcon.setImageResource(textLeftIconResource);
    }

    private void setDropIconStyle() {
        dropIcon = new ImageButton(getContext());
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(CENTER_VERTICAL);
        params.addRule(END_OF, textPicker.getId());
        params.setMarginEnd(basicBlankSpacingWidth);
        addView(dropIcon, params);
        dropIcon.setBackgroundColor(0);
        dropIcon.setImageResource(R.drawable.smartisan_rom_selector_dropdown_arrow);
        dropIcon.setOnClickListener(view -> {
            if (dropDownListener != null) dropDownListener.onDropDownClick();
        });
        updateIconContentDescriptions();
    }

    private void setRangeIconStyle() {
        rangeLeftIcon = new ImageButton(getContext());
        LayoutParams leftParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        leftParams.addRule(CENTER_VERTICAL);
        leftParams.addRule(START_OF, textPicker.getId());
        leftParams.setMarginStart(basicBlankSpacingWidth);
        addView(rangeLeftIcon, leftParams);
        rangeLeftIcon.setBackgroundColor(0);
        rangeLeftIcon.setImageResource(R.drawable.smartisan_rom_selector_previous_arrow);

        rangeRightIcon = new ImageButton(getContext());
        LayoutParams rightParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rightParams.addRule(CENTER_VERTICAL);
        rightParams.addRule(END_OF, textPicker.getId());
        rightParams.setMarginEnd(basicBlankSpacingWidth);
        addView(rangeRightIcon, rightParams);
        rangeRightIcon.setBackgroundColor(0);
        rangeRightIcon.setImageResource(R.drawable.smartisan_rom_selector_next_arrow);

        rangeLeftIcon.setOnClickListener(view -> {
            if (rangeClickListener != null) rangeClickListener.onRangeLeftClick();
        });
        rangeRightIcon.setOnClickListener(view -> {
            if (rangeClickListener != null) rangeClickListener.onRangeRightClick();
        });
        textPicker.setMinimumWidth(getResources().getDimensionPixelSize(
                R.dimen.smartisan_rom_spinner_view_text_width));
        applyRtlToDirectionalIcons();
        updateIconContentDescriptions();
    }

    private void adjustTextPickerLayoutParams() {
        LayoutParams params = (LayoutParams) textPicker.getLayoutParams();
        params.leftMargin = textLeftIconResource <= 0 ? basicBlankSpacingWidth * 2 : 0;
        textPicker.setLayoutParams(params);
    }

    private void setPickerText() {
        if (titleArray == null) setTitleArray(titleText);
        else setTitleArray(titleArray);
    }

    void setTitleArray(String... titles) {
        if (titles == null || titles.length == 0) titles = new String[] { "" };
        titleArray = titles;
        textPicker.setIsNeedRotate(titles.length != 1);
        textPicker.setDisplayedValues(titles);
        updateIconContentDescriptions();
    }

    /** Kept with the original misspelled public API. */
    public void seTextLeftIcon(int iconResource) {
        if (iconResource <= 0 && spinnerStyle == SPINNER_STYLE_RANGE) return;
        textLeftIconResource = iconResource;
        if (textLeftIcon != null) textLeftIcon.setImageResource(iconResource);
        else setLeftIconStyle();
        adjustTextPickerLayoutParams();
    }

    public void setDropDownClickListener(SpinnerDropDownClickListener listener) {
        dropDownListener = listener;
    }

    public void setRangeClickListener(SpinnerRangeClickListener listener) {
        rangeClickListener = listener;
    }

    public void setRangeWheelTextChangeListener(
            SpinnerRangeWheelTextChangeListener listener) {
        rangeWheelTextChangeListener = listener;
    }

    public void setLeftRangeSpinnerIconVisible(int visibility) {
        if (spinnerStyle == SPINNER_STYLE_RANGE && rangeLeftIcon != null) {
            rangeLeftIcon.setVisibility(visibility);
        }
    }

    public void setRightRangeSpinnerIconVisible(int visibility) {
        if (spinnerStyle == SPINNER_STYLE_RANGE && rangeRightIcon != null) {
            rangeRightIcon.setVisibility(visibility);
        }
    }

    public void setRangeSpinnerIconVisible(int visibility) {
        setLeftRangeSpinnerIconVisible(visibility);
        setRightRangeSpinnerIconVisible(visibility);
    }

    public void setDropdownIconVisibility(int visibility) {
        if (dropIcon != null) dropIcon.setVisibility(visibility);
    }

    public View getDropdownIconView() {
        return dropIcon;
    }

    public ImageButton getRangeLeftIcon() {
        return rangeLeftIcon;
    }

    public ImageButton getRangeRightIcon() {
        return rangeRightIcon;
    }

    @Override public void setOnClickListener(OnClickListener listener) {
        textClickListener = listener;
        if (textPicker != null) textPicker.setOnClickListener(listener);
    }

    public int getSpinnerStyle() {
        return spinnerStyle;
    }

    @Override public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (textPicker != null) textPicker.setEnabled(enabled);
        if (textLeftIcon != null) textLeftIcon.setEnabled(enabled);
        if (dropIcon != null) dropIcon.setEnabled(enabled);
        if (rangeLeftIcon != null) rangeLeftIcon.setEnabled(enabled);
        if (rangeRightIcon != null) rangeRightIcon.setEnabled(enabled);
    }

    @Override public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        applyRtlToDirectionalIcons();
        updateIconContentDescriptions();
    }

    @Override public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(android.widget.Spinner.class.getName());
        if (textPicker != null && info.getContentDescription() == null) {
            info.setContentDescription(textPicker.getAccessibilityText());
        }
    }

    private void applyRtlToDirectionalIcons() {
        float scale = getLayoutDirection() == LAYOUT_DIRECTION_RTL ? -1f : 1f;
        if (rangeLeftIcon != null) rangeLeftIcon.setScaleX(scale);
        if (rangeRightIcon != null) rangeRightIcon.setScaleX(scale);
    }

    private void updateIconContentDescriptions() {
        if (textPicker == null) return;
        CharSequence value = textPicker.getAccessibilityText();
        String prefix = value == null ? "" : value.toString();
        if (dropIcon != null) dropIcon.setContentDescription(prefix + " ▼");
        boolean rtl = getLayoutDirection() == LAYOUT_DIRECTION_RTL;
        if (rangeLeftIcon != null) {
            rangeLeftIcon.setContentDescription(prefix + (rtl ? " →" : " ←"));
        }
        if (rangeRightIcon != null) {
            rangeRightIcon.setContentDescription(prefix + (rtl ? " ←" : " →"));
        }
    }

    @Override protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        // Every SpinnerView uses the original internal wheel ID for RelativeLayout rules. Saving
        // children globally would therefore let one instance overwrite another in the host
        // hierarchy's SparseArray. Keep the complete wheel state inside this parent's state.
        dispatchFreezeSelfOnly(container);
    }

    @Override protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    @Override protected Parcelable onSaveInstanceState() {
        Parcelable wheelState = textPicker == null ? null : textPicker.onSaveInstanceState();
        return new SavedState(super.onSaveInstanceState(), spinnerStyle, verticalScroll,
                titleArray, titleText, textLeftIconResource, wheelState);
    }

    @Override protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        verticalScroll = savedState.verticalScroll;
        titleArray = savedState.titleArray;
        titleText = savedState.titleText;
        textLeftIconResource = savedState.textLeftIconResource;
        setSpinnerStyle(savedState.spinnerStyle);
        if (savedState.wheelState != null) {
            textPicker.onRestoreInstanceState(savedState.wheelState);
        }
        updateIconContentDescriptions();
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

        final int spinnerStyle;
        final boolean verticalScroll;
        final String[] titleArray;
        final String titleText;
        final int textLeftIconResource;
        final Parcelable wheelState;

        SavedState(Parcelable superState, int spinnerStyle, boolean verticalScroll,
                String[] titleArray, String titleText, int textLeftIconResource,
                Parcelable wheelState) {
            super(superState);
            this.spinnerStyle = spinnerStyle;
            this.verticalScroll = verticalScroll;
            this.titleArray = titleArray == null ? null : titleArray.clone();
            this.titleText = titleText;
            this.textLeftIconResource = textLeftIconResource;
            this.wheelState = wheelState;
        }

        SavedState(Parcel source) {
            super(source);
            spinnerStyle = source.readInt();
            verticalScroll = source.readInt() != 0;
            titleArray = source.createStringArray();
            titleText = source.readString();
            textLeftIconResource = source.readInt();
            wheelState = source.readParcelable(SmartisanWheelTextView.class.getClassLoader());
        }

        @Override public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(spinnerStyle);
            out.writeInt(verticalScroll ? 1 : 0);
            out.writeStringArray(titleArray);
            out.writeString(titleText);
            out.writeInt(textLeftIconResource);
            out.writeParcelable(wheelState, flags);
        }
    }
}
