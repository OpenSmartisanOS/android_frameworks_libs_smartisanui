package org.opensmartisanos.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;

import org.opensmartisanos.ui.R;

import java.util.Calendar;

/** Extended five-row date picker dialog used by the later Smartisan UI. */
public class SmartisanDatePickerExDialog extends Dialog
        implements SmartisanDatePickerEx.OnDateChangedListener, View.OnClickListener {
    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DAY = "day";
    private static final String TYPE = "type";

    public interface OnDateSetListener {
        void onDateSet(SmartisanDatePickerEx view, int year, int monthOfYear, int dayOfMonth);
    }

    private final Calendar calendar = Calendar.getInstance();
    private final OnDateSetListener callback;
    private final SmartisanDatePickerEx datePicker;
    private final SmartisanDialogTitleBar titleBar;
    private SmartisanDatePickerEx.DatePickerType pickerType;

    public SmartisanDatePickerExDialog(Context context, OnDateSetListener callback,
            int year, int monthOfYear, int dayOfMonth) {
        this(context, callback, year, monthOfYear, dayOfMonth,
                SmartisanDatePickerEx.DatePickerType.EVENT);
    }

    public SmartisanDatePickerExDialog(Context context, OnDateSetListener callback,
            int year, int monthOfYear, int dayOfMonth,
            SmartisanDatePickerEx.DatePickerType type) {
        super(context, R.style.Theme_SmartisanUi_PickerDialog);
        this.callback = callback;
        pickerType = type == null ? SmartisanDatePickerEx.DatePickerType.EVENT : type;
        setContentView(R.layout.smartisan_rom_date_picker_ex_dialog);
        titleBar = findViewById(R.id.smartisan_rom_menu_dialog_title_bar);
        datePicker = findViewById(R.id.smartisan_rom_date_picker);
        datePicker.init(pickerType, year, monthOfYear, dayOfMonth, this);
        updateTitle(year, monthOfYear, dayOfMonth);
        SmartisanPickerDialogSupport.bindActions(titleBar, this, this);
        setCanceledOnTouchOutside(true);
        if (SmartisanLocaleUtils.isExternalDisplay(context)) {
            datePicker.setBackgroundResource(R.drawable.smartisan_rom_revone_dialog_bottom_bg);
        }
        SmartisanPickerDialogSupport.configure(this, true);
    }

    @Override protected void onStart() {
        super.onStart();
        SmartisanPickerDialogSupport.configure(this, true);
    }

    @Override protected void onStop() {
        datePicker.clearFocus();
        super.onStop();
    }

    @Override public void onDateChanged(SmartisanDatePickerEx view, int year, int month,
            int day, SmartisanDatePickerEx.DatePickerType type) {
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

    public SmartisanDatePickerEx getDatePicker() { return datePicker; }

    public void updateDate(int year, int monthOfYear, int dayOfMonth) {
        datePicker.updateDate(year, monthOfYear, dayOfMonth);
    }

    public void setRightImageRes(int resourceId) { titleBar.setRightImageRes(resourceId); }
    public void setLeftImageRes(int resourceId) { titleBar.setLeftImageViewRes(resourceId); }
    @Deprecated public void setRightButtonText(CharSequence text) { titleBar.setRightButtonText(text); }
    @Deprecated public void setRightButtonText(int resourceId) { setRightButtonText(getContext().getText(resourceId)); }
    @Deprecated public void setLeftButtonText(CharSequence text) { titleBar.setLeftButtonText(text); }
    @Deprecated public void setLeftButtonText(int resourceId) { setLeftButtonText(getContext().getText(resourceId)); }

    private void tryNotifyDateSet() {
        datePicker.clearFocus();
        if (callback != null) callback.onDateSet(datePicker, datePicker.getYear(),
                datePicker.getMonth(), datePicker.getDayOfMonth());
    }

    private void updateTitle(int year, int month, int day) {
        calendar.clear();
        calendar.set(year, month, day);
        int flags = SmartisanDatePickerEx.seemsUnsetYear(year, pickerType) ? 65560 : 98326;
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
        int ordinal = Math.max(0, Math.min(SmartisanDatePickerEx.DatePickerType.values().length - 1,
                state.getInt(TYPE, pickerType.ordinal())));
        pickerType = SmartisanDatePickerEx.DatePickerType.values()[ordinal];
        datePicker.init(pickerType, state.getInt(YEAR), state.getInt(MONTH),
                state.getInt(DAY), this);
        updateTitle(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
    }
}
