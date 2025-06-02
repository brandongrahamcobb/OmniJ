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

// For Charset
import java.nio.charset.StandardCharsets;

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

    private String completionApiUrl = Maps.OPENAI_ENDPOINT_URLS.get("completions");
    private String moderationApiUrl = Maps.OPENAI_ENDPOINT_URLS.get("moderations");
    private String responseApiUrl = Maps.OPENAI_ENDPOINT_URLS.get("responses");
    private String openRouterCompletionApiUrl = Maps.OPENROUTER_ENDPOINT_URLS.get("completions");
    private String openRouterModerationApiUrl = Maps.OPENROUTER_ENDPOINT_URLS.get("moderations");
    private String openRouterResponseApiUrl = Maps.OPENROUTER_ENDPOINT_URLS.get("responses");
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
        String requestType,
        String instructions,
        String provider,
        boolean stream
    ) {
        return completeCalculateMaxOutputTokens(model, content).thenApplyAsync(tokens -> {
            Map<String, Object> body = new HashMap<>();
            if (provider.toLowerCase().equals("openrouter")) {
                
            } else if (provider.toLowerCase().equals("llama")) {
                // TODO: consider other endpoints other than chat/completions
                body.put("model", ModelRegistry.LOCAL_RESPONSE_MODEL.asString());
                List<Map<String, Object>> messages = new ArrayList<>();
                Map<String, Object> msgMap = new HashMap<>();
                Map<String, Object> systemMsg = new HashMap<>();
                systemMsg.put("role", "system");
                systemMsg.put("content", instructions);
                Map<String, Object> userMsg = new HashMap<>();
                userMsg.put("role", "user");
                userMsg.put("content", content);
                body.put("stream", true);
                messages.add(systemMsg);
                messages.add(userMsg);
                body.put("messages", messages);
            } else if (provider.toLowerCase().equals("openai")) {
                if ("completion".equals(requestType)) {
                    body.put("model", ModelRegistry.OPENAI_RESPONSE_MODEL.asString());
                    List<Map<String, Object>> messages = new ArrayList<>();
                    Map<String, Object> msgMap = new HashMap<>();
                    Map<String, Object> systemMsg = new HashMap<>();
                    systemMsg.put("role", "system");
                    systemMsg.put("content", instructions);
                    Map<String, Object> userMsg = new HashMap<>();
                    userMsg.put("role", "user");
                    userMsg.put("content", content);
                    messages.add(systemMsg);
                    messages.add(userMsg);
                    body.put("messages", messages);
                }
                else if ("moderation".equals(requestType)) {
                    body.put("model", model);
                    ModelInfo info = Maps.OPENAI_RESPONSE_MODEL_CONTEXT_LIMITS.get(model);
                    body.put("input", content);
                    if (ModelRegistry.OPENAI_RESPONSE_STORE.asBoolean()) {
                        body.put("metadata", List.of(Map.of("timestamp", LocalDateTime.now().toString())));
                    }
                }
                else if ("response".equals(requestType)){
                    if (model == null) {
                        String setting = ModelRegistry.LOCAL_RESPONSE_MODEL.asString();
                        body.put("model", setting);
                        ModelInfo info = Maps.OPENAI_RESPONSE_MODEL_CONTEXT_LIMITS.get(setting);
                        if (info != null && info.status()) {
                            body.put("max_output_tokens", tokens);
                        } else {
                            body.put("max_tokens", tokens);
                        }
                    } else {
                        body.put("model", model);
                        ModelInfo info = Maps.OPENAI_RESPONSE_MODEL_CONTEXT_LIMITS.get(model);
                        if (info != null && info.status()) {
                            body.put("max_output_tokens", tokens);
                        } else {
                            body.put("max_tokens", tokens);
                        }
                    }
                    body.put("instructions", ModelRegistry.SHELL_RESPONSE_SYS_INPUT.asString());
                    List<Map<String, Object>> messages = new ArrayList<>();
                    Map<String, Object> msgMap = new HashMap<>();
                    msgMap.put("role", "user");
                    msgMap.put("content", content);
                    messages.add(msgMap);
                    body.put("input", messages);
                    body.put("stream", stream);
                    if (previousResponseId != null && !previousResponseId.isEmpty()) {
                        body.put("previous_response_id", previousResponseId);
                    }
                    if (ModelRegistry.OPENAI_RESPONSE_STORE.asBoolean()) {
                        body.put("metadata", List.of(Map.of("timestamp", LocalDateTime.now().toString())));
                    }
                }
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
                ModelInfo outputInfo = Maps.OPENAI_RESPONSE_MODEL_OUTPUT_LIMITS.get(model);
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

    public CompletableFuture<MetadataContainer> completeRequest(
            String content,
            String previousResponseId,
            String model,
            String requestType,
            String provider,
            boolean stream,
            Consumer<String> onContentChunk
    ) {
        switch (provider.toLowerCase()) {
            case "llama":
                return completeLlamaRequest(content, previousResponseId, model, requestType, stream, onContentChunk);

            //case "ollama":
              //  return completeOllamaRequest(content, previousResponseId, model, requestType);

            case "openai":
                return completeOpenAIRequest(content, previousResponseId, model, requestType, stream);

            case "openrouter":
                return completeOpenRouterRequest(content, previousResponseId, model, requestType, stream);

            default:
                return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Unknown provider: " + provider)
                );
        }
    }
    
    public CompletableFuture<MetadataContainer> completeLlamaRequest(String content, String previousResponseId, String model, String requestType, boolean stream, Consumer<String> onContentChunk) {
        SchemaMerger sm = new SchemaMerger();
        String instructions = switch (requestType) {
            case "completion" -> "";
            case "moderation" -> ""; //TODO
            case "response" -> "";
            default -> throw new IllegalArgumentException("Unsupported requestType: " + requestType);
        };
        String endpoint = "completion";
        return completeBuildRequestBody(content, previousResponseId, model, requestType, instructions, "llama", stream)
                .thenCompose(reqBody -> completeProcessStreamedLlamaRequest(reqBody, endpoint, onContentChunk));
    }

    public CompletableFuture<MetadataContainer> completeOpenAIRequest(String content, String previousResponseId, String model, String requestType, boolean stream) {
        SchemaMerger sm = new SchemaMerger();
        String endpoint = "moderation".equals(requestType)
            ? moderationApiUrl
            : "completion".equals(requestType)
                ? completionApiUrl
                : responseApiUrl;
        String instructions = switch (requestType) {
            case "completion" -> "";
            case "moderation" -> "";
            case "response" -> ModelRegistry.SHELL_RESPONSE_SYS_INPUT.asString() + sm.completeGetShellToolSchemaNestResponse().join(); // or provide appropriate default
            default -> throw new IllegalArgumentException("Unsupported requestType: " + requestType);
        };
        return completeBuildRequestBody(content, previousResponseId, model, requestType, instructions, "openai", stream)
                .thenCompose(reqBody -> completeProcessOpenAIRequest(reqBody, endpoint));
    }
    
    public CompletableFuture<MetadataContainer> completeOpenRouterRequest(String content, String previousResponseId, String model, String requestType, boolean stream) {
        SchemaMerger sm = new SchemaMerger();
        String endpoint = "moderation".equals(requestType)
            ? moderationApiUrl
            : "completion".equals(requestType)
                ? openRouterCompletionApiUrl
                : openRouterResponseApiUrl;
        String instructions = switch (requestType) {
            case "completion" -> "";
            case "moderation" -> ""; //TODO
            case "response" -> ModelRegistry.SHELL_RESPONSE_SYS_INPUT.asString() + sm.completeGetShellToolSchemaNestResponse().join();
            default -> throw new IllegalArgumentException("Unsupported requestType: " + requestType);
        };
        return completeBuildRequestBody(content, previousResponseId, model, requestType, instructions, "openrouter", stream)
                .thenCompose(reqBody -> completeProcessOpenRouterRequest(reqBody, endpoint));
    }
    
    public CompletableFuture<MetadataContainer> completeProcessStreamedLlamaRequest(
        Map<String, Object> requestBody,
        String endpoint,
        Consumer<String> onContentChunk
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = HttpClients.custom()
                    .setDefaultRequestConfig(REQUEST_CONFIG)
                    .build()) {

                HttpPost post = new HttpPost("http://localhost:8080/api/chat");
                post.setHeader("Content-Type", "application/json");
                requestBody.put("stream", true);  // force stream true

                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(requestBody);
                post.setEntity(new StringEntity(json));

                try (CloseableHttpResponse resp = client.execute(post)) {
                    int code = resp.getStatusLine().getStatusCode();
                    System.out.println(code);
                    if (code < 200 || code >= 300) {
                        String respBody = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
                        throw new IOException("HTTP " + code + ": " + respBody);
                    }
                    StringBuilder builder = new StringBuilder();
                    Map<String, Object> lastChunk = null;

                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(resp.getEntity().getContent(), StandardCharsets.UTF_8))) {
                                
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (!line.startsWith("data:")) continue;

                            String data = line.substring(5).trim();
                            if (data.equals("[DONE]")) break;

                            Map<String, Object> chunk = mapper.readValue(data, new TypeReference<>() {});
                            lastChunk = chunk;

                            Map<String, Object> delta = (Map<String, Object>) ((Map<String, Object>) ((List<?>) chunk.get("choices")).get(0)).get("delta");
                            String content = (String) delta.get("content");

                            if (content != null && !content.isBlank()) {
                                onContentChunk.accept(content);
                                builder.append(content);
                            }
                        }
                    }

                    if (lastChunk == null) {
                        throw new IllegalStateException("No valid chunk received.");
                    }

                    // Build final metadata container with content
                    LlamaContainer container = new LlamaContainer(lastChunk);
                    container.put(new MetadataKey<>("content", Metadata.STRING), builder.toString());

                    return container;

                }

            } catch (Exception e) {
                throw new RuntimeException("Local stream request failed: " + e.getMessage(), e);
            }
        });
    }





    private CompletableFuture<MetadataContainer> completeProcessLMStudioRequest(Map<String, Object> requestBody, String endpoint) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = HttpClients.custom()
                    .setDefaultRequestConfig(REQUEST_CONFIG)
                    .build()) {
                HttpPost post = new HttpPost("http://127.0.0.1:1234/v1/chat/completions");
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
                            return new OpenAIContainer(outer);  // Fallback: wrap entire response if not endpoint-specific
                        }
                    } else {
                        System.out.println(code);
                        throw new IOException("HTTP " + code + ": " + respBody);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Local request failed" + e.getMessage(), e);
            }
        });
    }
    
    private CompletableFuture<MetadataContainer> completeProcessOpenRouterRequest(Map<String, Object> requestBody, String endpoint) {
        String apiKey = System.getenv("OPENROUTER_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Missing OPENROUTER_API_KEY"));
        }

        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = HttpClients.custom()
                    .setDefaultRequestConfig(REQUEST_CONFIG)
                    .build()) {

                HttpPost post = new HttpPost("https://openrouter.ai/api/v1/chat/completions");
                post.setHeader("Content-Type", "application/json");
                post.setHeader("Authorization", "Bearer " + apiKey);

                ObjectMapper mapper = new ObjectMapper();
                requestBody.putIfAbsent("stream", false);
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
                            // Content is raw JSON as a string
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
    
    private CompletableFuture<MetadataContainer> completeProcessOpenAIRequest(Map<String, Object> requestBody, String endpoint) {
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
                        Map<String, Object> inner = mapper.readValue(jsonContent, new TypeReference<>() {});
                        MetadataKey<String> previousResponseIdKey = new MetadataKey<>("id", Metadata.STRING);
                        String previousResponseId = (String) openaiOuterUtils.completeGetResponseId().join();
                        OpenAIContainer openaiInnerResponse = new OpenAIContainer(inner);
                        openaiInnerResponse.put(previousResponseIdKey, previousResponseId);
                        return (MetadataContainer) openaiInnerResponse;
                    } else {
                        throw new IOException("Unexpected response code: " + statusCode + ", body: " + responseBody);
                    }
                }
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }
    
    private CompletableFuture<Map<String, Object>> completeOpenRouterRequest(
        String content,
        String previousResponseId,
        String model,
        String requestType,
        String instructions
    ) {
        return completeCalculateMaxOutputTokens(model, content).thenApplyAsync(tokens -> {
            Map<String, Object> body = new HashMap<>();
            if (model == null) {
                String setting = ModelRegistry.OPENROUTER_RESPONSE_MODEL.asString();
                body.put("model", setting);
                ModelInfo info = Maps.OPENROUTER_RESPONSE_MODEL_CONTEXT_LIMITS.get(setting);
                if (info != null && info.status()) {
                    body.put("max_output_tokens", tokens);
                } else {
                    body.put("max_tokens", tokens);
                }
            } else {
                body.put("model", model);
                ModelInfo info = Maps.OPENROUTER_RESPONSE_MODEL_CONTEXT_LIMITS.get(model);
                if (info != null && info.status()) {
                    body.put("max_output_tokens", tokens);
                } else {
                    body.put("max_tokens", tokens);
                }
            }
            body.put("text", Map.of("format", Maps.GEMINI_RESPONSE_FORMAT));
            body.put("instructions", instructions);
            List<Map<String, Object>> messages = new ArrayList<>();
            Map<String, Object> msgMap = new HashMap<>();
            msgMap.put("role", "user");
            msgMap.put("content", content);
            messages.add(msgMap);
            body.put("input", messages);
            if (previousResponseId != null && !previousResponseId.isEmpty()) {
                body.put("previous_response_id", previousResponseId);
            }
            if (ModelRegistry.OPENAI_RESPONSE_STORE.asBoolean()) {
                body.put("metadata", List.of(Map.of("timestamp", LocalDateTime.now().toString())));
            }
            return body;
        });
    }

    public List<String> searchVectorStore(
            String vectorStoreId,
            String query,
            Integer maxResults,
            Map<String, Object> filters,
            Map<String, Object> rankingOptions
    ) {
        // TODO: implement vector store/file_search lookup
        return List.of();
    }

    public CompletableFuture<String> completeResolveModel(String content, Boolean multiModal, String model) { //TODO Deprecated
        return CompletableFuture.supplyAsync(() -> model);
    }
}
