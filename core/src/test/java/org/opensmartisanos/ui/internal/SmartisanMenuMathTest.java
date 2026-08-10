package org.opensmartisanos.ui.internal;

import static org.junit.Assert.assertEquals;

import android.content.res.Configuration;

import org.junit.Test;

public class SmartisanMenuMathTest {
    @Test public void bottomMenuColumnsFollowOriginalOrientationRule() {
        assertEquals(4, SmartisanMenuMath.defaultBottomMenuColumns(
                Configuration.ORIENTATION_PORTRAIT));
        assertEquals(3, SmartisanMenuMath.defaultBottomMenuColumns(
                Configuration.ORIENTATION_LANDSCAPE));
    }

    @Test public void gridUsesThreeRowsPerPage() {
        assertEquals(0, SmartisanMenuMath.gridPageCount(0, 4));
        assertEquals(1, SmartisanMenuMath.gridPageCount(12, 4));
        assertEquals(2, SmartisanMenuMath.gridPageCount(13, 4));
        assertEquals(2, SmartisanMenuMath.gridPageCount(19, 4));
    }
}
