package com.termux.app.launcher.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class PinnedAppItem implements PinnedItem {
    public final AppRef appRef;
    @Nullable public final PinnedIconOverride iconOverride;

    public PinnedAppItem(@NonNull AppRef appRef) {
        this(appRef, null);
    }

    public PinnedAppItem(@NonNull AppRef appRef, @Nullable PinnedIconOverride iconOverride) {
        this.appRef = appRef;
        this.iconOverride = iconOverride;
    }

    @Override
    public int getType() {
        return TYPE_APP;
    }
}
