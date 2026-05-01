package com.termux.app.launcher.data;

import androidx.annotation.NonNull;

import com.termux.app.launcher.model.LauncherAppEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import me.xdrop.fuzzywuzzy.FuzzySearch;

public final class LauncherRankingEngine {
    private LauncherRankingEngine() {}

    public static List<LauncherAppEntry> filterAndRank(@NonNull List<LauncherAppEntry> entries, @NonNull String query, int tolerance) {
        String trimmed = query.trim();
        String input = trimmed.toLowerCase(Locale.US);
        String normalizedInput = normalizeLookupValue(trimmed);
        if (input.isEmpty()) {
            return new ArrayList<>(entries);
        }

        final boolean fuzzy = input.length() > 2 || normalizedInput.length() > 2;
        List<ScoredEntry> scored = new ArrayList<>();
        for (LauncherAppEntry entry : entries) {
            int tier = matchTier(entry, input, normalizedInput);
            if (tier < 0) {
                continue;
            }

            int score = 100;
            if (fuzzy) {
                score = computeScore(entry, input, normalizedInput);
                if (tier <= 3) {
                    score = Math.max(score, tolerance);
                } else if (score < tolerance) {
                    continue;
                }
            }
            scored.add(new ScoredEntry(entry, score, tier));
        }

        Collections.sort(scored, new Comparator<ScoredEntry>() {
            @Override
            public int compare(ScoredEntry a, ScoredEntry b) {
                if (a.tier != b.tier) return Integer.compare(a.tier, b.tier);
                if (a.score != b.score) return Integer.compare(b.score, a.score);
                return a.entry.label.compareToIgnoreCase(b.entry.label);
            }
        });

        List<LauncherAppEntry> out = new ArrayList<>(scored.size());
        for (ScoredEntry item : scored) {
            out.add(item.entry);
        }
        return out;
    }

    private static int computeScore(@NonNull LauncherAppEntry entry, @NonNull String input, @NonNull String normalizedInput) {
        String label = entry.label == null ? "" : entry.label;
        String labelLower = label.toLowerCase(Locale.US);
        String labelNormalized = normalizeLookupValue(label);
        String packageName = entry.appRef.packageName.toLowerCase(Locale.US);
        String activityName = entry.appRef.activityName.toLowerCase(Locale.US);

        int best = FuzzySearch.partialRatio(input, labelLower);
        best = Math.max(best, FuzzySearch.partialRatio(input, packageName));
        best = Math.max(best, FuzzySearch.partialRatio(input, activityName));
        if (!normalizedInput.isEmpty()) {
            best = Math.max(best, FuzzySearch.partialRatio(normalizedInput, labelNormalized));
        }
        return best;
    }

    private static int matchTier(@NonNull LauncherAppEntry entry, @NonNull String input, @NonNull String normalizedInput) {
        String label = entry.label == null ? "" : entry.label;
        String labelLower = label.toLowerCase(Locale.US);
        String labelNormalized = normalizeLookupValue(label);
        String packageName = entry.appRef.packageName.toLowerCase(Locale.US);
        String activityName = entry.appRef.activityName.toLowerCase(Locale.US);
        String stableId = entry.appRef.stableId().toLowerCase(Locale.US);

        if (packageName.equals(input) || activityName.equals(input) || stableId.equals(input)) return 0;
        if (labelLower.equals(input) || (!normalizedInput.isEmpty() && labelNormalized.equals(normalizedInput))) return 1;
        if (packageName.startsWith(input) || activityName.startsWith(input)) return 2;
        if (labelLower.startsWith(input) || (!normalizedInput.isEmpty() && labelNormalized.startsWith(normalizedInput))) return 3;
        String[] words = labelNormalized.split("\\s+");
        for (String word : words) {
            if (!normalizedInput.isEmpty() && word.startsWith(normalizedInput)) return 4;
        }
        if (packageName.contains(input) || activityName.contains(input)) return 5;
        if (labelLower.contains(input) || (!normalizedInput.isEmpty() && labelNormalized.contains(normalizedInput))) return 6;
        return -1;
    }

    static String normalizeLookupValue(String value) {
        if (value == null || value.isEmpty()) return "";
        StringBuilder normalized = new StringBuilder(value.length());
        boolean previousWasSpace = true;
        for (int i = 0; i < value.length(); i++) {
            char c = Character.toLowerCase(value.charAt(i));
            if (Character.isLetterOrDigit(c)) {
                normalized.append(c);
                previousWasSpace = false;
            } else if (!previousWasSpace) {
                normalized.append(' ');
                previousWasSpace = true;
            }
        }
        int length = normalized.length();
        if (length > 0 && normalized.charAt(length - 1) == ' ') {
            normalized.setLength(length - 1);
        }
        return normalized.toString();
    }

    private static final class ScoredEntry {
        final LauncherAppEntry entry;
        final int score;
        final int tier;

        ScoredEntry(LauncherAppEntry entry, int score, int tier) {
            this.entry = entry;
            this.score = score;
            this.tier = tier;
        }
    }
}
