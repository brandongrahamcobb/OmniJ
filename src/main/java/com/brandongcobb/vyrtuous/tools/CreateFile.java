package com.brandongcobb.vyrtuous.tools;

import com.brandongcobb.vyrtuous.objects.ContextEntry;
import com.brandongcobb.vyrtuous.utils.handlers.ContextManager;

import com.brandongcobb.vyrtuous.domain.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.CompletableFuture;

public class CreateFile implements Tool<CreateFileInput, CreateFileStatus> {

    private final ContextManager contextManager;

    public CreateFile(ContextManager contextManager) {
        this.contextManager = contextManager;
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

                contextManager.addEntry(new ContextEntry(
                    ContextEntry.Type.TOOL_OUTPUT,
                    "Created file: " + filePath.toString()
                ));

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
