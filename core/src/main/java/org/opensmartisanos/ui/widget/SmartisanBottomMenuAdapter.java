package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.util.TypedValue;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.opensmartisanos.ui.R;

import java.util.Collections;
import java.util.List;

/** MenuItem adapter used by {@link SmartisanBottomMenuPopupWindow}. */
public class SmartisanBottomMenuAdapter extends BaseAdapter {
    public static final int TOP_ITEM = 0;
    public static final int RIGHT_BOTTOTM_ITEM = 1;
    public static final int MIDLE_BOTTOM_ITEM = 2;

    /** @deprecated Kept as a correctly-spelled alias for the original R2 constant. */
    @Deprecated public static final int RIGHT_BOTTOM_ITEM = RIGHT_BOTTOTM_ITEM;
    /** @deprecated Kept as a correctly-spelled alias for the original R2 constant. */
    @Deprecated public static final int MIDDLE_BOTTOM_ITEM = MIDLE_BOTTOM_ITEM;

    public int mType;

    public interface ItemPressStateListener {
        void onItemPressStateChanged(int position, boolean pressed);
    }

    private final LayoutInflater inflater;
    private final List<? extends MenuItem> items;
    private int childHeight;

    public SmartisanBottomMenuAdapter(Context context, List<? extends MenuItem> items) {
        inflater = LayoutInflater.from(context);
        // R2 keeps the caller's list. This is intentional: callers may update MenuItem state and
        // call notifyDataSetChanged() without replacing the adapter.
        this.items = items == null ? Collections.emptyList() : items;
    }

    @Override public int getCount() { return items.size(); }
    @Override public MenuItem getItem(int position) { return items.get(position); }
    @Override public long getItemId(int position) { return position; }
    @Override public boolean isEnabled(int position) { return getItem(position).isEnabled(); }

    public Object getItems() { return items; }
    public boolean isItemCheck(int position) { return getItem(position).isChecked(); }
    public void setChildHeight(int height) { childHeight = height; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.smartisan_rom_bottom_menu_item, parent, false);
            if (childHeight != 0 && childHeight != ViewGroup.LayoutParams.MATCH_PARENT) {
                convertView.getLayoutParams().height = childHeight;
            }
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        MenuItem item = getItem(position);
        boolean enabled = item.isEnabled();
        holder.icon.setImageDrawable(item.getIcon());
        holder.icon.setEnabled(enabled);
        CharSequence title = item.getTitle();
        if (title != null && title.length() > 6) {
            float maximum = dip2px(holder.title.getContext(), 12d);
            if (holder.title.getTextSize() > maximum) {
                holder.title.setTextSize(TypedValue.COMPLEX_UNIT_PX, maximum);
            }
        }
        holder.title.setText(title);
        holder.title.setTextColor(holder.title.getContext().getColor(
                R.color.smartisan_rom_bottom_menu_text));
        convertView.setEnabled(enabled);
        convertView.setActivated(item.isChecked());
        convertView.setContentDescription(title);
        bindViewCustom(holder.icon, holder.title, position);
        return convertView;
    }

    protected void bindViewCustom(ImageView icon, TextView title, int position) {}

    public static int dip2px(Context context, double dipValue) {
        return (int) (context.getResources().getDisplayMetrics().density * dipValue + 0.5d);
    }

    public static int getCompatibilityStatusBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier(
                "status_bar_height", "dimen", "android");
        return resourceId > 0 ? context.getResources().getDimensionPixelSize(resourceId) : 0;
    }

    private static final class ViewHolder {
        final ImageView icon;
        final TextView title;

        ViewHolder(View view) {
            icon = view.findViewById(R.id.smartisan_rom_bottom_menu_icon);
            title = view.findViewById(R.id.smartisan_rom_bottom_menu_text);
        }
    }
}
