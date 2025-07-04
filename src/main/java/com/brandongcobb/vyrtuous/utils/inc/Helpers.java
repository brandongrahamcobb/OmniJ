/*  Helpers.java The purpose of this program is to support the Vytuous class
 *  for any values which would make the legibility of the code worsen if it
 *  was inluded explicitly.
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

import com.brandongcobb.vyrtuous.Vyrtuous;
import com.brandongcobb.vyrtuous.enums.StructuredOutput;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Helpers {
    
    private static final Logger LOGGER = Logger.getLogger(Vyrtuous.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String FILE_AI_SERVICE;
    public static String FILE_CHAT_CONTAINER;
    public static String FILE_COG;
    public static String FILE_COUNT_FILE_LINES;
    public static String FILE_COUNT_FILE_LINES_INPUT;
    public static String FILE_CREATE_FILE;
    public static String FILE_CREATE_FILE_INPUT;
    public static String FILE_CUSTOM_TOOL;
    public static String FILE_DISCORD_BOT;
    public static String FILE_EVENT_LISTENERS;
    public static String FILE_FIND_IN_FILE;
    public static String FILE_FIND_IN_FILE_INPUT;
    public static String FILE_HELPERS;
    public static String FILE_HYBRID_COMMANDS;
    public static String FILE_LLAMA_CONTAINER;
    public static String FILE_LLAMA_UTILS;
    public static String FILE_LMSTUDIO_CONTAINER;
    public static String FILE_LMSTUDIO_UTILS;
    public static String FILE_MAIN_CONTAINER;
    public static String FILE_MARKDOWN_CONTAINER;
    public static String FILE_MARKDOWN_UTILS;
    public static String FILE_MESSAGE_SERVICE;
    public static String FILE_MODEL_INFO;
    public static String FILE_MODEL_REGISTRY;
    public static String FILE_MODERATION_SERVICE;
    public static String FILE_OLLAMA_CONTAINER;
    public static String FILE_OLLAMA_UTILS;
    public static String FILE_OPENAI_CONTAINER;
    public static String FILE_OPENAI_UTILS;
    public static String FILE_OPENROUTER_CONTAINER;
    public static String FILE_OPENROUTER_UTILS;
    public static String FILE_PATCH;
    public static String FILE_PATCH_INPUT;
    public static String FILE_PATCH_OPERATION;
    public static String FILE_READ_FILE;
    public static String FILE_READ_FILE_INPUT;
    public static String FILE_REPL_SERVICE;
    public static String FILE_SEARCH_FILES;
    public static String FILE_SEARCH_FILES_INPUT;
    public static String FILE_SEARCH_WEB;
    public static String FILE_SEARCH_WEB_INPUT;
    public static String FILE_SERVER_REQUEST;
    public static String FILE_SOURCE;
    public static String FILE_STRUCTURED_OUTPUT;
    public static String FILE_TOOL_INPUT;
    public static String FILE_TOOL_SERVICE;
    public static String FILE_TOOL_STATUS;
    public static String FILE_VYRTUOUS;


    private static String finalSchema;

    public static final Path DIR_BASE = Paths.get("/app/source").toAbsolutePath();
    public static final Path DIR_TEMP = Paths.get(DIR_BASE.toString(), "vyrtuous", "temp");
    public static final Path PATH_AI_SERVICE                = Paths.get(DIR_BASE.toString(), "vyrtuous", "service", "AIService.java");
    public static final Path PATH_CHAT_MEMORY_CONFIG        = Paths.get(DIR_BASE.toString(), "vyrtuous", "config", "ChatMemoryConfig.java");
    public static final Path PATH_COG                       = Paths.get(DIR_BASE.toString(), "vyrtuous", "cogs", "Cog.java");
    public static final Path PATH_COUNT_FILE_LINES          = Paths.get(DIR_BASE.toString(), "vyrtuous", "tools", "CountFileLines.java");
    public static final Path PATH_COUNT_FILE_LINES_INPUT    = Paths.get(DIR_BASE.toString(), "vyrtuous", "domain", "arguments", "CountFileLinesInput.java");
    public static final Path PATH_CREATE_FILE               = Paths.get(DIR_BASE.toString(), "vyrtuous", "tools", "CreateFile.java");
    public static final Path PATH_CREATE_FILE_INPUT         = Paths.get(DIR_BASE.toString(), "vyrtuous", "domain", "arguments", "CreateFileInput.java");
    public static final Path PATH_CUSTOM_TOOL               = Paths.get(DIR_BASE.toString(), "vyrtuous", "tools", "CustomTool.java");
    public static final Path PATH_DISCORD_BOT               = Paths.get(DIR_BASE.toString(), "vyrtuous", "component", "bot", "DiscordBot.java");
    public static final Path PATH_EVENT_LISTENERS           = Paths.get(DIR_BASE.toString(), "vyrtuous", "cogs", "EventListeners.java");
    public static final Path PATH_FIND_IN_FILE              = Paths.get(DIR_BASE.toString(), "vyrtuous", "tools", "FindInFile.java");
    public static final Path PATH_FIND_IN_FILE_INPUT        = Paths.get(DIR_BASE.toString(), "vyrtuous", "domain", "arguments", "FindInFileInput.java");
    public static final Path PATH_HELPERS                   = Paths.get(DIR_BASE.toString(), "vyrtuous", "utils", "inc", "Helpers.java");
    public static final Path PATH_HYBRID_COMMANDS           = Paths.get(DIR_BASE.toString(), "vyrtuous", "cogs", "HybridCommands.java");
    public static final Path PATH_LLAMA_CONTAINER           = Paths.get(DIR_BASE.toString(), "vyrtuous", "objects", "LlamaContainer.java");
    public static final Path PATH_LLAMA_UTILS               = Paths.get(DIR_BASE.toString(), "vyrtuous", "utils", "handlers", "LlamaUtils.java");
    public static final Path PATH_LMSTUDIO_CONTAINER        = Paths.get(DIR_BASE.toString(), "vyrtuous", "objects", "LMStudioContainer.java");
    public static final Path PATH_LMSTUDIO_UTILS            = Paths.get(DIR_BASE.toString(), "vyrtuous", "utils", "handlers", "LMStudioUtils.java");
    public static final Path PATH_MAIN_CONTAINER            = Paths.get(DIR_BASE.toString(), "vyrtuous", "objects", "MainContainer.java");
    public static final Path PATH_MARKDOWN_CONTAINER        = Paths.get(DIR_BASE.toString(), "vyrtuous", "objects", "MarkdownContainer.java");
    public static final Path PATH_MARKDOWN_UTILS            = Paths.get(DIR_BASE.toString(), "vyrtuous", "utils", "handlers", "MarkdownUtils.java");
    public static final Path PATH_MESSAGE_SERVICE           = Paths.get(DIR_BASE.toString(), "vyrtuous", "service", "MessageService.java");
    public static final Path PATH_MODEL_INFO                = Paths.get(DIR_BASE.toString(), "vyrtuous", "records", "ModelInfo.java");
    public static final Path PATH_MODEL_REGISTRY            = Paths.get(DIR_BASE.toString(), "vyrtuous", "utils", "inc", "ModelRegistry.java");
    public static final Path PATH_MODERATION_SERVICE        = Paths.get(DIR_BASE.toString(), "vyrtuous", "service", "ModerationService.java");
    public static final Path PATH_OLLAMA_CONTAINER          = Paths.get(DIR_BASE.toString(), "vyrtuous", "objects", "OllamaContainer.java");
    public static final Path PATH_OLLAMA_UTILS              = Paths.get(DIR_BASE.toString(), "vyrtuous", "objects", "OllamaUtils.java");
    public static final Path PATH_OPENAI_CONTAINER          = Paths.get(DIR_BASE.toString(), "vyrtuous", "objects", "OpenAIContainer.java");
    public static final Path PATH_OPENAI_UTILS              = Paths.get(DIR_BASE.toString(), "vyrtuous", "utils", "handlers", "OpenAIUtils.java");
    public static final Path PATH_OPENROUTER_CONTAINER      = Paths.get(DIR_BASE.toString(), "vyrtuous", "objects", "OpenRouterContainer.java");
    public static final Path PATH_OPENROUTER_UTILS          = Paths.get(DIR_BASE.toString(), "vyrtuous", "objects", "OpenRouterUtils.java");
    public static final Path PATH_PATCH                     = Paths.get(DIR_BASE.toString(), "vyrtuous", "tools", "Patch.java");
    public static final Path PATH_PATCH_INPUT               = Paths.get(DIR_BASE.toString(), "vyrtuous", "domain", "arguments", "PatchInput.java");
    public static final Path PATH_PATCH_OPERATION           = Paths.get(DIR_BASE.toString(), "vyrtuous", "domain", "PatchOperation.java");
    public static final Path PATH_READ_FILE                 = Paths.get(DIR_BASE.toString(), "vyrtuous", "tools", "ReadFile.java");
    public static final Path PATH_READ_FILE_INPUT           = Paths.get(DIR_BASE.toString(), "vyrtuous", "domain", "arguments", "ReadFileInput.java");
    public static final Path PATH_REPL_SERVICE              = Paths.get(DIR_BASE.toString(), "vyrtuous", "service", "REPLServicejava");
    public static final Path PATH_SEARCH_FILES              = Paths.get(DIR_BASE.toString(), "vyrtuous", "tools", "SearchFiles.java");
    public static final Path PATH_SEARCH_FILES_INPUT        = Paths.get(DIR_BASE.toString(), "vyrtuous", "domain",  "arguments", "SearchFilesInput.java");
    public static final Path PATH_SEARCH_WEB                 = Paths.get(DIR_BASE.toString(), "vyrtuous", "tools", "SearchWeb.java");
    public static final Path PATH_SEARCH_WEB_INPUT          = Paths.get(DIR_BASE.toString(), "vyrtuous", "domain",  "arguments", "SearchWebInput.java");
    public static final Path PATH_SERVER_REQUEST            = Paths.get(DIR_BASE.toString(), "vyrtuous", "objects", "ServerRequest.java");
    public static final Path PATH_SOURCE                    = Paths.get(DIR_BASE.toString(), "vyrtuous", "utils", "inc", "Source.java");
    public static final Path PATH_STRUCTURED_OUTPUT         = Paths.get(DIR_BASE.toString(), "vyrtuous", "utils", "inc", "StructuredOutput.java");
    public static final Path PATH_TOOL_CONFIG               = Paths.get(DIR_BASE.toString(), "vyrtuous", "config", "ToolConfig.java");
    public static final Path PATH_TOOL_INPUT                = Paths.get(DIR_BASE.toString(), "vyrtuous", "domain",  "arguments", "ToolInput.java");
    public static final Path PATH_TOOL_SERVICE              = Paths.get(DIR_BASE.toString(), "vyrtuous", "service", "ToolService.java");
    public static final Path PATH_TOOL_STATUS               = Paths.get(DIR_BASE.toString(), "vyrtuous", "domain", "ToolStatus.java");
    public static final Path PATH_VYRTUOUS                  = Paths.get(DIR_BASE.toString(), "vyrtuous", "Vyrtuous.java");
    
    public CompletableFuture<String> completeGetModerationSchemaNestResponse() {
        return CompletableFuture.supplyAsync(() -> {
            String baseSchema = StructuredOutput.RESPONSE.asString();
            try {
                String mergedSchemas = mergeModeration(baseSchema, StructuredOutput.MODERATION.asString());
                return mergedSchemas;
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return ioe.getMessage();
            }
        });
    }
    
    public static boolean containsString(String[] array, String target) {
        for (String item : array) {
            if (item.equals(target)) {
                return true;
            }
        }
        return false;
    }

    public static <T> T convertValue(Object value, Class<T> type) {
        if (type.isInstance(value)) {
            return (T) value;
        }
        if (type == Boolean.class) {
            if (value instanceof String) return (T) Boolean.valueOf((String) value);
        } else if (type == Integer.class) {
            if (value instanceof Number) return (T) Integer.valueOf(((Number) value).intValue());
            if (value instanceof String) return (T) Integer.valueOf(Integer.parseInt((String) value));
        } else if (type == Long.class) {
            if (value instanceof Number) return (T) Long.valueOf(((Number) value).longValue());
            if (value instanceof String) return (T) Long.valueOf(Long.parseLong((String) value));
        } else if (type == Float.class) {
            if (value instanceof Number) return (T) Float.valueOf(((Number) value).floatValue());
            if (value instanceof String) return (T) Float.valueOf(Float.parseFloat((String) value));
        } else if (type == Double.class) {
            if (value instanceof Number) return (T) Double.valueOf(((Number) value).doubleValue());
            if (value instanceof String) return (T) Double.valueOf(Double.parseDouble((String) value));
        } else if (type == String.class) {
            return (T) value.toString();
        }
        throw new IllegalArgumentException("Unsupported type conversion for: " + type.getName());
    }

    public static Map<String, Object> deepMerge(Map<String, Object> defaults, Map<String, Object> loaded) {
        Map<String, Object> merged = new HashMap<>(defaults);
        for (Map.Entry<String, Object> entry : loaded.entrySet()) {
            String key = entry.getKey();
            Object loadedVal = entry.getValue();
            if (merged.containsKey(key)) {
                Object defaultVal = merged.get(key);
                if (defaultVal instanceof Map && loadedVal instanceof Map) {
                    merged.put(key, deepMerge((Map<String, Object>) defaultVal, (Map<String, Object>) loadedVal));
                } else {
                    merged.put(key, loadedVal);
                }
            } else {
                merged.put(key, loadedVal);
            }
        }
        return merged;
    }

    public static boolean isNullOrEmpty(Object[] objects) {
        for (int i = 0; i < objects.length; i++) {
            if (objects[i] instanceof String) {
                if (objects[i] == null || ((String) objects[i]).trim().isEmpty()) {
                    return true;
                }
            } else if (objects[i] == null) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isDangerousCommand(String command) {
        if (command == null) return false;
        List<String> dangerous = List.of("rm", "mv", "git", "patch", "shutdown", "reboot", "mvn compile");
        boolean isDangerous = dangerous.stream().anyMatch(command::contains);
        LOGGER.finer("Checked command for danger: '" + command + "' => " + isDangerous);
        return isDangerous;
    }
    
    public static String loadProjectSource() {
        Path sourceRoot = Paths.get("/app/src");
        try {
            return Files.walk(sourceRoot)
                .filter(p -> !Files.isDirectory(p))
                .filter(p -> p.toString().endsWith(".java"))
                .map(p -> {
                    try {
                        return Files.readString(p);
                    } catch (IOException e) {
                        return "";
                    }
                })
                .collect(Collectors.joining("\n\n"));
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static <K, V> String mapToString(Map<K, V> map) {
        return map.entrySet().stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining(", ", "{", "}"));
    }
    
    public static String mergeModeration(String baseSchemaString, String outputItemSnippet) throws IOException {
        JsonNode baseSchema = objectMapper.readTree(baseSchemaString);
        JsonNode outputItemsNode = baseSchema.path("properties").path("output").path("items").path("properties");
        if (outputItemsNode instanceof ObjectNode) {
            JsonNode outputItemToAdd = objectMapper.readTree(outputItemSnippet);
            ((ObjectNode) outputItemsNode).setAll((ObjectNode) outputItemToAdd);
        }
        return objectMapper.writeValueAsString(baseSchema);
    }
    
    public static Long parseCommaNumber(String number) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < number.length(); i++) {
            char c = number.charAt(i);
            if (c != ',') {
                sb.append(c);
            }
        }
        String cleanedNumber = sb.toString();
        try {
            int intVal = Integer.parseInt(cleanedNumber);
            return (long) intVal;
        } catch (NumberFormatException e) {
            return Long.parseLong(cleanedNumber);
        }
    }

    private static String safeRead(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            return "";
        }
    }
}
