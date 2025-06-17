
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
        String prompt = firstRun
            ? originalDirective
            : contextManager.buildPromptContext();
        String model = System.getenv("CLI_MODEL");
        String provider = System.getenv("CLI_PROVIDER");
        String requestType = System.getenv("CLI_REQUEST_TYPE");// assuming this is already set elsewhere
        CompletableFuture<String> endpointFuture =
            aim.completeGetAIEndpoint(false, provider, "cli", requestType);
        CompletableFuture<String> instructionsFuture =
            aim.completeGetInstructions(false, provider, "cli");
        return endpointFuture
            .thenCombine(instructionsFuture, (endpoint, instructions) -> new AbstractMap.SimpleEntry<>(endpoint, instructions))
            .thenCompose(pair -> {
                String endpoint = pair.getKey();
                String instructions = pair.getValue();
                
                CompletableFuture<MetadataContainer> call;

                try {
                    if (firstRun) {
                        call = aim.completeRequest(instructions, prompt, null, model, requestType, endpoint, false, null, provider);
                    } else {
                        MetadataKey<String> previousResponseIdKey = new MetadataKey<>("id", Metadata.STRING);
                        String prevId = (String) lastAIResponseContainer.get(previousResponseIdKey);
                        call = aim.completeRequest(instructions, prompt, prevId, model, requestType, endpoint, Boolean.valueOf(System.getenv("CLI_STREAM")), null, provider);
                    }
                    return call.handle((resp, ex) -> {
                        if (ex != null) {
                            LOGGER.severe("completeRequest failed: " + ex.getMessage());
                            throw new CompletionException(ex);
                        }
                        if (resp == null) {
                            throw new CompletionException(new IllegalStateException("AI returned null"));
                        }
                        String content = null;
                        String previousResponseId = null;
                        ObjectMapper mapper = new ObjectMapper();
                        switch (provider) {
                            case "llama":
                                LlamaUtils llamaUtils = new LlamaUtils(resp);
                                content = llamaUtils.completeGetContent().join();
                                previousResponseId = llamaUtils.completeGetResponseId().join();
                                break;
                            case "openai":
                                OpenAIUtils openaiUtils = new OpenAIUtils(resp);
                                content = (String) openaiUtils.completeGetOutput().join();
                                previousResponseId = openaiUtils.completeGetResponseId().join();
                                break;
                            default:
                                return new MetadataContainer();
                        }
                        String jsonContent = ToolHandler.extractJsonContent(content);
                        LOGGER.fine("Extracted JSON: " + jsonContent);
                        jsonContent = ToolHandler.sanitizeJsonContent(jsonContent);
                        try {
                            JsonNode rootNode = mapper.readTree(jsonContent);
                            JsonNode actualObject;
                            if (rootNode.isObject()) {
                                actualObject = rootNode;
                            } else if (rootNode.isArray()) {
                                if (rootNode.size() == 0) {
                                    throw new RuntimeException("JSON array is empty. Cannot extract metadata.");
                                }
                                actualObject = rootNode.get(0);
                                if (!actualObject.isObject()) {
                                    throw new RuntimeException("First element of array is not a JSON object.");
                                }
                            } else {
                                throw new RuntimeException("Unexpected JSON structure: not object or array.");
                            }
                            Map<String, Object> resultMap = mapper.convertValue(actualObject, new TypeReference<>() {});
                            resultMap.put("id", previousResponseId);
                            String entityType = (String) resultMap.get("entityType");
                            if (entityType != null) {
                                if (entityType.startsWith("json_tool")) {
                                    lastAIResponseContainer = new ToolContainer(resultMap);
                                } else if (entityType.startsWith("json_chat")) {
                                    lastAIResponseContainer = new MarkdownContainer(resultMap);
                                }
                            }
                        } catch (JsonProcessingException jpe) {
                            jpe.printStackTrace();
                        }
                        return lastAIResponseContainer;
                    });
                } catch (Exception e) {
                    CompletableFuture<MetadataContainer> failed = new CompletableFuture<>();
                    failed.completeExceptionally(e);
                    return failed;
                }
            });
    }

    
    private CompletableFuture<Void> completeEStep(MetadataContainer response, Scanner scanner, boolean firstRun) {
        LOGGER.fine("Starting E-step");
        if (response instanceof ToolContainer tool) {
            ToolUtils tu = new ToolUtils(response);
            String lastAIResponseText = tu.completeGetText().join();
            contextManager.addEntry(new ContextEntry(ContextEntry.Type.AI_RESPONSE, lastAIResponseText));
            lastAIResponseText = null;
            List<List<String>> newCmds = (List<List<String>>) tool.getResponseMap().get(th.LOCALSHELLTOOL_COMMANDS_LIST);
            if (newCmds == null || newCmds.isEmpty()) {
                LOGGER.warning("No shell commands returned from tool");
                return CompletableFuture.completedFuture(null);
            }
            for (List<String> cmdParts : newCmds) {
                String flat = String.join(" ", cmdParts);
                contextManager.addEntry(new ContextEntry(ContextEntry.Type.COMMAND, flat));
            }
            Supplier<CompletableFuture<String>> runner = () -> th.executeCommands(newCmds);
            return runner.get().thenCompose(out -> {
                contextManager.addEntry(new ContextEntry(ContextEntry.Type.COMMAND_OUTPUT, out));
                return CompletableFuture.completedFuture(null);
            });
        } else if (response instanceof MarkdownContainer) {
            MarkdownUtils markdownUtils = new MarkdownUtils(response);
            boolean needsClarification = markdownUtils.completeGetClarification().join();
            boolean acceptingTokens = true; //markdownUtils.completeGetAcceptingTokens().join();
            String output = markdownUtils.completeGetText().join();
            contextManager.addEntry(new ContextEntry(ContextEntry.Type.AI_RESPONSE, output));
            output = null;
            if (needsClarification && acceptingTokens) {
                contextManager.printNewEntries(true, true, true, true, false, true, true);
                System.out.print("> ");
                String reply = scanner.nextLine();
                contextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, reply));
                reply = null;
            }
            boolean finished = markdownUtils.completeGetLocalShellFinished().join();
            if (finished) {
                String finalReason = markdownUtils.completeGetText().join();
                System.out.println(finalReason);
                System.out.println("✅ Task complete.");
                contextManager.clear();
                System.out.print("> ");
                String newInput = scanner.nextLine();
                return startREPL(scanner, newInput);
            }
            return CompletableFuture.completedFuture(null);
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
