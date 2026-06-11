package com.termux.ai;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public final class TaiModelCatalog {
    private static final Map<String, CatalogEntry> BUILT_IN_ENTRIES = buildEntries();
    private static volatile Map<String, CatalogEntry> entries = BUILT_IN_ENTRIES;
    private TaiModelCatalog() {}

    @NonNull public static Map<String, CatalogEntry> entries() { return entries; }
    @Nullable public static CatalogEntry get(@Nullable String modelId) { return modelId == null ? null : entries.get(modelId); }

    static synchronized void applyRemotePayload(@NonNull JSONObject payload, boolean allowEqualVersion) {
        JSONArray remoteEntries = payload.optJSONArray("entries");
        if (remoteEntries == null) return;
        LinkedHashMap<String, CatalogEntry> merged = new LinkedHashMap<>(BUILT_IN_ENTRIES);
        for (int i = 0; i < remoteEntries.length(); i++) {
            JSONObject item = remoteEntries.optJSONObject(i);
            if (item == null) continue;
            try {
                LinkedHashSet<String> capabilities = new LinkedHashSet<>();
                JSONArray values = item.optJSONArray("capabilities");
                if (values != null) for (int j = 0; j < values.length(); j++) capabilities.add(values.getString(j));
                CatalogEntry entry = new CatalogEntry(item.getString("modelId"), item.getString("displayName"),
                    item.optString("roleHint", "Curated model"), item.getString("repositoryId"),
                    item.getString("revision"), item.isNull("artifactPath") ? null : item.optString("artifactPath"),
                    item.getString("license"), item.getLong("sizeBytes"), item.optBoolean("gated", false),
                    item.getString("backend"), item.getString("format"), item.optString("architecture", ""),
                    item.isNull("quantization") ? null : item.optString("quantization"), item.optInt("contextWindow", 4096),
                    item.optInt("recommendedRamGb", 0), item.isNull("sha256") ? null : item.optString("sha256"),
                    capabilities);
                if (!TaiModelSpec.BACKEND_LITERT_LM.equals(entry.backend)
                    || !TaiModelSpec.FORMAT_LITERTLM.equals(entry.format)) continue;
                merged.put(entry.modelId, entry);
            } catch (Exception ignored) {}
        }
        entries = Collections.unmodifiableMap(merged);
    }

    @NonNull
    private static Map<String, CatalogEntry> buildEntries() {
        LinkedHashMap<String, CatalogEntry> entries = new LinkedHashMap<>();
        entries.put(TaiModelRegistry.MODEL_GEMMA_4_E2B_IT, liteRt(
            TaiModelRegistry.MODEL_GEMMA_4_E2B_IT, "Gemma 4 E2B IT", "Fast assistant",
            "litert-community/gemma-4-E2B-it-litert-lm", "6e5c4f1e395deb959c494953478fa5cec4b8008f",
            "gemma-4-E2B-it.litertlm", "Apache-2.0", 2_588_147_712L, false,
            setOf("text_chat", "image_input", "audio_input", "tool_use", "llm_thinking", "speculative_decoding")));
        entries.put(TaiModelRegistry.MODEL_GEMMA_4_E4B_IT, liteRt(
            TaiModelRegistry.MODEL_GEMMA_4_E4B_IT, "Gemma 4 E4B IT", "Coding and reasoning",
            "litert-community/gemma-4-E4B-it-litert-lm", "28299f30ee4d43294517a4ac93abd6163412f07f",
            "gemma-4-E4B-it.litertlm", "Apache-2.0", 3_659_530_240L, false,
            setOf("text_chat", "image_input", "audio_input", "tool_use", "llm_thinking", "speculative_decoding")));
        entries.put(TaiModelRegistry.MODEL_MOBILE_ACTIONS_270M, liteRt(
            TaiModelRegistry.MODEL_MOBILE_ACTIONS_270M, "Mobile Actions 270M", "Mobile actions",
            "litert-community/functiongemma-270m-ft-mobile-actions", "38942192c9b723af836d489074823ff33d4a3e7a",
            "mobile_actions_q8_ekv1024.litertlm", "Gemma Terms of Use", 288_964_608L, true,
            setOf("text_chat", "tool_use", "mobile_actions")));

        return Collections.unmodifiableMap(entries);
    }

    private static CatalogEntry liteRt(String id, String name, String role, String repo, String revision,
                                        String file, String license, long size, boolean gated, LinkedHashSet<String> capabilities) {
        return new CatalogEntry(id, name, role, repo, revision, file, license, size, gated,
            TaiModelSpec.BACKEND_LITERT_LM, TaiModelSpec.FORMAT_LITERTLM, "gemma", null, 4096,
            id.equals(TaiModelRegistry.MODEL_MOBILE_ACTIONS_270M) ? 0 : 6, null, capabilities);
    }

    private static LinkedHashSet<String> setOf(String... values) { return new LinkedHashSet<>(Arrays.asList(values)); }

    public static final class CatalogEntry {
        public final String modelId, displayName, roleHint, repositoryId, revision, artifactPath, license;
        public final long sizeBytes;
        public final boolean gated;
        public final String backend, format, architecture, quantization;
        public final int contextWindow, recommendedRamGb;
        @Nullable public final String sha256;
        public final LinkedHashSet<String> capabilities;
        public final String providerPageUrl, downloadUrl;

        private CatalogEntry(String modelId, String displayName, String roleHint, String repositoryId,
                             String revision, @Nullable String artifactPath, String license, long sizeBytes,
                             boolean gated, String backend, String format, String architecture,
                             @Nullable String quantization, int contextWindow, int recommendedRamGb,
                             @Nullable String sha256, LinkedHashSet<String> capabilities) {
            this.modelId = modelId; this.displayName = displayName; this.roleHint = roleHint;
            this.repositoryId = repositoryId; this.revision = revision; this.artifactPath = artifactPath;
            this.license = license; this.sizeBytes = sizeBytes; this.gated = gated; this.backend = backend;
            this.format = format; this.architecture = architecture; this.quantization = quantization;
            this.contextWindow = contextWindow; this.recommendedRamGb = recommendedRamGb;
            this.sha256 = sha256; this.capabilities = capabilities;
            this.providerPageUrl = "https://huggingface.co/" + repositoryId;
            this.downloadUrl = artifactPath == null ? providerPageUrl : providerPageUrl + "/resolve/" + revision + "/" + artifactPath + "?download=true";
        }
    }
}
