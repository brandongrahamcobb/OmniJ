/* ModelRegistry.java The purpose of this class is to host information for use by LLM models.
 *
 * Copyright (C) 2025  github.com/brandongrahamcobb
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.brandongcobb.vyrtuous.registry;


import com.brandongcobb.vyrtuous.enums.*;
import com.brandongcobb.vyrtuous.records.*;
import com.brandongcobb.vyrtuous.utils.inc.*;

import java.util.concurrent.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelRegistry {
    
    public static final Map<String, String> BUILD_PROTOCOL = Map.ofEntries (
        Map.entry("http://127.0.0.1:1234/v1/chat/completion", "deprecated"),
        Map.entry("http://127.0.0.1:8080/api/chat", "deprecated"),
        Map.entry("https://api.openai.com/v1/completions", "deprecated"),
        Map.entry("https://api.openai.com/v1/embeddings", "embeddings"),
        Map.entry("https://api.openai.com/v1/files", "files"),
        Map.entry("https://api.openai.com/v1/fine_tuning/jobs", "fine_tuning"),
        Map.entry("https://api.openai.com/v1/images/generations", "generations"),
        Map.entry("https://api.openai.com/v1/models", "models"),
        Map.entry("https://api.openai.com/v1/moderations", "moderations"),
        Map.entry("https://api.openai.com/v1/responses", "responses"),
        Map.entry("https://api.openai.com/v1/uploads", "uploads"),
        Map.entry("https://generativelanguage.googleapis.com/v1beta/openai/chat/completions", "deprecated"),
        Map.entry("https://openrouter.ai/api/v1/chat/completions", "deprecated")
    );
    
    /*
     *  CLI
     */
    public static final Map<String, String> CLI_INSTRUCTIONS = Map.ofEntries(
        Map.entry("lmstudio", Instructions.LMSTUDIO_TEXT_INSTRUCTIONS_CLI.asString()),
        Map.entry("llama", Instructions.LLAMA_TEXT_INSTRUCTIONS_CLI.asString()),
        Map.entry("google", Instructions.LLAMA_TEXT_INSTRUCTIONS_CLI.asString()),
        Map.entry("ollama", Instructions.OLLAMA_TEXT_INSTRUCTIONS_CLI.asString()),
        Map.entry("openai", Instructions.OPENAI_TEXT_INSTRUCTIONS_CLI.asString()),
        Map.entry("openrouter", Instructions.OPENROUTER_TEXT_INSTRUCTIONS_CLI.asString())
    );
    
    /*
     *  Discord
     */
    public static final Map<String, String> DISCORD_IMAGE_INSTRUCTIONS = Map.ofEntries(
        Map.entry("google", Instructions.GOOGLE_IMAGE_INSTRUCTIONS_DISCORD.asString()),
        Map.entry("llama", ""),
        Map.entry("openai", Instructions.OPENAI_IMAGE_INSTRUCTIONS_DISCORD.asString())
    );
    public static final Map<String, String> DISCORD_MULTIMODAL_ENDPOINTS = Map.ofEntries(
        Map.entry("latest", "http://127.0.0.1:8080/api/chat"),
        Map.entry("llama", "http://127.0.0.1:8080/api/chat"),
        Map.entry("lmstudio", "http://127.0.0.1:1234/v1/chat/completion"),
        Map.entry("openai", "https://api.openai.com/v1/completions"),
        Map.entry("openrouter", "https://openrouter.ai/api/v1/chat/completions")
    );
    public static final Map<String, String> DISCORD_TEXT_ENDPOINTS = Map.ofEntries(
        Map.entry("latest", "http://127.0.0.1:8080/api/chat"),
        Map.entry("llama", "http://127.0.0.1:8080/api/chat"),
        Map.entry("lmstudio", "http://127.0.0.1:1234/v1/chat/completion"),
        Map.entry("openai", "https://api.openai.com/v1/completions"),
        Map.entry("openrouter", "https://openrouter.ai/api/v1/chat/completions")
    );
    public static final Map<String, String> DISCORD_TEXT_INSTRUCTIONS = Map.ofEntries(
        Map.entry("google", Instructions.GOOGLE_TEXT_INSTRUCTIONS_DISCORD.asString()),
        Map.entry("lmstudio", Instructions.LMSTUDIO_TEXT_INSTRUCTIONS_DISCORD.asString()),
        Map.entry("llama", Instructions.LLAMA_TEXT_INSTRUCTIONS_DISCORD.asString()),
        Map.entry("ollama", Instructions.OLLAMA_TEXT_INSTRUCTIONS_DISCORD.asString()),
        Map.entry("openai", Instructions.OPENAI_TEXT_INSTRUCTIONS_DISCORD.asString()),
        Map.entry("openrouter", Instructions.OPENROUTER_TEXT_INSTRUCTIONS_DISCORD.asString())
    );
    
    /*
     *  google
     */
    public static final Map<String, String> GOOGLE_CLI_ENDPOINT_URLS = Map.ofEntries(
        Map.entry("deprecated", "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions")
    );
    public static final Map<String, String> GOOGLE_DISCORD_MULTIMODAL_ENDPOINT_URLS = Map.ofEntries(
        Map.entry("deprecated", "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions")
    );
    public static final Map<String, String> GOOGLE_DISCORD_TEXT_ENDPOINT_URLS = Map.ofEntries(
         Map.entry("deprecated", "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions")
    );
    public static final Map<String, String> GOOGLE_ENDPOINT_URLS = Map.ofEntries(
        Map.entry("deprecated", "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions")
    );

    /*
     *  latest
     */
    public static final Map<String, String> LATEST_CLI_ENDPOINT_URLS = Map.ofEntries(
        Map.entry("placeholder", "placeholder")
    );
    public static final Map<String, String> LATEST_DISCORD_MULTIMODAL_ENDPOINT_URLS = Map.ofEntries(
        Map.entry("placeholder", "placeholder")
    );
    public static final Map<String, String> LATEST_DISCORD_TEXT_ENDPOINT_URLS = Map.ofEntries(
         Map.entry("placeholder", "placeholder")
    );
    
    /*
     *  llama.cpp
     */
    public static final Map<String, String> LLAMA_CLI_ENDPOINT_URLS = Map.ofEntries(
        Map.entry("deprecated", "http://127.0.0.1:8080/api/chat")
    );
    public static final Map<String, String> LLAMA_DISCORD_MULTIMODAL_ENDPOINT_URLS = Map.ofEntries(
        Map.entry("deprecated", "http://127.0.0.1:8080/api/chat")
    );
    public static final Map<String, String> LLAMA_DISCORD_TEXT_ENDPOINT_URLS = Map.ofEntries(
        Map.entry("deprecated", "http://127.0.0.1:8080/api/chat")
    );
    public static final Map<String, String> LLAMA_ENDPOINT_URLS = Map.ofEntries(
        Map.entry("deprecated", "http://127.0.0.1:8080/api/chat")
    );
    public static final String[] LLAMA_MODELS = {"gemma-3-12B-it-QAT-Q4_0.gguf"}; // TODO: enable user installations of models.

    /*
     *  ollama
     */
    public static final Map<String, String> OLLAMA_ENDPOINT_URLS = Map.ofEntries(
        Map.entry("placeholder", "placeholder")
    );
    
    /*
     *  lmstudio
     */
    public static final Map<String, String> LMSTUDIO_ENDPOINT_URLS = Map.ofEntries(
        Map.entry("deprecated", "http://127.0.0.1:1234/v1/chat/completions")
    );
    
    /*
     *  openai
     */
    public static final Map<String, String> OPENAI_CLI_ENDPOINT_URLS = Map.ofEntries(
        Map.entry("completions", "https://api.openai.com/v1/completions"),
        Map.entry("responses", "https://api.openai.com/v1/responses")
    );
    public static final Map<String, String> OPENAI_DISCORD_TEXT_ENDPOINT_URLS = Map.ofEntries(
        Map.entry("completions", "https://api.openai.com/v1/completions"),
        Map.entry("images", "https://api.openai.com/v1/images/generations"),
        Map.entry("responses", "https://api.openai.com/v1/responses")
    );
    public static final Map<String, String> OPENAI_DISCORD_MULTIMODAL_ENDPOINT_URLS = Map.ofEntries(
        Map.entry("completions", "https://api.openai.com/v1/completions"),
        Map.entry("responses", "https://api.openai.com/v1/responses")
    );
    public static final Map<String, String> OPENAI_ENDPOINT_URLS = Map.ofEntries(
        Map.entry("audio", "https://api.openai.com/v1/audio/speech"),
        Map.entry("batch", "https://api.openai.com/v1/audio/batches"),
        Map.entry("completions", "https://api.openai.com/v1/completions"),
        Map.entry("embeddings", "https://api.openai.com/v1/embeddings"),
        Map.entry("files", "https://api.openai.com/v1/files"),
        Map.entry("fine-tuning", "https://api.openai.com/v1/fine_tuning/jobs"),
        Map.entry("images", "https://api.openai.com/v1/images/generations"),
        Map.entry("models", "https://api.openai.com/v1/models"),
        Map.entry("moderations", "https://api.openai.com/v1/moderations"),
        Map.entry("responses", "https://api.openai.com/v1/responses"),
        Map.entry("uploads", "https://api.openai.com/v1/uploads")
    );
    public static final Map<String, String> OPENAI_RESPONSE_HEADERS = Map.of(
        "Content-Type", "application/json",
        "OpenAI-Organization", "org-3LYwtg7DSFJ7RLn9bfk4hATf",
        "User-Agent", "brandongrahamcobb@icloud.com",
        "OpenAI-Project", "proj_u5htBCWX0LSHxkw45po1Vfz9"
    );
    public static final Map<String, ModelInfo> RESPONSE_MODEL_CONTEXT_LIMITS = Map.ofEntries(
        Map.entry("ft:gpt-4o-mini-2024-07-18:spawd:vyrtuous:AjZpTNN2", new ModelInfo(Helpers.parseCommaNumber("16,384"), false)),
        Map.entry("gpt-4.1", new ModelInfo(Helpers.parseCommaNumber("300,000"), true)),
        Map.entry("gpt-4.1-mini", new ModelInfo(Helpers.parseCommaNumber("1,047,576"), true)),
        Map.entry("gpt-4.1-nano", new ModelInfo(Helpers.parseCommaNumber("1,047,576"), true)),
        Map.entry("gpt-4o", new ModelInfo(Helpers.parseCommaNumber("128,000"), false)),
        Map.entry("gpt-4o-audio", new ModelInfo(Helpers.parseCommaNumber("128,000"), false)),
        Map.entry("gpt-4o-mini", new ModelInfo(Helpers.parseCommaNumber("128,000"), false)),
        Map.entry("o3-mini", new ModelInfo(Helpers.parseCommaNumber("200,000"), true)),
        Map.entry("o4-mini", new ModelInfo(Helpers.parseCommaNumber("200,000"), true)),
        Map.entry("codex-mini-latest", new ModelInfo(Helpers.parseCommaNumber("200,000"), true)),
        Map.entry("gemma-3-27b-it", new ModelInfo(Helpers.parseCommaNumber("32,768"), true))
    );
    
    public static final Map<String, ModelInfo> RESPONSE_MODEL_OUTPUT_LIMITS = Map.ofEntries(
        Map.entry("ft:gpt-4o-mini-2024-07-18:spawd:vyrtuous:AjZpTNN2", new ModelInfo(Helpers.parseCommaNumber("128,000"), false)),
        Map.entry("gpt-4.1", new ModelInfo(Helpers.parseCommaNumber("32,768"), true)),
        Map.entry("gpt-4.1-mini", new ModelInfo(Helpers.parseCommaNumber("32,768"), true)),
        Map.entry("gpt-4.1-nano", new ModelInfo(Helpers.parseCommaNumber("32,768"), true)),
        Map.entry("gpt-4o", new ModelInfo(Helpers.parseCommaNumber("4,096"), false)),
        Map.entry("gpt-4o-audio", new ModelInfo(Helpers.parseCommaNumber("16,384"), false)),
        Map.entry("gpt-4o-mini", new ModelInfo(Helpers.parseCommaNumber("16,384"), false)),
        Map.entry("o3-mini", new ModelInfo(Helpers.parseCommaNumber("100,000"), true)),
        Map.entry("o4-mini", new ModelInfo(Helpers.parseCommaNumber("100,000"), true)),
        Map.entry("codex-mini-latest", new ModelInfo(Helpers.parseCommaNumber("100,000"), true)),
        Map.entry("gemma-3-27b-it", new ModelInfo(Helpers.parseCommaNumber("8,192"), true))
    );

    public static final String OPENAI_RESPONSE_SYS_INPUT = "";

    public static final String[] OPENAI_RESPONSE_MODELS = {"gpt-4.1", "gpt-4.1-mini", "gpt-4o", "gpt-4o-mini", "o1", "o3-mini", "o4-mini"};
    
    /*
     *  openrouter
     */
    public static final Map<String, String> OPENROUTER_CLI_ENDPOINT_URLS = Map.ofEntries(
        Map.entry("deprecated", "https://openrouter.ai/api/v1/chat/completions")
    );
    public static final Map<String, String> OPENROUTER_DISCORD_MULTIMODAL_ENDPOINT_URLS = Map.ofEntries(
        Map.entry("deprecated", "https://openrouter.ai/api/v1/chat/completions")
    );
    public static final Map<String, String> OPENROUTER_DISCORD_TEXT_ENDPOINT_URLS = Map.ofEntries(
        Map.entry("deprecated", "https://openrouter.ai/api/v1/chat/completions")
    );
    public static final Map<String, ModelInfo> OPENROUTER_RESPONSE_MODEL_CONTEXT_LIMITS = Map.ofEntries(
        Map.entry("deepseek/deepseek-r1-0528:free", new ModelInfo(Helpers.parseCommaNumber("128,000"), true))
    );
    public static final Map<String, ModelInfo> OPENROUTER_RESPONSE_MODEL_OUTPUT_LIMITS = Map.ofEntries(
        Map.entry("deepseek/deepseek-r1-0528:free", new ModelInfo(Helpers.parseCommaNumber("128,000"), true))
    );
    public static final Map<String, String> OPENROUTER_ENDPOINT_URLS = Map.ofEntries(
        Map.entry("deprecated", "https://openrouter.ai/api/v1/chat/completions"),
        Map.entry("moderations", "https://openrouter.ai/api/v1/chat/completions"),
        Map.entry("responses", "https://openrouter.ai/api/v1/chat/completions")
    );
    /*
     *  Getters
     */
    public CompletableFuture<String> completeGetAIEndpoint(boolean multimodal, String provider, String sourceOfRequest, String requestType
    ) {
        String endpoint = null;
        if ("cli".equals(sourceOfRequest)) {
            if ("latest".equals(provider)) {
                endpoint = LATEST_CLI_ENDPOINT_URLS.get(requestType);
            } else if ("google".equals(provider)) {
                endpoint = GOOGLE_CLI_ENDPOINT_URLS.get(requestType);
            } else if ("llama".equals(provider)) {
                endpoint = LLAMA_CLI_ENDPOINT_URLS.get(requestType);
            } else if ("openai".equals(provider)) {
                endpoint = OPENAI_CLI_ENDPOINT_URLS.get(requestType);
            } else if ("openrouter".equals(provider)) {
                endpoint = OPENROUTER_CLI_ENDPOINT_URLS.get(requestType);
            }
        } else if ("discord".equals(sourceOfRequest)) {
            if ("latest".equals(provider)) {
                endpoint = multimodal
                    ? LATEST_DISCORD_MULTIMODAL_ENDPOINT_URLS.get(requestType)
                    : LATEST_DISCORD_TEXT_ENDPOINT_URLS.get(requestType);
            } else if ("google".equals(provider)) {
                endpoint = multimodal
                    ? GOOGLE_DISCORD_MULTIMODAL_ENDPOINT_URLS.get(requestType)
                    : GOOGLE_DISCORD_TEXT_ENDPOINT_URLS.get(requestType);
            } else if ("llama".equals(provider)) {
                endpoint = multimodal
                    ? LLAMA_DISCORD_MULTIMODAL_ENDPOINT_URLS.get(requestType)
                    : LLAMA_DISCORD_TEXT_ENDPOINT_URLS.get(requestType);
            } else if ("openai".equals(provider)) {
                endpoint = multimodal
                    ? OPENAI_DISCORD_MULTIMODAL_ENDPOINT_URLS.get(requestType)
                    : OPENAI_DISCORD_TEXT_ENDPOINT_URLS.get(requestType);
            } else if ("openrouter".equals(provider)) {
                endpoint = multimodal
                    ? OPENROUTER_DISCORD_MULTIMODAL_ENDPOINT_URLS.get(requestType)
                    : OPENROUTER_DISCORD_TEXT_ENDPOINT_URLS.get(requestType);
            }
        } else if ("twitch".equals(sourceOfRequest)) {
        }
        if (endpoint == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException(
                "completeGetAIEndpoint failed: Invalid combination of provider: " + provider + " and sourceOfRequest: " + sourceOfRequest));
        }
        return CompletableFuture.completedFuture(endpoint);
    }
    
    public CompletableFuture<String> completeGetInstructions(boolean multimodal, String provider, String sourceOfRequest) {
        String instructions = null;
        if ("cli".equals(sourceOfRequest)) {
            instructions = CLI_INSTRUCTIONS.get(provider);
        } else if ("discord".equals(sourceOfRequest)) {
            if (multimodal) {
                instructions = DISCORD_IMAGE_INSTRUCTIONS.get(provider);
            } else {
                instructions = DISCORD_TEXT_INSTRUCTIONS.get(provider);
            }
        }
        if (instructions == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException(
                "completeGetInstruction failed: Invalid combination of provider: " + provider + " and sourceOfRequest: " + sourceOfRequest));
        }
        return CompletableFuture.completedFuture(instructions);
    }
}
