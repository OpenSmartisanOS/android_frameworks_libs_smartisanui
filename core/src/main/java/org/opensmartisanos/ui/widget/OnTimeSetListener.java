package org.opensmartisanos.ui.widget;

/** Receives the millisecond value selected by {@link SmartisanDateTimePickerDialog}. */
@FunctionalInterface
public interface OnTimeSetListener {
    void onTimeSet(long timeMillis);
}
