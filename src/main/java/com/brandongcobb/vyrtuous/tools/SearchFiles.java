//
//  SearchFiles.java
//  
//
//  Created by Brandon Cobb on 6/24/25.
//
package com.brandongcobb.vyrtuous.tools;

import com.brandongcobb.vyrtuous.domain.*;
import com.brandongcobb.vyrtuous.objects.ContextEntry;
import com.brandongcobb.vyrtuous.utils.handlers.ContextManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class SearchFiles implements Tool<SearchFilesInput, SearchFilesStatus> {

    private final ContextManager contextManager;

    public SearchFiles(ContextManager contextManager) {
        this.contextManager = contextManager;
    }

    @Override
    public String getName() {
        return "search_files";
    }

    @Override
    public CompletableFuture<SearchFilesStatus> run(SearchFilesInput input) {
        return CompletableFuture.supplyAsync(() -> {
            List<SearchFilesStatus.Result> results = new ArrayList<>();
            try (Stream<Path> stream = Files.walk(Paths.get(input.getRootDirectory()))) {
                stream
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        if (input.getFileExtensions() != null && !input.getFileExtensions().isEmpty()) {
                            boolean match = input.getFileExtensions().stream()
                                .anyMatch(ext -> path.toString().endsWith(ext));
                            if (!match) return false;
                        }
                        if (input.getFileNameContains() != null &&
                            !path.getFileName().toString().contains(input.getFileNameContains())) {
                            return false;
                        }
                        return true;
                    })
                    .limit(input.getMaxResults())
                    .forEach(path -> {
                        String snippet = null;
                        if (input.getGrepContains() != null) {
                            try {
                                String content = Files.readString(path, StandardCharsets.UTF_8);
                                if (!content.contains(input.getGrepContains())) return;
                                int index = content.indexOf(input.getGrepContains());
                                int start = Math.max(0, index - 20);
                                int end = Math.min(content.length(), index + 80);
                                snippet = content.substring(start, end).replaceAll("\\s+", " ");
                            } catch (IOException ignored) {}
                        }
                        results.add(new SearchFilesStatus.Result(path.toString(), snippet));
                    });

                String summary = String.format("Found %d matching file(s).", results.size());
                contextManager.addEntry(new ContextEntry(ContextEntry.Type.TOOL_OUTPUT, summary));
                return new SearchFilesStatus(true, summary, results);
            } catch (IOException e) {
                return new SearchFilesStatus(false, "IO error: " + e.getMessage(), null);
            }
        });
    }
}

