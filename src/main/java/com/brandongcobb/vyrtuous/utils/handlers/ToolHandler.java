/* ToolHandler.java The purpose of this class is to employ tools.
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
import com.brandongcobb.vyrtuous.objects.*;
import java.util.AbstractMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.jline.reader.*;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.TerminalBuilder;

// call once, reuse:





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
    
    private static final Pattern SAFE_TOKEN = Pattern.compile("^[a-zA-Z0-9/_\\-\\.]+$");
    private static final Pattern SHELL_SPECIALS = Pattern.compile("(?<!\\\\)([;{}()|])");
    private static final Set<String> SHELL_OPERATORS = Set.of("|", ";", "&&", "||", ">", "<", ">>");
    
    private static final Pattern THINK_PATTERN = Pattern.compile("<think>(.*?)</think>", Pattern.DOTALL);
    private static final Pattern JSON_CODE_BLOCK_PATTERN = Pattern.compile("```json\\s*(\\{.*?})\\s*```", Pattern.DOTALL);
    public static List<String> parseOperators(String commandLine) {
        List<String> operators = new ArrayList<>();
        List<String> tokens = tokenize(commandLine);

        for (String token : tokens) {
            if (SHELL_OPERATORS.contains(token)) {
                operators.add(token);
            }
        }
        return operators;
    }
    public CompletableFuture<String> executeCommandsAsList(List<List<String>> allCommands) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (allCommands == null || allCommands.isEmpty()) {
                    throw new IllegalArgumentException("No commands provided");
                }

                // Flatten tokens into a single sequence
                List<String> flat = allCommands.stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

                // Split on shell operators like |, &&, >, etc.
                AbstractMap.SimpleEntry<List<List<String>>, List<String>> split = splitIntoSegmentsAndOperators(flat);
                List<List<String>> segments = split.getKey();
                List<String> operators = split.getValue();

                // Apply ~ expansion and escaping
                segments = segments.stream()
                    .map(segment -> segment.stream()
                        .map(this::expandHome)
                        .map(this::escapeCommandParts)
                        .collect(Collectors.toList()))
                    .collect(Collectors.toList());

                // Execute pipeline
                return executePipeline(segments, operators);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, Executors.newSingleThreadExecutor());
    }

    private static List<String> tokenize(String commandLine) {
        List<String> tokens = new ArrayList<>();
        int i = 0;
        while (i < commandLine.length()) {
            char c = commandLine.charAt(i);

            // Skip whitespace
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            // Check for multi-char operators first (&&, ||, >>)
            if (i + 1 < commandLine.length()) {
                String twoChar = commandLine.substring(i, i + 2);
                if (SHELL_OPERATORS.contains(twoChar)) {
                    tokens.add(twoChar);
                    i += 2;
                    continue;
                }
            }

            // Check for single-char operator
            if (SHELL_OPERATORS.contains(String.valueOf(c))) {
                tokens.add(String.valueOf(c));
                i++;
                continue;
            }

            // Otherwise, parse a normal token until next whitespace or operator
            int start = i;
            while (i < commandLine.length() &&
                  !Character.isWhitespace(commandLine.charAt(i)) &&
                  !isOperatorStart(commandLine, i)) {
                i++;
            }
            tokens.add(commandLine.substring(start, i));
        }
        return tokens;
    }

    private static boolean isOperatorStart(String str, int index) {
        // Check 2-char operator
        if (index + 1 < str.length()) {
            String twoChar = str.substring(index, index + 2);
            if (SHELL_OPERATORS.contains(twoChar)) return true;
        }
        // Check 1-char operator
        return SHELL_OPERATORS.contains(String.valueOf(str.charAt(index)));
    }
    public static List<List<String>> parseSegments(String commandLine) {
        List<List<String>> segments = new ArrayList<>();
        List<String> tokens = tokenize(commandLine);

        List<String> currentSegment = new ArrayList<>();
        for (String token : tokens) {
            if (SHELL_OPERATORS.contains(token)) {
                if (!currentSegment.isEmpty()) {
                    segments.add(currentSegment);
                    currentSegment = new ArrayList<>();
                }
            } else {
                currentSegment.add(token);
            }
        }
        if (!currentSegment.isEmpty()) {
            segments.add(currentSegment);
        }
        return segments;
    }
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
    
    public static String removeThinkBlocks(String text) {
        String regex = "(?s)<think>.*?</think>";
        return text.replaceAll(regex, "");
    }

    private String escapeControlCharacters(String input) {
        // Replace newline and other control characters with their escaped equivalents
        return input.replaceAll("[\\x00-\\x1F\\x7F]", "\\\\u00$0");
    }

    public static String extractJsonContent(String input) {
        Matcher matcher = JSON_CODE_BLOCK_PATTERN.matcher(input);
        if (matcher.find()) {
            String raw = matcher.group(1).trim();
            return sanitizeJsonContent(unescapeIfQuoted(raw));
        }
        return sanitizeJsonContent(unescapeIfQuoted(input));
    }

    private static String unescapeIfQuoted(String input) {
        if ((input.startsWith("\"") && input.endsWith("\"")) ||
            (input.startsWith("'") && input.endsWith("'"))) {
            try {
                return new ObjectMapper().readValue(input, String.class); // unescape inner quotes
            } catch (IOException e) {
                return input; // fallback
            }
        }
        return input;
    }

    public static String sanitizeJsonContent(String input) {
        // Attempt JSON parsing
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readTree(input);
            return input;
        } catch (IOException e) {
            // Try fix common escape issues
            input = input.replace("\\\"", "\"").replace("\\\\", "\\"); // double escapes
            try {
                mapper.readTree(input);
                return input;
            } catch (IOException ignored) {}
        }
        return input;
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

    public String escapeCommandParts(String s) {
        if (s.startsWith("'") && s.endsWith("'")) return s; // already quoted
        if (SAFE_TOKEN.matcher(s).matches()) return s; // safe as-is

        return "'" + s.replace("'", "'\"'\"'") + "'"; // safest quoting method
    }

    
    private SimpleEntry<List<List<String>>, List<String>> splitIntoSegmentsAndOperators(List<String> tokens) {
        List<List<String>> segments = new ArrayList<>();
        List<String> operators = new ArrayList<>();
        List<String> current = new ArrayList<>();

        for (String token : tokens) {
            if (SHELL_OPERATORS.contains(token)) {
                if (current.isEmpty()) throw new IllegalArgumentException("Operator without preceding command: " + token);
                segments.add(new ArrayList<>(current));
                operators.add(token);
                current.clear();
            } else {
                current.add(token);
            }
        }
        if (!current.isEmpty()) segments.add(current);
        if (segments.size() != operators.size() + 1)
            throw new IllegalStateException("Misaligned segments/operators");
        return new SimpleEntry<>(segments, operators);
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
    
    
//    public CompletableFuture<String> executeCommandsAsList(List<List<String>> allCommands) {
//        ExecutorService executor = Executors.newSingleThreadExecutor();
//
//        return CompletableFuture.supplyAsync(() -> {
//            try {
//                if (allCommands.isEmpty()) {
//                    throw new IllegalArgumentException("No commands provided");
//                }
//                if (SHELL_OPERATORS.contains(allCommands.get(0).get(0))) {
//                    throw new IllegalArgumentException("Command cannot start with operator");
//                }
//                if (SHELL_OPERATORS.contains(allCommands.get(allCommands.size()-1).get(0))) {
//                    throw new IllegalArgumentException("Command cannot end with operator");
//                }
//                // Construct segments as List<List<String>> for executePipeline
//                List<List<String>> segmentTokens = allCommands.stream()
//                    .map(segment -> segment.stream()
//                        .map(this::expandHome)// if you want to expand ~ in each token
//                        .collect(Collectors.toList()))
//                    .collect(Collectors.toList());
//                AbstractMap.SimpleEntry<List<List<String>>, List<String>> split = splitIntoSegmentsAndOperators(segmentTokens);
//                List<List<String>> segments = split.getKey();
//                segments = segments.stream()
//                    .map(segment -> segment.stream()
//                        .map(token -> {
//                                return escapeCommandParts(token); // leave quoted segments untouched
//                        })
//                        .collect(Collectors.toList()))
//                    .collect(Collectors.toList());// Correct type: List<List<String>>
//                List<String> operators = split.getValue();
//                return executePipeline(segments, operators);
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            } finally {
//                executor.shutdown();
//            }
//        }, executor);
//    }
    
    public CompletableFuture<String> executeBase64Commands(String base64Command) throws Exception {
        System.out.println(base64Command);
        String decodedCommand = new String(Base64.getDecoder().decode(base64Command), StandardCharsets.UTF_8);

        // Parse decodedCommand to segments and operators:
        // e.g. "ls -l | grep foo > out.txt" =>
        // segments = [["ls","-l"], ["grep","foo"], ["out.txt"]]
        // operators = ["|", ">"]

        List<List<String>> segments = parseSegments(decodedCommand);
        List<String> operators = parseOperators(decodedCommand);

        return CompletableFuture.supplyAsync(() -> {
            try {
                return executePipeline(segments, operators);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
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

                    proc.getInputStream();
                    proc.getErrorStream();
                    continue;
                } else if (op.equals("<")) {
                    if (i + 1 >= segments.size()) throw new IllegalArgumentException("Expected filename after <");
                    File inFile = new File(segments.get(i + 1).get(0));
                    pb.redirectInput(ProcessBuilder.Redirect.from(inFile));
                    i += 2;
                    Process proc = pb.start();
                    
                    if (i == segments.size() - 1) {
                        // Last process, read output later synchronously, just drain error
                        proc.getErrorStream();
                    } else {
                        // Intermediate process, drain both to avoid blocking
                        proc.getInputStream();
                        proc.getErrorStream();
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
                    currProc.getErrorStream();
                } else {
                    // Intermediate process, drain both to avoid blocking
                    currProc.getInputStream();
                    currProc.getErrorStream();
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
            else if (i > 0 && List.of("&&", "||").contains(operators.get(i - 1))) {
                Process prevProc = processes.get(processes.size() - 1);
                int prevExit = prevProc.waitFor();
                String op = operators.get(i - 1);
                if (("&&".equals(op) && prevExit != 0) || ("||".equals(op) && prevExit == 0)) {
                    break;
                }
                Process proc = pb.start();
                
                if (i == segments.size() - 1) {
                    proc.getErrorStream();
                } else {
                    proc.getInputStream();
                    proc.getErrorStream();
                }
                processes.add(proc);
                lastProcess = proc;
            }
            else {
                Process proc = pb.start();
                if (i == segments.size() - 1) {
                    proc.getErrorStream();
                } else {
                    proc.getInputStream();
                    proc.getErrorStream();
                }
                processes.add(proc);
                lastProcess = proc;
            }
            i++;
        }
        if (lastProcess == null) throw new IllegalStateException("No process executed");
        StringBuilder fullOutput = new StringBuilder();
        for (int j = 0; j < processes.size(); j++) {
            Process proc = processes.get(j);
            boolean isRedirected = false;
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
            proc.waitFor();
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
    
    private static String quoteToken(String token) {
        if (token.matches("^[a-zA-Z0-9/_\\-\\.]+$")) {
            return token;
        }
        return "'" + token.replace("'", "'\\''") + "'";
    }
    
    public static CompletableFuture<String> executeCommands(List<List<String>> allCommands) {
        CompletableFuture<String> future = new CompletableFuture<>();

        if (allCommands.isEmpty()) {
            future.completeExceptionally(new IllegalArgumentException("No commands provided"));
            return future;
        }

        String commandLine = allCommands.stream()
            .map(segment -> {
                if (segment.size() == 1 && SHELL_OPERATORS.contains(segment.get(0))) {
                    return segment.get(0);
                } else {
                    return segment.stream()
                                  .map(ToolHandler::quoteToken)
                                  .collect(Collectors.joining(" "));
                }
            })
            .collect(Collectors.joining(" "));

        CommandLine cmd = new CommandLine("/bin/sh");
        cmd.addArgument("-c", false);
        cmd.addArgument(commandLine, false);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);

        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(streamHandler);
        executor.setWatchdog(new ExecuteWatchdog(20_000));
        executor.setExitValues(null); // accept any exit value

        DefaultExecuteResultHandler handler = new DefaultExecuteResultHandler() {
            @Override
            public void onProcessComplete(int exitValue) {
                // always complete with output, even if exit code is nonzero
                future.complete(outputStream.toString(StandardCharsets.UTF_8));
            }

            @Override
            public void onProcessFailed(ExecuteException e) {
                // still return captured output, even on failure
                future.complete(outputStream.toString(StandardCharsets.UTF_8));
            }
        };

        try {
            executor.execute(cmd, handler);
        } catch (IOException e) {
            // I/O failure – complete with exception
            future.completeExceptionally(e);
        }

        return future;
    }

}
    
//    public static List<String> executeFileSearch(OpenAIContainer responseObject, String query) {
//        String type = responseObject.get(FILESEARCHTOOL_TYPE);
//        if (!"file_search".equals(type)) {
//            System.out.println("⚠️ Tool type is not file_search.");
//            return List.of();
//        }
//        List<String> vectorStoreIds = responseObject.get(FILESEARCHTOOL_VECTOR_STORE_IDS);
//        Map<String, Object> filters = responseObject.get(FILESEARCHTOOL_FILTERS);
//        Integer maxResults = responseObject.get(FILESEARCHTOOL_MAX_NUM_RESULTS);
//        Map<String, Object> rankingOptions = responseObject.get(FILESEARCHTOOL_RANKING_OPTIONS);
//        if (vectorStoreIds == null || vectorStoreIds.isEmpty()) {
//            System.err.println("❌ No vector store IDs provided.");
//            return List.of();
//        }
//        AIManager aim = new AIManager();
//        List<String> allResults = new ArrayList<>();
//        for (String storeId : vectorStoreIds) {
//            List<String> results = aim.searchVectorStore(storeId, query, maxResults, filters, rankingOptions);
//            if (results != null) allResults.addAll(results);
//        }
//        return allResults;
//    }

