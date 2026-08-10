package org.opensmartisanos.ui.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.opensmartisanos.ui.R;
import org.opensmartisanos.ui.internal.RomFontFitTextView;

import java.util.List;

/** Direct public-SDK port of the original deprecated legacy multi-row menu adapter. */
@Deprecated
public class SmartisanMenuDialogMultiAdapter extends BaseAdapter {
    private final Context context;
    private final LayoutInflater inflater;
    private final List<String> labels;
    private final boolean hasRecentCall;

    public SmartisanMenuDialogMultiAdapter(Context context, List<String> labels) {
        this(context, labels, false);
    }

    public SmartisanMenuDialogMultiAdapter(Context context, List<String> labels,
            boolean hasRecentCall) {
        if (labels == null) throw new IllegalArgumentException();
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.labels = labels;
        this.hasRecentCall = hasRecentCall;
    }

    @Override public int getCount() { return labels.size(); }
    @Override public String getItem(int position) { return labels.get(position); }
    @Override public long getItemId(int position) { return position; }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.smartisan_rom_menu_dialog_list_multi_item,
                    null);
        }
        if (position == getCount() - 1) {
            convertView.setBackgroundResource(
                    R.drawable.smartisan_rom_menu_dialog_last_item_selector);
        } else if (hasRecentCall && position == 0) {
            convertView.setBackgroundResource(
                    R.drawable.smartisan_rom_recent_call_multi_item_selector);
            int padding = context.getResources().getDimensionPixelSize(
                    R.dimen.smartisan_rom_menu_dialog_recent_padding);
            convertView.setPadding(padding, 0, padding, 0);
        } else {
            convertView.setBackgroundResource(
                    R.drawable.smartisan_rom_menu_dialog_multi_item_selector);
        }
        RomFontFitTextView text = (RomFontFitTextView) convertView;
        text.setText(labels.get(position));
        text.setMinTextSize(context.getResources().getDimension(
                R.dimen.smartisan_rom_menu_dialog_item_text_min_size));
        return convertView;
    }
}
