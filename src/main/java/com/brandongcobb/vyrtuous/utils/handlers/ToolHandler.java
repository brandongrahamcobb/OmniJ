/* ToolHandler.java The purpose of this class is to handle the tools.
 *
 * Copyright (C) 2025  github.com/brandongrahamcobb
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.brandongcobb.vyrtuous.utils.handlers;

import com.brandongcobb.metadata.*;
import com.brandongcobb.vyrtuous.utils.inc.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class ToolHandler {

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
    
    private static String readStream(InputStream stream) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
        }
        return builder.toString().trim();
    }

    private List<String> escapeCommandParts(List<String> commandParts) {
        return commandParts.stream()
            .map(s -> s.replace("*", "'*'")
                       .replace(";", "\\;")
                       .replace("&&", "\\&\\&")
                       .replace("(", "\\(")
                       .replace(")", "\\)")
                       .replace("{", "\\{")
                       .replace("}", "\\}")
                       .replace("|", "\\|"))
            .toList();
    }

    public CompletableFuture<String> executeCommandsAsList(List<List<String>> allCommands) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        return CompletableFuture.supplyAsync(() -> {
            StringBuilder result = new StringBuilder();
            for (List<String> commandParts : allCommands) {
                try {
                    List<String> processCommand = new ArrayList<>();
                    processCommand.add("gtimeout");
                    processCommand.add("20");
                    String joinedCommand = String.join(" ", commandParts);
                    processCommand.add("sh");
                    processCommand.add("-c");
                    processCommand.add(joinedCommand);
//                    processCommand.addAll(commandParts);
                    ProcessBuilder pb = new ProcessBuilder(processCommand);
                    pb.redirectErrorStream(true);
                    Process proc = pb.start();
                    String output = readStream(proc.getInputStream());
                    int exitCode = proc.waitFor();
                    result.append(String.join(" ", commandParts))
                          .append("\nExit code: ").append(exitCode)
                          .append("\nOutput:\n").append(output).append("\n\n");
                } catch (Exception e) {
                    result.append("Error running command ").append(commandParts)
                          .append(": ").append(e.getMessage()).append("\n");
                }
            }
            executor.shutdown();
            return result.toString();
        }, executor);
    }

    
    public static List<String> executeFileSearch(OpenAIContainer responseObject, String query) {
        String type = responseObject.get(FILESEARCHTOOL_TYPE);
        if (!"file_search".equals(type)) {
            System.out.println("⚠️ Tool type is not file_search.");
            return List.of();
        }
        List<String> vectorStoreIds = responseObject.get(FILESEARCHTOOL_VECTOR_STORE_IDS);
        Map<String, Object> filters = responseObject.get(FILESEARCHTOOL_FILTERS);
        Integer maxResults = responseObject.get(FILESEARCHTOOL_MAX_NUM_RESULTS);
        Map<String, Object> rankingOptions = responseObject.get(FILESEARCHTOOL_RANKING_OPTIONS);
        if (vectorStoreIds == null || vectorStoreIds.isEmpty()) {
            System.err.println("❌ No vector store IDs provided.");
            return List.of();
        }
        AIManager aim = new AIManager();
        List<String> allResults = new ArrayList<>();
        for (String storeId : vectorStoreIds) {
            List<String> results = aim.searchVectorStore(storeId, query, maxResults, filters, rankingOptions);
            if (results != null) allResults.addAll(results);
        }
        return allResults;
    }
}
