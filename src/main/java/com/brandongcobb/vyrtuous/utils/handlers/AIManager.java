/*  AIManager.java The primary purpose of this class is to manage the=
 *  core AI functions of Vyrtuous.
 *
 *  Copyright (C) 2025  github.com/brandongrahamcobb
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.brandongcobb.vyrtuous.utils.handlers;

import com.brandongcobb.vyrtuous.Vyrtuous;
import com.brandongcobb.metadata.*;
import com.brandongcobb.vyrtuous.records.ModelInfo;
import com.brandongcobb.vyrtuous.utils.handlers.*;
import com.brandongcobb.vyrtuous.utils.inc.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
// For Consumer
import java.util.function.Consumer;

// For Map
import java.util.Map;

// For BufferedReader and InputStreamReader
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// For Charset
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class AIManager {
    
    private EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
    private static final RequestConfig REQUEST_CONFIG = RequestConfig.custom()
            .setConnectTimeout(10_000)
            .setConnectionRequestTimeout(10_000)
            .setSocketTimeout(600_000)
            .build();
    private StringBuilder builder = new StringBuilder();

    private CompletableFuture<Map<String, Object>> completeBuildRequestBody(
        String content,
        String previousResponseId,
        String model,
        String endpointWithState,
        String instructions,
        boolean stream
    ) {
        return completeCalculateMaxOutputTokens(model, content).thenApplyAsync(tokens -> {
            Map<String, Object> body = new HashMap<>();
            final int stateIndex = endpointWithState.indexOf("?state");
            final String endpoint = (stateIndex != -1)
                ? endpointWithState.substring(0, stateIndex)
                : endpointWithState;
            List<Map<String, Object>> messages = new ArrayList<>();
            Map<String, Object> msgMap = new HashMap<>();
            Map<String, Object> userMsg = new HashMap<>();
            Map<String, Object> systemMsg = new HashMap<>();
            switch (Maps.BUILD_PROTOCOL.get(endpoint)) {
                case "deprecated":
                    body.put("model", model);
                    systemMsg.put("role", "system");
                    systemMsg.put("content", instructions);
                    userMsg.put("role", "user");
                    userMsg.put("content", content);
                    body.put("stream", stream);
                    messages.add(systemMsg);
                    messages.add(userMsg);
                    body.put("messages", messages);
                case "moderation":
                    body.put("model", model);
                    body.put("input", content);
                    body.put("metadata", List.of(Map.of("timestamp", LocalDateTime.now().toString())));
                case "latest":
                    body.put("placeholder", "");
                case "response":
                    body.put("model", model);
                    ModelInfo info = Maps.RESPONSE_MODEL_CONTEXT_LIMITS.get(model);
                    if (info != null && info.status()) {
                        body.put("max_output_tokens", tokens);
                    } else {
                        body.put("max_tokens", tokens);
                    }
                    body.put("instructions", instructions);
                    systemMsg.put("role", "system");
                    systemMsg.put("content", instructions);
                    userMsg.put("role", "user");
                    userMsg.put("content", content);
                    messages.add(systemMsg);
                    messages.add(userMsg);
                    body.put("input", messages);
                    body.put("stream", stream);
                    if (previousResponseId != null && !previousResponseId.isEmpty()) {
                        body.put("previous_response_id", previousResponseId);
                    }
                    body.put("metadata", List.of(Map.of("timestamp", LocalDateTime.now().toString())));
                default:
                    body.put("placeholder", "");
            }
            return body;
        });
    }

    private CompletableFuture<Long> completeCalculateMaxOutputTokens(String model, String prompt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Encoding encoding = registry.getEncoding("cl100k_base")
                    .orElseThrow(() -> new IllegalStateException("Encoding cl100k_base not available"));
                long promptTokens = encoding.encode(prompt).size();
                ModelInfo outputInfo = Maps.RESPONSE_MODEL_OUTPUT_LIMITS.get(model);
                long outputLimit = outputInfo != null ? outputInfo.upperLimit() : 4096;
                long tokens = Math.max(1, outputLimit - promptTokens - 20);
                if (tokens < 16) tokens = 16;
                return tokens;
            } catch (Exception e) {
                return 0L;
            }
        });
    }
    
    public CompletableFuture<Map<String, Object>> completeCreateVectorStore(List<String> fileIds) {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Missing OPENAI_API_KEY"));
        }
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = HttpClients.custom()
                    .setDefaultRequestConfig(REQUEST_CONFIG)
                    .build()) {
                HttpPost post = new HttpPost("https://api.openai.com/v1/vector_stores");
                post.setHeader("Authorization", "Bearer " + apiKey);
                post.setHeader("Content-Type", "application/json");
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> body = Map.of("file_ids", fileIds);
                String json = mapper.writeValueAsString(body);
                post.setEntity(new StringEntity(json));
                try (CloseableHttpResponse resp = client.execute(post)) {
                    int code = resp.getStatusLine().getStatusCode();
                    String respBody = EntityUtils.toString(resp.getEntity(), "UTF-8");
                    System.out.println("📦 Vector Store Response:\n" + respBody);
                    if (code >= 200 && code < 300) {
                        return mapper.readValue(respBody, new TypeReference<Map<String, Object>>() {});
                    } else {
                        throw new IOException("HTTP " + code + ": " + respBody);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to create vector store", e);
            }
        });
    }

    /*
     *  llama.cpp
     */
    private CompletableFuture<MetadataContainer> completeLlamaRequest(
        String content,
        String previousResponseId,
        String model,
        String endpointWithState,
        boolean stream,
        Consumer<String> onContentChunk
    ) {
        final int stateIndex = endpointWithState.indexOf("?state");
        final String endpoint = (stateIndex != -1)
            ? endpointWithState.substring(0, stateIndex)
            : endpointWithState;
        return completeBuildRequestBody(content, previousResponseId, model, endpointWithState, Maps.INSTRUCTIONS.get(endpointWithState), stream)
            .thenCompose(reqBody -> completeLlamaProcessRequest(reqBody, endpoint, onContentChunk));
    }

    public static String removeThinkBlocks(String text) {
        // Use a regular expression to find and replace the <think>...</think> pattern
        // (?s) enables the DOTALL mode, so dot (.) matches newline characters as well.
        // <think> matches the starting tag.
        // .*? matches any characters (non-greedily) between the tags.
        // </think> matches the closing tag.
        String regex = "(?s)<think>.*?</think>";
        return text.replaceAll(regex, "");
    }
    
    private CompletableFuture<MetadataContainer> completeLlamaProcessRequest(
        Map<String, Object> requestBody,
        String endpoint,
        Consumer<String> onContentChunk
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = HttpClients.custom()
                    .setDefaultRequestConfig(REQUEST_CONFIG)
                    .build()) {
                HttpPost post = new HttpPost(endpoint);
                post.setHeader("Content-Type", "application/json");
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(requestBody);
                post.setEntity(new StringEntity(json));
                try (CloseableHttpResponse resp = client.execute(post)) {
                    int code = resp.getStatusLine().getStatusCode();
                    String respBody = null;
                    if (code <= 200 || code > 300) {
                        respBody = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
                        if (onContentChunk == null) {
                            Map<String, Object> outer = mapper.readValue(respBody, new TypeReference<>() {});
                            LlamaContainer llamaOuterResponse = new LlamaContainer(outer);
                            LlamaUtils llamaOuterUtils = new LlamaUtils(llamaOuterResponse);
                            String content = llamaOuterUtils.completeGetContent().join();
                            // Remove ```json markers if present
                            String jsonContent = "";
                            if (content.startsWith("<think>")) {
                                jsonContent = removeThinkBlocks(content);
                            } else {
                                jsonContent = content
                                    .replaceFirst("^```json\\s*", "")
                                    .replaceFirst("\\s*```$", "")
                                    .trim();
                            }
                            // Match the "commands": [ ... ] section
                            Pattern pattern = Pattern.compile("\"commands\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL);
                            Matcher matcher = pattern.matcher(jsonContent);
                            String value = null;
                            if (matcher.find()) {
                                String originalCommands = matcher.group(1); // everything inside the brackets

                                // Apply escapes
                                String escaped = originalCommands
                                    .replace("\\", "\\\\\\\\")  // quadruple backslashes
                                    .replace("\"", "\\\"")      // escape double quotes
                                    .replace("$", "\\$")        // escape $
                                    .replace("`", "\\`")
                                    .replace("(", "\\(")
                                    .replace(")", "\\)")
                                    .replace(":", "\\:")
                                    .replace("{", "\\{")
                                    .replace("}", "\\}")
                                    .replace("@", "\\@")
                                    .replace("'", "\\'");

                                // Reconstruct
                                String replacedJson = matcher.replaceFirst("\"commands\": [" + escaped + "]");
                                value = replacedJson;
                            } else {
                                value = jsonContent;
                            }
                            
                            System.out.println(Vyrtuous.CYAN + value + Vyrtuous.RESET);
                            Map<String, Object> inner = mapper.readValue(value, new TypeReference<>() {});
                            MetadataKey<String> previousResponseIdKey = new MetadataKey<>("id", Metadata.STRING);
                            String previousResponseId = UUID.randomUUID().toString();
                            inner.put("id", previousResponseId);
                            ToolContainer toolResponse = new ToolContainer(inner);
                            toolResponse.put(previousResponseIdKey, previousResponseId);
                            return (MetadataContainer) toolResponse;
                        } else {
                            StringBuilder builder = new StringBuilder();
                            Map<String, Object> lastChunk = null;
                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent(), StandardCharsets.UTF_8))) {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    if (!line.startsWith("data:")) continue;
                                    String data = line.substring(5).trim();
                                    if (data.equals("[DONE]")) break;
                                    Map<String, Object> chunk = mapper.readValue(data, new TypeReference<>() {});
                                    lastChunk = chunk;
                                    Map<String, Object> delta = (Map<String, Object>) ((Map<String, Object>) ((List<?>) chunk.get("choices")).get(0)).get("delta");
                                    String content = (String) delta.get("content");
                                    if (content != null) {
                                        onContentChunk.accept(content);
                                        builder.append(content);
                                    }
                                }
                            }
                            if (lastChunk == null) {
                                throw new IllegalStateException("No valid chunk received.");
                            }
                            LlamaContainer container = new LlamaContainer(lastChunk);
                            container.put(new MetadataKey<>("content", Metadata.STRING), builder.toString());
                            return container;
                        }
                    } else {
                        throw new IOException("HTTP " + code + ": " + respBody);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Local stream request failed: " + e.getMessage(), e);
            }
        });
    }
    
    /*
     *  lmstudio
     */
    private CompletableFuture<MetadataContainer> completeLMStudioRequest(String content, String previousResponseId, String model, String endpointWithState, boolean stream, Consumer<String> onContentChunk) {
        final int stateIndex = endpointWithState.indexOf("?state");
        final String endpoint = (stateIndex != -1)
            ? endpointWithState.substring(0, stateIndex)
            : endpointWithState;
        return completeBuildRequestBody(content, previousResponseId, model, endpointWithState, Maps.INSTRUCTIONS.get(endpointWithState), stream)
                .thenCompose(reqBody -> completeLMStudioProcessRequest(reqBody, endpoint, onContentChunk));
    }
    
    private CompletableFuture<MetadataContainer> completeLMStudioProcessRequest(Map<String, Object> requestBody, String endpoint, Consumer<String> onContentChunk) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = HttpClients.custom()
                    .setDefaultRequestConfig(REQUEST_CONFIG)
                    .build()) {
                HttpPost post = new HttpPost(endpoint);
                post.setHeader("Content-Type", "application/json");
                requestBody.putIfAbsent("stream", false);
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(requestBody);
                post.setEntity(new StringEntity(json));
                try (CloseableHttpResponse resp = client.execute(post)) {
                    int code = resp.getStatusLine().getStatusCode();
                    String respBody = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
                    if (code >= 200 && code < 300) {
                        Map<String, Object> outer = mapper.readValue(respBody, new TypeReference<>() {});
                        if (endpoint.contains("response")) {
                            Map<String, Object> message = (Map<String, Object>) outer.get("message");
                            String content = (String) message.get("content");
                            String jsonContent = content
                            .replaceFirst("^```json\\s*", "")
                            .replaceFirst("\\s*```$", "")
                            .trim();
                            Map<String, Object> inner = mapper.readValue(jsonContent, new TypeReference<>() {});
                            OpenAIContainer response = new OpenAIContainer(inner);
                            return (MetadataContainer) response;
                        } else {
                            return new OpenAIContainer(outer);
                        }
                    } else {;
                        throw new IOException("HTTP " + code + ": " + respBody);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Local request failed" + e.getMessage(), e);
            }
        });
    }
    
    /*
     *  Ollama
     */
    private CompletableFuture<MetadataContainer> completeOllamaRequest(String content, String previousResponseId, String model, String endpointWithState, boolean stream, Consumer<String> onContentChunk) {
        final int stateIndex = endpointWithState.indexOf("?state");
        final String endpoint = (stateIndex != -1)
            ? endpointWithState.substring(0, stateIndex)
            : endpointWithState;
        return completeBuildRequestBody(content, previousResponseId, model, endpointWithState, Maps.INSTRUCTIONS.get(endpointWithState), stream)
                .thenCompose(reqBody -> completeLMStudioProcessRequest(reqBody, endpoint, onContentChunk));
    }
    
    /*
     *  OpenAI
     */
    private CompletableFuture<MetadataContainer> completeOpenAIRequest(String content, String previousResponseId, String model, String endpointWithState, boolean stream, Consumer<String> onContentChunk) {
        final int stateIndex = endpointWithState.indexOf("?state");
        final String endpoint = (stateIndex != -1)
            ? endpointWithState.substring(0, stateIndex)
            : endpointWithState;
        return completeBuildRequestBody(content, previousResponseId, model, endpointWithState, Maps.INSTRUCTIONS.get(endpointWithState), stream) // TODO: remove the get shell tool schema from this definition and embed it into the instruction
                .thenCompose(reqBody -> completeOpenAIProcessRequest(reqBody, endpoint, onContentChunk));
    }
    
    private CompletableFuture<MetadataContainer> completeOpenAIProcessRequest(Map<String, Object> requestBody, String endpoint, Consumer<String> onContentChunk) {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Missing OPENAI_API_KEY"));
        }
        return CompletableFuture.supplyAsync(() -> {
            ObjectMapper mapper = new ObjectMapper();
            try (CloseableHttpClient client = HttpClients.custom()
                    .setDefaultRequestConfig(REQUEST_CONFIG)
                    .build()) {
                HttpPost post = new HttpPost(endpoint);
                post.setHeader("Authorization", "Bearer " + apiKey);
                post.setHeader("Content-Type", "application/json");
                String json = mapper.writeValueAsString(requestBody);
                post.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
                try (CloseableHttpResponse resp = client.execute(post)) {
                    int statusCode = resp.getStatusLine().getStatusCode();
                    String responseBody = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
                    if (statusCode >= 200 && statusCode < 300) {
                        Map<String, Object> outer = mapper.readValue(responseBody, new TypeReference<>() {});
                        OpenAIContainer openaiOuterResponse = new OpenAIContainer(outer);
                        OpenAIUtils openaiOuterUtils = new OpenAIUtils(openaiOuterResponse);
                        String content = (String) openaiOuterUtils.completeGetOutput().join();
                        String jsonContent = content
                            .replaceFirst("^```json\\s*", "")
                            .replaceFirst("\\s*```$", "")
                            .trim();
                        if (!jsonContent.contains("local_shell_command_sequence_finished")) {
                            throw new Exception("CRITICAL ERROR");
                        }
                        System.out.flush();
                        Map<String, Object> inner = mapper.readValue(jsonContent, new TypeReference<>() {});
                        MetadataKey<String> previousResponseIdKey = new MetadataKey<>("id", Metadata.STRING);
                        String previousResponseId = (String) openaiOuterUtils.completeGetResponseId().join();
                        String text = (String) openaiOuterUtils.completeGetText().join();
                        String reasoning = (String) openaiOuterUtils.completeGetText().join();
                        inner.put("id", previousResponseId);
                        String summary = (String) inner.get("summary");
                        inner.put("summary", text + reasoning + summary);
                        ToolContainer toolResponse = new ToolContainer(inner);
                        toolResponse.put(previousResponseIdKey, previousResponseId);
                        return (MetadataContainer) toolResponse;
                    } else {
                        throw new IOException("Unexpected response code: " + statusCode + ", body: " + responseBody);
                    }
                }
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }
    
    /*
     *  OpenRouter
     */
    private CompletableFuture<MetadataContainer> completeOpenRouterRequest(String content, String previousResponseId, String model, String endpointWithState, boolean stream, Consumer<String> onContentChunk) {
        final int stateIndex = endpointWithState.indexOf("?state");
        final String endpoint = (stateIndex != -1)
            ? endpointWithState.substring(0, stateIndex)
            : endpointWithState;
        return completeBuildRequestBody(content, previousResponseId, model, endpointWithState, Maps.INSTRUCTIONS.get(endpointWithState), stream)
                .thenCompose(reqBody -> completeOpenRouterProcessRequest(reqBody, endpoint, onContentChunk));
    }
    
    private CompletableFuture<MetadataContainer> completeOpenRouterProcessRequest(Map<String, Object> requestBody, String endpoint, Consumer<String> onContentChunk) {
        String apiKey = System.getenv("OPENROUTER_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Missing OPENROUTER_API_KEY"));
        }
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = HttpClients.custom()
                    .setDefaultRequestConfig(REQUEST_CONFIG)
                    .build()) {
                HttpPost post = new HttpPost(endpoint);
                post.setHeader("Content-Type", "application/json");
                post.setHeader("Authorization", "Bearer " + apiKey);
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(requestBody);
                post.setEntity(new StringEntity(json));
                try (CloseableHttpResponse resp = client.execute(post)) {
                    int code = resp.getStatusLine().getStatusCode();
                    String respBody = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
                    if (code >= 200 && code < 300) {
                        Map<String, Object> outer = mapper.readValue(respBody, new TypeReference<>() {});
                        String id = (String) outer.get("id");
                        List<Map<String, Object>> choices = (List<Map<String, Object>>) outer.get("choices");
                        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                        String content = (String) message.get("content");
                        if (id != null && id.startsWith("gen-")) {
                            Map<String, Object> inner = mapper.readValue(content, new TypeReference<>() {});
                            OpenAIContainer response = new OpenAIContainer(inner);
                            return (MetadataContainer) response;
                        } else if (id != null && id.startsWith("resp_")) {
                            String jsonContent = content
                                .replaceFirst("^```json\\s*", "")
                                .replaceFirst("\\s*```$", "")
                                .trim();
                            Map<String, Object> inner = mapper.readValue(jsonContent, new TypeReference<>() {});
                            OpenAIContainer response = new OpenAIContainer(inner);
                            return (MetadataContainer) response;
                        } else {
                            OllamaContainer response = new OllamaContainer(outer);
                            return (MetadataContainer) response;
                        }
                    } else {
                        System.out.println("HTTP error code: " + code);
                        throw new IOException("HTTP " + code + ": " + respBody);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Local request failed: " + e.getMessage(), e);
            }
        });
    }

    public List<String> searchVectorStore(
            String vectorStoreId,
            String query,
            Integer maxResults,
            Map<String, Object> filters,
            Map<String, Object> rankingOptions
    ) {
        return List.of();
    }

    /*
     *  Main method
     */
    public CompletableFuture<MetadataContainer> completeRequest(
            String content,
            String previousResponseId,
            String model,
            String endpointWithState,
            boolean stream,
            Consumer<String> onContentChunk
    ) throws Exception {  // <-- declare exception here
        final int stateIndex = endpointWithState.indexOf("?state");
        final String endpoint = (stateIndex != -1)
            ? endpointWithState.substring(0, stateIndex)
            : endpointWithState;
        if (Maps.LLAMA_ENDPOINT_URLS.containsValue(endpoint)) {
            return completeLlamaRequest(content, previousResponseId, model, endpointWithState, stream, onContentChunk);
        } else if (Maps.OLLAMA_ENDPOINT_URLS.containsValue(endpoint)) {
            return completeOllamaRequest(content, previousResponseId, model, endpointWithState, stream, onContentChunk);
        } else if (Maps.OPENAI_ENDPOINT_URLS.containsValue(endpoint)) {
            return completeOpenAIRequest(content, previousResponseId, model, endpointWithState, stream, onContentChunk);
        } else if (Maps.OLLAMA_ENDPOINT_URLS.containsValue(endpoint)) {
            return completeOpenRouterRequest(content, previousResponseId, model, endpointWithState, stream, onContentChunk);
        } else {
            return CompletableFuture.failedFuture(new IllegalStateException("Invalid endpoint.")); // Prefer throwing unchecked exceptions here
        }
    }

    
    public CompletableFuture<String> getAIEndpointWithState(boolean multimodal, String requestedSource, String sourceOfRequest, String requestType) {
        String endpoint = null;
        if ("cli".equals(sourceOfRequest)) {
            if ("latest".equals(requestedSource)) {
                endpoint = Maps.LATEST_CLI_ENDPOINT_URLS.get(requestType);
            } else if ("llama".equals(requestedSource)) {
                endpoint = Maps.LLAMA_CLI_ENDPOINT_URLS.get(requestType);
            } else if ("openai".equals(requestedSource)) {
                endpoint = Maps.OPENAI_CLI_ENDPOINT_URLS.get(requestType);
            }
        } else if ("discord".equals(sourceOfRequest)) {
            if ("latest".equals(requestedSource)) {
                endpoint = multimodal
                    ? Maps.LATEST_DISCORD_MULTIMODAL_ENDPOINT_URLS.get(requestType)
                    : Maps.LATEST_DISCORD_TEXT_ENDPOINT_URLS.get(requestType);
            } else if ("llama".equals(requestedSource)) {
                endpoint = multimodal
                    ? Maps.LLAMA_DISCORD_MULTIMODAL_ENDPOINT_URLS.get(requestType)
                    : Maps.LLAMA_DISCORD_TEXT_ENDPOINT_URLS.get(requestType);
            } else if ("openai".equals(requestedSource)) {
                endpoint = multimodal
                    ? Maps.OPENAI_DISCORD_MULTIMODAL_ENDPOINT_URLS.get(requestType)
                    : Maps.OPENAI_DISCORD_TEXT_ENDPOINT_URLS.get(requestType);
            }
        } else if ("twitch".equals(sourceOfRequest)) {
            // Optional: Add Twitch logic here
        }
        if (endpoint == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException(
                "Invalid combination of requestedSource: " + requestedSource + " and sourceOfRequest: " + sourceOfRequest));
        }
        return CompletableFuture.completedFuture(endpoint);
    }
}
