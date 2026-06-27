package com.termux.ai;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TaiApiCompatibilityTest {

    @Test
    public void responsesRequest_convertsTextImageAndToolsToNeutralChatRequest() throws Exception {
        JSONObject request = new JSONObject()
            .put("model", "vision-model")
            .put("instructions", "Be concise")
            .put("input", new JSONArray().put(new JSONObject().put("role", "user")
                .put("content", new JSONArray()
                    .put(new JSONObject().put("type", "input_text").put("text", "describe"))
                    .put(new JSONObject().put("type", "input_image").put("image_url", "data:image/png;base64,AQID")))))
            .put("tools", new JSONArray().put(new JSONObject().put("type", "function")
                .put("name", "lookup").put("parameters", new JSONObject().put("type", "object"))));

        JSONObject chat = TaiApiCompatibility.responsesRequestToChat(request.toString());

        assertEquals("vision-model", chat.getString("model"));
        assertEquals("developer", chat.getJSONArray("messages").getJSONObject(0).getString("role"));
        assertEquals("image_url", chat.getJSONArray("messages").getJSONObject(1)
            .getJSONArray("content").getJSONObject(1).getString("type"));
        assertEquals("lookup", chat.getJSONArray("tools").getJSONObject(0)
            .getJSONObject("function").getString("name"));
    }

    @Test
    public void ollamaDiscovery_mapsEndpointCapabilities() throws Exception {
        JSONObject model = new JSONObject().put("id", "gemma-vision").put("_backend", "litert-lm")
            .put("_format", "litertlm").put("_endpoint_context_window", 4096)
            .put("_capabilities", new JSONArray().put("text_chat").put("image_input").put("tool_use"));
        JSONObject models = new JSONObject().put("data", new JSONArray().put(model));

        JSONObject tags = TaiApiCompatibility.ollamaTags(models);
        JSONObject show = TaiApiCompatibility.ollamaShow(models,
            new JSONObject().put("model", "gemma-vision").toString());

        assertEquals("gemma-vision", tags.getJSONArray("models").getJSONObject(0).getString("name"));
        assertTrue(show.getJSONArray("capabilities").toString().contains("vision"));
        assertTrue(show.getJSONArray("capabilities").toString().contains("tools"));
    }

    @Test
    public void responsesOutput_convertsToolCalls() throws Exception {
        JSONObject function = new JSONObject().put("name", "read_file").put("arguments", "{\"path\":\"x\"}");
        JSONObject call = new JSONObject().put("id", "call_1").put("type", "function").put("function", function);
        JSONObject message = new JSONObject().put("role", "assistant").put("content", JSONObject.NULL)
            .put("tool_calls", new JSONArray().put(call));
        JSONObject chat = new JSONObject().put("model", "tool-model").put("choices", new JSONArray().put(
            new JSONObject().put("finish_reason", "tool_calls").put("message", message)));

        JSONObject response = TaiApiCompatibility.responsesFromChat(chat);

        assertEquals("function_call", response.getJSONArray("output").getJSONObject(0).getString("type"));
        assertEquals("read_file", response.getJSONArray("output").getJSONObject(0).getString("name"));
    }
}
