/* Ported from smartisanos.widget.SettingItemText in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.opensmartisanos.ui.R;

public class SmartisanSettingItemText extends SmartisanListContentItemText {
    private ImageView badgeImageView;
    private ImageView infoButton;
    private ViewStub infoButtonStub;

    public SmartisanSettingItemText(Context context) { this(context, null); }
    public SmartisanSettingItemText(Context context, AttributeSet attrs) { this(context, attrs, 0); }
    public SmartisanSettingItemText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override protected int getDefaultMidLayout() {
        return R.layout.smartisan_rom_setting_item_mid_layout;
    }
    @Override protected void initMidWidget() {
        super.initMidWidget();
        infoButtonStub = findViewById(R.id.smartisan_rom_info_btn_viewstub);
    }
    public ImageView getBadgeImageView() { return badgeImageView; }
    public void setIconResource(int resource) { setIcon(resource); }
    public void setIconDrawable(Drawable drawable) { setIcon(drawable); }
    public void setIconRightMargin(int margin) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) leftContainer.getLayoutParams();
        params.rightMargin = margin;
        leftContainer.setLayoutParams(params);
    }
    public void setSubTitle(CharSequence value) { setSubtitle(value); }
    public void setSubTitle(int resource) { setSubtitle(resource); }
    public void setupInfoButton(boolean visible, View.OnClickListener listener) {
        if (visible) {
            if (infoButton == null) infoButton = (ImageView) infoButtonStub.inflate();
            infoButton.setVisibility(VISIBLE);
            infoButton.setOnClickListener(listener);
        } else if (infoButton != null) infoButton.setVisibility(GONE);
    }
    @Deprecated public void setItemEnable(boolean enabled) { setEnabled(enabled); }
    public void setBadgeResources(int resource) {
        if (resource > 0) {
            if (badgeImageView == null) badgeImageView = new ImageView(getContext());
            badgeImageView.setImageResource(resource);
        } else badgeImageView = null;
        setRightExpandView(badgeImageView);
    }
    public void setupView(int titleResource, CharSequence subtitle, int iconResource,
            int backgroundStyle, int iconRightMargin) {
        if (titleResource > 0) setTitle(titleResource); else setTitle((CharSequence) null);
        setSubtitle(subtitle);
        setIconResource(iconResource);
        setIconRightMargin(iconRightMargin);
        setBackgroundStyle(backgroundStyle);
    }
}
