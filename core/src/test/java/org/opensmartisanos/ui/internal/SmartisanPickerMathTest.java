package org.opensmartisanos.ui.internal;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SmartisanPickerMathTest {
    @Test public void leapYearAndCenturyRulesMatchCalendar() {
        assertEquals(29, SmartisanPickerMath.daysInMonth(2000, 1));
        assertEquals(28, SmartisanPickerMath.daysInMonth(1900, 1));
        assertEquals(29, SmartisanPickerMath.daysInMonth(2024, 1));
    }

    @Test public void invalidMonthEndReturnsOriginalFirstDayRule() {
        assertEquals(1, SmartisanPickerMath.normalizeDayOrFirst(31, 30));
        assertEquals(30, SmartisanPickerMath.normalizeDayOrFirst(30, 30));
    }

    @Test public void selectorWrapKeepsOriginalBoundarySemantics() {
        assertEquals(1, SmartisanPickerMath.wrapSelectorIndex(13, 1, 12));
        assertEquals(12, SmartisanPickerMath.wrapSelectorIndex(0, 1, 12));
        assertEquals(7, SmartisanPickerMath.wrapSelectorIndex(7, 1, 12));
    }
}
