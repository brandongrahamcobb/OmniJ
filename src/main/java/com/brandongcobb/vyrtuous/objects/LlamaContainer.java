/*  OllamaContainer.java The purpose of this class is to extend and
 *  a MetadataContainer for ollama HTTP posts.
 *
 *  Copyright (th.C) 2025  github.com/brandongrahamcobb
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
 *  aInteger with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.brandongcobb.vyrtuous.objects;

import com.brandongcobb.metadata.Metadata;
import com.brandongcobb.metadata.MetadataKey;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

public class LlamaContainer extends MainContainer {
    
    
    public LlamaContainer(Map<String, Object> responseMap) {
        
        MetadataKey<String> idKey = new MetadataKey<>("id", Metadata.STRING);
        MetadataKey<Integer> tokenCountKey = new MetadataKey<>("token_count", Metadata.INTEGER);
        Map<String, Object> usage = (Map<String, Object>) responseMap.get("usage");
        if (usage != null && usage.get("total_tokens") instanceof Number) {
            Integer totalTokens =  ((Number) usage.get("total_tokens")).intValue();
            put(tokenCountKey, totalTokens);
        } 
        MetadataKey<String> modelKey = new MetadataKey<>("model", Metadata.STRING);
        MetadataKey<Integer> createdKey = new MetadataKey<>("created", Metadata.INTEGER);
        put(idKey, (String) responseMap.get("id"));
        put(modelKey, (String) responseMap.get("model"));
        put(createdKey, (Integer) responseMap.get("created"));
        MetadataKey<Map<String, Object>> usageKey = new MetadataKey<>("usage", Metadata.MAP);
        put(usageKey, (Map<String, Object>) responseMap.get("usage"));
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
        if (choices == null || choices.isEmpty()) return;
        Map<String, Object> choice = choices.get(0);
        Map<String, Object> message = (Map<String, Object>) choice.get("message");
        if (message != null) {
            MetadataKey<String> roleKey = new MetadataKey<>("role", Metadata.STRING);
            put(roleKey, (String) message.get("role"));
            Object contentObj = message.get("content");
            MetadataKey<String> contentKey = new MetadataKey<>("content", Metadata.STRING);
            if (contentObj instanceof String contentStr) {
                put(contentKey, contentStr);
            }
            List<Map<String, Object>> toolCalls = (List<Map<String, Object>>) message.get("tool_calls");
                if (toolCalls != null && !toolCalls.isEmpty()) {
                    for (int i = 0; i < toolCalls.size(); i++) {
                        Map<String, Object> toolCall = toolCalls.get(i);
                        if (!"function".equals(toolCall.get("type"))) continue;

                        Map<String, Object> function = (Map<String, Object>) toolCall.get("function");
                        if (function == null) continue;

                        String name = (String) function.get("name");
                        String arguments = (String) function.get("arguments");

                        // Store each tool call under its own key (indexed if multiple)
                        MetadataKey<String> functionNameKey = new MetadataKey<>("tool_call[" + i + "].name", Metadata.STRING);
                        MetadataKey<String> functionArgsKey = new MetadataKey<>("tool_call[" + i + "].arguments", Metadata.STRING);
                        put(functionNameKey, name);
                        put(functionArgsKey, arguments);

                        // Optional: parse JSON arguments to a Map
                        try {
                            Map<String, Object> parsedArgs = new ObjectMapper().readValue(arguments, new TypeReference<>() {});
                            MetadataKey<Map<String, Object>> parsedArgsKey =
                                new MetadataKey<>("tool_call[" + i + "].arguments_parsed", Metadata.MAP);
                            put(parsedArgsKey, parsedArgs);
                        } catch (Exception e) {
                            // Log or ignore parse failure
                        }
                    }
                }
        }
    }
}
