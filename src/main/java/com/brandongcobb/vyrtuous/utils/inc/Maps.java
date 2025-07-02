/*  Maps.java
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
package com.brandongcobb.vyrtuous.utils.inc;

import com.brandongcobb.vyrtuous.enums.ModelRegistry;
import com.brandongcobb.vyrtuous.records.ModelInfo;
import com.brandongcobb.vyrtuous.utils.handlers.SchemaMerger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Maps {

    private static SchemaMerger sm = new SchemaMerger();
    
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
    
    public static final Map<String, String> DISCORD_TEXT_ENDPOINTS = Map.ofEntries(
        Map.entry("latest", "http://127.0.0.1:8080/api/chat"),
        Map.entry("llama", "http://127.0.0.1:8080/api/chat"),
        Map.entry("lmstudio", "http://127.0.0.1:1234/v1/chat/completion"),
        Map.entry("openai", "https://api.openai.com/v1/completions"),
        Map.entry("openrouter", "https://openrouter.ai/api/v1/chat/completions")
    );
    
    public static final Map<String, String> DISCORD_MULTIMODAL_ENDPOINTS = Map.ofEntries(
        Map.entry("latest", "http://127.0.0.1:8080/api/chat"),
        Map.entry("llama", "http://127.0.0.1:8080/api/chat"),
        Map.entry("lmstudio", "http://127.0.0.1:1234/v1/chat/completion"),
        Map.entry("openai", "https://api.openai.com/v1/completions"),
        Map.entry("openrouter", "https://openrouter.ai/api/v1/chat/completions")
    );
                                    
    public static final String[] LLAMA_MODELS = {"gemma-3-12B-it-QAT-Q4_0.gguf"}; // TODO: enable user installations of models.
    
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
        Map.entry("https://openrouter.ai/api/v1/chat/completions", "deprecated")
    );
    
    public static final Map<String, String> DISCORD_IMAGE_INSTRUCTIONS = Map.ofEntries(
                                                                                       
        Map.entry("llama", ""),
        Map.entry("openai", ModelRegistry.OPENAI_IMAGE_INSTRUCTIONS_DISCORD.asString())
    );
    
    public static final Map<String, String> DISCORD_TEXT_INSTRUCTIONS = Map.ofEntries(
        Map.entry("lmstudio", ModelRegistry.LMSTUDIO_TEXT_INSTRUCTIONS_DISCORD.asString()),
        Map.entry("llama", ModelRegistry.LLAMA_TEXT_INSTRUCTIONS_DISCORD.asString()),
        Map.entry("ollama", ModelRegistry.OLLAMA_TEXT_INSTRUCTIONS_DISCORD.asString()),
        Map.entry("openai", ModelRegistry.OPENAI_TEXT_INSTRUCTIONS_DISCORD.asString()),
        Map.entry("openrouter", ModelRegistry.OPENROUTER_TEXT_INSTRUCTIONS_DISCORD.asString())
    );
    
    public static final Map<String, String> CLI_INSTRUCTIONS = Map.ofEntries(
        Map.entry("lmstudio", ModelRegistry.LMSTUDIO_TEXT_INSTRUCTIONS_CLI.asString()),
        Map.entry("llama", ModelRegistry.LLAMA_TEXT_INSTRUCTIONS_CLI.asString()),
        Map.entry("ollama", ModelRegistry.OLLAMA_TEXT_INSTRUCTIONS_CLI.asString()),
        Map.entry("openai", ModelRegistry.OPENAI_TEXT_INSTRUCTIONS_CLI.asString()),
        Map.entry("openrouter", ModelRegistry.OPENROUTER_TEXT_INSTRUCTIONS_CLI.asString())
    );
    
    public static final Map<String, String> TWITCH_INSTRUCTIONS = Map.ofEntries(
        Map.entry("lmstudio", ModelRegistry.LMSTUDIO_TEXT_INSTRUCTIONS_TWITCH.asString()),
        Map.entry("llama", ModelRegistry.LLAMA_TEXT_INSTRUCTIONS_TWITCH.asString()),
        Map.entry("ollama", ModelRegistry.OLLAMA_TEXT_INSTRUCTIONS_TWITCH.asString()),
        Map.entry("openai", ModelRegistry.OPENAI_TEXT_INSTRUCTIONS_TWITCH.asString()),
        Map.entry("openrouter", ModelRegistry.OPENROUTER_TEXT_INSTRUCTIONS_TWITCH.asString())
    );
    
    public static final Map<String, String> LLAMA_ENDPOINT_URLS = Map.ofEntries(
        Map.entry("deprecated", "http://127.0.0.1:8080/api/chat")
    );

    public static final Map<String, String> LATEST_CLI_ENDPOINT_URLS = Map.ofEntries(
        Map.entry("placeholder", "placeholder")
    );
    
    public static final Map<String, String> LATEST_DISCORD_MULTIMODAL_ENDPOINT_URLS = Map.ofEntries(
        Map.entry("placeholder", "placeholder")
    );
    
    public static final Map<String, String> LATEST_DISCORD_TEXT_ENDPOINT_URLS = Map.ofEntries(
         Map.entry("placeholder", "placeholder")
    );

    public static final Map<String, String> LLAMA_CLI_ENDPOINT_URLS = Map.ofEntries(
        Map.entry("deprecated", "http://127.0.0.1:8080/api/chat")
    );
    
    public static final Map<String, String> LLAMA_DISCORD_MULTIMODAL_ENDPOINT_URLS = Map.ofEntries(
        Map.entry("deprecated", "http://127.0.0.1:8080/api/chat")
    );
    
    public static final Map<String, String> LLAMA_DISCORD_TEXT_ENDPOINT_URLS = Map.ofEntries(
        Map.entry("deprecated", "http://127.0.0.1:8080/api/chat")
    );

    public static final Map<String, String> OLLAMA_ENDPOINT_URLS = Map.ofEntries(
        Map.entry("placeholder", "placeholder")
    );
    
    public static final Map<String, String> LMSTUDIO_ENDPOINT_URLS = Map.ofEntries(
        Map.entry("deprecated", "http://127.0.0.1:1234/v1/chat/completions")
    );
    
    public static final Map<String, String> OPENAI_DISCORD_MULTIMODAL_URLS = Map.ofEntries(
        Map.entry("completions", "https://api.openai.com/v1/completions"),
        Map.entry("responses", "https://api.openai.com/v1/responses")
    );
    
    public static final Map<String, String> OPENAI_CLI_ENDPOINT_URLS = Map.ofEntries(
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
    
    public static final Map<String, String> OPENAI_DISCORD_TEXT_ENDPOINT_URLS = Map.ofEntries(
        Map.entry("completions", "https://api.openai.com/v1/completions"),
        Map.entry("images", "https://api.openai.com/v1/images/generations"),
        Map.entry("responses", "https://api.openai.com/v1/responses")
    );
    
    public static final Map<String, String> OPENAI_DISCORD_MULTIMODAL_ENDPOINT_URLS = Map.ofEntries(
        Map.entry("completions", "https://api.openai.com/v1/completions"),
        Map.entry("responses", "https://api.openai.com/v1/responses")
    );
    
    public static final Map<String, String> OPENROUTER_DISCORD_TEXT_ENDPOINT_URLS = Map.ofEntries(
        Map.entry("deprecated", "https://openrouter.ai/api/v1/chat/completions")
    );

    public static final Map<String, String> OPENROUTER_DISCORD_MULTIMODAL_ENDPOINT_URLS = Map.ofEntries(
        Map.entry("deprecated", "https://openrouter.ai/api/v1/chat/completions")
    );
    
    public static final Map<String, String> OPENROUTER_CLI_ENDPOINT_URLS = Map.ofEntries(
        Map.entry("deprecated", "https://openrouter.ai/api/v1/chat/completions")
    );
    
    public static Map<String, Object> createModerationSchema() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("id", Map.of("type", "string"));
        properties.put("model", Map.of("type", "string"));
        Map<String, Object> categoriesProps = new HashMap<>();
        String[] categoryKeys = {
            "sexual", "hate", "harassment", "self-harm", "sexual/minors",
            "hate/threatening", "violence/graphic", "self-harm/intent",
            "self-harm/instructions", "harassment/threatening", "violence"
        };
        for (String key : categoryKeys) {
            categoriesProps.put(key, Map.of("type", "boolean"));
        }
        Map<String, Object> categories = new HashMap<>();
        categories.put("type", "object");
        categories.put("properties", categoriesProps);
        categories.put("required", Arrays.asList(categoryKeys));
        categories.put("additionalProperties", false); // Disallow extra props here
        Map<String, Object> scoresProps = new HashMap<>();
        for (String key : categoryKeys) {
            scoresProps.put(key, Map.of("type", "number"));
        }
        Map<String, Object> categoryScores = new HashMap<>();
        categoryScores.put("type", "object");
        categoryScores.put("properties", scoresProps);
        categoryScores.put("required", Arrays.asList(categoryKeys));
        categoryScores.put("additionalProperties", false); // Disallow extra props here
        Map<String, Object> resultProps = new HashMap<>();
        resultProps.put("flagged", Map.of("type", "boolean"));
        resultProps.put("categories", categories);
        resultProps.put("category_scores", categoryScores);
        Map<String, Object> resultObject = new HashMap<>();
        resultObject.put("type", "object");
        resultObject.put("properties", resultProps);
        resultObject.put("required", List.of("flagged", "categories", "category_scores"));
        resultObject.put("additionalProperties", false);  // <-- This line is essential!
        Map<String, Object> results = new HashMap<>();
        results.put("type", "array");
        results.put("items", resultObject);
        properties.put("results", results);
        Map<String, Object> mainSchema = new HashMap<>();
        mainSchema.put("type", "object");
        mainSchema.put("properties", properties);
        mainSchema.put("required", Arrays.asList("id", "model", "results"));
        mainSchema.put("additionalProperties", false);
        Map<String, Object> format = new HashMap<>();
        format.put("type", "json_schema");
        format.put("strict", true);
        format.put("name", "moderations");
        format.put("schema", mainSchema);
        return format;
    }
    
    public static final Map<String, Object> OPENAI_MODERATION_RESPONSE_FORMAT = createModerationSchema();
    public static final Map<String, Object> OPENAI_RESPONSE_FORMAT_COLORIZE = createColorizeSchema();
    
    @SuppressWarnings("unchecked")
    public static Map<String, Object> createColorizeSchema() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("r", Map.of("type", "integer"));
        properties.put("g", Map.of("type", "integer"));
        properties.put("b", Map.of("type", "integer"));
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", List.of("r", "g", "b"));
        schema.put("additionalProperties", false);
        Map<String, Object> format = new HashMap<>();
        format.put("type", "json_schema");
        format.put("strict", true);
        format.put("schema", schema);
        format.put("name", "colorize");
        return format;
    }
    
    public static final Map<String, Object> GEMINI_RESPONSE_FORMAT = createResponsesApiSchema();

    @SuppressWarnings("unchecked")
    public static Map<String, Object> createResponsesApiSchema() {
        Map<String, Object> rootProperties = new HashMap<>();
        rootProperties.put("id", Map.of("type", "string"));
        rootProperties.put("object", Map.of("type", "string"));
        rootProperties.put("created_at", Map.of("type", "integer"));
        rootProperties.put("status", Map.of("type", "string"));
        rootProperties.put("error", Map.of("type", List.of("null", "object"))); // can be null or object
        rootProperties.put("incomplete_details", Map.of("type", List.of("null", "object")));
        rootProperties.put("instructions", Map.of("type", List.of("null", "object")));
        rootProperties.put("max_output_tokens", Map.of("type", List.of("null", "integer")));
        rootProperties.put("model", Map.of("type", "string"));
        rootProperties.put("parallel_tool_calls", Map.of("type", "boolean"));
        rootProperties.put("previous_response_id", Map.of("type", List.of("null", "string")));
        rootProperties.put("store", Map.of("type", "boolean"));
        rootProperties.put("temperature", Map.of("type", "number"));
        rootProperties.put("tool_choice", Map.of("type", "string"));
        rootProperties.put("top_p", Map.of("type", "number"));
        rootProperties.put("truncation", Map.of("type", "string"));
        rootProperties.put("user", Map.of("type", List.of("null", "object")));
        rootProperties.put("metadata", Map.of("type", "object"));
        Map<String, Object> reasoningProperties = new HashMap<>();
        reasoningProperties.put("effort", Map.of("type", List.of("null", "string")));
        reasoningProperties.put("summary", Map.of("type", List.of("null", "string")));
        Map<String, Object> reasoningSchema = new HashMap<>();
        reasoningSchema.put("type", "object");
        reasoningSchema.put("properties", reasoningProperties);
        reasoningSchema.put("required", List.of("effort", "summary"));
        reasoningSchema.put("additionalProperties", false);
        rootProperties.put("reasoning", reasoningSchema);
        Map<String, Object> textFormatProperties = new HashMap<>();
        textFormatProperties.put("type", "string");
        Map<String, Object> textFormatSchema = new HashMap<>();
        textFormatSchema.put("type", "object");
        textFormatSchema.put("properties", textFormatProperties);
        textFormatSchema.put("required", List.of("type"));
        textFormatSchema.put("additionalProperties", false);
        Map<String, Object> textProperties = new HashMap<>();
        textProperties.put("format", textFormatSchema);
        Map<String, Object> textSchema = new HashMap<>();
        textSchema.put("type", "object");
        textSchema.put("properties", textProperties);
        textSchema.put("required", List.of("format"));
        textSchema.put("additionalProperties", false);
        rootProperties.put("text", textSchema);
        Map<String, Object> inputTokensDetailsProperties = Map.of(
            "cached_tokens", Map.of("type", "integer")
        );
        Map<String, Object> inputTokensDetailsSchema = Map.of(
            "type", "object",
            "properties", inputTokensDetailsProperties,
            "required", List.of("cached_tokens"),
            "additionalProperties", false
        );
        Map<String, Object> outputTokensDetailsProperties = Map.of(
            "reasoning_tokens", Map.of("type", "integer")
        );
        Map<String, Object> outputTokensDetailsSchema = Map.of(
            "type", "object",
            "properties", outputTokensDetailsProperties,
            "required", List.of("reasoning_tokens"),
            "additionalProperties", false
        );
        Map<String, Object> usageProperties = new HashMap<>();
        usageProperties.put("input_tokens", Map.of("type", "integer"));
        usageProperties.put("input_tokens_details", inputTokensDetailsSchema);
        usageProperties.put("output_tokens", Map.of("type", "integer"));
        usageProperties.put("output_tokens_details", outputTokensDetailsSchema);
        usageProperties.put("total_tokens", Map.of("type", "integer"));
        Map<String, Object> usageSchema = new HashMap<>();
        usageSchema.put("type", "object");
        usageSchema.put("properties", usageProperties);
        usageSchema.put("required", List.of(
            "input_tokens", "input_tokens_details", "output_tokens", "output_tokens_details", "total_tokens"
        ));
        usageSchema.put("additionalProperties", false);
        rootProperties.put("usage", usageSchema);
        Map<String, Object> toolsSchema = Map.of(
            "type", "array",
            "items", Map.of("type", "object")
        );
        rootProperties.put("tools", toolsSchema);
        Map<String, Object> outputTextContentProperties = new HashMap<>();
        outputTextContentProperties.put("type", Map.of("type", "string"));
        outputTextContentProperties.put("text", Map.of("type", "string"));
        outputTextContentProperties.put("annotations", Map.of("type", "array"));
        Map<String, Object> outputTextContentSchema = new HashMap<>();
        outputTextContentSchema.put("type", "object");
        outputTextContentSchema.put("properties", outputTextContentProperties);
        outputTextContentSchema.put("required", List.of("type", "text", "annotations"));
        outputTextContentSchema.put("additionalProperties", false);
        Map<String, Object> messageContentArraySchema = Map.of(
            "type", "array",
            "items", outputTextContentSchema
        );
        Map<String, Object> messageProperties = new HashMap<>();
        messageProperties.put("type", Map.of("type", "string"));
        messageProperties.put("id", Map.of("type", "string"));
        messageProperties.put("status", Map.of("type", "string"));
        messageProperties.put("role", Map.of("type", "string"));
        messageProperties.put("content", messageContentArraySchema);
        Map<String, Object> messageSchema = new HashMap<>();
        messageSchema.put("type", "object");
        messageSchema.put("properties", messageProperties);
        messageSchema.put("required", List.of("type", "id", "status", "role", "content"));
        messageSchema.put("additionalProperties", false);
        Map<String, Object> outputSchema = Map.of(
            "type", "array",
            "items", messageSchema
        );
        rootProperties.put("output", outputSchema);
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties", rootProperties);
        schema.put("required", List.of(
            "id", "object", "created_at", "status", "model", "output", "parallel_tool_calls", "store",
            "temperature", "tool_choice", "top_p", "truncation", "usage", "tools", "text", "reasoning", "metadata"
        ));
        schema.put("additionalProperties", false);
        Map<String, Object> format = new HashMap<>();
        format.put("type", "json_schema");
        format.put("strict", true);
        format.put("schema", schema);
        format.put("name", "responses_api");
        return format;
    }

    public static final Map<String, Object> OPENAI_RESPONSE_FORMAT_PERPLEXITY = createPerplexitySchema();
    @SuppressWarnings("unchecked")
    public static Map<String, Object> createPerplexitySchema() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("perplexity", Map.of("type", "integer"));
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", List.of("perplexity"));
        schema.put("additionalProperties", false);
        Map<String, Object> format = new HashMap<>();
        format.put("type", "json_schema");
        format.put("strict", true);
        format.put("schema", schema);
        format.put("name", "perplexity");
        return format;
    }
    
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
        Map.entry("codex-mini-latest", new ModelInfo(Helpers.parseCommaNumber("200,000"), true))
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
        Map.entry("codex-mini-latest", new ModelInfo(Helpers.parseCommaNumber("100,000"), true))
    );

    public static final String OPENAI_RESPONSE_SYS_INPUT = "";

    public static final String[] OPENAI_RESPONSE_MODELS = {"gpt-4.1", "gpt-4.1-mini", "gpt-4o", "gpt-4o-mini", "o1", "o3-mini", "o4-mini"};
}
