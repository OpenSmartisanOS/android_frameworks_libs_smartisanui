package org.opensmartisanos.ui.app;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.PopupWindow;

import org.opensmartisanos.ui.R;
import org.opensmartisanos.ui.widget.SmartisanShadowButton;

import java.util.List;

/** Direct public-SDK port of the original action-row MenuDialog adapter. */
public class SmartisanMenuDialogListAdapter extends BaseAdapter {
    private final Context context;
    private final List<String> labels;
    private final List<View.OnClickListener> listeners;
    private final boolean hasRecentCall;
    private Dialog dialog;
    private PopupWindow popupWindow;

    public SmartisanMenuDialogListAdapter(Context context, List<String> labels,
            List<View.OnClickListener> listeners) {
        this(context, labels, listeners, false);
    }

    public SmartisanMenuDialogListAdapter(Context context, List<String> labels,
            List<View.OnClickListener> listeners, boolean hasRecentCall) {
        if (labels == null || listeners == null || labels.size() != listeners.size()) {
            throw new IllegalArgumentException();
        }
        this.context = context;
        this.labels = labels;
        this.listeners = listeners;
        this.hasRecentCall = hasRecentCall;
    }

    public void setDialog(Dialog value) {
        dialog = value;
        popupWindow = null;
    }

    /** Public-SDK replacement for the original private SmartisanPopupWindowBase overload. */
    public void setPopupWindow(PopupWindow value) {
        popupWindow = value;
        dialog = null;
    }

    @Override public int getCount() { return labels.size(); }
    @Override public String getItem(int position) { return labels.get(position); }
    @Override public long getItemId(int position) { return position; }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.smartisan_rom_menu_dialog_list_item, null);
        }
        if (hasRecentCall && position == 0) {
            convertView.setBackgroundResource(R.drawable.smartisan_rom_recent_call_item_selector);
            int padding = context.getResources().getDimensionPixelSize(
                    R.dimen.smartisan_rom_menu_dialog_recent_padding_left);
            convertView.setPadding(padding, 0, padding, 0);
        } else {
            convertView.setBackgroundResource(R.drawable.smartisan_rom_menu_dialog_item_selector);
        }
        SmartisanShadowButton button = (SmartisanShadowButton) convertView;
        button.setText(labels.get(position));
        button.setOnClickListener(view -> {
            if (dialog != null) dialog.dismiss();
            if (popupWindow != null) popupWindow.dismiss();
            listeners.get(position).onClick(button);
        });
        return convertView;
    }
}
