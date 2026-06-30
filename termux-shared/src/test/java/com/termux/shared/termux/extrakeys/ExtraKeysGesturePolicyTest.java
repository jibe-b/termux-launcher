package com.termux.shared.termux.extrakeys;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExtraKeysGesturePolicyTest {

    @Test
    public void leftDominantSwipeChangesToolbarPage() {
        assertTrue(ExtraKeysGesturePolicy.isToolbarPageSwipe(40f, 8f, 10f));
    }

    @Test
    public void tapVerticalPopupAndRightSwipeDoNotChangeToolbarPage() {
        assertFalse(ExtraKeysGesturePolicy.isToolbarPageSwipe(8f, 1f, 10f));
        assertFalse(ExtraKeysGesturePolicy.isToolbarPageSwipe(12f, 24f, 10f));
        assertFalse(ExtraKeysGesturePolicy.isToolbarPageSwipe(-40f, 2f, 10f));
    }
}
