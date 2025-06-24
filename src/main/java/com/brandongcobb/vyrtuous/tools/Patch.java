//
//  Patch.java
//  
//
//  Created by Brandon Cobb on 6/24/25.
//
package com.brandongcobb.vyrtuous.tools;

import com.brandongcobb.vyrtuous.domain.*;
import com.brandongcobb.vyrtuous.utils.handlers.*;
import com.brandongcobb.vyrtuous.objects.*;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Patch implements Tool<PatchInput, PatchStatus> {

    private final ContextManager contextManager;

    public Patch(ContextManager contextManager) {
        this.contextManager = contextManager;
    }

    @Override
    public String getName() {
        return "patch";
    }

    @Override
    public PatchStatus run(PatchInput input) {
        contextManager.addEntry(new ContextEntry(ContextEntry.Type.TOOL, input.getOriginalJson().toString()));

        String targetFile = input.getTargetFile();
        List<PatchOperation> operations = input.getPatches();

        if (targetFile == null || operations == null || operations.isEmpty()) {
            return new PatchStatus(false, "Invalid patch input.");
        }

        try {
            Path filePath = Path.of(targetFile);
            List<String> originalLines = Files.readAllLines(filePath);
            List<String> patchedLines = applyOperations(originalLines, operations);
            Files.write(filePath, patchedLines);
            return new PatchStatus(true, "Patch applied successfully.");
        } catch (IOException e) {
            return new PatchStatus(false, "IO error: " + e.getMessage());
        } catch (Exception e) {
            return new PatchStatus(false, "Unexpected error: " + e.getMessage());
        }
    }

    private List<String> applyOperations(List<String> original, List<PatchOperation> ops) {
        List<String> result = new ArrayList<>(original);
        for (PatchOperation op : ops) {
            String type = op.getType();
            String match = op.getMatch();
            switch (type) {
                case "replace" -> {
                    String replacement = op.getReplacement();
                    for (int i = 0; i < result.size(); i++) {
                        if (result.get(i).contains(match)) {
                            result.set(i, result.get(i).replace(match, replacement));
                        }
                    }
                }
                case "delete" -> {
                    result.removeIf(line -> line.contains(match));
                }
                case "insertAfter" -> {
                    String code = op.getCode();
                    for (int i = 0; i < result.size(); i++) {
                        if (result.get(i).contains(match)) {
                            result.add(i + 1, code);
                            i++;
                        }
                    }
                }
                case "insertBefore" -> {
                    String code = op.getCode();
                    for (int i = 0; i < result.size(); i++) {
                        if (result.get(i).contains(match)) {
                            result.add(i, code);
                            i++;
                        }
                    }
                }
                case "append" -> {
                    String code = op.getCode();
                    for (int i = 0; i < result.size(); i++) {
                        if (result.get(i).contains(match)) {
                            result.set(i, result.get(i) + "\n" + code);
                        }
                    }
                }
                default -> throw new UnsupportedOperationException("Unsupported patch type: " + type);
            }
        }
        return result;
    }
}
