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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.HttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.util.EntityUtils;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.CompletionException;
import java.nio.charset.StandardCharsets;
import org.apache.http.entity.ContentType;

public class AIManager {

    private String moderationApiUrl = Maps.OPENAI_ENDPOINT_URLS.get("moderations");
    private String responseApiUrl = Maps.OPENAI_ENDPOINT_URLS.get("responses");
    // Align HTTP socket timeout with our application-level 30s timeout to avoid unbounded hangs
    private static final RequestConfig REQUEST_CONFIG = RequestConfig.custom()
            .setConnectTimeout(10_000)
            .setConnectionRequestTimeout(10_000)
            .setSocketTimeout(600_000)
            .build();
    private EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
    private Map<String, Object> OPENAI_RESPONSE_FORMAT = new HashMap<>();
    private final Map<Long, ResponseObject> userResponseMap = new ConcurrentHashMap<>();

    private CompletableFuture<Map<String, Object>> completeBuildRequestBody(
        String content,
        String previousResponseId,
        String model,
        String requestType,
        String instructions
    ) {
        return completeCalculateMaxOutputTokens(model, content).thenApplyAsync(tokens -> {
            Map<String, Object> body = new HashMap<>();
            if ("completion".equals(requestType)) {
                body.put("model", ModelRegistry.GEMINI_RESPONSE_MODEL.asString());
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
            else if ("response".equals(requestType)){
                body.put("model", ModelRegistry.GEMINI_RESPONSE_MODEL.asString());
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
//                if (model == null) {
//                    String setting = ModelRegistry.OPENAI_RESPONSE_MODEL.asString();
//                    body.put("model", setting);
//                    ModelInfo info = Maps.OPENAI_RESPONSE_MODEL_CONTEXT_LIMITS.get(setting);
//                    if (info != null && info.status()) {
//                        body.put("max_output_tokens", tokens);
//                    } else {
//                        body.put("max_tokens", tokens);
//                    }
//                } else {
//                    body.put("model", model);
//                    ModelInfo info = Maps.OPENAI_RESPONSE_MODEL_CONTEXT_LIMITS.get(model);
//                    if (info != null && info.status()) {
//                        body.put("max_output_tokens", tokens);
//                    } else {
//                        body.put("max_tokens", tokens);
//                    }
//                }
//                body.put("text", Map.of("format", Maps.GEMINI_RESPONSE_FORMAT));
//                body.put("instructions", instructions);
//                List<Map<String, Object>> messages = new ArrayList<>();
//                Map<String, Object> msgMap = new HashMap<>();
//                msgMap.put("role", "user");
//                msgMap.put("content", content);
//                messages.add(msgMap);
//                body.put("input", messages);
//                if (previousResponseId != null && !previousResponseId.isEmpty()) {
//                    body.put("previous_response_id", previousResponseId);
//                }
//                if (ModelRegistry.OPENAI_RESPONSE_STORE.asBoolean()) {
//                    body.put("metadata", List.of(Map.of("timestamp", LocalDateTime.now().toString())));
//                }

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

    public CompletableFuture<ResponseObject> completeLocalRequest(String content, String previousResponseId, String model, String requestType) {
        SchemaMerger sm = new SchemaMerger();
        String endpoint = "moderation".equals(requestType)
                ? moderationApiUrl
                : responseApiUrl;

        String instructions = switch (requestType) {
            case "completion" -> "";
            case "moderation" -> ""; //TODO
            case "response" -> ModelRegistry.RESPONSE_SYS_INPUT.asString() + sm.completeGetShellToolSchemaNestResponse().join();
            default -> throw new IllegalArgumentException("Unsupported requestType: " + requestType);
        };

        return completeBuildRequestBody(content, previousResponseId, model, requestType, instructions)
                .thenCompose(reqBody -> completeProcessLocalRequest(reqBody, endpoint));
    }

    public CompletableFuture<ResponseObject> completeRequest(String content, String previousResponseId, String model, String requestType) {
        String endpoint = "moderation".equals(requestType)
                ? moderationApiUrl
                : responseApiUrl;

        String instructions = switch (requestType) {
            case "completion" -> "";
            case "moderation" -> "";
            case "response" -> ""; // or provide appropriate default
            default -> throw new IllegalArgumentException("Unsupported requestType: " + requestType);
        };

        return completeBuildRequestBody(content, previousResponseId, model, requestType, instructions)
                .thenCompose(reqBody -> completeProcessRequest(reqBody, endpoint));
    }
    
    private CompletableFuture<ResponseObject> completeProcessLocalRequest(Map<String, Object> requestBody, String endpoint) {
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
                    if (code >= 200 && code < 300) {
                        Map<String, Object> outer = mapper.readValue(respBody, new TypeReference<>() {});
                        Map<String, Object> message = (Map<String, Object>) outer.get("message");
                        String content = (String) message.get("content");
                        String jsonContent = content
                            .replaceFirst("^```json\\s*", "")
                            .replaceFirst("\\s*```$", "")
                            .trim();
                        Map<String, Object> inner = mapper.readValue(jsonContent, new TypeReference<>() {});
                        ResponseObject response = new ResponseObject(inner);
                        return response;
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

//    public CompletableFuture<Map<String, Object>> completeCreateVectorStore(List<String> fileIds) {
//        String apiKey = System.getenv("OPENAI_API_KEY");
//        if (apiKey == null || apiKey.isEmpty()) {
//            return CompletableFuture.failedFuture(new IllegalStateException("Missing OPENAI_API_KEY"));
//        }
//
//        return CompletableFuture.supplyAsync(() -> {
//            try (CloseableHttpClient client = HttpClients.custom()
//                    .setDefaultRequestConfig(REQUEST_CONFIG)
//                    .build()) {
//                HttpPost post = new HttpPost("https://api.openai.com/v1/vector_stores");
//
//                post.setHeader("Authorization", "Bearer " + apiKey);
//                post.setHeader("Content-Type", "application/json");
//
//                ObjectMapper mapper = new ObjectMapper();
//                Map<String, Object> body = Map.of("file_ids", fileIds);
//                String json = mapper.writeValueAsString(body);
//
//                post.setEntity(new StringEntity(json));
//
//                try (CloseableHttpResponse resp = client.execute(post)) {
//                    int code = resp.getStatusLine().getStatusCode();
//                    String respBody = EntityUtils.toString(resp.getEntity(), "UTF-8");
//
//                    System.out.println("📦 Vector Store Response:\n" + respBody);
//
//                    if (code >= 200 && code < 300) {
//                        return mapper.readValue(respBody, new TypeReference<Map<String, Object>>() {});
//                    } else {
//                        throw new IOException("HTTP " + code + ": " + respBody);
//                    }
//                }
//            } catch (Exception e) {
//                throw new RuntimeException("Failed to create vector store", e);
//            }
//        });
//    }

    private CompletableFuture<ResponseObject> completeProcessRequest(Map<String, Object> requestBody, String endpoint) {
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
                throw new CompletionException(e); // ensures exception is correctly wrapped for async
            }
        });
    }

    /**
     * Stub for searching a vector store or using OpenAI's built-in file_search tool.
     * Replace this stub with a call to your chosen file_search API or client helper.
     */
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


    public CompletableFuture<String> completeResolveModel(String content, Boolean multiModal, String model) {
        return CompletableFuture.supplyAsync(() -> model);
//        return completeRequest(content, null, model, "perplexity")
//            .thenCompose(resp -> resp.completeGetPerplexity())
//            .thenApply(perplexityObj -> {
//                Integer perplexity = (Integer) perplexityObj;
//                if (perplexity < 100) return "o4-mini";
//                if (perplexity > 100 && perplexity < 150 && Boolean.TRUE.equals(multiModal))
//                    return "o4-mini";
//                if (perplexity > 175 && perplexity < 200 && Boolean.TRUE.equals(multiModal))
//                    return "o4-mini";
//                return "o4-mini";
//            });
    }
}
