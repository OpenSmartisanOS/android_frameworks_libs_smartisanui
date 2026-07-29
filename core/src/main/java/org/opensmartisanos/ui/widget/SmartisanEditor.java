/* Ported from smartisanos.widget.editor.AbsEditor in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.opensmartisanos.ui.R;

public abstract class SmartisanEditor extends LinearLayout implements View.OnClickListener {
    public static final int BG_STYLE_SINGLE = 1, BG_STYLE_TOP = 2, BG_STYLE_MIDDLE = 3, BG_STYLE_BOTTOM = 4;
    public static final int EDITOR_TYPE_DEFAULT = 0, EDITOR_TYPE_PASSWORD = 1, EDITOR_TYPE_QUICK_DELETE = 2;
    protected int backgroundStyle;
    protected EditText editor;
    protected LinearLayout midContainer;
    private int editorType;
    private FrameLayout leftContainer;
    private LinearLayout rightContainer;
    private InputMethodManager inputMethodManager;

    protected abstract int getDefaultLeftLayout();
    protected abstract int getDefaultRightLayout();
    protected abstract void initLeftWidget();
    protected abstract void initRightWidget();
    public SmartisanEditor(Context context) { this(context, null); }
    public SmartisanEditor(Context context, AttributeSet attrs) { this(context, attrs, 0); }
    public SmartisanEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr); setOrientation(HORIZONTAL);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.smartisan_rom_abs_editor_layout, this, true);
        leftContainer = findViewById(R.id.smartisan_rom_left_container);
        midContainer = findViewById(R.id.smartisan_rom_mid_container);
        rightContainer = findViewById(R.id.smartisan_rom_right_container);
        TypedArray values = context.obtainStyledAttributes(attrs, R.styleable.SmartisanEditor, defStyleAttr, 0);
        editorType = values.getInt(R.styleable.SmartisanEditor_editorType, EDITOR_TYPE_DEFAULT);
        backgroundStyle = values.getInt(R.styleable.SmartisanEditor_backgroundStyle, -1);
        int left = getDefaultLeftLayout(), right = getDefaultRightLayout();
        boolean onlyEditor = left <= 0 && right <= 0;
        if (left > 0) inflater.inflate(left, leftContainer); else setLeftContainerVisible(false);
        inflater.inflate(getDefaultMidLayout(), midContainer);
        if (right > 0) inflater.inflate(right, rightContainer);
        initLeftWidget(); initMidWidget(); initRightWidget();
        editor.setHint(values.getText(R.styleable.SmartisanEditor_editorHint));
        setSingleLine(values.getBoolean(R.styleable.SmartisanEditor_editorSingleLine, true));
        setEditable(values.getBoolean(R.styleable.SmartisanEditor_editorEditable, true));
        if (onlyEditor) setOnClickListener(this);
        values.recycle(); requestLayout();
    }
    public void setEditorType(int type) { if (type < 0 || type > 2) type = 0; if (type != editorType) { editorType = type; reInflateMidView(); } }
    private void reInflateMidView() { midContainer.removeAllViews(); LayoutInflater.from(getContext()).inflate(getDefaultMidLayout(), midContainer); initMidWidget(); }
    private void setLeftContainerVisible(boolean visible) {
        leftContainer.setVisibility(visible ? VISIBLE : GONE);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) midContainer.getLayoutParams();
        if (visible) { params.removeRule(RelativeLayout.ALIGN_PARENT_LEFT); params.addRule(RelativeLayout.RIGHT_OF, R.id.smartisan_rom_left_container); }
        else params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        midContainer.setLayoutParams(params);
    }
    protected void initMidWidget() { editor = findViewById(R.id.smartisan_rom_editor); editor.setId(generateViewId()); setRightPaddingForSpecialEditor(); }
    public void setParagraphMode(boolean paragraph) {
        setSingleLine(!paragraph);
        int margin = getResources().getDimensionPixelSize(paragraph ? R.dimen.smartisan_rom_editor_large_vertical_margin : R.dimen.smartisan_rom_editor_small_vertical_margin);
        setEditorMarginVertically(margin, margin); editor.setGravity(paragraph ? Gravity.TOP : Gravity.CENTER_VERTICAL);
    }
    public EditText getEditText() { return editor; }
    public EditText getEditor() { return editor; }
    public void setEditable(boolean enabled) { editor.setFocusable(enabled); editor.setFocusableInTouchMode(enabled); }
    public void setEditorMarginVertically(int top, int bottom) { LayoutParams params = (LayoutParams) editor.getLayoutParams(); params.topMargin = top; params.bottomMargin = bottom; editor.setLayoutParams(params); }
    public void setText(CharSequence text) { editor.setText(text); }
    public void setText(int resId) { setText(getResources().getString(resId)); }
    public void setInputType(int type) { editor.setInputType(type); }
    public void setTransformationMethod(TransformationMethod method) { editor.setTransformationMethod(method); }
    public void setSingleLine(boolean singleLine) { if (editorType != EDITOR_TYPE_PASSWORD) editor.setSingleLine(singleLine); }
    public void setLeftContainerCenterVertical(boolean center) { RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) leftContainer.getLayoutParams(); if (center) params.addRule(RelativeLayout.CENTER_VERTICAL); else params.removeRule(RelativeLayout.CENTER_VERTICAL); leftContainer.setLayoutParams(params); }
    public void setEditorGravity(int gravity) { editor.setGravity(gravity); }
    protected int getDefaultMidLayout() { return editorType == EDITOR_TYPE_PASSWORD ? R.layout.smartisan_rom_pwd_edit_text : editorType == EDITOR_TYPE_QUICK_DELETE ? R.layout.smartisan_rom_quick_del_edit_text : R.layout.smartisan_rom_edit_text; }
    private void setRightPaddingForSpecialEditor() {
        int padding = getResources().getDimensionPixelOffset(R.dimen.smartisan_rom_editor_text_icon_horizontal_margin);
        if (editor instanceof SmartisanPasswordEditText) { ((SmartisanPasswordEditText) editor).setEyePaddingLeft(padding); ((SmartisanPasswordEditText) editor).setEyePaddingRight(0); }
        else if (editor instanceof SmartisanQuickDeleteEditText) { ((SmartisanQuickDeleteEditText) editor).setIconPaddingLeft(padding); ((SmartisanQuickDeleteEditText) editor).setIconPaddingRight(0); }
    }
    public void setBackgroundStyle(int style) {
        if (backgroundStyle == style) return;
        int resource = style == 1 ? R.drawable.smartisan_rom_editor_bg_single : style == 2 ? R.drawable.smartisan_rom_editor_bg_top : style == 3 ? R.drawable.smartisan_rom_editor_bg_middle : style == 4 ? R.drawable.smartisan_rom_editor_bg_bottom : 0;
        if (resource != 0) { backgroundStyle = style; setBackgroundResource(resource); }
    }
    public void addRightViewComponent(View view) { FrameLayout container = findViewById(R.id.smartisan_rom_right_view_component); if (view != null) { container.setVisibility(VISIBLE); container.addView(view); } }
    protected void setContainerRightPadding(int padding) { View view = findViewById(R.id.smartisan_rom_container); view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), padding, view.getPaddingBottom()); }
    protected void setContainerLeftPadding(int padding) { View view = findViewById(R.id.smartisan_rom_container); view.setPadding(padding, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom()); }
    @Override public void onClick(View view) { editor.requestFocus(); if (editor.isFocused()) { if (inputMethodManager == null) inputMethodManager = getContext().getSystemService(InputMethodManager.class); inputMethodManager.showSoftInput(editor, 0); } }
}
