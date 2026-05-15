package com.termux.app.launcher.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class PinnedIconOverride {
    public static final String SOURCE_ICON_PACK = "icon_pack";

    @NonNull public final String sourceType;
    @NonNull public final String iconPackPackage;
    @NonNull public final String drawableName;
    @NonNull public final String displayLabel;

    public PinnedIconOverride(
        @NonNull String sourceType,
        @NonNull String iconPackPackage,
        @NonNull String drawableName,
        @Nullable String displayLabel
    ) {
        this.sourceType = sourceType;
        this.iconPackPackage = iconPackPackage;
        this.drawableName = drawableName;
        this.displayLabel = displayLabel == null ? "" : displayLabel;
    }

    public boolean isValid() {
        return SOURCE_ICON_PACK.equals(sourceType)
            && !iconPackPackage.trim().isEmpty()
            && !drawableName.trim().isEmpty();
    }
}
