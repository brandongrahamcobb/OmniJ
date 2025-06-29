
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
import com.brandongcobb.vyrtuous.bots.*;
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
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.logging.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;

public class REPLManager {

    private AIManager aim = new AIManager();
    private JDA api;
    private final ExecutorService inputExecutor = Executors.newSingleThreadExecutor();
    private MetadataContainer lastAIResponseContainer = null;
    private List<JsonNode> lastResults;
    private static final Logger LOGGER = Logger.getLogger(Vyrtuous.class.getName());
    private ObjectMapper mapper = new ObjectMapper();
    private MCPServer mcpServer;
    private MessageManager mem;
    private final ContextManager modelContextManager;
    private String originalDirective;
    private GuildChannel rawChannel;
    private final ExecutorService replExecutor = Executors.newFixedThreadPool(2);
    private final ContextManager userContextManager;
    
    public REPLManager(DiscordBot discordBot, MCPServer server, ContextManager modelContextManager, ContextManager userContextManager) {
        this.api = discordBot.getJDA();
        this.mcpServer = server;
        this.mem = new MessageManager(this.api);
        this.modelContextManager = modelContextManager;
        this.rawChannel = api.getGuildById(System.getenv("REPL_DISCORD_GUILD_ID")).getGuildChannelById(System.getenv("REPL_DISCORD_CHANNEL_ID"));
        this.userContextManager = userContextManager;
    }
    
    /*
     *  Helpers
     */
    private void addToolOutput(String content) {
        modelContextManager.addEntry(new ContextEntry(ContextEntry.Type.TOOL_OUTPUT, content));
        userContextManager.addEntry(new ContextEntry(ContextEntry.Type.TOOL_OUTPUT, content));
    }
    /*
     *  E-Step
     */
    private CompletableFuture<Void> completeESubStep(JsonNode toolCallNode) {
        LOGGER.fine("Starting E-substep for tool calls...");
        return CompletableFuture.runAsync(() -> {
            String toolName = toolCallNode.get("tool").asText();
            try {
                ObjectNode toolCallRequest = mapper.createObjectNode();
                toolCallRequest.put("method", "tools/call");
                ObjectNode params = mapper.createObjectNode();
                params.put("name", toolName);
                params.set("arguments", toolCallNode.get("input"));
                toolCallRequest.set("params", params);
                StringWriter toolBuffer = new StringWriter();
                CountDownLatch latch = new CountDownLatch(1);
                PrintWriter wrappedWriter = new PrintWriter(new Writer() {
                    @Override public void write(char[] cbuf, int off, int len) { toolBuffer.write(cbuf, off, len); }
                    @Override public void flush() { latch.countDown(); }
                    @Override public void close() {}
                }, true);
                mcpServer.handleRequest(toolCallRequest.toString(), wrappedWriter);
                if (!latch.await(2, TimeUnit.SECONDS)) {
                    throw new TimeoutException("Tool execution timed out");
                }
                String responseStr = toolBuffer.toString().trim();
                if (responseStr.isEmpty()) throw new IOException("Empty tool response");
                JsonNode resultNode = mapper.readTree(responseStr).get("result");
                if (resultNode == null) throw new IllegalStateException("Missing 'result' node");
                if (resultNode.has("error")) {
                    String msg = resultNode.get("error").get("message").asText();
                    addToolOutput("Error executing " + toolName + ": " + msg);
                    LOGGER.severe(msg);
                } else if (resultNode.has("content") && resultNode.get("content").isArray()) {
                    String output = resultNode.get("content").get(0).get("text").asText();
                    addToolOutput(output);
                    LOGGER.fine("Tool output: " + output);
                } else {
                    LOGGER.warning("Tool '" + toolName + "' returned unexpected result format");
                }
            } catch (Exception e) {
                String msg = "Exception executing tool '" + toolName + "': " + e.getMessage();
                LOGGER.severe(msg);
                addToolOutput(msg);
            }
        }).exceptionally(ex -> {
            LOGGER.severe("Exception executing tool: " + ex.getMessage());
            return null;
        });
    }

    private CompletableFuture<Void> completeESubStep(boolean firstRun) {
        LOGGER.fine("Starting E-substep for first run...");
        if (!firstRun) return CompletableFuture.completedFuture(null);
        return CompletableFuture.runAsync(() -> {
            try {
                ObjectNode initRequest = mapper.createObjectNode();
                initRequest.put("jsonrpc", "2.0");
                initRequest.put("method", "initialize");
                initRequest.set("params", mapper.createObjectNode());
                initRequest.put("id", "init-001");
                StringWriter initBuffer = new StringWriter();
                PrintWriter initWriter = new PrintWriter(initBuffer, true);
                mcpServer.handleRequest(initRequest.toString(), initWriter);
                String responseStr = initBuffer.toString().trim();
                if (responseStr.isEmpty()) throw new IOException("Empty initialization response");
                JsonNode responseJson = mapper.readTree(responseStr);
                LOGGER.fine("Initialization completed: " + responseJson.toString());
            } catch (Exception e) {
                LOGGER.severe("Initialization error: " + e.getMessage());
            }
        }).exceptionally(ex -> {
            LOGGER.severe("Initialization error: " + ex.getMessage());
            return null;
        });
    }
    
    private CompletableFuture<Void> completeEStep(MetadataContainer response, Scanner scanner, boolean firstRun) {
        LOGGER.fine("Starting E-step...");
        return new MetadataUtils(response).completeGetContent().thenCompose(contentStr -> {
            /*
             *  Null Checks
             */
            if ((contentStr == null || contentStr.isBlank()) && lastResults != null && !lastResults.isEmpty()) {
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                return completeESubStep(firstRun).thenCompose(v -> {
                    for (JsonNode toolCallNode : lastResults) {
                        futures.add(completeESubStep(toolCallNode));
                    }
                    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                            .exceptionally(ex -> {
                                LOGGER.severe("One or more tool executions failed: " + ex.getMessage());
                                return null;
                            });
                });
            } else {
                LOGGER.fine("No tools to run, falling back to user input");
                System.out.print("> ");
                String newInput = scanner.nextLine();
                modelContextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, newInput));
                userContextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, newInput));
                mem.completeSendResponse(rawChannel, userContextManager.generateNewEntry(true, true, true, true, true, true, true, true));
                return CompletableFuture.completedFuture(null);
            }
        }).exceptionally(ex -> {
            LOGGER.severe("Tool call error: " + ex.getMessage());
            return null;
        });
    }
    
    /*
     * L-Step
     */
    private CompletableFuture<Void> completeLStep(Scanner scanner) {
        LOGGER.fine("Starting L-step...");
        System.out.println(Vyrtuous.BLURPLE + "Thinking..." + Vyrtuous.RESET);
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
    
    /*
     * P-Step
     */
    private CompletableFuture<Void> completePStep(Scanner scanner) {
        LOGGER.fine("Starting P-step");
        userContextManager.printNewEntries(false, true, true, true, true, true, true, true);
        return CompletableFuture.completedFuture(null); // <-- NO looping here!
    }

    /*
     *  R-Step
     */
    private CompletableFuture<MetadataContainer> completeRStepWithTimeout(Scanner scanner, boolean firstRun) {
        final int maxRetries = 2;
        final long timeout = 300_000;
        CompletableFuture<MetadataContainer> result = new CompletableFuture<>();
        Runnable attempt = new Runnable() {
            int retries = 0;
            @Override
            public void run() {
                retries++;
                completeRStep(scanner, firstRun)
                    .orTimeout(timeout, TimeUnit.SECONDS)
                    .whenComplete((resp, err) -> {
                        /*
                         *  Null Checks
                         */
                        if (err != null || resp == null) {
                            userContextManager.clearModified();
                            modelContextManager.addEntry(new ContextEntry(ContextEntry.Type.PROGRESSIVE_SUMMARY, "The previous output was greater than the token limit (32768 tokens) and as a result the request failed."));
                            userContextManager.addEntry(new ContextEntry(ContextEntry.Type.PROGRESSIVE_SUMMARY, "The previous output was greater than the token limit (32768 tokens) and as a result the request failed."));
                            mem.completeSendResponse(rawChannel, userContextManager.generateNewEntry(true, true, true, true, true, true, true, true));
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
        boolean stream = Boolean.parseBoolean(System.getenv("CLI_STREAM"));
        CompletableFuture<String> endpointFuture = aim.completeGetAIEndpoint(false, provider, "cli", requestType);
        CompletableFuture<String> instructionsFuture = aim.completeGetInstructions(false, provider, "cli");
        CompletableFuture<String> responseIdFuture = firstRun
            ? CompletableFuture.completedFuture(null)
            : new MetadataUtils(lastAIResponseContainer).completeGetPreviousResponseId();
        return endpointFuture
            .thenCombine(instructionsFuture, AbstractMap.SimpleEntry::new)
            .thenCombine(responseIdFuture, (pair, responseId) -> new Object[]{ pair.getKey(), pair.getValue(), responseId })
            .thenCompose(tuple -> {
                try {
                    String endpoint = (String) tuple[0];
                    String instructions = (String) tuple[1];
                    String responseId = (String) tuple[2];
                    return aim.completeRequest(instructions, prompt, responseId, model, requestType, endpoint, stream, null, provider)
                        .thenCompose(resp -> {
                            LOGGER.fine("Completing request...");
                            if (resp == null) {
                                return CompletableFuture.failedFuture(new IllegalStateException("AI returned null"));
                            }
                            lastAIResponseContainer = resp;
                            return completeRSubStep(resp);
                        });
                } catch (Exception e) {
                    LOGGER.severe("Exception in thenCompose: " + e.getMessage());
                    return CompletableFuture.failedFuture(e);
                }
            })
            .exceptionally(ex -> {
                LOGGER.severe("Exception in completeRStep: " + ex.getMessage());
                ex.printStackTrace();
                return new MetadataContainer(); // fallback
            });
    }
    
    private CompletableFuture<LlamaContainer> completeRSubStep(LlamaContainer container) {
        LOGGER.fine("Starting R-substep with Llama...");
        LlamaUtils llamaUtils = new LlamaUtils(container);
        CompletableFuture<String> contentFuture = llamaUtils.completeGetContent();
        CompletableFuture<String> responseIdFuture = llamaUtils.completeGetResponseId();
        CompletableFuture<Integer> tokensFuture = llamaUtils.completeGetTokens();
        return contentFuture.thenCombine(responseIdFuture, (content, responseId) -> {
            MetadataContainer metadataContainer = new MetadataContainer();
            metadataContainer.put(new MetadataKey<>("content", Metadata.STRING), content);
            metadataContainer.put(new MetadataKey<>("id", Metadata.STRING), responseId);
            return metadataContainer;
        }).thenCombine(tokensFuture, (metadataContainer, tokens) -> {
            String tokenCount = String.valueOf(tokens);
            modelContextManager.addEntry(new ContextEntry(ContextEntry.Type.TOKENS, tokenCount));
            userContextManager.addEntry(new ContextEntry(ContextEntry.Type.TOKENS, tokenCount));
            return (LlamaContainer) metadataContainer;
        })
        .exceptionally(ex -> {
            LOGGER.severe("Exception in completeRSubStep: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        });
    }
      
    private CompletableFuture<MetadataContainer> completeRSubStep(MetadataContainer container) {
        LOGGER.fine("Starting async R-substep JSON response handling...");
        return new MetadataUtils(container).completeGetContent().thenCompose(content ->
            CompletableFuture.supplyAsync(() -> {
                boolean validJson = false;
                List<JsonNode> results = new ArrayList<>();
                Pattern pattern = Pattern.compile("```json\\s*([\\s\\S]*?)\\s*```", Pattern.DOTALL);
                Matcher matcher = pattern.matcher(content);

                int matchStart = -1;
                int matchEnd = -1;

                while (matcher.find()) {
                    matchStart = matcher.start();
                    matchEnd = matcher.end();

                    String jsonText = matcher.group(1).trim();
                    try {
                        JsonNode node = mapper.readTree(jsonText);
                        if (node.isArray()) {
                            for (JsonNode element : node) {
                                if (element.has("tool")) {
                                    results.add(element);
                                    validJson = true;
                                }
                            }
                        } else if (node.has("tool")) {
                            results.add(node);
                            validJson = true;
                        }
                    } catch (Exception e) {
                        LOGGER.warning("Skipping invalid JSON block: " + e.getMessage());
                    }
                }

                lastResults = results;
                MetadataKey<String> contentKey = new MetadataKey<>("content", Metadata.STRING);

                if (!validJson || matchStart == -1 || matchEnd == -1) {
                    LOGGER.fine("Invalid JSON from container...");
                    container.put(contentKey, content);
                    modelContextManager.addEntry(new ContextEntry(ContextEntry.Type.AI_RESPONSE, content));
                    userContextManager.addEntry(new ContextEntry(ContextEntry.Type.AI_RESPONSE, content));
                    mem.completeSendResponse(rawChannel, userContextManager.generateNewEntry(true, true, true, true, true, true, true, true));
                    userContextManager.printNewEntries(false, true, true, true, true, true, true, true);
                } else {
                    LOGGER.fine("Valid JSON from container...");
                    String before = content.substring(0, matchStart).replaceAll("[\\n]+$", "");
                    String after = content.substring(matchEnd).replaceAll("^[\\n]+", "");
                    String cleanedText = before + after;

                    container.put(contentKey, cleanedText);
                    modelContextManager.addEntry(new ContextEntry(ContextEntry.Type.AI_RESPONSE, cleanedText));
                    userContextManager.addEntry(new ContextEntry(ContextEntry.Type.AI_RESPONSE, cleanedText));
                    mem.completeSendResponse(rawChannel, userContextManager.generateNewEntry(true, true, true, true, true, true, true, true));
                }

                return container;
            })
        ).exceptionally(ex -> {
            LOGGER.severe("Exception processing JSON response: " + ex.getMessage());
            return container;
        });
    }

    
    private CompletableFuture<OpenAIContainer> completeRSubStep(OpenAIContainer container) {
        LOGGER.fine("Starting R-substep with OpenAI...");
        OpenAIUtils openaiUtils = new OpenAIUtils(container);
        CompletableFuture<String> contentFuture = openaiUtils.completeGetOutput().thenApply(String.class::cast);
        CompletableFuture<String> responseIdFuture = openaiUtils.completeGetResponseId();
        return contentFuture.thenCombine(responseIdFuture, (content, responseId) -> {
            MetadataContainer metadataContainer = new MetadataContainer();
            metadataContainer.put(new MetadataKey<>("content", Metadata.STRING), content);
            metadataContainer.put(new MetadataKey<>("id", Metadata.STRING), responseId);
            return (OpenAIContainer) metadataContainer;
        });
    }
    
    /*
     * Full-REPL
     */
    private CompletableFuture<Void> startREPL(Scanner scanner, String userInput) {
        System.out.println(Vyrtuous.BLURPLE + "Thinking..." + Vyrtuous.RESET);
        /*
         *  Null Checks
         */
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
        modelContextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, userInput));
        userContextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, userInput));
        mem.completeSendResponse(rawChannel, userContextManager.generateNewEntry(true, true, true, true, true, true, true, true));
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
