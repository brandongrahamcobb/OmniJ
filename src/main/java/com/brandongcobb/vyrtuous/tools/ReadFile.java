
package com.brandongcobb.vyrtuous.tools;

import com.brandongcobb.vyrtuous.objects.ContextEntry;
import com.brandongcobb.vyrtuous.utils.handlers.ContextManager;

import com.brandongcobb.vyrtuous.domain.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.CompletableFuture;

    // Add a comment here to indicate the purpose of this class
    // This class reads a file and adds its content to the context.

public class ReadFile implements Tool<ReadFileInput, ReadFileStatus> {

    private final ContextManager modelContextManager;
    private final ContextManager userContextManager;

    public ReadFile(ContextManager modelContextManager, ContextManager userContextManager) {
        this.modelContextManager = modelContextManager;
        this.userContextManager = userContextManager;
    }

    @Override
    public String getName() {
        return "read_file";
    }

    @Override
    public CompletableFuture<ReadFileStatus> run(ReadFileInput input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path filePath = Paths.get(input.getPath());
                if (!Files.exists(filePath)) {
                    return new ReadFileStatus(false, "File not found: " + filePath, null);
                }

                String content = Files.readString(filePath, StandardCharsets.UTF_8);

                userContextManager.addEntry(new ContextEntry(ContextEntry.Type.TOOL, input.getOriginalJson().toString()));
                return new ReadFileStatus(true, "File read successfully.", content);
            } catch (IOException e) {
                return new ReadFileStatus(false, "IO error: " + e.getMessage(), null);
            } catch (Exception e) {
                return new ReadFileStatus(false, "Unexpected error: " + e.getMessage(), null);
            }
        });
    }
}
