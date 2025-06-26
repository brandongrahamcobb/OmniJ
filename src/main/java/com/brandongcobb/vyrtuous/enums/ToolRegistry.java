/*  ToolRegistry.java The primary purpose of this class is to act include
 *  call, convert and list tools.
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
package com.brandongcobb.vyrtuous.enums;

import com.brandongcobb.vyrtuous.tools.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ToolRegistry {
    
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, Tool> tools = new HashMap<>();
    
    public CompletableFuture<JsonNode> callTool(String name, JsonNode arguments) {
        Tool<?, ?> tool = tools.get(name);
        if (tool == null) {
            CompletableFuture<JsonNode> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IllegalArgumentException("Tool not found: " + name));
            return failed;
        }
        try {
            return ((Tool<JsonNode, ?>) tool)
                .run(arguments)
                .thenApply(result -> mapper.valueToTree(result));
        } catch (Exception e) {
            CompletableFuture<JsonNode> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }
    
    private ToolDefinition convertToMCPFormat(Tool tool) {
        return ToolDefinition.builder()
            .name(tool.getName())
            .description(tool.getDescription())
            .inputSchema(tool.getJsonSchema())
            .build();
    }
    
    public List<ToolDefinition> listTools() {
        return tools.values().stream()
            .map(this::convertToMCPFormat)
            .collect(Collectors.toList());
    }
}
