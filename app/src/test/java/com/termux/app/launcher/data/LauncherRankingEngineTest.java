package com.termux.app.launcher.data;

import com.termux.app.launcher.model.AppRef;
import com.termux.app.launcher.model.LauncherAppEntry;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LauncherRankingEngineTest {

    @Test
    public void filterAndRank_prefersExactPackageQueries() {
        List<LauncherAppEntry> entries = Arrays.asList(
            entry("com.termux", "com.termux.app.TermuxActivity", "Termux"),
            entry("com.termux.api", "com.termux.api.MainActivity", "Termux API")
        );

        List<LauncherAppEntry> ranked = LauncherRankingEngine.filterAndRank(entries, "com.termux.api", 70);

        assertEquals(2, ranked.size());
        assertEquals("com.termux.api", ranked.get(0).appRef.packageName);
    }

    @Test
    public void filterAndRank_matchesNormalizedPunctuationInLabels() {
        List<LauncherAppEntry> entries = Arrays.asList(
            entry("com.termux.api", "com.termux.api.MainActivity", "Termux:API"),
            entry("com.example.notes", "com.example.notes.MainActivity", "Notes")
        );

        List<LauncherAppEntry> ranked = LauncherRankingEngine.filterAndRank(entries, "termux api", 70);

        assertEquals(1, ranked.size());
        assertEquals("com.termux.api", ranked.get(0).appRef.packageName);
    }

    @Test
    public void filterAndRank_matchesPackageFragments() {
        List<LauncherAppEntry> entries = Arrays.asList(
            entry("com.example.devtools", "com.example.devtools.MainActivity", "Tools"),
            entry("com.example.music", "com.example.music.MainActivity", "Music")
        );

        List<LauncherAppEntry> ranked = LauncherRankingEngine.filterAndRank(entries, "devtools", 70);

        assertEquals(1, ranked.size());
        assertEquals("com.example.devtools", ranked.get(0).appRef.packageName);
    }

    @Test
    public void normalizeLookupValue_collapsesPunctuationToSpaces() {
        assertEquals("termux api", LauncherRankingEngine.normalizeLookupValue("Termux:API"));
        assertEquals("foo bar baz", LauncherRankingEngine.normalizeLookupValue(" Foo__Bar/Baz "));
        assertTrue(LauncherRankingEngine.normalizeLookupValue(null).isEmpty());
    }

    private static LauncherAppEntry entry(String packageName, String activityName, String label) {
        return new LauncherAppEntry(new AppRef(packageName, activityName), label, null);
    }
}
