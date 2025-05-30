/*  REPLManager.java The purpose of this class is to loop through
 *  a cli sequence for shell commands.
 *
 *  Copyright (C) 2025  github.com/brandongrahamcobb
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  aInteger with this program.  If not, see <https://www.gnu.org/licenses/>.
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
    private final ContextManager contextManager = new ContextManager(3200);
    private final ExecutorService inputExecutor = Executors.newSingleThreadExecutor();
    private static final Logger LOGGER = Logger.getLogger(REPLManager.class.getName());
    private final long maxSessionDurationMillis;
    private String originalDirective;
    private final ExecutorService replExecutor = Executors.newFixedThreadPool(2);
    private final List<String> shellHistory = new ArrayList<>();

    public REPLManager(ApprovalMode mode, long maxSessionDurationMillis) {
        setApprovalMode(mode);
        this.maxSessionDurationMillis = maxSessionDurationMillis;
        LOGGER.setLevel(Level.FINE);
        LOGGER.fine("REPLManager initialized with mode " + mode + " and max duration " + maxSessionDurationMillis + "ms");
    }

    public REPLManager(ApprovalMode mode) {
        this(mode, 0L);
    }

    public void setApprovalMode(ApprovalMode mode) {
        LOGGER.fine("Approval mode set to: " + mode);
        this.approvalMode = mode;
    }

    private boolean isDangerousCommand(String command) {
        if (command == null) return false;
        List<String> dangerous = List.of("rm", "mv", "git", "patch", "shutdown", "reboot", "mvn compile");
        boolean isDangerous = dangerous.stream().anyMatch(command::contains);
        LOGGER.fine("Checked command for danger: '" + command + "' => " + isDangerous);
        return isDangerous;
    }

    private boolean requiresApproval(String command) {
        boolean result = switch (approvalMode) {
            case FULL_AUTO -> false;
            case EDIT_APPROVE_ALL -> true;
            case EDIT_APPROVE_DESTRUCTIVE -> isDangerousCommand(command);
        };
        LOGGER.fine("Approval required for command '" + command + "' => " + result);
        return result;
    }

    private CompletableFuture<String> completeCommandAndContinue(
        MetadataContainer response,
        AIManager aim,
        StringBuilder transcript,
        Scanner scanner,
        String modelSetting,
        long startTimeMillis
    ) {
        List<String> shellCommands = response.get(ResponseObject.LOCALSHELLTOOL_COMMANDS);
        if (shellCommands == null || shellCommands.isEmpty()) {
            return CompletableFuture.completedFuture("No shell commands to execute.");
        }
        ToolHandler toolHandler = new ToolHandler();
        return completeMultipleCommands(shellCommands, 0, toolHandler, response, aim, transcript, scanner, modelSetting, startTimeMillis);
    }
    
    
    private CompletableFuture<String> completeMultipleCommands(
            List<String> commands,
            int index,
            ToolHandler toolHandler,
            MetadataContainer response,
            AIManager aim,
            StringBuilder transcript,
            Scanner scanner,
            String modelSetting,
            long startTimeMillis
    ) {
        if (index >= commands.size()) {
            String updatedPrompt = contextManager.buildPromptContext();
            return aim.completeWebShellRequest(updatedPrompt, null, modelSetting, "response")
                    .thenCompose(nextResponse -> completeProcessREPLLoop(nextResponse, aim, transcript, scanner, modelSetting, startTimeMillis));
        }
        String shellCommand = commands.get(index);
        LOGGER.fine("Executing shell command: " + shellCommand);
        contextManager.addEntry(new ContextEntry(ContextEntry.Type.COMMAND, shellCommand));
        return toolHandler.completeShellCommand(response, shellCommand)
                .thenCompose(output -> {
                    transcript.append("> ").append(shellCommand).append("\n").append(output).append("\n");
                    System.out.println("> " + shellCommand);
                    contextManager.addEntry(new ContextEntry(ContextEntry.Type.SHELL_OUTPUT, output));
                    LOGGER.info("Shell command output: " + output);
                    return completeMultipleCommands(commands, index + 1, toolHandler, response, aim, transcript, scanner, modelSetting, startTimeMillis);
                });
    }
    
    private static String truncateOutput(String output, int maxChars, int maxLines) {
        if (output == null || output.isBlank()) return "";

        String[] lines = output.split("\n");
        StringBuilder builder = new StringBuilder();
        int count = 0;

        for (String line : lines) {
            if (builder.length() + line.length() + 1 > maxChars || count >= maxLines) break;
            builder.append(line).append("\n");
            count++;
        }

        if (count < lines.length) {
            builder.append("...\n⚠️ Output truncated (").append(lines.length - count).append(" lines omitted)");
        }

        return builder.toString().trim();
    }

    private String prepareShellOutputSummary(MetadataContainer response) {
        String stdout = truncateOutput(response.get(ToolHandler.SHELL_STDOUT), 4000, 40);
        String stderr = truncateOutput(response.get(ToolHandler.SHELL_STDERR), 2000, 20);
        int exitCode = response.get(ToolHandler.SHELL_EXIT_CODE);

        return """
            [Shell Execution Result]
            Exit Code: %d

            --- STDOUT ---
            %s

            --- STDERR ---
            %s
            """.formatted(exitCode, stdout, stderr);
    }
    
    private CompletableFuture<String> completeProcessREPLLoop(
        MetadataContainer response,
        AIManager aim,
        StringBuilder transcript,
        Scanner scanner,
        String modelSetting,
        long startTimeMillis
    ) {
        System.out.println("test");
        List<String> shellCommands = response.get(ResponseObject.LOCALSHELLTOOL_COMMANDS);
        LOGGER.fine("Shell commands received: " + shellCommands);;
        System.out.println("Shell commands received: " + shellCommands);
        ResponseUtils ru = new ResponseUtils(response);
        String summary = ru.completeGetLocalShellToolSummary().join();
        if (summary != null && !summary.isBlank()) {
            System.out.println("\n[Model Summary]: " + summary + "\n");
            String shellSummary = prepareShellOutputSummary(response);
            contextManager.addEntry(new ContextEntry(ContextEntry.Type.AI_RESPONSE, shellSummary));
            LOGGER.fine("Model summary: " + summary);
        }
        return ru.completeGetShellToolFinished().thenCompose(finished -> {
            if (Boolean.TRUE.equals(finished)) {
                LOGGER.fine("AI indicated task is finished.");
                System.out.println("✅ Task complete.");
                System.out.println("\nFinal Summary:\n" + transcript.toString());
                return CompletableFuture.completedFuture(transcript.toString());
            }
            if (shellCommands == null || shellCommands.isEmpty()) {
                
                LOGGER.warning("No shell commands received. Asking user for clarification.");
                String plainText = ru.completeGetResponseMap().join();
                System.out.println("[Model]: I need clarification before proceeding. " + plainText);
                System.out.print("> ");
                String userInput = scanner.nextLine();
                contextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, userInput));
                return completeREPLLoop(userInput, aim, transcript, scanner, modelSetting, startTimeMillis);
            }
            String element = shellCommands.get(0); // Assume only the first for now
            LOGGER.fine("Received shell command: " + element);
            String plainText = ru.completeGetOutput().join();
            if (element == null || element.isBlank() || element.startsWith("echo")) {
                LOGGER.warning("Shell command is blank or starts with echo. Asking for clarification.");
                System.out.println("[Model]: I need clarification before proceeding. " + plainText);
                System.out.print("> ");
                String userInput = scanner.nextLine();
                contextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, userInput));
                return completeREPLLoop(userInput, aim, transcript, scanner, modelSetting, startTimeMillis);
            }
            long tokens = contextManager.getContextTokenCount();
            System.out.println("Current context token count: " + tokens);
            if (requiresApproval(element)) {
                return completeRequestApproval(element, scanner).thenCompose(approved -> {
                    if (!approved) {
                        String rejectionMsg = "⛔ Command rejected by user.";
                        System.out.println(rejectionMsg);
                        transcript.append(rejectionMsg).append("\n");
                        LOGGER.warning("User rejected command: " + element);
                        return CompletableFuture.completedFuture(transcript.toString());
                    }
                    return completeCommandAndContinue(response, aim, transcript, scanner, modelSetting, startTimeMillis);
                });
            } else {
                return completeCommandAndContinue(response, aim, transcript, scanner, modelSetting, startTimeMillis);
            }
        });
    }

    private CompletableFuture<String> completeREPLLoop(
        String input,
        AIManager aim,
        StringBuilder transcript,
        Scanner scanner,
        String modelSetting,
        long startTimeMillis
    ) {
        LOGGER.fine("Running REPL loop with input: " + input);
        if (maxSessionDurationMillis > 0) {
            long elapsed = System.currentTimeMillis() - startTimeMillis;
            if (elapsed > maxSessionDurationMillis) {
                String timeoutMsg = "\n⏰ REPL session timed out after " + (maxSessionDurationMillis / 1000) + " seconds.\n";
                transcript.append(timeoutMsg);
                LOGGER.warning("REPL session timed out.");
                return CompletableFuture.completedFuture(transcript.toString());
            }
        }
        contextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, input));
        String prompt = contextManager.buildPromptContext();
        return aim.completeWebShellRequest(prompt, null, modelSetting, "response")
            .thenCompose(response -> completeProcessREPLLoop(response, aim, transcript, scanner, modelSetting, startTimeMillis));
    }
    
    private CompletableFuture<Boolean> completeRequestApproval(String command, Scanner scanner) {
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
        }, inputExecutor);
    }

    private CompletableFuture<String> completeREPLAsync(Scanner scanner, String initialMessage) {
        this.originalDirective = initialMessage;
        LOGGER.fine("Starting REPL session with: " + initialMessage);
        contextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, initialMessage));
        AIManager aim = new AIManager();
        StringBuilder transcript = new StringBuilder();
        String model = ModelRegistry.OPENROUTER_RESPONSE_MODEL.asString();
        return completeREPLLoop(initialMessage, aim, transcript, scanner, model, System.currentTimeMillis());
    }
    
    public void startResponseInputThread() {
        LOGGER.fine("Starting response input thread...");
        inputExecutor.submit(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                System.out.println("Response input thread started. Type your messages:");
                while (true) {
                    System.out.print("> ");
                    String input;
                    try {
                        input = scanner.nextLine();
                    } catch (NoSuchElementException e) {
                        LOGGER.warning("Input stream closed unexpectedly.");
                        System.out.println("Input stream closed.");
                        break;
                    }

                    if (input.equalsIgnoreCase(".exit") || input.equalsIgnoreCase(".quit")) {
                        LOGGER.fine("User requested REPL shutdown.");
                        System.out.println("Exiting response input thread.");
                        break;
                    }

                    LOGGER.fine("Received user input: " + input);
                    completeREPLAsync(scanner, input)
                        .thenAcceptAsync(response -> {
                            System.out.println("Bot: " + response);
                            LOGGER.fine("REPL output: " + response);
                        }, replExecutor);
                }
            } catch (IllegalStateException e) {
                LOGGER.severe("System.in is unavailable.");
                System.out.println("System.in is unavailable.");
            }
        });
    }
}

// theres an issue with multiple commands vs single command and the redundant call to AIM.
