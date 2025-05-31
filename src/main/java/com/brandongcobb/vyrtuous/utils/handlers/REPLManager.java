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
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.logging.*;

import com.brandongcobb.vyrtuous.utils.inc.*;
import com.brandongcobb.metadata.*;

public class REPLManager {

    private ApprovalMode approvalMode = ApprovalMode.FULL_AUTO;
    private final ContextManager contextManager = new ContextManager(3200);
    private final ExecutorService inputExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService replExecutor = Executors.newFixedThreadPool(2);
    private static final Logger LOGGER = Logger.getLogger(REPLManager.class.getName());
    private final long maxSessionDurationMillis;
    private final ToolHandler toolHandler = new ToolHandler();
    private final List<String> shellHistory = new ArrayList<>();
    private String originalDirective;

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
        return dangerous.stream().anyMatch(command::contains);
    }

    private boolean requiresApproval(String command) {
        return switch (approvalMode) {
            case FULL_AUTO -> false;
            case EDIT_APPROVE_ALL -> true;
            case EDIT_APPROVE_DESTRUCTIVE -> isDangerousCommand(command);
        };
    }

    private CompletableFuture<String> completeProcessREPLLoop(
        MetadataContainer response,
        AIManager aim,
        StringBuilder transcript,
        Scanner scanner,
        String modelSetting,
        long startTimeMillis
    ) {
        List<String> shellCommands = response.get(ResponseObject.LOCALSHELLTOOL_COMMANDS);
        ResponseUtils ru = new ResponseUtils(response);
        String summary = ru.completeGetLocalShellToolSummary().join();
        if (summary != null && !summary.isBlank()) {
            System.out.println("\n[Model Summary]: " + summary + "\n");
        }

        if (Boolean.TRUE.equals(ru.completeGetShellToolFinished().join())) {
            System.out.println("✅ Task complete.");
            System.out.println("\nFinal Summary:\n" + transcript);
            return CompletableFuture.completedFuture(transcript.toString());
        }

        if (shellCommands == null || shellCommands.isEmpty()) {
            System.out.println("[Model]: I need clarification before proceeding. " + ru.completeGetResponseMap().join());
            System.out.print("> ");
            String userInput = scanner.nextLine();
            contextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, userInput));
            return completeREPLLoop(userInput, aim, transcript, scanner, modelSetting, startTimeMillis);
        }

        if (shellCommands.size() == 1 && shellCommands.get(0).contains("&&")) {
            List<String> cleaned = Arrays.stream(shellCommands.get(0).split("&&"))
                .map(String::trim).filter(cmd -> !cmd.isBlank()).toList();
            response.put(ResponseObject.LOCALSHELLTOOL_COMMANDS, cleaned);
            shellCommands = cleaned;
        }

        int index = response.getOrDefault(ResponseObject.LOCALSHELLTOOL_COMMAND_INDEX, 0);
        if (index < shellCommands.size()) {
            String command = shellCommands.get(index);
            if (command == null || command.isBlank() || command.startsWith("echo")) {
                System.out.println("[Model]: I need clarification before proceeding. " + ru.completeGetOutput().join());
                System.out.print("> ");
                String userInput = scanner.nextLine();
                contextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, userInput));
                return completeREPLLoop(userInput, aim, transcript, scanner, modelSetting, startTimeMillis);
            }

            if (requiresApproval(command)) {
                final List<String> finalShellCommands = shellCommands;
                final int finalIndex = index;
                final MetadataContainer finalResponse = response;
                final AIManager finalAim = aim;
                final StringBuilder finalTranscript = transcript;
                final Scanner finalScanner = scanner;
                final String finalModelSetting = modelSetting;
                final long finalStartTimeMillis = startTimeMillis;

                return completeRequestApproval(command, scanner).thenCompose(approved -> {
                    if (!approved) {
                        String msg = "⛔ Command rejected by user.";
                        System.out.println(msg);
                        finalTranscript.append(msg).append("\n");
                        String updatedPrompt = contextManager.buildPromptContext();
                        return finalAim.completeLocalShellRequest(updatedPrompt, null, finalModelSetting, "response")
                            .thenCompose(next -> completeProcessREPLLoop(next, finalAim, finalTranscript, finalScanner, finalModelSetting, finalStartTimeMillis));
                    }
                    return completeMultipleCommands(finalShellCommands, finalIndex, finalResponse, finalAim, finalTranscript, finalScanner, finalModelSetting, finalStartTimeMillis);
                });
            }
        }

        String updatedPrompt = contextManager.buildPromptContext();
        return aim.completeLocalShellRequest(updatedPrompt, null, modelSetting, "response")
            .thenCompose(next -> completeProcessREPLLoop(next, aim, transcript, scanner, modelSetting, startTimeMillis));
    }

    private CompletableFuture<String> completeMultipleCommands(
        List<String> commands,
        int index,
        MetadataContainer response,
        AIManager aim,
        StringBuilder transcript,
        Scanner scanner,
        String modelSetting,
        long startTimeMillis
    ) {
        if (index >= commands.size()) {
            return CompletableFuture.completedFuture(transcript.toString());
        }

        String command = commands.get(index);
        contextManager.addEntry(new ContextEntry(ContextEntry.Type.COMMAND, command));

        // Required for lambda
        final int currentIndex = index;
        final List<String> finalCommands = new ArrayList<>(commands);
        final MetadataContainer finalResponse = response;
        final AIManager finalAim = aim;
        final StringBuilder finalTranscript = transcript;
        final Scanner finalScanner = scanner;
        final String finalModelSetting = modelSetting;
        final long finalStartTimeMillis = startTimeMillis;

        return toolHandler.completeShellCommand(finalResponse, command, contextManager)
            .thenCompose(output -> {
                finalTranscript.append("> ").append(command).append("\n").append(output).append("\n");
                System.out.println("> " + command);
                contextManager.addEntry(new ContextEntry(ContextEntry.Type.SHELL_OUTPUT, output));
                finalResponse.put(ResponseObject.LOCALSHELLTOOL_COMMAND_INDEX, currentIndex + 1);
                return completeMultipleCommands(finalCommands, currentIndex + 1, finalResponse, finalAim, finalTranscript, finalScanner, finalModelSetting, finalStartTimeMillis);
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
        if (maxSessionDurationMillis > 0 &&
            System.currentTimeMillis() - startTimeMillis > maxSessionDurationMillis) {
            String msg = "\n⏰ REPL session timed out after " + (maxSessionDurationMillis / 1000) + " seconds.\n";
            transcript.append(msg);
            return CompletableFuture.completedFuture(transcript.toString());
        }

        contextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, input));
        String prompt = contextManager.buildPromptContext();
        return aim.completeLocalShellRequest(prompt, null, modelSetting, "response")
            .thenCompose(response -> {
                response.put(ResponseObject.LOCALSHELLTOOL_COMMAND_INDEX, 0);
                return completeProcessREPLLoop(response, aim, transcript, scanner, modelSetting, startTimeMillis);
            });
    }

    private CompletableFuture<Boolean> completeRequestApproval(String command, Scanner scanner) {
        return CompletableFuture.supplyAsync(() -> {
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
        contextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, initialMessage));
        AIManager aim = new AIManager();
        StringBuilder transcript = new StringBuilder();
        String model = ModelRegistry.OPENROUTER_RESPONSE_MODEL.asString();
        return completeREPLLoop(initialMessage, aim, transcript, scanner, model, System.currentTimeMillis());
    }

    public void startResponseInputThread() {
        inputExecutor.submit(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                System.out.println("Response input thread started. Type your messages:");
                while (true) {
                    System.out.print("> ");
                    String input;
                    try {
                        input = scanner.nextLine();
                    } catch (NoSuchElementException e) {
                        System.out.println("Input stream closed.");
                        break;
                    }

                    if (input.equalsIgnoreCase(".exit") || input.equalsIgnoreCase(".quit")) {
                        System.out.println("Exiting response input thread.");
                        break;
                    }

                    completeREPLAsync(scanner, input)
                        .thenAcceptAsync(response -> System.out.println("Bot: " + response), replExecutor);
                }
            } catch (IllegalStateException e) {
                System.out.println("System.in is unavailable.");
            }
        });
    }
}


// theres an issue with multiple commands vs single command and the redundant call to AIM.
