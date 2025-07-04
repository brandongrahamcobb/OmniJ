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

import com.brandongcobb.metadata.Metadata;
import com.brandongcobb.metadata.MetadataContainer;
import com.brandongcobb.metadata.MetadataKey;
import com.brandongcobb.vyrtuous.Vyrtuous;
import com.brandongcobb.vyrtuous.component.bot.DiscordBot;
import com.brandongcobb.vyrtuous.component.server.CustomMCPServer;
import com.brandongcobb.vyrtuous.records.ToolCall;
import com.brandongcobb.vyrtuous.utils.handlers.LlamaUtils;
import com.brandongcobb.vyrtuous.utils.handlers.MetadataUtils;
import com.brandongcobb.vyrtuous.utils.handlers.OpenAIUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
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
    private static final Pattern ALREADY_ESCAPED = Pattern.compile("\\\\([\\\\\"`])");
    private JDA api;
    private static final AtomicLong counter = new AtomicLong();
    private final ExecutorService inputExecutor = Executors.newSingleThreadExecutor();
    private MetadataContainer lastAIResponseContainer = null;
    private List<JsonNode> lastResults;
    private static final Logger LOGGER = Logger.getLogger(Vyrtuous.class.getName());
    private ObjectMapper mapper = new ObjectMapper();
    private CustomMCPServer mcpServer;
    private MessageService mess;
    private String originalDirective;
    private ToolService toolService;
    private GuildChannel rawChannel;
    private static ChatMemory replChatMemory = MessageWindowChatMemory.builder().build();
    private final ExecutorService replExecutor = Executors.newFixedThreadPool(2);
    
    public REPLService(DiscordBot discordBot, CustomMCPServer server, ChatMemory chatMemory, ToolService toolService) {
        this.api = discordBot.getJDA();
        this.ais = new AIService(chatMemory, toolService);
        this.toolService = toolService;
        this.replChatMemory = chatMemory;
        this.mcpServer = server;
        this.mess = new MessageService(this.api);
        this.rawChannel = api.getGuildById(System.getenv("REPL_DISCORD_GUILD_ID")).getGuildChannelById(System.getenv("REPL_DISCORD_CHANNEL_ID"));
    }
    
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
     *  Helpers
     */
    private void addToolOutput(String content) {
        String uuid = String.valueOf(counter.getAndIncrement());
        ToolResponseMessage.ToolResponse response = new ToolResponseMessage.ToolResponse(uuid, "tool", content);
        ToolResponseMessage toolMsg = new ToolResponseMessage(List.of(response));
        replChatMemory.add("assistant", toolMsg);
        replChatMemory.add("user", toolMsg);
        printIt();
        mess.completeSendResponse(rawChannel, content);
    }
    
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
                params.set("arguments", toolCallNode.get("arguments"));
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
                mcpServer.handleRequest(rpcText, out);
                if (!latch.await(2, TimeUnit.SECONDS)) {
                    throw new TimeoutException("Tool execution timed out");
                }
                String responseStr = buffer.toString().trim();
                LOGGER.finer("[JSON-RPC ←] " + responseStr);
                if (responseStr.isEmpty()) {
                    throw new IOException("Empty tool response");
                }
                JsonNode root   = mapper.readTree(responseStr);
                JsonNode result = root.get("result");
                if (result == null) {
                    throw new IllegalStateException("Missing 'result' in tool response");
                }
                String  message = result.path("message").asText("No message");
                boolean success = result.path("success").asBoolean(false);
                if (success) {
                    addToolOutput("[" + toolName + "] " + message);
                    LOGGER.finer("[" + toolName + "] succeeded: " + message);
                } else {
                    addToolOutput("[" + toolName + " ERROR] " + message);
                    LOGGER.severe(toolName + " failed: " + message);
                }
            } catch (Exception e) {
                String err = "Exception executing tool '" + toolName + "': " + e.getMessage();
                LOGGER.severe(err);
                addToolOutput(err);
            }
        }).exceptionally(ex -> {
            LOGGER.severe("Exception in E-substep thread: " + ex.getMessage());
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
                StringWriter initBuffer = new StringWriter();
                PrintWriter initWriter = new PrintWriter(initBuffer, true);
                mcpServer.handleRequest(initRequest.toString(), initWriter);
                String responseStr = initBuffer.toString().trim();
                if (responseStr.isEmpty()) throw new IOException("Empty initialization response");
                JsonNode responseJson = mapper.readTree(responseStr);
                LOGGER.finer("Initialization completed: " + responseJson.toString());
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
            if (lastResults != null && !lastResults.isEmpty()) {
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                return completeESubStep(firstRun).thenCompose(v -> {
                    for (JsonNode toolCallNode : lastResults) {
                        futures.add(completeESubStep(toolCallNode));
                        lastResults = null;
                    }
                    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
                }).exceptionally(ex -> {
                    LOGGER.severe("One or more tool executions failed: " + ex.getMessage());
                    return null;
                });
            } else {
                LOGGER.finer("No tools to run, falling back to user input.");
                System.out.print("> ");
                String newInput = scanner.nextLine();
                replChatMemory.add("assistant", new UserMessage(newInput));
                replChatMemory.add("user", new UserMessage(newInput));
                mess.completeSendResponse(rawChannel, newInput);
            }
            return CompletableFuture.completedFuture(null);
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
        return CompletableFuture.completedFuture(null);
    }
    /*
     *  R-Step
     */
    private CompletableFuture<MetadataContainer> completeRStepWithTimeout(Scanner scanner, boolean firstRun) {
        final int maxRetries = 2;
        final long timeout = 3600_000;
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
//                            List<Message> originalMessages = replChatMemory.get("assistant");
//                            ChatMemory newMemory = MessageWindowChatMemory.builder().build();
//                            for (int i = 0; i < originalMessages.size() - 1; i++) {
//                                newMemory.add("assistant", originalMessages.get(i));
//                            }
//                            replChatMemory = newMemory;
//                            addToolOutput("The previous output was greater than the token limit (32768 tokens) and as a result the request failed. The last entry has been removed from the context.");
//                            printIt();
//                            mess.completeSendResponse(rawChannel, "[SUMMARY]: The previous output was greater than the token limit (32768 tokens) and as a result the request failed. The last entry has been removed from the context.");
//                            if (retries <= maxRetries) {
//                                LOGGER.warning("R-step failed (attempt " + retries + "): " + (err != null ? err.getMessage() : "null response") + ", retrying...");
//                                replExecutor.submit(this);
//                            } else {
//                                LOGGER.severe("R-step permanently failed after " + retries + " attempts.");
                            result.completeExceptionally(err != null ? err : new IllegalStateException("Null result"));
//                            }
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
        String prompt = firstRun ? originalDirective : buildContext();
        String model = System.getenv("CLI_MODEL");
        String provider = System.getenv("CLI_PROVIDER");
        String requestType = System.getenv("CLI_REQUEST_TYPE");
        CompletableFuture<String> endpointFuture = ais.completeGetAIEndpoint(false, provider, "cli", requestType);
        CompletableFuture<String> instructionsFuture = ais.completeGetInstructions(false, provider, "cli");
        return endpointFuture.thenCombine(instructionsFuture, AbstractMap.SimpleEntry::new).thenCompose(pair -> {
            String endpoint = pair.getKey();
            String instructions = pair.getValue();
            String prevId = null;
            if (!firstRun) {
                MetadataKey<String> previousResponseIdKey = new MetadataKey<>("id", Metadata.STRING);
                prevId = (String) lastAIResponseContainer.get(previousResponseIdKey);
            }
            try {
                return ais.completeRequest(instructions, prompt, prevId, model, requestType, endpoint, Boolean.parseBoolean(System.getenv("CLI_STREAM")), null, provider)
                    .thenCompose(resp -> {
                        if (resp == null) {
                            return CompletableFuture.failedFuture(new IllegalStateException("AI returned null"));
                        }
                        CompletableFuture<String> contentFuture;
                        CompletableFuture<String> responseIdFuture;
                        switch (provider) {
                            case "google" -> {
                                OpenAIUtils openaiUtils = new OpenAIUtils(resp);
                                contentFuture = openaiUtils.completeGetContent().thenApply(String.class::cast);
                                responseIdFuture = openaiUtils.completeGetResponseId();
                                String tokensCount = String.valueOf(openaiUtils.completeGetTokens().join());
                                if (!firstRun) {
                                  //  replChatMemory.add("assistant", new AssistantMessage("[Tokens]: " + tokensCount));
                                    replChatMemory.add("user", new AssistantMessage(tokensCount));
                                 //   printIt();
                                    mess.completeSendResponse(rawChannel, "[Tokens]: " + tokensCount);
                                }
                            }
                            case "llama" -> {
                                LlamaUtils llamaUtils = new LlamaUtils(resp);
                                contentFuture = llamaUtils.completeGetContent();
                                responseIdFuture = llamaUtils.completeGetResponseId();
                                String tokensCount = String.valueOf(llamaUtils.completeGetTokens().join());
                                if (!firstRun) {
                                    replChatMemory.add("assistant", new AssistantMessage("[Tokens]: " + tokensCount));
                                    replChatMemory.add("user", new AssistantMessage("[Tokens]: " + tokensCount));
                                    printIt();
                                    mess.completeSendResponse(rawChannel, "[Tokens]: " + tokensCount);
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
                            LOGGER.finer(content);
                            MetadataContainer metadataContainer = new MetadataContainer();
                            boolean hasContent = content != null && !content.trim().isEmpty();
                            List<JsonNode> lastResults = new ArrayList<>();
                            int i = 0;
                            boolean validJson = false;
                            OpenAIUtils openaiUtils = new OpenAIUtils(resp);
                            String name = openaiUtils.completeGetFunctionName().join();
                            Map<String, Object> args = openaiUtils.completeGetArguments().join();
                            if (name == null || args == null) {
                                Pattern pattern = Pattern.compile("```json\\s*([\\s\\S]*?)\\s*```", Pattern.DOTALL);
                                Matcher matcher = pattern.matcher(content != null ? content : "");
                                while (matcher.find()) {
                                    String jsonText = matcher.group(1).trim();
                                    try {
                                        JsonNode node = mapper.readTree(jsonText);
                                        if (node.isArray()) {
                                            for (JsonNode element : node) {
                                                if (element.has("tool")) {
                                                    lastResults.add(element);
                                                    validJson = true;
                                                }
                                            }
                                        } else if (node.has("tool")) {
                                            lastResults.add(node);
                                            validJson = true;
                                        }
                                    } catch (Exception e) {
                                        LOGGER.severe("Skipping invalid JSON block: " + e.getMessage());
                                    }
                                }
                            } else {
                                try {
                                    JsonNode argsNode = mapper.valueToTree(args);
                                    ObjectNode toolCallNode = mapper.createObjectNode();
                                    toolCallNode.put("tool", name);
                                    toolCallNode.set("arguments", argsNode);
                                    lastResults.add(toolCallNode);

                                    ToolCall toolCall = new ToolCall(name, argsNode);
                                    toolService.callTool(toolCall.name(), toolCall.arguments()).join();
                                } catch (Exception e) {
                                    LOGGER.severe("Invalid JSON in tool arguments for tool_call[" + i + "]: " + e.getMessage());
                                }
                            }
                            if (!validJson && hasContent) {
                                MetadataKey<String> contentKey = new MetadataKey<>("response", Metadata.STRING);
                                metadataContainer.put(contentKey, content);
                                replChatMemory.add("assistant", new AssistantMessage(content));
                                replChatMemory.add("user", new AssistantMessage(content));
                                printIt();
                                mess.completeSendResponse(rawChannel, content);
                            } else {
                                MetadataKey<String> contentKey = new MetadataKey<>("response", Metadata.STRING);
                                String cleanedText = "";
                                if (name == null || args == null) {
                                    Pattern pattern = Pattern.compile("```json\\s*([\\s\\S]*?)\\s*```", Pattern.DOTALL);
                                    Matcher matcher = pattern.matcher(content != null ? content : "");
                                    if (matcher.find()) {
                                        String before = content.substring(0, matcher.start()).replaceAll("[\\n]+$", "");
                                        String after = content.substring(matcher.end()).replaceAll("^[\\n]+", "");
                                        cleanedText = before + after;
                                    } else {
                                        cleanedText = content != null ? content : "";
                                    }
                                }
                                metadataContainer.put(contentKey, cleanedText);
                                replChatMemory.add("assistant", new AssistantMessage(cleanedText));
                                replChatMemory.add("user", new AssistantMessage(cleanedText));
                                printIt();
                                mess.completeSendResponse(rawChannel, cleanedText);
                            }
                            this.lastResults = lastResults;
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

    /*
     * Full-REPL
     */
    private CompletableFuture<Void> startREPL(Scanner scanner, String userInput) {
        System.out.println(Vyrtuous.BLURPLE + "Thinking..." + Vyrtuous.RESET);
        if (scanner == null) {
            CompletableFuture<Void> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IllegalArgumentException("Scanner cannot be null"));
            return failed;
        }
        if (userInput == null || userInput.isBlank()) {
            return CompletableFuture.completedFuture(null);
        }
        replChatMemory.clear("assistant");
        replChatMemory.clear("user");
        originalDirective = userInput;
        replChatMemory.add("assistant", new AssistantMessage(userInput));
        replChatMemory.add("user", new AssistantMessage(userInput));
        mess.completeSendResponse(rawChannel, "[User]: " + userInput);
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

    @PostConstruct
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
