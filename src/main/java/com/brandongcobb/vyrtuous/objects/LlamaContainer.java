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

import com.brandongcobb.vyrtuous.utils.handlers.*;
import com.brandongcobb.metadata.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

public class LlamaContainer extends MainContainer {
    
    private ToolHandler th = new ToolHandler();
    
    public LlamaContainer(Map<String, Object> responseMap) {
        
        MetadataKey<String> idKey = new MetadataKey<>("id", Metadata.STRING);
        MetadataKey<Integer> tokenCountKey = new MetadataKey<>("token_count", Metadata.INTEGER);
        Map<String, Object> usage = (Map<String, Object>) responseMap.get("usage");
        if (usage != null && usage.get("total_tokens") instanceof Number) {
            Integer totalTokens =  ((Number) usage.get("total_tokens")).intValue();
            put(tokenCountKey, totalTokens);
        }
        //Integer tokensPredicted = (Integer) responseMap.get("tokens_predicted");
         //Evaluated + tokensPredicted);
            
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
            if (contentObj instanceof List<?> contentList) {
                for (Object contentItemObj : contentList) {
                    if (!(contentItemObj instanceof Map<?, ?> contentItem)) continue;
                    Object typeObj = contentItem.get("type");
                    if ("tool_call".equals(typeObj)) {
                        Object callIdObj = contentItem.get("call_id");
                        //if (callIdObj instanceof String callId) {
                        //    put(th.LOCALSHELLTOOL_CALL_ID, callId);
                        //}
                        Object actionObj = contentItem.get("action");
                        if (actionObj instanceof Map<?, ?> action) {
                            Object cmdObj = action.get("command");
                            if (cmdObj instanceof List<?> cmdList) {
                                List<String> commands = cmdList.stream()
                                    .map(Object::toString)
                                    .toList();

                                // Heuristic: if there's only one string, or if any entry contains an operator or semicolon, treat it as a full command string
                                boolean containsShellOperators = commands.stream().anyMatch(s ->
                                    s.matches(".*[;&|><(){}].*")
                                );

                                if (commands.size() == 1 || containsShellOperators) {
                                    // Treat as a single shell command string
                                    String fullCommand = String.join(" ", commands);
                                    put(th.LOCALSHELLTOOL_COMMANDS, List.of(fullCommand));
                                } else {
                                    // Treat as separate commands (e.g., ["ls", "-la"])
                                    put(th.LOCALSHELLTOOL_COMMANDS, commands);
                                }
                            } else if (cmdObj instanceof String singleCommand) {
                                put(th.LOCALSHELLTOOL_COMMANDS, List.of(singleCommand));
                            } else if (cmdObj != null) {
                                put(th.LOCALSHELLTOOL_COMMANDS, List.of(cmdObj.toString()));
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
                    Object typeObj = toolMap.get("type");
                    if (!(typeObj instanceof String type)) continue;
                    switch (type) {
                        case "file_search" -> {
                            put(th.FILESEARCHTOOL_TYPE, "file_search");
                            put(th.FILESEARCHTOOL_VECTOR_STORE_IDS, List.of("file-PLACEHOLDER"));
                            Object filtersObj = toolMap.get("filters");
                            if (filtersObj instanceof Map<?, ?> filterMap) {
                                put(th.FILESEARCHTOOL_FILTERS, (Map<String, Object>) filterMap);
                                if (filterMap.containsKey("key") && filterMap.containsKey("type") && filterMap.containsKey("value")) {
                                    put(th.FILESEARCHTOOL_FILTER_COMPARISON, (Map<String, Object>) filterMap);
                                }
                                if ("and".equals(filterMap.get("type")) || "or".equals(filterMap.get("type"))) {
                                    put(th.FILESEARCHTOOL_FILTER_COMPOUND, (Map<String, Object>) filterMap);
                                    Object subFilters = filterMap.get("filters");
                                    if (subFilters instanceof List<?> subList) {
                                        List<Map<String, Object>> casted = new ArrayList<>();
                                        for (Object subFilterObj : subList) {
                                            if (subFilterObj instanceof Map<?, ?> subFilterMap) {
                                                casted.add((Map<String, Object>) subFilterMap);
                                            }
                                        }
                                        put(th.FILESEARCHTOOL_FILTER_COMPOUND_LIST, casted);
                                    }
                                }
                            }
                            Object maxNumResults = toolMap.get("max_num_results");
                            if (maxNumResults instanceof Number num) {
                                put(th.FILESEARCHTOOL_MAX_NUM_RESULTS, num.intValue());
                            }
                            Object rankingOpts = toolMap.get("ranking_options");
                            if (rankingOpts instanceof Map<?, ?> rankingMap) {
                                put(th.FILESEARCHTOOL_RANKING_OPTIONS, (Map<String, Object>) rankingMap);
                            }
                        }
                        case "web_search_preview", "web_search_preview_2025_03_11" -> {
                            put(th.WEBSEARCHTOOL_TYPE, type);
                            Object searchContextSize = toolMap.get("search_context_size");
                            if (searchContextSize instanceof String size) {
                                put(th.WEBSEARCHTOOL_CONTEXT_SIZE, size);
                            }
                            Object userLocObj = toolMap.get("user_location");
                            if (userLocObj instanceof Map<?, ?> loc) {
                                if (loc.get("type") instanceof String locType) put(th.WEBSEARCHTOOL_LOCATION_TYPE, locType);
                                if (loc.get("city") instanceof String city) put(th.WEBSEARCHTOOL_LOCATION_CITY, city);
                                if (loc.get("country") instanceof String country) put(th.WEBSEARCHTOOL_LOCATION_COUNTRY, country);
                                if (loc.get("region") instanceof String region) put(th.WEBSEARCHTOOL_LOCATION_REGION, region);
                                if (loc.get("timezone") instanceof String tz) put(th.WEBSEARCHTOOL_LOCATION_TIMEZONE, tz);
                            }
                        }
                        case "computer_use_preview" -> {
                            put(th.COMPUTERTOOL_TYPE, type);
                            if (toolMap.get("display_height") instanceof Number height) put(th.COMPUTERTOOL_DISPLAY_HEIGHT, height.intValue());
                            if (toolMap.get("display_width") instanceof Number width) put(th.COMPUTERTOOL_DISPLAY_WIDTH, width.intValue());
                            if (toolMap.get("environment") instanceof String env) put(th.COMPUTERTOOL_ENVIRONMENT, env);
                        }
                        case "mcp" -> {
                            put(th.MCPTOOL_TYPE, type);
                            if (toolMap.get("server_label") instanceof String serverLabel) put(th.MCPTOOL_SERVER_LABEL, serverLabel);
                            if (toolMap.get("server_url") instanceof String serverUrl) put(th.MCPTOOL_SERVER_URL, serverUrl);
                            Object allowedToolsObj = toolMap.get("allowed_tools");
                            if (allowedToolsObj instanceof List<?> allowedToolList) {
                                put(th.MCPTOOL_ALLOWED_TOOLS, (List<String>) allowedToolList);
                            } else if (allowedToolsObj instanceof Map<?, ?> allowedToolMap) {
                                put(th.MCPTOOL_ALLOWED_TOOLS_FILTER, (Map<String, Object>) allowedToolMap);
                            }
                            if (toolMap.get("headers") instanceof Map<?, ?> headersMap) {
                                put(th.MCPTOOL_HEADERS, (Map<String, Object>) headersMap);
                            }
                            Object approvalObj = toolMap.get("require_approval");
                            if (approvalObj instanceof String approvalSetting) {
                                put(th.MCPTOOL_REQUIRE_APPROVAL_MODE, approvalSetting);
                            } else if (approvalObj instanceof Map<?, ?> approvalMap) {
                                Object always = approvalMap.get("always");
                                Object never = approvalMap.get("never");

                                if (always instanceof Map<?, ?> alwaysMap) {
                                    put(th.MCPTOOL_REQUIRE_APPROVAL_ALWAYS, (Map<String, Object>) alwaysMap);
                                }
                                if (never instanceof Map<?, ?> neverMap) {
                                    put(th.MCPTOOL_REQUIRE_APPROVAL_NEVER, (Map<String, Object>) neverMap);
                                }
                            }
                        }
                        case "code_interpreter" -> {
                            put(th.CODEINTERPRETERTOOL_TYPE, type);
                            Object containerObj = toolMap.get("container");
                            if (containerObj instanceof String containerId) {
                                put(th.CODEINTERPRETERTOOL_CONTAINER_ID, containerId);
                            } else if (containerObj instanceof Map<?, ?> containerMap) {
                                put(th.CODEINTERPRETERTOOL_CONTAINER_MAP, (Map<String, Object>) containerMap);
                            }
                        }
                        case "local_shell" -> {
                            put(th.LOCALSHELLTOOL_TYPE, type);
                            if (contentObj instanceof List<?> contentList) {
                                for (Object contentItemObj : contentList) {
                                    if (!(contentItemObj instanceof Map<?, ?> contentItem)) continue;
                                    Object actionObj = contentItem.get("action");
                                    if (actionObj instanceof Map<?, ?> action) {
                                        Object cmdObj = action.get("command");
                                        if (cmdObj instanceof List<?> cmdList) {
                                            List<String> commands = cmdList.stream()
                                                .map(Object::toString)
                                                .toList(); // Java 16+, otherwise use .collect(th.Collectors.toList())
                                            put(th.LOCALSHELLTOOL_COMMANDS, commands);
                                        } else if (cmdObj instanceof String singleCommand) {
                                            put(th.LOCALSHELLTOOL_COMMANDS, List.of(singleCommand));
                                        } else if (cmdObj != null) {
                                            put(th.LOCALSHELLTOOL_COMMANDS, List.of(cmdObj.toString()));
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
                    put(th.TOOLCHOICE_MODE, mode);
                }
                Object toolObj = toolChoice.get("tool");
                if (toolObj instanceof String tool) {
                    put(th.TOOLCHOICE_TOOL, tool);
                }
                Object indexObj = toolChoice.get("index");
                if (indexObj instanceof Number idx) {
                    put(th.TOOLCHOICE_INDEX, idx.intValue());
                }
                Object argumentsObj = toolChoice.get("arguments");
                if (argumentsObj instanceof Map<?, ?> argsMap) {
                    put(th.TOOLCHOICE_ARGUMENTS, (Map<String, Object>) argsMap);
                }
            } else if (contentObj instanceof List<?> contentList) {
                StringBuilder sb = new StringBuilder();
                for (Object part : contentList) {
                    if (!(part instanceof Map<?, ?> mapPart)) continue;
                    Object textObj = mapPart.get("text");
                    if (textObj instanceof String text) {
                        sb.append(text);
                    }
                }
                put(contentKey, sb.toString());
            }
        }
    }
}
