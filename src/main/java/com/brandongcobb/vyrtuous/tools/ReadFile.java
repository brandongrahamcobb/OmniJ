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

public class ReadFile implements Tool<ReadFileInput, ReadFileStatus> {
    
    private static final ObjectMapper mapper = new ObjectMapper();
    private final ContextManager modelContextManager;
    private final ContextManager userContextManager;
    
    public ReadFile(ContextManager modelContextManager, ContextManager userContextManager) {
        this.modelContextManager = modelContextManager;
        this.userContextManager = userContextManager;
    }

    /*
     *  Getters
     */
    @Override
    public String getDescription() {
        return "Reads and returns the contents of a file.";
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
                  "description": "The path to the file to be read."
                }
              },
              "additionalProperties": false
            }
            """);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build read_file schema", e);
        }
    }

    @Override
    public String getName() {
        return "read_file";
    }
    
    /*
     * Tool
     */
    @Override
    public CompletableFuture<ReadFileStatus> run(ReadFileInput input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path filePath = Paths.get(input.getPath());
                if (!Files.exists(filePath)) {
                    return new ReadFileStatus("File not found: " + filePath, false);
                }

                String content = Files.readString(filePath, StandardCharsets.UTF_8);
                modelContextManager.addEntry(new ContextEntry(ContextEntry.Type.TOOL, "{\"name\": " + "\"" + getName() + "\", \"input\": " + input.getOriginalJson().toString() + "\""));
                userContextManager.addEntry(new ContextEntry(ContextEntry.Type.TOOL, "{\"name\": " + "\"" + getName() + "\", \"input\": " + input.getOriginalJson().toString() + "\""));
                return new ReadFileStatus(content, true);
            } catch (IOException e) {
                return new ReadFileStatus("IO error: " + e.getMessage(), false);
            } catch (Exception e) {
                return new ReadFileStatus("Unexpected error: " + e.getMessage(), false);
            }
        });
    }
}
