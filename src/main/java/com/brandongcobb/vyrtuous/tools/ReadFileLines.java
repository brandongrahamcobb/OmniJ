/*  ReadFileLines.java The primary purpose of this class is to act as a tool
 *  for reading lines in a file.
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

import com.brandongcobb.vyrtuous.domain.*;
import com.brandongcobb.vyrtuous.domain.input.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;

//import static com.brandongcobb.vyrtuous.service.REPLService.printIt;

@Component
public class ReadFileLines implements CustomTool<ReadFileLinesInput, ToolStatus> {
    
    private static final ObjectMapper mapper = new ObjectMapper();
    private final ChatMemory chatMemory;

    @Autowired
    public ReadFileLines(ChatMemory replChatMemory) {
        this.chatMemory = replChatMemory;
    }

    /*
     *  Getters
     */
    @Override
    public String getDescription() {
        return "Reads between two line numbers and returns content in the file.";
    }

    @Override
    public JsonNode getJsonSchema() {
        try {
            return mapper.readTree("""
            {
              "type": "object",
              "required": ["path", "startLine", "numLines"],
              "properties": {
                "path": {
                  "type": "string",
                  "description": "The path to the file."
                },
                "startLine": {
                  "type": "integer",
                  "minimum": 0,
                  "description": "The starting line number (0-indexed)."
                },
                "numLines": {
                  "type": "integer",
                  "minimum": 1,
                  "description": "The number of lines to read."
                }
              },
              "additionalProperties": false
            }            """);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build count_file_lines schema", e);
        }
    }

    @Override
    public Class<ReadFileLinesInput> getInputClass() {
        return ReadFileLinesInput.class;
    }
    
    @Override
    public String getName() {
        return "read_file_lines";
    }
    
    /*
     * Tool
     */
    @Override
    public CompletableFuture<ToolStatus> run(ReadFileLinesInput input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path filePath = Paths.get(input.getPath());
                if (!Files.exists(filePath)) {
                    return new ToolStatusWrapper("File not found: " + filePath, false);
                }
                List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
                int startLine = input.getStartLine();
                int numLines = input.getNumLines();
                if (startLine < 0 || numLines <= 0) {
                    return new ToolStatusWrapper("Invalid startLine or numLines parameters", false);
                }
                if (startLine >= lines.size()) {
                    return new ToolStatusWrapper("{\"content\": \"\"}", true);
                }
                int endLine = Math.min(startLine + numLines, lines.size());
                List<String> subLines = lines.subList(startLine, endLine);
                String content = String.join("\n", subLines);
                String jsonResponse = "{\"content\": " + JSONObject.quote(content) + "}";
                chatMemory.add("assistant", new AssistantMessage("{\"tool\":\"" + getName() + "\",\"arguments\":" + input.getOriginalJson().toString() + "}"));
                chatMemory.add("user", new AssistantMessage("{\"tool\":\"" + getName() + "\",\"arguments\":" + input.getOriginalJson().toString() + "}"));
                return new ToolStatusWrapper(jsonResponse, true);
            } catch (IOException e) {
                return new ToolStatusWrapper("IO error: " + e.getMessage(), false);
            } catch (Exception e) {
                return new ToolStatusWrapper("Unexpected error: " + e.getMessage(), false);
            }
        });
    }

}
