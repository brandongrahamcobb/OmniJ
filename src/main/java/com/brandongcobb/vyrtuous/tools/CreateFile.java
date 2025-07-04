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

import com.brandongcobb.vyrtuous.Vyrtuous;
import com.brandongcobb.vyrtuous.domain.input.CreateFileInput;
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
import java.nio.file.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import static com.brandongcobb.vyrtuous.service.REPLService.printIt;

@Component
public class CreateFile implements CustomTool<CreateFileInput, ToolStatus> {
    
    private static final Logger LOGGER = Logger.getLogger(Vyrtuous.class.getName());
    private static final ObjectMapper mapper = new ObjectMapper();
    private final ChatMemory chatMemory;
    
    @Autowired
    public CreateFile(ChatMemory replChatMemory) {
        this.chatMemory = replChatMemory;
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
    public Class<CreateFileInput> getInputClass() {
        return CreateFileInput.class;
    }
    
    @Override
    public String getName() {
        return "create_file";
    }

    /*
     * Tool
     */
    @Override
    public CompletableFuture<ToolStatus> run(CreateFileInput input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LOGGER.finer(input.getPath());
                Path filePath = Paths.get(input.getPath());
                boolean fileExists = Files.exists(filePath);
                if (fileExists && !input.getOverwrite()) {
                    return new ToolStatusWrapper("File already exists and overwrite is false.", false);
                }
                Path parent = filePath.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                Files.writeString(
                    filePath,
                    input.getContent(),
                    StandardCharsets.UTF_8,
                    input.getOverwrite() ? StandardOpenOption.CREATE : StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.TRUNCATE_EXISTING
                );
                chatMemory.add("assistant", new AssistantMessage("{\"tool\":" + "\"" + getName() + "\",\"arguments\":" + input.getOriginalJson().toString() + "}"));
                chatMemory.add("user", new AssistantMessage("{\"tool\":" + "\"" + getName() + "\",\"arguments\":" + input.getOriginalJson().toString() + "}"));
                printIt();
                return new ToolStatusWrapper("File created successfully: " + filePath.toString(), true);
            } catch (FileAlreadyExistsException e) {
                return new ToolStatusWrapper("File already exists and overwrite not allowed.", false);
            } catch (IOException e) {
                return new ToolStatusWrapper("IO error: " + e.getMessage(), false);
            } catch (Exception e) {
                return new ToolStatusWrapper("Unexpected error: " + e.getMessage(), false);
            }
        });
    }
}
