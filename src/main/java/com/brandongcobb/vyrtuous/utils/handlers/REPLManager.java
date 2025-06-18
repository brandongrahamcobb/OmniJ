
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
import com.brandongcobb.vyrtuous.enums.*;
import com.brandongcobb.vyrtuous.objects.*;
import com.brandongcobb.vyrtuous.utils.inc.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.logging.*;

public class REPLManager {

    private AIManager aim = new AIManager();
    private ApprovalMode approvalMode;
    private final ContextManager contextManager = new ContextManager(3200);
    private final ExecutorService inputExecutor = Executors.newSingleThreadExecutor();
    private MetadataContainer lastAIResponseContainer = null;
    private static final Logger LOGGER = Logger.getLogger(Vyrtuous.class.getName());
    private String originalDirective;
    private final ExecutorService replExecutor = Executors.newFixedThreadPool(2);
    private ToolHandler th = new ToolHandler();
    
    public void setApprovalMode(ApprovalMode mode) {
        LOGGER.fine("Setting approval mode: " + mode);
        this.approvalMode = mode;
    }

    public REPLManager(ApprovalMode mode) {
        LOGGER.setLevel(Level.FINE);
        for (Handler h : LOGGER.getParent().getHandlers()) {
            h.setLevel(Level.FINE);
        }
        this.approvalMode = mode;
    }

    public CompletableFuture<Void> startREPL(Scanner scanner, String userInput) {
        if (scanner == null) {
            CompletableFuture<Void> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IllegalArgumentException("Scanner cannot be null"));
            return failed;
        }
        if (userInput == null || userInput.isBlank()) {
            return CompletableFuture.completedFuture(null);
        }
        contextManager.clear();
        originalDirective = userInput;
        contextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, userInput));
        userInput = null;
        return completeRStep(scanner, true)
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
        CompletableFuture<MetadataContainer> promise = new CompletableFuture<>();
        final int maxRetries = 2;
        Runnable attempt = new Runnable() {
            int retries = 0;
            @Override
            public void run() {
                retries++;
                completeRStep(scanner, firstRun)
                    .orTimeout(60, TimeUnit.SECONDS)
                    .whenComplete((resp, err) -> {
                        if (err != null) {
                            if (retries <= maxRetries) {
                                LOGGER.fine("R-step attempt " + retries + " failed, retrying...");
                                replExecutor.submit(this);
                            } else {
                                LOGGER.severe("R-step failed after retries");
                                promise.completeExceptionally(err);
                            }
                        } else if (resp == null) {
                            if (retries <= maxRetries) {
                                LOGGER.warning("R-step returned null, retrying...");
                                replExecutor.submit(this);
                            } else {
                                promise.completeExceptionally(new IllegalStateException("R-step null response"));
                            }
                        } else {
                            promise.complete(resp);
                        }
                    });
            }
        };
        replExecutor.submit(attempt);
        return promise;
    }

    private CompletableFuture<MetadataContainer> completeRStep(Scanner scanner, boolean firstRun) {
        LOGGER.fine("Starting R-step, firstRun=" + firstRun);
        String prompt = firstRun ? originalDirective : contextManager.buildPromptContext();
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
                        return contentFuture.thenCombine(responseIdFuture, (content, responseId) -> {
                            ObjectMapper mapper = new ObjectMapper();
                            try {
                                String jsonContent = ToolHandler.extractJsonContent(content);
                                LOGGER.fine("Extracted JSON: " + jsonContent);
                                jsonContent = ToolHandler.sanitizeJsonContent(jsonContent);
                                JsonNode rootNode = mapper.readTree(jsonContent);
                                JsonNode actualObject = rootNode.isObject()
                                    ? rootNode
                                    : (rootNode.isArray() && rootNode.size() > 0 && rootNode.get(0).isObject())
                                        ? rootNode.get(0)
                                        : null;
                                if (actualObject == null) {
                                    throw new RuntimeException("Unexpected JSON structure");
                                }
                                Map<String, Object> resultMap = mapper.convertValue(actualObject, new TypeReference<>() {});
                                resultMap.put("id", responseId);
                                String entityType = (String) resultMap.get("entityType");
                                if (entityType != null) {
                                    if (entityType.startsWith("json_tool")) {
                                        lastAIResponseContainer = new ToolContainer(resultMap);
                                    } else if (entityType.startsWith("json_chat")) {
                                        lastAIResponseContainer = new MarkdownContainer(resultMap);
                                    }
                                }
                            } catch (Exception e) {
                                LOGGER.severe("JSON parse error: " + e.getMessage());
                                e.printStackTrace();
                            }
                            return lastAIResponseContainer;
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
        if (response instanceof ToolContainer tool) {
            ToolUtils tu = new ToolUtils(response);
            return tu.completeGetText().thenCompose(lastAIResponseText -> {
                contextManager.addEntry(new ContextEntry(ContextEntry.Type.AI_RESPONSE, lastAIResponseText));
                List<List<String>> newCmds = (List<List<String>>) tool.getResponseMap().get(th.LOCALSHELLTOOL_COMMANDS_LIST);
                //String base64 = tu.completeGetStdinBase64().join();
                if (newCmds == null || newCmds.isEmpty()) {
                    LOGGER.warning("No shell commands returned from tool");
                    return CompletableFuture.completedFuture(null);
                }
                for (List<String> cmdParts : newCmds) {
                    String flat = String.join(" ", cmdParts);
                    contextManager.addEntry(new ContextEntry(ContextEntry.Type.COMMAND, flat));
                }
                
                return th.executeCommandsAsList(newCmds).thenAccept(out -> {
//                try {
//                    return th.executeBase64Commands(base64).thenAccept(out -> {
                        contextManager.addEntry(new ContextEntry(ContextEntry.Type.COMMAND_OUTPUT, out));
                    });
//                    });
//                } catch (Exception e){
//                    e.printStackTrace();
//                    return CompletableFuture.failedFuture(e);
//                }
            }).exceptionally(ex -> {
                ex.printStackTrace();
                // You can handle error logging here, then return null to complete normally
                return null;
            });
        } else if (response instanceof MarkdownContainer markdown) {
            MarkdownUtils markdownUtils = new MarkdownUtils(markdown);
            CompletableFuture<Boolean> clarificationFuture = markdownUtils.completeGetClarification();
            CompletableFuture<Boolean> acceptingTokensFuture = markdownUtils.completeGetAcceptingTokens();
            CompletableFuture<String> textFuture = markdownUtils.completeGetText();
            return clarificationFuture
                .thenCombine(acceptingTokensFuture, AbstractMap.SimpleEntry::new)
                .thenCombine(textFuture, (pair, output) -> {
                    contextManager.addEntry(new ContextEntry(ContextEntry.Type.AI_RESPONSE, output));
                    return new Object[] { pair.getKey(), pair.getValue(), output };
                })
                .thenCompose(data -> {
                    boolean needsClarification = (boolean) data[0];
                    boolean acceptingTokens = (boolean) data[1];
                    if (needsClarification && acceptingTokens) {
                        contextManager.printNewEntries(true, true, true, true, false, true, true);
                        System.out.print("> ");
                        String reply = scanner.nextLine();  // still sync, unless replaced with async input
                        contextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, reply));
                    }
                    return markdownUtils.completeGetLocalShellFinished().thenCompose(finished -> {
                        if (finished) {
                            return markdownUtils.completeGetText().thenCompose(finalReason -> {
                                System.out.println(finalReason);
                                System.out.println("✅ Task complete.");
                                contextManager.clear();
                                System.out.print("> ");
                                String newInput = scanner.nextLine();  // sync input
                                return startREPL(scanner, newInput);
                            });
                        } else {
                            return CompletableFuture.completedFuture(null);
                        }
                    });
                });
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    private CompletableFuture<Void> completePStep(Scanner scanner) {
        LOGGER.fine("Print-step");
        contextManager.addEntry(new ContextEntry(ContextEntry.Type.TOKENS, String.valueOf(contextManager.getContextTokenCount())));
        contextManager.printNewEntries(true, false, true, true, false, true, true);
        return CompletableFuture.completedFuture(null); // <-- NO looping here!
    }

    private CompletableFuture<Void> completeLStep(Scanner scanner) {
        LOGGER.fine("Loop to R-step");
        return completeRStep(scanner, false)
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
