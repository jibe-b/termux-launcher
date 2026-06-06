package com.termux.ai;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TaiCliFormatterTest {

    @Test
    public void unload_reportsPendingLoadCancellation() throws Exception {
        JSONObject response = new JSONObject();
        response.put("ok", true);
        response.put("runtime", "litert-lm");
        response.put("loadingModelId", "Gemma-4-E2B-it");
        response.put("loadCancellationRequested", true);

        String output = TaiCliFormatter.format("unload", response);

        assertTrue(output.contains("Model load cancellation requested"));
        assertTrue(output.contains("Loading model: Gemma-4-E2B-it"));
        assertFalse(output.contains("Model unloaded"));
    }

    @Test
    public void cancel_distinguishesModelLoadFromGeneration() throws Exception {
        JSONObject response = new JSONObject();
        response.put("ok", true);
        response.put("cancelled", true);
        response.put("loadCancellationRequested", true);
        response.put("message", "Model load cancellation requested.");

        String output = TaiCliFormatter.format("cancel", response);

        assertTrue(output.contains("Model load cancellation requested"));
        assertFalse(output.contains("Generation cancel requested"));
    }
}
