/*  FindInFile.java The primary purpose of this class is to act as a tool
 *  for finding text in a file.
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

import com.brandongcobb.vyrtuous.domain.ToolStatus;
import com.brandongcobb.vyrtuous.domain.ToolStatusWrapper;
import com.brandongcobb.vyrtuous.domain.input.FindInFileInput;
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
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import static com.brandongcobb.vyrtuous.service.REPLService.printIt;

@Component
public class FindInFile implements CustomTool<FindInFileInput, ToolStatus> {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final ChatMemory chatMemory;
    
    @Autowired
    public FindInFile(ChatMemory replChatMemory) {
        this.chatMemory = replChatMemory;
    }
    
    @Override
    public Class<FindInFileInput> getInputClass() {
        return FindInFileInput.class;
    }
    
    @Override
    public String getName() {
        return "find_in_file";
    }

    @Override
    public String getDescription() {
        return "Finds occurrences of a string or pattern in a single file and returns surrounding lines optionally.";
    }

    @Override
    public JsonNode getJsonSchema() {
        try {
            return mapper.readTree("""
            {
              "type": "object",
              "required": ["filePath", "searchTerms"],
              "properties": {
                "filePath": {
                  "type": "string",
                  "description": "Path to the file to search within."
                },
                "searchTerms": {
                  "type": "array",
                  "items": { "type": "string" },
                  "description": "Terms or patterns to search for in the file."
                },
                "useRegex": {
                  "type": "boolean",
                  "default": false,
                  "description": "If true, interpret search terms as regular expressions."
                },
                "ignoreCase": {
                  "type": "boolean",
                  "default": true,
                  "description": "If true, ignore case when searching."
                },
                "contextLines": {
                  "type": "integer",
                  "default": 2,
                  "description": "Number of lines of context to include before and after each match."
                },
                "maxResults": {
                  "type": "integer",
                  "default": 10,
                  "description": "Maximum number of matches to return."
                }
              },
              "additionalProperties": false
            }
            """);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate schema for find_in_file", e);
        }
    }

    @Override
    public CompletableFuture<ToolStatus> run(FindInFileInput input) {
        return CompletableFuture.supplyAsync(() -> {
            Path filePath = Paths.get(input.getFilePath());
            if (!Files.exists(filePath)) {
                return new ToolStatusWrapper("File not found: " + filePath, false);
            }
            try {
                List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
                List<Match> matches = new ArrayList<>();
                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);
                    for (String term : input.getSearchTerms()) {
                        boolean isMatch;
                        if (input.isUseRegex()) {
                            int flags = input.isIgnoreCase() ? Pattern.CASE_INSENSITIVE : 0;
                            isMatch = Pattern.compile(term, flags).matcher(line).find();
                        } else {
                            isMatch = input.isIgnoreCase()
                                ? line.toLowerCase().contains(term.toLowerCase())
                                : line.contains(term);
                        }
                        if (isMatch) {
                            int start = Math.max(0, i - input.getContextLines());
                            int end = Math.min(lines.size(), i + input.getContextLines() + 1);
                            List<String> snippet = lines.subList(start, end);
                            matches.add(new Match(i + 1, String.join("\n", snippet)));
                            if (matches.size() >= input.getMaxResults()) break;
                        }
                    }
                    if (matches.size() >= input.getMaxResults()) break;
                }
                String summary;
                if (matches.isEmpty()) {
                    summary = "No matches found in file.";
                } else {
                    StringBuilder sb = new StringBuilder("Found ")
                        .append(matches.size())
                        .append(" match(es) in ")
                        .append(filePath)
                        .append(":\n\n");
                    for (Match match : matches) {
                        sb.append("Line ").append(match.getLineNumber()).append(":\n")
                          .append(match.getContextSnippet()).append("\n\n");
                    }
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
    public class Match {

        private final int lineNumber;
        private final String contextSnippet;

        public Match(int lineNumber, String contextSnippet) {
            this.lineNumber = lineNumber;
            this.contextSnippet = contextSnippet;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public String getContextSnippet() {
            return contextSnippet;
        }
    }
}

