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

import com.brandongcobb.vyrtuous.utils.inc.*;
import com.brandongcobb.vyrtuous.records.ModelInfo;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Maps {

    public static final Map<String, String> OPENAI_ENDPOINT_URLS = Map.ofEntries(
        Map.entry("audio", "https://api.openai.com/v1/audio/speech"),
        Map.entry("batch", "https://api.openai.com/v1/audio/batches"),
        Map.entry("response", "https://api.openai.com/v1/response/completions"),
        Map.entry("embeddings", "https://api.openai.com/v1/embeddings"),
        Map.entry("files", "https://api.openai.com/v1/files"),
        Map.entry("fine-tuning", "https://api.openai.com/v1/fine_tuning/jobs"),
        Map.entry("images", "https://api.openai.com/v1/images/generations"),
        Map.entry("models", "https://api.openai.com/v1/models"),
        Map.entry("moderations", "https://api.openai.com/v1/moderations"),
        Map.entry("responses", "https://api.openai.com/v1/responses"),
        Map.entry("uploads", "https://api.openai.com/v1/uploads")
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
    public static final Map<String, ModelInfo> OPENAI_RESPONSE_MODEL_CONTEXT_LIMITS = Map.ofEntries(
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
    public static final Map<String, ModelInfo> OPENAI_RESPONSE_MODEL_OUTPUT_LIMITS = Map.ofEntries(
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

    public static final String OPENAI_RESPONSE_SYS_INPUT = "You are Codex CLI, an agentic OpenAI API implementation running on a homebrew Macbook M4. Always check if a program is installed before trying to run it.";
//    "You are Vyrtuous. This is your source" + String.join(" | ", Arrays.stream(Source.values())
//        .map(source -> source.fileContent)
//        .toArray(String[]::new));

    public static final String[] OPENAI_RESPONSE_MODELS = {"gpt-4.1", "gpt-4.1-mini", "gpt-4o", "gpt-4o-mini", "o1", "o3-mini", "o4-mini"};
}
