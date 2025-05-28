//
//  ToolHandler.java
//
//
//  Created by Brandon Cobb on 5/22/25.
//
package com.brandongcobb.vyrtuous.utils.handlers;
import com.brandongcobb.metadata.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;

public class ToolHandler {

    // Example metadata key for shell command string
    public static final MetadataKey<String> LOCALSHELLTOOL_COMMAND = new MetadataKey<>("localshelltool_command", Metadata.STRING);
    public static final MetadataKey<String> FILESEARCHTOOL_TYPE = new MetadataKey<>("filesearchtool_type", Metadata.STRING);
    public static final MetadataKey<List<String>> FILESEARCHTOOL_VECTOR_STORE_IDS = new MetadataKey<>("filesearchtool_vector_store_ids", Metadata.LIST_STRING);
    public static final MetadataKey<Map<String, Object>> FILESEARCHTOOL_FILTERS = new MetadataKey<>("filesearchtool_filters", Metadata.MAP);
    public static final MetadataKey<Integer> FILESEARCHTOOL_MAX_NUM_RESULTS = new MetadataKey<>("filesearchtool_max_num_results", Metadata.INTEGER);
    public static final MetadataKey<Map<String, Object>> FILESEARCHTOOL_RANKING_OPTIONS = new MetadataKey<>("filesearchtool_ranking_options", Metadata.MAP);
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
    // Metadata key for shell call identifier
    public static final MetadataKey<String> LOCALSHELLTOOL_CALL_ID = new MetadataKey<>("localshelltool_call_id", Metadata.STRING);


    public String executeShellCommand(ResponseObject responseObject) {
        String originalCommand = responseObject.get(LOCALSHELLTOOL_COMMAND);
        if (originalCommand == null || originalCommand.isBlank()) return "⚠️ No shell command provided.";

        try {
            // Diagnostic log
            System.out.println("🔧 Original command: " + originalCommand);

            // Execute the original command via bash login shell
            String raw = originalCommand.trim();
            String cmd;
            if (raw.startsWith("bash -lc ")) {
                cmd = raw.substring("bash -lc ".length()).trim();
            } else {
                cmd = raw;
            }
            System.out.println("🛠️ Executing shell command via bash -lc: " + cmd);
            ProcessBuilder builder = new ProcessBuilder("gtimeout", "5s", "bash", "-lc", cmd);
            // Redirect stderr to stdout
            builder.redirectErrorStream(true);
            // Prevent the subprocess from blocking on stdin (e.g., sed reading stdin) by redirecting to /dev/null
            builder.redirectInput(new File("/dev/null"));
            Process process = builder.start();

            StringBuilder outputBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                    outputBuilder.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return outputBuilder.toString();
            } else {
                return "❌ Shell command exited with code " + exitCode + ":\n" + outputBuilder;
            }

        } catch (Exception e) {
            return "⚠️ Shell execution failed: " + e.getMessage();
        }
    }
    
    public static List<String> executeFileSearch(ResponseObject responseObject, String query) {
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
            // perform vector store search; uses stub searchVectorStore in AIManager
            List<String> results = aim.searchVectorStore(storeId, query, maxResults, filters, rankingOptions);
            if (results != null) allResults.addAll(results);
        }
        return allResults;
    }
}
