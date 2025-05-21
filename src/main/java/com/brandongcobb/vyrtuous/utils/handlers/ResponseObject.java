/* ResponseObject.java The purpose of this class is to interpret and
 * containerize the metadata of OpenAI's response object.
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.HashMap;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

public class ResponseObject extends MetadataContainer {

    private static final MetadataType<List<String>> LIST = new MetadataList(Metadata.STRING);
    public static final MetadataKey<String> FILESEARCHTOOL_TYPE = new MetadataKey<>("filesearchtool_type", Metadata.STRING);
    public static final MetadataKey<List<String>> FILESEARCHTOOL_VECTOR_STORE_IDS = new MetadataKey<>("filesearchtool_vector_store_ids", Metadata.LIST_STRING);
    public static final MetadataKey<Map<String, Object>> FILESEARCHTOOL_FILTERS = new MetadataKey<>("filesearchtool_filters", Metadata.MAP);
    public static final MetadataKey<Integer> FILESEARCHTOOL_MAX_NUM_RESULTS = new MetadataKey<>("filesearchtool_max_num_results", Metadata.INTEGER);
    public static final MetadataKey<Map<String, Object>> FILESEARCHTOOL_RANKING_OPTIONS = new MetadataKey<>("filesearchtool_ranking_options", Metadata.MAP);

    public ResponseObject(Map<String, Object> responseMap) {
        MetadataKey<String> idKey = new MetadataKey<>("id", Metadata.STRING);
        String requestId = (String) responseMap.get("id");
        put(idKey, requestId);
        if (requestId.startsWith("responsecmpl")) {
            MetadataKey<String> objectKey = new MetadataKey<>("object", Metadata.STRING);
            String requestObject = (String) responseMap.get("object");
            if (requestObject == null) {
                throw new NullPointerException("The response map is missing the mandatory 'object' field.");
            }
            put(objectKey, requestObject);
            MetadataKey<Integer> completionCreatedKey = new MetadataKey<>("created", Metadata.INTEGER);
            Integer completionCreated = (Integer) responseMap.get("created");
            put(completionCreatedKey, completionCreated);
            MetadataKey<String> completionModelKey = new MetadataKey<>("model", Metadata.STRING);
            String completionModel = (String) responseMap.get("model");
            put(completionModelKey, completionModel);
            List<Map<String, Object>> completionChoices = (List<Map<String, Object>>) responseMap.get("choices");
            if (completionChoices != null && !completionChoices.isEmpty()) {
                Map<String, Object> completionChoice = completionChoices.get(0);
                Map<String, String> completionMessage = (Map<String, String>) completionChoice.get("message");
                MetadataKey<String> completionRoleKey = new MetadataKey<>("role", Metadata.STRING);
                String completionRole = completionMessage != null ? completionMessage.get("role") : null;
                put(completionRoleKey, completionRole);
                MetadataKey<String> completionContentKey = new MetadataKey<>("content", Metadata.STRING);
                String completionContent = completionMessage != null ? completionMessage.get("content") : null;
                put(completionContentKey, completionContent);
                MetadataKey<String> completionFinishReasonKey = new MetadataKey<>("finish_reason", Metadata.STRING);
                String completionFinishReason = (String) completionChoice.get("finish_reason");
                put(completionFinishReasonKey, completionFinishReason);
                MetadataKey<Integer> completionIndexKey = new MetadataKey<>("index", Metadata.INTEGER);
                Integer completionIndex = (Integer) completionChoice.get("index");
                put(completionIndexKey, completionIndex);
            }
            Map<String, Integer> completionUsage = (Map<String, Integer>) responseMap.get("usage");
            if (completionUsage != null) {
                MetadataKey<Integer> completionTotalTokensKey = new MetadataKey<>("total_tokens", Metadata.INTEGER);
                Integer completionTotalTokens = completionUsage.get("total_tokens");
                put(completionTotalTokensKey, completionTotalTokens);
                MetadataKey<Integer> completionPromptTokensKey = new MetadataKey<>("prompt_tokens", Metadata.INTEGER);
                Integer completionPromptTokens = completionUsage.get("prompt_tokens");
                put(completionPromptTokensKey, completionPromptTokens);
                MetadataKey<Integer> completionCompletionTokensKey = new MetadataKey<>("completion_tokens", Metadata.INTEGER);
                Integer completionCompletionTokens = completionUsage.get("completion_tokens");
                put(completionCompletionTokensKey, completionCompletionTokens);
            }
        }

        else if (requestId.startsWith("models")) {
            MetadataKey<String> objectKey = new MetadataKey<>("object", Metadata.STRING);
            String requestObject = (String) responseMap.get("object");
            if (requestObject == null) {
                throw new NullPointerException("The response map is missing the mandatory 'object' field.");
            }
            put(objectKey, requestObject);
            MetadataKey<Integer> modelCreatedKey = new MetadataKey<>("created", Metadata.INTEGER);
            Integer modelCreated = (Integer) responseMap.get("created");
            put(modelCreatedKey, modelCreated);
            MetadataKey<String> ownerCreatedKey = new MetadataKey<>("owned_by", Metadata.STRING);
            String ownerCreated = (String) responseMap.get("owned_by");
            put(ownerCreatedKey, ownerCreated);
        }

        else if (requestId.startsWith("modr")) {
            MetadataKey<Integer> moderationCreatedKey = new MetadataKey<>("created", Metadata.INTEGER);
            Integer moderationCreated = (Integer) responseMap.get("created");
            put(moderationCreatedKey, moderationCreated);
            MetadataKey<String> moderationModelKey = new MetadataKey<>("model", Metadata.STRING);
            String moderationModel = (String) responseMap.get("model");
            put(moderationModelKey, moderationModel);
            List<Map<String, Object>> results = (List<Map<String, Object>>) responseMap.get("results");
            if (results != null && !results.isEmpty()) {
                Map<String, Object> result = results.get(0);
                MetadataKey<Boolean> flaggedKey = new MetadataKey<>("flagged", Metadata.BOOLEAN);
                Boolean moderationFlagged = (Boolean) result.get("flagged");
                put(flaggedKey, moderationFlagged);
                Map<String, Boolean> categories = (Map<String, Boolean>) result.get("categories");
                if (categories != null) {
                    MetadataKey<Boolean> sexualKey = new MetadataKey<>("sexual", Metadata.BOOLEAN);
                    Boolean moderationSexual = categories.get("sexual");
                    put(sexualKey, moderationSexual);
                    MetadataKey<Boolean> sexualMinorsKey = new MetadataKey<>("sexual/minors", Metadata.BOOLEAN);
                    Boolean moderationSexualMinors = categories.get("sexual/minors");
                    put(sexualMinorsKey, moderationSexualMinors);
                    MetadataKey<Boolean> harassmentKey = new MetadataKey<>("harassment", Metadata.BOOLEAN);
                    Boolean moderationHarassment = categories.get("harassment");
                    put(harassmentKey, moderationHarassment);
                    MetadataKey<Boolean> harassmentThreateningKey = new MetadataKey<>("harassment/threatening", Metadata.BOOLEAN);
                    Boolean moderationHarassmentThreatening = categories.get("harassment/threatening");
                    put(harassmentThreateningKey, moderationHarassmentThreatening);
                    MetadataKey<Boolean> hateKey = new MetadataKey<>("hate", Metadata.BOOLEAN);
                    Boolean moderationHate = categories.get("hate");
                    put(hateKey, moderationHate);
                    MetadataKey<Boolean> hateThreateningKey = new MetadataKey<>("hate/threatening", Metadata.BOOLEAN);
                    Boolean moderationHateThreatening = categories.get("hate/threatening");
                    put(hateThreateningKey, moderationHateThreatening);
                    MetadataKey<Boolean> illicitKey = new MetadataKey<>("illicit", Metadata.BOOLEAN);
                    Boolean moderationIllicit = categories.get("illicit");
                    put(illicitKey, moderationIllicit);
                    // Note: watch out for extra spaces in key names
                    MetadataKey<Boolean> illicitViolentKey = new MetadataKey<>("illicit/violent", Metadata.BOOLEAN);
                    Boolean moderationIllicitViolent = categories.get("illicit/violent");
                    put(illicitViolentKey, moderationIllicitViolent);
                    MetadataKey<Boolean> selfHarmKey = new MetadataKey<>("self-harm", Metadata.BOOLEAN);
                    Boolean moderationSelfHarm = categories.get("self-harm");
                    put(selfHarmKey, moderationSelfHarm);
                    MetadataKey<Boolean> selfHarmIntentKey = new MetadataKey<>("self-harm/intent", Metadata.BOOLEAN);
                    Boolean moderationSelfHarmIntent = categories.get("self-harm/intent");
                    put(selfHarmIntentKey, moderationSelfHarmIntent);
                    MetadataKey<Boolean> selfHarmInstructionsKey = new MetadataKey<>("self-harm/instructions", Metadata.BOOLEAN);
                    Boolean moderationSelfHarmInstructions = categories.get("self-harm/instructions");
                    put(selfHarmInstructionsKey, moderationSelfHarmInstructions);
                    MetadataKey<Boolean> violenceKey = new MetadataKey<>("violence", Metadata.BOOLEAN);
                    Boolean moderationViolence = categories.get("violence");
                    put(violenceKey, moderationViolence);
                    MetadataKey<Boolean> violenceGraphicKey = new MetadataKey<>("violence/graphic", Metadata.BOOLEAN);
                    Boolean moderationViolenceGraphic = categories.get("violence/graphic");
                    put(violenceGraphicKey, moderationViolenceGraphic);
                }
                Map<String, Double> categoryScores = (Map<String, Double>) result.get("category_scores");
                if (categoryScores != null) {
                    MetadataKey<Double> sexualScoreKey = new MetadataKey<>("sexual", Metadata.DOUBLE);
                    Double moderationSexualScore = categoryScores.get("sexual");
                    put(sexualScoreKey, moderationSexualScore);
                    MetadataKey<Double> sexualMinorsScoreKey = new MetadataKey<>("sexual/minors", Metadata.DOUBLE);
                    Double moderationSexualMinorsScore = categoryScores.get("sexual/minors");
                    put(sexualMinorsScoreKey, moderationSexualMinorsScore);
                    MetadataKey<Double> harassmentScoreKey = new MetadataKey<>("harassment", Metadata.DOUBLE);
                    Double moderationHarassmentScore = categoryScores.get("harassment");
                    put(harassmentScoreKey, moderationHarassmentScore);
                    MetadataKey<Double> harassmentThreateningScoreKey = new MetadataKey<>("harassment/threatening", Metadata.DOUBLE);
                    Double moderationHarassmentThreateningScore = categoryScores.get("harassment/threatening");
                    put(harassmentThreateningScoreKey, moderationHarassmentThreateningScore);
                    MetadataKey<Double> hateScoreKey = new MetadataKey<>("hate", Metadata.DOUBLE);
                    Double moderationHateScore = categoryScores.get("hate");
                    put(hateScoreKey, moderationHateScore);
                    MetadataKey<Double> hateThreateningScoreKey = new MetadataKey<>("hate/threatening", Metadata.DOUBLE);
                    Double moderationHateThreateningScore = categoryScores.get("hate/threatening");
                    put(hateThreateningScoreKey, moderationHateThreateningScore);
                    MetadataKey<Double> illicitScoreKey = new MetadataKey<>("illicit", Metadata.DOUBLE);
                    Double moderationIllicitScore = categoryScores.get("illicit");
                    put(illicitScoreKey, moderationIllicitScore);
                    MetadataKey<Double> illicitViolentScoreKey = new MetadataKey<>("illicit/violent", Metadata.DOUBLE);
                    Double moderationIllicitViolentScore = categoryScores.get("illicit/violent");
                    put(illicitViolentScoreKey, moderationIllicitViolentScore);
                    MetadataKey<Double> selfHarmScoreKey = new MetadataKey<>("self-harm", Metadata.DOUBLE);
                    Double moderationSelfHarmScore = categoryScores.get("self-harm");
                    put(selfHarmScoreKey, moderationSelfHarmScore);
                    MetadataKey<Double> selfHarmIntentScoreKey = new MetadataKey<>("self-harm/intent", Metadata.DOUBLE);
                    Double moderationSelfHarmIntentScore = categoryScores.get("self-harm/intent");
                    put(selfHarmIntentScoreKey, moderationSelfHarmIntentScore);
                    MetadataKey<Double> selfHarmInstructionsScoreKey = new MetadataKey<>("self-harm/instructions", Metadata.DOUBLE);
                    Double moderationSelfHarmInstructionsScore = categoryScores.get("self-harm/instructions");
                    put(selfHarmInstructionsScoreKey, moderationSelfHarmInstructionsScore);
                    MetadataKey<Double> violenceScoreKey = new MetadataKey<>("violence", Metadata.DOUBLE);
                    Double moderationViolenceScore = categoryScores.get("violence");
                    put(violenceScoreKey, moderationViolenceScore);
                    MetadataKey<Double> violenceGraphicScoreKey = new MetadataKey<>("violence/graphic", Metadata.DOUBLE);
                    Double moderationViolenceGraphicScore = categoryScores.get("violence/graphic");
                    put(violenceGraphicScoreKey, moderationViolenceGraphicScore);
                }
            }
        }

        else if (requestId.startsWith("resp")) {
            MetadataKey<String> objectKey = new MetadataKey<>("object", Metadata.STRING);
            String requestObject = (String) responseMap.get("object");
            if (requestObject == null) {
                throw new NullPointerException("The response map is missing the mandatory 'object' field.");
            }
            put(objectKey, requestObject);
            MetadataKey<String> responsesIdKey = new MetadataKey<>("id", Metadata.STRING);
            String responsesId = (String) responseMap.get("id");
            put(responsesIdKey, responsesId);
            MetadataKey<String> responsesObjectKey = new MetadataKey<>("object", Metadata.STRING);
            String responsesObject = (String) responseMap.get("object");
            put(responsesObjectKey, responsesObject);
            MetadataKey<Integer> responsesCreatedAtKey = new MetadataKey<>("created_at", Metadata.INTEGER);
            Integer responsesCreatedAt = (Integer) responseMap.get("created_at");
            put(responsesCreatedAtKey, responsesCreatedAt);
            MetadataKey<String> responsesStatusKey = new MetadataKey<>("status", Metadata.STRING);
            String responsesStatus = (String) responseMap.get("status");
            put(responsesStatusKey, responsesStatus);
            MetadataKey<String> responsesErrorKey = new MetadataKey<>("error", Metadata.STRING);
            String responsesError = (String) responseMap.get("error");
            put(responsesErrorKey, responsesError);
            MetadataKey<String> responsesIncompleteDetailsReasonKey = new MetadataKey<>("reason", Metadata.STRING);
            Map<String, String> responsesIncompleteDetails = (Map<String, String>) responseMap.get("incomplete_details");
            String reason = responsesIncompleteDetails != null ? responsesIncompleteDetails.get("reason") : null;
            put(responsesIncompleteDetailsReasonKey, reason);
            MetadataKey<String> responsesInstructionsKey = new MetadataKey<>("instructions", Metadata.STRING);
            String responsesInstructions = (String) responseMap.get("instructions");
            put(responsesInstructionsKey, responsesInstructions);
            MetadataKey<Integer> responsesMaxOutputTokensKey = new MetadataKey<>("max_output_tokens", Metadata.INTEGER);
            Integer responsesMaxOutputTokens = (Integer) responseMap.get("max_output_tokens");
            put(responsesMaxOutputTokensKey, responsesMaxOutputTokens);
            MetadataKey<String> responsesModelKey = new MetadataKey<>("model", Metadata.STRING);
            String responsesModel = (String) responseMap.get("model");
            put(responsesModelKey, responsesModel);
            MetadataKey<Boolean> responsesParallelToolCallsKey = new MetadataKey<>("parallel_tool_calls", Metadata.BOOLEAN);
            Boolean responsesParallelToolCalls = (Boolean) responseMap.get("parallel_tool_calls");
            put(responsesParallelToolCallsKey, responsesParallelToolCalls);
            MetadataKey<String> responsesPreviousResponseIdKey = new MetadataKey<>("previous_response_id", Metadata.STRING);
            String responsesPreviousResponseId = (String) responseMap.get("previous_response_id");
            put(responsesPreviousResponseIdKey, responsesPreviousResponseId);
            MetadataKey<String> responsesReasoningEffortKey = new MetadataKey<>("effort", Metadata.STRING);
            MetadataKey<String> responsesReasoningSummaryKey = new MetadataKey<>("summary", Metadata.STRING);
            Map<String, String> responsesReasoning = (Map<String, String>) responseMap.get("reasoning");
            if (responsesReasoning != null) {
                String responsesReasoningEffort = responsesReasoning.get("effort");
                put(responsesReasoningEffortKey, responsesReasoningEffort);
                String responsesReasoningSummary = responsesReasoning.get("summary");
                put(responsesReasoningSummaryKey, responsesReasoningSummary);
            }
            MetadataKey<Double> responsesTemperatureKey = new MetadataKey<>("temperature", Metadata.DOUBLE);
            Double responsesTemperature = (Double) responseMap.get("temperature");
            put(responsesTemperatureKey, responsesTemperature);
            MetadataKey<Map<String, Object>> responsesTextFormatKey = new MetadataKey<>("text_format", Metadata.MAP);
            Map<String, Object> responsesTextFormat = (Map<String, Object>) responseMap.get("text");
            put(responsesTextFormatKey, responsesTextFormat);
            MetadataKey<List<Map<String, Object>>> toolsKey = new MetadataKey<>("tools", LIST);
            List<Map<String, Object>> toolsList = (List<Map<String, Object>>) responseMap.get("tools");
            put(toolsKey, toolsList);
            if (responseMap.containsKey("type")) {
                put(FILESEARCHTOOL_TYPE, String.valueOf(responseMap.get("type")));
            }
            if (responseMap.containsKey("vector_store_ids")) {
                put(FILESEARCHTOOL_VECTOR_STORE_IDS, (List<String>) responseMap.get("vector_store_ids"));
            }
            if (responseMap.containsKey("filters")) {
                put(FILESEARCHTOOL_FILTERS, (Map<String, Object>) responseMap.get("filters"));
            }
            if (responseMap.containsKey("max_num_results")) {
                put(FILESEARCHTOOL_MAX_NUM_RESULTS, (Integer) responseMap.get("max_num_results"));
            }
            if (responseMap.containsKey("ranking_options")) {
                put(FILESEARCHTOOL_RANKING_OPTIONS, (Map<String, Object>) responseMap.get("ranking_options"));
            }
            MetadataKey<Double> responsesTopPKey = new MetadataKey<>("top_p", Metadata.DOUBLE);
            Double responsesTopP = (Double) responseMap.get("top_p");
            put(responsesTopPKey, responsesTopP);
            MetadataKey<String> responsesTruncationKey = new MetadataKey<>("truncation", Metadata.STRING);
            String responsesTruncation = (String) responseMap.get("truncation");
            put(responsesTruncationKey, responsesTruncation);
            MetadataKey<Integer> responsesTotalTokensKey = new MetadataKey<>("total_tokens", Metadata.INTEGER);
            Map<String, Object> responsesUsage = (Map<String, Object>) responseMap.get("usage");
            if (responsesUsage != null) {
                Integer responsesTotalTokens = (Integer) responsesUsage.get("total_tokens");
                put(responsesTotalTokensKey, responsesTotalTokens);
            }
            MetadataKey<String> responsesUserKey = new MetadataKey<>("user", Metadata.STRING);
            String responsesUser = (String) responseMap.get("user");
            put(responsesUserKey, responsesUser);
            MetadataKey<Map<String, Object>> responsesMetadataKey = new MetadataKey<>("metadata", Metadata.MAP);
            Map<String, Object> responsesMetadata = (Map<String, Object>) responseMap.get("metadata");
            put(responsesMetadataKey, responsesMetadata);
            MetadataKey<String> responsesOutputContentKey = new MetadataKey<>("output_content", Metadata.STRING);
            Object outputObj = responseMap.get("output");
            if (outputObj instanceof List<?> outputList) {
                outer:
                for (Object outputItem : outputList) {
                    if (!(outputItem instanceof Map<?, ?> messageMap)) continue;
                    Object contentObj = messageMap.get("content");
                    if (!(contentObj instanceof List<?> contentList)) continue;
                    for (Object contentEntry : contentList) {
                        if (!(contentEntry instanceof Map<?, ?> contentMap)) continue;
                        Object text = contentMap.get("text");
                        if (text instanceof String textStr && !textStr.isBlank()) {
                            System.out.println(textStr);
                            put(responsesOutputContentKey, textStr);
                            break outer;
                        }
                    }
                }
            }
        }
    }

    /*
     *    Getters
     */
    public CompletableFuture<String> completeGetFileSearchToolType() {
        return CompletableFuture.completedFuture(this.get(FILESEARCHTOOL_TYPE));
    }

    public CompletableFuture<List<String>> completeGetFileSearchToolVectorStoreIds() {
        return CompletableFuture.completedFuture(this.get(FILESEARCHTOOL_VECTOR_STORE_IDS));
    }

    public CompletableFuture<Map<String, Object>> completeGetFileSearchToolFilters() {
        return CompletableFuture.completedFuture(this.get(FILESEARCHTOOL_FILTERS));
    }

    public CompletableFuture<Integer> completeGetFileSearchToolMaxNumResults() {
        return CompletableFuture.completedFuture(this.get(FILESEARCHTOOL_MAX_NUM_RESULTS));
    }

    public CompletableFuture<Map<String, Object>> completeGetFileSearchToolRankingOptions() {
        return CompletableFuture.completedFuture(this.get(FILESEARCHTOOL_RANKING_OPTIONS));
    }

    public CompletableFuture<Boolean> completeGetFlagged() {
        return CompletableFuture.supplyAsync(() -> {
            MetadataKey<Boolean> flaggedKey = new MetadataKey<>("flagged", Metadata.BOOLEAN);
            Object flaggedObj = this.get(flaggedKey);
            return flaggedObj != null && Boolean.parseBoolean(String.valueOf(flaggedObj));
        });
    }

    public CompletableFuture<String> completeGetToolChoice() {
        return CompletableFuture.supplyAsync(() -> {
            MetadataKey<String> toolChoiceKey = new MetadataKey<>("tool_choice", Metadata.STRING);
            Object toolChoiceObj = this.get(toolChoiceKey);
            return toolChoiceObj != null ? String.valueOf(toolChoiceObj) : null;
        });
    }

    public CompletableFuture<List<Map<String, Object>>> completeGetTools() {
        return CompletableFuture.supplyAsync(() -> {
            MetadataKey<List<Map<String, Object>>> toolsKey = new MetadataKey<>("tools", LIST);
            Object toolsObj = this.get(toolsKey);
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
                Object value = this.get(key);
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
            return this.get(responseIdKey);
        });
    }

    public CompletableFuture<String> completeGetOutput() {
        return CompletableFuture.supplyAsync(() -> {
            MetadataKey<String> outputKey = new MetadataKey<>("output_content", Metadata.STRING);
            return this.get(outputKey);
        });
    }

    public CompletableFuture<Integer> completeGetPerplexity() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MetadataKey<String> outputKey = new MetadataKey<>("output_content", Metadata.STRING);
                ObjectMapper objectMapper = new ObjectMapper();
                String json = this.get(outputKey);
                Map<String, Integer> responseMap = objectMapper.readValue(json, new TypeReference<Map<String, Integer>>() {});
                System.out.println(json);
                return responseMap.get("perplexity");
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    public CompletableFuture<String> completeGetPreviousResponseId() {
        return CompletableFuture.supplyAsync(() -> {
            MetadataKey<String> previousResponseIdKey = new MetadataKey<>("previous_response_id", Metadata.STRING);
            return this.get(previousResponseIdKey);
        });
    }

    /*
     *    Setters
     */
    public CompletableFuture<Void> completeSetPreviousResponseId(String previousResponseId) {
        return CompletableFuture.runAsync(() -> {
            MetadataKey<String> previousResponseIdKey = new MetadataKey<>("previous_response_id", Metadata.STRING);
            put(previousResponseIdKey, previousResponseId);
        });
    }
}
