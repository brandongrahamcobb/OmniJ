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
import com.brandongcobb.vyrtuous.metadata.*;
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
import java.util.logging.Logger;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.HttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class AIManager {

    private String moderationApiUrl = Maps.OPENAI_ENDPOINT_URLS.get("moderations");
    private String responseApiUrl = Maps.OPENAI_ENDPOINT_URLS.get("responses");
    private EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
    private Map<String, Object> OPENAI_RESPONSE_FORMAT = new HashMap<>();

    private CompletableFuture<Map<String, Object>> completeBuildRequestBody(
        String content,
        String previousResponseId,
        String model,
        String requestType
    ) {
        return completeCalculateMaxOutputTokens(model, content).thenApplyAsync(tokens -> {
            Map<String, Object> body = new HashMap<>();
            if ("perplexity".equals(requestType)) {
                if (model == null) {
                    String setting = ModelRegistry.OPENAI_PERPLEXITY_MODEL.asString();
                    body.put("model", setting);
                    ModelInfo info = Maps.OPENAI_RESPONSE_MODEL_CONTEXT_LIMITS.get(setting);
                    if (info != null && info.status()) {
                        body.put("max_output_tokens", tokens);
                    } else {
                        body.put("max_tokens", tokens);
                    }
                } else {
                    ModelInfo info = Maps.OPENAI_RESPONSE_MODEL_CONTEXT_LIMITS.get(model);
                    body.put("model", model);
                    if (info != null && info.status()) {
                        body.put("max_output_tokens", tokens);
                    } else {
                        body.put("max_tokens", tokens);
                    }
                }
                body.put("text", Map.of("format", Maps.OPENAI_RESPONSE_FORMAT_PERPLEXITY));
                body.put("instructions", ModelRegistry.OPENAI_PERPLEXITY_SYS_INPUT.asString());
                List<Map<String, Object>> messages = new ArrayList<>();
                Map<String, Object> msgMap = new HashMap<>();
                msgMap.put("role", "user");
                msgMap.put("content", content);
                messages.add(msgMap);
                body.put("input", messages);
            }
            else if ("moderation".equals(requestType)) {
                body.put("input", content);
                if (model == null) {
                    body.put("model", ModelRegistry.OPENAI_MODERATION_MODEL.asString());
                }
            }
            else if ("response".equals(requestType)){
                if (model == null) {
                    String setting = ModelRegistry.OPENAI_RESPONSE_MODEL.asString();
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
                body.put("instructions", Maps.OPENAI_RESPONSE_SYS_INPUT);
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

    public CompletableFuture<ResponseObject> completeRequest(String content, String previousResponseId, String model, String requestType) {
        return completeBuildRequestBody(content, previousResponseId, model, requestType)
            .thenCompose(reqBody -> {
                String endpoint = "moderation".equals(requestType)
                                  ? moderationApiUrl
                                  : responseApiUrl;
                return completeProcessRequest(reqBody, endpoint);
              });
    }

    private CompletableFuture<ResponseObject> completeProcessRequest(Map<String, Object> requestBody, String endpoint) {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Missing OPENAI_API_KEY"));
        }
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpPost post = new HttpPost(endpoint);
                post.setHeader("Authorization", "Bearer " + apiKey);
                post.setHeader("Content-Type", "application/json");
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(requestBody);
                post.setEntity(new StringEntity(json));
                try (CloseableHttpResponse resp = client.execute(post)) {
                    int code = resp.getStatusLine().getStatusCode();
                    String respBody = EntityUtils.toString(resp.getEntity(), "UTF-8");
                    System.out.println(respBody);
                    if (code >= 200 && code < 300) {
                        Map<String, Object> respMap = mapper.readValue(respBody, new TypeReference<>() {});
                        return new ResponseObject(respMap);
                    } else {
                        throw new IOException("HTTP " + code + ": " + respBody);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
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
