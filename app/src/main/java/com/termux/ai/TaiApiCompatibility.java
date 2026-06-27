package com.termux.ai;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;

/**
 * Pure protocol adapters over {@link TaiManager}. Backend runtimes never need to know whether a
 * request arrived through OpenAI Chat, OpenAI Responses, or Ollama's native API.
 */
public final class TaiApiCompatibility {
    public static final String OLLAMA_VERSION = "0.13.3-termux-launcher";

    private TaiApiCompatibility() {}

    @NonNull
    public static JSONObject responsesRequestToChat(@NonNull String body) throws JSONException {
        JSONObject source = body.trim().isEmpty() ? new JSONObject() : new JSONObject(body);
        JSONObject chat = new JSONObject();
        copy(source, chat, "model", "temperature", "top_p", "stream", "tool_choice");
        if (source.has("max_output_tokens")) chat.put("max_completion_tokens", source.opt("max_output_tokens"));

        JSONArray messages = new JSONArray();
        String instructions = source.optString("instructions", "").trim();
        if (!instructions.isEmpty()) messages.put(new JSONObject().put("role", "developer").put("content", instructions));
        Object input = source.opt("input");
        if (input instanceof String) {
            messages.put(new JSONObject().put("role", "user").put("content", input));
        } else if (input instanceof JSONArray) {
            appendResponseInput(messages, (JSONArray) input);
        }
        chat.put("messages", messages);

        JSONArray tools = source.optJSONArray("tools");
        if (tools != null) {
            JSONArray normalized = new JSONArray();
            for (int i = 0; i < tools.length(); i++) {
                JSONObject tool = tools.optJSONObject(i);
                if (tool == null) continue;
                if (tool.optJSONObject("function") != null) {
                    normalized.put(new JSONObject(tool.toString()));
                } else if ("function".equals(tool.optString("type", ""))) {
                    JSONObject function = new JSONObject();
                    function.put("name", tool.optString("name", ""));
                    function.put("description", tool.optString("description", ""));
                    function.put("parameters", tool.optJSONObject("parameters") == null
                        ? new JSONObject() : tool.optJSONObject("parameters"));
                    normalized.put(new JSONObject().put("type", "function").put("function", function));
                }
            }
            chat.put("tools", normalized);
        }
        return chat;
    }

    private static void appendResponseInput(JSONArray messages, JSONArray input) throws JSONException {
        for (int i = 0; i < input.length(); i++) {
            Object value = input.opt(i);
            if (value instanceof String) {
                messages.put(new JSONObject().put("role", "user").put("content", value));
                continue;
            }
            JSONObject item = value instanceof JSONObject ? (JSONObject) value : null;
            if (item == null) continue;
            String type = item.optString("type", "message");
            if ("function_call_output".equals(type)) {
                messages.put(new JSONObject().put("role", "tool")
                    .put("tool_call_id", item.optString("call_id", item.optString("id", "")))
                    .put("content", String.valueOf(item.opt("output"))));
                continue;
            }
            if ("function_call".equals(type)) {
                JSONObject function = new JSONObject().put("name", item.optString("name", ""))
                    .put("arguments", item.optString("arguments", "{}"));
                JSONObject call = new JSONObject().put("id", item.optString("call_id", item.optString("id", "")))
                    .put("type", "function").put("function", function);
                messages.put(new JSONObject().put("role", "assistant").put("content", JSONObject.NULL)
                    .put("tool_calls", new JSONArray().put(call)));
                continue;
            }
            JSONObject message = new JSONObject();
            message.put("role", item.optString("role", "user"));
            Object content = item.opt("content");
            message.put("content", normalizeResponseContent(content));
            messages.put(message);
        }
    }

    private static Object normalizeResponseContent(Object content) throws JSONException {
        if (!(content instanceof JSONArray)) return content == null ? "" : content;
        JSONArray output = new JSONArray();
        JSONArray parts = (JSONArray) content;
        for (int i = 0; i < parts.length(); i++) {
            JSONObject part = parts.optJSONObject(i);
            if (part == null) continue;
            String type = part.optString("type", "");
            if ("input_text".equals(type) || "output_text".equals(type)) {
                output.put(new JSONObject().put("type", "text").put("text", part.optString("text", "")));
            } else if ("input_image".equals(type)) {
                Object imageUrl = part.opt("image_url");
                output.put(new JSONObject().put("type", "image_url")
                    .put("image_url", imageUrl instanceof JSONObject ? imageUrl
                        : new JSONObject().put("url", String.valueOf(imageUrl))));
            }
        }
        return output;
    }

    @NonNull
    public static JSONObject responsesFromChat(@NonNull JSONObject chat) throws JSONException {
        if (chat.has("error")) return chat;
        String id = "resp_" + System.currentTimeMillis();
        JSONObject response = responseEnvelope(id, chat.optString("model", ""), "completed");
        JSONArray output = new JSONArray();
        JSONArray choices = chat.optJSONArray("choices");
        JSONObject firstChoice = choices == null || choices.length() == 0 ? null : choices.optJSONObject(0);
        JSONObject message = firstChoice == null ? null : firstChoice.optJSONObject("message");
        if (message != null) {
            JSONArray calls = message.optJSONArray("tool_calls");
            if (calls != null && calls.length() > 0) {
                for (int i = 0; i < calls.length(); i++) {
                    JSONObject call = calls.optJSONObject(i);
                    if (call == null) continue;
                    JSONObject function = call.optJSONObject("function");
                    output.put(new JSONObject().put("type", "function_call")
                        .put("id", "fc_" + i + "_" + System.currentTimeMillis())
                        .put("call_id", call.optString("id", "call_" + i))
                        .put("name", function == null ? "" : function.optString("name", ""))
                        .put("arguments", function == null ? "{}" : function.optString("arguments", "{}"))
                        .put("status", "completed"));
                }
            }
            String text = message.isNull("content") ? "" : message.optString("content", "");
            if (!text.isEmpty()) output.put(messageOutputItem("msg_" + System.currentTimeMillis(), text, "completed"));
        }
        response.put("output", output);
        response.put("usage", chat.optJSONObject("usage") == null ? zeroUsage() : chat.optJSONObject("usage"));
        return response;
    }

    @NonNull
    public static JSONObject responseEnvelope(String id, String model, String status) throws JSONException {
        return new JSONObject().put("id", id).put("object", "response")
            .put("created_at", System.currentTimeMillis() / 1000L).put("status", status)
            .put("model", model).put("output", new JSONArray());
    }

    @NonNull
    public static JSONObject messageOutputItem(String id, String text, String status) throws JSONException {
        JSONObject part = new JSONObject().put("type", "output_text").put("text", text)
            .put("annotations", new JSONArray());
        return new JSONObject().put("type", "message").put("id", id).put("status", status)
            .put("role", "assistant").put("content", new JSONArray().put(part));
    }

    @NonNull
    public static JSONObject ollamaTags(@NonNull JSONObject openAiModels) throws JSONException {
        JSONArray models = new JSONArray();
        JSONArray data = openAiModels.optJSONArray("data");
        if (data != null) for (int i = 0; i < data.length(); i++) {
            JSONObject source = data.optJSONObject(i);
            if (source == null) continue;
            String id = source.optString("id", "");
            JSONObject details = ollamaDetails(source);
            models.put(new JSONObject().put("name", id).put("model", id)
                .put("modified_at", Instant.EPOCH.toString()).put("size", source.optLong("_size", 0L))
                .put("digest", nullableString(source, "_sha256")).put("details", details));
        }
        return new JSONObject().put("models", models);
    }

    @NonNull
    public static JSONObject ollamaShow(@NonNull JSONObject openAiModels, @NonNull String body) throws JSONException {
        JSONObject request = body.trim().isEmpty() ? new JSONObject() : new JSONObject(body);
        String requested = request.optString("model", request.optString("name", ""));
        JSONObject model = findModel(openAiModels, requested);
        if (model == null) return ollamaError(404, "model_not_found", "model '" + requested + "' not found");
        JSONArray capabilities = new JSONArray().put("completion");
        JSONArray endpoint = model.optJSONArray("_capabilities");
        if (contains(endpoint, TaiModelSpec.CAPABILITY_IMAGE_INPUT)) capabilities.put("vision");
        if (contains(endpoint, TaiModelSpec.CAPABILITY_TOOL_USE)) capabilities.put("tools");
        if (contains(endpoint, TaiModelSpec.CAPABILITY_TEXT_EMBEDDINGS)) capabilities.put("embedding");
        JSONObject info = new JSONObject().put("general.architecture", model.optString("_architecture", ""))
            .put("general.context_length", model.optInt("_endpoint_context_window", 0));
        return new JSONObject().put("parameters", "num_ctx " + model.optInt("_endpoint_context_window", 0))
            .put("license", model.optString("_license", ""))
            .put("modified_at", Instant.EPOCH.toString()).put("details", ollamaDetails(model))
            .put("capabilities", capabilities).put("model_info", info);
    }

    @NonNull
    public static JSONObject ollamaChatRequest(@NonNull String body) throws JSONException {
        JSONObject source = body.trim().isEmpty() ? new JSONObject() : new JSONObject(body);
        JSONObject chat = new JSONObject().put("model", source.optString("model", ""))
            .put("stream", source.optBoolean("stream", true));
        JSONArray messages = source.optJSONArray("messages");
        JSONArray converted = new JSONArray();
        if (messages != null) for (int i = 0; i < messages.length(); i++) {
            JSONObject message = messages.optJSONObject(i);
            if (message == null) continue;
            JSONObject copy = new JSONObject(message.toString());
            JSONArray images = message.optJSONArray("images");
            if (images != null && images.length() > 0) {
                JSONArray content = new JSONArray().put(new JSONObject().put("type", "text")
                    .put("text", message.optString("content", "")));
                for (int j = 0; j < images.length(); j++) {
                    String image = images.optString(j, "");
                    if (!image.startsWith("data:")) image = "data:image/png;base64," + image;
                    content.put(new JSONObject().put("type", "image_url")
                        .put("image_url", new JSONObject().put("url", image)));
                }
                copy.put("content", content);
                copy.remove("images");
            }
            converted.put(copy);
        }
        chat.put("messages", converted);
        JSONArray tools = source.optJSONArray("tools");
        if (tools != null) chat.put("tools", tools);
        if (source.has("format")) {
            Object format = source.opt("format");
            chat.put("response_format", format instanceof JSONObject
                ? new JSONObject().put("type", "json_schema").put("json_schema", format)
                : new JSONObject().put("type", "json_object"));
        }
        JSONObject options = source.optJSONObject("options");
        if (options != null) {
            if (options.has("temperature")) chat.put("temperature", options.opt("temperature"));
            if (options.has("top_p")) chat.put("top_p", options.opt("top_p"));
            if (options.has("num_predict")) chat.put("max_tokens", options.opt("num_predict"));
            if (options.has("stop")) chat.put("stop", options.opt("stop"));
        }
        return chat;
    }

    @NonNull
    public static JSONObject ollamaChatFromOpenAi(@NonNull JSONObject chat) throws JSONException {
        if (chat.has("error")) return ollamaError(chat.optInt("_statusCode", 500),
            chat.optJSONObject("error") == null ? "generation_error" : chat.optJSONObject("error").optString("code", "generation_error"),
            chat.optJSONObject("error") == null ? "Generation failed" : chat.optJSONObject("error").optString("message", "Generation failed"));
        JSONArray choices = chat.optJSONArray("choices");
        JSONObject choice = choices == null ? null : choices.optJSONObject(0);
        JSONObject message = choice == null ? new JSONObject() : choice.optJSONObject("message");
        JSONObject outputMessage = message == null ? new JSONObject().put("role", "assistant").put("content", "")
            : new JSONObject(message.toString());
        JSONArray calls = outputMessage.optJSONArray("tool_calls");
        if (calls != null) for (int i = 0; i < calls.length(); i++) {
            JSONObject call = calls.optJSONObject(i);
            JSONObject function = call == null ? null : call.optJSONObject("function");
            if (function == null) continue;
            String arguments = function.optString("arguments", "{}");
            try {
                function.put("arguments", new JSONObject(arguments));
            } catch (JSONException ignored) {
                function.put("arguments", new JSONObject());
            }
        }
        return new JSONObject().put("model", chat.optString("model", ""))
            .put("created_at", Instant.now().toString()).put("message", outputMessage)
            .put("done", true).put("done_reason", choice == null ? "stop" : choice.optString("finish_reason", "stop"))
            .put("total_duration", 0L).put("load_duration", 0L)
            .put("prompt_eval_count", 0).put("prompt_eval_duration", 0L)
            .put("eval_count", 0).put("eval_duration", 0L);
    }

    @NonNull
    public static JSONObject ollamaGenerateRequest(@NonNull String body) throws JSONException {
        JSONObject source = body.trim().isEmpty() ? new JSONObject() : new JSONObject(body);
        JSONArray messages = new JSONArray();
        String system = source.optString("system", "");
        if (!system.isEmpty()) messages.put(new JSONObject().put("role", "system").put("content", system));
        messages.put(new JSONObject().put("role", "user").put("content", source.optString("prompt", "")));
        JSONObject chatLike = new JSONObject(source.toString());
        chatLike.put("messages", messages);
        return ollamaChatRequest(chatLike.toString());
    }

    @NonNull
    public static JSONObject ollamaGenerateFromOpenAi(@NonNull JSONObject chat) throws JSONException {
        JSONObject converted = ollamaChatFromOpenAi(chat);
        if (converted.has("error")) return converted;
        JSONObject message = converted.optJSONObject("message");
        converted.remove("message");
        converted.put("response", message == null ? "" : message.optString("content", ""));
        return converted;
    }

    @NonNull
    public static JSONObject ollamaPs(@NonNull JSONObject openAiModels, @NonNull TaiRuntimeState state) throws JSONException {
        JSONArray models = new JSONArray();
        if (state.loaded && state.loadedModelId != null) {
            JSONObject source = findModel(openAiModels, state.loadedModelId);
            models.put(new JSONObject().put("name", state.loadedModelId).put("model", state.loadedModelId)
                .put("size", source == null ? 0L : source.optLong("_size", 0L)).put("size_vram", 0L)
                .put("digest", source == null ? "" : nullableString(source, "_sha256"))
                .put("details", source == null ? new JSONObject() : ollamaDetails(source))
                .put("expires_at", state.keepWarmUntilMs > 0 ? Instant.ofEpochMilli(state.keepWarmUntilMs).toString() : Instant.now().toString())
                .put("context_length", source == null ? 0 : source.optInt("_endpoint_context_window", 0)));
        }
        return new JSONObject().put("models", models);
    }

    @NonNull
    public static JSONObject ollamaEmbedRequest(@NonNull String body) throws JSONException {
        JSONObject source = body.trim().isEmpty() ? new JSONObject() : new JSONObject(body);
        return new JSONObject().put("model", source.optString("model", ""))
            .put("input", source.opt("input"));
    }

    @NonNull
    public static JSONObject ollamaEmbedFromOpenAi(@NonNull JSONObject response, String model) throws JSONException {
        if (response.has("error")) return response;
        JSONArray embeddings = new JSONArray();
        JSONArray data = response.optJSONArray("data");
        if (data != null) for (int i = 0; i < data.length(); i++) {
            JSONObject item = data.optJSONObject(i);
            if (item != null) embeddings.put(item.optJSONArray("embedding"));
        }
        return new JSONObject().put("model", model).put("embeddings", embeddings)
            .put("total_duration", 0L).put("load_duration", 0L).put("prompt_eval_count", 0);
    }

    @NonNull
    public static JSONObject ollamaError(int status, String code, String message) throws JSONException {
        return new JSONObject().put("error", message).put("code", code).put("_statusCode", status);
    }

    private static JSONObject findModel(JSONObject models, String id) {
        JSONArray data = models.optJSONArray("data");
        if (data == null) return null;
        for (int i = 0; i < data.length(); i++) {
            JSONObject item = data.optJSONObject(i);
            if (item != null && id.equals(item.optString("id", ""))) return item;
        }
        return null;
    }

    private static JSONObject ollamaDetails(JSONObject model) throws JSONException {
        String family = nullableString(model, "_architecture");
        return new JSONObject().put("parent_model", "").put("format", nullableString(model, "_format"))
            .put("family", family).put("families", family.isEmpty() ? new JSONArray() : new JSONArray().put(family))
            .put("parameter_size", nullableString(model, "_parameter_size"))
            .put("quantization_level", nullableString(model, "_quantization"));
    }

    private static boolean contains(JSONArray values, String expected) {
        if (values == null) return false;
        for (int i = 0; i < values.length(); i++) if (expected.equals(values.optString(i, ""))) return true;
        return false;
    }

    private static JSONObject zeroUsage() throws JSONException {
        return new JSONObject().put("input_tokens", 0).put("output_tokens", 0).put("total_tokens", 0);
    }

    private static String nullableString(JSONObject object, String key) {
        if (object == null || !object.has(key) || object.isNull(key)) return "";
        String value = object.optString(key, "");
        return "null".equalsIgnoreCase(value) ? "" : value;
    }

    private static void copy(JSONObject source, JSONObject target, String... keys) throws JSONException {
        for (String key : keys) if (source.has(key)) target.put(key, source.opt(key));
    }
}
