/*  CustomMCPServer.java The primary purpose of this class is to serve as
 *  the Model Context Protocol server for the Vyrtuous spring boot application.
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
package com.brandongcobb.vyrtuous.component.server;

import com.brandongcobb.vyrtuous.Vyrtuous;
import com.brandongcobb.vyrtuous.domain.ToolStatus;
import com.brandongcobb.vyrtuous.service.ToolService;
import com.brandongcobb.vyrtuous.tools.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Logger;

@Component
public class CustomMCPServer {
    
    private static final Logger LOGGER = Logger.getLogger(Vyrtuous.class.getName());
    private final ObjectMapper mapper = new ObjectMapper();
    private boolean initialized = false;
    private final ChatMemory replChatMemory;
    private ToolService toolService;

    @Autowired
    public CustomMCPServer(ChatMemory replChatMemory, ToolService toolService) {
        this.replChatMemory = replChatMemory;
        this.toolService = toolService;
    }
    
    
    private ObjectNode createError(int code, String message) {
        ObjectNode error = mapper.createObjectNode();
        error.put("code", code);
        error.put("message", message);
        return error;
    }

    private ObjectNode createErrorResponse(String id, int code, String message) {
        ObjectNode response = mapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        if (id != null) {
            response.put("id", id);
        }
        response.set("error", createError(code, message));
        return response;
    }

    private CompletableFuture<JsonNode> handleInitialize(JsonNode params) {
        initialized = true;
        ObjectNode result = mapper.createObjectNode();
        result.put("protocolVersion", "2024-11-05");
        ObjectNode capabilities = mapper.createObjectNode();
        capabilities.put("tools", true);
        result.set("capabilities", capabilities);
        ObjectNode serverInfo = mapper.createObjectNode();
        serverInfo.put("name", "Vyrtuous Tool Server");
        serverInfo.put("version", "1.0.0");
        result.set("serverInfo", serverInfo);
        return CompletableFuture.completedFuture(result);
    }

    public void handleRequest(String requestLine, PrintWriter writer) {
        try {
            LOGGER.finer("[JSON-RPC →] " + requestLine);
            JsonNode request = mapper.readTree(requestLine);
            String method = request.get("method").asText();
            String id = request.has("id") ? request.get("id").asText() : null;
            JsonNode params = request.get("params");
            handleInitialize(params); // TODO: This is bad practice.
            CompletableFuture<JsonNode> responseFuture = switch (method) {
                case "initialize" -> handleInitialize(params);
                case "tools/list" -> handleToolsList();
                case "tools/call" -> handleToolCall(params);
                default -> CompletableFuture.completedFuture(createError(-32601, "Method not found"));
            };
            responseFuture.thenAccept(result -> {
                ObjectNode response = mapper.createObjectNode();
                response.put("jsonrpc", "2.0");
                if (id != null) response.put("id", id);
                response.set("result", result);
                LOGGER.finer("[JSON-RPC ←] " + response);
                writer.println(response.toString());
                writer.flush();
            }).exceptionally(ex -> {
                ObjectNode errorResponse = createErrorResponse(id, -32603, "Internal error: " + ex.getMessage());
                LOGGER.severe("[JSON-RPC ← ERROR] " + errorResponse.toString());
                writer.println(errorResponse.toString());
                writer.flush();
                return null;
            });
        } catch (Exception e) {
            ObjectNode errorResponse = createErrorResponse(null, -32700, "Parse error" + e.getMessage());
            LOGGER.severe("[JSON-RPC PARSE ERROR] " + errorResponse.toString());
            writer.flush();
        }
    }
    
    private CompletableFuture<JsonNode> handleToolCall(JsonNode params) {
        if (!initialized) {
            throw new IllegalStateException("Server not initialized");
        }
        try {
            String toolName = params.get("name").asText();
            JsonNode arguments = params.get("arguments");
            return toolService.callTool(toolName, arguments);
        } catch (Exception e) {
            throw new CompletionException(e);
        }
    }
    
    private CompletableFuture<JsonNode> handleToolsList() {
        if (!initialized) {
            return CompletableFuture.completedFuture(createError(-32002, "Server not initialized"));
        }
        ObjectNode result = mapper.createObjectNode();
        ArrayNode toolsArray = mapper.createArrayNode();
        for (CustomTool<?, ?> tool : toolService.getTools()) {
            ObjectNode toolDef = mapper.createObjectNode();
            toolDef.put("name", tool.getName());
            toolDef.put("description", tool.getDescription());
            toolDef.set("inputSchema", tool.getJsonSchema());
            toolsArray.add(toolDef);
        }
        result.set("tools", toolsArray);
        return CompletableFuture.completedFuture(result);
    }
    
    @PostConstruct
    public void initializeTools() {
        toolService.registerTool(new CountFileLines(replChatMemory));
        toolService.registerTool(new CreateFile(replChatMemory));
        toolService.registerTool(new FindInFile(replChatMemory));
        toolService.registerTool(new ListLatexStructure(replChatMemory));
        toolService.registerTool(new Maven(replChatMemory));
        toolService.registerTool(new Patch(replChatMemory));
        toolService.registerTool(new ReadFile(replChatMemory));
        toolService.registerTool(new ReadLatexSegment(replChatMemory));
        toolService.registerTool(new SearchFiles(replChatMemory));
        toolService.registerTool(new SearchWeb(replChatMemory));
        toolService.registerTool(new SummarizeLatexSection(replChatMemory));
    }

    public void start() {
        LOGGER.info("Starting MCP Server");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter writer = new PrintWriter(System.out, true)) {
            String line;
            while ((line = reader.readLine()) != null) {
                handleRequest(line, writer);
            }
        } catch (IOException e) {
            LOGGER.severe("Error in MCP server: " + e.getMessage());
        }
    }

    public static class ToolWrapper {
    
        private static final ObjectMapper mapper = new ObjectMapper();
    
        private final String name;
        private final String description;
        private final JsonNode inputSchema;
        private final ToolExecutor executor;
    
        public ToolWrapper(String name, String description, JsonNode inputSchema, ToolExecutor executor) {
            this.name = name;
            this.description = description;
            this.inputSchema = inputSchema;
            this.executor = executor;
        }
        
        public String getDescription() { return description; }
        public JsonNode getInputSchema() { return inputSchema; }
        public String getName() { return name; }
    
        public CompletableFuture<? extends ToolStatus> execute(JsonNode input) {
            try {
                return executor.execute(input);
            } catch (Exception e) {
                CompletableFuture<ToolStatus> failed = new CompletableFuture<>();
                failed.completeExceptionally(e);
                return failed;
            }
        }
    }

    @FunctionalInterface
    public interface ToolExecutor {
        CompletableFuture<? extends ToolStatus> execute(JsonNode input) throws Exception;
    }

    @Bean
    public List<ToolCallback> tools(ToolService service) {
        return List.of(ToolCallbacks.from(service));
    }
}

