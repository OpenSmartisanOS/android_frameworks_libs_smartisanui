package org.opensmartisanos.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;

import org.opensmartisanos.ui.R;

import java.util.Calendar;

/** Original five-row Smartisan date picker dialog. */
@Deprecated
public class SmartisanDatePickerDialog extends Dialog
        implements SmartisanDatePicker.OnDateChangedListener, View.OnClickListener {
    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DAY = "day";
    private static final String TYPE = "type";

    public interface OnDateSetListener {
        void onDateSet(SmartisanDatePicker view, int year, int monthOfYear, int dayOfMonth);
    }

    private final Calendar calendar = Calendar.getInstance();
    private final OnDateSetListener callback;
    private final SmartisanDatePicker datePicker;
    private final SmartisanDialogTitleBar titleBar;
    private SmartisanDatePicker.DatePickerType pickerType;

    public SmartisanDatePickerDialog(Context context, OnDateSetListener callback,
            int year, int monthOfYear, int dayOfMonth) {
        this(context, callback, year, monthOfYear, dayOfMonth,
                SmartisanDatePicker.DatePickerType.EVENT);
    }

    public SmartisanDatePickerDialog(Context context, OnDateSetListener callback,
            int year, int monthOfYear, int dayOfMonth,
            SmartisanDatePicker.DatePickerType type) {
        super(context, R.style.Theme_SmartisanUi_PickerDialog);
        this.callback = callback;
        pickerType = type == null ? SmartisanDatePicker.DatePickerType.EVENT : type;
        setContentView(R.layout.smartisan_rom_date_picker_dialog);
        titleBar = findViewById(R.id.smartisan_rom_menu_dialog_title_bar);
        datePicker = findViewById(R.id.smartisan_rom_date_picker);
        datePicker.init(pickerType, year, monthOfYear, dayOfMonth, this);
        updateTitle(year, monthOfYear, dayOfMonth);
        SmartisanPickerDialogSupport.bindActions(titleBar, this, this);
        setCanceledOnTouchOutside(true);
        SmartisanPickerDialogSupport.configure(this, false);
        SmartisanPickerDialogSupport.fitDatePickerWindowAfterMeasure(this, titleBar, datePicker,
                R.drawable.smartisan_rom_time_picker_widget_bottom);
    }

    @Override protected void onStart() {
        super.onStart();
        SmartisanPickerDialogSupport.configure(this, false);
    }

    @Override protected void onStop() {
        datePicker.clearFocus();
        super.onStop();
    }

    @Override public void onDateChanged(SmartisanDatePicker view, int year, int month,
            int day, SmartisanDatePicker.DatePickerType type) {
        pickerType = type;
        updateTitle(year, month, day);
    }

    @Override public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.smartisan_rom_menu_dialog_cancel_right) {
            tryNotifyDateSet();
            dismiss();
        } else if (id == R.id.smartisan_rom_menu_dialog_cancel_left) {
            dismiss();
        }
    }

    public SmartisanDatePicker getDatePicker() { return datePicker; }

    public void updateDate(int year, int monthOfYear, int dayOfMonth) {
        datePicker.updateDate(year, monthOfYear, dayOfMonth);
    }

    private void tryNotifyDateSet() {
        datePicker.clearFocus();
        if (callback != null) {
            callback.onDateSet(datePicker, datePicker.getYear(), datePicker.getMonth(),
                    datePicker.getDayOfMonth());
        }
    }

    private void updateTitle(int year, int month, int day) {
        calendar.clear();
        calendar.set(year, month, day);
        int flags = SmartisanDatePicker.seemsUnsetYear(year, pickerType) ? 65560 : 98326;
        titleBar.setTitle(DateUtils.formatDateTime(getContext(), calendar.getTimeInMillis(), flags));
    }

    @Override public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(YEAR, datePicker.getYear());
        state.putInt(MONTH, datePicker.getMonth());
        state.putInt(DAY, datePicker.getDayOfMonth());
        state.putInt(TYPE, pickerType.ordinal());
        return state;
    }

    @Override public void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        int ordinal = Math.max(0, Math.min(SmartisanDatePicker.DatePickerType.values().length - 1,
                state.getInt(TYPE, pickerType.ordinal())));
        pickerType = SmartisanDatePicker.DatePickerType.values()[ordinal];
        datePicker.init(pickerType, state.getInt(YEAR), state.getInt(MONTH),
                state.getInt(DAY), this);
        updateTitle(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
    }
}
