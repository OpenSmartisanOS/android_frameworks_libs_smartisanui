package org.opensmartisanos.ui.internal;

import java.util.Calendar;

/** Pure Java helpers shared by the ROM picker ports and host tests. */
public final class SmartisanPickerMath {
    private SmartisanPickerMath() {}

    public static int normalizeDayOrFirst(int requestedDay, int maximumDay) {
        return requestedDay <= maximumDay ? requestedDay : 1;
    }

    public static int daysInMonth(int year, int zeroBasedMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, zeroBasedMonth, 1);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public static int wrapSelectorIndex(int selectorIndex, int minimum, int maximum) {
        if (selectorIndex > maximum) {
            return (minimum + ((selectorIndex - maximum) % maximum)) - 1;
        }
        if (selectorIndex < minimum) {
            return (maximum - ((minimum - selectorIndex) % (maximum - minimum))) + 1;
        }
        return selectorIndex;
    }
}
