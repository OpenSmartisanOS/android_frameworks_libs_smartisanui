package org.opensmartisanos.ui.widget;

import android.widget.ImageView;

/** Convenience base matching the original popup-menu item defaults. */
public abstract class SmartisanAbsMenuItem implements SmartisanMenuItem {
    @Override public abstract String getTitle();
    @Override public boolean hasMenuIcon() { return true; }
    @Override public void setMenuIcon(ImageView imageView) { }
    @Override public boolean isSelected() { return false; }
    public String getSubtitle() { return null; }
}
