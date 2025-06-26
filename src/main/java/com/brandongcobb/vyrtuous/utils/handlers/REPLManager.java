
/* REPLManager.java The purpose of this class is to serve as the local
 * CLI interface for a variety of AI endpoints.
 *
 * Copyright (C) 2025  github.com/brandongrahamcobb
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * aInteger with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.brandongcobb.vyrtuous.utils.handlers;


import com.brandongcobb.metadata.*;
import com.brandongcobb.vyrtuous.Vyrtuous;
import com.brandongcobb.vyrtuous.domain.*;
import com.brandongcobb.vyrtuous.enums.*;
import com.brandongcobb.vyrtuous.objects.*;
import com.brandongcobb.vyrtuous.tools.*;
import com.brandongcobb.vyrtuous.utils.inc.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class REPLManager {

    private AIManager aim = new AIManager();
    private ApprovalMode approvalMode;;
    private final ExecutorService inputExecutor = Executors.newSingleThreadExecutor();
    private MetadataContainer lastAIResponseContainer = null;
    private List<JsonNode> lastResults;
    private static final Logger LOGGER = Logger.getLogger(Vyrtuous.class.getName());
    private ObjectMapper mapper = new ObjectMapper();
    private MCPServer mcpServer;
    private final ContextManager modelContextManager;
    private String originalDirective;
    private final ExecutorService replExecutor = Executors.newFixedThreadPool(2);
    private final ContextManager userContextManager;

    public void setApprovalMode(ApprovalMode mode) {
        LOGGER.fine("Setting approval mode: " + mode);
        this.approvalMode = mode;
    }

    public REPLManager(ApprovalMode mode, MCPServer server, ContextManager modelContextManager, ContextManager userContextManager) {
        LOGGER.setLevel(Level.FINE);
        for (Handler h : LOGGER.getParent().getHandlers()) {
            h.setLevel(Level.FINE);
        }
        this.approvalMode = mode;
        this.mcpServer = server;
        this.modelContextManager = modelContextManager;
        this.userContextManager = userContextManager;
    }

    public CompletableFuture<Void> startREPL(Scanner scanner, String userInput) {
        System.out.println("Thinking...");
        if (scanner == null) {
            CompletableFuture<Void> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IllegalArgumentException("Scanner cannot be null"));
            return failed;
        }
        if (userInput == null || userInput.isBlank()) {
            return CompletableFuture.completedFuture(null);
        }
        userContextManager.clear();
        originalDirective = userInput;
        userContextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, userInput));
        userInput = null;
        return completeRStepWithTimeout(scanner, true)
            .thenCompose(resp ->
                completeEStep(resp, scanner, true)
                    .thenCompose(eDone ->
                        completePStep(scanner)
                            .thenCompose(pDone ->
                                completeLStep(scanner)
                            )
                    )
            );
    }

    private CompletableFuture<MetadataContainer> completeRStepWithTimeout(Scanner scanner, boolean firstRun) {
        final int maxRetries = 2;
        final long timeout = 600_000;
        CompletableFuture<MetadataContainer> result = new CompletableFuture<>();
        Runnable attempt = new Runnable() {
            int retries = 0;
            @Override
            public void run() {
                retries++;
                completeRStep(scanner, firstRun)
                    .orTimeout(timeout, TimeUnit.SECONDS)
                    .whenComplete((resp, err) -> {
                        if (err != null || resp == null) {
                            userContextManager.clearModified();
                            userContextManager.addEntry(new ContextEntry(ContextEntry.Type.PROGRESSIVE_SUMMARY, "The previous output was greater than the token limit (32768 tokens) and as a result the request failed."));
                            if (retries <= maxRetries) {
                                LOGGER.warning("R-step failed (attempt " + retries + "): " + (err != null ? err.getMessage() : "null response") + ", retrying...");
                                replExecutor.submit(this);
                            } else {
                                LOGGER.severe("R-step permanently failed after " + retries + " attempts.");
                                result.completeExceptionally(err != null ? err : new IllegalStateException("Null result"));
                            }
                        } else {
                            result.complete(resp);
                        }
                    });
            }
        };
        replExecutor.submit(attempt);
        return result;
    }

    private CompletableFuture<MetadataContainer> completeRStep(Scanner scanner, boolean firstRun) {
        LOGGER.fine("Starting R-step, firstRun=" + firstRun);
        String prompt = firstRun ? originalDirective : modelContextManager.buildPromptContext();
        String model = System.getenv("CLI_MODEL");
        String provider = System.getenv("CLI_PROVIDER");
        String requestType = System.getenv("CLI_REQUEST_TYPE");
        CompletableFuture<String> endpointFuture = aim.completeGetAIEndpoint(false, provider, "cli", requestType);
        CompletableFuture<String> instructionsFuture = aim.completeGetInstructions(false, provider, "cli");
        return endpointFuture.thenCombine(instructionsFuture, AbstractMap.SimpleEntry::new).thenCompose(pair -> {
            String endpoint = pair.getKey();
            String instructions = pair.getValue();
            String prevId = null;
            if (!firstRun) {
                MetadataKey<String> previousResponseIdKey = new MetadataKey<>("id", Metadata.STRING);
                prevId = (String) lastAIResponseContainer.get(previousResponseIdKey);
            }
            try {
                return aim
                    .completeRequest(instructions, prompt, prevId, model, requestType, endpoint,
                        Boolean.parseBoolean(System.getenv("CLI_STREAM")), null, provider)
                    .thenCompose(resp -> {
                        if (resp == null) {
                            return CompletableFuture.failedFuture(new IllegalStateException("AI returned null"));
                        }
                        CompletableFuture<String> contentFuture;
                        CompletableFuture<String> responseIdFuture;
                        switch (provider) {
                            case "llama" -> {
                                LlamaUtils llamaUtils = new LlamaUtils(resp);
                                contentFuture = llamaUtils.completeGetContent();
                                responseIdFuture = llamaUtils.completeGetResponseId();
                                String tokensCount = String.valueOf(llamaUtils.completeGetTokens().join());
                                if (!firstRun) {
                                    userContextManager.addEntry(new ContextEntry(ContextEntry.Type.TOKENS, tokensCount));
                                }
                            }
                            case "openai" -> {
                                OpenAIUtils openaiUtils = new OpenAIUtils(resp);
                                contentFuture = openaiUtils.completeGetOutput().thenApply(String.class::cast);
                                responseIdFuture = openaiUtils.completeGetResponseId();
                            }
                            default -> {
                                return CompletableFuture.completedFuture(new MetadataContainer());
                            }
                        }
                        lastAIResponseContainer = resp;
                        return contentFuture.thenCombine(responseIdFuture, (content, responseId) -> {
                            MetadataContainer metadataContainer = new MetadataContainer();
                            boolean validJson = false;
                            JsonNode rootNode;
                            try {
                                List<JsonNode> results = new ArrayList<>();
                                Pattern pattern = Pattern.compile("```json\\s*([\\s\\S]*?)\\s*```", Pattern.DOTALL);
                                Matcher matcher = pattern.matcher(content);
                                while (matcher.find()) {
                                    String jsonText = matcher.group(1).trim();
                                    try {
                                        JsonNode node = mapper.readTree(jsonText);
                                        if (node.isArray()) {
                                            for (JsonNode element : node) {
                                                if (element.has("tool")) {
                                                    results.add(element);
                                                    validJson  = true;
                                                }
                                            }
                                        } else if (node.has("tool")) {
                                            results.add(node);
                                            validJson = true;
                                        }
                                    } catch (Exception e) {
                                        System.err.println("Skipping invalid JSON block: " + e.getMessage());
                                    }
                                }
                                lastResults = results;
                                if (!validJson) {
                                    MetadataKey<String> contentKey = new MetadataKey<>("response", Metadata.STRING);
                                    metadataContainer.put(contentKey, content);
                                    userContextManager.addEntry(new ContextEntry(ContextEntry.Type.AI_RESPONSE, content));
                                    userContextManager.printNewEntries(false, true, true, true, true, true, true, true);
                                } else {
                                    MetadataKey<String> contentKey = new MetadataKey<>("response", Metadata.STRING);
                                    String before = content.substring(0, matcher.start()).replaceAll("[\\n]+$", "");
                                    String after = content.substring(matcher.end()).replaceAll("^[\\n]+", "");
                                    String cleanedText = before + after;
                                    if (cleanedText.equals("")) {
                                        userContextManager.addEntry(new ContextEntry(ContextEntry.Type.AI_RESPONSE, content));
                                        metadataContainer.put(contentKey, content);
                                    }
                                    metadataContainer.put(contentKey, cleanedText);
                                    userContextManager.addEntry(new ContextEntry(ContextEntry.Type.AI_RESPONSE, cleanedText));
                                }
                            } catch (Exception e) {
                            }
                            return metadataContainer;
                        });
                    })
                    .exceptionally(ex -> {
                        LOGGER.severe("completeRequest failed: " + ex.getMessage());
                        throw new CompletionException(ex);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    return CompletableFuture.completedFuture(null);
                }
        });
    }
    
    private CompletableFuture<Void> completeEStep(MetadataContainer response, Scanner scanner, boolean firstRun) {
        LOGGER.fine("Starting E-step");
        String contentStr = new MetadataUtils(response).completeGetContent().join();
        if (contentStr == null && lastResults != null && !lastResults.isEmpty()) {
            List<CompletableFuture<Void>> toolExecutionFutures = new ArrayList<>();
            for (JsonNode toolCallNode : lastResults) {
                try {
                    String toolName = toolCallNode.get("tool").asText();
                    JsonNode toolArguments = toolCallNode.get("input");
                    ObjectNode toolCallRequest = mapper.createObjectNode();
                    toolCallRequest.put("method", "tools/call");
                    ObjectNode params = mapper.createObjectNode();
                    params.put("name", toolName);
                    params.set("arguments", toolArguments);
                    toolCallRequest.set("params", params);
                    if (firstRun) {
                        ObjectNode initRequest = mapper.createObjectNode();
                        initRequest.put("jsonrpc", "2.0");
                        initRequest.put("method", "initialize");
                        initRequest.set("params", mapper.createObjectNode());
                        initRequest.put("id", "init-001");
                        StringWriter initBuffer = new StringWriter();
                        PrintWriter initWriter = new PrintWriter(initBuffer, true);
                        mcpServer.handleRequest(initRequest.toString(), initWriter);
                    }
                    StringWriter outBuffer = new StringWriter();
                    PrintWriter writer = new PrintWriter(outBuffer, true);
                    CountDownLatch latch = new CountDownLatch(1);
                    PrintWriter wrappedWriter = new PrintWriter(new Writer() {
                        @Override
                        public void write(char[] cbuf, int off, int len) throws IOException {
                            outBuffer.write(cbuf, off, len);
                        }
                        @Override
                        public void flush() throws IOException {
                            outBuffer.flush();
                            latch.countDown();
                        }
                        @Override
                        public void close() throws IOException {
                            outBuffer.close();
                        }
                    }, true);
                    String requestJson = toolCallRequest.toString();
                    mcpServer.handleRequest(requestJson, wrappedWriter);
                    latch.await(2, TimeUnit.SECONDS);
                    String responseStr = outBuffer.toString().trim();
                    System.out.println(responseStr);
                    JsonNode responseJson = mapper.readTree(responseStr);
                    CompletableFuture<JsonNode> toolResponseFuture = CompletableFuture.completedFuture(responseJson);
                    CompletableFuture<Void> individualToolFuture = toolResponseFuture.thenAccept(toolResult -> {
                        JsonNode resultNode = toolResult.get("result");
                        if (resultNode == null) {
                            LOGGER.severe("Tool '" + toolName + "' response missing 'result'");
                        } else if (resultNode.has("error")) {
                            String errorMessage = resultNode.get("error").get("message").asText();
                            LOGGER.severe("Tool '" + toolName + "' execution failed: " + errorMessage);
                            modelContextManager.addEntry(new ContextEntry(ContextEntry.Type.TOOL_OUTPUT, "Error executing " + toolName + ": " + errorMessage));
                            userContextManager.addEntry(new ContextEntry(ContextEntry.Type.TOOL_OUTPUT, "Error executing " + toolName + ": " + errorMessage));
                        } else if (resultNode.has("content") && resultNode.get("content").isArray() && resultNode.get("content").size() > 0) {
                            String outputMessage = resultNode.get("content").get(0).get("text").asText();
                            modelContextManager.addEntry(new ContextEntry(ContextEntry.Type.TOOL_OUTPUT, outputMessage));
                            userContextManager.addEntry(new ContextEntry(ContextEntry.Type.TOOL_OUTPUT, outputMessage));
                            LOGGER.fine("Tool '" + toolName + "' executed successfully. Output: " + outputMessage);
                        } else {
                            LOGGER.warning("Tool '" + toolName + "' response does not contain expected content");
                        }
                    }).exceptionally(ex -> {
                        LOGGER.severe("Exception during tool '" + toolName + "' execution: " + ex.getMessage());
                        modelContextManager.addEntry(new ContextEntry(ContextEntry.Type.TOOL_OUTPUT, "Exception during tool " + toolName + ": " + ex.getMessage()));
                        userContextManager.addEntry(new ContextEntry(ContextEntry.Type.TOOL_OUTPUT, "Exception during tool " + toolName + ": " + ex.getMessage()));
                        return null;
                    });
                    toolExecutionFutures.add(individualToolFuture);

                } catch (Exception e) {
                    LOGGER.severe("Error parsing or preparing tool call: " + e.getMessage());
                    toolExecutionFutures.add(CompletableFuture.failedFuture(new RuntimeException("Error preparing tool call: " + e.getMessage())));
                }
            }
            return CompletableFuture.allOf(toolExecutionFutures.toArray(new CompletableFuture[0]))
                    .exceptionally(ex -> {
                        LOGGER.severe("One or more tool executions failed in E-step: " + ex.getMessage());
                        return null;
                    });
        } else {
            lastResults = null;
            LOGGER.fine("No JSON tool available for evaluation, resorting to plaintext...");
            System.out.print("> ");
            String newInput = scanner.nextLine();
            modelContextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, newInput));
            userContextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, newInput));
            return CompletableFuture.completedFuture(null);
        }
    }

    private CompletableFuture<Void> completePStep(Scanner scanner) {
        LOGGER.fine("Print-step");
        userContextManager.printNewEntries(false, true, true, true, true, true, true, true);
        return CompletableFuture.completedFuture(null); // <-- NO looping here!
    }

    private CompletableFuture<Void> completeLStep(Scanner scanner) {
        LOGGER.fine("Loop to R-step");
        return completeRStepWithTimeout(scanner, false)
            .thenCompose(resp ->
                completeEStep(resp, scanner, false)
                    .thenCompose(eDone ->
                        completePStep(scanner)
                            .thenCompose(pDone ->
                                completeLStep(scanner)
                            )
                    )
            );
    }

    public void startResponseInputThread() {
        inputExecutor.submit(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    System.out.print("> ");
                    String input = scanner.nextLine();
                    startREPL(scanner, input)
                        .exceptionally(ex -> {
                            LOGGER.log(Level.SEVERE, "REPL crash", ex);
                            return null;
                        })
                        .join();
                }
            }
        });
    }
    
}
