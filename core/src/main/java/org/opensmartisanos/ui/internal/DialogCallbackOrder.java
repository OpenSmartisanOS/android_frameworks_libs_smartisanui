package org.opensmartisanos.ui.internal;

/** Keeps the ROM's dismiss-before-callback order while allowing an absent SDK callback. */
public final class DialogCallbackOrder {
    private DialogCallbackOrder() {}

    public static void dismissThenRun(Runnable dismiss, Runnable callback) {
        dismiss.run();
        if (callback != null) callback.run();
    }
}
