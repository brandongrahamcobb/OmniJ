/*  Patch.java The primary purpose of this class is to act as a tool
 *  for patching (editing) files.
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
import com.brandongcobb.vyrtuous.utils.handlers.*;
import com.brandongcobb.vyrtuous.objects.*;
import com.brandongcobb.vyrtuous.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class Patch implements Tool<PatchInput, ToolStatus> {
    
    private static final Logger LOGGER = Logger.getLogger(Vyrtuous.class.getName());
    private static final ObjectMapper mapper = new ObjectMapper();
    private final ContextManager modelContextManager;
    private final ContextManager userContextManager;
    
    public Patch(ContextManager modelContextManager, ContextManager userContextManager) {
        this.modelContextManager = modelContextManager;
        this.userContextManager = userContextManager;
    }
    
    /*
     *  Getters
     */
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
    public String getName() {
        return "patch";
    }
    
    /*
     *  Tool
     */
    @Override
    public CompletableFuture<ToolStatus> run(PatchInput input) {
        return CompletableFuture.supplyAsync(() -> {
            modelContextManager.addEntry(new ContextEntry(ContextEntry.Type.TOOL, "{\"name\":" + "\"" + getName() + "\",\"input\":" + input.getOriginalJson().toString() + "\""));
            userContextManager.addEntry(new ContextEntry(ContextEntry.Type.TOOL, "{\"name\":" + "\"" + getName() + "\",\"input\":" + input.getOriginalJson().toString() + "\""));
            String targetFile = input.getTargetFile();
            List<PatchOperation> operations = input.getPatches();
            if (targetFile == null || operations == null || operations.isEmpty()) {
                return new ToolStatusWrapper("Invalid patch input.", false);
            }
            try {
                Path filePath = Path.of(targetFile);
                List<String> originalLines = Files.readAllLines(filePath);
                List<String> patchedLines = applyOperations(originalLines, operations);
                Files.write(filePath, patchedLines);
                return new ToolStatusWrapper("Patch applied successfully.", true);
            } catch (IOException e) {
                return new ToolStatusWrapper("IO error: " + e.getMessage(), false);
            } catch (Exception e) {
                return new ToolStatusWrapper("Unexpected error: " + e.getMessage(), false);
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
                            LOGGER.finer("Matched line: " + result.get(i));
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
                    if (match == null) {
                        result.add(code);  // Append to end of file
                    } else {
                        boolean matched = false;
                        for (int i = 0; i < result.size(); i++) {
                            if (result.get(i).contains(match)) {
                                result.set(i, result.get(i) + "\n" + code);
                                matched = true;
                            }
                        }
                        if (!matched) {
                            LOGGER.warning("Append operation: no match found for string '" + match + "'");
                        }
                    }
                }
                default -> throw new UnsupportedOperationException("Unsupported patch type: " + type);
            }
        }
        return result;
    }
}
