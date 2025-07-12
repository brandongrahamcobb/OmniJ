/* OpenAIContainer.java The purpose of this class is to interpret and
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
package com.brandongcobb.omnij.objects;

import com.brandongcobb.metadata.Metadata;
import com.brandongcobb.metadata.MetadataKey;
import com.brandongcobb.metadata.MetadataList;
import com.brandongcobb.omnij.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.logging.Logger;

public class OpenAIContainer extends MainContainer {
    
    public static Map<String, Object> mapMap;
    private static final Logger LOGGER = Logger.getLogger(Application.class.getName());
    public static final MetadataKey<Integer> SHELL_EXIT_CODE = new MetadataKey<>("shell.exit_code", Metadata.INTEGER);
    public static final MetadataKey<String> SHELL_STDOUT = new MetadataKey<>("shell.stdout", Metadata.STRING);
    public static final MetadataKey<String> SHELL_STDERR = new MetadataKey<>("shell.stderr", Metadata.STRING);
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
    public static final MetadataKey<String> TOOLCHOICE_TOOL = new MetadataKey<>("toolChoice_tool", Metadata.STRING);
    public static final MetadataKey<Integer> TOOLCHOICE_INDEX = new MetadataKey<>("toolChoice_index", Metadata.INTEGER);
    public static final MetadataKey<Map<String, Object>> TOOLCHOICE_ARGUMENTS = new MetadataKey<>("toolChoice_arguments", Metadata.MAP);
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
    public static final MetadataKey<String> LOCALSHELLTOOL_COMMAND = new MetadataKey<>("localshelltool_command", Metadata.STRING);
    public static final MetadataKey<List<String>> LOCALSHELLTOOL_COMMANDS = new MetadataKey<>("localshelltool_commands", Metadata.LIST_STRING);
    public static final MetadataKey<Boolean> LOCALSHELLTOOL_FINISHED = new MetadataKey<>("localshelltool_finished", Metadata.BOOLEAN);
    public static final MetadataKey<String> LOCALSHELLTOOL_TYPE = new MetadataKey<>("localshelltool_type", Metadata.STRING);
    public static final String LOCALSHELLTOOL_COMMANDS_LIST = "localshelltool_commands_list";
    public static final String LOCALSHELLTOOL_CALL_IDS = "localshelltool_call_ids";
    
    private boolean isQuoted(String s) {
        return (s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"));
    }
    
    public OpenAIContainer(Map<String, Object> responseMap) {
        MetadataKey<String> idKey = new MetadataKey<>("id", Metadata.STRING);
        String requestId = (String) responseMap.get("id");
        put(idKey, requestId);
        if (requestId.contains("chatcmpl")) {
            MetadataKey<Integer> tokenCountKey = new MetadataKey<>("token_count", Metadata.INTEGER);
            Map<String, Object> usage = (Map<String, Object>) responseMap.get("usage");
            if (usage != null && usage.get("total_tokens") instanceof Number) {
                Integer totalTokens =  ((Number) usage.get("total_tokens")).intValue();
                put(tokenCountKey, totalTokens);
            }
            List<Map<String, Object>> completionChoices = (List<Map<String, Object>>) responseMap.get("choices");
            if (completionChoices == null || completionChoices.isEmpty()) return;
            Map<String, Object> completionChoice = completionChoices.get(0);
            String finishReason = (String) completionChoice.get("finish_reason");
            LOGGER.finer(finishReason);
            if (finishReason != null) {
                MetadataKey<String> finishKey = new MetadataKey<>("finish_reason", Metadata.STRING);
                put(finishKey, finishReason);
            }
            Map<String, Object> completionMessage = (Map<String, Object>) completionChoice.get("message");
            if (completionMessage != null) {
                String completionContent = completionMessage != null ? (String) completionMessage.get("content") : null;
                MetadataKey<String> completionContentKey = new MetadataKey<>("content", Metadata.STRING);
                put(completionContentKey, completionContent);
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
                MetadataKey<Double> responsesTopPKey = new MetadataKey<>("top_p", Metadata.DOUBLE);                              // MUST MATCH THE RESP OBJECT     VALUE
                Double responsesTopP = (Double) responseMap.get("top_p");                                                        // DO NOT CHANGE TO INTEGER if NON OPENAI,     CAUSES ERROR
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
                MetadataKey<String> responsesOutputContentKey = new MetadataKey<>("output_content", Metadata.STRING);
                Object outputObj = responseMap.get("output");
                List<String> allCallIds = new ArrayList<>();
                List<List<String>> allCommands = new ArrayList<>();
                if (outputObj instanceof List<?> outputList) {
                    for (Object outputItemObj : outputList) {
                        if (!(outputItemObj instanceof Map<?, ?> outputItem)) continue;
                        Object typeObj = outputItem.get("type");
                        if ("tool_call".equals(typeObj)) {
                            Object callIdObj = outputItem.get("call_id");
                            if (callIdObj instanceof String callId) {
                                allCallIds.add(callId);
                            }
                            Object actionObj = outputItem.get("action");
                            if (actionObj instanceof Map<?, ?> action) {
                                Object cmdObj = action.get("command");

                                List<String> commandsForThisCall = new ArrayList<>();

                                if (cmdObj instanceof List<?> cmdList) {
                                    // Just keep the command parts as-is (no escaping here)
                                    commandsForThisCall = cmdList.stream()
                                        .map(Object::toString)
                                        .toList();
                                } else if (cmdObj instanceof String singleCommand) {
                                    commandsForThisCall = List.of(singleCommand);
                                } else if (cmdObj != null) {
                                    commandsForThisCall = List.of(cmdObj.toString());
                                }

                                allCommands.add(commandsForThisCall);
                            }
                        }
                        Object contentObject = outputItem.get("content");
                        if (contentObject instanceof List<?> contentObjectList) {
                            for (Object contentItemObj : contentObjectList) {
                                if (!(contentItemObj instanceof Map<?, ?> contentItem)) continue;
                                Object textObj = contentItem.get("text");
                                if (textObj instanceof String textStr && !textStr.isBlank()) {
                                    put(responsesOutputContentKey, textStr);
                                    break;
                                }
                            }
                        }
                    }
                }
                Map<String, Object> myMap = new HashMap<>();
                myMap.put(LOCALSHELLTOOL_CALL_IDS, allCallIds);
                myMap.put(LOCALSHELLTOOL_COMMANDS_LIST, allCommands);
                mapMap = myMap;
                MetadataKey<List<Map<String, Object>>> toolsKey = new MetadataKey<>("tools", Metadata.LIST_MAP);
                Object toolsObj = responseMap.get("tools");
                if (toolsObj instanceof List<?> toolsListRaw) {
                    List<Map<String, Object>> toolsList = new ArrayList<>();
                    for (Object toolObj : toolsListRaw) {
                        if (toolObj instanceof Map<?, ?> toolMapRaw) {
                            toolsList.add((Map<String, Object>) toolMapRaw);
                        }
                    }
                    put(toolsKey, toolsList);
                    for (Map<String, Object> toolMap : toolsList) {
                        Object typeObj = toolMap.get("type");
                        if (!(typeObj instanceof String type)) continue;
                        switch (type) {
                            case "file_search" -> {
                                put(FILESEARCHTOOL_TYPE, "file_search");
                                put(FILESEARCHTOOL_VECTOR_STORE_IDS, List.of("file-PLACEHOLDER"));
                                Object filtersObj = toolMap.get("filters");
                                if (filtersObj instanceof Map<?, ?> filterMap) {
                                    put(FILESEARCHTOOL_FILTERS, (Map<String, Object>) filterMap);
                                    if (filterMap.containsKey("key") && filterMap.containsKey("type") && filterMap.containsKey("value")) {
                                        put(FILESEARCHTOOL_FILTER_COMPARISON, (Map<String, Object>) filterMap);
                                    }
                                    if ("and".equals(filterMap.get("type")) || "or".equals(filterMap.get("type"))) {
                                        put(FILESEARCHTOOL_FILTER_COMPOUND, (Map<String, Object>) filterMap);
                                        Object subFilters = filterMap.get("filters");
                                        if (subFilters instanceof List<?> subList) {
                                            List<Map<String, Object>> casted = new ArrayList<>();
                                            for (Object subFilterObj : subList) {
                                                if (subFilterObj instanceof Map<?, ?> subFilterMap) {
                                                    casted.add((Map<String, Object>) subFilterMap);
                                                }
                                            }
                                            put(FILESEARCHTOOL_FILTER_COMPOUND_LIST, casted);
                                        }
                                    }
                                }
                                Object maxNumResults = toolMap.get("max_num_results");
                                if (maxNumResults instanceof Number num) {
                                    put(FILESEARCHTOOL_MAX_NUM_RESULTS, num.intValue());
                                }
                                Object rankingOpts = toolMap.get("ranking_options");
                                if (rankingOpts instanceof Map<?, ?> rankingMap) {
                                    put(FILESEARCHTOOL_RANKING_OPTIONS, (Map<String, Object>) rankingMap);
                                }
                            }
                            case "web_search_preview", "web_search_preview_2025_03_11" -> {
                                put(WEBSEARCHTOOL_TYPE, type);
                                Object searchContextSize = toolMap.get("search_context_size");
                                if (searchContextSize instanceof String size) {
                                    put(WEBSEARCHTOOL_CONTEXT_SIZE, size);
                                }
                                Object userLocObj = toolMap.get("user_location");
                                if (userLocObj instanceof Map<?, ?> loc) {
                                    if (loc.get("type") instanceof String locType) put(WEBSEARCHTOOL_LOCATION_TYPE, locType);
                                    if (loc.get("city") instanceof String city) put(WEBSEARCHTOOL_LOCATION_CITY, city);
                                    if (loc.get("country") instanceof String country) put(WEBSEARCHTOOL_LOCATION_COUNTRY, country);
                                    if (loc.get("region") instanceof String region) put(WEBSEARCHTOOL_LOCATION_REGION, region);
                                    if (loc.get("timezone") instanceof String tz) put(WEBSEARCHTOOL_LOCATION_TIMEZONE, tz);
                                }
                            }
                            case "computer_use_preview" -> {
                                put(COMPUTERTOOL_TYPE, type);
                                if (toolMap.get("display_height") instanceof Number height) put(COMPUTERTOOL_DISPLAY_HEIGHT, height.intValue());
                                if (toolMap.get("display_width") instanceof Number width) put(COMPUTERTOOL_DISPLAY_WIDTH, width.intValue());
                                if (toolMap.get("environment") instanceof String env) put(COMPUTERTOOL_ENVIRONMENT, env);
                            }
                            case "mcp" -> {
                                put(MCPTOOL_TYPE, type);
                                if (toolMap.get("server_label") instanceof String serverLabel) put(MCPTOOL_SERVER_LABEL, serverLabel);
                                if (toolMap.get("server_url") instanceof String serverUrl) put(MCPTOOL_SERVER_URL, serverUrl);
                                Object allowedToolsObj = toolMap.get("allowed_tools");
                                if (allowedToolsObj instanceof List<?> allowedToolList) {
                                    put(MCPTOOL_ALLOWED_TOOLS, (List<String>) allowedToolList);
                                } else if (allowedToolsObj instanceof Map<?, ?> allowedToolMap) {
                                    put(MCPTOOL_ALLOWED_TOOLS_FILTER, (Map<String, Object>) allowedToolMap);
                                }
                                if (toolMap.get("headers") instanceof Map<?, ?> headersMap) {
                                    put(MCPTOOL_HEADERS, (Map<String, Object>) headersMap);
                                }
                                Object approvalObj = toolMap.get("require_approval");
                                if (approvalObj instanceof String approvalSetting) {
                                    put(MCPTOOL_REQUIRE_APPROVAL_MODE, approvalSetting);
                                } else if (approvalObj instanceof Map<?, ?> approvalMap) {
                                    Object always = approvalMap.get("always");
                                    Object never = approvalMap.get("never");
    
                                    if (always instanceof Map<?, ?> alwaysMap) {
                                        put(MCPTOOL_REQUIRE_APPROVAL_ALWAYS, (Map<String, Object>) alwaysMap);
                                    }
                                    if (never instanceof Map<?, ?> neverMap) {
                                        put(MCPTOOL_REQUIRE_APPROVAL_NEVER, (Map<String, Object>) neverMap);
                                    }
                                }
                            }
                            case "code_interpreter" -> {
                                put(CODEINTERPRETERTOOL_TYPE, type);
                                Object containerObj = toolMap.get("container");
                                if (containerObj instanceof String containerId) {
                                    put(CODEINTERPRETERTOOL_CONTAINER_ID, containerId);
                                } else if (containerObj instanceof Map<?, ?> containerMap) {
                                    put(CODEINTERPRETERTOOL_CONTAINER_MAP, (Map<String, Object>) containerMap);
                                }
                            }
                            case "local_shell" -> {
                                put(LOCALSHELLTOOL_TYPE, type);
                                if (outputObj instanceof List<?> outputList) {
                                    for (Object outputItemObj : outputList) {
                                        if (!(outputItemObj instanceof Map<?, ?> outputItem)) continue;
                                        Object actionObj = outputItem.get("action");
                                        if (actionObj instanceof Map<?, ?> action) {
                                            Object cmdObj = action.get("command");
                                            if (cmdObj instanceof List<?> cmdList) {
                                                List<String> commands = cmdList.stream()
                                                    .map(Object::toString)
                                                    .toList();
                                                put(LOCALSHELLTOOL_COMMANDS, commands);
                                            } else if (cmdObj instanceof String singleCommand) {
                                                put(LOCALSHELLTOOL_COMMANDS, List.of(singleCommand));
                                            } else if (cmdObj != null) {
                                                put(LOCALSHELLTOOL_COMMANDS, List.of(cmdObj.toString()));
                                            }
                                        }
                
                                    }
                                }
                            }
                            default -> {
                            }
                        }
                    }
                }
                Object toolChoiceObj = responseMap.get("tool_choice");
                if (toolChoiceObj instanceof Map<?, ?> toolChoice) {
                    Object modeObj = toolChoice.get("mode");
                    if (modeObj instanceof String mode) {
                        put(TOOLCHOICE_MODE, mode);
                    }
                    Object toolObj = toolChoice.get("tool");
                    if (toolObj instanceof String tool) {
                        put(TOOLCHOICE_TOOL, tool);
                    }
                    Object indexObj = toolChoice.get("index");
                    if (indexObj instanceof Number idx) {
                        put(TOOLCHOICE_INDEX, idx.intValue());
                    }
                    Object argumentsObj = toolChoice.get("arguments");
                    if (argumentsObj instanceof Map<?, ?> argsMap) {
                        put(TOOLCHOICE_ARGUMENTS, (Map<String, Object>) argsMap);
                    }
                }
                MetadataKey<String> completionFinishReasonKey = new MetadataKey<>("finish_reason", Metadata.STRING);
                String completionFinishReason = (String) completionChoice.get("finish_reason");
                put(completionFinishReasonKey, completionFinishReason);
                MetadataKey<Integer> completionIndexKey = new MetadataKey<>("index", Metadata.INTEGER);
                Integer completionIndex = (Integer) completionChoice.get("index");
                put(completionIndexKey, completionIndex);
            }
            Map<String, Integer> completionUsage = (Map<String, Integer>) responseMap.get("usage");
            /*
             *  Null checks
             */
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
        else if (requestId.contains("models")) {
            MetadataKey<String> objectKey = new MetadataKey<>("object", Metadata.STRING);
            String requestObject = (String) responseMap.get("object");
            /*
             *  Null checks
             */
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
        } else if (requestId.startsWith("resp_")) {
            MetadataKey<String> responsesObjectKey = new MetadataKey<>("object", Metadata.STRING);
            /*
             *  Null checks
             */
            String responsesObject = responseMap != null ? (String) responseMap.get("object") : null;
            if (responsesObject != null) {
                put(responsesObjectKey, responsesObject);
            }

            MetadataKey<Integer> responsesCreatedAtKey = new MetadataKey<>("created_at", Metadata.INTEGER);
            /*
             *  Null checks
             */
            Integer responsesCreatedAt = responseMap != null ? (Integer) responseMap.get("created_at") : null;
            if (responsesCreatedAt != null) {
                put(responsesCreatedAtKey, responsesCreatedAt);
            }

            MetadataKey<String> responsesStatusKey = new MetadataKey<>("status", Metadata.STRING);
            /*
             *  Null checks
             */
            String responsesStatus = responseMap != null ? (String) responseMap.get("status") : null;
            if (responsesStatus != null) {
                put(responsesStatusKey, responsesStatus);
            }

            MetadataKey<String> responsesErrorKey = new MetadataKey<>("error", Metadata.STRING);
            /*
             *  Null checks
             */
            String responsesError = responseMap != null ? (String) responseMap.get("error") : null;
            if (responsesError != null) {
                put(responsesErrorKey, responsesError);
            }

            MetadataKey<String> responsesIncompleteDetailsReasonKey = new MetadataKey<>("reason", Metadata.STRING);
            /*
             *  Null checks
             */
            Map<String, String> responsesIncompleteDetails = responseMap != null ? (Map<String, String>) responseMap.get("incomplete_details") : null;
            String reason = (responsesIncompleteDetails != null) ? responsesIncompleteDetails.get("reason") : null;
            if (reason != null) {
                put(responsesIncompleteDetailsReasonKey, reason);
            }

            MetadataKey<String> responsesInstructionsKey = new MetadataKey<>("instructions", Metadata.STRING);
            /*
             *  Null checks
             */
            String responsesInstructions = responseMap != null ? (String) responseMap.get("instructions") : null;
            if (responsesInstructions != null) {
                put(responsesInstructionsKey, responsesInstructions);
            }

            MetadataKey<Integer> responsesMaxOutputTokensKey = new MetadataKey<>("max_output_tokens", Metadata.INTEGER);
            /*
             *  Null checks
             */
            Integer responsesMaxOutputTokens = responseMap != null ? (Integer) responseMap.get("max_output_tokens") : null;
            if (responsesMaxOutputTokens != null) {
                put(responsesMaxOutputTokensKey, responsesMaxOutputTokens);
            }

            MetadataKey<String> responsesModelKey = new MetadataKey<>("model", Metadata.STRING);
            /*
             *  Null checks
             */
            String responsesModel = responseMap != null ? (String) responseMap.get("model") : null;
            if (responsesModel != null) {
                put(responsesModelKey, responsesModel);
            }

            MetadataKey<Boolean> responsesParallelToolCallsKey = new MetadataKey<>("parallel_tool_calls", Metadata.BOOLEAN);
            /*
             *  Null checks
             */
            Boolean responsesParallelToolCalls = responseMap != null ? (Boolean) responseMap.get("parallel_tool_calls") : null;
            if (responsesParallelToolCalls != null) {
                put(responsesParallelToolCallsKey, responsesParallelToolCalls);
            }

            MetadataKey<String> responsesPreviousResponseIdKey = new MetadataKey<>("previous_response_id", Metadata.STRING);
            /*
             *  Null checks
             */
            String responsesPreviousResponseId = responseMap != null ? (String) responseMap.get("previous_response_id") : null;
            if (responsesPreviousResponseId != null) {
                put(responsesPreviousResponseIdKey, responsesPreviousResponseId);
            }

            MetadataKey<String> responsesReasoningEffortKey = new MetadataKey<>("effort", Metadata.STRING);
            MetadataKey<String> responsesReasoningSummaryKey = new MetadataKey<>("summary", Metadata.STRING);
            /*
             *  Null checks
             */
            Map<String, String> responsesReasoning = responseMap != null ? (Map<String, String>) responseMap.get("reasoning") : null;
            if (responsesReasoning != null) {
                String responsesReasoningEffort = responsesReasoning.get("effort");
                if (responsesReasoningEffort != null) {
                    put(responsesReasoningEffortKey, responsesReasoningEffort);
                }
                String responsesReasoningSummary = responsesReasoning.get("summary");
                if (responsesReasoningSummary != null) {
                    put(responsesReasoningSummaryKey, responsesReasoningSummary);
                }
            }

            MetadataKey<Double> responsesTemperatureKey = new MetadataKey<>("temperature", Metadata.DOUBLE);
            /*
             *  Null checks
             */
            Double responsesTemperature = responseMap != null ? (Double) responseMap.get("temperature") : null;
            if (responsesTemperature != null) {
                put(responsesTemperatureKey, responsesTemperature);
            }

            MetadataKey<Map<String, Object>> responsesTextFormatKey = new MetadataKey<>("text_format", Metadata.MAP);
            /*
             *  Null checks
             */
            Map<String, Object> responsesTextFormat = responseMap != null ? (Map<String, Object>) responseMap.get("text") : null;
            if (responsesTextFormat != null) {
                put(responsesTextFormatKey, responsesTextFormat);
            }

            MetadataKey<Double> responsesTopPKey = new MetadataKey<>("top_p", Metadata.DOUBLE);
            /*
             *  Null checks
             */
            Double responsesTopP = responseMap != null ? (Double) responseMap.get("top_p") : null;
            if (responsesTopP != null) {
                put(responsesTopPKey, responsesTopP);
            }

            MetadataKey<String> responsesTruncationKey = new MetadataKey<>("truncation", Metadata.STRING);
            /*
             *  Null checks
             */
            String responsesTruncation = responseMap != null ? (String) responseMap.get("truncation") : null;
            if (responsesTruncation != null) {
                put(responsesTruncationKey, responsesTruncation);
            }

            MetadataKey<Integer> responsesTotalTokensKey = new MetadataKey<>("total_tokens", Metadata.INTEGER);
            /*
             *  Null checks
             */
            Map<String, Object> responsesUsage = responseMap != null ? (Map<String, Object>) responseMap.get("usage") : null;
            if (responsesUsage != null) {
                Integer responsesTotalTokens = (Integer) responsesUsage.get("total_tokens");
                if (responsesTotalTokens != null) {
                    put(responsesTotalTokensKey, responsesTotalTokens);
                }
            }
            MetadataKey<String> responsesUserKey = new MetadataKey<>("user", Metadata.STRING);
            String responsesUser = responseMap != null ? (String) responseMap.get("user") : null;
            /*
             *  Null checks
             */
            if (responsesUser != null) {
                put(responsesUserKey, responsesUser);
            }
            MetadataKey<Map<String, Object>> responsesMetadataKey = new MetadataKey<>("metadata", Metadata.MAP);
            Map<String, Object> responsesMetadata = responseMap != null ? (Map<String, Object>) responseMap.get("metadata") : null;
            Boolean localShellFinished = (responsesMetadata != null) ? (Boolean) responsesMetadata.get("local_shell_command_sequence_finished") : null;
            /*
             *  Null checks
             */
            if (responsesMetadata != null) {
                put(responsesMetadataKey, responsesMetadata);
            }
            if (localShellFinished != null) {
                put(LOCALSHELLTOOL_FINISHED, localShellFinished);
            }
            MetadataKey<String> responsesOutputContentKey = new MetadataKey<>("output_content", Metadata.STRING);
            Object outputObj = responseMap != null ? responseMap.get("output") : null;
            List<String> allCallIds = new ArrayList<>();
            List<List<String>> allCommands = new ArrayList<>();
            if (outputObj instanceof List<?> outputList) {
                for (Object outputItemObj : outputList) {
                    if (!(outputItemObj instanceof Map<?, ?> outputItem)) continue;
                    Object contentObj = outputItem.get("content");
                    if (contentObj instanceof List<?> contentList) {
                        for (Object contentItemObj : contentList) {
                            if (!(contentItemObj instanceof Map<?, ?> contentItem)) continue;
                            Object textObj = contentItem.get("text");
                            if (textObj instanceof String textStr && !textStr.isBlank()) {
                                put(responsesOutputContentKey, textStr);
                                break;
                            }
                        }
                    }
                }
            }
            MetadataKey<List<Map<String, Object>>> toolsKey = new MetadataKey<>("tools", Metadata.LIST_MAP);
            Object toolsObj = responseMap.get("tools");
            if (toolsObj instanceof List<?> toolsListRaw) {
                List<Map<String, Object>> toolsList = new ArrayList<>();
                for (Object toolObj : toolsListRaw) {
                    if (toolObj instanceof Map<?, ?> toolMapRaw) {
                        toolsList.add((Map<String, Object>) toolMapRaw);
                    }
                }
                put(toolsKey, toolsList);
                for (Map<String, Object> toolMap : toolsList) {
                    if (toolMap == null) continue;
                    Object typeObj = toolMap.get("type");
                    if (!(typeObj instanceof String type)) continue;
                    switch (type) {
                        case "file_search" -> {
                            put(FILESEARCHTOOL_TYPE, "file_search");
                            put(FILESEARCHTOOL_VECTOR_STORE_IDS, List.of("file-PLACEHOLDER"));
                            Object filtersObj = toolMap.get("filters");
                            if (filtersObj instanceof Map<?, ?> filterMap) {
                                Map<String, Object> filterMapCasted = (Map<String, Object>) filterMap;
                                put(FILESEARCHTOOL_FILTERS, filterMapCasted);
                                if (filterMapCasted.containsKey("key") && filterMapCasted.containsKey("type") && filterMapCasted.containsKey("value")) {
                                    put(FILESEARCHTOOL_FILTER_COMPARISON, filterMapCasted);
                                }
                                Object filterType = filterMapCasted.get("type");
                                if ("and".equals(filterType) || "or".equals(filterType)) {
                                    put(FILESEARCHTOOL_FILTER_COMPOUND, filterMapCasted);
                                    Object subFilters = filterMapCasted.get("filters");
                                    if (subFilters instanceof List<?> subList) {
                                        List<Map<String, Object>> casted = new ArrayList<>();
                                        for (Object subFilterObj : subList) {
                                            if (subFilterObj instanceof Map<?, ?> subFilterMap) {
                                                casted.add((Map<String, Object>) subFilterMap);
                                            }
                                        }
                                        put(FILESEARCHTOOL_FILTER_COMPOUND_LIST, casted);
                                    }
                                }
                            }
                            Object maxNumResults = toolMap.get("max_num_results");
                            if (maxNumResults instanceof Number num) {
                                put(FILESEARCHTOOL_MAX_NUM_RESULTS, num.intValue());
                            }
                            Object rankingOpts = toolMap.get("ranking_options");
                            if (rankingOpts instanceof Map<?, ?> rankingMap) {
                                put(FILESEARCHTOOL_RANKING_OPTIONS, (Map<String, Object>) rankingMap);
                            }
                        }
                        case "web_search_preview", "web_search_preview_2025_03_11" -> {
                            put(WEBSEARCHTOOL_TYPE, type);
                            Object searchContextSize = toolMap.get("search_context_size");
                            if (searchContextSize instanceof String size) {
                                put(WEBSEARCHTOOL_CONTEXT_SIZE, size);
                            }
                            Object userLocObj = toolMap.get("user_location");
                            if (userLocObj instanceof Map<?, ?> loc) {
                                Map<String, Object> locMap = (Map<String, Object>) loc;
                                Object locTypeObj = locMap.get("type");
                                if (locTypeObj instanceof String locType) put(WEBSEARCHTOOL_LOCATION_TYPE, locType);
                                Object cityObj = locMap.get("city");
                                if (cityObj instanceof String city) put(WEBSEARCHTOOL_LOCATION_CITY, city);
                                Object countryObj = locMap.get("country");
                                if (countryObj instanceof String country) put(WEBSEARCHTOOL_LOCATION_COUNTRY, country);
                                Object regionObj = locMap.get("region");
                                if (regionObj instanceof String region) put(WEBSEARCHTOOL_LOCATION_REGION, region);
                                Object timezoneObj = locMap.get("timezone");
                                if (timezoneObj instanceof String tz) put(WEBSEARCHTOOL_LOCATION_TIMEZONE, tz);
                            }
                        }
                        case "computer_use_preview" -> {
                            put(COMPUTERTOOL_TYPE, type);
                            Object heightObj = toolMap.get("display_height");
                            if (heightObj instanceof Number height) put(COMPUTERTOOL_DISPLAY_HEIGHT, height.intValue());
                            Object widthObj = toolMap.get("display_width");
                            if (widthObj instanceof Number width) put(COMPUTERTOOL_DISPLAY_WIDTH, width.intValue());
                            Object envObj = toolMap.get("environment");
                            if (envObj instanceof String env) put(COMPUTERTOOL_ENVIRONMENT, env);
                        }
                        case "mcp" -> {
                            put(MCPTOOL_TYPE, type);
                            Object serverLabelObj = toolMap.get("server_label");
                            if (serverLabelObj instanceof String serverLabel) put(MCPTOOL_SERVER_LABEL, serverLabel);
                            Object serverUrlObj = toolMap.get("server_url");
                            if (serverUrlObj instanceof String serverUrl) put(MCPTOOL_SERVER_URL, serverUrl);
            
                            Object allowedToolsObj = toolMap.get("allowed_tools");
                            if (allowedToolsObj instanceof List<?> allowedToolList) {
                                put(MCPTOOL_ALLOWED_TOOLS, (List<String>) allowedToolList);
                            } else if (allowedToolsObj instanceof Map<?, ?> allowedToolMap) {
                                put(MCPTOOL_ALLOWED_TOOLS_FILTER, (Map<String, Object>) allowedToolMap);
                            }
                            Object headersObj = toolMap.get("headers");
                            if (headersObj instanceof Map<?, ?> headersMap) {
                                put(MCPTOOL_HEADERS, (Map<String, Object>) headersMap);
                            }
                            Object approvalObj = toolMap.get("require_approval");
                            if (approvalObj instanceof String approvalSetting) {
                                put(MCPTOOL_REQUIRE_APPROVAL_MODE, approvalSetting);
                            } else if (approvalObj instanceof Map<?, ?> approvalMap) {
                                Map<String, Object> approvalMapCasted = (Map<String, Object>) approvalMap;
                                Object always = approvalMapCasted.get("always");
                                Object never = approvalMapCasted.get("never");
            
                                if (always instanceof Map<?, ?> alwaysMap) {
                                    put(MCPTOOL_REQUIRE_APPROVAL_ALWAYS, (Map<String, Object>) alwaysMap);
                                }
                                if (never instanceof Map<?, ?> neverMap) {
                                    put(MCPTOOL_REQUIRE_APPROVAL_NEVER, (Map<String, Object>) neverMap);
                                }
                            }
                        }
            
                        case "code_interpreter" -> {
                            put(CODEINTERPRETERTOOL_TYPE, type);
                            Object containerObj = toolMap.get("container");
                            if (containerObj instanceof String containerId) {
                                put(CODEINTERPRETERTOOL_CONTAINER_ID, containerId);
                            } else if (containerObj instanceof Map<?, ?> containerMap) {
                                put(CODEINTERPRETERTOOL_CONTAINER_MAP, (Map<String, Object>) containerMap);
                            }
                        }
            
                        case "local_shell" -> {
                            put(LOCALSHELLTOOL_TYPE, type);
            
                            if (outputObj instanceof List<?> outputList) {
                                for (Object outputItemObj : outputList) {
                                    if (!(outputItemObj instanceof Map<?, ?> outputItem)) continue;
            
                                    Object actionObj = outputItem.get("action");
                                    if (actionObj instanceof Map<?, ?> action) {
                                        Object cmdObj = action.get("command");
                                        List<String> commands = new ArrayList<>();
            
                                        if (cmdObj instanceof List<?> cmdList) {
                                            // flatten not allowed, so treat list as separate commands
                                            commands = cmdList.stream()
                                                .map(Object::toString)
                                                .toList();
                                        } else if (cmdObj instanceof String singleCommand) {
                                            commands = Arrays.stream(singleCommand.split("&&"))
                                                .map(String::trim)
                                                .filter(s -> !s.isEmpty())
                                                .toList();
                                        } else if (cmdObj != null) {
                                            commands = Arrays.stream(cmdObj.toString().split("&&"))
                                                .map(String::trim)
                                                .filter(s -> !s.isEmpty())
                                                .toList();
                                        }
            
                                        put(LOCALSHELLTOOL_COMMANDS, commands);
                                    }
                                }
                            }
                        }
            
                        default -> {
                            // no-op
                        }
                    }
                }
            }
            
            Object toolChoiceObj = responseMap.get("tool_choice");
            if (toolChoiceObj instanceof Map<?, ?> toolChoice) {
                Object modeObj = toolChoice.get("mode");
                if (modeObj instanceof String mode) {
                    put(TOOLCHOICE_MODE, mode);
                }
                Object toolObj = toolChoice.get("tool");
                if (toolObj instanceof String tool) {
                    put(TOOLCHOICE_TOOL, tool);
                }
                Object indexObj = toolChoice.get("index");
                if (indexObj instanceof Number idx) {
                    put(TOOLCHOICE_INDEX, idx.intValue());
                }
                Object argumentsObj = toolChoice.get("arguments");
                if (argumentsObj instanceof Map<?, ?> argsMap) {
                    put(TOOLCHOICE_ARGUMENTS, (Map<String, Object>) argsMap);
                }
            }
        } else if (requestId.contains("modr")) {
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
                /*
                 *  Null checks
                 */
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
                /*
                 *  Null checks
                 */
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
        } else {
            List<Map<String, Object>> completionChoices = (List<Map<String, Object>>) responseMap.get("choices");
            if (completionChoices == null || completionChoices.isEmpty()) return;
            Map<String, Object> completionChoice = completionChoices.get(0);
            Map<String, Object> completionMessage = (Map<String, Object>) completionChoice.get("message");
            if (completionMessage != null) {
                String completionContent = completionMessage != null ? (String) completionMessage.get("content") : null;
                MetadataKey<String> completionContentKey = new MetadataKey<>("content", Metadata.STRING);
                put(completionContentKey, completionContent);
            }
            String finishReason = (String) completionChoice.get("finish_reason");

            // Store finish_reason
            if (finishReason != null) {
                MetadataKey<String> finishKey = new MetadataKey<>("finish_reason", Metadata.STRING);
                put(finishKey, finishReason);
            }
            MetadataKey<Integer> tokenCountKey = new MetadataKey<>("token_count", Metadata.INTEGER);
            Map<String, Object> usage = (Map<String, Object>) responseMap.get("usage");
            if (usage != null && usage.get("total_tokens") instanceof Number) {
                Integer totalTokens =  ((Number) usage.get("total_tokens")).intValue();
                put(tokenCountKey, totalTokens);
            }
            List<Map<String, Object>> toolCalls = (List<Map<String, Object>>) completionMessage.get("tool_calls");
            if (toolCalls == null) return;

            for (Map<String, Object> toolCall : toolCalls) {
                Map<String, Object> function = (Map<String, Object>) toolCall.get("function");
                if (function == null) continue;

                String functionName = (String) function.get("name");
                MetadataKey<String> functionNameKey = new MetadataKey<>("name", Metadata.STRING);
                put(functionNameKey, functionName);
                String argumentsJson = (String) function.get("arguments");
                ObjectMapper mapper = new ObjectMapper();
                try {
                    Map<String, Object> argumentsMap = mapper.readValue(argumentsJson, new TypeReference<Map<String, Object>>() {});
                    
                    MetadataKey<Map<String, Object>> argumentsMapKey = new MetadataKey<>("arguments", Metadata.MAP);
                    put(argumentsMapKey, argumentsMap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (completionMessage != null) {
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
                MetadataKey<Double> responsesTopPKey = new MetadataKey<>("top_p", Metadata.DOUBLE);                              // MUST MATCH THE RESP OBJECT         VALUE
                Double responsesTopP = (Double) responseMap.get("top_p");                                                        // DO NOT CHANGE TO INTEGER if     NON OPENAI,     CAUSES ERROR
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
            }
        }
    }
}
