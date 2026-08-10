package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import org.opensmartisanos.ui.R;
import org.opensmartisanos.ui.internal.SmartisanPickerMath;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;



/* JADX INFO: loaded from: classes.dex */
public class SmartisanDatePickerEx extends FrameLayout {
    private static final String DATE_FORMAT = "MM/dd/yyyy";
    public static final int DEFAULT_BIRTHDAY_START_YEAR = 1800;
    private static final boolean DEFAULT_ENABLED_STATE = true;
    private static final int DEFAULT_EVENT_END_YEAR = 2037;
    private static final int DEFAULT_EVENT_START_YEAR = 1970;
    private static final boolean DEFAULT_SPINNERS_SHOWN = true;
    private static final String LOG_TAG = "SmartisanDatePickerEx";
    private Calendar mCurrentDate;
    private Locale mCurrentLocale;
    private final DateFormat mDateFormat;
    private final SmartisanNumberPickerEx mDaySpinner;
    private boolean mIsEnabled;
    private Calendar mMaxDate;
    private Calendar mMinDate;
    private final SmartisanNumberPickerEx mMonthSpinner;
    private int mNumberOfMonths;
    private OnDateChangedListener mOnDateChangedListener;
    private DatePickerType mPickerType;
    private String[] mShortMonths;
    private final LinearLayout mSpinners;
    private Calendar mTempDate;
    private final SmartisanNumberPickerEx mYearSpinner;

    public enum DatePickerType {
        EVENT,
        BIRTHDAY,
        BIRTHDAY_LUNAR
    }

    public interface OnDateChangedListener {
        void onDateChanged(SmartisanDatePickerEx smartisanDatePickerEx, int i, int i2, int i3, DatePickerType datePickerType);
    }

    public SmartisanDatePickerEx(Context context) {
        this(context, null);
    }

    public SmartisanDatePickerEx(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmartisanDatePickerEx(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mDateFormat = new SimpleDateFormat(DATE_FORMAT);
        this.mIsEnabled = true;
        setCurrentLocale(Locale.getDefault());
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.smartisan_rom_date_picker_ex, (ViewGroup) this, true);
        Resources res = getResources();
        int normalColor = res.getColor(R.color.smartisan_rom_date_pick_ex_normal_day_color);
        int selectColor = res.getColor(R.color.smartisan_rom_date_pick_ex_select_day_color);
        SmartisanNumberPickerEx.OnValueChangeListener onChangeListener = new SmartisanNumberPickerEx.OnValueChangeListener() { // from class: smartisanos.widget.SmartisanDatePickerEx.1
            @Override // smartisanos.widget.SmartisanNumberPickerEx.OnValueChangeListener
            public void onValueChange(SmartisanNumberPickerEx picker, int oldVal, int newVal) {
                SmartisanDatePickerEx.this.mTempDate.setTimeInMillis(SmartisanDatePickerEx.this.mCurrentDate.getTimeInMillis());
                if (picker == SmartisanDatePickerEx.this.mDaySpinner) {
                    SmartisanDatePickerEx.this.mTempDate.set(5, newVal);
                } else if (picker == SmartisanDatePickerEx.this.mMonthSpinner) {
                    SmartisanDatePickerEx.this.mTempDate.set(2, newVal);
                    SmartisanDatePickerEx.this.mTempDate.set(5, 1);
                } else if (picker == SmartisanDatePickerEx.this.mYearSpinner) {
                    SmartisanDatePickerEx.this.mTempDate.set(1, newVal);
                    SmartisanDatePickerEx.this.mTempDate.set(5, 1);
                } else {
                    throw new IllegalArgumentException();
                }
                int daySpinnerValue = SmartisanDatePickerEx.this.mDaySpinner.getValue();
                SmartisanDatePickerEx smartisanDatePickerEx = SmartisanDatePickerEx.this;
                smartisanDatePickerEx.setDate(smartisanDatePickerEx.mTempDate.get(1),
                        SmartisanDatePickerEx.this.mTempDate.get(2),
                        SmartisanPickerMath.normalizeDayOrFirst(daySpinnerValue,
                                SmartisanDatePickerEx.this.mTempDate.getActualMaximum(5)));
                SmartisanDatePickerEx.this.updateSpinners();
                SmartisanDatePickerEx.this.notifyDateChanged();
            }
        };
        this.mSpinners = (LinearLayout) findViewById(R.id.smartisan_rom_pickers);
        SmartisanNumberPickerEx smartisanNumberPickerEx = (SmartisanNumberPickerEx) findViewById(R.id.smartisan_rom_day);
        this.mDaySpinner = smartisanNumberPickerEx;
        smartisanNumberPickerEx.setFormatter(SmartisanNumberPickerEx.getTwoDigitFormatter());
        this.mDaySpinner.setOnValueChangedListener(onChangeListener);
        this.mDaySpinner.setTextSize(SmartisanLocaleUtils.dp(getContext(), 15.0f), SmartisanLocaleUtils.dp(getContext(), 20.0f));
        this.mDaySpinner.setTextColor(normalColor, selectColor);
        SmartisanNumberPickerEx smartisanNumberPickerEx2 = (SmartisanNumberPickerEx) findViewById(R.id.smartisan_rom_month);
        this.mMonthSpinner = smartisanNumberPickerEx2;
        smartisanNumberPickerEx2.setMinValue(0);
        this.mMonthSpinner.setMaxValue(this.mNumberOfMonths - 1);
        this.mMonthSpinner.setDisplayedValues(this.mShortMonths);
        this.mMonthSpinner.setOnValueChangedListener(onChangeListener);
        this.mMonthSpinner.setTextSize(SmartisanLocaleUtils.dp(getContext(), 15.0f), SmartisanLocaleUtils.dp(getContext(), 20.0f));
        this.mMonthSpinner.setTextColor(normalColor, selectColor);
        SmartisanNumberPickerEx smartisanNumberPickerEx3 = (SmartisanNumberPickerEx) findViewById(R.id.smartisan_rom_year);
        this.mYearSpinner = smartisanNumberPickerEx3;
        smartisanNumberPickerEx3.setOnValueChangedListener(onChangeListener);
        this.mYearSpinner.setTextSize(SmartisanLocaleUtils.dp(getContext(), 15.0f), SmartisanLocaleUtils.dp(getContext(), 20.0f));
        this.mYearSpinner.setTextColor(normalColor, selectColor);
        boolean isChinese = "zh".equals(Locale.getDefault().getLanguage());
        if (isChinese) {
            this.mDaySpinner.setHightlightSuffix(context.getResources().getString(R.string.smartisan_rom_date_picker_day));
            this.mMonthSpinner.setHightlightSuffix(context.getResources().getString(R.string.smartisan_rom_date_picker_month));
            this.mYearSpinner.setHightlightSuffix(context.getResources().getString(R.string.smartisan_rom_date_picker_year));
        }
        setSpinnersShown(true);
        if (getImportantForAccessibility() == 0) {
            setImportantForAccessibility(1);
        }
        setBackgroundResource(R.drawable.smartisan_rom_time_picker_widget_bg_ex_new);
    }

    public void setMinDate(long minDate) {
        this.mTempDate.setTimeInMillis(minDate);
        if (this.mTempDate.get(1) == this.mMinDate.get(1) && this.mTempDate.get(6) != this.mMinDate.get(6)) {
            return;
        }
        this.mMinDate.setTimeInMillis(minDate);
        if (this.mCurrentDate.before(this.mMinDate)) {
            this.mCurrentDate.setTimeInMillis(this.mMinDate.getTimeInMillis());
        }
    }

    public void setMaxDate(long maxDate) {
        this.mTempDate.setTimeInMillis(maxDate);
        if (this.mTempDate.get(1) == this.mMaxDate.get(1) && this.mTempDate.get(6) != this.mMaxDate.get(6)) {
            return;
        }
        this.mMaxDate.setTimeInMillis(maxDate);
        if (this.mCurrentDate.after(this.mMaxDate)) {
            this.mCurrentDate.setTimeInMillis(this.mMaxDate.getTimeInMillis());
        }
    }

    @Override // android.view.View
    public void setEnabled(boolean enabled) {
        if (this.mIsEnabled == enabled) {
            return;
        }
        super.setEnabled(enabled);
        this.mDaySpinner.setEnabled(enabled);
        this.mMonthSpinner.setEnabled(enabled);
        this.mYearSpinner.setEnabled(enabled);
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
        event.setClassName(SmartisanDatePickerEx.class.getName());
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(SmartisanDatePickerEx.class.getName());
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setCurrentLocale(newConfig.locale);
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
        this.mMinDate = getCalendarForLocale(this.mMinDate, locale);
        this.mMaxDate = getCalendarForLocale(this.mMaxDate, locale);
        this.mCurrentDate = getCalendarForLocale(this.mCurrentDate, locale);
        this.mNumberOfMonths = this.mTempDate.getActualMaximum(2) + 1;
        this.mShortMonths = new DateFormatSymbols().getShortMonths();
        if (usingNumericMonths()) {
            this.mShortMonths = new String[this.mNumberOfMonths];
            for (int i = 0; i < this.mNumberOfMonths; i++) {
                this.mShortMonths[i] = String.format("%d", Integer.valueOf(i + 1));
            }
        }
    }

    private boolean usingNumericMonths() {
        return Character.isDigit(this.mShortMonths[0].charAt(0));
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

    private void reorderSpinners() {
        this.mSpinners.removeAllViews();
        String pattern = android.text.format.DateFormat.getBestDateTimePattern(
                Locale.getDefault(), "yyyyMMMdd");
        char[] order = SmartisanLocaleUtils.dateOrder(pattern);
        for (char c : order) {
            if (c == 'M') {
                this.mSpinners.addView(this.mMonthSpinner);
            } else if (c == 'd') {
                this.mSpinners.addView(this.mDaySpinner);
            } else if (c == 'y') {
                this.mSpinners.addView(this.mYearSpinner);
            } else {
                throw new IllegalArgumentException(Arrays.toString(order));
            }
        }
    }

    public void updateDate(int year, int month, int dayOfMonth) {
        if (!isNewDate(year, month, dayOfMonth)) {
            return;
        }
        setDate(year, month, dayOfMonth);
        updateSpinners();
        notifyDateChanged();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    @Override // android.view.View
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        DatePickerType pickerType = mPickerType != null ? mPickerType : DatePickerType.EVENT;
        return new SavedState(superState, getYear(), getMonth(), getDayOfMonth(),
                pickerType.ordinal());
    }

    @Override // android.view.View
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        DatePickerType[] pickerTypes = DatePickerType.values();
        int pickerTypeOrdinal = Math.max(0, Math.min(ss.mPickerType, pickerTypes.length - 1));
        mPickerType = pickerTypes[pickerTypeOrdinal];
        // R2 restores into the already-initialized picker. Calling init() here would silently
        // replace a caller-provided date range with the default event/birthday bounds.
        setDate(ss.mYear, ss.mMonth, ss.mDay);
        updateSpinners();
    }

    public void init(DatePickerType type, int year, int monthOfYear, int dayOfMonth, OnDateChangedListener onDateChangedListener) {
        this.mPickerType = type;
        if (type == DatePickerType.EVENT) {
            this.mTempDate.clear();
            this.mTempDate.set(1970, 0, 1);
            this.mMinDate.setTimeInMillis(this.mTempDate.getTimeInMillis());
            setMinDate(this.mTempDate.getTimeInMillis());
            this.mTempDate.clear();
            this.mTempDate.set(2037, 11, 31);
            this.mMaxDate.setTimeInMillis(this.mTempDate.getTimeInMillis());
            setMaxDate(this.mTempDate.getTimeInMillis());
            this.mCurrentDate.setTimeInMillis(System.currentTimeMillis());
            setDate(year, monthOfYear, dayOfMonth);
        } else {
            this.mTempDate.clear();
            this.mTempDate.set(1800, 0, 1);
            this.mMinDate.setTimeInMillis(this.mTempDate.getTimeInMillis());
            setMinDate(this.mTempDate.getTimeInMillis());
            long time = System.currentTimeMillis();
            this.mTempDate.clear();
            this.mTempDate.setTimeInMillis(time);
            this.mMaxDate.set(this.mTempDate.get(1), 11, 31);
            setMaxDate(this.mMaxDate.getTimeInMillis());
            this.mCurrentDate.setTimeInMillis(time);
            if (seemsUnsetYear(year, type)) {
                setDate(4, monthOfYear, dayOfMonth);
            } else {
                setDate(year, monthOfYear, dayOfMonth);
            }
        }
        updateSpinners();
        setSpinnersInitialValues();
        this.mOnDateChangedListener = onDateChangedListener;
    }

    private boolean parseDate(String date, Calendar outDate) {
        try {
            outDate.setTime(this.mDateFormat.parse(date));
            return true;
        } catch (ParseException e) {
            Log.w(LOG_TAG, "Date: " + date + " not in format: " + DATE_FORMAT);
            return false;
        }
    }

    private boolean isNewDate(int year, int month, int dayOfMonth) {
        return (this.mCurrentDate.get(1) == year && this.mCurrentDate.get(2) == dayOfMonth && this.mCurrentDate.get(5) == month) ? false : true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    private void setDate(int year, int month, int dayOfMonth) {
        this.mCurrentDate.set(year, month, dayOfMonth);
        if (year == 4) {
            return;
        }
        if (this.mCurrentDate.before(this.mMinDate)) {
            this.mCurrentDate.setTimeInMillis(this.mMinDate.getTimeInMillis());
        } else if (this.mCurrentDate.after(this.mMaxDate)) {
            this.mCurrentDate.setTimeInMillis(this.mMaxDate.getTimeInMillis());
        }
    }

    private void setSpinnersInitialValues() {
        this.mYearSpinner.setInitialValue(this.mCurrentDate.get(1));
        this.mMonthSpinner.setInitialValue(this.mCurrentDate.get(2));
        this.mDaySpinner.setInitialValue(this.mCurrentDate.get(5));
    }

    /* JADX INFO: Access modifiers changed from: private */
    private void updateSpinners() {
        if (this.mCurrentDate.equals(this.mMinDate)) {
            this.mDaySpinner.setMinValue(this.mCurrentDate.get(5));
            this.mDaySpinner.setMaxValue(this.mCurrentDate.getActualMaximum(5));
            this.mMonthSpinner.setDisplayedValues(null);
            this.mMonthSpinner.setMinValue(this.mCurrentDate.get(2));
            this.mMonthSpinner.setMaxValue(this.mCurrentDate.getActualMaximum(2));
        } else if (this.mCurrentDate.equals(this.mMaxDate)) {
            this.mDaySpinner.setMinValue(this.mCurrentDate.getActualMinimum(5));
            this.mDaySpinner.setMaxValue(this.mCurrentDate.get(5));
            this.mMonthSpinner.setDisplayedValues(null);
            this.mMonthSpinner.setMinValue(this.mCurrentDate.getActualMinimum(2));
            this.mMonthSpinner.setMaxValue(this.mCurrentDate.get(2));
        } else {
            this.mDaySpinner.setMinValue(1);
            this.mDaySpinner.setMaxValue(this.mCurrentDate.getActualMaximum(5));
            this.mMonthSpinner.setDisplayedValues(null);
            this.mMonthSpinner.setMinValue(0);
            this.mMonthSpinner.setMaxValue(11);
        }
        String[] displayedValues = (String[]) Arrays.copyOfRange(this.mShortMonths, this.mMonthSpinner.getMinValue(), this.mMonthSpinner.getMaxValue() + 1);
        this.mMonthSpinner.setDisplayedValues(displayedValues);
        this.mYearSpinner.setMinValue(this.mMinDate.get(1));
        if (AnonymousClass2.$SwitchMap$smartisanos$widget$SmartisanDatePickerEx$DatePickerType[this.mPickerType.ordinal()] == 1) {
            this.mYearSpinner.setMaxValue(this.mMaxDate.get(1), true);
        } else {
            this.mYearSpinner.setMaxValue(this.mMaxDate.get(1));
        }
        this.mYearSpinner.setValue(this.mCurrentDate.get(1));
        this.mMonthSpinner.setValue(this.mCurrentDate.get(2));
        this.mDaySpinner.setValue(this.mCurrentDate.get(5));
    }

    /* JADX INFO: renamed from: smartisanos.widget.SmartisanDatePickerEx$2, reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$smartisanos$widget$SmartisanDatePickerEx$DatePickerType;

        static {
            int[] iArr = new int[DatePickerType.values().length];
            $SwitchMap$smartisanos$widget$SmartisanDatePickerEx$DatePickerType = iArr;
            try {
                iArr[DatePickerType.BIRTHDAY.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$smartisanos$widget$SmartisanDatePickerEx$DatePickerType[DatePickerType.EVENT.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    public int getYear() {
        return this.mCurrentDate.get(1);
    }

    public int getMonth() {
        return this.mCurrentDate.get(2);
    }

    public int getDayOfMonth() {
        return this.mCurrentDate.get(5);
    }

    /* JADX INFO: Access modifiers changed from: private */
    private void notifyDateChanged() {
        sendAccessibilityEvent(4);
        OnDateChangedListener onDateChangedListener = this.mOnDateChangedListener;
        if (onDateChangedListener != null) {
            onDateChangedListener.onDateChanged(this, getYear(), getMonth(), getDayOfMonth(), this.mPickerType);
        }
    }

    private void trySetContentDescription(View root, int viewId, int contDescResId) {
        View target = root.findViewById(viewId);
        if (target != null) {
            target.setContentDescription(getContext().getString(contDescResId));
        }
    }

    public static boolean seemsUnsetYear(int year, DatePickerType type) {
        if (type == DatePickerType.BIRTHDAY) {
            if (year < 1800) {
                return true;
            }
            Calendar cal = Calendar.getInstance(Locale.getDefault());
            cal.clear();
            cal.setTimeInMillis(System.currentTimeMillis());
            return year > cal.get(1);
        }
        return false;
    }

    private static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() { // from class: smartisanos.widget.SmartisanDatePickerEx.SavedState.1
            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        private final int mDay;
        private final int mMonth;
        private final int mPickerType;
        private final int mYear;

        private SavedState(Parcelable superState, int year, int month, int day,
                int pickerType) {
            super(superState);
            this.mYear = year;
            this.mMonth = month;
            this.mDay = day;
            this.mPickerType = pickerType;
        }

        private SavedState(Parcel in) {
            super(in);
            this.mYear = in.readInt();
            this.mMonth = in.readInt();
            this.mDay = in.readInt();
            this.mPickerType = in.readInt();
        }

        @Override // android.view.View.BaseSavedState, android.view.AbsSavedState, android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.mYear);
            dest.writeInt(this.mMonth);
            dest.writeInt(this.mDay);
            dest.writeInt(this.mPickerType);
        }
    }
}
