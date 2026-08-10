package org.opensmartisanos.ui.widget;


import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import org.opensmartisanos.ui.R;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;



/* JADX INFO: loaded from: classes.dex */
public class SmartisanTimePicker extends FrameLayout {
    public interface OnTimeChangedListener {
        void onTimeChanged(SmartisanTimePicker view, int hourOfDay, int minute);
    }

    private static final boolean DEFAULT_ENABLED_STATE = true;
    private static final int HOURS_IN_HALF_DAY = 12;
    private Button mAmPmButton;
    public SmartisanNumberPicker mAmPmSpinner;
    private String[] mAmPmStrings;
    private Locale mCurrentLocale;
    public SmartisanNumberPicker mHourSpinner;
    private boolean mIs24HourView;
    private boolean mIsAm;
    private boolean mIsEnabled;
    public SmartisanNumberPicker mMinuteSpinner;
    private Calendar mTempCalendar;
    private OnTimeChangedListener mOnTimeChangedListener;

    public SmartisanTimePicker(Context context) {
        this(context, null);
    }

    public SmartisanTimePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmartisanTimePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mIsEnabled = true;
        setCurrentLocale(Locale.getDefault());
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (SmartisanLocaleUtils.isExternalDisplay(context)) {
            inflater.inflate(R.layout.smartisan_rom_revone_time_picker, (ViewGroup) this, true);
        } else {
            inflater.inflate(R.layout.smartisan_rom_time_picker, (ViewGroup) this, true);
        }
        Resources res = getResources();
        int normalColor = res.getColor(R.color.smartisan_rom_date_pick_normal_day_color);
        int selectColor = res.getColor(R.color.smartisan_rom_date_pick_select_day_color);
        SmartisanNumberPicker smartisanNumberPicker = (SmartisanNumberPicker) findViewById(R.id.smartisan_rom_hour);
        this.mHourSpinner = smartisanNumberPicker;
        smartisanNumberPicker.setTextSize(SmartisanLocaleUtils.dp(getContext(), 16.0f), SmartisanLocaleUtils.dp(getContext(), 18.0f));
        this.mHourSpinner.setTextColor(normalColor, selectColor);
        this.mHourSpinner.setOnValueChangedListener(new SmartisanNumberPicker.OnValueChangeListener() { // from class: smartisanos.widget.SmartisanTimePicker.1
            @Override // smartisanos.widget.SmartisanNumberPicker.OnValueChangeListener
            public void onValueChange(SmartisanNumberPicker spinner, int oldVal, int newVal) {
                if (!SmartisanTimePicker.this.is24HourView() && ((oldVal == 11 && newVal == 12) || (oldVal == 12 && newVal == 11))) {
                    SmartisanTimePicker smartisanTimePicker = SmartisanTimePicker.this;
                    smartisanTimePicker.mIsAm = !smartisanTimePicker.mIsAm;
                    SmartisanTimePicker.this.updateAmPmControl();
                }
                SmartisanTimePicker.this.onTimeChanged();
            }
        });
        SmartisanNumberPicker smartisanNumberPicker2 = (SmartisanNumberPicker) findViewById(R.id.smartisan_rom_minute);
        this.mMinuteSpinner = smartisanNumberPicker2;
        smartisanNumberPicker2.setMinValue(0);
        this.mMinuteSpinner.setMaxValue(59);
        this.mMinuteSpinner.setFormatter(SmartisanNumberPicker.getTwoDigitFormatter());
        this.mMinuteSpinner.setTextSize(SmartisanLocaleUtils.dp(getContext(), 16.0f), SmartisanLocaleUtils.dp(getContext(), 18.0f));
        this.mMinuteSpinner.setTextColor(normalColor, selectColor);
        this.mMinuteSpinner.setOnValueChangedListener(new SmartisanNumberPicker.OnValueChangeListener() { // from class: smartisanos.widget.SmartisanTimePicker.2
            @Override // smartisanos.widget.SmartisanNumberPicker.OnValueChangeListener
            public void onValueChange(SmartisanNumberPicker picker, int oldVal, int newVal) {
                SmartisanTimePicker.this.onTimeChanged();
            }
        });
        this.mAmPmStrings = new DateFormatSymbols().getAmPmStrings();
        View amPmView = findViewById(R.id.smartisan_rom_amPm);
        if (amPmView instanceof Button) {
            this.mAmPmSpinner = null;
            Button button = (Button) amPmView;
            this.mAmPmButton = button;
            button.setOnClickListener(new View.OnClickListener() { // from class: smartisanos.widget.SmartisanTimePicker.3
                @Override // android.view.View.OnClickListener
                public void onClick(View button2) {
                    button2.requestFocus();
                    SmartisanTimePicker smartisanTimePicker = SmartisanTimePicker.this;
                    smartisanTimePicker.mIsAm = !smartisanTimePicker.mIsAm;
                    SmartisanTimePicker.this.updateAmPmControl();
                    SmartisanTimePicker.this.onTimeChanged();
                }
            });
        } else {
            this.mAmPmButton = null;
            SmartisanNumberPicker smartisanNumberPicker3 = (SmartisanNumberPicker) amPmView;
            this.mAmPmSpinner = smartisanNumberPicker3;
            smartisanNumberPicker3.setMinValue(0);
            this.mAmPmSpinner.setMaxValue(1);
            this.mAmPmSpinner.setDisplayedValues(this.mAmPmStrings);
            this.mAmPmSpinner.setOnValueChangedListener(new SmartisanNumberPicker.OnValueChangeListener() { // from class: smartisanos.widget.SmartisanTimePicker.4
                @Override // smartisanos.widget.SmartisanNumberPicker.OnValueChangeListener
                public void onValueChange(SmartisanNumberPicker picker, int oldVal, int newVal) {
                    SmartisanTimePicker smartisanTimePicker = SmartisanTimePicker.this;
                    smartisanTimePicker.mIsAm = !smartisanTimePicker.mIsAm;
                    SmartisanTimePicker.this.updateAmPmControl();
                    SmartisanTimePicker.this.onTimeChanged();
                }
            });
            this.mAmPmSpinner.setTextSize(SmartisanLocaleUtils.dp(getContext(), 16.0f), SmartisanLocaleUtils.dp(getContext(), 18.0f));
            this.mAmPmSpinner.setTextColor(normalColor, selectColor);
        }
        setIs24HourView(android.text.format.DateFormat.is24HourFormat(context));
        updateHourControl();
        updateAmPmControl();
        if (!isEnabled()) {
            setEnabled(false);
        }
        if (getImportantForAccessibility() == 0) {
            setImportantForAccessibility(1);
        }
        setBackgroundResource(R.drawable.smartisan_rom_time_picker_widget_bg);
    }

    public String[] getDisplayDate(Context mcContext, int julian, Time time) {
        int[] julianl = {julian - 2, julian - 1, julian, julian + 1, julian + 2};
        String[] ss = new String[5];
        for (int i = 0; i < julianl.length; i++) {
            long millims = time.setJulianDay(julianl[i]);
            ss[i] = DateUtils.formatDateRange(mcContext, millims, millims, 16);
        }
        return ss;
    }

    @Override // android.view.View
    public void setEnabled(boolean enabled) {
        if (this.mIsEnabled == enabled) {
            return;
        }
        super.setEnabled(enabled);
        this.mMinuteSpinner.setEnabled(enabled);
        this.mHourSpinner.setEnabled(enabled);
        SmartisanNumberPicker smartisanNumberPicker = this.mAmPmSpinner;
        if (smartisanNumberPicker != null) {
            smartisanNumberPicker.setEnabled(enabled);
        } else {
            this.mAmPmButton.setEnabled(enabled);
        }
        this.mIsEnabled = enabled;
    }

    @Override // android.view.View
    public boolean isEnabled() {
        return this.mIsEnabled;
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setCurrentLocale(SmartisanLocaleUtils.primaryLocale(newConfig));
    }

    private void setCurrentLocale(Locale locale) {
        if (locale.equals(this.mCurrentLocale)) {
            return;
        }
        this.mCurrentLocale = locale;
        this.mTempCalendar = Calendar.getInstance(locale);
    }

    private static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() { // from class: smartisanos.widget.SmartisanTimePicker.SavedState.1
            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        private final int mHour;
        private final int mMinute;

        private SavedState(Parcelable superState, int hour, int minute) {
            super(superState);
            this.mHour = hour;
            this.mMinute = minute;
        }

        private SavedState(Parcel in) {
            super(in);
            this.mHour = in.readInt();
            this.mMinute = in.readInt();
        }

        public int getHour() {
            return this.mHour;
        }

        public int getMinute() {
            return this.mMinute;
        }

        @Override // android.view.View.BaseSavedState, android.view.AbsSavedState, android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.mHour);
            dest.writeInt(this.mMinute);
        }
    }

    @Override // android.view.View
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, getCurrentHour().intValue(), getCurrentMinute().intValue());
    }

    @Override // android.view.View
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setCurrentHour(Integer.valueOf(ss.getHour()));
        setCurrentMinute(Integer.valueOf(ss.getMinute()));
    }

    public Integer getCurrentHour() {
        int currentHour = this.mHourSpinner.getValue();
        if (is24HourView()) {
            return Integer.valueOf(currentHour);
        }
        if (this.mIsAm) {
            return Integer.valueOf(currentHour % 12);
        }
        return Integer.valueOf((currentHour % 12) + 12);
    }

    public void setCurrentHour(Integer currentHour) {
        if (currentHour == null || currentHour.equals(getCurrentHour())) {
            return;
        }
        if (!is24HourView()) {
            if (currentHour.intValue() >= 12) {
                this.mIsAm = false;
                if (currentHour.intValue() > 12) {
                    currentHour = Integer.valueOf(currentHour.intValue() - 12);
                }
            } else {
                this.mIsAm = true;
                if (currentHour.intValue() == 0) {
                    currentHour = 12;
                }
            }
            updateAmPmControl();
        }
        this.mHourSpinner.setValue(currentHour.intValue());
    }

    public void setIs24HourView(Boolean is24HourView) {
        if (this.mIs24HourView == is24HourView.booleanValue()) {
            return;
        }
        this.mIs24HourView = is24HourView.booleanValue();
        int currentHour = getCurrentHour().intValue();
        updateHourControl();
        setCurrentHour(Integer.valueOf(currentHour));
        updateAmPmControl();
        setBackgroundResource(R.drawable.smartisan_rom_time_picker_widget_bg);
    }

    public boolean is24HourView() {
        return this.mIs24HourView;
    }

    public Integer getCurrentMinute() {
        return Integer.valueOf(this.mMinuteSpinner.getValue());
    }

    public void setCurrentMinute(Integer currentMinute) {
        if (currentMinute == null || currentMinute.equals(getCurrentMinute())) {
            return;
        }
        this.mMinuteSpinner.setValue(currentMinute.intValue());
    }

    /** @deprecated Original Smartisan callers observe changes through accessibility and dialogs. */
    @Deprecated
    public void setOnTimeChangedListener(OnTimeChangedListener listener) {
        this.mOnTimeChangedListener = listener;
    }

    @Override // android.view.View
    public int getBaseline() {
        return this.mHourSpinner.getBaseline();
    }

    @Override // android.view.View
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        onPopulateAccessibilityEvent(event);
        return true;
    }

    @Override // android.view.View
    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        int flags;
        super.onPopulateAccessibilityEvent(event);
        if (this.mIs24HourView) {
            flags = 1 | 128;
        } else {
            flags = 1 | 64;
        }
        this.mTempCalendar.set(11, getCurrentHour().intValue());
        this.mTempCalendar.set(12, getCurrentMinute().intValue());
        String selectedDateUtterance = DateUtils.formatDateTime(getContext(), this.mTempCalendar.getTimeInMillis(), flags);
        event.getText().add(selectedDateUtterance);
    }

    @Override // android.view.View
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(SmartisanTimePicker.class.getName());
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(SmartisanTimePicker.class.getName());
    }

    private void updateHourControl() {
        if (is24HourView()) {
            this.mHourSpinner.setMinValue(0);
            this.mHourSpinner.setMaxValue(23);
            this.mHourSpinner.setFormatter(SmartisanNumberPicker.getTwoDigitFormatter());
        } else {
            this.mHourSpinner.setMinValue(1);
            this.mHourSpinner.setMaxValue(12);
            this.mHourSpinner.setFormatter(null);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    private void updateAmPmControl() {
        if (is24HourView()) {
            SmartisanNumberPicker smartisanNumberPicker = this.mAmPmSpinner;
            if (smartisanNumberPicker != null) {
                smartisanNumberPicker.setVisibility(View.GONE);
            } else {
                this.mAmPmButton.setVisibility(View.GONE);
            }
        } else {
            int i = !this.mIsAm ? 1 : 0;
            SmartisanNumberPicker smartisanNumberPicker2 = this.mAmPmSpinner;
            if (smartisanNumberPicker2 != null) {
                smartisanNumberPicker2.setValue(i);
                this.mAmPmSpinner.setVisibility(View.VISIBLE);
            } else {
                this.mAmPmButton.setText(this.mAmPmStrings[i]);
                this.mAmPmButton.setVisibility(View.VISIBLE);
            }
        }
        sendAccessibilityEvent(4);
    }

    /* JADX INFO: Access modifiers changed from: private */
    private void onTimeChanged() {
        sendAccessibilityEvent(4);
        if (this.mOnTimeChangedListener != null) {
            this.mOnTimeChangedListener.onTimeChanged(
                    this, getCurrentHour(), getCurrentMinute());
        }
    }
}
