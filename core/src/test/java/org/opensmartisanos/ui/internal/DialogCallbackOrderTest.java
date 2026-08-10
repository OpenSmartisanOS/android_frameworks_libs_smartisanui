package org.opensmartisanos.ui.internal;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class DialogCallbackOrderTest {
    @Test public void callbackRunsAfterDismissal() {
        List<String> order = new ArrayList<>();
        DialogCallbackOrder.dismissThenRun(
                () -> order.add("dismiss"), () -> order.add("callback"));
        assertEquals(List.of("dismiss", "callback"), order);
    }

    @Test public void absentCallbackStillDismisses() {
        List<String> order = new ArrayList<>();
        DialogCallbackOrder.dismissThenRun(() -> order.add("dismiss"), null);
        assertEquals(List.of("dismiss"), order);
    }
}
