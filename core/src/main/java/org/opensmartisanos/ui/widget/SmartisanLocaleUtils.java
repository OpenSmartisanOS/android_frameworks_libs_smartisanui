package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

import java.util.Locale;

/** Package-local compatibility helpers for the public-SDK picker family. */
final class SmartisanLocaleUtils {
    private SmartisanLocaleUtils() { }

    @SuppressWarnings("deprecation")
    static Locale primaryLocale(Configuration configuration) {
        if (Build.VERSION.SDK_INT >= 24) {
            return configuration.getLocales().get(0);
        }
        return configuration.locale;
    }

    static int dp(Context context, float value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    static char[] dateOrder(String pattern) {
        StringBuilder order = new StringBuilder(3);
        boolean quoted = false;
        for (int i = 0; i < pattern.length(); i++) {
            char value = pattern.charAt(i);
            if (value == '\'') {
                if (i + 1 < pattern.length() && pattern.charAt(i + 1) == '\'') {
                    i++;
                } else {
                    quoted = !quoted;
                }
                continue;
            }
            if (quoted) continue;
            char normalized;
            if (value == 'd') normalized = 'd';
            else if (value == 'M' || value == 'L') normalized = 'M';
            else if (value == 'y') normalized = 'y';
            else continue;
            if (order.indexOf(String.valueOf(normalized)) < 0) order.append(normalized);
        }
        for (char fallback : new char[]{'y', 'M', 'd'}) {
            if (order.indexOf(String.valueOf(fallback)) < 0) order.append(fallback);
        }
        return order.toString().toCharArray();
    }

    @SuppressWarnings("deprecation")
    static boolean isExternalDisplay(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager == null ? null : manager.getDefaultDisplay();
        return display != null && display.getDisplayId() != Display.DEFAULT_DISPLAY;
    }
}
