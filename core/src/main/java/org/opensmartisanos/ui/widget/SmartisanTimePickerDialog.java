package org.opensmartisanos.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Outline;
import android.os.Bundle;
import android.view.View;
import android.view.ViewOutlineProvider;

import org.opensmartisanos.ui.R;

/** Original Smartisan time picker dialog. */
public class SmartisanTimePickerDialog extends Dialog implements View.OnClickListener {
    private static final String HOUR = "hour";
    private static final String MINUTE = "minute";
    private static final String IS_24_HOUR = "is24hour";

    public interface OnTimeSetListener {
        void onTimeSet(SmartisanTimePicker view, int hourOfDay, int minute);
    }

    private final OnTimeSetListener callback;
    private final SmartisanTimePicker timePicker;
    private final SmartisanDialogTitleBar titleBar;

    public SmartisanTimePickerDialog(Context context, OnTimeSetListener callback,
            int hourOfDay, int minute, boolean is24HourView) {
        super(context, R.style.Theme_SmartisanUi_PickerDialog);
        this.callback = callback;
        boolean externalDisplay = SmartisanLocaleUtils.isExternalDisplay(context);
        setContentView(externalDisplay
                ? R.layout.smartisan_rom_revone_time_picker_dialog
                : R.layout.smartisan_rom_time_picker_dialog);
        titleBar = findViewById(R.id.smartisan_rom_menu_dialog_title_bar);
        timePicker = findViewById(R.id.smartisan_rom_time_picker);
        timePicker.setIs24HourView(is24HourView);
        timePicker.setCurrentHour(hourOfDay);
        timePicker.setCurrentMinute(minute);
        SmartisanPickerDialogSupport.bindActions(titleBar, this, this);
        setCanceledOnTouchOutside(true);
        if (externalDisplay) {
            View container = findViewById(R.id.smartisan_rom_picker_container);
            if (container != null) {
                container.setBackgroundResource(R.drawable.smartisan_rom_revone_dialog_bottom_bg);
                container.setOutlineProvider(new ViewOutlineProvider() {
                    @Override public void getOutline(View view, Outline outline) {
                        outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(),
                                SmartisanLocaleUtils.dp(context, 4));
                    }
                });
                container.setClipToOutline(true);
            }
        }
        SmartisanPickerDialogSupport.configure(this, true);
    }

    @Override protected void onStart() {
        super.onStart();
        SmartisanPickerDialogSupport.configure(this, true);
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

    public SmartisanTimePicker getTimePicker() { return timePicker; }

    @Override public void setTitle(CharSequence title) {
        if (titleBar != null) titleBar.setTitle(title);
    }

    @Override public void setTitle(int titleId) {
        if (titleBar != null) titleBar.setTitle(titleId);
    }

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
