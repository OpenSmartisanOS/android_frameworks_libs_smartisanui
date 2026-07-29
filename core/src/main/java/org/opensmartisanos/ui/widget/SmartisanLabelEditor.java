/* Ported from smartisanos.widget.editor.LabelEditor in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import org.opensmartisanos.ui.R;
import org.opensmartisanos.ui.internal.SmartisanEditorLeftLabelWidget;
import org.opensmartisanos.ui.internal.SmartisanEditorRightIconWidget;

public class SmartisanLabelEditor extends SmartisanEditor {
    public enum LeftIconContainerBgStyle { GRAY }
    private SmartisanEditorLeftLabelWidget leftWidget;
    private SmartisanEditorRightIconWidget rightWidget;
    private final TextWatcher lineChangeWatcher = new TextWatcher() {
        private int lineCount = -1;
        @Override public void beforeTextChanged(CharSequence text, int start, int count, int after) { }
        @Override public void onTextChanged(CharSequence text, int start, int before, int count) {
            int current = editor.getLineCount();
            if (current != 0) {
                editorLineCountChange(current == 1);
                if (current != lineCount && lineCount != -1) editor.requestLayout();
                lineCount = current;
            }
        }
        @Override public void afterTextChanged(Editable editable) { }
    };

    public SmartisanLabelEditor(Context context) { this(context, null); }
    public SmartisanLabelEditor(Context context, AttributeSet attrs) { this(context, attrs, 0); }
    public SmartisanLabelEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray values = context.obtainStyledAttributes(attrs, R.styleable.SmartisanLabelEditor);
        setLeftIcon(values.getDrawable(R.styleable.SmartisanLabelEditor_leftIcon));
        setLeftLabel(values.getText(R.styleable.SmartisanLabelEditor_leftLabel));
        setShowLeftLabelArrow(values.getBoolean(R.styleable.SmartisanLabelEditor_showLeftArrow, false));
        Drawable right = values.getDrawable(R.styleable.SmartisanLabelEditor_rightIcon);
        setRightIcon(right);
        if (right != null) setShowRightDevide(values.getBoolean(R.styleable.SmartisanLabelEditor_showRightDivider, false));
        setRightLabel(values.getText(R.styleable.SmartisanLabelEditor_rightLabel));
        values.recycle();
        editor.addTextChangedListener(lineChangeWatcher);
    }
    @Override protected void onDetachedFromWindow() { editor.removeTextChangedListener(lineChangeWatcher); super.onDetachedFromWindow(); }
    public void setShowLeftWidget(boolean show) { leftWidget.setVisibility(show ? VISIBLE : GONE); }
    public void setOnLeftLabelClickListener(View.OnClickListener listener) { leftWidget.setOnLabelClickListener(listener); }
    public void setOnLeftIconClickListener(View.OnClickListener listener) { leftWidget.setOnIconClickListener(listener); }
    public void setLeftIcon(Drawable drawable) { setContainerLeftPadding(drawable == null ? getResources().getDimensionPixelOffset(R.dimen.smartisan_rom_editor_horizontal_padding) : 0); leftWidget.setIconDrawable(drawable); }
    public void setLeftIconResource(int resId) { setContainerLeftPadding(0); leftWidget.setIconResource(resId); }
    public void setLeftIconContainerBgStyle(LeftIconContainerBgStyle style) {
        int[] backgrounds = {R.drawable.smartisan_rom_editor_left_icon_bg_single, R.drawable.smartisan_rom_editor_left_icon_bg_top, R.drawable.smartisan_rom_editor_left_icon_bg_middle, R.drawable.smartisan_rom_editor_left_icon_bg_bottom};
        if (style == LeftIconContainerBgStyle.GRAY && backgroundStyle >= 1 && backgroundStyle <= 4) leftWidget.setIconContainerBackground(backgrounds[backgroundStyle - 1]);
    }
    public void setLeftLabelClickListener(View.OnClickListener listener) { leftWidget.setOnLabelClickListener(listener); }
    public void setLeftWidgetExpandView(View view) { leftWidget.setRightExpandView(view); }
    public void setLeftLabel(CharSequence text) { leftWidget.setText(text); }
    public void setLeftLabel(int resId) { leftWidget.setText(resId); }
    public void setShowLeftLabelArrow(boolean show) { leftWidget.setArrowVisible(show); }
    public void setLeftLabelWidth(int width) { leftWidget.setLabelWidth(width); }
    public int getLeftLabelWidth() { return (int) (leftWidget.getTextWidth() + 0.5f); }
    public void setLeftWidgetBackgroundResource(int id) { setContainerLeftPadding(0); leftWidget.setBackgroundResource(id); }
    public void setLeftLabelRightMargin(int margin) { leftWidget.setLabelRightMargin(margin); }
    public void setLeftLabelLeftMargin(int margin) { leftWidget.setLabelLeftMargin(margin); }
    public void setLeftLabelGravity(int gravity) { editor.removeTextChangedListener(lineChangeWatcher); leftWidget.setLabelGravity(gravity); }
    public View getLeftWidgetIconView() { return leftWidget.getIconView(); }
    public void setShowRightWidget(boolean show) { rightWidget.setVisibility(show ? VISIBLE : GONE); }
    public void setRightLabel(CharSequence text) { rightWidget.setText(text); }
    public void setRightLabel(int resId) { rightWidget.setText(resId); }
    public void setRightIcon(Drawable drawable) { rightWidget.setIconDrawable(drawable); }
    public void setRightIconResource(int resId) { rightWidget.setIconResource(resId); }
    public void setRightIconContentDescription(String description) { rightWidget.setIconContentDescription(description); }
    public void setOnRightWidgetClickListener(View.OnClickListener listener) { rightWidget.setOnClickListener(listener); }
    public void setRightWidgetWidth(int width) { rightWidget.setWidth(width); }
    public void setShowRightDevide(boolean show) { if (show) setContainerRightPadding(0); rightWidget.setDevideVisible(show); }
    public void setShowLeftDivider(boolean show) { leftWidget.setShowDivider(show); }
    public void addSpecialEditorRightExpandView(View view) {
        if (editor instanceof SmartisanQuickDeleteEditText) {
            ViewGroup container = midContainer.findViewById(R.id.smartisan_rom_rightExpandView);
            container.removeAllViews(); if (view != null) container.addView(view);
        }
    }
    @Override protected int getDefaultLeftLayout() { return R.layout.smartisan_rom_label_editor_left_layout; }
    @Override protected int getDefaultRightLayout() { return R.layout.smartisan_rom_label_editor_right_layout; }
    @Override protected void initLeftWidget() { leftWidget = findViewById(R.id.smartisan_rom_left_widget); }
    @Override protected void initRightWidget() { rightWidget = findViewById(R.id.smartisan_rom_right_widget); }
    @Override public void setSingleLine(boolean singleLine) { super.setSingleLine(singleLine); editorLineCountChange(singleLine); }
    private void editorLineCountChange(boolean singleLine) { setLeftContainerCenterVertical(singleLine); leftWidget.setLabelGravity(singleLine ? Gravity.CENTER_VERTICAL : Gravity.TOP); }
    public View getLeftContainer() { return leftWidget; }
    public View getRightContainer() { return rightWidget; }
}
