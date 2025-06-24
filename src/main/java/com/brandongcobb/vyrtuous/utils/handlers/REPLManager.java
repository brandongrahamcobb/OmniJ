
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
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.logging.*;
import java.util.function.Function;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    
    private ObjectMapper mapper = new ObjectMapper();
    
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
                                String tokensCount = String.valueOf(llamaUtils.completeGetTokens().join());
                                contextManager.addEntry(new ContextEntry(ContextEntry.Type.TOKENS, tokensCount));
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

                            // Try to check if it's valid JSON
                            boolean isJson = false;
                            JsonNode rootNode;
                            try {
                                Pattern CODE_BLOCK_JSON_PATTERN =
                                    Pattern.compile("```(?:\\w+)?\\s*([\\[{].*?[\\]}])\\s*```", Pattern.DOTALL);
                                Matcher matcher = CODE_BLOCK_JSON_PATTERN.matcher(content);

                                if (matcher.find()) {
                                    String jsonContent = matcher.group(1).trim();
                                    MetadataKey<String> contentKey = new MetadataKey<>("response", Metadata.STRING);
                                    metadataContainer.put(contentKey, jsonContent);

                                    rootNode = mapper.readTree(jsonContent); // throws if invalid
                                    isJson = true;
                                }
                            } catch (JsonProcessingException e) {
                                // fall through to REPL below
                            }

                            // Not valid JSON: prompt user for new input
                            if (!isJson) {
                                contextManager.addEntry(new ContextEntry(ContextEntry.Type.AI_RESPONSE, content));
                                MetadataKey<String> contentKey = new MetadataKey<>("response", Metadata.STRING);
                                metadataContainer.put(contentKey, content);
                                contextManager.printNewEntries(true, true, true, true, true, true, true, true);

                                System.out.print("> ");
                                String newInput = scanner.nextLine();
                                contextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, newInput));

                            }
                            
                            // blocking
                            return metadataContainer;
                        }); // flatten nested CompletableFuture
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
        ObjectMapper mapper = new ObjectMapper();
        List<JsonNode> results = new ArrayList<>();
        String contentStr = new MetadataUtils(response).completeGetContent().join();

        try {
            JsonNode root = mapper.readTree(contentStr);
            if (root.isArray()) {
                for (JsonNode node : root) {
                    if (node.has("tool")) {
                        results.add(node);
                    }
                }
            } else if (root.has("tool")) {
                results.add(root);
            }
            for (JsonNode result : results) {
                try {
                    String toolName = result.get("tool").asText();
                    switch (toolName) {
                        case "patch" -> {
                            PatchInput patchInput = mapper.treeToValue(result.get("input"), PatchInput.class);
                            patchInput.setOriginalJson(result);
                            Patch patchTool = new Patch(contextManager);
                            PatchStatus status = patchTool.run(patchInput);
                            contextManager.addEntry(new ContextEntry(ContextEntry.Type.TOOL_OUTPUT, status.getMessage()));
                            System.out.println("debug");
                            contextManager.printNewEntries(true, true, true, true, true, true, true, true);
                        }
                        case "refresh_context" -> {
                            RefreshContextInput input = mapper.treeToValue(result.get("input"), RefreshContextInput.class);
                            RefreshContext tool = new RefreshContext(contextManager);
                            RefreshContextStatus status = tool.run(input);
                            contextManager.addEntry(new ContextEntry(ContextEntry.Type.TOOL_OUTPUT, status.getMessage()));
                            contextManager.printNewEntries(true, true, true, true, true, true, true, true);
                        }
                        default -> {
                            contextManager.addEntry(new ContextEntry(ContextEntry.Type.AI_RESPONSE, contentStr));
                            contextManager.printNewEntries(true, true, true, true, true, true, true, true);
                            System.out.print("> ");
                            String newInput = scanner.nextLine(); // blocking
                            
                            contextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, newInput));
                            return startREPL(scanner, newInput);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (JsonProcessingException e) {
        }
        
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> completePStep(Scanner scanner) {
        LOGGER.fine("Print-step");
        contextManager.printNewEntries(true, true, true, true, true, true, true, true);
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
