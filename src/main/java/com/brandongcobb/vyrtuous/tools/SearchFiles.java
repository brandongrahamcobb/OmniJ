//
//  SearchFiles.java
//  
//
//  Created by Brandon Cobb on 6/24/25.
//
package com.brandongcobb.vyrtuous.tools;

import com.brandongcobb.vyrtuous.domain.*;
import com.brandongcobb.vyrtuous.objects.ContextEntry;
import com.brandongcobb.vyrtuous.utils.handlers.ContextManager;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SearchFiles implements Tool<SearchFilesInput, SearchFilesStatus> {

    private final ContextManager modelContextManager;
    private final ContextManager userContextManager;
    private static final ObjectMapper mapper = new ObjectMapper();
    
    public SearchFiles(ContextManager modelContextManager, ContextManager userContextManager) {
        this.modelContextManager = modelContextManager;
        this.userContextManager = userContextManager;
    }
    
    @Override
    public String getName() {
        return "search_files";
    }

    @Override
    public String getDescription() {
        return "Searches files recursively from a root directory.";
    }

    @Override
    public JsonNode getJsonSchema() {
        try {
            return mapper.readTree("""
            {
              "type": "object",
              "required": ["rootDirectory"],
              "properties": {
                "rootDirectory": {
                  "type": "string",
                  "description": "Directory to search from (recursively)."
                },
                "fileExtensions": {
                  "type": "array",
                  "items": { "type": "string" },
                  "description": "Optional file extensions to filter by (e.g. ['.java', '.kt'])."
                },
                "fileNameContains": {
                  "type": "array",
                  "items": { "type": "string" },
                  "description": "Optional substring that must appear in file name."
                },
                "grepContains": {
                  "type": "array",
                  "items": { "type": "string" },
                  "description": "Optional text that must appear in file contents."
                },
                "maxResults": {
                  "type": "integer",
                  "default": 100,
                  "description": "Maximum number of files to return."
                }
              },
              "additionalProperties": false
            }
            """);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build search_files schema", e);
        }
    }
    
    @Override
    public CompletableFuture<SearchFilesStatus> run(SearchFilesInput input) {
        return CompletableFuture.supplyAsync(() -> {
            List<SearchFilesStatus.Result> results = new ArrayList<>();
            try (Stream<Path> stream = Files.walk(Paths.get(input.getRootDirectory()))) {
                stream
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        if (input.getFileExtensions() != null && !input.getFileExtensions().isEmpty()) {
                            boolean match = input.getFileExtensions().stream()
                                .anyMatch(ext -> path.toString().endsWith(ext));
                            if (!match) return false;
                        }
                        if (input.getFileNameContains() != null && !input.getFileNameContains().isEmpty()) {
                            boolean match = input.getFileNameContains().stream()
                                .anyMatch(sub -> path.getFileName().toString().contains(sub));
                            if (!match) return false;
                        }
                        return true;
                    })

                    .limit(input.getMaxResults())
                    .forEach(path -> {
                        String snippet = null;
                        if (input.getGrepContains() != null && !input.getGrepContains().isEmpty()) {
                            try {
                                String content = Files.readString(path, StandardCharsets.UTF_8);
                                boolean found = input.getGrepContains().stream().anyMatch(content::contains);
                                if (!found) return;
                                // Get first match index for snippet
                                String match = input.getGrepContains().stream()
                                    .filter(content::contains)
                                    .findFirst()
                                    .orElse(null);
                                int index = content.indexOf(match);
                                int start = Math.max(0, index - 20);
                                int end = Math.min(content.length(), index + 80);
                                snippet = content.substring(start, end).replaceAll("\\s+", " ");
                            } catch (IOException ignored) {}
                        }
                        results.add(new SearchFilesStatus.Result(path.toString(), snippet));
                    });

                String summary;
                if (results.isEmpty()) {
                    summary = "No matching files found.";
                } else {
                    StringBuilder sb = new StringBuilder("Found ")
                        .append(results.size())
                        .append(" file(s):\n");
                    results.forEach(r -> sb.append("• ").append(r.path).append("\n"));
                    summary = sb.toString().trim();
                }
                
                userContextManager.addEntry(new ContextEntry(ContextEntry.Type.TOOL, input.getOriginalJson().toString()));
                return new SearchFilesStatus(true, summary, results);
            } catch (IOException e) {
                return new SearchFilesStatus(false, "IO error: " + e.getMessage(), null);
            }
        });
    }
    
}

