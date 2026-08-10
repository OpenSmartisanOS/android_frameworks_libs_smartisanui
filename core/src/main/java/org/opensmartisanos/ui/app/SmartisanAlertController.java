package org.opensmartisanos.ui.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.opensmartisanos.ui.R;

/**
 * Public-SDK port of the Smartisan branch of the framework AlertController.
 *
 * <p>The view hierarchy is inflated from a direct namespace remap of
 * {@code alert_dialog_smartisanos.xml}. Code here only replaces hidden framework controller
 * plumbing; it does not recreate the dialog's visual structure.</p>
 */
final class SmartisanAlertController {
    private static final String STATE_CHECKED_ITEM = "smartisan:alert:checkedItem";
    private static final String STATE_CHECKED_ITEMS = "smartisan:alert:checkedItems";

    private final SmartisanAlertDialog dialog;
    private final Context context;
    private final Params params;
    private final Button[] buttons = new Button[3];

    private View topPanel;
    private View contentPanel;
    private View customPanel;
    private View buttonPanel;
    private View titleTemplate;
    private FrameLayout customContainer;
    private ScrollView scrollView;
    private ListView listView;
    private View customView;
    private TextView titleView;
    private TextView messageView;
    private ImageView iconView;
    private Button highlightedButton;

    SmartisanAlertController(SmartisanAlertDialog dialog, Params params) {
        this.dialog = dialog;
        context = dialog.getContext();
        this.params = params;
    }

    void installContent(Bundle state) {
        restoreChoices(state);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.smartisan_rom_alert_dialog);
        bindPanels();
        setupCustomContent();
        setupContent();
        setupTitle();
        setupButtons();
        requestInitialFocus();
        applyPanelBackgrounds();
        configureWindow();
    }

    private void bindPanels() {
        topPanel = dialog.findViewById(R.id.smartisan_rom_alert_top_panel);
        contentPanel = dialog.findViewById(R.id.smartisan_rom_alert_content_panel);
        customPanel = dialog.findViewById(R.id.smartisan_rom_alert_custom_panel);
        buttonPanel = dialog.findViewById(R.id.smartisan_rom_alert_button_panel);
        titleTemplate = dialog.findViewById(R.id.smartisan_rom_alert_title_template);
        customContainer = dialog.findViewById(R.id.smartisan_rom_alert_custom);
        scrollView = dialog.findViewById(R.id.smartisan_rom_alert_scroll_view);
        messageView = dialog.findViewById(R.id.smartisan_rom_alert_message);
        titleView = dialog.findViewById(R.id.smartisan_rom_alert_title);
        iconView = dialog.findViewById(R.id.smartisan_rom_alert_icon);
        buttons[0] = dialog.findViewById(R.id.smartisan_rom_alert_button_positive);
        buttons[1] = dialog.findViewById(R.id.smartisan_rom_alert_button_negative);
        buttons[2] = dialog.findViewById(R.id.smartisan_rom_alert_button_neutral);
        scrollView.setFocusable(false);
    }

    void saveState(Bundle state) {
        if (listView == null) return;
        if (params.singleChoice) {
            state.putInt(STATE_CHECKED_ITEM, listView.getCheckedItemPosition());
        } else if (params.multiChoice) {
            boolean[] checked = new boolean[listView.getCount()];
            SparseBooleanArray values = listView.getCheckedItemPositions();
            for (int i = 0; i < checked.length; i++) checked[i] = values.get(i);
            state.putBooleanArray(STATE_CHECKED_ITEMS, checked);
        }
    }

    Button getButton(int which) {
        if (which == SmartisanAlertDialog.BUTTON_POSITIVE) return buttons[0];
        if (which == SmartisanAlertDialog.BUTTON_NEGATIVE) return buttons[1];
        if (which == SmartisanAlertDialog.BUTTON_NEUTRAL) return buttons[2];
        return null;
    }

    ListView getListView() {
        return listView;
    }

    View getCustomView() {
        return customView;
    }

    void setTitle(CharSequence title) {
        params.title = title;
        if (titleView != null) titleView.setText(title);
    }

    void setMessage(CharSequence message) {
        params.message = message;
        if (messageView != null) messageView.setText(message);
    }

    void setIcon(Drawable icon) {
        params.icon = icon;
        if (iconView != null) {
            iconView.setImageDrawable(icon);
            iconView.setVisibility(icon == null ? View.GONE : View.VISIBLE);
        }
    }

    boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isEnterKey(keyCode) && event.hasNoModifiers()
                && !dialog.getWindow().getDecorView().hasFocus()
                && isButtonAvailable(highlightedButton)) {
            return highlightedButton.requestFocus()
                    && highlightedButton.onKeyDown(keyCode, event);
        }
        return scrollView != null && scrollView.executeKeyEvent(event);
    }

    boolean onKeyUp(int keyCode, KeyEvent event) {
        if (isEnterKey(keyCode) && event.hasNoModifiers()
                && isButtonAvailable(highlightedButton)
                && highlightedButton.hasFocus() && highlightedButton.isPressed()) {
            return highlightedButton.onKeyUp(keyCode, event);
        }
        return scrollView != null && scrollView.executeKeyEvent(event);
    }

    private void configureWindow() {
        Window window = dialog.getWindow();
        if (window == null) return;
        if (!canTextInput(customView)) {
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        }
    }

    private void setupCustomContent() {
        if (params.customView != null) {
            customView = detach(params.customView);
        } else if (params.customViewLayoutResId != 0) {
            customView = LayoutInflater.from(context).inflate(
                    params.customViewLayoutResId, customContainer, false);
        }
        if (customView == null) {
            customPanel.setVisibility(View.GONE);
            return;
        }
        customContainer.addView(customView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (params.viewSpacingSpecified) {
            customContainer.setPadding(params.viewSpacingLeft, params.viewSpacingTop,
                    params.viewSpacingRight, params.viewSpacingBottom);
        }
        customPanel.setVisibility(View.VISIBLE);
        if (params.hasListContent()) {
            LinearLayout.LayoutParams layoutParams =
                    (LinearLayout.LayoutParams) customPanel.getLayoutParams();
            layoutParams.weight = 0f;
            customPanel.setLayoutParams(layoutParams);
        }
    }

    @SuppressLint("WrongConstant") // Layout's API-23 constant is the public equivalent used by R2.
    private void setupContent() {
        messageView.setBreakStrategy(android.text.Layout.BREAK_STRATEGY_SIMPLE);
        if (params.adapter != null || params.items != null || params.cursor != null) {
            listView = createList();
            ViewGroup parent = (ViewGroup) scrollView.getParent();
            int index = parent.indexOfChild(scrollView);
            parent.removeViewAt(index);
            parent.addView(listView, index, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            contentPanel.setVisibility(View.VISIBLE);
            return;
        }
        if (TextUtils.isEmpty(params.message)) {
            contentPanel.setVisibility(View.GONE);
            return;
        }
        messageView.setText(params.message);
        contentPanel.setVisibility(View.VISIBLE);
    }

    private void setupTitle() {
        if (params.customTitle != null) {
            View customTitle = detach(params.customTitle);
            ((ViewGroup) topPanel).addView(customTitle, 0,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
            titleTemplate.setVisibility(View.GONE);
            topPanel.setVisibility(View.VISIBLE);
            return;
        }
        if (TextUtils.isEmpty(params.title)) {
            topPanel.setVisibility(View.GONE);
            return;
        }
        titleView.setText(params.title);
        if (params.icon != null) {
            iconView.setImageDrawable(params.icon);
            iconView.setVisibility(View.VISIBLE);
        } else {
            iconView.setVisibility(View.GONE);
        }
        topPanel.setVisibility(View.VISIBLE);
    }

    private void setupButtons() {
        highlightedButton = null;
        for (Button button : buttons) {
            button.setOnTouchListener(new SmartisanAlertButtonScaleHelper(button));
        }
        configureButton(SmartisanAlertDialog.BUTTON_NEGATIVE, params.negativeText,
                params.negativeListener);
        configureButton(SmartisanAlertDialog.BUTTON_NEUTRAL, params.neutralText,
                params.neutralListener);
        configureButton(SmartisanAlertDialog.BUTTON_POSITIVE, params.positiveText,
                params.positiveListener);

        int count = buttonCount();
        if (count == 0) {
            buttonPanel.setVisibility(View.GONE);
            return;
        }
        int ordinal = 0;
        int[] order = {
                SmartisanAlertDialog.BUTTON_NEGATIVE,
                SmartisanAlertDialog.BUTTON_NEUTRAL,
                SmartisanAlertDialog.BUTTON_POSITIVE
        };
        for (int which : order) {
            Button button = getButton(which);
            if (button.getVisibility() != View.VISIBLE) continue;
            int position = count == 1 ? 0 : ordinal == 0 ? 1
                    : ordinal == count - 1 ? 3 : 2;
            button.setBackgroundResource(positionBackground(position));
            boolean highlighted = ordinal == count - 1;
            ColorStateList colors = context.getColorStateList(highlighted
                    ? R.color.smartisan_rom_dialog_highlight_btn_text
                    : R.color.smartisan_rom_dialog_normal_btn_text);
            if (params.destructivePositive
                    && which == SmartisanAlertDialog.BUTTON_POSITIVE) {
                colors = context.getColorStateList(
                        R.color.smartisan_rom_dialog_destructive_btn_text);
            }
            button.setTextColor(colors);
            if (highlighted && Build.VERSION.SDK_INT >= 26
                    && !hasFocusedByDefault(dialog.getWindow().getDecorView())) {
                button.setFocusedByDefault(true);
            }
            if (highlighted) highlightedButton = button;
            ordinal++;
        }
        buttonPanel.setVisibility(View.VISIBLE);
    }

    /** Mirrors AlertController's non-touch default-focus order. */
    private void requestInitialFocus() {
        View parentPanel = dialog.findViewById(R.id.smartisan_rom_alert_parent_panel);
        if (parentPanel == null || parentPanel.isInTouchMode()) return;
        View content = customPanel.getVisibility() == View.VISIBLE ? customPanel : contentPanel;
        if (content != null && content.requestFocus()) return;
        if (listView != null) {
            listView.setSelection(0);
            return;
        }
        if (buttons[0].getVisibility() == View.VISIBLE) {
            buttons[0].requestFocus();
        } else if (buttons[1].getVisibility() == View.VISIBLE) {
            buttons[1].requestFocus();
        } else if (buttons[2].getVisibility() == View.VISIBLE) {
            buttons[2].requestFocus();
        }
    }

    private void configureButton(int which, CharSequence text,
            DialogInterface.OnClickListener listener) {
        Button button = getButton(which);
        if (TextUtils.isEmpty(text)) {
            button.setVisibility(View.GONE);
            return;
        }
        button.setText(text);
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(view -> {
            if (listener != null) listener.onClick(dialog, which);
            dialog.dismiss();
        });
    }

    /** Mirrors framework AlertController#setBackground for the Smartisan light resource set. */
    private void applyPanelBackgrounds() {
        View[] candidates = {topPanel, contentPanel, customPanel, buttonPanel};
        View[] visible = new View[candidates.length];
        int count = 0;
        for (View candidate : candidates) {
            if (candidate != null && candidate.getVisibility() != View.GONE) {
                visible[count++] = candidate;
            }
        }
        if (count == 0) return;
        if (count == 1) {
            visible[0].setBackgroundResource(R.drawable.smartisan_rom_dialog_full);
            return;
        }
        visible[0].setBackgroundResource(count == 2
                ? R.drawable.smartisan_rom_dialog_top_no_divider
                : R.drawable.smartisan_rom_dialog_top);
        for (int i = 1; i < count - 1; i++) {
            visible[i].setBackgroundResource(R.drawable.smartisan_rom_dialog_middle);
        }
        visible[count - 1].setBackgroundResource(R.drawable.smartisan_rom_dialog_bottom);
    }

    private ListView createList() {
        ListView view = (ListView) LayoutInflater.from(context).inflate(
                R.layout.smartisan_rom_select_dialog, null);
        view.setChoiceMode(params.multiChoice ? ListView.CHOICE_MODE_MULTIPLE
                : params.singleChoice ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);

        ListAdapter adapter = params.adapter;
        if (adapter == null && params.cursor != null) {
            adapter = new CursorChoiceAdapter(params.cursor);
        }
        if (adapter == null) {
            int layout = choiceLayout();
            CharSequence[] items = params.items == null ? new CharSequence[0] : params.items;
            if (params.multiChoice) {
                adapter = new ArrayAdapter<CharSequence>(context, layout,
                        R.id.smartisan_rom_alert_item_text, items) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View row = super.getView(position, convertView, parent);
                        // This is the original AlertController behavior: the ListView owns false
                        // states, while initially checked entries are applied during row binding.
                        if (params.checkedItems != null
                                && position < params.checkedItems.length
                                && params.checkedItems[position]) {
                            view.setItemChecked(position, true);
                        }
                        return row;
                    }
                };
            } else {
                adapter = new CheckedItemAdapter(context, layout, items);
            }
        }
        view.setAdapter(adapter);
        if (params.singleChoice && params.checkedItem >= 0) {
            view.setItemChecked(params.checkedItem, true);
            view.setSelectionFromTop(params.checkedItem, 0);
        }
        if (params.multiChoice && params.cursor != null && params.isCheckedColumn != null) {
            int checkedColumn = params.cursor.getColumnIndexOrThrow(params.isCheckedColumn);
            for (params.cursor.moveToFirst(); !params.cursor.isAfterLast();
                    params.cursor.moveToNext()) {
                view.setItemChecked(params.cursor.getPosition(),
                        params.cursor.getInt(checkedColumn) == 1);
            }
        }
        if (params.itemListener != null) {
            view.setOnItemClickListener((parent, row, position, id) -> {
                if (params.singleChoice) params.checkedItem = position;
                params.itemListener.onClick(dialog, position);
                if (!params.singleChoice) dialog.dismiss();
            });
        } else if (params.multiChoiceListener != null) {
            view.setOnItemClickListener((parent, row, position, id) -> {
                boolean checked = view.isItemChecked(position);
                if (params.checkedItems != null && position < params.checkedItems.length) {
                    params.checkedItems[position] = checked;
                }
                params.multiChoiceListener.onClick(dialog, position, checked);
            });
        }
        view.setOnItemSelectedListener(params.itemSelectedListener);
        return view;
    }

    private int choiceLayout() {
        if (params.multiChoice) return R.layout.smartisan_rom_select_dialog_multichoice;
        if (params.singleChoice) return R.layout.smartisan_rom_select_dialog_singlechoice;
        return R.layout.smartisan_rom_select_dialog_item;
    }

    private final class CursorChoiceAdapter extends CursorAdapter {
        private final int labelIndex;
        private final int checkedIndex;

        CursorChoiceAdapter(Cursor cursor) {
            super(context, cursor, false);
            labelIndex = cursor.getColumnIndexOrThrow(params.labelColumn);
            checkedIndex = params.multiChoice && params.isCheckedColumn != null
                    ? cursor.getColumnIndexOrThrow(params.isCheckedColumn) : -1;
        }

        @Override
        public View newView(Context itemContext, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(itemContext).inflate(choiceLayout(), parent, false);
        }

        @Override
        public void bindView(View row, Context itemContext, Cursor cursor) {
            TextView text = row.findViewById(R.id.smartisan_rom_alert_item_text);
            text.setText(cursor.getString(labelIndex));
            if (checkedIndex >= 0) {
                listView.setItemChecked(cursor.getPosition(), cursor.getInt(checkedIndex) == 1);
            }
        }
    }

    /** Direct public-SDK counterpart of AlertController.CheckedItemAdapter. */
    private static final class CheckedItemAdapter extends ArrayAdapter<CharSequence> {
        CheckedItemAdapter(Context context, int layout, CharSequence[] items) {
            super(context, layout, R.id.smartisan_rom_alert_item_text, items);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }

    private void restoreChoices(Bundle state) {
        if (state == null) return;
        if (params.singleChoice) {
            params.checkedItem = state.getInt(STATE_CHECKED_ITEM, params.checkedItem);
        } else if (params.multiChoice) {
            boolean[] restored = state.getBooleanArray(STATE_CHECKED_ITEMS);
            if (restored != null) {
                if (params.checkedItems != null) {
                    System.arraycopy(restored, 0, params.checkedItems, 0,
                            Math.min(restored.length, params.checkedItems.length));
                } else {
                    params.checkedItems = restored;
                }
            }
        }
    }

    private static boolean canTextInput(View view) {
        if (view == null) return false;
        if (view.onCheckIsTextEditor()) return true;
        if (!(view instanceof ViewGroup)) return false;
        ViewGroup group = (ViewGroup) view;
        for (int i = group.getChildCount() - 1; i >= 0; i--) {
            if (canTextInput(group.getChildAt(i))) return true;
        }
        return false;
    }

    private static View detach(View view) {
        if (view.getParent() instanceof ViewGroup) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
        return view;
    }

    private int buttonCount() {
        int count = 0;
        if (!TextUtils.isEmpty(params.negativeText)) count++;
        if (!TextUtils.isEmpty(params.neutralText)) count++;
        if (!TextUtils.isEmpty(params.positiveText)) count++;
        return count;
    }

    private static boolean isEnterKey(int keyCode) {
        return keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER;
    }

    private static boolean isButtonAvailable(View button) {
        return button != null && button.isEnabled() && button.isFocusable()
                && (button.isClickable() || button.isLongClickable())
                && button.getVisibility() == View.VISIBLE;
    }

    private static boolean hasFocusedByDefault(View view) {
        if (Build.VERSION.SDK_INT < 26 || view == null) return false;
        if (view.isFocusedByDefault()) return true;
        if (!(view instanceof ViewGroup)) return false;
        ViewGroup group = (ViewGroup) view;
        for (int i = 0; i < group.getChildCount(); i++) {
            if (hasFocusedByDefault(group.getChildAt(i))) return true;
        }
        return false;
    }

    private static int positionBackground(int position) {
        if (position == 0) return R.drawable.smartisan_rom_dialog_button_single;
        if (position == 1) return R.drawable.smartisan_rom_dialog_button_left;
        if (position == 3) return R.drawable.smartisan_rom_dialog_button_right;
        return R.drawable.smartisan_rom_dialog_button_middle;
    }

    static final class Params {
        CharSequence title;
        CharSequence message;
        CharSequence positiveText;
        CharSequence negativeText;
        CharSequence neutralText;
        Drawable icon;
        View customTitle;
        View customView;
        int customViewLayoutResId;
        CharSequence[] items;
        ListAdapter adapter;
        Cursor cursor;
        String labelColumn;
        String isCheckedColumn;
        boolean singleChoice;
        boolean multiChoice;
        boolean destructivePositive;
        boolean cancelable = true;
        int checkedItem = -1;
        boolean[] checkedItems;
        boolean viewSpacingSpecified;
        int viewSpacingLeft;
        int viewSpacingTop;
        int viewSpacingRight;
        int viewSpacingBottom;
        DialogInterface.OnClickListener positiveListener;
        DialogInterface.OnClickListener negativeListener;
        DialogInterface.OnClickListener neutralListener;
        DialogInterface.OnClickListener itemListener;
        DialogInterface.OnMultiChoiceClickListener multiChoiceListener;
        DialogInterface.OnCancelListener cancelListener;
        DialogInterface.OnDismissListener dismissListener;
        DialogInterface.OnShowListener showListener;
        DialogInterface.OnKeyListener keyListener;
        AdapterView.OnItemSelectedListener itemSelectedListener;

        Params() {}

        Params(Params source) {
            title = source.title;
            message = source.message;
            positiveText = source.positiveText;
            negativeText = source.negativeText;
            neutralText = source.neutralText;
            icon = source.icon;
            customTitle = source.customTitle;
            customView = source.customView;
            customViewLayoutResId = source.customViewLayoutResId;
            items = source.items;
            adapter = source.adapter;
            cursor = source.cursor;
            labelColumn = source.labelColumn;
            isCheckedColumn = source.isCheckedColumn;
            singleChoice = source.singleChoice;
            multiChoice = source.multiChoice;
            destructivePositive = source.destructivePositive;
            cancelable = source.cancelable;
            checkedItem = source.checkedItem;
            checkedItems = source.checkedItems;
            viewSpacingSpecified = source.viewSpacingSpecified;
            viewSpacingLeft = source.viewSpacingLeft;
            viewSpacingTop = source.viewSpacingTop;
            viewSpacingRight = source.viewSpacingRight;
            viewSpacingBottom = source.viewSpacingBottom;
            positiveListener = source.positiveListener;
            negativeListener = source.negativeListener;
            neutralListener = source.neutralListener;
            itemListener = source.itemListener;
            multiChoiceListener = source.multiChoiceListener;
            cancelListener = source.cancelListener;
            dismissListener = source.dismissListener;
            showListener = source.showListener;
            keyListener = source.keyListener;
            itemSelectedListener = source.itemSelectedListener;
        }

        void clearListContent() {
            items = null;
            adapter = null;
            cursor = null;
            labelColumn = null;
            isCheckedColumn = null;
            singleChoice = false;
            multiChoice = false;
            checkedItem = -1;
            checkedItems = null;
            itemListener = null;
            multiChoiceListener = null;
        }

        boolean hasListContent() {
            return adapter != null || items != null || cursor != null;
        }
    }
}
