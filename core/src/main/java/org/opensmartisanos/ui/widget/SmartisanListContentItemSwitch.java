/* Ported from smartisanos.widget.ListContentItemSwitch in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import org.opensmartisanos.ui.R;

public class SmartisanListContentItemSwitch extends SmartisanListContentItem {
    public interface SwitcherCallback { void onDisabledSwitchClicked(); }

    private SmartisanSwitch smartisanSwitch;
    private LinearLayout rightSlot;
    private CompoundButton.OnCheckedChangeListener checkedChangeListener;
    private DisabledClickDelegate disabledClickDelegate;

    public SmartisanListContentItemSwitch(Context context) { this(context, null); }
    public SmartisanListContentItemSwitch(Context context, AttributeSet attrs) { this(context, attrs, 0); }
    public SmartisanListContentItemSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray values = context.obtainStyledAttributes(attrs,
                R.styleable.SmartisanListContentItemSwitch, defStyleAttr, 0);
        boolean checked = values.getBoolean(
                R.styleable.SmartisanListContentItemSwitch_isChecked, false);
        values.recycle();
        if (!customRightView) {
            setClickable(false);
            setFocusable(false);
            smartisanSwitch.setChecked(checked);
            smartisanSwitch.setOnCheckedChangeListener((button, value) -> {
                if (checkedChangeListener != null) checkedChangeListener.onCheckedChanged(button, value);
            });
            setSaveFromParentEnabled(false);
            setPressable(false);
        }
    }

    @Override protected int getDefaultRightLayout() {
        return R.layout.smartisan_rom_list_content_right_switch;
    }
    @Override protected void initRightWidget() {
        smartisanSwitch = findViewById(R.id.smartisan_rom_switchex);
        rightSlot = findViewById(R.id.smartisan_rom_rightExpandView);
    }
    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        checkedChangeListener = listener;
    }
    public void setChecked(boolean checked) {
        if (customRightView) return;
        CompoundButton.OnCheckedChangeListener saved = checkedChangeListener;
        checkedChangeListener = null;
        smartisanSwitch.setChecked(checked);
        checkedChangeListener = saved;
    }
    public void setCheckedWithListenerCallback(boolean checked) {
        if (!customRightView) smartisanSwitch.setChecked(checked);
    }
    public boolean isChecked() { return !customRightView && smartisanSwitch.isChecked(); }
    public SmartisanSwitch getSwitch() { return smartisanSwitch; }

    @Override public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!customRightView && smartisanSwitch != null) {
            smartisanSwitch.setEnabled(enabled);
            if (summary != null) summary.setEnabled(enabled);
            if (title != null) title.setEnabled(enabled);
        }
    }

    @Override protected Parcelable onSaveInstanceState() {
        SavedState state = new SavedState(super.onSaveInstanceState());
        state.checked = isChecked();
        return state;
    }
    @Override protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState saved = (SavedState) state;
        super.onRestoreInstanceState(saved.getSuperState());
        setChecked(saved.checked);
        requestLayout();
    }

    @Deprecated public void setSwitcherCallback(SwitcherCallback callback) {
        if (callback == null) disabledClickDelegate = null;
        else if (disabledClickDelegate == null) disabledClickDelegate = new DisabledClickDelegate(callback);
        else disabledClickDelegate.delegate = callback;
        setOnDisabledClickListener(disabledClickDelegate);
    }

    @Override protected boolean shouldHandleDisabledTouchEvent(MotionEvent event) {
        if (smartisanSwitch == null || smartisanSwitch.isEnabled()
                || (disabledClickDelegate == null && disabledReasonStringId <= 0)) return false;
        Rect bounds = new Rect();
        smartisanSwitch.getGlobalVisibleRect(bounds);
        return bounds.contains((int) event.getRawX(), (int) event.getRawY());
    }

    public void setRightExpandView(View view) {
        if (customRightView) return;
        rightSlot.removeAllViews();
        if (view == null) rightSlot.setVisibility(GONE);
        else { rightSlot.addView(view); rightSlot.setVisibility(VISIBLE); }
    }

    private static final class DisabledClickDelegate implements OnDisabledClickListener {
        private SwitcherCallback delegate;
        DisabledClickDelegate(SwitcherCallback value) { delegate = value; }
        @Override public void onDisabledClick() {
            if (delegate != null) delegate.onDisabledSwitchClicked();
        }
    }

    static final class SavedState extends BaseSavedState {
        static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            @Override public SavedState createFromParcel(Parcel source) { return new SavedState(source); }
            @Override public SavedState[] newArray(int size) { return new SavedState[size]; }
        };
        boolean checked;
        SavedState(Parcel source) { super(source); checked = source.readInt() != 0; }
        SavedState(Parcelable state) { super(state); }
        @Override public void writeToParcel(Parcel destination, int flags) {
            super.writeToParcel(destination, flags);
            destination.writeInt(checked ? 1 : 0);
        }
    }
}
