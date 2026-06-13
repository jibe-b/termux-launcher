package com.termux.app.fragments.settings;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreferenceCompat;

import com.termux.R;
import com.termux.app.fragments.settings.termux.TaiModelPreference;

public final class SettingsLayoutUtils {

    private SettingsLayoutUtils() {}

    public static void applyRootLayout(@NonNull PreferenceFragmentCompat fragment) {
        PreferenceScreen screen = fragment.getPreferenceScreen();
        if (screen == null) return;
        applyLayouts(screen, true);
    }

    public static void applyScreenLayout(@NonNull PreferenceFragmentCompat fragment) {
        PreferenceScreen screen = fragment.getPreferenceScreen();
        if (screen == null) return;
        applyLayouts(screen, false);
    }

    private static void applyLayouts(@NonNull PreferenceGroup group, boolean rootRows) {
        for (int i = 0; i < group.getPreferenceCount(); i++) {
            Preference preference = group.getPreference(i);
            preference.setIconSpaceReserved(false);

            if (preference instanceof PreferenceCategory) {
                preference.setLayoutResource(R.layout.preference_settings_category);
            } else if (preference instanceof SeekBarPreference || preference instanceof TaiModelPreference) {
                preference.setIconSpaceReserved(false);
            } else if (usesCardLayout(preference)) {
                preference.setLayoutResource(R.layout.preference_settings_card);
            } else {
                preference.setLayoutResource(rootRows
                    ? R.layout.preference_settings_root_row
                    : R.layout.preference_settings_row);
                if (usesChevron(preference)) {
                    preference.setWidgetLayoutResource(R.layout.preference_widget_chevron);
                }
            }

            if (preference instanceof PreferenceGroup) {
                applyLayouts((PreferenceGroup) preference, false);
            }
        }
    }

    private static boolean usesCardLayout(@NonNull Preference preference) {
        String key = preference.getKey();
        return "privileged_backend_status".equals(key)
            || "tai_runtime_status".equals(key)
            || "tai_model_privacy_notice".equals(key)
            || "tai_endpoint_notice".equals(key);
    }

    private static boolean usesChevron(@NonNull Preference preference) {
        if (!preference.isSelectable()) return false;
        if (preference instanceof SwitchPreferenceCompat) return false;
        return !(preference instanceof PreferenceCategory);
    }
}
