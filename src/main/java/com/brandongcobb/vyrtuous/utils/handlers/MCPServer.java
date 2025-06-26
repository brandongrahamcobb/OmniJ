/*  MCPServer.java The primary purpose of this class is to serve as
 * the Model Context Protocol server for the Vyrtuous spring boot application.
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
package com.brandongcobb.vyrtuous.utils.handlers;

import com.brandongcobb.vyrtuous.domain.*;
import com.brandongcobb.vyrtuous.enums.*;
import com.brandongcobb.vyrtuous.tools.*;
import com.brandongcobb.vyrtuous.utils.handlers.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class MCPServer {
    
    private static final Logger LOGGER = Logger.getLogger(MCPServer.class.getName());
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, ToolWrapper> tools = new ConcurrentHashMap<>();
    private final ContextManager modelContextManager;
    private final ContextManager userContextManager;
    private boolean initialized = false;

    public MCPServer(ContextManager modelContextManager, ContextManager userContextManager) {
        this.modelContextManager = modelContextManager;
        this.userContextManager = userContextManager;
        registerTools();
    }

    private void registerTools() {
        tools.put("create_file", new ToolWrapper(
            "create_file",
            "Create a new file with specified content",
            createCreateFileSchema(),
            (input) -> {
                CreateFileInput createFileInput = mapper.treeToValue(input, CreateFileInput.class);
                CreateFile createFile = new CreateFile(modelContextManager, userContextManager);
                createFileInput.setOriginalJson(input);
                return createFile.run(createFileInput).thenApply(result -> result);
            }
        ));
        tools.put("read_file", new ToolWrapper(
            "read_file",
            "Read the contents of a file",
            createReadFileSchema(),
            (input) -> {
                ReadFileInput readFileInput = mapper.treeToValue(input, ReadFileInput.class);
                ReadFile readFile = new ReadFile(modelContextManager, userContextManager);
                readFileInput.setOriginalJson(input);
                return readFile.run(readFileInput).thenApply(result -> result);
            }
        ));
        tools.put("patch", new ToolWrapper(
            "patch",
            "Apply patches to files",
            createPatchSchema(),
            (input) -> {
                PatchInput patchInput = mapper.treeToValue(input, PatchInput.class);
                Patch patch = new Patch(modelContextManager, userContextManager);
                patchInput.setOriginalJson(input);
                return patch.run(patchInput).thenApply(result -> result);
            }
        ));
        tools.put("load_context", new ToolWrapper(
            "load_context",
            "Load context from a source",
            createLoadContextSchema(),
            (input) -> {
                LoadContextInput loadContextInput = mapper.treeToValue(input, LoadContextInput.class);
                LoadContext loadContext = new LoadContext(modelContextManager, userContextManager);
                loadContextInput.setOriginalJson(input);
                return loadContext.run(loadContextInput).thenApply(result -> result);
            }
        ));
        tools.put("refresh_context", new ToolWrapper(
            "refresh_context",
            "Summarize the context",
            createRefreshContextSchema(),
            (input) -> {
                RefreshContextInput refreshContextInput = mapper.treeToValue(input, RefreshContextInput.class);
                RefreshContext refreshContext = new RefreshContext(modelContextManager, userContextManager);
                refreshContextInput.setOriginalJson(input);
                return refreshContext.run(refreshContextInput).thenApply(result -> result);
            }
        ));
        tools.put("save_context", new ToolWrapper(
            "save_context",
            "Save current context",
            createSaveContextSchema(),
            (input) -> {
                SaveContextInput saveContextInput = mapper.treeToValue(input, SaveContextInput.class);
                saveContextInput.setOriginalJson(input);
                SaveContext saveContext = new SaveContext(modelContextManager, userContextManager);
                return saveContext.run(saveContextInput).thenApply(result -> result);
            }
        ));
        tools.put("search_files", new ToolWrapper(
            "search_files",
            "Search for files matching criteria",
            createSearchFilesSchema(),
            (input) -> {
                SearchFilesInput searchFilesInput = mapper.treeToValue(input, SearchFilesInput.class);
                searchFilesInput.setOriginalJson(input);
                SearchFiles searchFiles = new SearchFiles(modelContextManager, userContextManager);
                return searchFiles.run(searchFilesInput).thenApply(result -> result);
            }
        ));
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

    public void handleRequest(String requestLine, PrintWriter writer) {
        try {
            JsonNode request = mapper.readTree(requestLine);
            String method = request.get("method").asText();
            String id = request.has("id") ? request.get("id").asText() : null;
            JsonNode params = request.get("params");
            CompletableFuture<JsonNode> responseFuture = switch (method) {
                case "initialize" -> handleInitialize(params);
                case "tools/list" -> handleToolsList();
                case "tools/call" -> handleToolCall(params);
                default -> CompletableFuture.completedFuture(createError(-32601, "Method not found"));
            };
            responseFuture.thenAccept(result -> {
                ObjectNode response = mapper.createObjectNode();
                response.put("jsonrpc", "2.0");
                if (id != null) {
                    response.put("id", id);
                }
                if (result.has("error")) {
                    response.set("error", result);
                } else {
                    response.set("result", result);
                }
                writer.println(response.toString());
                writer.flush();
            }).exceptionally(ex -> {
                ObjectNode errorResponse = createErrorResponse(id, -32603, "Internal error: " + ex.getMessage());
                writer.println(errorResponse.toString());
                writer.flush();
                return null;
            });
        } catch (Exception e) {
            ObjectNode errorResponse = createErrorResponse(null, -32700, "Parse error");
            writer.println(errorResponse.toString());
            writer.flush();
        }
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

    private CompletableFuture<JsonNode> handleToolsList() {
        if (!initialized) {
            return CompletableFuture.completedFuture(createError(-32002, "Server not initialized"));
        }
        ObjectNode result = mapper.createObjectNode();
        ArrayNode toolsArray = mapper.createArrayNode();
        for (ToolWrapper tool : tools.values()) {
            ObjectNode toolDef = mapper.createObjectNode();
            toolDef.put("name", tool.getName());
            toolDef.put("description", tool.getDescription());
            toolDef.set("inputSchema", tool.getInputSchema());
            ((com.fasterxml.jackson.databind.node.ArrayNode) toolsArray).add(toolDef);
        }
        result.set("tools", toolsArray);
        return CompletableFuture.completedFuture(result);
    }

    private CompletableFuture<JsonNode> handleToolCall(JsonNode params) {
        if (!initialized) {
            return CompletableFuture.completedFuture(createError(-32002, "Server not initialized"));
        }
        try {
            String toolName = params.get("name").asText();
            JsonNode arguments = params.get("arguments");
            ToolWrapper tool = tools.get(toolName);
            if (tool == null) {
                return CompletableFuture.completedFuture(createError(-32602, "Tool not found: " + toolName));
            }
            return tool.execute(arguments).thenApply(status -> {
                ObjectNode result = mapper.createObjectNode();
                ObjectNode content = mapper.createObjectNode();
                content.put("type", "text");
                content.put("text", ((ToolStatus) status).getMessage());
                ArrayNode contentArray = mapper.createArrayNode();
                ((com.fasterxml.jackson.databind.node.ArrayNode) contentArray).add(content);
                result.set("content", contentArray);
                return (JsonNode) result;
            }).exceptionally(ex -> {
                LOGGER.severe("Tool execution failed: " + ex.getMessage());
                return createError(-32603, "Tool execution failed: " + ex.getMessage());
            });
        } catch (Exception e) {
            return CompletableFuture.completedFuture(createError(-32602, "Invalid parameters"));
        }
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

    // Schema creation methods based on your actual schemas
    private JsonNode createCreateFileSchema() {
        try {
            String schemaJson = """
            {
              "type": "object",
              "required": ["path", "content"],
              "properties": {
                "path": {
                  "type": "string",
                  "description": "The file path where content should be written."
                },
                "content": {
                  "type": "string",
                  "description": "The content to write into the file."
                },
                "overwrite": {
                  "type": "boolean",
                  "default": false,
                  "description": "Whether to overwrite the file if it already exists."
                }
              },
              "additionalProperties": false
            }
            """;
            return mapper.readTree(schemaJson);
        } catch (Exception e) {
            LOGGER.severe("Failed to create create_file schema: " + e.getMessage());
            return mapper.createObjectNode();
        }
    }

    private JsonNode createReadFileSchema() {
        try {
            String schemaJson = """
            {
              "type": "object",
              "required": ["path"],
              "properties": {
                "path": {
                  "type": "string",
                  "description": "The path to the file to be read."
                }
              },
              "additionalProperties": false
            }
            """;
            return mapper.readTree(schemaJson);
        } catch (Exception e) {
            LOGGER.severe("Failed to create read_file schema: " + e.getMessage());
            return mapper.createObjectNode();
        }
    }

    private JsonNode createPatchSchema() {
        try {
            String schemaJson = """
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
            """;
            return mapper.readTree(schemaJson);
        } catch (Exception e) {
            LOGGER.severe("Failed to create patch schema: " + e.getMessage());
            return mapper.createObjectNode();
        }
    }

    private JsonNode createLoadContextSchema() {
        try {
            String schemaJson = """
            {
              "type": "object",
              "required": ["name"],
              "properties": {
                "name": {
                  "type": "string",
                  "description": "The name of the previously saved snapshot to load."
                }
              },
              "additionalProperties": false
            }
            """;
            return mapper.readTree(schemaJson);
        } catch (Exception e) {
            LOGGER.severe("Failed to create load_context schema: " + e.getMessage());
            return mapper.createObjectNode();
        }
    }

    private JsonNode createSaveContextSchema() {
        try {
            String schemaJson = """
            {
              "type": "object",
              "required": ["name"],
              "properties": {
                "name": {
                  "type": "string",
                  "description": "A unique identifier for the context snapshot."
                },
                "description": {
                  "type": "string",
                  "description": "Optional description or annotation for the snapshot."
                }
              },
              "additionalProperties": false
            }
            """;
            return mapper.readTree(schemaJson);
        } catch (Exception e) {
            LOGGER.severe("Failed to create save_context schema: " + e.getMessage());
            return mapper.createObjectNode();
        }
    }

    private JsonNode createSearchFilesSchema() {
        try {
            String schemaJson = """
            {
              "type": "object",
              "required": ["rootDirectory"],
              "properties": {
                "rootDirectory": {
                  "type": "string",
                  "description": "Directory to search from (recursively)."
                },
                "fileExtensions": {
                  "type": "array",
                  "items": { "type": "string" },
                  "description": "Optional file extensions to filter by (e.g. ['.java', '.kt'])."
                },
                "fileNameContains": {
                  "type": "array",
                  "items": { "type": "string" },
                  "description": "Optional substring that must appear in file name."
                },
                "grepContains": {
                  "type": "array",
                  "items": { "type": "string" },
                  "description": "Optional text that must appear in file contents."
                },
                "maxResults": {
                  "type": "integer",
                  "default": 100,
                  "description": "Maximum number of files to return."
                }
              },
              "additionalProperties": false
            }
            """;
            return mapper.readTree(schemaJson);
        } catch (Exception e) {
            LOGGER.severe("Failed to create search_files schema: " + e.getMessage());
            return mapper.createObjectNode();
        }
    }

    private JsonNode createRefreshContextSchema() {
        try {
            String schemaJson = """
            {
                "type": "object",
                "properties": {
                    "progressiveSummary": {
                        "type": "string",
                        "description": "Optional summary content to inject into memory context."
                    }
                },
                "additionalProperties": false
            }
            """;
            return mapper.readTree(schemaJson);
        } catch (Exception e) {
            LOGGER.severe("Failed to create refresh context schema: " + e.getMessage());
            return mapper.createObjectNode();
        }
    }

    private static class ToolWrapper {
        
        private final String name;
        private final String description;
        private final JsonNode inputSchema;
        private final ToolExecutor executor;
        private static final ObjectMapper mapper = new ObjectMapper();

        public ToolWrapper(String name, String description, JsonNode inputSchema, ToolExecutor executor) {
            this.name = name;
            this.description = description;
            this.inputSchema = inputSchema;
            this.executor = executor;
        }

        public String getName() { return name; }
        
        public String getDescription() { return description; }
        
        public JsonNode getInputSchema() { return inputSchema; }
        
        public CompletableFuture<ToolStatus> execute(JsonNode input) {
            try {
                return executor.execute(input);
            } catch (Exception e) {
                e.printStackTrace();
                CompletableFuture<ToolStatus> failedFuture = new CompletableFuture<>();
                failedFuture.completeExceptionally(e);
                return failedFuture;
            }
        }
    }

    @FunctionalInterface
    private interface ToolExecutor {
        CompletableFuture<ToolStatus> execute(JsonNode input) throws Exception;
    }
}

