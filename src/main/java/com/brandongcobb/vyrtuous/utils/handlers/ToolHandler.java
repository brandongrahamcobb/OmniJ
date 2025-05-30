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
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ToolHandler {

    public static final MetadataKey<Integer> SHELL_EXIT_CODE = new MetadataKey<>("shell.exit_code", Metadata.INTEGER);
    public static final MetadataKey<String> SHELL_STDOUT = new MetadataKey<>("shell.stdout", Metadata.STRING);
    public static final MetadataKey<String> SHELL_STDERR = new MetadataKey<>("shell.stderr", Metadata.STRING);

    private static String readStream(InputStream stream) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            int maxLines = 200;
            int maxChars = 8000;
            int lineCount = 0;
            while ((line = reader.readLine()) != null && builder.length() < maxChars && lineCount < maxLines) {
                builder.append(line).append("\n");
                lineCount++;
            }
            if (line != null) {
                builder.append("...\n⚠️ Output truncated due to size.");
            }
        }
        return builder.toString().trim();
    }

    
    public CompletableFuture<String> completeShellCommand(MetadataContainer responseObject, String originalCommand) {
        if (originalCommand == null || originalCommand.isBlank()) {
            return CompletableFuture.completedFuture("⚠️ No shell command provided.");
        }

        return CompletableFuture.supplyAsync(() -> {
            ExecutorService streamExecutor = Executors.newFixedThreadPool(2);
            try {
                String raw = originalCommand.trim();
                String cmd = raw.startsWith("bash -lc ") ? raw.substring("bash -lc ".length()).trim() : raw;

                ProcessBuilder builder = new ProcessBuilder("gtimeout", "30s", "bash", "-lc", cmd);
                Process process = builder.start();

                // Stream gobblers for stdout and stderr
                Future<String> stdoutFuture = streamExecutor.submit(() -> readStream(process.getInputStream()));
                Future<String> stderrFuture = streamExecutor.submit(() -> readStream(process.getErrorStream()));

                int exitCode = process.waitFor();
                String stdout = stdoutFuture.get(10, TimeUnit.SECONDS); // Protect against stuck threads
                String stderr = stderrFuture.get(10, TimeUnit.SECONDS);

                responseObject.put(SHELL_EXIT_CODE, exitCode);
                responseObject.put(SHELL_STDOUT, stdout);
                responseObject.put(SHELL_STDERR, stderr);

                if (exitCode == 0) {
                    return stdout.isBlank() ? "✅ Command executed successfully with no output." : stdout;
                } else {
                    String errorMessage = stderr.isBlank() ? "No error message available." : stderr;
                    return "❌ Shell command exited with code " + exitCode + ":\n" + errorMessage;
                }
            } catch (TimeoutException e) {
                responseObject.put(SHELL_EXIT_CODE, 998);
                responseObject.put(SHELL_STDOUT, "");
                responseObject.put(SHELL_STDERR, "Stream reading timed out.");
                return "⏱️ Shell output reading timed out.";
            } catch (Exception e) {
                responseObject.put(SHELL_EXIT_CODE, 999);
                responseObject.put(SHELL_STDOUT, "");
                responseObject.put(SHELL_STDERR, e.getMessage());
                return "⚠️ Shell execution failed: " + e.getMessage();
            }
        });
    }
    
    public static List<String> executeFileSearch(ResponseObject responseObject, String query) {
        String type = responseObject.get(ResponseObject.FILESEARCHTOOL_TYPE);
        if (!"file_search".equals(type)) {
            System.out.println("⚠️ Tool type is not file_search.");
            return List.of();
        }
        List<String> vectorStoreIds = responseObject.get(ResponseObject.FILESEARCHTOOL_VECTOR_STORE_IDS);
        Map<String, Object> filters = responseObject.get(ResponseObject.FILESEARCHTOOL_FILTERS);
        Integer maxResults = responseObject.get(ResponseObject.FILESEARCHTOOL_MAX_NUM_RESULTS);
        Map<String, Object> rankingOptions = responseObject.get(ResponseObject.FILESEARCHTOOL_RANKING_OPTIONS);
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
