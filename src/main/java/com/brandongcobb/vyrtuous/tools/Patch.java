//
//  Patch.java
//  
//
//  Created by Brandon Cobb on 6/24/25.
//
package com.brandongcobb.vyrtuous.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.brandongcobb.vyrtuous.domain.*;
import com.brandongcobb.vyrtuous.utils.handlers.*;
import com.brandongcobb.vyrtuous.objects.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class Patch implements Tool<PatchInput, PatchStatus> {

    private final ContextManager modelContextManager;
    private final ContextManager userContextManager;
    private static final ObjectMapper mapper = new ObjectMapper();
    
    public Patch(ContextManager modelContextManager, ContextManager userContextManager) {
        this.modelContextManager = modelContextManager;
        this.userContextManager = userContextManager;
    }

    @Override
    public String getName() {
        return "patch";
    }
    
    @Override
    public String getDescription() {
        return "Applies a patch to a file using match and replace/insert rules.";
    }

    @Override
    public JsonNode getJsonSchema() {
        try {
            return mapper.readTree("""
            {
            "type": "object",
            "required": ["targetFile", "patches"],
            "properties": {
                "targetFile": {
                "type": "string",
                "description": "Relative or absolute path to the file to patch"
                },
                "patches": {
                "type": "array",
                "minItems": 1,
                "items": {
                    "type": "object",
                    "required": ["type", "match"],
                    "properties": {
                    "type": {
                        "type": "string",
                        "enum": ["replace", "insertBefore", "insertAfter", "delete", "append"],
                        "description": "Type of patch operation"
                    },
                    "match": {
                        "type": "string",
                        "description": "Exact string or regex to locate target for patch"
                    },
                    "replacement": {
                        "type": "string",
                        "description": "Replacement string for 'replace' type"
                    },
                    "code": {
                        "type": "string",
                        "description": "Code to insert for insertBefore/insertAfter/append"
                    }
                    },
                    "additionalProperties": false,
                    "allOf": [
                    {
                        "if": { "properties": { "type": { "const": "replace" } } },
                        "then": { "required": ["replacement"] }
                    },
                    {
                        "if": {
                        "properties": {
                            "type": {
                            "enum": ["insertBefore", "insertAfter", "append"]
                            }
                        }
                        },
                        "then": { "required": ["code"] }
                    }
                    ]
                }
                }
            },
            "additionalProperties": false
            }
            """);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build patch schema", e);
        }
    }


    @Override
    public CompletableFuture<PatchStatus> run(PatchInput input) {
        return CompletableFuture.supplyAsync(() -> {
            userContextManager.addEntry(new ContextEntry(ContextEntry.Type.TOOL, input.getOriginalJson().toString()));
    
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
        });
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
