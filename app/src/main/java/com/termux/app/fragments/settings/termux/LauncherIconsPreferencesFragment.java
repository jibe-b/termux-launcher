package com.termux.app.fragments.settings.termux;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Keep;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.termux.R;
import com.termux.app.TermuxActivity;
import com.termux.app.launcher.data.IconPackRepository;
import com.termux.app.launcher.model.IconPackInfo;
import com.termux.shared.termux.settings.preferences.TermuxAppSharedPreferences;

import java.util.ArrayList;
import java.util.List;

@Keep
public class LauncherIconsPreferencesFragment extends PreferenceFragmentCompat {
    private TermuxAppSharedPreferences preferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getContext();
        if (context == null)
            return;
        preferences = TermuxAppSharedPreferences.build(context, false);
        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setPreferenceDataStore(TermuxStylePreferencesDataStore.getInstance(context));
        setPreferencesFromResource(R.xml.launcher_icons_preferences, rootKey);
        configureIconPackPreferences();
    }

    @Override
    public void onResume() {
        super.onResume();
        configureIconPackPreferences();
    }

    private void configureIconPackPreferences() {
        Context context = getContext();
        if (context == null) return;

        populateIconPackList(context, findPreference("app_launcher_icon_pack_package"), false);
        populateIconPackList(context, findPreference("app_launcher_themed_icon_pack_package"), true);

        Preference refresh = findPreference("app_launcher_refresh_icon_packs");
        if (refresh != null) {
            refresh.setOnPreferenceClickListener(preference -> {
                configureIconPackPreferences();
                Toast.makeText(context, "Icon packs refreshed", Toast.LENGTH_SHORT).show();
                return true;
            });
        }

        Preference reset = findPreference("app_launcher_reset_icon_packs");
        if (reset != null) {
            reset.setOnPreferenceClickListener(preference -> {
                if (preferences != null) {
                    preferences.setAppLauncherIconPackPackage("");
                    preferences.setAppLauncherThemedIconPackPackage("");
                    preferences.setAppLauncherThemedIconsEnabled(false);
                    com.termux.app.launcher.data.LauncherAppDataProvider.getInstance(context).invalidate();
                    TermuxActivity.requestTermuxActivityStylingOnNextResume(context, false);
                }
                configureIconPackPreferences();
                Toast.makeText(context, "Icon packs reset", Toast.LENGTH_SHORT).show();
                return true;
            });
        }
    }

    private void populateIconPackList(Context context, ListPreference preference, boolean themedOnly) {
        if (preference == null) return;
        List<IconPackInfo> packs = new IconPackRepository(context).discoverIconPacks();
        List<CharSequence> entries = new ArrayList<>();
        List<CharSequence> values = new ArrayList<>();
        entries.add("System default");
        values.add("");
        for (IconPackInfo pack : packs) {
            if (themedOnly && !pack.themed) continue;
            entries.add(pack.label);
            values.add(pack.packageName);
        }
        preference.setEntries(entries.toArray(new CharSequence[0]));
        preference.setEntryValues(values.toArray(new CharSequence[0]));
        if (preferences != null) {
            preference.setValue(themedOnly
                ? preferences.getAppLauncherThemedIconPackPackage()
                : preferences.getAppLauncherIconPackPackage());
        }
        preference.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
    }
}
