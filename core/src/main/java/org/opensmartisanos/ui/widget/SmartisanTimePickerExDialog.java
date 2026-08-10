package org.opensmartisanos.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import org.opensmartisanos.ui.R;

/** Extended Smartisan time picker dialog. */
public class SmartisanTimePickerExDialog extends Dialog implements View.OnClickListener {
    private static final String HOUR = "hour";
    private static final String MINUTE = "minute";
    private static final String IS_24_HOUR = "is24hour";

    public interface OnTimeSetListener {
        void onTimeSet(SmartisanTimePickerEx view, int hourOfDay, int minute);
    }

    private final OnTimeSetListener callback;
    private final SmartisanTimePickerEx timePicker;

    public SmartisanTimePickerExDialog(Context context, OnTimeSetListener callback,
            int hourOfDay, int minute, boolean is24HourView) {
        super(context, R.style.Theme_SmartisanUi_PickerDialog);
        this.callback = callback;
        setContentView(R.layout.smartisan_rom_time_picker_ex_dialog);
        SmartisanDialogTitleBar titleBar = findViewById(R.id.smartisan_rom_menu_dialog_title_bar);
        timePicker = findViewById(R.id.smartisan_rom_time_picker);
        timePicker.setIs24HourView(is24HourView);
        timePicker.setCurrentHour(hourOfDay);
        timePicker.setCurrentMinute(minute);
        timePicker.setSpinnersInitialValues();
        SmartisanPickerDialogSupport.bindActions(titleBar, this, this);
        setCanceledOnTouchOutside(true);
        SmartisanPickerDialogSupport.configure(this, false);
    }

    @Override protected void onStart() {
        super.onStart();
        SmartisanPickerDialogSupport.configure(this, false);
    }

    @Override protected void onStop() {
        timePicker.clearFocus();
        super.onStop();
    }

    @Override public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.smartisan_rom_menu_dialog_cancel_right) {
            tryNotifyTimeSet();
            dismiss();
        } else if (id == R.id.smartisan_rom_menu_dialog_cancel_left) {
            dismiss();
        }
    }

    private void tryNotifyTimeSet() {
        timePicker.clearFocus();
        if (callback != null) callback.onTimeSet(timePicker, timePicker.getCurrentHour(),
                timePicker.getCurrentMinute());
    }

    public void updateTime(int hourOfDay, int minuteOfHour) {
        timePicker.setCurrentHour(hourOfDay);
        timePicker.setCurrentMinute(minuteOfHour);
    }

    public SmartisanTimePickerEx getTimePicker() { return timePicker; }

    @Override public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(HOUR, timePicker.getCurrentHour());
        state.putInt(MINUTE, timePicker.getCurrentMinute());
        state.putBoolean(IS_24_HOUR, timePicker.is24HourView());
        return state;
    }

    @Override public void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        timePicker.setIs24HourView(state.getBoolean(IS_24_HOUR));
        timePicker.setCurrentHour(state.getInt(HOUR));
        timePicker.setCurrentMinute(state.getInt(MINUTE));
    }
}
