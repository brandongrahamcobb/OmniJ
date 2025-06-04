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
// ... [HEADER REMAINS UNCHANGED]

package com.brandongcobb.vyrtuous.utils.handlers;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.logging.*;
import com.brandongcobb.metadata.*;
import com.brandongcobb.vyrtuous.Vyrtuous;
import com.brandongcobb.vyrtuous.utils.inc.*;

public class REPLManager {

    public static final String RESET = "\u001B[0m";
    public static final String BLUE = "\u001B[34m";
    public static final String BRIGHT_BLUE = "\u001B[94m";
    public static final String CYAN = "\u001B[36m";
    public static final String BRIGHT_CYAN = "\u001B[96m";
    public static final String NAVY = "\u001B[38;5;18m";
    public static final String SKY_BLUE = "\u001B[38;5;117m";
    public static final String DODGER_BLUE = "\u001B[38;5;33m";
    public static final String TEAL = "\u001B[38;5;30m";
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

    public REPLManager(ApprovalMode mode) {
        LOGGER.setLevel(Level.OFF);
        for (Handler handler : LOGGER.getParent().getHandlers()) {
            handler.setLevel(Level.OFF); // Ensure handlers output FINE logs
        }

        LOGGER.fine("Initializing REPLManager with approval mode: " + mode);
        setApprovalMode(mode);
    }

    public void setApprovalMode(ApprovalMode mode) {
        LOGGER.fine("Setting approval mode: " + mode);
        this.approvalMode = mode;
    }

    public CompletableFuture<Void> startREPL(Scanner scanner, String userInput) {
        return completeStartREPL(scanner, userInput)
            .thenCompose(response -> handleEStepResponse(response, scanner))
            .exceptionally(ex -> {
                LOGGER.severe("REPL failed: " + ex);
                return null;
            });
    }


    private CompletableFuture<String> completeStartREPL(Scanner scanner, String userInput) {
        LOGGER.fine("Starting REPL with input: " + userInput);
        this.originalDirective = userInput;
        contextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, userInput));
        return completeRStepWithTimeout(scanner, true).thenCompose(response -> completeEStep(response, scanner));
    }
    
    private CompletableFuture<MetadataContainer> completeRStepWithTimeout(Scanner scanner, boolean firstRun) {
        CompletableFuture<MetadataContainer> promise = new CompletableFuture<>();
        int maxRetries = 2;
        Runnable attempt = new Runnable() {
            int retries = 0;

            @Override
            public void run() {
                retries++;
                completeRStep(scanner, firstRun)
                    .orTimeout(60, TimeUnit.SECONDS)
                    .whenComplete((result, error) -> {
                        if (error != null) {
                            if (retries <= maxRetries) {
                                LOGGER.fine("R-step timeout or failure (attempt " + retries + "). Retrying...");
                                replExecutor.submit(this); // retry again
                            } else {
                                LOGGER.severe("R-step failed after " + retries + " attempts.");
                                promise.completeExceptionally(error);
                            }
                        } else {
                            LOGGER.fine("R-step completed successfully.");
                            promise.complete(result);
                        }
                    });
            }
        };

        replExecutor.submit(attempt); // start first attempt
        return promise;
    }


    private CompletableFuture<MetadataContainer> completeRStep(Scanner scanner, boolean firstRun) {
        LOGGER.fine("Starting R-step, firstRun=" + firstRun);
        String model = ModelRegistry.OPENAI_RESPONSE_MODEL.asString();
        String prompt = firstRun ? this.originalDirective : contextManager.buildPromptContext();
        LOGGER.fine("Prompt to AI: " + prompt);

        if (firstRun) {
            return aim.completeRequest(prompt, null, model, "response", "openai", false, null)
                .thenApply(response -> {
                    LOGGER.fine("Received AI response on first run");
                    lastAIResponseContainer = response;
                    ToolUtils toolUtils = new ToolUtils(response);
                    lastAIResponseText = toolUtils.completeGetCustomReasoning().join();
                    contextManager.addEntry(new ContextEntry(ContextEntry.Type.AI_RESPONSE, lastAIResponseText));
                    return response;
                });
        } else {
            return new ToolUtils(lastAIResponseContainer).completeGetResponseId()
                .thenCompose(previousResponseId -> {
                    LOGGER.fine("Continuing REPL with previous response ID: " + previousResponseId);
                    return aim.completeRequest(prompt, previousResponseId, model, "response", "openai", false, null)
                        .thenApply(response -> {
                            lastAIResponseContainer = response;
                            ToolUtils toolUtils = new ToolUtils(response);
                            lastAIResponseText = toolUtils.completeGetCustomReasoning().join();
                            contextManager.addEntry(new ContextEntry(ContextEntry.Type.AI_RESPONSE, lastAIResponseText));
                            return response;
                        });
                })
                .exceptionally(ex -> {
                    LOGGER.log(Level.SEVERE, "Failed in second REPL step", ex);
                    return null;
                });
        }
    }


    private CompletableFuture<Void> handleEStepResponse(Object responseOrInput, Scanner scanner) {
        if (responseOrInput instanceof MetadataContainer) {
            // continue processing normally with AI response
            return completeEStep((MetadataContainer) responseOrInput, scanner)
                .thenCompose(next -> handleEStepResponse(next, scanner));
        } else if (responseOrInput instanceof String) {
            // input from user is returned; wait for explicit next step
            String userInput = (String) responseOrInput;
            LOGGER.fine("Received user input during waiting: " + userInput);
            // now you can decide what to do — e.g. call completeRStepWithTimeout or completeStartREPL again
            return completeRStepWithTimeout(scanner, false).thenCompose(newResponse ->
                handleEStepResponse(newResponse, scanner)
            );
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }
    
    private CompletableFuture<String> completeEStep(MetadataContainer response, Scanner scanner) {
        LOGGER.fine("Starting E-step");
        ToolContainer toolContainer = (ToolContainer) response;
        ToolUtils toolOtherUtils = new ToolUtils(toolContainer);
        boolean needsClarification = toolOtherUtils.completeGetClarification().join();
        if (needsClarification) {
            LOGGER.fine("Model requested clarification");
            // Prompt user and return their answer as the next “step”
            System.out.println("🤔 I need more details" + toolContainer.getResponseMap().get("summary"));
            System.out.flush();
            System.out.print("> ");
            System.out.flush();
            String input = scanner.nextLine();
            // Tell handleEStepResponse “hey, here’s the user reply”
            return CompletableFuture.completedFuture(input);
        }

        // 2) Normal command-processing path follows
        boolean finished = toolContainer.getOrDefault(th.LOCALSHELLTOOL_FINISHED, false);
        @SuppressWarnings("unchecked")
        List<List<String>> allCommands =
            (List<List<String>>) toolContainer.getResponseMap()
                                              .get(th.LOCALSHELLTOOL_COMMANDS_LIST);

        LOGGER.fine("Command list size: "
                + (allCommands != null ? allCommands.size() : 0)
            + ", finished=" + finished);
        
        return completeESubStep(allCommands, 0, response, scanner).thenCompose(transcript -> {
            if (finished) {
                LOGGER.fine("REPL task marked complete");
                ToolUtils toolUtils = new ToolUtils(response);
                System.out.println(toolUtils.completeGetCustomReasoning().join());
                System.out.flush();
                System.out.println("✅ Task complete.");
                System.out.flush();
                contextManager.clear();

                System.out.print("> ");
                System.out.flush();
                String input = scanner.nextLine(); // ← synchronous
                this.originalDirective = input;

                return completeRStepWithTimeout(scanner, true)
                        .thenCompose(newResponse -> completeEStep(newResponse, scanner));
            }
            return completeLStep(scanner);
        });
    }


    private CompletableFuture<String> completeESubStep(
            List<List<String>> allCommands,
            int index,
            MetadataContainer response,
            Scanner scanner
    ) {
        if (index >= allCommands.size()) {
            LOGGER.fine("All commands have been processed.");
            return CompletableFuture.completedFuture("");
        }

        List<String> commandParts = allCommands.get(index);
        String commandStr = String.join(" ", commandParts);
        LOGGER.fine("Processing command: " + commandStr);

        if (Helpers.requiresApproval(commandStr, approvalMode)) {
            LOGGER.fine("Approval required for command: " + commandStr);
            System.out.println("Approval required for command: " + commandStr);
            System.out.flush();
            System.out.print("Approve? (yes/no): ");
            System.out.flush();

            boolean approved = false;
            while (true) {
                String input = scanner.nextLine().trim().toLowerCase();
                if (input.equals("yes") || input.equals("y")) {
                    approved = true;
                    break;
                }
                if (input.equals("no") || input.equals("n")) {
                    approved = false;
                    break;
                }
                System.out.print("Please type 'yes' or 'no': ");
                System.out.flush();
            }

            if (!approved) {
                LOGGER.fine("Command not approved by user: " + commandStr);
                System.out.println("⛔ Skipping command: " + commandStr);
                System.out.flush();
                return completeESubStep(allCommands, index + 1, response, scanner);
            }

            return completeESubSubStep(commandParts).thenCompose(output -> {
                System.out.println("> " + commandStr);
                System.out.flush();
                contextManager.addEntry(new ContextEntry(ContextEntry.Type.SHELL_OUTPUT, output));
                LOGGER.fine("Added shell output: “" + output + "”");
                int i = 0;
                for (ContextEntry e : contextManager.getEntries()) {
                    LOGGER.fine("  → entry #" + i++ + ": " + e.getType() + " | “" + CYAN + e.getContent() + RESET + "”");
                }
                lastShellOutput = output;
                return completeESubStep(allCommands, index + 1, response, scanner);
            });
        } else {
            return completeESubSubStep(commandParts).thenCompose(output -> {
                System.out.println("> " + commandStr);
                System.out.flush();
                contextManager.addEntry(new ContextEntry(ContextEntry.Type.SHELL_OUTPUT, output));
                LOGGER.fine("Added shell output: “" + output + "”");
                int i = 0;
                for (ContextEntry e : contextManager.getEntries()) {
                    LOGGER.fine("  → entry #" + i++ + ": " + e.getType() + " | “" + CYAN + e.getContent() + RESET + "”");
                }
                lastShellOutput = output;
                return completeESubStep(allCommands, index + 1, response, scanner);
            });
        }
    }

    private CompletableFuture<String> completeESubSubStep(List<String> commandParts) {
        final int maxRetries = 2;
        final long timeoutMillis = 60000;
        final String commandStr = String.join(" ", commandParts);

        Supplier<CompletableFuture<String>> tryCommand = () -> th.executeCommandsAsList(Collections.singletonList(commandParts));
        CompletableFuture<String> retryingCommand = new CompletableFuture<>();

        Runnable attempt = new Runnable() {
            int attempts = 0;

            @Override
            public void run() {
                attempts++;
                tryCommand.get().orTimeout(timeoutMillis, TimeUnit.MILLISECONDS)
                    .whenComplete((output, ex) -> {
                        if (ex != null) {
                            if (attempts <= maxRetries) {
                                LOGGER.fine("Retry " + attempts + " for command: " + commandStr);
                                replExecutor.submit(this);
                            } else {
                                LOGGER.severe("Command failed after retries: " + commandStr);
                                retryingCommand.complete("❌ Command failed after " + maxRetries + " retries: " + commandStr);
                            }
                        } else {
                            LOGGER.fine("Command succeeded: " + commandStr);
                            retryingCommand.complete(output);
                        }
                    });
            }
        };

        replExecutor.submit(attempt);
        return retryingCommand;
    }

    private CompletableFuture<Void> completePStep(MetadataContainer response, Scanner scanner) {
        LOGGER.fine("Starting P-step (Print)");
        contextManager.printEntries(true, true, true, true, true, true);
        System.out.flush();
        long tokens = contextManager.getContextTokenCount();
        System.out.println("Current token count: " + CYAN + tokens + RESET);
        return completeLStep(scanner).thenAccept(x -> {});
    }


    private CompletableFuture<String> completeLStep(Scanner scanner) {
        LOGGER.fine("Looping back to R-step");
        return completeRStepWithTimeout(scanner, false)
            .thenCompose(response -> completePStep(response, scanner)
            .thenCompose(ignored -> completeEStep(response, scanner)));
    }
    
    public void startResponseInputThread() {
        LOGGER.fine("Starting input thread...");
        inputExecutor.submit(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    System.out.print("> ");
                    String input = scanner.nextLine();
                    LOGGER.fine("User input received: " + input);
                    startREPL(scanner, input)
                        .exceptionally(ex -> {
                            LOGGER.severe("Error during REPL execution: " + ex);
                            return null;
                        })
                        .join(); // block here to ensure commands finish before next prompt
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Input thread crashed", e);
            }
        });
    }


}
