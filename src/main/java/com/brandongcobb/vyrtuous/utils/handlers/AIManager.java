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
    

    private CompletableFuture<Map<String, Object>> completeBuildLocalRequestBody(
        String content,
        String previousResponseId,
        String model,
        String requestType,
        String instructions
    ) {
        return completeCalculateMaxOutputTokens(model, content).thenApplyAsync(tokens -> {
            Map<String, Object> body = new HashMap<>();
            if ("completion".equals(requestType)) {
                body.put("model", ModelRegistry.LOCAL_RESPONSE_MODEL.asString());
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
                body.put("format", "json");
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
                body.put("model", ModelRegistry.LOCAL_RESPONSE_MODEL.asString());
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
                body.put("format", "json");
            }
            return body;
        });
    }
    
    private CompletableFuture<Map<String, Object>> completeBuildWebRequestBody(
        String content,
        String previousResponseId,
        String model,
        String requestType,
        String instructions
    ) {
        return completeCalculateMaxOutputTokens(model, content).thenApplyAsync(tokens -> {
            Map<String, Object> body = new HashMap<>();
            if ("response".equals(requestType)) {
                body.put("model", ModelRegistry.OPENROUTER_RESPONSE_MODEL.asString());
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
                body.put("format", "json");
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

    public CompletableFuture<MetadataContainer> completeLocalShellRequest(String content, String previousResponseId, String model, String requestType) {
        SchemaMerger sm = new SchemaMerger();
        String endpoint = "moderation".equals(requestType)
            ? moderationApiUrl
            : "completion".equals(requestType)
                ? completionApiUrl
                : responseApiUrl;
        String instructions = switch (requestType) {
            case "completion" -> "";
            case "moderation" -> ""; //TODO
            case "response" -> ModelRegistry.SHELL_RESPONSE_SYS_INPUT.asString() + sm.completeGetShellToolSchemaNestResponse().join();
            default -> throw new IllegalArgumentException("Unsupported requestType: " + requestType);
        };
        return completeBuildLocalRequestBody(content, previousResponseId, model, requestType, instructions)
                .thenCompose(reqBody -> completeProcessLocalRequest(reqBody, endpoint));
    }

    public CompletableFuture<MetadataContainer> completeLocalWebRequest(String content, String previousResponseId, String model, String requestType) {
        SchemaMerger sm = new SchemaMerger();
        String endpoint = "moderation".equals(requestType)
            ? moderationApiUrl
            : "completion".equals(requestType)
                ? completionApiUrl
                : responseApiUrl;
        String instructions = switch (requestType) {
            case "completion" -> "";
            case "moderation" -> "";
            case "response" -> "Reply with the content in the content field of this schema: " + sm.completeGetShellToolSchemaNestResponse().join(); // or provide appropriate default
            default -> throw new IllegalArgumentException("Unsupported requestType: " + requestType);
        };
        return completeBuildLocalRequestBody(content, previousResponseId, model, requestType, instructions)
                .thenCompose(reqBody -> completeProcessLocalRequest(reqBody, endpoint));
    }
    
    public CompletableFuture<MetadataContainer> completeWebShellRequest(String content, String previousResponseId, String model, String requestType) {
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
        return completeBuildWebRequestBody(content, previousResponseId, model, requestType, instructions)
                .thenCompose(reqBody -> completeProcessWebRequest(reqBody, endpoint));
    }
    
    public CompletableFuture<MetadataContainer> completeWebRequest(String content, String previousResponseId, String model, String requestType) {
        SchemaMerger sm = new SchemaMerger();
        String endpoint = "moderation".equals(requestType)
            ? moderationApiUrl
            : "completion".equals(requestType)
                ? openRouterCompletionApiUrl
                : openRouterResponseApiUrl;
        String instructions = switch (requestType) {
            case "completion" -> "";
            case "moderation" -> "";
            case "response" -> "Reply with the content in the content field of this schema: " + sm.completeGetShellToolSchemaNestResponse().join(); // or provide appropriate default
            default -> throw new IllegalArgumentException("Unsupported requestType: " + requestType);
        };
        return completeBuildWebRequestBody(content, previousResponseId, model, requestType, instructions)
                .thenCompose(reqBody -> completeProcessWebRequest(reqBody, endpoint));
    }
    
    private CompletableFuture<MetadataContainer> completeProcessLocalRequest(Map<String, Object> requestBody, String endpoint) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = HttpClients.custom()
                    .setDefaultRequestConfig(REQUEST_CONFIG)
                    .build()) {
                HttpPost post = new HttpPost("http://localhost:11434/api/chat");
                post.setHeader("Content-Type", "application/json");
                requestBody.putIfAbsent("stream", false);
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(requestBody);
                post.setEntity(new StringEntity(json));
                try (CloseableHttpResponse resp = client.execute(post)) {
                    int code = resp.getStatusLine().getStatusCode();
                    String respBody = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
                    System.out.println(respBody);
                    if (code >= 200 && code < 300) {
                        Map<String, Object> outer = mapper.readValue(respBody, new TypeReference<>() {});
                        Map<String, Object> message = (Map<String, Object>) outer.get("message");
                        String content = message != null ? (String) message.get("content") : null;
                        String jsonContent = content.strip()
                            .replaceFirst("^```json\\s*", "")
                            .replaceFirst("\\s*```$", "")
                            .trim();
                        Map<String, Object> map = mapper.readValue(jsonContent, new TypeReference<Map<String, Object>> () {});
                        String id = map.containsKey("id") ? (String) map.get("id") : null;
                        if (id != null && id.startsWith("gen-")) {
                            ResponseObject response = new ResponseObject(map);
                            return (MetadataContainer) response;
                        } else if (id != null && id.startsWith("resp_")) {
                            System.out.println("test");
                            ResponseObject response = new ResponseObject(map);
                            return (MetadataContainer) response;
                        } else {
                            System.out.println("chatObject");
                            ChatObject response = new ChatObject(map);
                            return (MetadataContainer) response;
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

    private CompletableFuture<MetadataContainer> completeProcessWebRequest(Map<String, Object> requestBody, String endpoint) {
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
                            ResponseObject response = new ResponseObject(inner);
                            return (MetadataContainer) response;
                        } else if (id != null && id.startsWith("resp_")) {
                            // Content is raw JSON as a string
                            String jsonContent = content
                                .replaceFirst("^```json\\s*", "")
                                .replaceFirst("\\s*```$", "")
                                .trim();
                            Map<String, Object> inner = mapper.readValue(jsonContent, new TypeReference<>() {});
                            ResponseObject response = new ResponseObject(inner);
                            return (MetadataContainer) response;
                        } else {
                            ChatObject response = new ChatObject(outer);
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
    
    private CompletableFuture<ResponseObject> completeProcessLocalWebRequest(Map<String, Object> requestBody, String endpoint) {
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
                try (CloseableHttpResponse response = client.execute(post)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    if (statusCode >= 200 && statusCode < 300) {
                        Map<String, Object> outer = mapper.readValue(responseBody, new TypeReference<>() {});
                        return new ResponseObject(outer);
                    } else {
                        throw new IOException("Unexpected response code: " + statusCode + ", body: " + responseBody);
                    }
                }
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }
    
    private CompletableFuture<Map<String, Object>> completeWebRequest(
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
