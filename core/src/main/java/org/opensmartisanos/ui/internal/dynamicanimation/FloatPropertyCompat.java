package org.opensmartisanos.ui.internal.dynamicanimation;

import android.util.FloatProperty;

/* JADX INFO: loaded from: classes.dex */
public abstract class FloatPropertyCompat<T> {
    final String mPropertyName;

    public abstract float getValue(T t);

    public abstract void setValue(T t, float f);

    public FloatPropertyCompat(String name) {
        this.mPropertyName = name;
    }

    public static <T> FloatPropertyCompat<T> createFloatPropertyCompat(final FloatProperty<T> property) {
        return new FloatPropertyCompat<T>(property.getName()) { // from class: smartisanos.widget.dynamicanimation.FloatPropertyCompat.1
            @Override // smartisanos.widget.dynamicanimation.FloatPropertyCompat
            public float getValue(T object) {
                return ((Float) property.get(object)).floatValue();
            }

            @Override // smartisanos.widget.dynamicanimation.FloatPropertyCompat
            public void setValue(T object, float value) {
                property.set(object, value);
            }
        };
    }
}
