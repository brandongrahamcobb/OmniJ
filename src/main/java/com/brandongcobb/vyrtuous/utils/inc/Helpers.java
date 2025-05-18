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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Helpers {

    public static String FILE_ACTIVITY_OBJECT;
    public static String FILE_AI_MANAGER;
    public static String FILE_COST_OBJECT;
    public static String FILE_DISCORD_BOT;
    public static String FILE_EVENT_LISTENERS;
    public static String FILE_HELPERS;
    public static String FILE_HYBIRD_COMMANDS;
    public static String FILE_METADATA_CONTAINER;
    public static String FILE_METADATA_HOLDER;
    public static String FILE_METADATA_KEY;
    public static String FILE_METADATA_TYPE;
    public static String FILE_MESSAGE_MANAGER;
    public static String FILE_MODEL_INFO;
    public static String FILE_MODEL_REGISTRY;
    public static String FILE_MODERATION_MANAGER;
    public static String FILE_PREDICATOR;
    public static String FILE_REQUEST_OBJECT;
    public static String FILE_RESPONSE_OBJECT;
    public static String FILE_VYRTUOUS;

    private static String finalSchema;

    public static final Path DIR_BASE = Paths.get("/app/source").toAbsolutePath();
    public static final Path DIR_TEMP = Paths.get(DIR_BASE.toString(), "vyrtuous", "temp");
    public static final Path PATH_ACTIVITY_OBJECT = Paths.get(DIR_BASE.toString(), "vyrtuous", "metadata", "Activity.java");
    public static final Path PATH_AI_MANAGER = Paths.get(DIR_BASE.toString(), "vyrtuous", "utils", "handlers", "AIManager.java");
    public static final Path PATH_COG = Paths.get(DIR_BASE.toString(), "vyrtuous", "cogs", "Cog.java");
    public static final Path PATH_COST_OBJECT = Paths.get(DIR_BASE.toString(), "vyrtuous", "metadata", "Costs.java");
    public static final Path PATH_DISCORD_BOT = Paths.get(DIR_BASE.toString(), "vyrtuous", "bots", "DiscordBot.java");
    public static final Path PATH_EVENT_LISTENERS = Paths.get(DIR_BASE.toString(), "vyrtuous", "cogs", "EventListeners.java");
    public static final Path PATH_HELPERS = Paths.get(DIR_BASE.toString(), "vyrtuous", "utils", "inc", "Helpers.java");
    public static final Path PATH_HYBIRD_COMMANDS = Paths.get(DIR_BASE.toString(), "vyrtuous", "cogs", "HybridCommands.java");
    public static final Path PATH_MESSAGE_MANAGER = Paths.get(DIR_BASE.toString(), "vyrtuous", "utils", "handlers", "MessageManager.java");
    public static final Path PATH_METADATA_CONTAINER = Paths.get(DIR_BASE.toString(), "vyrtuous", "metadata", "MetadataContainer.java");
    public static final Path PATH_METADATA_HOLDER = Paths.get(DIR_BASE.toString(), "vyrtuous", "metadata", "MetadataHolder.java");
    public static final Path PATH_METADATA_KEY = Paths.get(DIR_BASE.toString(), "vyrtuous", "metadata", "MetadataKey.java");
    public static final Path PATH_METADATA_TYPE = Paths.get(DIR_BASE.toString(), "vyrtuous", "metadata", "MetadataType.java");
    public static final Path PATH_MODEL_INFO = Paths.get(DIR_BASE.toString(), "vyrtuous", "records", "ModelInfo.java");
    public static final Path PATH_MODEL_REGISTRY = Paths.get(DIR_BASE.toString(), "vyrtuous", "utils", "inc", "ModelRegistry.java");
    public static final Path PATH_MODERATION_MANAGER = Paths.get(DIR_BASE.toString(), "vyrtuous", "utils", "handlers", "ModerationManager.java");
    public static final Path PATH_PREDICATOR = Paths.get(DIR_BASE.toString(), "vyrtuous", "utils", "handlers", "Predicator.java");
    public static final Path PATH_REQUEST_OBJECT = Paths.get(DIR_BASE.toString(), "vyrtuous", "utils", "handlers", "RequestObject.java");
    public static final Path PATH_RESPONSE_OBJECT = Paths.get(DIR_BASE.toString(), "vyrtuous", "utils", "handlers", "ResponseObject.java");
    public static final Path PATH_VYRTUOUS = Paths.get(DIR_BASE.toString(), "vyrtuous", "Vyrtuous.java");

    static {
        try {
            FILE_ACTIVITY_OBJECT  = Files.readString(EnvironmentPaths.ACTIVITY_OBJECT.get());
            FILE_AI_MANAGER  = Files.readString(EnvironmentPaths.AI_MANAGER.get());
            FILE_COST_OBJECT  = Files.readString(EnvironmentPaths.COST_OBJECT.get());
            FILE_DISCORD_BOT  = Files.readString(EnvironmentPaths.DISCORD_BOT.get());
            FILE_EVENT_LISTENERS  = Files.readString(EnvironmentPaths.EVENT_LISTENERS.get());
            FILE_HELPERS  = Files.readString(EnvironmentPaths.HELPERS.get());
            FILE_HYBIRD_COMMANDS  = Files.readString(EnvironmentPaths.HYBIRD_COMMANDS.get());
            FILE_METADATA_CONTAINER  = Files.readString(EnvironmentPaths.METADATA_CONTAINER.get());
            FILE_METADATA_HOLDER  = Files.readString(EnvironmentPaths.METADATA_HOLDER.get());
            FILE_METADATA_KEY  = Files.readString(EnvironmentPaths.METADATA_KEY.get());
            FILE_METADATA_TYPE  = Files.readString(EnvironmentPaths.METADATA_TYPE.get());
            FILE_MESSAGE_MANAGER  = Files.readString(EnvironmentPaths.MESSAGE_MANAGER.get());
            FILE_MODEL_INFO  = Files.readString(EnvironmentPaths.MODEL_INFO.get());
            FILE_MODEL_REGISTRY  = Files.readString(EnvironmentPaths.MODEL_REGISTRY.get());
            FILE_MODERATION_MANAGER  = Files.readString(EnvironmentPaths.MODERATION_MANAGER.get());
            FILE_PREDICATOR  = Files.readString(EnvironmentPaths.PREDICATOR.get());
            FILE_REQUEST_OBJECT  = Files.readString(EnvironmentPaths.REQUEST_OBJECT.get());
            FILE_RESPONSE_OBJECT  = Files.readString(EnvironmentPaths.RESPONSE_OBJECT.get());
            FILE_VYRTUOUS  = Files.readString(EnvironmentPaths.VYRTUOUS.get());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static boolean containsString(String[] array, String target) {
        for (String item : array) {
            if (item.equals(target)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("unchecked")
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
}
