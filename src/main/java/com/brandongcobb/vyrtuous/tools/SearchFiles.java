/*  SearchFiles.java The primary purpose of this class is to act as a tool
 *  for making searching files. This is a crude method which will be
 *  replaced with a VectorStore.
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
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.brandongcobb.vyrtuous.tools;

import com.brandongcobb.vyrtuous.domain.input.SearchFilesInput;
import com.brandongcobb.vyrtuous.domain.ToolStatus;
import com.brandongcobb.vyrtuous.domain.ToolStatusWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static com.brandongcobb.vyrtuous.service.REPLService.printIt;

@Component
public class SearchFiles implements CustomTool<SearchFilesInput, ToolStatus> {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final ChatMemory chatMemory;
    
    @Autowired
    public SearchFiles(ChatMemory replChatMemory) {
        this.chatMemory = replChatMemory;
    }
    
    /*
     *  Getters
     */
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
    public Class<SearchFilesInput> getInputClass() {
        return SearchFilesInput.class;
    }
    
    @Override
    public String getName() {
        return "search_files";
    }
    
    /*
     * Tool
     */
    @Override
    public CompletableFuture<ToolStatus> run(SearchFilesInput input) {
        return CompletableFuture.supplyAsync(() -> {
            List<Result> results = new ArrayList<>();
            Set<Path> forbidden = Set.of(
                Paths.get("/System"),
                Paths.get("/usr/sbin"),
                Paths.get("/private"),
                Paths.get("/Volumes"),
                Paths.get("/dev"),
                Paths.get("/proc")
            );
            String rootDir = input.getRootDirectory();

            if (rootDir.startsWith("~")) {
                String home = System.getProperty("user.home");
                rootDir = home + rootDir.substring(1);
            }

            Path rootPath = Paths.get(rootDir);
            try (Stream<Path> stream = Files.walk(rootPath)) {
                stream
                    .filter(path -> {
                        Path normalized = path.toAbsolutePath().normalize();
                        for (Path forbiddenRoot : forbidden) {
                            if (normalized.startsWith(forbiddenRoot)) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        if (input.getFileExtensions() != null && !input.getFileExtensions().isEmpty()) {
                            if (!input.getFileExtensions().contains("*")) {
                                boolean match = input.getFileExtensions().stream()
                                    .anyMatch(ext -> path.toString().endsWith(ext));
                                if (!match) return false;
                            }
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
                        results.add(new Result(path.toString(), snippet));
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
                chatMemory.add("assistant", new AssistantMessage("{\"tool\":" + "\"" + getName() + "\",\"input\":" + input.getOriginalJson().toString() + "}"));
                chatMemory.add("user", new AssistantMessage("{\"tool\":" + "\"" + getName() + "\",\"input\":" + input.getOriginalJson().toString() + "}"));
                printIt();
                return new ToolStatusWrapper(summary, true);
            } catch (IOException e) {
                return new ToolStatusWrapper("IO error: " + e.getMessage(), false);
            }
        });
    }

    /*
     *  Nested class
     */
    public static class Result {
    
        public String path;
        public String snippet;
        
        public Result(String path, String snippet) {
            this.path = path;
            this.snippet = snippet;
        }
    }
}
