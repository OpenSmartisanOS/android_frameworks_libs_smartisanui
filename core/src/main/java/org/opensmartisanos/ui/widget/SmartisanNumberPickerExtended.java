package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.util.AttributeSet;

/* JADX INFO: loaded from: classes.dex */
@Deprecated
public class SmartisanNumberPickerExtended extends SmartisanNumberPickerEx {
    private int mFackIndex;
    private int mTempMaxValue;

    public SmartisanNumberPickerExtended(Context context) {
        super(context);
        this.mFackIndex = 3;
    }

    public SmartisanNumberPickerExtended(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mFackIndex = 3;
    }

    public SmartisanNumberPickerExtended(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mFackIndex = 3;
    }

    @Override // smartisanos.widget.SmartisanNumberPickerEx
    public void setMaxValue(int maxValue) {
        int i = this.mFackIndex + maxValue;
        this.mTempMaxValue = i;
        super.setMaxValue(i);
    }

    public void setFackIndex(int fackIndex) {
        this.mFackIndex = fackIndex;
        setMaxValue(getMaxValue());
    }

    @Override // smartisanos.widget.SmartisanNumberPickerEx
    public void setFormatter(SmartisanNumberPickerEx.Formatter formatter) {
        super.setFormatter(new FackValueWrapperFormatter(formatter));
    }

    class FackValueWrapperFormatter implements SmartisanNumberPickerEx.Formatter {
        private SmartisanNumberPickerEx.Formatter mFormatter;

        FackValueWrapperFormatter(SmartisanNumberPickerEx.Formatter formatter) {
            this.mFormatter = formatter;
        }

        @Override // smartisanos.widget.SmartisanNumberPickerEx.Formatter
        public String format(int value) {
            if (SmartisanNumberPickerExtended.this.getMaxValue() - value <= SmartisanNumberPickerExtended.this.mFackIndex) {
                return "";
            }
            return this.mFormatter.format(value);
        }
    }

    @Override // smartisanos.widget.SmartisanNumberPickerEx, android.view.View
    public void scrollBy(int x, int y) {
        if (y > 0 && getValue() == getMinValue()) {
            super.scrollBy(x, 0);
        } else if (y < 0 && getValue() >= (getMaxValue() - this.mFackIndex) - 1) {
            super.scrollBy(x, 0);
        } else {
            super.scrollBy(x, y);
        }
    }
}
