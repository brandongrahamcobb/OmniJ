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

import com.brandongcobb.metadata.*;
import com.brandongcobb.vyrtuous.utils.inc.*;
import com.brandongcobb.vyrtuous.Vyrtuous;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.*;
import java.util.Map;

public class Helpers {
    
    
    private static final Logger LOGGER = Logger.getLogger(Vyrtuous.class.getName());

    public static String FILE_AI_MANAGER;
    public static String FILE_APPROVAL_MODE;
    public static String FILE_CHAT_CONTAINER;
    public static String FILE_CONTEXT_ENTRY;
    public static String FILE_CONTEXT_MANAGER;
    public static String FILE_DISCORD_BOT;
    public static String FILE_EVENT_LISTENERS;
    public static String FILE_HELPERS;
    public static String FILE_HYBRID_COMMANDS;
    public static String FILE_LLAMA_CONTAINER;
    public static String FILE_LLAMA_UTILS;
    public static String FILE_OPENROUTER_UTILS;
    public static String FILE_MESSAGE_MANAGER;
    public static String FILE_MODEL_INFO;
    public static String FILE_MODEL_REGISTRY;
    public static String FILE_MODERATION_MANAGER;
    public static String FILE_PREDICATOR;
    public static String FILE_PROJECT_LOADER;
    public static String FILE_REPL_MANAGER;
    public static String FILE_SOURCE;
    public static String FILE_STRUCTURED_OUTPUT;
    public static String FILE_TOOL_HANDLER;
    public static String FILE_VYRTUOUS;

    private static String finalSchema;

    public static final Path DIR_BASE = Paths.get("/app/source").toAbsolutePath();
    public static final Path DIR_TEMP = Paths.get(DIR_BASE.toString(), "vyrtuous", "temp");
    public static final Path PATH_AI_MANAGER  = Paths.get(DIR_BASE.toString(), "vyrtuous", "utils", "handlers", "AIManager.java");
    public static final Path PATH_APPROVAL_MODE  = Paths.get(DIR_BASE.toString(), "vyrtuous", "utils", "handlers", "ApprovalMode.java");
    public static final Path PATH_CHAT_CONTAINER = Paths.get(DIR_BASE.toString(), "vyrtuous", "utils", "handlers", "ChatContainer.java");
    public static final Path PATH_COG = Paths.get(DIR_BASE.toString(), "vyrtuous", "cogs", "Cog.java");
    public static final Path PATH_CONTEXT_ENTRY  = Paths.get(DIR_BASE.toString(), "vyrtuous", "utils", "handlers", "ContextEntry.java");
    public static final Path PATH_CONTEXT_MANAGER = Paths.get(DIR_BASE.toString(), "vyrtuous", "utils", "handlers", "ContextManager.java");
    public static final Path PATH_DISCORD_BOT = Paths.get(DIR_BASE.toString(), "vyrtuous", "bots", "DiscordBot.java");
    public static final Path PATH_EVENT_LISTENERS = Paths.get(DIR_BASE.toString(), "vyrtuous", "cogs", "EventListeners.java");
    public static final Path PATH_HELPERS = Paths.get(DIR_BASE.toString(), "vyrtuous", "utils", "inc", "Helpers.java");
    public static final Path PATH_HYBRID_COMMANDS = Paths.get(DIR_BASE.toString(), "vyrtuous", "cogs", "HybridCommands.java");
    public static final Path PATH_LLAMA_CONTAINER = Paths.get(DIR_BASE.toString(), "vyrtuous", "utils", "handlers", "LlamaContainer.java");
    public static final Path PATH_LLAMA_UTILS = Paths.get(DIR_BASE.toString(), "vyrtuous", "utils", "handlers", "LlamaUtils.java");
    public static final Path PATH_MESSAGE_MANAGER = Paths.get(DIR_BASE.toString(), "vyrtuous", "utils", "handlers", "MessageManager.java");
    public static final Path PATH_MODEL_INFO = Paths.get(DIR_BASE.toString(), "vyrtuous", "records", "ModelInfo.java");
    public static final Path PATH_MODEL_REGISTRY = Paths.get(DIR_BASE.toString(), "vyrtuous", "utils", "inc", "ModelRegistry.java");
    public static final Path PATH_MODERATION_MANAGER = Paths.get(DIR_BASE.toString(), "vyrtuous", "utils", "handlers", "ModerationManager.java");
    public static final Path PATH_OPENROUTER_UTILS  = Paths.get(DIR_BASE.toString(), "vyrtuous", "utils", "handlers", "OpenRouterUtils.java");
    public static final Path PATH_PREDICATOR  = Paths.get(DIR_BASE.toString(), "vyrtuous", "utils", "handlers", "Predicator.java");
    public static final Path PATH_PROJECT_LOADER = Paths.get(DIR_BASE.toString(), "vyrtuous", "utils", "handlers", "ProjectLoader.java");
    public static final Path PATH_REPL_MANAGER = Paths.get(DIR_BASE.toString(), "vyrtuous", "utils", "handlers", "REPLManager.java");
    public static final Path PATH_SOURCE = Paths.get(DIR_BASE.toString(), "vyrtuous", "utils", "inc", "Source.java");
    
    public static final Path PATH_STRUCTURED_OUTPUT = Paths.get(DIR_BASE.toString(), "vyrtuous", "utils", "inc", "StructuredOutput.java");
    public static final Path PATH_TOOL_HANDLER = Paths.get(DIR_BASE.toString(), "vyrtuous", "utils", "handlers", "ToolHandler.java");
    public static final Path PATH_VYRTUOUS = Paths.get(DIR_BASE.toString(), "vyrtuous", "Vyrtuous.java");

    static {
        FILE_AI_MANAGER           = safeRead(EnvironmentPaths.AI_MANAGER.get());
        FILE_APPROVAL_MODE        = safeRead(EnvironmentPaths.APPROVAL_MODE.get());
        FILE_CHAT_CONTAINER       = safeRead(EnvironmentPaths.CHAT_CONTAINER.get());
        FILE_CONTEXT_ENTRY        = safeRead(EnvironmentPaths.CONTEXT_ENTRY.get());
        FILE_CONTEXT_MANAGER      = safeRead(EnvironmentPaths.CONTEXT_MANAGER.get());
        FILE_DISCORD_BOT          = safeRead(EnvironmentPaths.DISCORD_BOT.get());
        FILE_EVENT_LISTENERS      = safeRead(EnvironmentPaths.EVENT_LISTENERS.get());
        FILE_HELPERS              = safeRead(EnvironmentPaths.HELPERS.get());
        FILE_HYBRID_COMMANDS      = safeRead(EnvironmentPaths.HYBRID_COMMANDS.get());
        FILE_LLAMA_CONTAINER      = safeRead(EnvironmentPaths.LLAMA_CONTAINER.get());
        FILE_LLAMA_UTILS          = safeRead(EnvironmentPaths.LLAMA_UTILS.get());
        FILE_OPENROUTER_UTILS     = safeRead(EnvironmentPaths.OPENROUTER_UTILS.get());
        FILE_MESSAGE_MANAGER      = safeRead(EnvironmentPaths.MESSAGE_MANAGER.get());
        FILE_MODEL_INFO           = safeRead(EnvironmentPaths.MODEL_INFO.get());
        FILE_MODEL_REGISTRY       = safeRead(EnvironmentPaths.MODEL_REGISTRY.get());
        FILE_MODERATION_MANAGER   = safeRead(EnvironmentPaths.MODERATION_MANAGER.get());
        FILE_PREDICATOR           = safeRead(EnvironmentPaths.PREDICATOR.get());
        FILE_PROJECT_LOADER       = safeRead(EnvironmentPaths.PROJECT_LOADER.get());
        FILE_REPL_MANAGER         = safeRead(EnvironmentPaths.REPL_MANAGER.get());
        FILE_SOURCE               = safeRead(EnvironmentPaths.SOURCE.get());
        FILE_STRUCTURED_OUTPUT    = safeRead(EnvironmentPaths.STRUCTURED_OUTPUT.get());
        FILE_TOOL_HANDLER         = safeRead(EnvironmentPaths.TOOL_HANDLER.get());
        FILE_VYRTUOUS             = safeRead(EnvironmentPaths.VYRTUOUS.get());
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
        // List of commands considered dangerous
        List<String> dangerous = List.of("rm", "mv", "git", "patch", "shutdown", "reboot", "mvn compile");
        boolean isDangerous = dangerous.stream().anyMatch(command::contains);
        LOGGER.fine("Checked command for danger: '" + command + "' => " + isDangerous);
        return isDangerous;
    }

    public static boolean requiresApproval(String command, ApprovalMode approvalMode) {
        boolean result = switch (approvalMode) {
            case FULL_AUTO -> false; // No approval needed in full auto mode
            case EDIT_APPROVE_ALL -> true; // All commands require approval
            case EDIT_APPROVE_DESTRUCTIVE -> isDangerousCommand(command); // Only dangerous commands require approval
        };
        return result;
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
