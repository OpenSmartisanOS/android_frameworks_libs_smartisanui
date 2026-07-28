/* Ported from smartisanos.widget.SettingItemSwitch in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewStub;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.opensmartisanos.ui.R;

public class SmartisanSettingItemSwitch extends SmartisanListContentItemSwitch {
    private ImageView infoButton;
    private ViewStub infoButtonStub;
    private CompoundButton.OnCheckedChangeListener realCheckedChangeListener;
    private View.OnClickListener switchClickListener;
    private boolean switchAnimating;

    public SmartisanSettingItemSwitch(Context context) { this(context, null); }
    public SmartisanSettingItemSwitch(Context context, AttributeSet attrs) { this(context, attrs, 0); }
    public SmartisanSettingItemSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
    }
    @Override protected int getDefaultMidLayout() {
        return R.layout.smartisan_rom_setting_item_mid_layout;
    }
    @Override protected void initMidWidget() {
        super.initMidWidget();
        infoButtonStub = findViewById(R.id.smartisan_rom_info_btn_viewstub);
    }
    @Override protected void initRightWidget() {
        super.initRightWidget();
        SmartisanSwitch view = getSwitch();
        super.setOnCheckedChangeListener((button, checked) -> {
            if (realCheckedChangeListener != null) {
                if (button.getTag() == null) button.setTag(getId());
                button.sendAccessibilityEvent(1);
                realCheckedChangeListener.onCheckedChanged(button, checked);
            }
        });
        view.setOnClickListener(clicked -> {
            switchAnimating = true;
            removeCallbacks(resetAnimationState);
            postDelayed(resetAnimationState, 300L);
            if (switchClickListener != null) switchClickListener.onClick(clicked);
        });
        view.setOnTouchListener((ignored, event) -> switchAnimating);
    }
    private final Runnable resetAnimationState = () -> switchAnimating = false;

    public void setIconMarginRight(int margin) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) leftContainer.getLayoutParams();
        params.rightMargin = margin;
        leftContainer.setLayoutParams(params);
    }
    public void setIconMarginRightDimen(int dimension) {
        setIconMarginRight(dimension > 0 ? getResources().getDimensionPixelOffset(dimension) : 0);
    }
    @Deprecated public void setIconResource(int resource) { setIcon(resource); }
    @Override public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        realCheckedChangeListener = listener;
    }
    public void setupInfoButton(boolean visible, View.OnClickListener listener) {
        if (visible) {
            if (infoButton == null) infoButton = (ImageView) infoButtonStub.inflate();
            infoButton.setVisibility(VISIBLE);
            infoButton.setOnClickListener(listener);
        } else if (infoButton != null) infoButton.setVisibility(GONE);
    }
    @Deprecated public void setSwitchEnable(boolean enabled) { setEnabled(enabled); }
    public void setSwitchDisabledTips(int stringId) { setDisabledTips(stringId); }
    public void setSwitchClickListener(View.OnClickListener listener) { switchClickListener = listener; }
    @Override protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(resetAnimationState);
    }
}
