package org.opensmartisanos.ui.app;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.opensmartisanos.ui.R;

/**
 * Public-SDK implementation of the Smartisan system alert-dialog contract.
 *
 * <p>The public surface deliberately follows {@link android.app.AlertDialog}: callers use a
 * builder, while a package-private controller owns layout, choices, buttons and state. This keeps
 * the component portable without depending on private framework controllers.
 */
public class SmartisanAlertDialog extends Dialog {
    public static final int BUTTON_POSITIVE = DialogInterface.BUTTON_POSITIVE;
    public static final int BUTTON_NEGATIVE = DialogInterface.BUTTON_NEGATIVE;
    public static final int BUTTON_NEUTRAL = DialogInterface.BUTTON_NEUTRAL;

    private final SmartisanAlertController controller;

    private SmartisanAlertDialog(Context context, int themeResId,
            SmartisanAlertController.Params params) {
        super(context, themeResId == 0 ? R.style.Theme_SmartisanUi_AlertDialog : themeResId);
        controller = new SmartisanAlertController(this, params);
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        controller.installContent(state);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (controller.onKeyDown(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (controller.onKeyUp(keyCode, event)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        controller.saveState(state);
        return state;
    }

    public Button getButton(int which) {
        return controller.getButton(which);
    }

    public ListView getListView() {
        return controller.getListView();
    }

    public View getCustomView() {
        return controller.getCustomView();
    }

    @Override public void setTitle(CharSequence title) {
        super.setTitle(title);
        if (controller != null) controller.setTitle(title);
    }

    public void setMessage(CharSequence message) { controller.setMessage(message); }
    public void setIcon(int resourceId) {
        controller.setIcon(resourceId == 0 ? null : getContext().getDrawable(resourceId));
    }
    public void setIcon(Drawable icon) { controller.setIcon(icon); }

    public static final class Builder {
        private final Context context;
        private final int themeResId;
        private final SmartisanAlertController.Params params =
                new SmartisanAlertController.Params();

        public Builder(Context context) {
            this(context, 0);
        }

        public Builder(Context context, int themeResId) {
            int resolvedTheme = themeResId == 0
                    ? R.style.Theme_SmartisanUi_AlertDialog : themeResId;
            this.context = new ContextThemeWrapper(context, resolvedTheme);
            this.themeResId = themeResId;
        }

        public Context getContext() {
            return context;
        }

        public Builder setTitle(int resourceId) {
            return setTitle(context.getText(resourceId));
        }

        public Builder setTitle(CharSequence title) {
            params.title = title;
            return this;
        }

        public Builder setCustomTitle(View customTitle) {
            params.customTitle = customTitle;
            return this;
        }

        public Builder setMessage(int resourceId) {
            return setMessage(context.getText(resourceId));
        }

        public Builder setMessage(CharSequence message) {
            params.message = message;
            return this;
        }

        public Builder setIcon(int resourceId) {
            params.icon = resourceId == 0 ? null : context.getDrawable(resourceId);
            return this;
        }

        public Builder setIcon(Drawable icon) {
            params.icon = icon;
            return this;
        }

        public Builder setIconAttribute(int attributeId) {
            android.util.TypedValue value = new android.util.TypedValue();
            if (context.getTheme().resolveAttribute(attributeId, value, true)) {
                setIcon(value.resourceId);
            }
            return this;
        }

        public Builder setView(int layoutResourceId) {
            params.customView = null;
            params.customViewLayoutResId = layoutResourceId;
            params.viewSpacingSpecified = false;
            return this;
        }

        public Builder setView(View view) {
            params.customView = view;
            params.customViewLayoutResId = 0;
            params.viewSpacingSpecified = false;
            return this;
        }

        public Builder setView(View view, int viewSpacingLeft, int viewSpacingTop,
                int viewSpacingRight, int viewSpacingBottom) {
            setView(view);
            params.viewSpacingSpecified = true;
            params.viewSpacingLeft = viewSpacingLeft;
            params.viewSpacingTop = viewSpacingTop;
            params.viewSpacingRight = viewSpacingRight;
            params.viewSpacingBottom = viewSpacingBottom;
            return this;
        }

        public Builder setPositiveButton(int resourceId,
                DialogInterface.OnClickListener listener) {
            return setPositiveButton(context.getText(resourceId), listener);
        }

        public Builder setPositiveButton(CharSequence text,
                DialogInterface.OnClickListener listener) {
            params.positiveText = text;
            params.positiveListener = listener;
            return this;
        }

        public Builder setNegativeButton(int resourceId,
                DialogInterface.OnClickListener listener) {
            return setNegativeButton(context.getText(resourceId), listener);
        }

        public Builder setNegativeButton(CharSequence text,
                DialogInterface.OnClickListener listener) {
            params.negativeText = text;
            params.negativeListener = listener;
            return this;
        }

        public Builder setNeutralButton(int resourceId,
                DialogInterface.OnClickListener listener) {
            return setNeutralButton(context.getText(resourceId), listener);
        }

        public Builder setNeutralButton(CharSequence text,
                DialogInterface.OnClickListener listener) {
            params.neutralText = text;
            params.neutralListener = listener;
            return this;
        }

        public Builder setDestructivePositive(boolean destructive) {
            params.destructivePositive = destructive;
            return this;
        }

        public Builder setItems(CharSequence[] items, DialogInterface.OnClickListener listener) {
            params.clearListContent();
            // Framework AlertParams keeps the caller's array; preserve that observable contract.
            params.items = items;
            params.itemListener = listener;
            return this;
        }

        public Builder setItems(int resourceId, DialogInterface.OnClickListener listener) {
            return setItems(context.getResources().getTextArray(resourceId), listener);
        }

        public Builder setAdapter(ListAdapter adapter, DialogInterface.OnClickListener listener) {
            params.clearListContent();
            params.adapter = adapter;
            params.itemListener = listener;
            return this;
        }

        public Builder setCursor(Cursor cursor, DialogInterface.OnClickListener listener,
                String labelColumn) {
            params.clearListContent();
            params.cursor = cursor;
            params.labelColumn = labelColumn;
            params.itemListener = listener;
            return this;
        }

        public Builder setSingleChoiceItems(CharSequence[] items, int checkedItem,
                DialogInterface.OnClickListener listener) {
            params.clearListContent();
            params.items = items;
            params.singleChoice = true;
            params.checkedItem = checkedItem;
            params.itemListener = listener;
            return this;
        }

        public Builder setSingleChoiceItems(int resourceId, int checkedItem,
                DialogInterface.OnClickListener listener) {
            return setSingleChoiceItems(context.getResources().getTextArray(resourceId),
                    checkedItem, listener);
        }

        public Builder setSingleChoiceItems(ListAdapter adapter, int checkedItem,
                DialogInterface.OnClickListener listener) {
            params.clearListContent();
            params.adapter = adapter;
            params.singleChoice = true;
            params.checkedItem = checkedItem;
            params.itemListener = listener;
            return this;
        }

        public Builder setSingleChoiceItems(Cursor cursor, int checkedItem,
                String labelColumn, DialogInterface.OnClickListener listener) {
            params.clearListContent();
            params.cursor = cursor;
            params.labelColumn = labelColumn;
            params.singleChoice = true;
            params.checkedItem = checkedItem;
            params.itemListener = listener;
            return this;
        }

        public Builder setMultiChoiceItems(CharSequence[] items, boolean[] checkedItems,
                DialogInterface.OnMultiChoiceClickListener listener) {
            params.clearListContent();
            params.items = items;
            params.multiChoice = true;
            // The platform API updates this caller-owned array as choices change.
            params.checkedItems = checkedItems;
            params.multiChoiceListener = listener;
            return this;
        }

        public Builder setMultiChoiceItems(int resourceId, boolean[] checkedItems,
                DialogInterface.OnMultiChoiceClickListener listener) {
            return setMultiChoiceItems(context.getResources().getTextArray(resourceId),
                    checkedItems, listener);
        }

        public Builder setMultiChoiceItems(Cursor cursor, String isCheckedColumn,
                String labelColumn, DialogInterface.OnMultiChoiceClickListener listener) {
            params.clearListContent();
            params.cursor = cursor;
            params.isCheckedColumn = isCheckedColumn;
            params.labelColumn = labelColumn;
            params.multiChoice = true;
            params.multiChoiceListener = listener;
            return this;
        }

        public Builder setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {
            params.itemSelectedListener = listener;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            params.cancelable = cancelable;
            return this;
        }

        public Builder setOnCancelListener(DialogInterface.OnCancelListener listener) {
            params.cancelListener = listener;
            return this;
        }

        public Builder setOnDismissListener(DialogInterface.OnDismissListener listener) {
            params.dismissListener = listener;
            return this;
        }

        public Builder setOnShowListener(DialogInterface.OnShowListener listener) {
            params.showListener = listener;
            return this;
        }

        public Builder setOnKeyListener(DialogInterface.OnKeyListener listener) {
            params.keyListener = listener;
            return this;
        }

        public SmartisanAlertDialog create() {
            SmartisanAlertController.Params copy =
                    new SmartisanAlertController.Params(params);
            SmartisanAlertDialog dialog =
                    new SmartisanAlertDialog(context, themeResId, copy);
            dialog.setCancelable(copy.cancelable);
            dialog.setCanceledOnTouchOutside(copy.cancelable);
            dialog.setOnCancelListener(copy.cancelListener);
            dialog.setOnDismissListener(copy.dismissListener);
            dialog.setOnShowListener(copy.showListener);
            if (copy.keyListener != null) {
                dialog.setOnKeyListener(copy.keyListener);
            }
            return dialog;
        }

        public SmartisanAlertDialog show() {
            SmartisanAlertDialog dialog = create();
            dialog.show();
            return dialog;
        }
    }
}
