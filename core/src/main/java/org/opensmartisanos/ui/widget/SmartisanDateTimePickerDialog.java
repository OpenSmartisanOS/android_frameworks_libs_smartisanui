package org.opensmartisanos.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import org.opensmartisanos.ui.R;

import java.util.Calendar;

/** Combined extended date/time picker dialog. */
public class SmartisanDateTimePickerDialog extends Dialog implements View.OnClickListener {
    /** @deprecated Use the original {@link OnTimeSetListener}. */
    @Deprecated
    @FunctionalInterface
    public interface OnDateTimeSetListener extends OnTimeSetListener {
        void onDateTimeSet(long timeMillis);

        @Override default void onTimeSet(long timeMillis) {
            onDateTimeSet(timeMillis);
        }
    }

    private static final String CURRENT = "current";
    private static final String MINIMUM = "minimum";
    private static final String MAXIMUM = "maximum";

    private final OnTimeSetListener callback;
    private final SmartisanDateTimePicker dateTimePicker;
    private final SmartisanDialogTitleBar titleBar;

    public SmartisanDateTimePickerDialog(Context context, OnTimeSetListener callback,
            long currentTimeMillis) {
        this(context, callback, currentTimeMillis, currentTimeMillis, maxTimeMillis());
    }

    public SmartisanDateTimePickerDialog(Context context, OnTimeSetListener callback,
            long currentTimeMillis, long minimumTimeMillis, long maximumTimeMillis) {
        super(context, R.style.Theme_SmartisanUi_PickerDialog);
        this.callback = callback;
        setContentView(R.layout.smartisan_rom_date_time_picker_dialog);
        titleBar = findViewById(R.id.smartisan_rom_menu_dialog_title_bar_parent);
        dateTimePicker = findViewById(R.id.smartisan_rom_date_time_picker);
        dateTimePicker.init(currentTimeMillis, minimumTimeMillis, maximumTimeMillis, null);
        SmartisanPickerDialogSupport.bindActions(titleBar, this, this);
        setCanceledOnTouchOutside(true);
        if (SmartisanLocaleUtils.isExternalDisplay(context)) {
            dateTimePicker.setBackgroundResource(R.drawable.smartisan_rom_revone_dialog_bottom_bg);
        }
        SmartisanPickerDialogSupport.configure(this, true);
    }

    private static long maxTimeMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(2037, Calendar.DECEMBER, 31, 0, 0, 0);
        return calendar.getTimeInMillis();
    }

    @Override protected void onStart() {
        super.onStart();
        SmartisanPickerDialogSupport.configure(this, true);
    }

    @Override protected void onStop() {
        dateTimePicker.clearFocus();
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
        dateTimePicker.clearFocus();
        if (callback != null) callback.onTimeSet(dateTimePicker.getCurrentMills());
    }

    public SmartisanDateTimePicker getDateTimePicker() { return dateTimePicker; }
    public void updateTitle(String title) { titleBar.setTitle(title); }
    public void setRightImageRes(int resourceId) { titleBar.setRightImageRes(resourceId); }
    public void setLeftImageRes(int resourceId) { titleBar.setLeftImageViewRes(resourceId); }
    @Deprecated public void setRightButtonText(CharSequence text) { titleBar.setRightButtonText(text); }
    @Deprecated public void setRightButtonText(int resourceId) { setRightButtonText(getContext().getText(resourceId)); }
    @Deprecated public void setLeftButtonText(CharSequence text) { titleBar.setLeftButtonText(text); }
    @Deprecated public void setLeftButtonText(int resourceId) { setLeftButtonText(getContext().getText(resourceId)); }

    @Override public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putLong(CURRENT, dateTimePicker.getCurrentMills());
        state.putLong(MINIMUM, dateTimePicker.getMinDate());
        state.putLong(MAXIMUM, dateTimePicker.getMaxDate());
        return state;
    }

    @Override public void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        dateTimePicker.init(state.getLong(CURRENT), state.getLong(MINIMUM),
                state.getLong(MAXIMUM), null);
    }
}
