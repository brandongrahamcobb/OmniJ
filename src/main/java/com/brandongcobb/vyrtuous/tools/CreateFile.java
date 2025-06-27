/*  CreateFile.java The primary purpose of this class is to act as a tool
 *  for creating files.
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

public class CreateFile implements Tool<CreateFileInput, CreateFileStatus> {

    private final ContextManager modelContextManager;
    private final ContextManager userContextManager;
    private static final ObjectMapper mapper = new ObjectMapper();
    
    public CreateFile(ContextManager modelContextManager, ContextManager userContextManager) {
        this.modelContextManager = modelContextManager;
        this.userContextManager = userContextManager;
    }

    /*
     *  Getters
     */
    @Override
    public String getDescription() {
        return "Creates a file with specified content, optionally overwriting.";
    }

    @Override
    public JsonNode getJsonSchema() {
        try {
            String schemaJson = """
            {
              "type": "object",
              "required": ["path", "content"],
              "properties": {
                "path": {
                  "type": "string",
                  "description": "The file path where content should be written."
                },
                "content": {
                  "type": "string",
                  "description": "The content to write into the file."
                },
                "overwrite": {
                  "type": "boolean",
                  "default": false,
                  "description": "Whether to overwrite the file if it already exists."
                }
              },
              "additionalProperties": false
            }
            """;
            return mapper.readTree(schemaJson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build create_file schema", e);
        }
    }
    
    @Override
    public String getName() {
        return "create_file";
    }

    @Override
    public CompletableFuture<CreateFileStatus> run(CreateFileInput input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path filePath = Paths.get(input.getPath());
                boolean fileExists = Files.exists(filePath);
                if (fileExists && !input.getOverwrite()) {
                    return new CreateFileStatus("File already exists and overwrite is false.", false);
                }
                Files.createDirectories(filePath.getParent());
                Files.writeString(
                    filePath,
                    input.getContent(),
                    StandardCharsets.UTF_8,
                    input.getOverwrite() ? StandardOpenOption.CREATE : StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.TRUNCATE_EXISTING
                );
                modelContextManager.addEntry(new ContextEntry(ContextEntry.Type.TOOL, input.getOriginalJson().toString()));
                userContextManager.addEntry(new ContextEntry(ContextEntry.Type.TOOL, input.getOriginalJson().toString()));
                return new CreateFileStatus("File created successfully: " + filePath.toString(), true);
            } catch (FileAlreadyExistsException e) {
                return new CreateFileStatus("File already exists and overwrite not allowed.", false);
            } catch (IOException e) {
                return new CreateFileStatus("IO error: " + e.getMessage(), false);
            } catch (Exception e) {
                return new CreateFileStatus("Unexpected error: " + e.getMessage(), false);
            }
        });
    }
}
