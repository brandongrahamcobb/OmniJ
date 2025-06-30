/*  ReadFile.java The primary purpose of this class is to act as a tool
 *  for reading file contents. This is a crude method which can be replaced
 *  by a cli agent.
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
import com.brandongcobb.vyrtuous.objects.*;
import com.brandongcobb.vyrtuous.utils.handlers.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class CountFileLines implements Tool<CountFileLinesInput, ToolStatus> {
    
    private static final ObjectMapper mapper = new ObjectMapper();
    private final ContextManager modelContextManager;
    private final ContextManager userContextManager;
    
    public CountFileLines(ContextManager modelContextManager, ContextManager userContextManager) {
        this.modelContextManager = modelContextManager;
        this.userContextManager = userContextManager;
    }

    /*
     *  Getters
     */
    @Override
    public String getDescription() {
        return "Reads and returns the number of lines in a file.";
    }

    @Override
    public JsonNode getJsonSchema() {
        try {
            return mapper.readTree("""
            {
              "type": "object",
              "required": ["path"],
              "properties": {
                "path": {
                  "type": "string",
                  "description": "The path to the file to be counted."
                }
              },
              "additionalProperties": false
            }
            """);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build count_file_lines schema", e);
        }
    }

    @Override
    public String getName() {
        return "count_file_lines";
    }
    
    /*
     * Tool
     */
    @Override
    public CompletableFuture<ToolStatus> run(CountFileLinesInput input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path filePath = Paths.get(input.getPath());
                if (!Files.exists(filePath)) {
                    return new ToolStatusWrapper("File not found: " + filePath, false);
                }
                long lineCount;
                try (Stream<String> lines = Files.lines(filePath, StandardCharsets.UTF_8)) {
                    lineCount = lines.count();
                }
                modelContextManager.addEntry(new ContextEntry(ContextEntry.Type.TOOL, "{\"name\":" + "\"" + getName() + "\",\"input\":" + input.getOriginalJson().toString() + "}"));
                userContextManager.addEntry(new ContextEntry(ContextEntry.Type.TOOL, "{\"name\":" + "\"" + getName() + "\",\"input\":" + input.getOriginalJson().toString() + "}"));
                return new ToolStatusWrapper(String.valueOf(lineCount), true);
            } catch (IOException e) {
                return new ToolStatusWrapper("IO error: " + e.getMessage(), false);
            } catch (Exception e) {
                return new ToolStatusWrapper("Unexpected error: " + e.getMessage(), false);
            }
        });
    }
}
