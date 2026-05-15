package com.termux.app.launcher.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class IconPackDrawableItem {
    @NonNull public final String drawableName;
    @NonNull public final String label;

    public IconPackDrawableItem(@NonNull String drawableName, @Nullable String label) {
        this.drawableName = drawableName;
        this.label = label == null || label.trim().isEmpty() ? drawableName : label;
    }
}
