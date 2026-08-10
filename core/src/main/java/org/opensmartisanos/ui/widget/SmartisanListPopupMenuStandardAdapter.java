package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import org.opensmartisanos.ui.R;

import java.util.List;

/** Standard selected/removable popup-menu adapter. */
public class SmartisanListPopupMenuStandardAdapter extends ArrayAdapter<SmartisanMenuItem> {
    public static final int STYLE_SELECTED = 1;
    public static final int STYLE_REMOVABLE = 2;
    private int itemStyle = STYLE_SELECTED;
    private boolean closeIconVisible = true;
    private View.OnClickListener closeIconClickListener;
    private final View.OnHoverListener hoverListener = (view, event) -> {
        if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
            view.setBackgroundResource(R.drawable.smartisan_rom_revone_list_popup_menu_pressed);
        } else if (event.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
            view.setBackgroundResource(R.drawable.smartisan_rom_menu_list_item_normal);
        }
        return false;
    };

    public SmartisanListPopupMenuStandardAdapter(Context context, List<SmartisanMenuItem> items) {
        super(context, 0, items);
    }
    public SmartisanListPopupMenuStandardAdapter(Context context, int resource,
            List<SmartisanMenuItem> items) { super(context, resource, items); }
    public SmartisanListPopupMenuStandardAdapter(Context context, SmartisanMenuItem[] items) {
        super(context, 0, items);
    }
    public SmartisanListPopupMenuStandardAdapter(Context context, int resource,
            SmartisanMenuItem[] items) { super(context, resource, items); }

    public void setMenuItemStyle(int style) {
        itemStyle = style;
    }
    public void setCloseIconClickListener(View.OnClickListener listener) { closeIconClickListener = listener; }
    public void setCloseIconVisible(boolean visible) { closeIconVisible = visible; }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        return itemStyle == STYLE_REMOVABLE
                ? removableView(position, convertView) : selectedView(position, convertView);
    }

    private View selectedView(int position, View convertView) {
        SmartisanPopupMenuStandardListItem view = convertView instanceof SmartisanPopupMenuStandardListItem
                ? (SmartisanPopupMenuStandardListItem) convertView
                : new SmartisanPopupMenuStandardListItem(getContext());
        if (convertView == null && SmartisanLocaleUtils.isExternalDisplay(getContext())) {
            view.setOnHoverListener(hoverListener);
        }
        SmartisanMenuItem item = getItem(position);
        view.setEnabled(isEnabled(position));
        if (item != null) {
            if (item.hasMenuIcon()) {
                view.getIconView().setVisibility(View.VISIBLE);
                item.setMenuIcon(view.getIconView());
            }
            else view.setMenuIcon((Drawable) null);
            view.setChecked(item.isSelected());
            view.setMenuTitle(item.getTitle());
            if (item instanceof SmartisanAbsMenuItem) {
                String subtitle = ((SmartisanAbsMenuItem) item).getSubtitle();
                if (subtitle != null) view.setMenuSubtitle(subtitle);
            }
        }
        return view;
    }

    private View removableView(int position, View convertView) {
        SmartisanPopupMenuRemovableListItem view = convertView instanceof SmartisanPopupMenuRemovableListItem
                ? (SmartisanPopupMenuRemovableListItem) convertView
                : new SmartisanPopupMenuRemovableListItem(getContext());
        view.setCloseIconVisibility(closeIconVisible ? View.VISIBLE : View.GONE);
        SmartisanMenuItem item = getItem(position);
        if (item != null) {
            if (item.hasMenuIcon()) {
                view.getIconView().setVisibility(View.VISIBLE);
                item.setMenuIcon(view.getIconView());
            }
            else view.setMenuIcon((Drawable) null);
            view.getCloseIconView().setTag(item);
            view.setMenuTitle(item.getTitle());
            view.setOnCloseIconClickListener(closeIconClickListener);
        }
        return view;
    }
}
