/*  AIManager.java The primary purpose of this class is to resolve
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
package com.brandongcobb.vyrtuous.utils.handlers;

import com.brandongcobb.metadata.*;
import com.brandongcobb.vyrtuous.Vyrtuous;
import com.brandongcobb.vyrtuous.objects.*;
import com.brandongcobb.vyrtuous.records.ModelInfo;
import com.brandongcobb.vyrtuous.utils.handlers.*;
import com.brandongcobb.vyrtuous.utils.inc.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private static final Logger LOGGER = Logger.getLogger(Vyrtuous.class.getName());

    private CompletableFuture<Map<String, Object>> completeBuildRequestBody(
        String content,
        String previousResponseId,
        String model,
        String requestType,
        String instructions,
        boolean stream
    ) {
        return completeCalculateMaxOutputTokens(model, content).thenApplyAsync(tokens -> {
            Map<String, Object> body = new HashMap<>();
            List<Map<String, Object>> messages = new ArrayList<>();
            Map<String, Object> msgMap = new HashMap<>();
            Map<String, Object> userMsg = new HashMap<>();
            Map<String, Object> systemMsg = new HashMap<>();
            switch (requestType) {
                case "deprecated":
                    body.put("model", model);
                    systemMsg.put("role", "system");
                    systemMsg.put("content", instructions);
                    userMsg.put("role", "user");
                    userMsg.put("content", content);
                    body.put("stream", stream);
                    body.put("n_predict", -1);
                    body.put("max_tokens", -1);
                    body.put("max_completion_tokens", -1);
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
                    if (previousResponseId != null) {
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

    /*
     *  llama.cpp
     */
    private CompletableFuture<MetadataContainer> completeLlamaRequest(
        String instructions,
        String content,
        String model,
        String requestType,
        String endpoint,
        boolean stream,
        Consumer<String> onContentChunk
    ) {
        return completeBuildRequestBody(content, null, model, requestType, instructions, stream)
            .thenCompose(reqBody -> completeLlamaProcessRequest(reqBody, endpoint, onContentChunk));
    }
    
    private CompletableFuture<MetadataContainer> completeLlamaProcessRequest(
        Map<String, Object> requestBody,
        String endpoint,
        Consumer<String> onContentChunk
    ) {
        return CompletableFuture.supplyAsync(() -> {
            String apiKey = System.getenv("LLAMA_API_KEY");
    //        if (apiKey == null || apiKey.isEmpty()) {
    //            return CompletableFuture.failedFuture(new IllegalStateException("Missing LMSTUDIO_API_KEY"));
    //        }
            try (CloseableHttpClient client = HttpClients.custom()
                    .setDefaultRequestConfig(REQUEST_CONFIG)
                    .build()) {
                HttpPost post = new HttpPost(endpoint);
                post.setHeader("Authorization", "Bearer " + apiKey);
                post.setHeader("Content-Type", "application/json");
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(requestBody);
                post.setEntity(new StringEntity(json));
                try (CloseableHttpResponse resp = client.execute(post)) {
                    int code = resp.getStatusLine().getStatusCode();
                    if (code < 200 || code >= 300) {
                        String errorBody = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
                        throw new IOException("HTTP " + code + ": " + errorBody);
                    }
                    if (onContentChunk == null) {
                        String respBody = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
                        LOGGER.fine(respBody);
                        Map<String, Object> outer = mapper.readValue(respBody, new TypeReference<>() {});
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
                            throw new IllegalStateException("No valid chunk received.");
                        }
                        LlamaContainer container = new LlamaContainer(lastChunk);
                        container.put(new MetadataKey<>("content", Metadata.STRING), builder.toString());
                        return container;
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
    private CompletableFuture<MetadataContainer> completeLMStudioRequest(String instructions, String content, String model, String requestType, String endpoint, boolean stream, Consumer<String> onContentChunk) {
        return completeBuildRequestBody(content, null, model, requestType, instructions, stream)
                .thenCompose(reqBody -> completeLMStudioProcessRequest(reqBody, endpoint, onContentChunk));
    }
    
    private CompletableFuture<MetadataContainer> completeLMStudioProcessRequest(Map<String, Object> requestBody, String endpoint, Consumer<String> onContentChunk) {
        String apiKey = System.getenv("LMSTUDIO_API_KEY");
//        if (apiKey == null || apiKey.isEmpty()) {
//            return CompletableFuture.failedFuture(new IllegalStateException("Missing LMSTUDIO_API_KEY"));
//        }
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = HttpClients.custom()
                    .setDefaultRequestConfig(REQUEST_CONFIG)
                    .build()) {
                HttpPost post = new HttpPost(endpoint);
                post.setHeader("Authorization", "Bearer " + apiKey);
                post.setHeader("Content-Type", "application/json");
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(requestBody);
                post.setEntity(new StringEntity(json));
                try (CloseableHttpResponse resp = client.execute(post)) {
                    int code = resp.getStatusLine().getStatusCode();
                    String respBody = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
                    if (code >= 200 && code < 300) {
                        if (onContentChunk == null) {
                            LOGGER.fine(respBody);
                            Map<String, Object> outer = mapper.readValue(respBody, new TypeReference<>() {});
                            LMStudioContainer lmStudioContainer = new LMStudioContainer(outer);
                            return (MetadataContainer) lmStudioContainer;
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
                                throw new IllegalStateException("No valid chunk received.");
                            }
                            LMStudioContainer container = new LMStudioContainer(lastChunk);
                            container.put(new MetadataKey<>("content", Metadata.STRING), builder.toString());
                            return container;
                        }
                    } else {
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
    private CompletableFuture<MetadataContainer> completeOllamaRequest(String instructions, String content, String model, String requestType, String endpoint, boolean stream, Consumer<String> onContentChunk) {
        return completeBuildRequestBody(content, null, model, requestType, instructions, stream)
            .thenCompose(reqBody -> completeLlamaProcessRequest(reqBody, endpoint, onContentChunk));
    }
    
    /*
     *  OpenAI
     */
    private CompletableFuture<MetadataContainer> completeOpenAIRequest(String instructions, String content, String previousResponseId, String model, String requestType, String endpoint, boolean stream, Consumer<String> onContentChunk) {
        return completeBuildRequestBody(content, previousResponseId, model, endpoint, instructions, stream)
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
                        
                        if (onContentChunk == null) {
                            LOGGER.fine(responseBody);
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
                                throw new IllegalStateException("No valid chunk received.");
                            }
                            OpenAIContainer container = new OpenAIContainer(lastChunk);
                            container.put(new MetadataKey<>("content", Metadata.STRING), builder.toString());
                            return container;
                        }

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
    private CompletableFuture<MetadataContainer> completeOpenRouterRequest(String instructions, String content, String model, String requestType, String endpoint, boolean stream, Consumer<String> onContentChunk) {
        return completeBuildRequestBody(content, null, model, requestType, instructions, stream)
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
                        if (onContentChunk == null) {
                            LOGGER.fine(respBody);
                            Map<String, Object> outer = mapper.readValue(respBody, new TypeReference<>() {});
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
                                throw new IllegalStateException("No valid chunk received.");
                            }
                            OpenRouterContainer container = new OpenRouterContainer(lastChunk);
                            container.put(new MetadataKey<>("content", Metadata.STRING), builder.toString());
                            return container;
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

    /*
     *  Main method
     */
    public CompletableFuture<MetadataContainer> completeRequest(String instructions, String content, String previousResponseId, String model, String requestType, String endpoint, boolean stream, Consumer<String> onContentChunk, String source
    ) throws Exception {
        if (Maps.LLAMA_ENDPOINT_URLS.containsValue(endpoint)) {
            return completeLlamaRequest(instructions, content, model, requestType, endpoint, stream, onContentChunk);
        } else if (Maps.OLLAMA_ENDPOINT_URLS.containsValue(endpoint)) {
            return completeOllamaRequest(instructions, content, model, requestType, endpoint, stream, onContentChunk);
        } else if (Maps.OPENAI_ENDPOINT_URLS.containsValue(endpoint)) {
            return completeOpenAIRequest(instructions, content, previousResponseId, model, requestType, endpoint, stream, onContentChunk);
        } else if (Maps.OPENROUTER_ENDPOINT_URLS.containsValue(endpoint)) {
            return completeOpenRouterRequest(instructions, content, model, requestType, endpoint, stream, onContentChunk);
        } else {
            return CompletableFuture.failedFuture(new IllegalStateException("Invalid endpoint."));
        }
    }

    
    public CompletableFuture<String> completeGetAIEndpoint(boolean multimodal, String provider, String sourceOfRequest, String requestType
    ) {
        String endpoint = null;
        if ("cli".equals(sourceOfRequest)) {
            if ("latest".equals(provider)) {
                endpoint = Maps.LATEST_CLI_ENDPOINT_URLS.get(requestType);
            } else if ("llama".equals(provider)) {
                endpoint = Maps.LLAMA_CLI_ENDPOINT_URLS.get(requestType);
            } else if ("openai".equals(provider)) {
                endpoint = Maps.OPENAI_CLI_ENDPOINT_URLS.get(requestType);
            }
        } else if ("discord".equals(sourceOfRequest)) {
            if ("latest".equals(provider)) {
                endpoint = multimodal
                    ? Maps.LATEST_DISCORD_MULTIMODAL_ENDPOINT_URLS.get(requestType)
                    : Maps.LATEST_DISCORD_TEXT_ENDPOINT_URLS.get(requestType);
            } else if ("llama".equals(provider)) {
                endpoint = multimodal
                    ? Maps.LLAMA_DISCORD_MULTIMODAL_ENDPOINT_URLS.get(requestType)
                    : Maps.LLAMA_DISCORD_TEXT_ENDPOINT_URLS.get(requestType);
            } else if ("openai".equals(provider)) {
                endpoint = multimodal
                    ? Maps.OPENAI_DISCORD_MULTIMODAL_ENDPOINT_URLS.get(requestType)
                    : Maps.OPENAI_DISCORD_TEXT_ENDPOINT_URLS.get(requestType);
            }
        } else if ("twitch".equals(sourceOfRequest)) {
            // Optional: Add Twitch logic here
        }
        if (endpoint == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException(
                "Invalid combination of provider: " + provider + " and sourceOfRequest: " + sourceOfRequest));
        }
        return CompletableFuture.completedFuture(endpoint);
    }
    
    public CompletableFuture<String> completeGetInstructions(boolean multimodal, String provider, String sourceOfRequest) {
        String instructions = null;
        if ("cli".equals(sourceOfRequest)) {
            instructions = Maps.CLI_INSTRUCTIONS.get(provider);
        } else if ("discord".equals(sourceOfRequest)) {
            if (multimodal) {
                instructions = Maps.DISCORD_IMAGE_INSTRUCTIONS.get(provider);
            } else {
                instructions = Maps.DISCORD_TEXT_INSTRUCTIONS.get(provider);
            }
        } else if ("twitch".equals(sourceOfRequest)) {
            instructions = Maps.TWITCH_INSTRUCTIONS.get(provider);
        }
        if (instructions == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException(
                "Invalid combination of provider: " + provider + " and sourceOfRequest: " + sourceOfRequest));
        }
        return CompletableFuture.completedFuture(instructions);
    }
}
