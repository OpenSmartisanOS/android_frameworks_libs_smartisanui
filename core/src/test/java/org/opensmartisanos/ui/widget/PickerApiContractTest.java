package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.os.Parcelable;
import android.view.View;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/** Host-side reflection guard for R2 picker contracts that do not require Android runtime code. */
public final class PickerApiContractTest {
    @Test public void pickerDialogsExposeOriginalClickContract() throws Exception {
        assertClickContract(SmartisanDatePickerDialog.class);
        assertClickContract(SmartisanDatePickerExDialog.class);
        assertClickContract(SmartisanDateTimePickerDialog.class);
        assertClickContract(SmartisanTimePickerDialog.class);
        assertClickContract(SmartisanTimePickerExDialog.class);
    }

    @Test public void dateTimeDialogUsesTopLevelListenerWithoutAmbiguousOverload()
            throws Exception {
        assertTrue(OnTimeSetListener.class.isAssignableFrom(
                SmartisanDateTimePickerDialog.OnDateTimeSetListener.class));
        assertNotNull(SmartisanDateTimePickerDialog.class.getConstructor(
                Context.class, OnTimeSetListener.class, long.class));
        assertNotNull(SmartisanDateTimePickerDialog.class.getConstructor(
                Context.class, OnTimeSetListener.class,
                long.class, long.class, long.class));

        for (Constructor<?> constructor
                : SmartisanDateTimePickerDialog.class.getConstructors()) {
            Class<?>[] parameters = constructor.getParameterTypes();
            assertFalse("deprecated listener constructor recreates lambda ambiguity",
                    parameters.length > 1
                            && parameters[1]
                            == SmartisanDateTimePickerDialog.OnDateTimeSetListener.class);
        }
    }

    @Test public void everyCustomSavedStatePublishesParcelableCreator() throws Exception {
        assertPublicCreator(SmartisanNumberPicker.class);
        assertPublicCreator(SmartisanNumberPickerEx.class);
        assertPublicCreator(SmartisanDatePicker.class);
        assertPublicCreator(SmartisanDatePickerEx.class);
        assertPublicCreator(SmartisanTimePicker.class);
        assertPublicCreator(SmartisanTimePickerEx.class);
        assertPublicCreator(SmartisanDateTimePicker.class);
        assertPublicCreator(SmartisanWheelTextView.class);
        assertPublicCreator(SmartisanSpinnerView.class);
    }

    private static void assertClickContract(Class<?> dialogClass) throws Exception {
        assertTrue(View.OnClickListener.class.isAssignableFrom(dialogClass));
        Method onClick = dialogClass.getMethod("onClick", View.class);
        assertTrue(Modifier.isPublic(onClick.getModifiers()));
        assertEquals(void.class, onClick.getReturnType());
    }

    private static void assertPublicCreator(Class<?> owner) throws Exception {
        Class<?> savedState = null;
        for (Class<?> nested : owner.getDeclaredClasses()) {
            if ("SavedState".equals(nested.getSimpleName())) {
                savedState = nested;
                break;
            }
        }
        assertNotNull(owner.getName() + " has no SavedState", savedState);
        Field creator = savedState.getField("CREATOR");
        int modifiers = creator.getModifiers();
        assertTrue(Modifier.isPublic(modifiers));
        assertTrue(Modifier.isStatic(modifiers));
        assertTrue(Modifier.isFinal(modifiers));
        assertTrue(Parcelable.Creator.class.isAssignableFrom(creator.getType()));
    }
}
