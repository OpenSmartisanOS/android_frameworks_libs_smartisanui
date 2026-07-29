/* Ported from smartisanos.widget.SearchBar in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.opensmartisanos.ui.R;
import org.opensmartisanos.ui.internal.SmartisanBarsHelper;

public class SmartisanSearchBar extends RelativeLayout implements View.OnClickListener {
    public interface AnimationListener { void onAnimationStart(); void onAnimationEnd(); }
    public interface OnCancelClickListener { void onClick(View view); }
    public interface OnEditorClickListener { void onClick(View view); }
    public interface OnFilterClickListener { void onClick(View view); }
    public interface OnSearchIconClickListener { void onClick(View view); }
    public interface OnSecondaryFilterClickListener { void onClick(View view); }
    public interface OnCommitCompletionListener { void onCommitCompletion(CompletionInfo completion); }
    public interface OnQueryTextListener { void onQueryTextChanged(String query); }

    private final SmartisanSearchEditText editor;
    private final RelativeLayout editLayout;
    private final View leftIcon;
    private final View clearView;
    private final ImageView cancelView;
    private final LinearLayout rightContainer;
    private final LinearLayout secondaryFilter;
    private final TextView secondaryFilterText;
    private final View shadowView;
    private boolean searchMode;
    private boolean playingAnimation;
    private boolean autoFocus = true;
    private boolean withAnimation = true;
    private boolean searchEnabled = true;
    private boolean imageScaleEnabled = true;
    private boolean secondaryFilterEnabled;
    private AnimationListener animationListener;
    private OnCancelClickListener cancelClickListener;
    private OnEditorClickListener editorClickListener;
    private OnFilterClickListener filterClickListener;
    private OnSearchIconClickListener searchIconClickListener;
    private OnSecondaryFilterClickListener secondaryFilterClickListener;
    private OnQueryTextListener queryTextListener;

    public SmartisanSearchBar(Context context) { this(context, null); }
    public SmartisanSearchBar(Context context, AttributeSet attrs) { this(context, attrs, 0); }
    public SmartisanSearchBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundColor(0xffffffff);
        setMinimumHeight(dp(48));
        LayoutInflater.from(context).inflate(R.layout.smartisan_rom_search_bar, this, true);
        rightContainer = findViewById(R.id.smartisan_search_bar_right_view_container);
        cancelView = findViewById(R.id.smartisan_search_bar_cancel_button);
        editLayout = findViewById(R.id.smartisan_search_bar_edit_layout);
        leftIcon = findViewById(R.id.smartisan_search_bar_left_icon);
        clearView = findViewById(R.id.smartisan_search_bar_clear_text);
        secondaryFilter = findViewById(R.id.smartisan_search_bar_secondary_filter);
        secondaryFilterText = findViewById(R.id.smartisan_search_bar_secondary_filter_btn);
        editor = findViewById(R.id.smartisan_search_bar_edit_text);

        shadowView = new View(context);
        shadowView.setBackgroundResource(R.drawable.smartisan_rom_title_bar_shadow);
        LayoutParams shadowParams = new LayoutParams(LayoutParams.MATCH_PARENT, dp(14));
        shadowParams.addRule(ALIGN_PARENT_BOTTOM);
        shadowView.setTranslationY(dp(14));
        addView(shadowView, shadowParams);
        bindEvents();
        applySecondaryFilterState();
    }

    private void bindEvents() {
        editLayout.setOnClickListener(this); editor.setOnClickListener(this); leftIcon.setOnClickListener(this);
        clearView.setOnClickListener(this); cancelView.setOnClickListener(this); secondaryFilter.setOnClickListener(this);
        SmartisanBarsHelper.setBarIconScaleTouchListener(cancelView);
        editor.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearView.setVisibility(s.length() == 0 ? GONE : VISIBLE);
                if (queryTextListener != null) queryTextListener.onQueryTextChanged(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        editor.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) hideKeyboard();
            return false;
        });
    }

    private ImageView icon(int resource) {
        ImageView view = new ImageView(getContext());
        view.setImageResource(resource);
        view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        return view;
    }
    private int dp(float value) { return (int) (value * getResources().getDisplayMetrics().density + 0.5f); }

    @Override public void onClick(View view) {
        if (!searchEnabled) return;
        if (view == clearView) editor.setText("");
        else if (view == cancelView) onClickCancelView(true);
        else if (view == leftIcon) { if (searchIconClickListener != null) searchIconClickListener.onClick(view); }
        else if (view == secondaryFilter) {
            if (secondaryFilterClickListener != null) secondaryFilterClickListener.onClick(view);
            if (filterClickListener != null) filterClickListener.onClick(view);
        } else onClickSearchEditor(true);
    }

    public void onClickSearchEditor(boolean animation) {
        if (editorClickListener != null) editorClickListener.onClick(editor);
        if (!searchMode) startAnimation(true, animation);
        editor.setFocusableInTouchMode(true);
        editor.setFocusable(true);
        editor.requestFocus();
        editor.setCursorVisible(true);
        if (autoFocus) showKeyboard();
    }

    public void onClickCancelView(boolean animation) {
        if (cancelClickListener != null) cancelClickListener.onClick(cancelView);
        editor.setText("");
        hideKeyboard();
        editor.clearFocus();
        editor.setFocusable(false);
        editor.setFocusableInTouchMode(false);
        editor.setCursorVisible(false);
        startAnimation(false, animation);
    }

    private void startAnimation(boolean toSearchMode, boolean requestedAnimation) {
        if (playingAnimation || searchMode == toSearchMode) return;
        searchMode = toSearchMode;
        if (toSearchMode) applySecondaryFilterState();
        boolean animate = requestedAnimation && withAnimation && isLaidOut();
        if (!animate) { applyMode(); return; }
        playingAnimation = true;
        if (animationListener != null) animationListener.onAnimationStart();
        cancelView.setVisibility(VISIBLE);
        float end = toSearchMode ? 1f : 0f;
        if (toSearchMode) cancelView.setAlpha(0f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(ObjectAnimator.ofFloat(cancelView, ALPHA, cancelView.getAlpha(), end),
                ObjectAnimator.ofFloat(rightContainer, ALPHA, rightContainer.getAlpha(), 1f - end));
        set.setDuration(300L);
        set.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                playingAnimation = false; applyMode();
                if (animationListener != null) animationListener.onAnimationEnd();
            }
        });
        set.start();
    }

    private void applyMode() {
        cancelView.setVisibility(searchMode ? VISIBLE : GONE);
        cancelView.setAlpha(1f);
        rightContainer.setVisibility(searchMode ? GONE : (rightContainer.getChildCount() == 0 ? GONE : VISIBLE));
        rightContainer.setAlpha(1f);
        LayoutParams params = (LayoutParams) editLayout.getLayoutParams();
        params.removeRule(START_OF);
        params.addRule(START_OF, searchMode ? cancelView.getId() : rightContainer.getId());
        if (!searchMode && rightContainer.getChildCount() == 0) params.rightMargin = dp(6);
        editLayout.setLayoutParams(params);
        applySecondaryFilterState();
    }

    private void applySecondaryFilterState() {
        boolean showFilter = secondaryFilterEnabled && !searchMode;
        secondaryFilter.setVisibility(secondaryFilterEnabled
                ? (showFilter ? VISIBLE : INVISIBLE) : GONE);
        LayoutParams params = (LayoutParams) editor.getLayoutParams();
        params.removeRule(START_OF);
        params.addRule(START_OF, showFilter ? secondaryFilter.getId() : clearView.getId());
        editor.setLayoutParams(params);
    }

    public void addShadow() { shadowView.setVisibility(VISIBLE); }
    public void removeShadow() { shadowView.setVisibility(GONE); }
    public View getShadowView() { return shadowView; }
    public boolean isSearchMode() { return searchMode; }
    public boolean isPlayingAnimation() { return playingAnimation; }
    public boolean isAutoFocus() { return autoFocus; }
    public void setAutoFocus(boolean value) { autoFocus = value; }
    public boolean isWithAnimation() { return withAnimation; }
    public void setWithAnimation(boolean value) { withAnimation = value; }
    public SmartisanSearchEditText getSearchEditor() { return editor; }
    public View getSearchEditLayout() { return editLayout; }
    public View getSearchLeftIcon() { return leftIcon; }
    public View getCancelView() { return cancelView; }
    public View getClearView() { return clearView; }
    public View getSecondaryFilterLayout() { return secondaryFilter; }
    public void setSearchLeftIcon(Drawable icon) { leftIcon.setBackground(icon); }
    public void setSearchLeftIcon(int resource) { leftIcon.setBackgroundResource(resource); }
    public void setCancelViewVisibility(int visibility) { cancelView.setVisibility(visibility); }
    public void setSecondaryFilterVisibility(int visibility) {
        secondaryFilterEnabled = visibility == VISIBLE;
        applySecondaryFilterState();
    }
    public void setSecondaryFilterText(CharSequence text) { secondaryFilterText.setText(text); }
    public void setSecondaryFilterText(int resource) { secondaryFilterText.setText(resource); }
    public void setSearchEnabled(boolean enabled) { searchEnabled = enabled; setEnabled(enabled); }
    public void setHint(CharSequence hint) { editor.setHint(hint); }
    public CharSequence getQuery() { return editor.getText(); }
    public void setQuery(CharSequence query) { editor.setText(query); editor.setSelection(editor.length()); }
    public void hideKeyboard() { ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(editor.getWindowToken(), 0); }
    public void showKeyboard() { editor.post(() -> ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(editor, InputMethodManager.SHOW_IMPLICIT)); }
    public ImageView addRightImageView(int resource) {
        ImageView view = icon(resource);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(36), dp(36));
        if (rightContainer.getChildCount() > 0) params.leftMargin = dp(6);
        rightContainer.addView(view, params);
        view.setOnClickListener(v -> { if (filterClickListener != null) filterClickListener.onClick(v); });
        if (imageScaleEnabled) SmartisanBarsHelper.setBarIconScaleTouchListener(view);
        applyMode(); return view;
    }
    public void removeRightView(View view) { rightContainer.removeView(view); applyMode(); }
    public void removeAllRightImageViews() { rightContainer.removeAllViews(); applyMode(); }
    public void setImageScaleEnabled(boolean enabled) { imageScaleEnabled = enabled; }
    public void avoidImageViewScale(ImageView target) { SmartisanBarsHelper.setBarIconScaleTouchListener(target, false); }
    public void setAnimationListener(AnimationListener listener) { animationListener = listener; }
    public void setOnCancelClickListener(OnCancelClickListener listener) { cancelClickListener = listener; }
    public void setOnEditorClickListener(OnEditorClickListener listener) { editorClickListener = listener; }
    public void setOnFilterClickListener(OnFilterClickListener listener) { filterClickListener = listener; }
    public void setOnSearchIconClickListener(OnSearchIconClickListener listener) { searchIconClickListener = listener; }
    public void setOnSecondaryFilterClickListener(OnSecondaryFilterClickListener listener) { secondaryFilterClickListener = listener; }
    public void setOnCommitCompletionListener(OnCommitCompletionListener listener) { editor.setOnCommitCompletionListener(listener == null ? null : listener::onCommitCompletion); }
    public void setOnQueryTextListener(OnQueryTextListener listener) { queryTextListener = listener; }
}
