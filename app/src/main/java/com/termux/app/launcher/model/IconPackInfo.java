package com.termux.app.launcher.model;

import androidx.annotation.NonNull;

public final class IconPackInfo {
    @NonNull public final String packageName;
    @NonNull public final String label;
    public final long versionCode;
    public final boolean themed;

    public IconPackInfo(@NonNull String packageName, @NonNull String label, long versionCode, boolean themed) {
        this.packageName = packageName;
        this.label = label;
        this.versionCode = versionCode;
        this.themed = themed;
    }
}
