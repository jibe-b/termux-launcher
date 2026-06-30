package com.termux.shared.termux.extrakeys;

final class ExtraKeysGesturePolicy {

    private ExtraKeysGesturePolicy() {}

    static boolean isToolbarPageSwipe(float leftTravelPx, float verticalTravelPx, float thresholdPx) {
        return leftTravelPx >= thresholdPx && leftTravelPx > Math.abs(verticalTravelPx);
    }
}
