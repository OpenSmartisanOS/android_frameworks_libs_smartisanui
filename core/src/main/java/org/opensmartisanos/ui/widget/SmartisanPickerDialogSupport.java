package org.opensmartisanos.ui.widget;

import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;

/** Shared public-SDK window behavior used by the original picker-dialog family. */
final class SmartisanPickerDialogSupport {
    private SmartisanPickerDialogSupport() { }

    static void bindActions(SmartisanDialogTitleBar titleBar,
            View.OnClickListener cancel, View.OnClickListener complete) {
        titleBar.setLeftButtonVisibility(View.VISIBLE);
        titleBar.setRightButtonVisibility(View.VISIBLE);
        titleBar.addCancelImage(true);
        titleBar.addCompleteImage(false);
        titleBar.setOnLeftButtonClickListener(cancel);
        titleBar.setOnRightButtonClickListener(complete);
    }

    static void configure(Dialog dialog, boolean supportsExternalDisplay) {
        Window window = dialog.getWindow();
        if (window == null) return;
        window.setBackgroundDrawableResource(android.R.color.transparent);
        if (supportsExternalDisplay
                && SmartisanLocaleUtils.isExternalDisplay(dialog.getContext())) {
            window.setGravity(Gravity.CENTER);
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
        } else {
            window.setGravity(Gravity.BOTTOM);
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    static void fitDatePickerWindowAfterMeasure(Dialog dialog, View titleBar, View picker,
            int bottomDrawableResource) {
        if (Build.VERSION.SDK_INT < 26
                || SmartisanLocaleUtils.isExternalDisplay(dialog.getContext())) {
            return;
        }
        Drawable bottom = dialog.getContext().getDrawable(bottomDrawableResource);
        picker.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override public boolean onPreDraw() {
                picker.getViewTreeObserver().removeOnPreDrawListener(this);
                Window window = dialog.getWindow();
                if (window != null) {
                    window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                            titleBar.getMeasuredHeight() + picker.getMeasuredHeight()
                                    + bottom.getIntrinsicHeight());
                }
                return false;
            }
        });
    }
}
