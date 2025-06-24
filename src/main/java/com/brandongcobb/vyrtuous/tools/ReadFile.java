
package com.brandongcobb.vyrtuous.tools;

import com.brandongcobb.vyrtuous.objects.ContextEntry;
import com.brandongcobb.vyrtuous.utils.handlers.ContextManager;

import com.brandongcobb.vyrtuous.domain.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.CompletableFuture;

public class ReadFile implements Tool<ReadFileInput, ReadFileStatus> {

    private final ContextManager contextManager;

    public ReadFile(ContextManager contextManager) {
        this.contextManager = contextManager;
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

                contextManager.addEntry(new ContextEntry(
                    ContextEntry.Type.TOOL_OUTPUT, content
                ));

                return new ReadFileStatus(true, "File read successfully.", content);
            } catch (IOException e) {
                return new ReadFileStatus(false, "IO error: " + e.getMessage(), null);
            } catch (Exception e) {
                return new ReadFileStatus(false, "Unexpected error: " + e.getMessage(), null);
            }
        });
    }
}
