
/* REPLManager.java The purpose of this class is to loop through
 * a cli sequence for shell commands.
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
import com.brandongcobb.vyrtuous.utils.inc.*;
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
    private String lastAIResponseText = "";
    private String lastShellOutput = "";
    private static final Logger LOGGER = Logger.getLogger(Vyrtuous.class.getName());
    private String originalDirective;
    private final ExecutorService replExecutor = Executors.newFixedThreadPool(2);
    private ToolHandler th = new ToolHandler();
    private final List<List<String>> pendingShellCommands = new ArrayList<>();
    private final String responseSource = System.getenv("REPL_RESPONSE_SOURCE");
    private final Set<String> seenCommandStrings = new HashSet<>();
    private String lastCommandOutput = "";
    private boolean acceptingTokens = true;
    private boolean needsClarification = false;
    
    private List<List<String>> oldCommands;
    
    
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
        pendingShellCommands.clear();
        seenCommandStrings.clear();

        contextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, userInput));
        originalDirective = userInput;

        return completeRStep(scanner, true)
            .thenCompose(resp ->
                completeEStep(resp, scanner, true)
                    .thenCompose(done -> {
                        boolean finished = resp.getOrDefault(th.LOCALSHELLTOOL_FINISHED, false);
                        return finished ? CompletableFuture.completedFuture(null)
                                        : completePStep(scanner)
                                            .thenCompose(ignored -> completeLStep(scanner));
                    })
            )
            .exceptionally(ex -> {
                LOGGER.log(Level.SEVERE, "REPL failed: ", ex);
                System.err.println("An error occurred. Please try again.");
                return null;
            });
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
        CompletableFuture<String> endpointFuture;
        String model;
        try {
            if ("llama".equals(responseSource)) {
                model = ModelRegistry.LLAMA_MODEL.asString();
                endpointFuture = aim.getAIEndpointWithState(false, responseSource, "cli", "completions");
            } else if ("openai".equals(responseSource)) {
                model = ModelRegistry.OPENAI_RESPONSE_MODEL.asString();
                endpointFuture = aim.getAIEndpointWithState(false, responseSource, "cli", "responses");
            } else {
                CompletableFuture<MetadataContainer> failed = new CompletableFuture<>();
                failed.completeExceptionally(new IllegalStateException("Unknown model for response source of type: " + responseSource));
                return failed;
            }
        } catch (Exception e) {
            CompletableFuture<MetadataContainer> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
        return endpointFuture.thenCompose(endpoint -> {
            CompletableFuture<MetadataContainer> call;
            try {
                if (firstRun) {
                    call = aim.completeRequest(prompt, null, model, endpoint, false, null);
                } else {
                    MetadataKey<String> previousResponseIdKey = new MetadataKey<>("id", Metadata.STRING);
                    String prevId = (String) lastAIResponseContainer.get(previousResponseIdKey); // may throw
                    call = aim.completeRequest(prompt, prevId, model, endpoint, false, null);
                }
            } catch (Exception e) {
                CompletableFuture<MetadataContainer> failed = new CompletableFuture<>();
                failed.completeExceptionally(e);
                return failed;
            }
            return call.handle((resp, ex) -> {
                if (ex != null) {
                    LOGGER.severe("completeRequest failed: " + ex.getMessage());
                    throw new CompletionException(ex);
                }
                if (resp == null) {
                    throw new CompletionException(new IllegalStateException("AI returned null"));
                }
                lastAIResponseContainer = resp;
                return resp;
            });
        });

    }
    
    private CompletableFuture<Void> completeEStep(MetadataContainer response, Scanner scanner, boolean firstRun) {
        LOGGER.fine("Starting E-step");
        if (response instanceof ToolContainer) {
            ToolUtils tu = new ToolUtils(response);
            lastAIResponseText = tu.completeGetText().join();
            contextManager.addEntry(new ContextEntry(ContextEntry.Type.AI_RESPONSE, lastAIResponseText));
            List<List<String>> newCmds = (List<List<String>>) ((ToolContainer) response).getResponseMap()
            .get(th.LOCALSHELLTOOL_COMMANDS_LIST);
            if (newCmds != null) {
                for (List<String> parts : newCmds) {
                    String cmd = String.join(" ", parts);
                    if (seenCommandStrings.add(cmd)) {
                        contextManager.addEntry(new ContextEntry(ContextEntry.Type.COMMAND, cmd));
                        pendingShellCommands.add(parts);
                    }
                }
            }
            boolean finished = response.getOrDefault(th.LOCALSHELLTOOL_FINISHED, false);
            return completeESubStep(scanner, firstRun).thenCompose(done -> {
                if (finished) {
                    String finalReason = tu.completeGetText().join();
                    System.out.println(finalReason);
                    System.out.println("✅ Task complete.");
                    pendingShellCommands.clear();
                    seenCommandStrings.clear();
                    contextManager.clear();
                    System.out.print("> ");
                    String newInput = scanner.nextLine();
                    return startREPL(scanner, newInput);
                } else {
                    return completeLStep(scanner);
                }
            });
        } else if (response instanceof MarkdownContainer) {
            MarkdownUtils markdownUtils = new MarkdownUtils(response);
            needsClarification = markdownUtils.completeGetClarification().join();
            acceptingTokens = markdownUtils.completeGetAcceptingTokens().join();
            String output = markdownUtils.completeGetText().join();

            if (needsClarification) {
                System.out.println("🤔 I need more details: " + output);
                System.out.print("> ");
                String reply = scanner.nextLine();
                contextManager.addEntry(new ContextEntry(ContextEntry.Type.SYSTEM_NOTE, output));
                contextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, reply));
                pendingShellCommands.clear();
                seenCommandStrings.clear();
                return completeLStep(scanner); // loop back
            }
            
            contextManager.addEntry(new ContextEntry(ContextEntry.Type.AI_RESPONSE, output));
            if (acceptingTokens) {
                // Accept the previous command's output and continue
                if (lastCommandOutput != null && !lastCommandOutput.isBlank()) {
                    contextManager.addEntry(new ContextEntry(ContextEntry.Type.COMMAND_OUTPUT, lastCommandOutput));
                } else {
                    LOGGER.warning("⚠️ acceptingTokens was true, but lastCommandOutput is null or blank.");
                }
                return completeESubStep(scanner, firstRun);
            }
        } else {
            return null;
        }
        return null;
    }
    
    private CompletableFuture<Void> completeESubStep(Scanner scanner, boolean firstRun) {
        if (pendingShellCommands.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        List<String> parts = pendingShellCommands.remove(0);
        String cmd = String.join(" ", parts);
        if (Helpers.requiresApproval(cmd, approvalMode)) {
            System.out.println("Approve command? " + cmd + " (yes/no)");
            System.out.print("> ");
            boolean ok = scanner.nextLine().trim().equalsIgnoreCase("yes");
            if (!ok) {
                System.out.println("⛔ Skipped: " + cmd);
                return completeESubStep(scanner, firstRun);
            }
        }
        return completeESubSubStep(Collections.singletonList(parts), firstRun).thenCompose(ignored -> {
            return completePStep(scanner).thenCompose(ignoredTwo -> {
                return completeLStep(scanner);
            });
        });
    }

    private CompletableFuture<Void> completeESubSubStep(List<List<String>> commands, boolean firstRun) {
        oldCommands = commands;
        Supplier<CompletableFuture<String>> runner = () -> th.executeCommandsAsList(commands);
        return runner.get().thenCompose(out -> {
            lastCommandOutput = out;
            if (acceptingTokens) {
                contextManager.addEntry(new ContextEntry(ContextEntry.Type.COMMAND_OUTPUT, out));
                return CompletableFuture.completedFuture(null);
            } else {
                long tokenCount = contextManager.getTokenCount(out);
                StringBuilder response = new StringBuilder();
                response.append("⚠️ The console output token count is: ").append(tokenCount).append("\n");
                response.append("📋 Do you want to accept the console output?\n");
                contextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, response.toString()));
                return CompletableFuture.completedFuture(null);
            }
        });
    }


    private CompletableFuture<Void> completePStep(Scanner scanner) {
        LOGGER.fine("Print-step");
        contextManager.addEntry(new ContextEntry(ContextEntry.Type.TOKENS, String.valueOf(contextManager.getContextTokenCount())));
        contextManager.printNewEntries(true, true, true, true, true, true, true);
        return CompletableFuture.completedFuture(null); // <-- NO looping here!
    }


    private CompletableFuture<Void> completeLStep(Scanner scanner) {
        LOGGER.fine("Loop to R-step");
        return completeRStep(scanner, false)
            .thenCompose(resp -> completeEStep(resp, scanner, false));
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
