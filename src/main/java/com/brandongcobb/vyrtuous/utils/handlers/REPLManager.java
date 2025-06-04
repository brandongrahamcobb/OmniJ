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

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.logging.*;
import com.brandongcobb.metadata.*;
import com.brandongcobb.vyrtuous.Vyrtuous;
import com.brandongcobb.vyrtuous.utils.inc.*;

public class REPLManager {

    /*
     *  Runtime variables
     */
    private final ContextManager contextManager = new ContextManager(3200);
    private final ExecutorService inputExecutor = Executors.newSingleThreadExecutor();
    private static final Logger LOGGER = Logger.getLogger(Vyrtuous.class.getName());
    private String originalDirective;
    private final ExecutorService replExecutor = Executors.newFixedThreadPool(2);
    private AIManager aim = new AIManager();
    private ToolHandler th = new ToolHandler();
    private MetadataContainer lastAIResponseContainer = null;
    private String lastAIResponseText = "";
    private String lastShellOutput = "";
    private ApprovalMode approvalMode;

    /*
     *  Constructors
     */
    public REPLManager(ApprovalMode mode) {
        setApprovalMode(mode);
    }

    /*
     *  Helpers
     */
    public void setApprovalMode(ApprovalMode mode) {
        this.approvalMode = mode;
    }

    /**
     * Starts a new thread to continuously read user input from the console.
     * Each input triggers the REPL sequence.
     */
    public void startResponseInputThread() {
        inputExecutor.submit(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    System.out.print("> ");
                    String input = scanner.nextLine();
                    completeStartREPL(scanner, input).thenAcceptAsync(System.out::println, replExecutor);
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Input thread crashed", e);
            }
        });
    }

    /**
     * Initiates the REPL (Read-Execute-Print-Loop) sequence for a new user directive.
     * This is the 'start' step.
     * @param scanner The scanner for user input.
     * @param userInput The initial directive from the user.
     * @return A CompletableFuture that completes with the final output of the REPL cycle.
     */
    private CompletableFuture<String> completeStartREPL(Scanner scanner, String userInput) {
        this.originalDirective = userInput;
        contextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, userInput));
        return completeRStep(scanner, true).thenCompose(response -> completeEStep(response, scanner));
    }

    /**
     * The 'R' (Read/Response) step of the REPL.
     * It sends a prompt to the AI and processes its response.
     * @param scanner The scanner for user input (used for subsequent steps).
     * @param firstRun A boolean indicating if this is the initial AI request for a new user directive.
     * @return A CompletableFuture that completes with the MetadataContainer from the AI's response.
     */
    private CompletableFuture<MetadataContainer> completeRStep(Scanner scanner, boolean firstRun) {
        String model = ModelRegistry.OPENAI_RESPONSE_MODEL.asString();
        String prompt;
        if (firstRun) {
            prompt = this.originalDirective;
        } else {
            prompt = contextManager.buildPromptContext();
        }
        if (firstRun) {
            return aim.completeRequest(prompt, null, model, "response", "openai", false, null)
                    .thenApply(response -> {
                        lastAIResponseContainer = response;
                        OpenAIUtils openaiUtils = new OpenAIUtils(response);
                        lastAIResponseText = openaiUtils.completeGetOutput().join();
                        contextManager.addEntry(new ContextEntry(ContextEntry.Type.AI_RESPONSE, lastAIResponseText));
                        return response;
                    });
        } else {
            return new OpenAIUtils(lastAIResponseContainer).completeGetResponseId()
                    .thenCompose(previousResponseId -> {
                        System.out.println(previousResponseId);
                        return aim.completeRequest(prompt, previousResponseId, model, "response", "openai", false, null)
                                .thenApply(response -> {
                                    lastAIResponseContainer = response;
                                    OpenAIUtils openaiUtils = new OpenAIUtils(response);
                                    lastAIResponseText = openaiUtils.completeGetOutput().join();
                                    contextManager.addEntry(new ContextEntry(ContextEntry.Type.AI_RESPONSE, lastAIResponseText));
                                    System.out.println(lastAIResponseText);
                                    return response;
                                });
                    });
        }
    }

    /**
     * The 'E' (Execute) step of the REPL.
     * It processes the commands provided by the AI's response.
     * @param response The MetadataContainer containing the AI's response, including commands.
     * @param scanner The scanner for user input.
     * @return A CompletableFuture that completes with the transcript of executed commands.
     */
    private CompletableFuture<String> completeEStep(MetadataContainer response, Scanner scanner) {
        List<List<String>> allCommands = null;
        if (response instanceof OpenAIContainer) {
            OpenAIContainer openaiContainer = (OpenAIContainer) response;
            allCommands = (List<List<String>>) openaiContainer.getResponseMap().get(th.LOCALSHELLTOOL_COMMANDS_LIST);
        }
        boolean finished = response.getOrDefault(th.LOCALSHELLTOOL_FINISHED, false);
        if ((allCommands == null || allCommands.isEmpty()) && !finished) {
            System.out.println("💡 Model is awaiting your input or guidance.");
            System.out.println(lastAIResponseText);
            return CompletableFuture.supplyAsync(() -> {
                System.out.print("↪ Your input: ");
                return scanner.nextLine();
            }, inputExecutor).thenCompose(input -> {
                this.originalDirective = input;
                return completeRStep(scanner, true)
                        .thenCompose(newResponse -> completeEStep(newResponse, scanner));
            });
        }
        return completeESubStep(allCommands, 0, response, scanner).thenCompose(transcript -> {
            if (finished) {
                System.out.println("✅ Task complete.");
                System.out.println(lastAIResponseText);
                return CompletableFuture.supplyAsync(() -> {
                    System.out.print("↪ Your input: ");
                    return scanner.nextLine();
                }, inputExecutor).thenCompose(input -> {
                    this.originalDirective = input;
                    return completeRStep(scanner, true)
                            .thenCompose(newResponse -> completeEStep(newResponse, scanner));
                });
            }
            return completeLStep(scanner);
        });
    }

    /**
     * A sub-step of the 'E' (Execute) phase, handling individual command execution and approval.
     * @param allCommands The list of commands to execute.
     * @param index The current index of the command to execute.
     * @param response The AI's MetadataContainer response.
     * @param scanner The scanner for user input.
     * @return A CompletableFuture that completes with the output of the executed commands.
     */
    private CompletableFuture<String> completeESubStep(
            List<List<String>> allCommands,
            int index,
            MetadataContainer response,
            Scanner scanner
    ) {
        if (index >= allCommands.size()) {
            return CompletableFuture.completedFuture(""); // All commands done
        }
        List<String> commandParts = allCommands.get(index);
        String commandStr = String.join(" ", commandParts);
        if (Helpers.requiresApproval(commandStr, approvalMode)) {
            return CompletableFuture.supplyAsync(() -> {
                LOGGER.fine("Requesting user approval for command: " + commandStr);
                System.out.println("Approval required for command: " + commandStr);
                System.out.print("Approve? (yes/no): ");
                while (true) {
                    String input = scanner.nextLine().trim().toLowerCase();
                    if (input.equals("yes") || input.equals("y")) return true;
                    if (input.equals("no") || input.equals("n")) return false;
                    System.out.print("Please type 'yes' or 'no': ");
                }
            }, inputExecutor).thenCompose(approved -> {
                if (!approved) {
                    System.out.println("⛔ Skipping command: " + commandStr);
                    return completeESubStep(allCommands, index + 1, response, scanner);
                }
                return completeESubSubStep(commandParts).thenCompose(output -> {
                    System.out.println("> " + commandStr + "\n" + output);
                    contextManager.addEntry(new ContextEntry(ContextEntry.Type.SHELL_OUTPUT, output));
                    lastShellOutput = output;
                    return completeESubStep(allCommands, index + 1, response, scanner);
                });
            });
        } else {
            return completeESubSubStep(commandParts).thenCompose(output -> {
                System.out.println("> " + commandStr + "\n" + output);
                contextManager.addEntry(new ContextEntry(ContextEntry.Type.SHELL_OUTPUT, output));
                lastShellOutput = output;
                return completeESubStep(allCommands, index + 1, response, scanner);
            });
        }
    }

    private CompletableFuture<String> completeESubSubStep(List<String> commandParts) {
        return th.executeCommandsAsList(Collections.singletonList(commandParts));
    }
    
    /**
     * The 'P' (Print) step of the REPL.
     * It prints a summary from the model if available.
     * @param response The MetadataContainer from the AI's response.
     */
    private void completePStep(MetadataContainer response, Scanner scanner) {
        String summary = new OpenAIUtils(response).completeGetLocalShellToolSummary().join();
        if (summary != null && !summary.isBlank()) {
            System.out.println("\n[Model Summary]:\n" + summary + "\n");
        }
        long tokens = contextManager.getContextTokenCount();
        System.out.println("Current context token count: " + tokens);
        completeLStep(scanner);
    }

    /**
     * The 'L' (Loop) step of the REPL.
     * It cycles back to the 'R' step to get a new response from the AI,
     * effectively continuing the conversation or task.
     * @param scanner The scanner for user input.
     * @return A CompletableFuture that completes with the final output of the loop.
     */
    private CompletableFuture<String> completeLStep(Scanner scanner) {
        return completeRStep(scanner, false).thenCompose(response -> {
            completePStep(response, scanner);
            return completeEStep(response, scanner);
        });
    }
}
