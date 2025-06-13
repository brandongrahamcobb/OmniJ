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

import com.brandongcobb.vyrtuous.Vyrtuous;
import com.brandongcobb.metadata.*;
import com.brandongcobb.vyrtuous.utils.inc.*;
import java.util.AbstractMap.SimpleEntry;

import java.util.AbstractMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.AbstractMap;

import java.util.stream.Collectors;


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

    private void drainStream(InputStream inputStream) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                while (reader.readLine() != null) { /* discard or collect output */ }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private String escapeCommandParts(String s) {
        if (s.startsWith("//'") && s.endsWith("//'")) {
            // Extract inner content
            String inner = s.substring(3, s.length() - 3);

            // Escape for Java + shell
            StringBuilder escaped = new StringBuilder();
            for (char c : inner.toCharArray()) {
                switch (c) {
                    case '"':
                        escaped.append("\\\"");
                        break;
                    case '\'':
                        escaped.append("\\'");
                        break;
                    case '\\':
                        escaped.append("\\\\");
                        break;
                    case '{':
                        escaped.append("\\{");
                        break;
                    case '}':
                        escaped.append("\\}");
                        break;
                    case '$':
                        escaped.append("\\$");
                        break;
                    case '`':
                        escaped.append("\\`");
                        break;
                    case '|':
                        escaped.append("\\|");
                        break;
                    case '>':
                    case '<':
                    case '&':
                    case ';':
                        escaped.append("\\" + c);  // Shell metacharacters
                        break;
                    default:
                        escaped.append(c);
                }
            }

            return "'" + escaped.toString() + "'";
        } else {
            return s;
        }
    }



    private static final Pattern SAFE_TOKEN = Pattern.compile("^[a-zA-Z0-9/_\\-\\.]+$");
    private static final Pattern SHELL_SPECIALS = Pattern.compile("(?<!\\\\)([;{}()|])");
    private static final Set<String> SHELL_OPERATORS = Set.of("|", ";", "&&", "||", ">", "<", ">>");
    
    private AbstractMap.SimpleEntry<List<List<String>>, List<String>> splitIntoSegmentsAndOperators(List<List<String>> tokens) {
        List<List<String>> segments = new ArrayList<>();
        List<String> operators = new ArrayList<>();

        for (int i = 0; i < tokens.size(); i++) {
            List<String> part = tokens.get(i);
            if (part.size() == 1 && SHELL_OPERATORS.contains(part.get(0))) {
                if (segments.isEmpty()) {
                    throw new IllegalArgumentException("Command cannot start with operator: " + part.get(0));
                }
                operators.add(part.get(0));
            } else {
                segments.add(part);
                if (segments.size() > 1 && operators.size() < segments.size() - 1) {
                    operators.add(";");
                }
            }
        }
        if (segments.size() != operators.size() + 1) {
            throw new IllegalStateException("Segments and operators misaligned.");
        }

        return new AbstractMap.SimpleEntry<>(segments, operators);
    }
    
    private String expandHome(String path) {
        String home = System.getProperty("user.home");
        if (path.equals("~")) {
            return home;
        } else if (path.startsWith("~/")) {
            return home + path.substring(1);
        }
        return path;
    }
    
    
    public CompletableFuture<String> executeCommandsAsList(List<List<String>> allCommands) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        return CompletableFuture.supplyAsync(() -> {
            try {
                if (allCommands.isEmpty()) {
                    throw new IllegalArgumentException("No commands provided");
                }
                if (SHELL_OPERATORS.contains(allCommands.get(0).get(0))) {
                    throw new IllegalArgumentException("Command cannot start with operator");
                }
                if (SHELL_OPERATORS.contains(allCommands.get(allCommands.size()-1).get(0))) {
                    throw new IllegalArgumentException("Command cannot end with operator");
                }
                // Construct segments as List<List<String>> for executePipeline
                List<List<String>> segmentTokens = allCommands.stream()
                    .map(segment -> segment.stream()
                        .map(this::expandHome)// if you want to expand ~ in each token
                        .collect(Collectors.toList()))
                    .collect(Collectors.toList());
                AbstractMap.SimpleEntry<List<List<String>>, List<String>> split = splitIntoSegmentsAndOperators(segmentTokens);
                List<List<String>> segments = split.getKey();
                segments = segments.stream()
                    .map(segment -> segment.stream()
                        .map(token -> {
                                return escapeCommandParts(token); // leave quoted segments untouched
                        })
                        .collect(Collectors.toList()))
                    .collect(Collectors.toList());// Correct type: List<List<String>>
                List<String> operators = split.getValue();
                return executePipeline(segments, operators);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                executor.shutdown();
            }
        }, executor);
    }
    
    private String executePipeline(List<List<String>> segments, List<String> operators) throws Exception {
        List<Process> processes = new ArrayList<>();
        Process lastProcess = null;

        int i = 0;
        while (i < segments.size()) {
            
            List<String> segment = segments.get(i);
            String command = String.join(" ", segment);

            List<String> commandList = List.of("gtimeout", "20", "bash", "-c", command);
            ProcessBuilder pb = new ProcessBuilder(commandList);
            pb.redirectErrorStream(true);

            // Handle redirection operator that follows current segment
            if (i < operators.size()) {
                String op = operators.get(i);
                if (op.equals(">") || op.equals(">>")) {
                    if (i + 1 >= segments.size()) throw new IllegalArgumentException("Expected filename after " + op);
                    File outFile = new File(segments.get(i + 1).get(0));
                    pb.redirectOutput(op.equals(">") ?
                        ProcessBuilder.Redirect.to(outFile) :
                        ProcessBuilder.Redirect.appendTo(outFile));
                    i += 2;

                    Process proc = pb.start();
                    processes.add(proc);
                    lastProcess = proc;

                    drainStream(proc.getInputStream());
                    drainStream(proc.getErrorStream());
                    continue;
                } else if (op.equals("<")) {
                    if (i + 1 >= segments.size()) throw new IllegalArgumentException("Expected filename after <");
                    File inFile = new File(segments.get(i + 1).get(0));
                    pb.redirectInput(ProcessBuilder.Redirect.from(inFile));
                    i += 2;
                    Process proc = pb.start();
                    
                    if (i == segments.size() - 1) {
                        // Last process, read output later synchronously, just drain error
                        drainStream(proc.getErrorStream());
                    } else {
                        // Intermediate process, drain both to avoid blocking
                        drainStream(proc.getInputStream());
                        drainStream(proc.getErrorStream());
                    }
                    processes.add(proc);
                    lastProcess = proc;
                    continue;
                }
            }

            // Handle piping
            if (i > 0 && "|".equals(operators.get(i - 1))) {
                Process prevProc = processes.get(processes.size() - 1);
                pb.redirectInput(ProcessBuilder.Redirect.PIPE);
                Process currProc = pb.start();
                if (i == segments.size() - 1) {
                    // Last process, read output later synchronously, just drain error
                    drainStream(currProc.getErrorStream());
                } else {
                    // Intermediate process, drain both to avoid blocking
                    drainStream(currProc.getInputStream());
                    drainStream(currProc.getErrorStream());
                }

                InputStream prevOut = prevProc.getInputStream();
                OutputStream currIn = currProc.getOutputStream();

                Thread pipeThread = new Thread(() -> {
                    try (prevOut; currIn) {
                        pipeStreams(prevOut, currIn);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                pipeThread.start();

                processes.add(currProc);
                lastProcess = currProc;
            }
            // Handle conditionals: &&, ||
            else if (i > 0 && List.of("&&", "||").contains(operators.get(i - 1))) {
                Process prevProc = processes.get(processes.size() - 1);
                int prevExit = prevProc.waitFor();
                String op = operators.get(i - 1);
                if (("&&".equals(op) && prevExit != 0) || ("||".equals(op) && prevExit == 0)) {
                    break;
                }
                Process proc = pb.start();
                
                if (i == segments.size() - 1) {
                    // Last process, read output later synchronously, just drain error
                    drainStream(proc.getErrorStream());
                } else {
                    // Intermediate process, drain both to avoid blocking
                    drainStream(proc.getInputStream());
                    drainStream(proc.getErrorStream());
                }
                processes.add(proc);
                lastProcess = proc;
            }
            // Sequential (e.g. `;`) or first process
            else {
                Process proc = pb.start();
                
                if (i == segments.size() - 1) {
                    // Last process, read output later synchronously, just drain error
                    drainStream(proc.getErrorStream());
                } else {
                    // Intermediate process, drain both to avoid blocking
                    drainStream(proc.getInputStream());
                    drainStream(proc.getErrorStream());
                }
                processes.add(proc);
                lastProcess = proc;
            }

            i++;
        }

        if (lastProcess == null) throw new IllegalStateException("No process executed");

        // Read last process output
        StringBuilder fullOutput = new StringBuilder();

        for (int j = 0; j < processes.size(); j++) {
            Process proc = processes.get(j);
            boolean isRedirected = false;

            // Handle redirected output (e.g., > or >>)
            if (j < operators.size()) {
                String op = operators.get(j);
                if (op.equals(">") || op.equals(">>")) {
                    isRedirected = true;
                }
            }

            if (!isRedirected) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        fullOutput.append(line).append("\n");
                    }
                }
            }

            proc.waitFor(); // ensure process completes
        }

        return fullOutput.toString().trim();
    }


    private void pipeStreams(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[8192];
        int length;
        while ((length = input.read(buffer)) != -1) {
            output.write(buffer, 0, length);
            output.flush();
        }
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
