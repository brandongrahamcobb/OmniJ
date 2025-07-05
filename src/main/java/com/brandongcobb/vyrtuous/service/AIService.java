/*  AIService.java The primary purpose of this class is to resolve
 *  AI model instructions, endpoints, request sources, request bodies and
 *  more.
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
package com.brandongcobb.vyrtuous.service;

import com.brandongcobb.metadata.*;
import com.brandongcobb.vyrtuous.Vyrtuous;
import com.brandongcobb.vyrtuous.registry.*;
import com.brandongcobb.vyrtuous.objects.*;
import com.brandongcobb.vyrtuous.records.*;
import com.brandongcobb.vyrtuous.tools.CustomTool;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;

@Service
public class AIService {
    
    private EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
    private static final RequestConfig REQUEST_CONFIG = RequestConfig.custom().setConnectTimeout(3600_000).setConnectionRequestTimeout(3600_000).setSocketTimeout(3600_000).build();
    private StringBuilder builder = new StringBuilder();
    private static final Logger LOGGER = Logger.getLogger(Vyrtuous.class.getName());
    private ChatMemory chatMemory;
    private final Map<String, CustomTool<?, ?>> tools = new ConcurrentHashMap<>();
    private ToolService toolService;
    private ModelRegistry modelRegistry = new ModelRegistry();
    
    public AIService(ChatMemory chatMemory) {
        this.chatMemory = chatMemory;
    }
    
    @Autowired
    public AIService(ChatMemory chatMemory, ToolService toolService) {
        this.chatMemory = chatMemory;
        this.toolService = toolService;
    }
    
    /*
     *  Generic
     */
    private CompletableFuture<Map<String, Object>> completeBuildRequestBody(String content, String previousResponseId, String model, String requestType, String instructions, boolean stream) {
        return completeCalculateMaxOutputTokens(model, content).thenApplyAsync(tokens -> {
            Map<String, Object> body = new HashMap<>();
            List<Map<String, Object>> messages = new ArrayList<>();
            Map<String, Object> msgMap = new HashMap<>();
            Map<String, Object> userMsg = new HashMap<>();
            Map<String, Object> systemMsg = new HashMap<>();
            List<Map<String, Object>> tools = new ArrayList<>();
            ObjectMapper objectMapper = new ObjectMapper();
            for (CustomTool<?, ?> tool : toolService.getTools()) {
                Map<String, Object> toolMap = Map.of(
                    "type", "function",
                    "function", Map.of(
                        "name", tool.getName(),
                        "description", tool.getDescription(),
                        "parameters", objectMapper.convertValue(
                            tool.getJsonSchema(),
                            new TypeReference<Map<String, Object>>() {})
                    )
                );
                tools.add(toolMap);
            }
            switch (requestType) {
                case "deprecated":
                    body.put("model", model);
                    body.put("max_completion_tokens", tokens);
                    if (System.getenv("CLI_PROVIDER").equals("null")) {
                        systemMsg.put("role", "system");
                        systemMsg.put("content", instructions);
                        messages.add(systemMsg);
                        userMsg.put("role", "user");
                        userMsg.put("content", content);
                        body.put("tools", tools);
                    } else {
                        userMsg.put("role", "user");
                        userMsg.put("content", instructions + content);
                    }
                    messages.add(userMsg);
                    body.put("messages", messages);
                    break;
                case "moderation":
                    body.put("model", model);
                    body.put("arguments", content);
                    body.put("metadata", List.of(Map.of("timestamp", LocalDateTime.now().toString())));
                    break;
                case "latest":
                    body.put("placeholder", "");
                    break;
                case "response":
                    body.put("model", model);
                    ModelInfo info = modelRegistry.RESPONSE_MODEL_CONTEXT_LIMITS.get(model);
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
                    body.put("arguments", messages);
                    body.put("stream", stream);
                    if (previousResponseId != null) {
                        body.put("previous_response_id", previousResponseId);
                    }
                    body.put("metadata", List.of(Map.of("timestamp", LocalDateTime.now().toString())));
                    body.put("tools", tools);
                    break;
                default:
                    body.put("placeholder", "");
                    break;
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
                ModelInfo outputInfo = modelRegistry.RESPONSE_MODEL_OUTPUT_LIMITS.get(model);
                long outputLimit = outputInfo != null ? outputInfo.upperLimit() : 4096;
                long tokens = Math.max(1, outputLimit - promptTokens - 20);
                if (tokens < 16) tokens = 16;
                return tokens;
            } catch (Exception e) {
                return 0L;
            }
        });
    }
    private CompletableFuture<MetadataContainer> completeGoogleRequest(String instructions, String content, String previousResponseId, String model, String requestType, String endpoint, boolean stream, Consumer<String> onContentChunk) {
        return completeBuildRequestBody(content, previousResponseId, model, requestType, instructions, stream).thenCompose(reqBody -> completeGoogleProcessRequest(reqBody, endpoint, onContentChunk));
    }
    
    private CompletableFuture<MetadataContainer> completeGoogleProcessRequest(Map<String, Object> requestBody, String endpoint, Consumer<String> onContentChunk) {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("completeGoogleProcessRequest failed: Missing GEMINI_API_KEY. Provider option `google` will not work");
        }
        return CompletableFuture.supplyAsync(() -> {
            ObjectMapper mapper = new ObjectMapper();
            try (CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(REQUEST_CONFIG).build()) {
                HttpPost post = new HttpPost(endpoint);
                post.setHeader("Authorization", "Bearer " + apiKey);
                post.setHeader("Content-Type", "application/json");
                String json = mapper.writeValueAsString(requestBody);
                post.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
                try (CloseableHttpResponse resp = client.execute(post)) {
                    int statusCode = resp.getStatusLine().getStatusCode();
                    String responseBody = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
                    LOGGER.finer(responseBody);
                    int code = resp.getStatusLine().getStatusCode();
                    if (code < 200 || code >= 300) {
                        throw new IOException("completeGoogleProcessRequest failed: HTTP: " + statusCode + ", body: " + responseBody);
                    }
                    if (onContentChunk == null) {
                        Map<String, Object> outer = mapper.readValue(responseBody, new TypeReference<>() {});
                        OpenAIContainer openaiContainer = new OpenAIContainer(outer);
                        return (MetadataContainer) openaiContainer;
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
                                Map<String, Object> choice = (Map<String, Object>) ((List<?>) chunk.get("choices")).get(0);
                                Map<String, Object> delta = (Map<String, Object>) choice.get("delta");
                                String content = (String) delta.get("content");
                                if (content != null) {
                                    onContentChunk.accept(content);
                                    builder.append(content);
                                }
                            }
                        }
                        if (lastChunk == null) {
                            throw new IllegalStateException("completeGoogleProcessRequest failed: No valid chunk received.");
                        }
                        OpenAIContainer container = new OpenAIContainer(lastChunk);
                        container.put(new MetadataKey<>("content", Metadata.STRING), builder.toString());
                        return container;
                    }
                }
            } catch (Exception e) {
                throw new CompletionException("completeGoogleProcessRequest failed: " + e.getMessage(), e);
            }
        });
    }
    
    /*
     *  llama.cpp
     */
    private CompletableFuture<MetadataContainer> completeLlamaRequest(String instructions, String content, String model, String requestType, String endpoint, boolean stream, Consumer<String> onContentChunk
    ) {
        return completeBuildRequestBody(content, null, model, requestType, instructions, stream).thenCompose(reqBody -> completeLlamaProcessRequest(reqBody, endpoint, onContentChunk));
    }
    
    private CompletableFuture<MetadataContainer> completeLlamaProcessRequest(Map<String, Object> requestBody, String endpoint, Consumer<String> onContentChunk
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(REQUEST_CONFIG).build()) {
                HttpPost post = new HttpPost(endpoint);
                post.setHeader("Content-Type", "application/json");
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(requestBody);
                post.setEntity(new StringEntity(json));
                try (CloseableHttpResponse resp = client.execute(post)) {
                    int code = resp.getStatusLine().getStatusCode();
                    String responseBody = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
                    if (code < 200 || code >= 300) {
                        throw new  IOException("HTTP " + code + ": " + responseBody);
                    }
                    if (onContentChunk == null) {
                        Map<String, Object> outer = mapper.readValue(responseBody, new TypeReference<>() {});
                        LlamaContainer llamaContainer = new LlamaContainer(outer);
                        return llamaContainer;
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
                                Map<String, Object> choice = (Map<String, Object>) ((List<?>) chunk.get("choices")).get(0);
                                Map<String, Object> delta = (Map<String, Object>) choice.get("delta");
                                String content = (String) delta.get("content");
                                if (content != null) {
                                    onContentChunk.accept(content);
                                    builder.append(content);
                                }
                            }
                        }
                        if (lastChunk == null) {
                            throw new IllegalStateException("completeLlamaProcessRequest failed: No valid chunk received.");
                        }
                        LlamaContainer container = new LlamaContainer(lastChunk);
                        container.put(new MetadataKey<>("content", Metadata.STRING), builder.toString());
                        return container;
                    }
                }
            } catch (Exception e) {
                throw new CompletionException("completeLlamaProcessRequest failed: " + e.getMessage(), e);
            }
        });
    }
    
    /*
     *  lmstudio
     */
    private CompletableFuture<MetadataContainer> completeLMStudioRequest(String instructions, String content, String model, String requestType, String endpoint, boolean stream, Consumer<String> onContentChunk) {
        return completeBuildRequestBody(content, null, model, requestType, instructions, stream).thenCompose(reqBody -> completeLMStudioProcessRequest(reqBody, endpoint, onContentChunk));
    }
    
    private CompletableFuture<MetadataContainer> completeLMStudioProcessRequest(Map<String, Object> requestBody, String endpoint, Consumer<String> onContentChunk) {
        String apiKey = System.getenv("LMSTUDIO_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("completeLMStudioProcessRequest failed: Missing LMSTUDIO_API_KEY. Provider option `lmstudio` will not work.");
        }
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(REQUEST_CONFIG).build()) {
                HttpPost post = new HttpPost(endpoint);
                post.setHeader("Authorization", "Bearer " + apiKey);
                post.setHeader("Content-Type", "application/json");
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(requestBody);
                post.setEntity(new StringEntity(json));
                try (CloseableHttpResponse resp = client.execute(post)) {
                    int code = resp.getStatusLine().getStatusCode();
                    String responseBody = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
                    if (code < 200 || code >= 300) {
                        throw new IOException("completeLMStudioProcessRequest failed: HTTP " + code + ": " + responseBody);
                    }
                    if (onContentChunk == null) {
                        Map<String, Object> outer = mapper.readValue(responseBody, new TypeReference<>() {});
                        LMStudioContainer lmstudioContainer = new LMStudioContainer(outer);
                        return lmstudioContainer;
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
                                Map<String, Object> choice = (Map<String, Object>) ((List<?>) chunk.get("choices")).get(0);
                                Map<String, Object> delta = (Map<String, Object>) choice.get("delta");
                                String content = (String) delta.get("content");
                                if (content != null) {
                                    onContentChunk.accept(content);
                                    builder.append(content);
                                }
                            }
                        }
                        if (lastChunk == null) {
                            throw new IllegalStateException("completeLMStudioProcessRequest failed: No valid chunk received.");
                        }
                        LMStudioContainer container = new LMStudioContainer(lastChunk);
                        container.put(new MetadataKey<>("content", Metadata.STRING), builder.toString());
                        return container;
                    }
                }
            } catch (Exception e) {
                throw new CompletionException("completeLMStudioProcessRequest failed: " + e.getMessage(), e);
            }
        });
    }
    
    /*
     *  Ollama
     */
    private CompletableFuture<MetadataContainer> completeOllamaRequest(String instructions, String content, String model, String requestType, String endpoint, boolean stream, Consumer<String> onContentChunk) {
        return completeBuildRequestBody(content, null, model, requestType, instructions, stream).thenCompose(reqBody -> completeLlamaProcessRequest(reqBody, endpoint, onContentChunk));
    }
    
    /*
     *  OpenAI
     */
    private CompletableFuture<MetadataContainer> completeOpenAIRequest(String instructions, String content, String previousResponseId, String model, String requestType, String endpoint, boolean stream, Consumer<String> onContentChunk) {
        return completeBuildRequestBody(content, previousResponseId, model, requestType, instructions, stream).thenCompose(reqBody -> completeOpenAIProcessRequest(reqBody, endpoint, onContentChunk));
    }
    
    private CompletableFuture<MetadataContainer> completeOpenAIProcessRequest(Map<String, Object> requestBody, String endpoint, Consumer<String> onContentChunk) {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("completeOpenAIProcessRequest failed: Missing OPENAI_API_KEY. Provider option `openai` will not work");
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
                    int code = resp.getStatusLine().getStatusCode();
                    String responseBody = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
                    if (code < 200 || code >= 300) {
                        throw new IOException("completeOpenAIProcessRequest failed: HTTP " + code + ": " + responseBody);
                    }
                    if (onContentChunk == null) {
                        String respBody = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
                        LOGGER.finer(respBody);
                        Map<String, Object> outer = mapper.readValue(responseBody, new TypeReference<>() {});
                        OpenAIContainer openaiContainer = new OpenAIContainer(outer);
                        return (MetadataContainer) openaiContainer;
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
                                Map<String, Object> choice = (Map<String, Object>) ((List<?>) chunk.get("choices")).get(0);
                                Map<String, Object> delta = (Map<String, Object>) choice.get("delta");
                                String content = (String) delta.get("content");
                                if (content != null) {
                                    onContentChunk.accept(content);
                                    builder.append(content);
                                }
                            }
                        }
                        if (lastChunk == null) {
                            throw new IllegalStateException("completeOpenAIProcessRequest failed: No valid chunk received.");
                        }
                        OpenAIContainer container = new OpenAIContainer(lastChunk);
                        container.put(new MetadataKey<>("content", Metadata.STRING), builder.toString());
                        return container;
                    }
                }
            } catch (Exception e) {
                throw new CompletionException("completeOpenAIProcessRequest failed: " + e.getMessage(), e);
            }
        });
    }
    
    /*
     *  OpenRouter
     */
    private CompletableFuture<MetadataContainer> completeOpenRouterRequest(String instructions, String content, String model, String requestType, String endpoint, boolean stream, Consumer<String> onContentChunk) {
        return completeBuildRequestBody(content, null, model, requestType, instructions, stream)
                .thenCompose(reqBody -> completeOpenRouterProcessRequest(reqBody, endpoint, onContentChunk));
    }
    
    private CompletableFuture<MetadataContainer> completeOpenRouterProcessRequest(Map<String, Object> requestBody, String endpoint, Consumer<String> onContentChunk) {
        String apiKey = System.getenv("OPENROUTER_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("completeOpenRouterProcessRequest failed: Missing OPENROUTER_API_KEY. Provider option `openrouter` will not work.");
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
                    String responseBody = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
                    if (code < 200 || code >= 300) {
                        throw new IOException("completeOpenRouterProcessRequest failed: HTTP " + code + ": " + responseBody);
                    }
                    if (onContentChunk == null) {
                        String respBody = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
                        LOGGER.finer(respBody);
                        Map<String, Object> outer = mapper.readValue(responseBody, new TypeReference<>() {});
                        OpenRouterContainer openRouterContainer = new OpenRouterContainer(outer);
                        return (MetadataContainer) openRouterContainer;
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
                                Map<String, Object> choice = (Map<String, Object>) ((List<?>) chunk.get("choices")).get(0);
                                Map<String, Object> delta = (Map<String, Object>) choice.get("delta");
                                String content = (String) delta.get("content");
                                if (content != null) {
                                    onContentChunk.accept(content);
                                    builder.append(content);
                                }
                            }
                        }
                        if (lastChunk == null) {
                            throw new IllegalStateException("completeOpenRouterProcessRequest failed: No valid chunk received.");
                        }
                        OpenRouterContainer container = new OpenRouterContainer(lastChunk);
                        container.put(new MetadataKey<>("content", Metadata.STRING), builder.toString());
                        return container;
                    }
                }
            } catch (Exception e) {
                throw new CompletionException("completeOpenRouterProcessRequest failed: " + e.getMessage(), e);
            }
        });
    }

    
    public CompletableFuture<MetadataContainer> completeRequest(String instructions, String content, String previousResponseId, String model, String requestType, String endpoint, boolean stream, Consumer<String> onContentChunk, String source
    ) throws Exception {
        if (modelRegistry.GOOGLE_ENDPOINT_URLS.containsValue(endpoint)) {
            return completeGoogleRequest(instructions, content, previousResponseId, model, requestType, endpoint, stream, onContentChunk);
        } else if (modelRegistry.LLAMA_ENDPOINT_URLS.containsValue(endpoint)) {
            return completeLlamaRequest(instructions, content, model, requestType, endpoint, stream, onContentChunk);
        } else if (modelRegistry.OLLAMA_ENDPOINT_URLS.containsValue(endpoint)) {
            return completeOllamaRequest(instructions, content, model, requestType, endpoint, stream, onContentChunk);
        } else if (modelRegistry.OPENAI_ENDPOINT_URLS.containsValue(endpoint)) {
            return completeOpenAIRequest(instructions, content, previousResponseId, model, requestType, endpoint, stream, onContentChunk);
        } else if (modelRegistry.OPENROUTER_ENDPOINT_URLS.containsValue(endpoint)) {
            return completeOpenRouterRequest(instructions, content, model, requestType, endpoint, stream, onContentChunk);
        } else {
            return CompletableFuture.failedFuture(new IllegalStateException("completeRequest failed: Invalid endpoint" + endpoint));
        }
    }
}
