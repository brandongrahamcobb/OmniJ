/* REPLService.java The purpose of this class is to serve as the local
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

package com.brandongcobb.vyrtuous.service;

import com.brandongcobb.vyrtuous.registry.*;
import com.brandongcobb.metadata.Metadata;
import com.brandongcobb.metadata.MetadataContainer;
import com.brandongcobb.metadata.MetadataKey;
import com.brandongcobb.vyrtuous.Vyrtuous;
import com.brandongcobb.vyrtuous.component.bot.DiscordBot;
import com.brandongcobb.vyrtuous.component.server.CustomMCPServer;
import com.brandongcobb.vyrtuous.utils.handlers.MetadataUtils;
import com.brandongcobb.vyrtuous.utils.handlers.OpenAIUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class REPLService {

    private AIService ais;
    private static final AtomicLong counter = new AtomicLong();
    private MetadataContainer lastAIResponseContainer = null;
    private List<JsonNode> lastResults;
    private static final Logger LOGGER = Logger.getLogger(Vyrtuous.class.getName());
    private ObjectMapper mapper = new ObjectMapper();
    private CustomMCPServer mcpServer;
    private ModelRegistry modelRegistry = new ModelRegistry();
    private CompletableFuture<String> nextInputFuture = null;
    private String originalDirective;
    private ToolService toolService;
    private static ChatMemory replChatMemory = MessageWindowChatMemory.builder().build();
    private final ExecutorService replExecutor = Executors.newFixedThreadPool(2);
    private volatile boolean waitingForInput = false;
    
    public REPLService(CustomMCPServer server, ChatMemory chatMemory, ToolService toolService) {
        this.ais = new AIService(chatMemory, toolService);
        this.toolService = toolService;
        this.replChatMemory = chatMemory;
        this.mcpServer = server;
    }

    /*
     *  Helper
     */
    private void addToolOutput(String content, ChatMemory chatMemory) {
        String uuid = String.valueOf(counter.getAndIncrement());
        ToolResponseMessage.ToolResponse response = new ToolResponseMessage.ToolResponse(uuid, "tool", content);
        ToolResponseMessage toolMsg = new ToolResponseMessage(List.of(response));
        ToolResponseMessage.ToolResponse otherResponse = new ToolResponseMessage.ToolResponse(uuid, "tool", content.length() <= 500 ? content : content.substring(0, 500));
        ToolResponseMessage otherToolMsg = new ToolResponseMessage(List.of(response));
        chatMemory.add("assistant", toolMsg);
        chatMemory.add("user", otherToolMsg);
        printIt();
    }
    /*
     *  Helper
     */
    public String buildContext() {
        List<Message> messages = replChatMemory.get("assistant");
        if (messages.isEmpty()) {
            return "No conversation context available.";
        }
        return messages.stream()
            .map(msg -> {
                if (msg instanceof ToolResponseMessage toolMsg) {
                    var responses = toolMsg.getResponses();
                    if (!responses.isEmpty()) {
                        return msg.getMessageType() + ": " + responses.get(0).responseData();
                    } else {
                        return msg.getMessageType() + ": [no tool response data]";
                    }
                } else {
                    String text = msg.getText();
                    return msg.getMessageType() + ": " + (text != null ? text : "[no text]");
                }
            })
            .collect(Collectors.joining("\n"));
    }
    /*
     *  E-Step
     */
    private CompletableFuture<Void> completeESubStep(JsonNode toolCallNode) {
        LOGGER.finer("Starting E-substep for tool calls...");
        return CompletableFuture.runAsync(() -> {
            String toolName = toolCallNode.get("tool").asText();
            try {
                JsonNode argsNode = toolCallNode.get("arguments");
                if (toolName == null || toolName.isBlank() || argsNode == null || argsNode.isEmpty()) {
                    LOGGER.finer("Skipping tool call with missing or empty name/arguments.");
                    return;
                }
                ObjectNode rpcRequest = mapper.createObjectNode();
                rpcRequest.put("jsonrpc", "2.0");
                rpcRequest.put("method", "tools/call");
                ObjectNode params = rpcRequest.putObject("params");
                params.put("name", toolName);
                params.set("arguments", argsNode);
                String rpcText = rpcRequest.toString();
                LOGGER.finer("[JSON-RPC →] " + rpcText);
                StringWriter buffer = new StringWriter();
                CountDownLatch latch = new CountDownLatch(1);
                PrintWriter out = new PrintWriter(new Writer() {
                    @Override public void write(char[] cbuf, int off, int len)  {
                        buffer.write(cbuf, off, len);
                    }
                    @Override public void flush() {
                        latch.countDown();
                    }
                    @Override public void close() {}
                }, true);
                String responseStr = mcpServer.handleRequest(rpcText).join();
                if (!latch.await(2, TimeUnit.SECONDS)) {
                    String timeoutMsg = "TOOL: [" + toolName + "] Error: Tool execution timed out";
                    LOGGER.severe(timeoutMsg);
                    addToolOutput(timeoutMsg, replChatMemory);
                    return;
                }
                LOGGER.finer("[JSON-RPC ←] " + responseStr);
                if (responseStr.isEmpty()) {
                    String emptyMsg = "TOOL: [" + toolName + "] Error: Empty tool response";
                    LOGGER.severe(emptyMsg);
                    addToolOutput(emptyMsg, replChatMemory);
                    return;
                }
                JsonNode root = mapper.readTree(responseStr);
                JsonNode result = root.path("result");
                String message = result.path("message").asText("No message");
                boolean success = result.path("success").asBoolean(false);
                if (success) {
                    addToolOutput("[" + toolName + "] " + message, replChatMemory);
                    LOGGER.finer("[" + toolName + "] succeeded: " + message);
                } else {
                    addToolOutput("TOOL: [" + toolName + "] Error: " + message, replChatMemory);
                    LOGGER.severe(toolName + " failed: " + message);
                }
            } catch (Exception e) {
                String err = "TOOL: [" + toolName + "] Error: Exception executing tool: " + e.getMessage();
                LOGGER.severe(err);
                addToolOutput(err, replChatMemory);
            }
        }).exceptionally(ex -> {
            LOGGER.severe("completeESubStep (exec) failed: " + ex.getMessage());
            return null;
        });
    }


    private CompletableFuture<Void> completeESubStep(boolean firstRun) {
        LOGGER.finer("Starting E-substep for first run...");
        if (!firstRun) return CompletableFuture.completedFuture(null);
        return CompletableFuture.runAsync(() -> {
            try {
                ObjectNode initRequest = mapper.createObjectNode();
                initRequest.put("jsonrpc", "2.0");
                initRequest.put("method", "initialize");
                initRequest.set("params", mapper.createObjectNode());
                initRequest.put("id", "init-001");
                String responseStr = mcpServer.handleRequest(initRequest.toString()).join();
                if (responseStr.isEmpty()) throw new IOException("Empty initialization response");
                JsonNode responseJson = mapper.readTree(responseStr);
                LOGGER.finer("Initialization completed: " + responseJson.toString());
            } catch (Exception e) {
                LOGGER.severe("Initialization error: " + e.getMessage());
            }
        }).exceptionally(ex -> {
            LOGGER.severe("completeESubStep (init) failed: " + ex.getMessage());
            return null;
        });
    }
    
    private CompletableFuture<Void> completeEStep(MetadataContainer response, boolean firstRun) {
        LOGGER.fine("Starting E-step...");
        return new MetadataUtils(response).completeGetContent().thenCompose(contentStr -> {
            String finishReason = new OpenAIUtils(response).completeGetFinishReason().join();
            if (finishReason != null) {
                if (lastResults != null && !lastResults.isEmpty() && !finishReason.contains("MALFORMED_FUNCTION_CALL")) {
                    List<CompletableFuture<Void>> futures = new ArrayList<>();
                    return completeESubStep(firstRun).thenCompose(v -> {
                        for (JsonNode toolCallNode : lastResults) {
                            futures.add(completeESubStep(toolCallNode));
                        }
                        lastResults = null;
                        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
                    }).exceptionally(ex -> {
                        LOGGER.severe("One or more tool executions failed: " + ex.getMessage());
                        return null;
                    });
                } else {
                    LOGGER.finer("No tools to run, falling back to user input.");
                    waitingForInput = true;
                    nextInputFuture = new CompletableFuture<>();
                    System.out.print("USER: ");
                    return nextInputFuture.thenCompose(userInput -> {
                        replChatMemory.add("assistant", new UserMessage(userInput));
                        replChatMemory.add("user", new UserMessage(userInput));
                        originalDirective = userInput;
                        return completeRStepWithTimeout(false)
                            .thenCompose(resp -> completeEStep(resp, false));
                    });
                }
            }
            return CompletableFuture.completedFuture(null);
        }).exceptionally(ex -> {
            LOGGER.severe("completeEStep failed: " + ex.getMessage());
            return null;
        });
    }
    
    /*
     * L-Step
     */
    private CompletableFuture<Void> completeLStep() {
        LOGGER.fine("Starting L-step...");
        System.out.println("ASSISTANT: Thinking...");
        return completeRStepWithTimeout(false)
            .thenCompose(resp ->
                completeEStep(resp, false)
                    .thenCompose(eDone ->
                        completePStep()
                            .thenCompose(pDone ->
                                completeLStep()
                            )
                    )
            );
    }
    
    /*
     * P-Step
     */
    private CompletableFuture<Void> completePStep() {
        LOGGER.fine("Starting P-step");
        return CompletableFuture.completedFuture(null);
    }
    /*
     *  R-Step
     */
    private CompletableFuture<MetadataContainer> completeRStepWithTimeout(boolean firstRun) {
        final int maxRetries = 2;
        final long timeout = 3600_000;
        CompletableFuture<MetadataContainer> result = new CompletableFuture<>();
        Runnable attempt = new Runnable() {
            int retries = 0;
            @Override
            public void run() {
                retries++;
                completeRStep(firstRun)
                    .orTimeout(timeout, TimeUnit.SECONDS)
                    .whenComplete((resp, err) -> {
                        if (err != null || resp == null) {
                            LOGGER.finer(err.toString());
                            List<Message> originalMessages = replChatMemory.get("assistant");
                            ChatMemory newMemory = MessageWindowChatMemory.builder().build();
                            for (int i = 0; i < originalMessages.size() - 1; i++) {
                                newMemory.add("assistant", originalMessages.get(i));
                            }
                            replChatMemory = newMemory;
                            addToolOutput("The previous output was greater than the token limit (32768 tokens) or errored and as a result the request failed. The last entry has been removed from the context.", replChatMemory);
                            boolean shouldRetry = false;
                            if (resp != null) {
                                String content = resp.get(new MetadataKey<>("response", Metadata.STRING));
                                String finishReason = new OpenAIUtils(resp).completeGetFinishReason().join();
                                if (finishReason != null) {
                                    if (finishReason.contains("MALFORMED_FUNCTION_CALL")) {
                                        LOGGER.warning("Detected MALFORMED_FUNCTION_CALL, retrying...");
                                        result.complete(resp);
                                    }
                                }
                            }
                            if (retries < maxRetries) {
                                retries++;
                                replExecutor.submit(this);
                            } else {
                                LOGGER.severe("completeRStepWithTimeoutfailed: " + retries + " attempts.");
                                result.completeExceptionally(err != null ? err : new IllegalStateException("completeRStepWithTimeoutfailed: " + retries + " attempts."));
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
    
    private CompletableFuture<MetadataContainer> completeRStep(boolean firstRun) {
        LOGGER.fine("Starting R-step, firstRun=" + firstRun);
        String prompt = firstRun ? originalDirective : buildContext();
        String model = System.getenv("CLI_MODEL");
        String provider = System.getenv("CLI_PROVIDER");
        String requestType = System.getenv("CLI_REQUEST_TYPE");
        CompletableFuture<String> endpointFuture = modelRegistry.completeGetAIEndpoint(false, provider, "cli", requestType);
        CompletableFuture<String> instructionsFuture = modelRegistry.completeGetInstructions(false, provider, "cli");
        return endpointFuture.thenCombine(instructionsFuture, AbstractMap.SimpleEntry::new).thenCompose(pair -> {
            String endpoint = pair.getKey();
            String instructions = pair.getValue();
            String prevId = null;
            if (!firstRun) {
                MetadataKey<String> previousResponseIdKey = new MetadataKey<>("id", Metadata.STRING);
                prevId = (String) lastAIResponseContainer.get(previousResponseIdKey);
            }
            try {
                return ais.completeRequest(instructions, prompt, prevId, model, requestType, endpoint,
                        Boolean.parseBoolean(System.getenv("CLI_STREAM")), null, provider)
                    .thenApply(resp -> {
                        if (resp == null) {
                            throw new CompletionException(new IllegalStateException("AI returned null"));
                        }
                        lastAIResponseContainer = resp;
                        OpenAIUtils utils = new OpenAIUtils(resp);
                        String finishReason = utils.completeGetFinishReason().join();
                        String content = utils.completeGetContent().join();
                        this.lastResults = new ArrayList<>();
                        if (content == null || content.isBlank()) {
                            LOGGER.warning("No content in model response.");
                        } else {
                            String toolName = utils.completeGetFunctionName().join();
                            Map<String, Object> toolArgs = utils.completeGetArguments().join();
                            if (toolName != null && toolArgs != null) {
                                ObjectNode toolCallNode = mapper.createObjectNode();
                                toolCallNode.put("tool", toolName);
                                toolCallNode.set("arguments", mapper.valueToTree(toolArgs));
                                lastResults.add(toolCallNode);
                            } else {
                                Pattern jsonBlock = Pattern.compile("```json\\s*([\\s\\S]*?)\\s*```", Pattern.DOTALL);
                                Matcher matcher = jsonBlock.matcher(content);
                                while (matcher.find()) {
                                    String jsonText = matcher.group(1).trim();
                                    try {
                                        JsonNode toolCallNode = mapper.readTree(jsonText);
                                        if (toolCallNode.has("tool") && toolCallNode.has("arguments")) {
                                            lastResults.add(toolCallNode);
                                        }
                                    } catch (Exception e) {
                                        LOGGER.severe("Failed to parse inline tool JSON: " + e.getMessage());
                                    }
                                }
                                if (lastResults.isEmpty()) {
                                    replChatMemory.add("assistant", new AssistantMessage(content));
                                    replChatMemory.add("user", new AssistantMessage(content));
                                    System.out.println(content);
                                }
                            }
                        }

                        MetadataContainer metadata = new MetadataContainer();
                        metadata.put(new MetadataKey<>("finish_reason", Metadata.STRING), finishReason);
                        return metadata;
                    });
            } catch (Exception e) {
                e.printStackTrace();
                return CompletableFuture.completedFuture(new MetadataContainer());
            }
        });
    }
    
    /*
     *  Helper
     */
    public void completeWithUserInput(String input) {
        if (nextInputFuture != null && !nextInputFuture.isDone()) {
            nextInputFuture.complete(input);
            nextInputFuture = null;
            waitingForInput = false;
        }
    }
    
    /*
     *  Helper
     */
    public boolean isWaitingForInput() {
        return nextInputFuture != null && !nextInputFuture.isDone();
    }
    
    /*
     *  Helper
     */
    public static void printIt() {
        List<Message> messages = replChatMemory.get("user");
        Message lastMessage = messages.get(messages.size() - 1);
        String content  = "";
        if (lastMessage instanceof UserMessage userMsg) {
            content = userMsg.getText();
        } else if (lastMessage instanceof AssistantMessage assistantMsg) {
            content = assistantMsg.getText();
        } else if (lastMessage instanceof SystemMessage systemMsg) {
            content = systemMsg.getText();
        } else if (lastMessage instanceof ToolResponseMessage toolResponseMsg) {
            var responses = toolResponseMsg.getResponses();
            if (!responses.isEmpty()) {
                content = responses.get(0).responseData();
            }
        }
        System.out.println(content);
    }

    /*
     * Full-REPL
     */
    public CompletableFuture<Void> startREPL(String userInput) {
        waitingForInput = false;
        System.out.println("ASSISTANT: Thinking...");
        if (userInput == null || userInput.isBlank()) {
            return CompletableFuture.completedFuture(null);
        }
        replChatMemory.clear("assistant");
        replChatMemory.clear("user");
        originalDirective = userInput;
        replChatMemory.add("assistant", new AssistantMessage(userInput));
        replChatMemory.add("user", new AssistantMessage(userInput));
        userInput = null;
        return completeRStepWithTimeout(true)
            .thenCompose(resp ->
                completeEStep(resp, true)
                    .thenCompose(eDone ->
                        completePStep()
                            .thenCompose(pDone ->
                                completeLStep()
                            )
                    )
            );
    }
    
}
