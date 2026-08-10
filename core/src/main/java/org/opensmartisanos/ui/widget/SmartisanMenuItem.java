package org.opensmartisanos.ui.widget;

import android.widget.ImageView;

/** Data contract used by the standard popup-menu adapter. */
public interface SmartisanMenuItem {
    String getTitle();
    boolean hasMenuIcon();
    boolean isSelected();
    void setMenuIcon(ImageView imageView);
}
