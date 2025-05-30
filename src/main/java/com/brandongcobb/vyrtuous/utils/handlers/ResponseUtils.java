/*  ResponseUtils.java The purpose of this class is access the response
 *  object metadata.
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
 *  aInteger with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.brandongcobb.vyrtuous.utils.handlers;

import com.brandongcobb.metadata.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ResponseUtils {
    
    private MetadataContainer container;
    
    public ResponseUtils(MetadataContainer container) {
        this.container = container;
    }
    
    /*
     *    Getters
     */
    public CompletableFuture<String> completeGetFileSearchToolType() {
        return CompletableFuture.completedFuture(this.container.get(ResponseObject.FILESEARCHTOOL_TYPE));
    }

    public CompletableFuture<List<String>> completeGetFileSearchToolVectorStoreIds() {
        return CompletableFuture.completedFuture(this.container.get(ResponseObject.FILESEARCHTOOL_VECTOR_STORE_IDS));
    }

    public CompletableFuture<String> completeGetLocalShellToolSummary() {
        MetadataKey<String> responsesReasoningSummaryKey = new MetadataKey<>("summary", Metadata.STRING);
        return CompletableFuture.completedFuture(this.container.get(responsesReasoningSummaryKey));
    }
    
    public CompletableFuture<Map<String, Object>> completeGetFileSearchToolFilters() {
        return CompletableFuture.completedFuture(this.container.get(ResponseObject.FILESEARCHTOOL_FILTERS));
    }
    
    public CompletableFuture<String> completeGetReasoning() {
        MetadataKey<String> summaryKey = new MetadataKey<>("summary", Metadata.STRING);
        return CompletableFuture.completedFuture(this.container.get(summaryKey));
    }
    
    public CompletableFuture<String> completeGetResponseMap() {
        MetadataKey<String> responseMapKey = new MetadataKey<>("response_map", Metadata.STRING);
        return CompletableFuture.completedFuture(this.container.get(responseMapKey));
    }

    public CompletableFuture<Integer> completeGetFileSearchToolMaxNumResults() {
        return CompletableFuture.completedFuture(this.container.get(ResponseObject.FILESEARCHTOOL_MAX_NUM_RESULTS));
    }

    public CompletableFuture<Map<String, Object>> completeGetFileSearchToolRankingOptions() {
        return CompletableFuture.completedFuture(this.container.get(ResponseObject.FILESEARCHTOOL_RANKING_OPTIONS));
    }

    public CompletableFuture<List<String>> completeGetShellToolCommand() {
        return CompletableFuture.completedFuture(this.container.get(ResponseObject.LOCALSHELLTOOL_COMMANDS));
    }
    
    public CompletableFuture<Boolean> completeGetShellToolFinished() {
        return CompletableFuture.completedFuture(this.container.get(ResponseObject.LOCALSHELLTOOL_FINISHED));
    }
    
    
    public CompletableFuture<Boolean> completeGetFlagged() {
        return CompletableFuture.supplyAsync(() -> {
            MetadataKey<Boolean> flaggedKey = new MetadataKey<>("flagged", Metadata.BOOLEAN);
            Object flaggedObj = this.container.get(flaggedKey);
            return flaggedObj != null && Boolean.parseBoolean(String.valueOf(flaggedObj));
        });
    }
    
    public CompletableFuture<String> completeGetToolChoice() {
        return CompletableFuture.supplyAsync(() -> {
            MetadataKey<String> toolChoiceKey = new MetadataKey<>("tool_choice", Metadata.STRING);
            Object toolChoiceObj = this.container.get(toolChoiceKey);
            return toolChoiceObj != null ? String.valueOf(toolChoiceObj) : null;
        });
    }

    public CompletableFuture<List<Map<String, Object>>> completeGetTools() {
        return CompletableFuture.supplyAsync(() -> {
            MetadataKey<List<Map<String, Object>>> toolsKey = new MetadataKey<>("tools", Metadata.LIST_MAP);
            Object toolsObj = this.container.get(toolsKey);
            if (toolsObj instanceof List) {
                return (List<Map<String, Object>>) toolsObj;
            } else {
                return Collections.emptyList();
            }
        });
    }
    
    public CompletableFuture<Map<String, Object>> completeGetToolByName(String toolName) {
        return completeGetTools().thenApply(tools -> {
            for (Map<String, Object> tool : tools) {
                Object functionObj = tool.get("function");
                if (functionObj instanceof Map) {
                    Map<String, Object> functionMap = (Map<String, Object>) functionObj;
                    if (toolName.equals(functionMap.get("name"))) {
                        return tool;
                    }
                }
            }
            return null;
        });
    }

    public CompletableFuture<Map<String, Boolean>> completeGetFlaggedReasons() {
        return CompletableFuture.supplyAsync(() -> {
            MetadataKey<Boolean>[] keys = new MetadataKey[] {
                new MetadataKey<>("sexual", Metadata.BOOLEAN),
                new MetadataKey<>("sexual/minors", Metadata.BOOLEAN),
                new MetadataKey<>("harassment", Metadata.BOOLEAN),
                new MetadataKey<>("harassment/threatening", Metadata.BOOLEAN),
                new MetadataKey<>("hate", Metadata.BOOLEAN),
                new MetadataKey<>("hate/threatening", Metadata.BOOLEAN),
                new MetadataKey<>("illicit", Metadata.BOOLEAN),
                new MetadataKey<>("illicit/violent", Metadata.BOOLEAN),
                new MetadataKey<>("self-harm", Metadata.BOOLEAN),
                new MetadataKey<>("self-harm/intent", Metadata.BOOLEAN),
                new MetadataKey<>("self-harm/instructions", Metadata.BOOLEAN),
                new MetadataKey<>("violence", Metadata.BOOLEAN),
                new MetadataKey<>("violence/graphic", Metadata.BOOLEAN)
            };
            Map<String, Boolean> reasonValues = new LinkedHashMap<>();
            for (MetadataKey<Boolean> key : keys) {
                Object value = this.container.get(key);
                reasonValues.put(key.getName(), value != null && Boolean.parseBoolean(String.valueOf(value)));
            }
            return reasonValues;
        });
    }

    public CompletableFuture<String> completeGetFormatFlaggedReasons() {
        return completeGetFlaggedReasons().thenApply(reasons -> {
            String joinedReasons = reasons.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.joining(", "));
            return "⚠️ Flagged for: " + joinedReasons;
        });
    }

    public CompletableFuture<String> completeGetResponseId() {
        return CompletableFuture.supplyAsync(() -> {
            MetadataKey<String> responseIdKey = new MetadataKey<>("id", Metadata.STRING);
            return this.container.get(responseIdKey);
        });
    }

    public CompletableFuture<String> completeGetOutput() {
        MetadataKey<String> outputKey = new MetadataKey<>("output_content", Metadata.STRING);
        return CompletableFuture.completedFuture(this.container.get(outputKey));
    }

    public CompletableFuture<Integer> completeGetPerplexity() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MetadataKey<String> outputKey = new MetadataKey<>("output_content", Metadata.STRING);
                ObjectMapper objectMapper = new ObjectMapper();
                String json = this.container.get(outputKey);
                Map<String, Integer> responseMap = objectMapper.readValue(json, new TypeReference<Map<String, Integer>>() {});
                return responseMap.get("perplexity");
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    public CompletableFuture<String> completeGetPreviousResponseId() {
        return CompletableFuture.supplyAsync(() -> {
            MetadataKey<String> previousResponseIdKey = new MetadataKey<>("previous_response_id", Metadata.STRING);
            return this.container.get(previousResponseIdKey);
        });
    }
    
    /*
     *    Setters
     */
    public CompletableFuture<Void> completeSetPreviousResponseId(String previousResponseId) {
        return CompletableFuture.runAsync(() -> {
            MetadataKey<String> previousResponseIdKey = new MetadataKey<>("previous_response_id", Metadata.STRING);
            this.container.put(previousResponseIdKey, previousResponseId);
        });
    }
}
