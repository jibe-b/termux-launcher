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
        int best = FuzzySearch.partialRatio(input, entry.labelLower);
        best = Math.max(best, FuzzySearch.partialRatio(input, entry.packageLower));
        best = Math.max(best, FuzzySearch.partialRatio(input, entry.activityLower));
        if (!normalizedInput.isEmpty()) {
            best = Math.max(best, FuzzySearch.partialRatio(normalizedInput, entry.labelNormalized));
        }
        return best;
    }

    private static int matchTier(@NonNull LauncherAppEntry entry, @NonNull String input, @NonNull String normalizedInput) {
        if (entry.packageLower.equals(input) || entry.activityLower.equals(input) || entry.stableIdLower.equals(input)) return 0;
        if (entry.labelLower.equals(input) || (!normalizedInput.isEmpty() && entry.labelNormalized.equals(normalizedInput))) return 1;
        if (entry.packageLower.startsWith(input) || entry.activityLower.startsWith(input)) return 2;
        if (entry.labelLower.startsWith(input) || (!normalizedInput.isEmpty() && entry.labelNormalized.startsWith(normalizedInput))) return 3;
        for (String word : entry.normalizedWords) {
            if (!normalizedInput.isEmpty() && word.startsWith(normalizedInput)) return 4;
        }
        if (entry.packageLower.contains(input) || entry.activityLower.contains(input)) return 5;
        if (entry.labelLower.contains(input) || (!normalizedInput.isEmpty() && entry.labelNormalized.contains(normalizedInput))) return 6;
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
