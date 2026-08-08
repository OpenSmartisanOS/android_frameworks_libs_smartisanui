/* Copyright (C) 2026 OpenSmartisanOS. Licensed under the Apache License, Version 2.0. */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.widget.Spinner;

import org.opensmartisanos.ui.R;

/**
 * OpenSmartisanOS platform Spinner adapter using Smartisan editor and popup surfaces.
 * This class is project glue, not a source port of {@code SmartisanSpinnerView}.
 */
public class SmartisanSpinner extends Spinner {
    private static final int MODE_THEME = -1;

    public SmartisanSpinner(Context context) {
        this(context, null);
    }

    public SmartisanSpinner(Context context, int mode) {
        this(context, null, R.attr.smartisanSpinnerStyle,
                R.style.Widget_SmartisanUi_Spinner, mode, null);
    }

    public SmartisanSpinner(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.smartisanSpinnerStyle);
    }

    public SmartisanSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Widget_SmartisanUi_Spinner,
                MODE_THEME, null);
    }

    public SmartisanSpinner(Context context, AttributeSet attrs, int defStyleAttr, int mode) {
        this(context, attrs, defStyleAttr, R.style.Widget_SmartisanUi_Spinner,
                mode, null);
    }

    public SmartisanSpinner(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes, int mode) {
        this(context, attrs, defStyleAttr, defStyleRes, mode, null);
    }

    public SmartisanSpinner(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes, int mode, Resources.Theme popupTheme) {
        super(context, attrs, defStyleAttr, defStyleRes, mode, popupTheme);
    }
}
