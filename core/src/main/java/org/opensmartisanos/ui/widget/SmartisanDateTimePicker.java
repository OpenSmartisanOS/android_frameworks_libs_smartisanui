package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import org.opensmartisanos.ui.R;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;



/* JADX INFO: loaded from: classes.dex */
public class SmartisanDateTimePicker extends FrameLayout {
    private static final String DATE_FORMAT = "yyyy/MM/dd";
    private static final boolean DEFAULT_ENABLED_STATE = true;
    private long mBaseDateMills;
    private Calendar mCurrentDate;
    private Locale mCurrentLocale;
    private final DateFormat mDateFormat;
    private final SmartisanNumberPickerEx mDateSpinner;
    private final SmartisanNumberPickerEx mHourSpinner;
    private boolean mIsEnabled;
    private Calendar mMaxDate;
    private Calendar mMinDate;
    private final SmartisanNumberPickerEx mMinuteSpinner;
    private OnDateChangedListener mOnDateChangedListener;
    private final LinearLayout mSpinners;
    private Calendar mTempDate;

    public interface OnDateChangedListener {
        void onDateChanged(long j);
    }

    public SmartisanDateTimePicker(Context context) {
        this(context, null);
    }

    public SmartisanDateTimePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmartisanDateTimePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mDateFormat = new SimpleDateFormat(DATE_FORMAT);
        this.mIsEnabled = true;
        setCurrentLocale(Locale.getDefault());
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.smartisan_rom_date_time_picker_ex, (ViewGroup) this, true);
        Resources res = getResources();
        int normalColor = res.getColor(R.color.smartisan_rom_date_pick_ex_normal_day_color);
        int selectColor = res.getColor(R.color.smartisan_rom_date_pick_ex_select_day_color);
        this.mSpinners = (LinearLayout) findViewById(R.id.smartisan_rom_pickers);
        SmartisanNumberPickerEx smartisanNumberPickerEx = (SmartisanNumberPickerEx) findViewById(R.id.smartisan_rom_minute);
        this.mMinuteSpinner = smartisanNumberPickerEx;
        smartisanNumberPickerEx.setMinValue(0);
        this.mMinuteSpinner.setMaxValue(59);
        this.mMinuteSpinner.setFormatter(SmartisanNumberPickerEx.getTwoDigitFormatter());
        this.mMinuteSpinner.setTextColor(normalColor, selectColor);
        this.mMinuteSpinner.setTextSize(SmartisanLocaleUtils.dp(getContext(), 16.0f), SmartisanLocaleUtils.dp(getContext(), 18.0f));
        this.mMinuteSpinner.setOnValueChangedListener(new SmartisanNumberPickerEx.OnValueChangeListener() { // from class: smartisanos.widget.SmartisanDateTimePicker.1
            @Override // smartisanos.widget.SmartisanNumberPickerEx.OnValueChangeListener
            public void onValueChange(SmartisanNumberPickerEx picker, int oldVal, int newVal) {
                SmartisanDateTimePicker.this.mCurrentDate.set(12, newVal);
                SmartisanDateTimePicker.this.notifyDateChanged();
            }
        });
        SmartisanNumberPickerEx smartisanNumberPickerEx2 = (SmartisanNumberPickerEx) findViewById(R.id.smartisan_rom_hour);
        this.mHourSpinner = smartisanNumberPickerEx2;
        smartisanNumberPickerEx2.setMinValue(0);
        this.mHourSpinner.setMaxValue(23);
        this.mHourSpinner.setTextSize(SmartisanLocaleUtils.dp(getContext(), 16.0f), SmartisanLocaleUtils.dp(getContext(), 18.0f));
        this.mHourSpinner.setTextColor(normalColor, selectColor);
        this.mHourSpinner.setOnValueChangedListener(new SmartisanNumberPickerEx.OnValueChangeListener() { // from class: smartisanos.widget.SmartisanDateTimePicker.2
            @Override // smartisanos.widget.SmartisanNumberPickerEx.OnValueChangeListener
            public void onValueChange(SmartisanNumberPickerEx picker, int oldVal, int newVal) {
                SmartisanDateTimePicker.this.mCurrentDate.set(11, newVal);
                SmartisanDateTimePicker.this.notifyDateChanged();
            }
        });
        SmartisanNumberPickerEx smartisanNumberPickerEx3 = (SmartisanNumberPickerEx) findViewById(R.id.smartisan_rom_date);
        this.mDateSpinner = smartisanNumberPickerEx3;
        smartisanNumberPickerEx3.setTextSize(SmartisanLocaleUtils.dp(getContext(), 15.0f), SmartisanLocaleUtils.dp(getContext(), 17.0f));
        this.mDateSpinner.setTextColor(normalColor, selectColor);
        this.mDateSpinner.setMinValue(0);
        this.mDateSpinner.setFormatter(new SmartisanNumberPickerEx.Formatter() { // from class: smartisanos.widget.SmartisanDateTimePicker.3
            @Override // smartisanos.widget.SmartisanNumberPickerEx.Formatter
            public String format(int value) {
                long newTimeMills = SmartisanDateTimePicker.this.getTimMillsForDayValue(value);
                return SmartisanDateTimePicker.this.mDateFormat.format(Long.valueOf(newTimeMills));
            }
        });
        this.mDateSpinner.setOnValueChangedListener(new SmartisanNumberPickerEx.OnValueChangeListener() { // from class: smartisanos.widget.SmartisanDateTimePicker.4
            @Override // smartisanos.widget.SmartisanNumberPickerEx.OnValueChangeListener
            public void onValueChange(SmartisanNumberPickerEx picker, int oldVal, int newVal) {
                long newTimeMills = SmartisanDateTimePicker.this.getTimMillsForDayValue(newVal);
                SmartisanDateTimePicker.this.mCurrentDate.setTimeInMillis(newTimeMills);
                SmartisanDateTimePicker.this.notifyDateChanged();
            }
        });
        boolean isChinese = "zh".equals(Locale.getDefault().getLanguage());
        if (isChinese) {
            this.mHourSpinner.setHightlightSuffix(context.getResources().getString(R.string.smartisan_rom_date_time_picker_hour));
            this.mMinuteSpinner.setHightlightSuffix(context.getResources().getString(R.string.smartisan_rom_date_time_picker_minute));
        }
        setSpinnersShown(true);
        if (getImportantForAccessibility() == 0) {
            setImportantForAccessibility(1);
        }
        setBackgroundResource(R.drawable.smartisan_rom_time_picker_widget_bg_ex_new);
    }

    /* JADX INFO: Access modifiers changed from: private */
    private long getTimMillsForDayValue(int value) {
        return this.mBaseDateMills + (((long) value) * 86400000L) + (((long) this.mHourSpinner.getValue()) * 3600000) + (((long) this.mMinuteSpinner.getValue()) * 60000);
    }

    private int getDayValueForTimeMills(long timeMills) {
        return Long.valueOf((timeMills - this.mBaseDateMills) / 86400000L).intValue();
    }

    public long getCurrentMills() {
        return this.mCurrentDate.getTimeInMillis();
    }

    /** @deprecated Convenience accessor added before the original API was restored. */
    @Deprecated
    public long getMinDate() {
        return this.mMinDate.getTimeInMillis();
    }

    /** @deprecated Convenience accessor added before the original API was restored. */
    @Deprecated
    public long getMaxDate() {
        return this.mMaxDate.getTimeInMillis();
    }

    public void setMinDate(long minTimeMills) {
        if (this.mMinDate.getTimeInMillis() == minTimeMills) {
            return;
        }
        changeMinDate(minTimeMills);
        if (this.mCurrentDate.before(this.mMinDate)) {
            this.mCurrentDate.setTimeInMillis(this.mMinDate.getTimeInMillis());
        }
        updateSpinners();
    }

    public void setCurrentDate(long currentDateMills) {
        if (this.mCurrentDate.getTimeInMillis() == currentDateMills) {
            return;
        }
        this.mTempDate.setTimeInMillis(currentDateMills);
        if (this.mTempDate.before(this.mMinDate) || this.mTempDate.after(this.mMaxDate)) {
            return;
        }
        this.mCurrentDate.setTimeInMillis(currentDateMills);
        updateSpinners();
    }

    private void changeMinDate(long minDate) {
        this.mMinDate.setTimeInMillis(minDate);
        this.mTempDate.set(this.mMinDate.get(1), this.mMinDate.get(2), this.mMinDate.get(5), 0, 0, 0);
        this.mBaseDateMills = this.mTempDate.getTimeInMillis();
    }

    public void setMaxDate(long maxTimeMIlls) {
        if (maxTimeMIlls == this.mMaxDate.getTimeInMillis()) {
            return;
        }
        this.mMaxDate.setTimeInMillis(maxTimeMIlls);
        if (this.mCurrentDate.after(this.mMaxDate)) {
            this.mCurrentDate.setTimeInMillis(this.mMaxDate.getTimeInMillis());
        }
        this.mDateSpinner.setMaxValue(getDayValueForTimeMills(maxTimeMIlls));
        this.mDateSpinner.setWrapSelectorWheel(false);
        updateSpinners();
    }

    @Override // android.view.View
    public void setEnabled(boolean enabled) {
        if (this.mIsEnabled == enabled) {
            return;
        }
        super.setEnabled(enabled);
        this.mMinuteSpinner.setEnabled(enabled);
        this.mHourSpinner.setEnabled(enabled);
        this.mDateSpinner.setEnabled(enabled);
        this.mIsEnabled = enabled;
    }

    @Override // android.view.View
    public boolean isEnabled() {
        return this.mIsEnabled;
    }

    @Override // android.view.View
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        onPopulateAccessibilityEvent(event);
        return true;
    }

    @Override // android.view.View
    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        super.onPopulateAccessibilityEvent(event);
        String selectedDateUtterance = DateUtils.formatDateTime(getContext(), this.mCurrentDate.getTimeInMillis(), 20);
        event.getText().add(selectedDateUtterance);
    }

    @Override // android.view.View
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(SmartisanDateTimePicker.class.getName());
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(SmartisanDateTimePicker.class.getName());
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setCurrentLocale(SmartisanLocaleUtils.primaryLocale(newConfig));
    }

    public boolean getSpinnersShown() {
        return this.mSpinners.isShown();
    }

    public void setSpinnersShown(boolean shown) {
        this.mSpinners.setVisibility(shown ? View.VISIBLE : View.GONE);
    }

    private void setCurrentLocale(Locale locale) {
        if (locale.equals(this.mCurrentLocale)) {
            return;
        }
        this.mCurrentLocale = locale;
        this.mTempDate = getCalendarForLocale(this.mTempDate, locale);
        this.mCurrentDate = getCalendarForLocale(this.mCurrentDate, locale);
        this.mMinDate = getCalendarForLocale(this.mMinDate, locale);
        this.mMaxDate = getCalendarForLocale(this.mMaxDate, locale);
    }

    private Calendar getCalendarForLocale(Calendar oldCalendar, Locale locale) {
        if (oldCalendar == null) {
            return Calendar.getInstance(locale);
        }
        long currentTimeMillis = oldCalendar.getTimeInMillis();
        Calendar newCalendar = Calendar.getInstance(locale);
        newCalendar.setTimeInMillis(currentTimeMillis);
        return newCalendar;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    @Override // android.view.View
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, this.mCurrentDate.getTimeInMillis(), this.mMinDate.getTimeInMillis(), this.mMaxDate.getTimeInMillis());
    }

    @Override // android.view.View
    public void onRestoreInstanceState(Parcelable state) {
        if (state == null || !(state instanceof SavedState)) {
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        init(ss.mCurrentMills, ss.mMinMills, ss.mMaxMills, null);
        setCurrentDate(ss.mCurrentMills);
        updateSpinners();
    }

    public void init(long initTimeMills, long miniTimeMills, long maxTimeMills, OnDateChangedListener onDateChangedListener) {
        this.mCurrentDate.setTimeInMillis(initTimeMills);
        setMinDate(miniTimeMills);
        setMaxDate(maxTimeMills);
        setSpinnersInitialValues();
        setOnDateChangedListener(onDateChangedListener);
    }

    public void setOnDateChangedListener(OnDateChangedListener listener) {
        this.mOnDateChangedListener = listener;
    }

    private void setSpinnersInitialValues() {
        this.mDateSpinner.setInitialValue(getDayValueForTimeMills(this.mCurrentDate.getTimeInMillis()));
        this.mHourSpinner.setInitialValue(this.mCurrentDate.get(11));
        this.mMinuteSpinner.setInitialValue(this.mCurrentDate.get(12));
    }

    private void updateSpinners() {
        this.mDateSpinner.setValue(getDayValueForTimeMills(this.mCurrentDate.getTimeInMillis()));
        this.mHourSpinner.setValue(this.mCurrentDate.get(11));
        this.mMinuteSpinner.setValue(this.mCurrentDate.get(12));
    }

    /* JADX INFO: Access modifiers changed from: private */
    private void notifyDateChanged() {
        sendAccessibilityEvent(4);
        OnDateChangedListener onDateChangedListener = this.mOnDateChangedListener;
        if (onDateChangedListener != null) {
            onDateChangedListener.onDateChanged(this.mCurrentDate.getTimeInMillis());
        }
    }

    private static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() { // from class: smartisanos.widget.SmartisanDateTimePicker.SavedState.1
            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        private long mCurrentMills;
        private long mMaxMills;
        private long mMinMills;

        private SavedState(Parcelable superState, long currentMills, long minMills, long maxMills) {
            super(superState);
            this.mCurrentMills = 0L;
            this.mMinMills = 0L;
            this.mMaxMills = 0L;
            this.mCurrentMills = currentMills;
            this.mMinMills = minMills;
            this.mMaxMills = maxMills;
        }

        private SavedState(Parcel in) {
            super(in);
            this.mCurrentMills = 0L;
            this.mMinMills = 0L;
            this.mMaxMills = 0L;
            this.mCurrentMills = in.readLong();
            this.mMinMills = in.readLong();
            this.mMaxMills = in.readLong();
        }

        @Override // android.view.View.BaseSavedState, android.view.AbsSavedState, android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeLong(this.mCurrentMills);
            dest.writeLong(this.mMinMills);
            dest.writeLong(this.mMaxMills);
        }
    }
}
