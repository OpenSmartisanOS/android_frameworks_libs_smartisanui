package org.opensmartisanos.ui.internal;

import android.content.res.Configuration;

/** Deterministic menu geometry used by the public-SDK popup implementations. */
public final class SmartisanMenuMath {
    private static final int MAX_GRID_ROWS = 3;

    private SmartisanMenuMath() {}

    public static int defaultBottomMenuColumns(int orientation) {
        return orientation == Configuration.ORIENTATION_LANDSCAPE ? 3 : 4;
    }

    public static int gridPageCount(int iconCount, int columns) {
        if (iconCount <= 0 || columns <= 0) return 0;
        int pageSize = columns * MAX_GRID_ROWS;
        return (iconCount + pageSize - 1) / pageSize;
    }
}
