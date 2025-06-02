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
import com.brandongcobb.vyrtuous.utils.inc.*;
import com.brandongcobb.metadata.*;

public class REPLManager {

    private ApprovalMode approvalMode = ApprovalMode.FULL_AUTO;
    // private final ContextManager contextManager = new ContextManager(3200); // ContextManager usage commented out
    private final ExecutorService inputExecutor = Executors.newSingleThreadExecutor();
    private static final Logger LOGGER = Logger.getLogger(REPLManager.class.getName());
    private String originalDirective; // Stores the initial user input
    private final ExecutorService replExecutor = Executors.newFixedThreadPool(2);
    private AIManager aim = new AIManager();
    private ToolHandler th = new ToolHandler();
    private MetadataContainer lastAIResponseContainer = null; // Stores the MetadataContainer of the last AI response
    private String lastAIResponseText = ""; // Stores the text content of the last AI response
    private String lastShellOutput = ""; // Stores the output of the last executed shell command

    /*
     * constructors
     */
    public REPLManager(ApprovalMode mode) {
        setApprovalMode(mode);
    }

    /*
     * basic helper methods
     */
    public void setApprovalMode(ApprovalMode mode) {
        this.approvalMode = mode;
    }

    /**
     * Checks if a given command is considered dangerous.
     * Dangerous commands are defined in a hardcoded list.
     * @param command The command string to check.
     * @return true if the command contains any dangerous keywords, false otherwise.
     */
    private boolean isDangerousCommand(String command) {
        if (command == null) return false;
        // List of commands considered dangerous
        List<String> dangerous = List.of("rm", "mv", "git", "patch", "shutdown", "reboot", "mvn compile");
        boolean isDangerous = dangerous.stream().anyMatch(command::contains);
        LOGGER.fine("Checked command for danger: '" + command + "' => " + isDangerous);
        return isDangerous;
    }

    /**
     * Determines if a command requires user approval based on the current approval mode.
     * @param command The command string to evaluate.
     * @return true if approval is required, false otherwise.
     */
    private boolean requiresApproval(String command) {
        boolean result = switch (approvalMode) {
            case FULL_AUTO -> false; // No approval needed in full auto mode
            case EDIT_APPROVE_ALL -> true; // All commands require approval
            case EDIT_APPROVE_DESTRUCTIVE -> isDangerousCommand(command); // Only dangerous commands require approval
        };
        return result;
    }

    /*
     * REPL : start
     */
    /**
     * Starts a new thread to continuously read user input from the console.
     * Each input triggers the REPL sequence.
     */
    public void startResponseInputThread() {
        inputExecutor.submit(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    System.out.print("> "); // Prompt for user input
                    String input = scanner.nextLine();
                    // Start the REPL process for the user input
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
        this.originalDirective = userInput; // Store the original user directive
        // contextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, userInput)); // Context entry removed
        // Start the 'R' (Read/Response) step, indicating it's the first run
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
            // For the first run, the prompt for the AI is the original user input.
            prompt = this.originalDirective;
        } else {
            // For subsequent runs (L-step), the prompt is the last AI response combined with the last shell output.
            prompt = lastAIResponseText + "\n" + lastShellOutput;
        }

        // Determine if it's the very first AI call or a follow-up
        if (firstRun || lastAIResponseContainer == null) {
            // Make the initial AI request
            return aim.completeRequest(prompt, null, model, "response", "openai", false, null)
                    .thenApply(response -> {
                        lastAIResponseContainer = response;
                        OpenAIUtils openaiUtils = new OpenAIUtils(response);
                        lastAIResponseText = openaiUtils.completeGetOutput().join(); // Store the AI's response text
                        return response;
                    });
        } else {
            // For follow-up AI requests, get the previous response ID and make a new request
            return new OpenAIUtils(lastAIResponseContainer).completeGetPreviousResponseId()
                    .thenCompose(previousResponseId -> {
                        System.out.println(previousResponseId); // Print the previous response ID for debugging/logging
                        return aim.completeRequest(prompt, previousResponseId, model, "response", "openai", false, null)
                                .thenApply(response -> {
                                    lastAIResponseContainer = response;
                                    OpenAIUtils openaiUtils = new OpenAIUtils(response);
                                    lastAIResponseText = openaiUtils.completeGetOutput().join(); // Store the AI's response text
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
        List<String> commands = response.get(th.LOCALSHELLTOOL_COMMANDS);
        boolean finished = response.getOrDefault(th.LOCALSHELLTOOL_FINISHED, false);

        if (commands == null || commands.isEmpty()) {
            return CompletableFuture.completedFuture("No shell commands to execute.");
        }

        // Recursively execute commands one by one
        return completeESubStep(commands, 0, response, scanner).thenCompose(transcript -> {
            if (finished) {
                System.out.println("✅ Task complete.");
                return CompletableFuture.completedFuture(transcript);
            }
            // If not finished, proceed to the 'L' (Loop) step
            return completeLStep(scanner);
        });
    }

    /**
     * A sub-step of the 'E' (Execute) phase, handling individual command execution and approval.
     * @param commands The list of commands to execute.
     * @param index The current index of the command to execute.
     * @param response The AI's MetadataContainer response.
     * @param scanner The scanner for user input.
     * @return A CompletableFuture that completes with the output of the executed commands.
     */
    private CompletableFuture<String> completeESubStep(
            List<String> commands,
            int index,
            MetadataContainer response,
            Scanner scanner
    ) {
        if (index >= commands.size()) {
            return CompletableFuture.completedFuture(""); // All commands executed
        }
        String command = commands.get(index);

        if (requiresApproval(command)) {
            // If approval is required, prompt the user
            return CompletableFuture.supplyAsync(() -> {
                LOGGER.fine("Requesting user approval for command: " + command);
                System.out.println("Approval required for command: " + command);
                System.out.print("Approve? (yes/no): ");
                while (true) {
                    String input = scanner.nextLine().trim().toLowerCase();
                    if (input.equals("yes") || input.equals("y")) return true;
                    if (input.equals("no") || input.equals("n")) return false;
                    System.out.print("Please type 'yes' or 'no': ");
                }
            }, inputExecutor).thenCompose(approved -> {
                if (!approved) {
                    System.out.println("⛔ Skipping command: " + command);
                    // If not approved, skip this command and proceed to the next
                    return completeESubStep(commands, index + 1, response, scanner);
                }
                // If approved, execute the command
                return completeESubSubStep(command, commands, index, response, scanner);
            });
        } else {
            // No approval needed, execute directly
            return completeESubSubStep(command, commands, index, response, scanner);
        }
    }

    /**
     * The final sub-step of the 'E' (Execute) phase, handling the actual shell command execution.
     * @param command The command to execute.
     * @param commands The full list of commands.
     * @param index The current index of the command.
     * @param response The AI's MetadataContainer response.
     * @param scanner The scanner for user input.
     * @return A CompletableFuture that completes with the output of the command.
     */
    private CompletableFuture<String> completeESubSubStep(
            String command,
            List<String> commands,
            int index,
            MetadataContainer response,
            Scanner scanner
    ) {
        // contextManager.addEntry(new ContextEntry(ContextEntry.Type.COMMAND, command)); // Context entry removed
        // Execute the shell command
        return th.completeShellCommand(response, command).thenCompose(output -> {
            System.out.println("> " + command + "\n" + output); // Print the command and its output
            // contextManager.addEntry(new ContextEntry(ContextEntry.Type.SHELL_OUTPUT, output)); // Context entry removed
            this.lastShellOutput = output; // Store the output of the current shell command
            // Proceed to the next command in the list
            return completeESubStep(commands, index + 1, response, scanner);
        });
    }

    /**
     * The 'P' (Print) step of the REPL.
     * It prints a summary from the model if available.
     * @param response The MetadataContainer from the AI's response.
     */
    private void completePStep(MetadataContainer response) {
        String summary = new OpenAIUtils(response).completeGetLocalShellToolSummary().join();
        if (summary != null && !summary.isBlank()) {
            System.out.println("\n[Model Summary]:\n" + summary + "\n");
        }
        // long tokens = contextManager.getContextTokenCount(); // Context token count removed
        // System.out.println("Current context token count: " + tokens); // Context token count print removed
    }

    /**
     * The 'L' (Loop) step of the REPL.
     * It cycles back to the 'R' step to get a new response from the AI,
     * effectively continuing the conversation or task.
     * @param scanner The scanner for user input.
     * @return A CompletableFuture that completes with the final output of the loop.
     */
    private CompletableFuture<String> completeLStep(Scanner scanner) {
        // Loop back to the 'R' step, indicating it's not the first run for this directive
        return completeRStep(scanner, false).thenCompose(response -> {
            completePStep(response); // Print any summary from the model
            return completeEStep(response, scanner); // Proceed to the 'E' step
        });
    }
}
