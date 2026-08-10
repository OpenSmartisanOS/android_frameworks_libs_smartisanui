package org.opensmartisanos.ui.app;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.provider.Settings;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.opensmartisanos.ui.R;
import org.opensmartisanos.ui.internal.DialogCallbackOrder;
import org.opensmartisanos.ui.widget.SmartisanDialogTitleBar;
import org.opensmartisanos.ui.widget.SmartisanShadowButton;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.List;

/** Direct public-SDK port of the Smartisan OS 8.5.3 R2 MenuDialog. */
public class SmartisanMenuDialog extends Dialog implements DialogInterface.OnKeyListener {
    public static final int LOCATION_APP_BOTTOM = 0;
    public static final int LOCATION_APP_CENTER = 1;
    public static final int LOCATION_DISPLAY_CENTER = 2;
    public static final int LOCATION_BOTTOM = LOCATION_APP_BOTTOM;
    public static final int LOCATION_CENTER = LOCATION_APP_CENTER;

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.CLASS)
    public @interface DialogLocation {
        int from() default LOCATION_APP_BOTTOM;
        int to() default LOCATION_DISPLAY_CENTER;
    }

    private final Context context;
    private final boolean externalDisplay;
    private final int location;
    private final View.OnClickListener cancelListener = view -> dismiss();
    private SmartisanDialogTitleBar titleBar;
    private SmartisanShadowButton positiveButton;
    private ListView listView;
    private View contentPanel;
    private int buttonMarginEdge;
    private int buttonMarginView;

    public SmartisanMenuDialog(Context context) {
        this(context, LOCATION_APP_BOTTOM, true);
    }

    public SmartisanMenuDialog(Context context, @DialogLocation int location) {
        this(context, location, false);
    }

    private SmartisanMenuDialog(Context context, @DialogLocation int location,
            boolean defaultConstructor) {
        super(context, defaultConstructor && isExternalDisplay(context)
                ? R.style.Theme_SmartisanUi_ExternalMenuDialog
                : R.style.Theme_SmartisanUi_MenuDialog);
        this.context = context;
        this.location = location;
        externalDisplay = isExternalDisplay(context);
        init();
        // The original default constructor keeps an external-display dialog centered.
        // Explicit location constructors still override the initial gravity.
        if (!defaultConstructor) locateDialog(location);
    }

    private void init() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(externalDisplay
                ? R.layout.smartisan_rom_revone_menu_dialog
                : R.layout.smartisan_rom_menu_dialog);
        titleBar = findViewById(R.id.smartisan_rom_menu_dialog_title_bar);
        positiveButton = findViewById(R.id.smartisan_rom_menu_dialog_ok);
        listView = findViewById(R.id.smartisan_rom_menu_dialog_content_list);
        contentPanel = findViewById(R.id.smartisan_rom_menu_dialog_content_panel);

        if (externalDisplay) {
            titleBar.getTitleBarContainer().setBackgroundColor(Color.TRANSPARENT);
            titleBar.setBackgroundResource(R.drawable.smartisan_rom_revone_dialog_bg_title);
            View divider = titleBar.findViewById(R.id.smartisan_rom_shadow_divider);
            if (divider != null) divider.setVisibility(View.GONE);
        }
        titleBar.forceRequestAccessibilityFocusWhenAttached(false);
        titleBar.setOnRightButtonClickListener(cancelListener);
        titleBar.setOnLeftButtonClickListener(cancelListener);
        titleBar.setShadowVisible(false);
        initLeftRightHands();
        listView.setFocusable(false);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setGravity(externalDisplay ? Gravity.CENTER : Gravity.BOTTOM);
            window.setLayout(externalDisplay
                            ? WindowManager.LayoutParams.WRAP_CONTENT
                            : WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
            // Original values are FLAG_WATCH_OUTSIDE_TOUCH and FLAG_ALT_FOCUSABLE_IM.
            window.addFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                    | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            installNavigationBarAdapter(window);
        }

        buttonMarginView = context.getResources().getDimensionPixelOffset(
                R.dimen.smartisan_rom_menu_dialog_button_margin_view);
        buttonMarginEdge = context.getResources().getDimensionPixelOffset(
                R.dimen.smartisan_rom_menu_dialog_button_margin_edge);
        setOnKeyListener(this);
    }

    private void installNavigationBarAdapter(Window window) {
        window.getDecorView().setOnApplyWindowInsetsListener((view, insets) -> {
            boolean landscape = context.getResources().getConfiguration().orientation
                    == Configuration.ORIENTATION_LANDSCAPE;
            boolean navigationBarVisible = insets.getSystemWindowInsetBottom() > 0;
            onApplyNavigationBarStatusChange(landscape || navigationBarVisible);
            return insets;
        });
    }

    private void initLeftRightHands() {
        int value = 1;
        try {
            value = Settings.Global.getInt(context.getContentResolver(), "one_hand_mode", 1);
        } catch (RuntimeException ignored) {
            // The public SDK cannot depend on the private SettingsSmt key accessor.
        }
        titleBar.setLeftButtonVisibility(value == 0 ? View.VISIBLE : View.INVISIBLE);
        titleBar.setRightButtonVisibility(value == 0 ? View.INVISIBLE : View.VISIBLE);
    }

    private void locateDialog(int value) {
        Window window = getWindow();
        if (window == null) return;
        if (value == LOCATION_APP_BOTTOM) {
            window.setGravity(Gravity.BOTTOM);
        } else if (value == LOCATION_APP_CENTER || value == LOCATION_DISPLAY_CENTER) {
            window.setGravity(Gravity.CENTER);
            // The original DISPLAY_CENTER external-display type 2056 is a hidden system API.
            // A public SDK window remains attached to the caller's display context.
        }
    }

    @SuppressWarnings("deprecation")
    private static boolean isExternalDisplay(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager == null ? null : manager.getDefaultDisplay();
        return display != null && display.getDisplayId() != Display.DEFAULT_DISPLAY;
    }

    @Override public void setTitle(int titleId) { setTitle(context.getText(titleId)); }
    @Override public void setTitle(CharSequence title) { titleBar.setTitle(title); }
    public void setTitleSingleLine(boolean singleLine) { titleBar.setTitleSingleLine(singleLine); }
    public void setTitleSinleLine(boolean singleLine) { titleBar.setTitleSingleLine(singleLine); }

    public void setAdapter(SmartisanMenuDialogListAdapter adapter) {
        listView.setVisibility(View.VISIBLE);
        adjustLayoutParams();
        listView.setAdapter(adapter);
        listView.getLayoutParams().height = adapter.getCount() >= 5
                ? context.getResources().getDimensionPixelOffset(
                        R.dimen.smartisan_rom_menu_dialog_long_list_height)
                : ViewGroup.LayoutParams.WRAP_CONTENT;
        adapter.setDialog(this);
    }

    /** Additive public-SDK convenience API; the original path uses the typed adapter overload. */
    @Deprecated public void setAdapter(ListAdapter adapter) { setAdapter(adapter, null); }

    /** Additive public-SDK convenience API; the original path uses the typed adapter overload. */
    @Deprecated public void setAdapter(ListAdapter adapter,
            AdapterView.OnItemClickListener listener) {
        listView.setVisibility(View.VISIBLE);
        adjustLayoutParams();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(listener);
        listView.getLayoutParams().height = adapter != null && adapter.getCount() >= 5
                ? context.getResources().getDimensionPixelOffset(
                        R.dimen.smartisan_rom_menu_dialog_long_list_height)
                : ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    /** Original misspelled API retained. This is a legacy row style, not multi-selection. */
    public void setAdaper(SmartisanMenuDialogMultiAdapter adapter,
            AdapterView.OnItemClickListener listener) {
        listView.setVisibility(View.VISIBLE);
        adjustLayoutParams();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(listener);
        listView.getLayoutParams().height = context.getResources().getDimensionPixelOffset(
                R.dimen.smartisan_rom_menu_dialog_legacy_list_height);
        listView.setBackgroundResource(R.drawable.smartisan_rom_menu_dialog_multi_list_bg);
    }

    public ListView getListView() { return listView; }
    public SmartisanDialogTitleBar getTitleBar() { return titleBar; }

    public void setNegativeButton(View.OnClickListener listener) {
        titleBar.setOnRightButtonClickListener(listener);
        titleBar.setOnLeftButtonClickListener(listener);
    }

    @Deprecated public void setNegativeButton(int textId, View.OnClickListener listener) {
        setNegativeButton(context.getText(textId), listener);
    }

    @Deprecated public void setNegativeButton(CharSequence text,
            View.OnClickListener listener) {
        titleBar.setRightButtonText(text);
        titleBar.setLeftButtonText(text);
        setNegativeButton(listener);
    }

    public void setNegativeImage(int resourceId, View.OnClickListener listener) {
        titleBar.setRightImageRes(resourceId);
        titleBar.setLeftImageViewRes(resourceId);
        setNegativeButton(listener);
    }

    public void setPositiveRedBg(boolean red) {
        setPositiveBgStyle(red
                ? SmartisanShadowButton.LongButtonStyle.RED
                : SmartisanShadowButton.LongButtonStyle.GRAY);
    }

    public void setPositiveBgStyle(SmartisanShadowButton.LongButtonStyle style) {
        positiveButton.updateBackgroundStyle(style);
    }

    public void setPositiveRedBackground(boolean red) { setPositiveRedBg(red); }
    public void setPositiveBackgroundStyle(SmartisanShadowButton.LongButtonStyle style) {
        setPositiveBgStyle(style);
    }

    public void setPositiveButton(int textId, View.OnClickListener listener) {
        setPositiveButton(context.getText(textId), listener);
    }

    public void setPositiveButton(CharSequence text, View.OnClickListener listener) {
        positiveButton.setVisibility(View.VISIBLE);
        adjustLayoutParams();
        positiveButton.setText(text);
        positiveButton.setOnClickListener(view -> DialogCallbackOrder.dismissThenRun(
                this::dismiss, listener == null ? null : () -> listener.onClick(view)));
    }

    public void setPositiveButtonGone() {
        positiveButton.setText(null);
        positiveButton.setOnClickListener(null);
        positiveButton.setVisibility(View.GONE);
        adjustLayoutParams();
    }

    private void adjustLayoutParams() {
        LinearLayout.LayoutParams buttonParams =
                (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
        boolean bothVisible = listView.getVisibility() == View.VISIBLE
                && positiveButton.getVisibility() == View.VISIBLE;
        buttonParams.topMargin = bothVisible ? 0 : buttonMarginView;
        positiveButton.setLayoutParams(buttonParams);
        int listBottomPadding = bothVisible ? buttonMarginView : buttonMarginEdge;
        listView.setPadding(listView.getPaddingLeft(), listView.getPaddingTop(),
                listView.getPaddingRight(), listBottomPadding);
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) initLeftRightHands();
    }

    protected View[] getButtons() {
        return new View[]{positiveButton, titleBar.getLeftImageView(),
                titleBar.getRightImageView()};
    }

    private boolean isButtonAvailable(View button) {
        return button != null && button.isEnabled() && button.isFocusable()
                && (button.isClickable() || button.isLongClickable())
                && button.getVisibility() == View.VISIBLE;
    }

    private View getMostAvailableButton() {
        for (View button : getButtons()) if (isButtonAvailable(button)) return button;
        return null;
    }

    @Override public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) return onKeyEventDown(keyCode, event);
        if (event.getAction() == KeyEvent.ACTION_UP) return onKeyEventUp(keyCode, event);
        return false;
    }

    private boolean onKeyEventDown(int keyCode, KeyEvent event) {
        Window window = getWindow();
        if ((window != null && window.getDecorView().hasFocus()) || !event.hasNoModifiers()
                || (keyCode != KeyEvent.KEYCODE_ENTER
                && keyCode != KeyEvent.KEYCODE_NUMPAD_ENTER)) return false;
        View button = getMostAvailableButton();
        return button != null && button.requestFocus() && button.onKeyDown(keyCode, event);
    }

    private boolean onKeyEventUp(int keyCode, KeyEvent event) {
        if (!event.hasNoModifiers() || (keyCode != KeyEvent.KEYCODE_ENTER
                && keyCode != KeyEvent.KEYCODE_NUMPAD_ENTER)) return false;
        for (View button : getButtons()) {
            if (isButtonAvailable(button) && button.hasFocus() && button.isPressed()) {
                return button.onKeyUp(keyCode, event);
            }
        }
        return false;
    }

    public void onApplyNavigationBarStatusChange(boolean shown) {
        if (contentPanel == null) return;
        int bottom = shown ? 0 : context.getResources().getDimensionPixelOffset(
                R.dimen.smartisan_rom_menu_dialog_hidden_nav_space);
        contentPanel.setPadding(contentPanel.getPaddingLeft(), contentPanel.getPaddingTop(),
                contentPanel.getPaddingRight(), bottom);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (externalDisplay) return;
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            onApplyNavigationBarStatusChange(true);
        }
        int horizontal = context.getResources().getDimensionPixelOffset(
                R.dimen.smartisan_rom_menu_dialog_horizontal_distance);
        listView.setPadding(horizontal, listView.getPaddingTop(), horizontal,
                listView.getPaddingBottom());
        ViewGroup.MarginLayoutParams params =
                (ViewGroup.MarginLayoutParams) positiveButton.getLayoutParams();
        params.leftMargin = horizontal;
        params.rightMargin = horizontal;
        positiveButton.setLayoutParams(params);
    }

    /** Deprecated compatibility helpers. Choice lists belong to SmartisanAlertDialog. */
    @Deprecated public static ListAdapter singleChoiceAdapter(Context context,
            List<? extends CharSequence> items) {
        return new ChoiceTextAdapter(context, items, false);
    }

    /** Deprecated compatibility helpers. Choice lists belong to SmartisanAlertDialog. */
    @Deprecated public static ListAdapter multiChoiceAdapter(Context context,
            List<? extends CharSequence> items) {
        return new ChoiceTextAdapter(context, items, true);
    }

    private static final class ChoiceTextAdapter extends BaseAdapter {
        private final Context context;
        private final List<? extends CharSequence> items;
        private final boolean multi;
        ChoiceTextAdapter(Context context, List<? extends CharSequence> items, boolean multi) {
            this.context = context;
            this.items = items == null ? Collections.emptyList() : items;
            this.multi = multi;
        }
        @Override public int getCount() { return items.size(); }
        @Override public Object getItem(int position) { return items.get(position); }
        @Override public long getItemId(int position) { return position; }
        @Override public View getView(int position, View convertView, ViewGroup parent) {
            CheckedTextView checked = convertView instanceof CheckedTextView
                    ? (CheckedTextView) convertView : new CheckedTextView(context);
            checked.setCheckMarkDrawable(multi
                    ? R.drawable.smartisan_rom_selector_check_box
                    : R.drawable.smartisan_rom_selector_radio_choice);
            TextView text = checked;
            text.setText(items.get(position));
            text.setTextSize(17f);
            text.setTextColor(0xcc000000);
            text.setGravity(Gravity.CENTER_VERTICAL);
            int padding = Math.round(18 * context.getResources().getDisplayMetrics().density);
            text.setPaddingRelative(padding, 0, padding, 0);
            text.setMinHeight(Math.round(48 * context.getResources().getDisplayMetrics().density));
            text.setBackgroundResource(R.drawable.smartisan_rom_menu_dialog_item_selector);
            return text;
        }
    }
}
