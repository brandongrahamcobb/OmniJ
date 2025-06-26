package com.brandongcobb.vyrtuous.tools;

import com.brandongcobb.vyrtuous.objects.ContextEntry;
import com.brandongcobb.vyrtuous.utils.handlers.ContextManager;

import com.fasterxml.jackson.databind.JsonNode;
import com.brandongcobb.vyrtuous.domain.*;

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

    @Override
    public String getName() {
        return "create_file";
    }
    
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
    public CompletableFuture<CreateFileStatus> run(CreateFileInput input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path filePath = Paths.get(input.getPath());
                boolean fileExists = Files.exists(filePath);

                if (fileExists && !input.isOverwrite()) {
                    return new CreateFileStatus(false, "File already exists and overwrite is false.");
                }

                Files.createDirectories(filePath.getParent());

                Files.writeString(
                    filePath,
                    input.getContent(),
                    StandardCharsets.UTF_8,
                    input.isOverwrite() ? StandardOpenOption.CREATE : StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.TRUNCATE_EXISTING
                );

                userContextManager.addEntry(new ContextEntry(ContextEntry.Type.TOOL, input.getOriginalJson().toString()));

                return new CreateFileStatus(true, "File created successfully: " + filePath.toString());
            } catch (FileAlreadyExistsException e) {
                return new CreateFileStatus(false, "File already exists and overwrite not allowed.");
            } catch (IOException e) {
                return new CreateFileStatus(false, "IO error: " + e.getMessage());
            } catch (Exception e) {
                return new CreateFileStatus(false, "Unexpected error: " + e.getMessage());
            }
        });
    }
}
