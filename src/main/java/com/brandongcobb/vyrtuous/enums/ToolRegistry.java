//
//  ToolRegistry.java
//  
//
//  Created by Brandon Cobb on 6/26/25.
//
package com.brandongcobb.vyrtuous.enums;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.CompletableFuture;
import com.brandongcobb.vyrtuous.tools.*;

public class ToolRegistry {
    private final Map<String, Tool> tools = new HashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();
    
    // Convert your existing tools to MCP format
    public List<ToolDefinition> listTools() {
        return tools.values().stream()
            .map(this::convertToMCPFormat)
            .collect(Collectors.toList());
    }
    
    private ToolDefinition convertToMCPFormat(Tool tool) {
        return ToolDefinition.builder()
            .name(tool.getName())
            .description(tool.getDescription())
            .inputSchema(tool.getJsonSchema()) // Your existing JSON schema
            .build();
    }
    
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


}
