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
package com.brandongcobb.omnij.component.server;

import com.brandongcobb.omnij.*;
import com.brandongcobb.omnij.component.bot.*;
import com.brandongcobb.omnij.domain.*;
import com.brandongcobb.omnij.service.*;
import com.brandongcobb.omnij.tools.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Logger;

@Component
public class CustomMCPServer {
    
    private static final Logger LOGGER = Logger.getLogger(Application.class.getName());
    private final ObjectMapper mapper = new ObjectMapper();
    private boolean initialized = false;
    private final ChatMemory replChatMemory;
    private ToolService toolService;
    private MessageService mess;
    private GuildChannel rawChannel;

    @Autowired
    public CustomMCPServer(DiscordBot discordBot, ChatMemory replChatMemory, ToolService toolService) {
        this.replChatMemory = replChatMemory;
        this.toolService = toolService;
        initializeTools();
    }
    
    
    private ObjectNode createError(int code, String message) {
        ObjectNode error = mapper.createObjectNode();
        error.put("code", code);
        error.put("message", message);
        return error;
    }

    private ObjectNode createErrorResponse(JsonNode idNode, int code, String message) {
        ObjectNode response = mapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        if (idNode != null && !idNode.isNull()) {
            response.set("id", idNode);
        }
        response.set("error", createError(code, message));
        return response;
    }


    private CompletableFuture<JsonNode> handleInitialize(JsonNode params, String id) {
        initialized = true;
        ObjectNode result = mapper.createObjectNode();
        result.put("protocolVersion", params.path("protocolVersion").asText("2025-06-18"));

        ObjectNode capabilities = mapper.createObjectNode();

        ObjectNode toolsCapabilities = mapper.createObjectNode();
        toolsCapabilities.put("canExecute", true);

        ArrayNode toolNames = mapper.createArrayNode();
        for (CustomTool<?, ?> tool : toolService.getTools()) {
            toolNames.add(tool.getName());
        }
        toolsCapabilities.set("toolNames", toolNames);

        capabilities.set("tools", toolsCapabilities);

        ObjectNode serverInfo = mapper.createObjectNode();
        serverInfo.put("name", "vyrtuous");
        serverInfo.put("version", "1.0.0");

        result.set("capabilities", capabilities);
        result.set("serverInfo", serverInfo);

        ObjectNode response = mapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        response.set("result", result);

        return CompletableFuture.completedFuture(response);
    }
    
    private void sendError(PrintWriter writer, JsonNode idNode, int code, String message) {
        ObjectNode errorResponse = createErrorResponse(idNode, code, message);
        try {
            writer.println(mapper.writeValueAsString(errorResponse));
            writer.flush();
        } catch (Exception e) {
            LOGGER.severe("Failed to send error response: " + e.getMessage());
        }
    }

    
    public CompletableFuture<String> handleRequest(String requestLine) {
        CompletableFuture<String> resultFuture = new CompletableFuture<>();
        try {
            LOGGER.finer("[JSON-RPC →] " + requestLine);
            JsonNode request = mapper.readTree(requestLine);
            String method = request.get("method").asText();
            JsonNode idNode = request.get("id");
            boolean isNotification = method.equals("notifications/initialized");
            JsonNode params = request.get("params");

            CompletableFuture<JsonNode> responseFuture = switch (method) {
                case "initialize" -> handleInitialize(params, idNode != null ? idNode.asText() : null);
                case "tools/list" -> handleToolsList(idNode != null ? idNode.asText() : null);
                case "tools/call" -> handleToolCall(params, idNode != null ? idNode.asText() : null);
                case "notifications/initialized" -> {
                    LOGGER.info("Received notifications/initialized → ignoring");
                    yield CompletableFuture.completedFuture(null); // skip response
                }
                default -> CompletableFuture.completedFuture(createErrorResponse(idNode, -32601, "Method not found"));
            };

            responseFuture.thenAccept(responseJson -> {
                if (isNotification) {
                    resultFuture.complete(null); // No response expected
                    return;
                }
                try {
                    String jsonString = mapper.writeValueAsString(responseJson);
                    LOGGER.finer("[JSON-RPC ←] " + jsonString);
                    resultFuture.complete(jsonString);
                } catch (JsonProcessingException e) {
                    LOGGER.severe("JSON serialization error: " + e.getMessage());
                    resultFuture.completeExceptionally(e);
                }
            }).exceptionally(ex -> {
                LOGGER.severe("Internal error: " + ex.getMessage());
                resultFuture.completeExceptionally(ex);
                return null;
            });
        } catch (Exception e) {
            LOGGER.severe("Failed to parse request: " + e.getMessage());
            resultFuture.completeExceptionally(e);
        }

        return resultFuture;
    }
    
//    private CompletableFuture<JsonNode> handleToolCall(JsonNode params) {
//        if (!initialized) {
//            throw new IllegalStateException("Server not initialized");
//        }
//        try {
//            String toolName = params.get("name").asText();
//            JsonNode arguments = params.get("arguments");
//            return toolService.callTool(toolName, arguments);
//        } catch (Exception e) {
//            throw new CompletionException(e);
//        }
//    }
    private CompletableFuture<JsonNode> handleToolCall(JsonNode params, String id) {
        if (!initialized) {
            throw new IllegalStateException("Server not initialized");
        }
        try {
            String toolName = params.get("name").asText();
            JsonNode arguments = params.get("arguments");
            return toolService.callTool(toolName, arguments)
                .thenApply(toolResult -> {
                    ObjectNode response = mapper.createObjectNode();
                    response.put("jsonrpc", "2.0");
                    response.put("id", id);
                    response.set("result", toolResult);
                    LOGGER.finer(response.toString());
                    return response;
                });
        } catch (Exception e) {
            throw new CompletionException(e);
        }
    }

    private CompletableFuture<JsonNode> handleToolsList(String id) {
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

        ObjectNode response = mapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        response.set("result", result);

        return CompletableFuture.completedFuture(response);
    }

    
    public void initializeTools() {
        toolService.registerTool(new CountFileLines(replChatMemory));
        toolService.registerTool(new CreateFile(replChatMemory));
        toolService.registerTool(new DiffFiles(replChatMemory));
        toolService.registerTool(new FindInFile(replChatMemory));
        toolService.registerTool(new ListLatexStructure(replChatMemory));
        toolService.registerTool(new PDFLatex(replChatMemory));
        toolService.registerTool(new Maven(replChatMemory));
        toolService.registerTool(new Patch(replChatMemory));
        toolService.registerTool(new ReadFile(replChatMemory));
        toolService.registerTool(new ReadFileLines(replChatMemory));
        toolService.registerTool(new ReadLatexSegment(replChatMemory));
        toolService.registerTool(new SearchFiles(replChatMemory));
        toolService.registerTool(new SearchWeb(replChatMemory));
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

