//
//  is.swift
//  
//
//  Created by Brandon Cobb on 6/4/25.
//


/* OpenAIContainer.java The purpose of this class is to interpret and
 * containerize the metadata of OpenAI's response object.
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
package com.brandongcobb.vyrtuous.utils.handlers;

import com.brandongcobb.vyrtuous.Vyrtuous;
import com.brandongcobb.metadata.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.HashMap;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import java.util.stream.Collectors;


public class ToolContainer extends MainContainer {
    
    private ToolHandler th = new ToolHandler();
    public static Map<String, Object> mapMap;
    
    private static final Logger LOGGER = Logger.getLogger(Vyrtuous.class.getName());

    private boolean isQuoted(String s) {
        return (s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"));
    }
    
    private static List<String> smartSplit(String command) {
        List<String> tokens = new ArrayList<>();
        Matcher m = Pattern.compile("\"([^\"]*)\"|'([^']*)'|\\S+").matcher(command);
        while (m.find()) {
            if (m.group(1) != null)
                tokens.add("\"" + m.group(1) + "\""); // keep double quotes
            else if (m.group(2) != null)
                tokens.add("'" + m.group(2) + "'");   // keep single quotes
            else
                tokens.add(m.group());  // unquoted token
        }
        return tokens;
    }


    public ToolContainer(Map<String, Object> responseMap) {
        
        MetadataKey<String> responsesObjectKey = new MetadataKey<>("entityType", Metadata.STRING);
        String responsesObject = responseMap != null ? (String) responseMap.get("entityType") : null;
        if (responsesObject != null) {
            put(responsesObjectKey, responsesObject);
        }

        MetadataKey<Integer> responsesCreatedAtKey = new MetadataKey<>("timestamp", Metadata.INTEGER);
        Integer responsesCreatedAt = responseMap != null ? (Integer) responseMap.get("timestamp") : null;
        if (responsesCreatedAt != null) {
            put(responsesCreatedAtKey, responsesCreatedAt);
        }

        MetadataKey<String> responsesStatusKey = new MetadataKey<>("resultStatus", Metadata.STRING);
        String responsesStatus = responseMap != null ? (String) responseMap.get("resultStatus") : null;
        if (responsesStatus != null) {
            put(responsesStatusKey, responsesStatus);
        }

//        MetadataKey<String> responsesErrorKey = new MetadataKey<>("error", Metadata.STRING);
//        String responsesError = responseMap != null ? (String) responseMap.get("error") : null;
//        if (responsesError != null) {
//            put(responsesErrorKey, responsesError);
//        }

//        MetadataKey<String> responsesIncompleteDetailsReasonKey = new MetadataKey<>("reason", Metadata.STRING);
//        Map<String, String> responsesIncompleteDetails = responseMap != null ? (Map<String, String>) responseMap.get("incomplete_details") : null;
//        String reason = (responsesIncompleteDetails != null) ? responsesIncompleteDetails.get("reason") : null;
//        if (reason != null) {
//            put(responsesIncompleteDetailsReasonKey, reason);
//        }

//        MetadataKey<String> responsesInstructionsKey = new MetadataKey<>("instructions", Metadata.STRING);
//        String responsesInstructions = responseMap != null ? (String) responseMap.get("instructions") : null;
//        if (responsesInstructions != null) {
//            put(responsesInstructionsKey, responsesInstructions);
//        }

//        MetadataKey<Integer> responsesMaxOutputTokensKey = new MetadataKey<>("max_output_tokens", Metadata.INTEGER);
//        Integer responsesMaxOutputTokens = responseMap != null ? (Integer) responseMap.get("max_output_tokens") : null;
//        if (responsesMaxOutputTokens != null) {
//            put(responsesMaxOutputTokensKey, responsesMaxOutputTokens);
//        }

        MetadataKey<String> responsesModelKey = new MetadataKey<>("modelVersion", Metadata.STRING);
        String responsesModel = responseMap != null ? (String) responseMap.get("modelVersion") : null;
        if (responsesModel != null) {
            put(responsesModelKey, responsesModel);
        }

        MetadataKey<Boolean> responsesParallelToolCallsKey = new MetadataKey<>("multipleCallsAllowed", Metadata.BOOLEAN);
        Boolean responsesParallelToolCalls = responseMap != null ? (Boolean) responseMap.get("parallel_tool_calls") : null;
        if (responsesParallelToolCalls != null) {
            put(responsesParallelToolCallsKey, responsesParallelToolCalls);
        }

//        MetadataKey<String> responsesPreviousResponseIdKey = new MetadataKey<>("previous_response_id", Metadata.STRING);
//        String responsesPreviousResponseId = responseMap != null ? (String) responseMap.get("previous_response_id") : null;
//        if (responsesPreviousResponseId != null) {
//            put(responsesPreviousResponseIdKey, responsesPreviousResponseId);
//        }

        MetadataKey<String> responsesReasoningEffortKey = new MetadataKey<>("effortLevel", Metadata.STRING);
        MetadataKey<String> responsesReasoningSummaryKey = new MetadataKey<>("summary", Metadata.STRING);
        Map<String, String> responsesReasoning = responseMap != null ? (Map<String, String>) responseMap.get("analysis") : null;
        if (responsesReasoning != null) {
            String responsesReasoningEffort = responsesReasoning.get("effortLevel");
            if (responsesReasoningEffort != null) {
                put(responsesReasoningEffortKey, responsesReasoningEffort);
            }
            String responsesReasoningSummary = responsesReasoning.get("summary");
            if (responsesReasoningSummary != null) {
                put(responsesReasoningSummaryKey, responsesReasoningSummary);
            }
        }

        MetadataKey<Double> responsesTemperatureKey = new MetadataKey<>("samplingTemperature", Metadata.DOUBLE);
        Double responsesTemperature = responseMap != null ? (Double) responseMap.get("samplingTemperature") : null;
        if (responsesTemperature != null) {
            put(responsesTemperatureKey, responsesTemperature);
        }

        MetadataKey<Map<String, Object>> responsesTextFormatKey = new MetadataKey<>("formatting", Metadata.MAP);
        Map<String, Object> responsesTextFormat = responseMap != null ? (Map<String, Object>) responseMap.get("formatting") : null;
        if (responsesTextFormat != null) {
            put(responsesTextFormatKey, responsesTextFormat);
        }

        MetadataKey<Double> responsesTopPKey = new MetadataKey<>("probabilityCutoff", Metadata.DOUBLE);
        Double responsesTopP = responseMap != null ? (Double) responseMap.get("probablitityCutoff") : null;
        if (responsesTopP != null) {
            put(responsesTopPKey, responsesTopP);
        }

        MetadataKey<String> responsesTruncationKey = new MetadataKey<>("truncationMode", Metadata.STRING);
        String responsesTruncation = responseMap != null ? (String) responseMap.get("truncationMode") : null;
        if (responsesTruncation != null) {
            put(responsesTruncationKey, responsesTruncation);
        }

        MetadataKey<Integer> responsesTotalTokensKey = new MetadataKey<>("totalTokenCount", Metadata.INTEGER);
        Map<String, Object> responsesUsage = responseMap != null ? (Map<String, Object>) responseMap.get("resourceUsage") : null;
        if (responsesUsage != null) {
            Integer responsesTotalTokens = (Integer) responsesUsage.get("totalTokenCount");
            if (responsesTotalTokens != null) {
                put(responsesTotalTokensKey, responsesTotalTokens);
            }
        }

//        MetadataKey<String> responsesUserKey = new MetadataKey<>("user", Metadata.STRING);
//        String responsesUser = responseMap != null ? (String) responseMap.get("user") : null;
//        if (responsesUser != null) {
//            put(responsesUserKey, responsesUser);
//        }

        MetadataKey<Map<String, Object>> responsesMetadataKey = new MetadataKey<>("extraMetadata", Metadata.MAP);
        Map<String, Object> responsesMetadata = responseMap != null ? (Map<String, Object>) responseMap.get("extraMetadata") : null;
        Boolean localShellFinished = (responsesMetadata != null) ? (Boolean) responsesMetadata.get("local_shell_command_sequence_finished") : null;
        
        MetadataKey<Boolean> clarificationMetadataKey = new MetadataKey<>("needsClarification", Metadata.BOOLEAN);
        Boolean needsClarification = (responsesMetadata != null) ? (Boolean) responsesMetadata.get("needsClarification") : null;
        if (responsesMetadata != null) {
            put(responsesMetadataKey, responsesMetadata);
        }
        if (localShellFinished != null) {
            put(th.LOCALSHELLTOOL_FINISHED, localShellFinished);
        }
        if (needsClarification != null) {
            put(clarificationMetadataKey, needsClarification);
        }
        MetadataKey<Boolean> acceptingTokensMetadataKey = new MetadataKey<>("acceptingTokens", Metadata.BOOLEAN);
        Boolean acceptingTokens = (responsesMetadata != null) ? (Boolean) responsesMetadata.get("acceptingTokens") : null;
        if (acceptingTokens != null) {
            put(acceptingTokensMetadataKey, acceptingTokens);
        }

        MetadataKey<String> responsesOutputContentKey = new MetadataKey<>("results", Metadata.STRING);
        Object outputObj = responseMap != null ? responseMap.get("results") : null;

        List<String> allCallIds = new ArrayList<>();
        List<List<String>> allCommands = new ArrayList<>();

        if (outputObj instanceof List<?> outputList && outputList != null) {
            for (Object outputItemObj : outputList) {
                if (!(outputItemObj instanceof Map<?, ?> outputItem) || outputItem == null) continue;

                Object entryTypeObj = outputItem.get("entryType");
                if ("local_shell".equals(entryTypeObj)) {
                    Object callIdObj = outputItem.get("entryId");
                    if (callIdObj instanceof String callId && callId != null) {
                        allCallIds.add(callId);
                    }

                    Object operationObj = outputItem.get("operation");
                    if (operationObj instanceof Map<?, ?> operation) {

                        Object commandsObj = operation.get("commands");

                        if (commandsObj instanceof List<?> outerList && !outerList.isEmpty()) {
                            Object first = outerList.get(0);

                            if (first instanceof String) {
                                for (Object obj : outerList) {
                                    if (obj instanceof String raw) {
                                        List<String> tokens = smartSplit(raw.trim());
                                        allCommands.add(tokens);
                                    } else {
                                        System.err.println("⚠ Unexpected non-string in single command list: " + obj);
                                    }
                                }

                            } else if (first instanceof List<?>) {
                                // [["git clone https://...", "cd jVyrtuous"]]
                                for (Object sub : outerList) {
                                    if (sub instanceof List<?> subList) {
                                        for (Object cmdString : subList) {
                                            if (cmdString instanceof String rawStr) {
                                                List<String> tokens = smartSplit(rawStr.trim()); // updated here too
                                                allCommands.add(tokens);
                                            } else {
                                                System.err.println("⚠️ Skipping non-string command part: " + cmdString);
                                            }
                                        }
                                    } else {
                                        System.err.println("⚠️ Skipping malformed sub-command (not a list): " + sub);
                                    }
                                }

                            } else {
                                System.err.println("⚠️ Unknown command structure: " + outerList);
                            }
                        } else {
                            System.err.println("⚠️ No commands found or commands not in list form.");
                        }
                    }
                }

                Object contentObj = outputItem.get("messages");
                if (contentObj instanceof List<?> contentList) {
                    for (Object contentItemObj : contentList) {
                        if (!(contentItemObj instanceof Map<?, ?> contentItem)) continue;
                        Object textObj = contentItem.get("messageText");
                        if (textObj instanceof String textStr && !textStr.isBlank()) {
                            put(responsesOutputContentKey, textStr);
                            break;
                        }
                    }
                }
            }
        }
        Map<String, Object> myMap = new HashMap<>();
        LOGGER.info(allCallIds.toString() + allCommands.toString());
        myMap.put(th.LOCALSHELLTOOL_CALL_IDS, allCallIds);
        myMap.put(th.LOCALSHELLTOOL_COMMANDS_LIST, allCommands);
        mapMap = myMap;

    }
    
    public Map<String, Object> getResponseMap() {
        return mapMap;
    }
}

