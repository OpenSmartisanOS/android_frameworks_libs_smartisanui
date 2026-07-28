/*
 * Ported from smartisanos.widget.SmartisanButton in Smartisan OS 8.5.3.
 */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.opensmartisanos.ui.R;

public class SmartisanButton extends Button {
    public static final int STYLE_NORMAL = 0;
    public static final int STYLE_BACK = 1;
    public static final int STYLE_SETTING = 2;
    public static final int STYLE_DELETE = 3;
    public static final int STYLE_HIGHLIGHT_BLUE = 4;
    public static final int STYLE_HIGHLIGHT_RED = 5;
    public static final int STYLE_NORMAL_WITH_ICON = 6;
    public static final int STYLE_AREA_ONLY_ICON = 7;
    public static final int STYLE_SEARCH = 8;
    public static final int STYLE_CUSTOM_BACKGROUND = 9;
    public static final int STYLE_MIX = 10;

    private final int fixedWidth;
    private final int fixedHeight;
    private final int iconAreaWidth;
    private final int iconAreaHeight;
    private int buttonStyle = -1;
    private Bitmap imageBitmap;

    public SmartisanButton(Context context) {
        this(context, null);
    }

    public SmartisanButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmartisanButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        fixedWidth = getResources().getDimensionPixelSize(R.dimen.smartisan_button_fixed_width);
        fixedHeight = getResources().getDimensionPixelSize(R.dimen.smartisan_button_fixed_height);
        iconAreaWidth = getResources().getDimensionPixelSize(R.dimen.smartisan_button_icon_area_width);
        iconAreaHeight = getResources().getDimensionPixelSize(R.dimen.smartisan_button_icon_area_height);

        TypedArray values = context.obtainStyledAttributes(attrs, R.styleable.SmartisanButton);
        buttonStyle = values.getInt(R.styleable.SmartisanButton_buttonStyle, -1);
        int imageSource = values.getResourceId(R.styleable.SmartisanButton_buttonSrc, -1);
        values.recycle();

        setGravity(Gravity.CENTER);
        setTypeface(Typeface.DEFAULT_BOLD);
        setMaxLines(1);
        setEllipsize(TextUtils.TruncateAt.END);
        setTextSize(0, getResources().getDimension(R.dimen.smartisan_button_text_size));
        if (imageSource >= 0) {
            setButtonSourceBitmap(imageSource);
        }
        if (buttonStyle >= 0) {
            init();
        }
    }

    public void setButtonSourceBitmap(int resId) {
        if (resId < 0 || (buttonStyle != STYLE_NORMAL_WITH_ICON
                && buttonStyle != STYLE_AREA_ONLY_ICON) || iconAreaHeight <= 0) {
            return;
        }
        Bitmap decoded = BitmapFactory.decodeResource(getResources(), resId);
        imageBitmap = decoded;
        if (decoded != null && decoded.getHeight() > iconAreaHeight) {
            Matrix matrix = new Matrix();
            float scale = (float) iconAreaHeight / decoded.getHeight();
            matrix.postScale(scale, scale);
            imageBitmap = Bitmap.createBitmap(decoded, 0, 0, decoded.getWidth(),
                    decoded.getHeight(), matrix, true);
        }
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (buttonStyle != STYLE_NORMAL_WITH_ICON && buttonStyle != STYLE_AREA_ONLY_ICON) {
            return;
        }
        int width = buttonStyle == STYLE_NORMAL_WITH_ICON ? fixedWidth : iconAreaWidth;
        int height = buttonStyle == STYLE_NORMAL_WITH_ICON ? fixedHeight : iconAreaHeight;
        if (View.MeasureSpec.getMode(widthMeasureSpec) == View.MeasureSpec.EXACTLY) {
            width = View.MeasureSpec.getSize(widthMeasureSpec);
        }
        if (View.MeasureSpec.getMode(heightMeasureSpec) == View.MeasureSpec.EXACTLY) {
            height = View.MeasureSpec.getSize(heightMeasureSpec);
        }
        setMeasuredDimension(width, height);
    }

    private void init() {
        setClickable(true);
        initButtonStyle(buttonStyle);
    }

    private void initButtonStyle(int style) {
        switch (style) {
            case STYLE_NORMAL:
                applyTextButton(R.drawable.smartisan_rom_selector_small_btn_standard, false);
                break;
            case STYLE_BACK:
                applyTextButton(R.drawable.smartisan_rom_selector_small_btn_back, false);
                break;
            case STYLE_SETTING:
                setText("");
                setBackgroundResource(R.drawable.smartisan_rom_selector_small_icon_btn_setting);
                break;
            case STYLE_DELETE:
                setText("");
                setBackgroundResource(R.drawable.smartisan_rom_selector_small_icon_btn_delete);
                break;
            case STYLE_HIGHLIGHT_BLUE:
                applyTextButton(R.drawable.smartisan_rom_selector_small_btn_highlight, true);
                break;
            case STYLE_HIGHLIGHT_RED:
                applyTextButton(R.drawable.smartisan_rom_selector_small_btn_highlight_red, true);
                break;
            case STYLE_NORMAL_WITH_ICON:
                setText("");
                setMinWidth(getResources().getDimensionPixelSize(
                        R.dimen.smartisan_button_limit_min_width));
                setMaxWidth(getResources().getDimensionPixelSize(
                        R.dimen.smartisan_button_limit_min_width));
                setBackgroundResource(R.drawable.smartisan_rom_selector_small_btn_standard);
                break;
            case STYLE_AREA_ONLY_ICON:
                setText("");
                setBackground(new ColorDrawable(0));
                break;
            case STYLE_SEARCH:
                setText("");
                setBackgroundResource(R.drawable.smartisan_rom_selector_small_icon_btn_search);
                break;
            case STYLE_CUSTOM_BACKGROUND:
                setText("");
                break;
            default:
                break;
        }
        ViewGroup.LayoutParams params = getLayoutParams();
        if (params == null) {
            return;
        }
        if (style == STYLE_NORMAL_WITH_ICON) {
            params.width = fixedWidth;
            params.height = fixedHeight;
        } else if (style == STYLE_AREA_ONLY_ICON) {
            params.width = iconAreaWidth;
            params.height = iconAreaHeight;
        } else {
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        setLayoutParams(params);
    }

    private void applyTextButton(int background, boolean highlighted) {
        setMaxWidth(getResources().getDimensionPixelSize(R.dimen.smartisan_button_limit_max_width));
        setMinWidth(getResources().getDimensionPixelSize(R.dimen.smartisan_button_limit_min_width));
        setTextColor(getResources().getColorStateList(highlighted
                ? R.color.smartisan_button_highlight_text
                : R.color.smartisan_button_normal_text));
        setShadowLayer(0.1f, 0f, highlighted ? -2f : 2f,
                getResources().getColor(highlighted
                        ? R.color.smartisan_button_highlight_enable_shadow_color
                        : R.color.smartisan_button_normal_enable_shadow_color));
        setBackgroundResource(background);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if ((buttonStyle == STYLE_NORMAL_WITH_ICON || buttonStyle == STYLE_AREA_ONLY_ICON)
                && imageBitmap != null) {
            float x = (getWidth() - imageBitmap.getWidth()) / 2f;
            float y = (getHeight() - imageBitmap.getHeight()) / 2f;
            canvas.drawBitmap(imageBitmap, x, y, new Paint());
        }
    }

    public void setButtonStyle(int style) {
        if (style >= 0) {
            buttonStyle = style;
            init();
        }
    }

    public int getButtonStyle() {
        return buttonStyle;
    }

    public void setButtonText(String text) {
        if (TextUtils.isEmpty(text) || buttonStyle == STYLE_SETTING || buttonStyle == STYLE_DELETE
                || buttonStyle == STYLE_SEARCH || buttonStyle == STYLE_NORMAL_WITH_ICON
                || buttonStyle == STYLE_AREA_ONLY_ICON || buttonStyle == STYLE_CUSTOM_BACKGROUND) {
            return;
        }
        setText(text);
    }

    public void setButtonText(int resId) {
        setButtonText(getResources().getString(resId));
    }

    public void setEnabledStyle(boolean enabled) {
        setAlpha(enabled ? 1f : 0.5f);
        if (buttonStyle == STYLE_NORMAL || buttonStyle == STYLE_BACK) {
            setShadowLayer(0.1f, 0f, 2f, enabled
                    ? getResources().getColor(R.color.smartisan_button_normal_enable_shadow_color)
                    : 0);
        } else if (buttonStyle == STYLE_HIGHLIGHT_BLUE
                || buttonStyle == STYLE_HIGHLIGHT_RED) {
            setShadowLayer(0.1f, 0f, -2f, enabled
                    ? getResources().getColor(R.color.smartisan_button_highlight_enable_shadow_color)
                    : 0);
        }
    }
}
