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
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ResponseObject extends MetadataContainer{
    
    private static final MetadataType<List<String>> LIST = new MetadataList(Metadata.STRING);
    public static final MetadataKey<String> FILESEARCHTOOL_TYPE = new MetadataKey<>("filesearchtool_type", Metadata.STRING);
    public static final MetadataKey<List<String>> FILESEARCHTOOL_VECTOR_STORE_IDS = new MetadataKey<>("filesearchtool_vector_store_ids", Metadata.LIST_STRING);
    public static final MetadataKey<Map<String, Object>> FILESEARCHTOOL_FILTERS = new MetadataKey<>("filesearchtool_filters", Metadata.MAP);
    public static final MetadataKey<Integer> FILESEARCHTOOL_MAX_NUM_RESULTS = new MetadataKey<>("filesearchtool_max_num_results", Metadata.INTEGER);
    public static final MetadataKey<Map<String, Object>> FILESEARCHTOOL_RANKING_OPTIONS = new MetadataKey<>("filesearchtool_ranking_options", Metadata.MAP);
    public static final MetadataKey<Map<String, Object>> FILESEARCHTOOL_FILTER_COMPARISON = new MetadataKey<>("filesearchtool_filter_comparison", Metadata.MAP);
    public static final MetadataKey<Map<String, Object>> FILESEARCHTOOL_FILTER_COMPOUND = new MetadataKey<>("filesearchtool_filter_compound", Metadata.MAP);
    public static final MetadataKey<List<Map<String, Object>>> FILESEARCHTOOL_FILTER_COMPOUND_LIST = new MetadataKey<>("filesearchtool_filter_compound_list", Metadata.LIST_MAP);
    public static final MetadataKey<String> TOOLCHOICE_MODE = new MetadataKey<>("toolChoice_mode", Metadata.STRING);
    public static final MetadataKey<String> TOOLCHOICE_TYPE = new MetadataKey<>("toolChoice_type", Metadata.STRING);
    public static final MetadataKey<String> TOOLCHOICE_NAME = new MetadataKey<>("toolChoice_name", Metadata.STRING);
    public static final MetadataKey<String> WEBSEARCHTOOL_TYPE = new MetadataKey<>("webSearchTool_type", Metadata.STRING);
    public static final MetadataKey<String> WEBSEARCHTOOL_CONTEXT_SIZE = new MetadataKey<>("webSearchTool_context_size", Metadata.STRING);
    public static final MetadataKey<String> WEBSEARCHTOOL_LOCATION_TYPE = new MetadataKey<>("webSearchTool_location_type", Metadata.STRING);
    public static final MetadataKey<String> WEBSEARCHTOOL_LOCATION_CITY = new MetadataKey<>("webSearchTool_location_city", Metadata.STRING);
    public static final MetadataKey<String> WEBSEARCHTOOL_LOCATION_COUNTRY = new MetadataKey<>("webSearchTool_location_country", Metadata.STRING);
    public static final MetadataKey<String> WEBSEARCHTOOL_LOCATION_REGION = new MetadataKey<>("webSearchTool_location_region", Metadata.STRING);
    public static final MetadataKey<String> WEBSEARCHTOOL_LOCATION_TIMEZONE = new MetadataKey<>("webSearchTool_location_timezone", Metadata.STRING);
    public static final MetadataKey<String> COMPUTERTOOL_TYPE = new MetadataKey<>("computertool_type", Metadata.STRING);
    public static final MetadataKey<Integer> COMPUTERTOOL_DISPLAY_HEIGHT = new MetadataKey<>("computertool_display_height", Metadata.INTEGER);
    public static final MetadataKey<Integer> COMPUTERTOOL_DISPLAY_WIDTH = new MetadataKey<>("computertool_display_width", Metadata.INTEGER);
    public static final MetadataKey<String> COMPUTERTOOL_ENVIRONMENT = new MetadataKey<>("computertool_environment", Metadata.STRING);
    public static final MetadataKey<String> MCPTOOL_TYPE = new MetadataKey<>("mcptool_type", Metadata.STRING);
    public static final MetadataKey<String> MCPTOOL_SERVER_LABEL = new MetadataKey<>("mcptool_server_label", Metadata.STRING);
    public static final MetadataKey<String> MCPTOOL_SERVER_URL = new MetadataKey<>("mcptool_server_url", Metadata.STRING);
    public static final MetadataKey<List<String>> MCPTOOL_ALLOWED_TOOLS = new MetadataKey<>("mcptool_allowed_tools", new MetadataList<>(Metadata.STRING));
    public static final MetadataKey<Map<String, Object>> MCPTOOL_ALLOWED_TOOLS_FILTER = new MetadataKey<>("mcptool_allowed_tools_filter", Metadata.MAP);
    public static final MetadataKey<Map<String, Object>> MCPTOOL_HEADERS = new MetadataKey<>("mcptool_headers", Metadata.MAP);
    public static final MetadataKey<String> MCPTOOL_REQUIRE_APPROVAL_MODE = new MetadataKey<>("mcptool_require_approval_mode", Metadata.STRING);
    public static final MetadataKey<Map<String, Object>> MCPTOOL_REQUIRE_APPROVAL_ALWAYS = new MetadataKey<>("mcptool_require_approval_always", Metadata.MAP);
    public static final MetadataKey<Map<String, Object>> MCPTOOL_REQUIRE_APPROVAL_NEVER = new MetadataKey<>("mcptool_require_approval_never", Metadata.MAP);
    public static final MetadataKey<String> CODEINTERPRETERTOOL_TYPE = new MetadataKey<>("codeinterpretertool_type", Metadata.STRING);
    public static final MetadataKey<String> CODEINTERPRETERTOOL_CONTAINER_ID = new MetadataKey<>("codeinterpretertool_container_id", Metadata.STRING);
    public static final MetadataKey<Map<String, Object>> CODEINTERPRETERTOOL_CONTAINER_MAP = new MetadataKey<>("codeinterpretertool_container_map", Metadata.MAP);
    public static final MetadataKey<String> LOCALSHELLTOOL_TYPE = new MetadataKey<>("localshelltool_type", Metadata.STRING);
    public static final MetadataKey<String> LOCALSHELLTOOL_COMMAND = new MetadataKey<>("localshelltool_command", Metadata.STRING);

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
            if (responseMap.containsKey("tools")) {
                for (Map<String, Object> toolMap : toolsList) {
                    if (toolMap.get("type") instanceof String type) {
                        switch (type) {
                            case "file_search" -> {
                                put(FILESEARCHTOOL_TYPE, type);
                                if (List.of("file-ToDXSxedx12w7xAFJa1kdM") instanceof List<?> vsIds)
                                    put(FILESEARCHTOOL_VECTOR_STORE_IDS, (List<String>) vsIds);
                                Object filtersObj = toolMap.get("filters");
                                if (filtersObj instanceof Map filterMap) {
                                    put(FILESEARCHTOOL_FILTERS, filterMap); // General flat storage

                                    // Detect Comparison Filter
                                    if (filterMap.containsKey("key") && filterMap.containsKey("type") && filterMap.containsKey("value")) {
                                        put(FILESEARCHTOOL_FILTER_COMPARISON, filterMap);
                                    }

                                    // Detect Compound Filter
                                    if ("and".equals(filterMap.get("type")) || "or".equals(filterMap.get("type"))) {
                                        put(FILESEARCHTOOL_FILTER_COMPOUND, filterMap);

                                        Object subFilters = filterMap.get("filters");
                                        if (subFilters instanceof List<?> subList) {
                                            @SuppressWarnings("unchecked")
                                            List<Map<String, Object>> casted = (List<Map<String, Object>>) subList;
                                            put(FILESEARCHTOOL_FILTER_COMPOUND_LIST, casted);
                                        }
                                    }
                                }
                                if (toolMap.get("max_num_results") instanceof Number num)
                                    put(FILESEARCHTOOL_MAX_NUM_RESULTS, num.intValue());
                                if (toolMap.get("ranking_options") instanceof Map<?, ?> rankingOpts)
                                    put(FILESEARCHTOOL_RANKING_OPTIONS, (Map<String, Object>) rankingOpts);
                            }
                            case "web_search_preview", "web_search_preview_2025_03_11" -> {
                                put(WEBSEARCHTOOL_TYPE, type);
                                if (toolMap.get("search_context_size") instanceof String size)
                                    put(WEBSEARCHTOOL_CONTEXT_SIZE, size);

                                Object locObj = toolMap.get("user_location");
                                if (locObj instanceof Map<?, ?> loc) {
                                    if (loc.get("type") instanceof String locType)
                                        put(WEBSEARCHTOOL_LOCATION_TYPE, locType);
                                    if (loc.get("city") instanceof String city)
                                        put(WEBSEARCHTOOL_LOCATION_CITY, city);
                                    if (loc.get("country") instanceof String country)
                                        put(WEBSEARCHTOOL_LOCATION_COUNTRY, country);
                                    if (loc.get("region") instanceof String region)
                                        put(WEBSEARCHTOOL_LOCATION_REGION, region);
                                    if (loc.get("timezone") instanceof String tz)
                                        put(WEBSEARCHTOOL_LOCATION_TIMEZONE, tz);
                                }
                            }
                            case "computer_use_preview" -> {
                                put(COMPUTERTOOL_TYPE, type);
                                if (toolMap.get("display_height") instanceof Number height)
                                    put(COMPUTERTOOL_DISPLAY_HEIGHT, height.intValue());
                                if (toolMap.get("display_width") instanceof Number width)
                                    put(COMPUTERTOOL_DISPLAY_WIDTH, width.intValue());
                                if (toolMap.get("environment") instanceof String env)
                                    put(COMPUTERTOOL_ENVIRONMENT, env);
                            }
                            case "mcp" -> {
                                put(MCPTOOL_TYPE, type);

                                if (toolMap.get("server_label") instanceof String serverLabel)
                                    put(MCPTOOL_SERVER_LABEL, serverLabel);

                                if (toolMap.get("server_url") instanceof String serverUrl)
                                    put(MCPTOOL_SERVER_URL, serverUrl);

                                if (toolMap.get("allowed_tools") instanceof List<?> allowedToolList)
                                    put(MCPTOOL_ALLOWED_TOOLS, (List<String>) allowedToolList);

                                if (toolMap.get("allowed_tools") instanceof Map<?, ?> allowedToolMap)
                                    put(MCPTOOL_ALLOWED_TOOLS_FILTER, (Map<String, Object>) allowedToolMap);

                                if (toolMap.get("headers") instanceof Map<?, ?> headersMap)
                                    put(MCPTOOL_HEADERS, (Map<String, Object>) headersMap);

                                if (toolMap.get("require_approval") instanceof String approvalSetting)
                                    put(MCPTOOL_REQUIRE_APPROVAL_MODE, approvalSetting);

                                if (toolMap.get("require_approval") instanceof Map<?, ?> approvalMap) {
                                    Object always = ((Map<?, ?>) approvalMap).get("always");
                                    Object never = ((Map<?, ?>) approvalMap).get("never");

                                    if (always instanceof Map<?, ?> alwaysMap)
                                        put(MCPTOOL_REQUIRE_APPROVAL_ALWAYS, (Map<String, Object>) alwaysMap);

                                    if (never instanceof Map<?, ?> neverMap)
                                        put(MCPTOOL_REQUIRE_APPROVAL_NEVER, (Map<String, Object>) neverMap);
                                }
                            }
                            case "code_interpreter" -> {
                                put(CODEINTERPRETERTOOL_TYPE, type);

                                Object containerObj = toolMap.get("container");

                                if (containerObj instanceof String containerId) {
                                    put(CODEINTERPRETERTOOL_CONTAINER_ID, containerId);
                                } else if (containerObj instanceof Map<?, ?> containerMap) {
                                    // Store the raw container object (e.g., uploaded files or other settings)
                                    put(CODEINTERPRETERTOOL_CONTAINER_MAP, (Map<String, Object>) containerMap);
                                }
                            }
                            case "local_shell" -> {
                                put(LOCALSHELLTOOL_TYPE, type);
                                // Capture the shell command if provided
                                Object cmdObj = toolMap.get("command");
                                if (cmdObj instanceof String cmd) {
                                    put(LOCALSHELLTOOL_COMMAND, cmd);
                                }
                            }
                        }
                    }
                }
            }

// Handl    e `tool_choice` root-level object
            if (responseMap.containsKey("tool_choice") && responseMap.get("tool_choice") instanceof Map<?, ?> toolChoice) {
                if (toolChoice.get("mode") instanceof String mode) {
                    put(TOOLCHOICE_MODE, mode);
                }
                if (toolChoice.get("type") instanceof String type) {
                    put(TOOLCHOICE_TYPE, type);
                    if ("function".equals(type) && toolChoice.get("name") instanceof String name) {
                        put(TOOLCHOICE_NAME, name);
                    }
                }
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
                for (Object outputItemObj : outputList) {
                    if (!(outputItemObj instanceof Map<?, ?> outputItem)) continue;

                    // Handle local shell tool calls
                    String type = (String) outputItem.get("type");
                    if ("local_shell_call".equals(type)) {
                        Object actionObj = outputItem.get("action");
                        if (actionObj instanceof Map<?, ?> action) {
                            // capture the call ID
                            Object idObj = outputItem.get("call_id");
                            if (idObj instanceof String cid) {
                                put(ToolHandler.LOCALSHELLTOOL_CALL_ID, cid);
                            }
                            // capture the shell command
                            Object commandObj = action.get("command");
                            if (commandObj instanceof List<?> cmdList) {
                                @SuppressWarnings("unchecked")
                                List<String> commandParts = (List<String>) cmdList;
                                String joinedCommand = String.join(" ", commandParts);
                                put(ToolHandler.LOCALSHELLTOOL_COMMAND, joinedCommand);
                            }
                        }
                    }

                    // Handle assistant messages (text output)
                    if ("message".equals(type)) {
                        Object contentObj = outputItem.get("content");
                        if (contentObj instanceof List<?> contentList) {
                            for (Object contentEntry : contentList) {
                                if (!(contentEntry instanceof Map<?, ?> contentMap)) continue;
                                Object text = contentMap.get("text");
                                if (text instanceof String textStr && !textStr.isBlank()) {
                                    System.out.println(textStr);
                                    put(responsesOutputContentKey, textStr);
                                    break;
                                }
                            }
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

    public CompletableFuture<String> completeGetShellToolCommand() {
        return CompletableFuture.completedFuture(this.get(LOCALSHELLTOOL_COMMAND));
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
