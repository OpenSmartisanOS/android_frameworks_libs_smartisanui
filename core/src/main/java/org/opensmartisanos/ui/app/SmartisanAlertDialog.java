package org.opensmartisanos.ui.app;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import org.opensmartisanos.ui.R;

/**
 * Self-contained implementation of the Revone dialog used by the rebuilt R2 Music app.
 *
 * <p>The 308dp phone layout, title/content/button sections and window behavior mirror the rebuilt
 * Music app. The controller uses public Android APIs so the result is stable on other ROMs.</p>
 */
public class SmartisanAlertDialog extends Dialog {
    public static final int BUTTON_POSITIVE = DialogInterface.BUTTON_POSITIVE;
    public static final int BUTTON_NEGATIVE = DialogInterface.BUTTON_NEGATIVE;
    public static final int BUTTON_NEUTRAL = DialogInterface.BUTTON_NEUTRAL;

    private static final String STATE_CHECKED_ITEM = "smartisan:checkedItem";
    private static final String STATE_CHECKED_ITEMS = "smartisan:checkedItems";

    private final Params params;
    private final Button[] buttons = new Button[3];
    private ListView listView;
    private View customView;

    private SmartisanAlertDialog(Context context, Params params) {
        super(context, R.style.Theme_SmartisanUi_AlertDialog);
        this.params = params;
    }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        restoreChoices(state);
        setContentView(createPanels());
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setGravity(Gravity.CENTER);
        }
    }

    @Override protected void onStart() {
        super.onStart();
        Window window = getWindow();
        if (window != null) {
            window.setDimAmount(0.54f);
            int available = getContext().getResources().getDisplayMetrics().widthPixels - dp(32);
            window.setLayout(Math.min(dp(308), available), WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        if (listView != null && params.singleChoice) {
            state.putInt(STATE_CHECKED_ITEM, listView.getCheckedItemPosition());
        } else if (listView != null && params.multiChoice) {
            boolean[] checked = new boolean[listView.getCount()];
            SparseBooleanArray values = listView.getCheckedItemPositions();
            for (int i = 0; i < checked.length; i++) checked[i] = values.get(i);
            state.putBooleanArray(STATE_CHECKED_ITEMS, checked);
        }
        return state;
    }

    private void restoreChoices(Bundle state) {
        if (state == null) return;
        if (params.singleChoice) params.checkedItem = state.getInt(STATE_CHECKED_ITEM, -1);
        if (params.multiChoice) {
            boolean[] restored = state.getBooleanArray(STATE_CHECKED_ITEMS);
            if (restored != null) params.checkedItems = restored;
        }
    }

    private View createPanels() {
        LinearLayout root = new LinearLayout(getContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundResource(R.drawable.smartisan_alert_revone_root);

        View title = createTitlePanel();
        if (title != null) {
            root.addView(title, matchWrap());
        }
        View content = createContentPanel();
        if (content != null) {
            LinearLayout.LayoutParams contentParams = matchWrap();
            if (content == listView) {
                int count = listView.getAdapter() == null ? 1 : listView.getAdapter().getCount();
                contentParams.height = dp(Math.min(240, Math.max(48, count * 48)));
            }
            root.addView(content, contentParams);
        }
        View buttonsPanel = createButtonPanel();
        if (buttonsPanel != null) {
            root.addView(buttonsPanel, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));
        }
        return root;
    }

    private View createTitlePanel() {
        if (TextUtils.isEmpty(params.title) && params.icon == null) return null;
        LinearLayout panel = new LinearLayout(getContext());
        panel.setGravity(Gravity.CENTER);
        panel.setPadding(dp(16), 0, dp(16), 0);
        panel.setMinimumHeight(dp(48));
        if (params.icon != null) {
            ImageView icon = new ImageView(getContext());
            icon.setImageDrawable(params.icon);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(32), dp(32));
            iconParams.rightMargin = dp(8);
            panel.addView(icon, iconParams);
        }
        TextView title = new TextView(getContext());
        title.setText(params.title);
        title.setTextSize(18);
        title.setTextColor(0xcc000000);
        title.setTypeface(title.getTypeface(), Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setSingleLine(true);
        title.setEllipsize(TextUtils.TruncateAt.END);
        panel.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return panel;
    }

    private View createContentPanel() {
        if (params.customView != null) {
            customView = params.customView;
            if (customView.getParent() instanceof ViewGroup) {
                ((ViewGroup) customView.getParent()).removeView(customView);
            }
            FrameLayout panel = new FrameLayout(getContext());
            panel.setBackgroundResource(R.drawable.smartisan_alert_revone_content);
            panel.setPadding(dp(18), dp(18), dp(18), dp(18));
            panel.addView(customView, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return panel;
        }
        if (params.adapter != null || params.items != null) return createList();
        if (TextUtils.isEmpty(params.message)) return null;
        TextView message = new TextView(getContext());
        message.setText(params.message);
        message.setTextSize(15);
        message.setTextColor(0x80000000);
        message.setGravity(Gravity.CENTER);
        message.setPadding(dp(24), dp(18), dp(24), dp(18));
        message.setMinHeight(dp(76));
        message.setLineSpacing(dp(2), 1);
        ScrollView scroll = new ScrollView(getContext());
        scroll.setClipToPadding(false);
        scroll.setBackgroundResource(R.drawable.smartisan_alert_revone_content);
        scroll.addView(message, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return scroll;
    }

    private View createList() {
        listView = new ListView(getContext());
        listView.setBackgroundResource(R.drawable.smartisan_alert_revone_content);
        listView.setDivider(null);
        listView.setSelector(android.R.color.transparent);
        listView.setChoiceMode(params.multiChoice ? ListView.CHOICE_MODE_MULTIPLE
                : params.singleChoice ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
        ListAdapter adapter = params.adapter;
        if (adapter == null) adapter = new ChoiceAdapter(getContext(), params.items);
        listView.setAdapter(adapter);
        if (params.singleChoice && params.checkedItem >= 0) {
            listView.setItemChecked(params.checkedItem, true);
        }
        if (params.multiChoice && params.checkedItems != null) {
            for (int i = 0; i < Math.min(params.checkedItems.length, adapter.getCount()); i++) {
                listView.setItemChecked(i, params.checkedItems[i]);
            }
        }
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (params.multiChoice) {
                boolean checked = listView.isItemChecked(position);
                if (params.checkedItems != null && position < params.checkedItems.length) {
                    params.checkedItems[position] = checked;
                }
                if (params.multiChoiceListener != null) {
                    params.multiChoiceListener.onClick(this, position, checked);
                }
            } else {
                if (params.singleChoice) params.checkedItem = position;
                if (params.itemListener != null) params.itemListener.onClick(this, position);
                if (!params.singleChoice) dismiss();
            }
        });
        listView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                dp(Math.min(240, Math.max(48, adapter.getCount() * 48)))));
        return listView;
    }

    private View createButtonPanel() {
        int count = buttonCount();
        if (count == 0) return null;
        LinearLayout panel = new LinearLayout(getContext());
        panel.setOrientation(LinearLayout.HORIZONTAL);
        int added = 0;
        int[] order = {BUTTON_NEGATIVE, BUTTON_NEUTRAL, BUTTON_POSITIVE};
        for (int which : order) {
            CharSequence text = textFor(which);
            if (TextUtils.isEmpty(text)) continue;
            if (added > 0) {
                View divider = new View(getContext());
                divider.setBackgroundResource(R.drawable.smartisan_alert_revone_divider);
                panel.addView(divider, new LinearLayout.LayoutParams(1,
                        ViewGroup.LayoutParams.MATCH_PARENT));
            }
            DialogInterface.OnClickListener listener = which == BUTTON_NEGATIVE
                    ? params.negativeListener : which == BUTTON_NEUTRAL
                    ? params.neutralListener : params.positiveListener;
            addButton(panel, which, text, listener, buttonPosition(which, count));
            added++;
        }
        return panel;
    }

    private void addButton(LinearLayout panel, int which, CharSequence text,
            DialogInterface.OnClickListener listener, int position) {
        if (TextUtils.isEmpty(text)) return;
        Button button = new Button(getContext());
        button.setText(text);
        button.setTextSize(13.5f);
        button.setTypeface(button.getTypeface(), Typeface.BOLD);
        button.setGravity(Gravity.CENTER);
        button.setMaxLines(2);
        button.setMinWidth(0);
        button.setMinHeight(dp(48));
        button.setAllCaps(false);
        button.setMinimumWidth(0);
        button.setMinimumHeight(0);
        button.setPadding(0, 0, 0, 0);
        button.setBackgroundResource(positionBackground(position));
        boolean highlight = which == rightmostButton();
        ColorStateList colors = highlight
                ? new ColorStateList(new int[][] {
                    new int[] {-android.R.attr.state_enabled}, new int[] {}
                }, new int[] {0xff5c89f2, 0xff5c89f2})
                : new ColorStateList(new int[][] {
                    new int[] {-android.R.attr.state_enabled}, new int[] {}
                }, new int[] {0xffd9d9d9, 0xff898989});
        if (params.destructivePositive && which == BUTTON_POSITIVE) {
            colors = new ColorStateList(new int[][] {new int[] {-android.R.attr.state_enabled},
                    new int[] {}}, new int[] {0x4dba3b3b, 0xffba3b3b});
        }
        button.setTextColor(colors);
        button.setOnClickListener(view -> {
            if (listener != null) listener.onClick(this, which);
            dismiss();
        });
        buttons[index(which)] = button;
        panel.addView(button, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT, 1));
    }

    private int buttonCount() {
        int count = 0;
        if (!TextUtils.isEmpty(params.negativeText)) count++;
        if (!TextUtils.isEmpty(params.neutralText)) count++;
        if (!TextUtils.isEmpty(params.positiveText)) count++;
        return count;
    }

    private int rightmostButton() {
        if (!TextUtils.isEmpty(params.positiveText)) return BUTTON_POSITIVE;
        if (!TextUtils.isEmpty(params.neutralText)) return BUTTON_NEUTRAL;
        return BUTTON_NEGATIVE;
    }

    private int buttonPosition(int which, int count) {
        if (count == 1) return 0;
        int ordinal = 0;
        int[] order = {BUTTON_NEGATIVE, BUTTON_NEUTRAL, BUTTON_POSITIVE};
        for (int candidate : order) {
            if (TextUtils.isEmpty(textFor(candidate))) continue;
            if (candidate == which) return ordinal == 0 ? 1 : ordinal == count - 1 ? 3 : 2;
            ordinal++;
        }
        return 2;
    }

    private CharSequence textFor(int which) {
        if (which == BUTTON_NEGATIVE) return params.negativeText;
        if (which == BUTTON_NEUTRAL) return params.neutralText;
        return params.positiveText;
    }

    private static int positionBackground(int position) {
        if (position == 0) return R.drawable.smartisan_alert_revone_button_single;
        if (position == 1) return R.drawable.smartisan_alert_revone_button_left;
        if (position == 3) return R.drawable.smartisan_alert_revone_button_right;
        return R.drawable.smartisan_alert_revone_button_middle;
    }

    public Button getButton(int which) { return buttons[index(which)]; }
    public ListView getListView() { return listView; }
    public View getCustomView() { return customView; }

    private static int index(int which) {
        if (which == BUTTON_POSITIVE) return 0;
        if (which == BUTTON_NEGATIVE) return 1;
        if (which == BUTTON_NEUTRAL) return 2;
        throw new IllegalArgumentException("Unknown button: " + which);
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private int dp(float value) {
        return (int) (value * getContext().getResources().getDisplayMetrics().density + 0.5f);
    }

    private final class ChoiceAdapter extends ArrayAdapter<CharSequence> {
        ChoiceAdapter(Context context, CharSequence[] values) {
            super(context, android.R.layout.simple_list_item_1, values);
        }

        @Override public View getView(int position, View convertView, ViewGroup parent) {
            if (!params.singleChoice && !params.multiChoice) {
                TextView text = convertView instanceof TextView ? (TextView) convertView
                        : new TextView(getContext());
                configureChoiceText(text);
                text.setText(getItem(position));
                return text;
            }
            CheckedTextView text = convertView instanceof CheckedTextView
                    ? (CheckedTextView) convertView : new CheckedTextView(getContext());
            configureChoiceText(text);
            text.setText(getItem(position));
            text.setCheckMarkDrawable(params.singleChoice ? android.R.drawable.radiobutton_on_background
                    : android.R.drawable.checkbox_on_background);
            return text;
        }

        private void configureChoiceText(TextView text) {
            text.setTextSize(16);
            text.setTextColor(0xcc000000);
            text.setGravity(Gravity.CENTER_VERTICAL);
            text.setSingleLine(true);
            text.setEllipsize(TextUtils.TruncateAt.END);
            text.setMinHeight(dp(48));
            text.setPadding(dp(params.singleChoice || params.multiChoice ? 20 : 18), 0,
                    dp(params.singleChoice || params.multiChoice ? 6 : 12), 0);
            text.setBackgroundColor(0x00000000);
        }
    }

    private static final class Params {
        CharSequence title, message, positiveText, negativeText, neutralText;
        Drawable icon;
        View customView;
        CharSequence[] items;
        ListAdapter adapter;
        boolean singleChoice, multiChoice, destructivePositive;
        int checkedItem = -1;
        boolean[] checkedItems;
        DialogInterface.OnClickListener positiveListener, negativeListener, neutralListener, itemListener;
        DialogInterface.OnMultiChoiceClickListener multiChoiceListener;
        DialogInterface.OnCancelListener cancelListener;
        DialogInterface.OnDismissListener dismissListener;
        DialogInterface.OnShowListener showListener;
        boolean cancelable = true;

        Params() {}
        Params(Params source) {
            title = source.title; message = source.message;
            positiveText = source.positiveText; negativeText = source.negativeText;
            neutralText = source.neutralText; icon = source.icon; customView = source.customView;
            items = source.items == null ? null : source.items.clone(); adapter = source.adapter;
            singleChoice = source.singleChoice; multiChoice = source.multiChoice;
            destructivePositive = source.destructivePositive; checkedItem = source.checkedItem;
            checkedItems = source.checkedItems == null ? null : source.checkedItems.clone();
            positiveListener = source.positiveListener; negativeListener = source.negativeListener;
            neutralListener = source.neutralListener; itemListener = source.itemListener;
            multiChoiceListener = source.multiChoiceListener; cancelListener = source.cancelListener;
            dismissListener = source.dismissListener; showListener = source.showListener;
            cancelable = source.cancelable;
        }
    }

    public static final class Builder {
        private final Context context;
        private final Params params = new Params();
        public Builder(Context context) { this.context = context; }
        public Context getContext() { return context; }
        public Builder setTitle(int id) { return setTitle(context.getText(id)); }
        public Builder setTitle(CharSequence value) { params.title = value; return this; }
        public Builder setMessage(int id) { return setMessage(context.getText(id)); }
        public Builder setMessage(CharSequence value) { clearContent(); params.message = value; return this; }
        public Builder setIcon(int id) { params.icon = context.getDrawable(id); return this; }
        public Builder setIcon(Drawable value) { params.icon = value; return this; }
        public Builder setView(int id) { return setView(View.inflate(context, id, null)); }
        public Builder setView(View value) { clearContent(); params.customView = value; return this; }
        public Builder setPositiveButton(int id, DialogInterface.OnClickListener listener) {
            return setPositiveButton(context.getText(id), listener);
        }
        public Builder setPositiveButton(CharSequence text, DialogInterface.OnClickListener listener) {
            params.positiveText = text; params.positiveListener = listener; return this;
        }
        public Builder setNegativeButton(int id, DialogInterface.OnClickListener listener) {
            return setNegativeButton(context.getText(id), listener);
        }
        public Builder setNegativeButton(CharSequence text, DialogInterface.OnClickListener listener) {
            params.negativeText = text; params.negativeListener = listener; return this;
        }
        public Builder setNeutralButton(int id, DialogInterface.OnClickListener listener) {
            return setNeutralButton(context.getText(id), listener);
        }
        public Builder setNeutralButton(CharSequence text, DialogInterface.OnClickListener listener) {
            params.neutralText = text; params.neutralListener = listener; return this;
        }
        public Builder setDestructivePositive(boolean value) {
            params.destructivePositive = value; return this;
        }
        public Builder setItems(CharSequence[] items, DialogInterface.OnClickListener listener) {
            clearContent(); params.items = items; params.itemListener = listener; return this;
        }
        public Builder setItems(int id, DialogInterface.OnClickListener listener) {
            return setItems(context.getResources().getTextArray(id), listener);
        }
        public Builder setAdapter(ListAdapter adapter, DialogInterface.OnClickListener listener) {
            clearContent(); params.adapter = adapter; params.itemListener = listener; return this;
        }
        public Builder setSingleChoiceItems(CharSequence[] items, int checked,
                DialogInterface.OnClickListener listener) {
            clearContent(); params.items = items; params.singleChoice = true;
            params.checkedItem = checked; params.itemListener = listener; return this;
        }
        public Builder setSingleChoiceItems(int id, int checked,
                DialogInterface.OnClickListener listener) {
            return setSingleChoiceItems(context.getResources().getTextArray(id), checked, listener);
        }
        public Builder setMultiChoiceItems(CharSequence[] items, boolean[] checked,
                DialogInterface.OnMultiChoiceClickListener listener) {
            clearContent(); params.items = items; params.multiChoice = true;
            params.checkedItems = checked; params.multiChoiceListener = listener; return this;
        }
        public Builder setMultiChoiceItems(int id, boolean[] checked,
                DialogInterface.OnMultiChoiceClickListener listener) {
            return setMultiChoiceItems(context.getResources().getTextArray(id), checked, listener);
        }
        public Builder setCancelable(boolean value) { params.cancelable = value; return this; }
        public Builder setOnCancelListener(DialogInterface.OnCancelListener value) {
            params.cancelListener = value; return this;
        }
        public Builder setOnDismissListener(DialogInterface.OnDismissListener value) {
            params.dismissListener = value; return this;
        }
        public Builder setOnShowListener(DialogInterface.OnShowListener value) {
            params.showListener = value; return this;
        }
        public SmartisanAlertDialog create() {
            Params copy = new Params(params);
            SmartisanAlertDialog dialog = new SmartisanAlertDialog(context, copy);
            dialog.setCancelable(copy.cancelable);
            dialog.setCanceledOnTouchOutside(copy.cancelable);
            dialog.setOnCancelListener(copy.cancelListener);
            dialog.setOnDismissListener(copy.dismissListener);
            dialog.setOnShowListener(copy.showListener);
            return dialog;
        }
        public SmartisanAlertDialog show() {
            SmartisanAlertDialog dialog = create();
            dialog.show();
            return dialog;
        }
        private void clearContent() {
            params.message = null; params.customView = null; params.items = null; params.adapter = null;
            params.singleChoice = false; params.multiChoice = false; params.checkedItem = -1;
            params.checkedItems = null; params.itemListener = null; params.multiChoiceListener = null;
        }
    }
}
