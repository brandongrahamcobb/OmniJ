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
    private String originalDirective;
    private final ExecutorService replExecutor = Executors.newFixedThreadPool(2);
    private AIManager aim = new AIManager();
    private ToolHandler toolHandler = new ToolHandler();
    
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
        };        return result;
    }
    
    /*
     * REPL : start
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
    
    // REPL : start
    private CompletableFuture<String> completeStartREPL(Scanner scanner, String userInput) {
        this.originalDirective = userInput;
        contextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, userInput));
        return completeRStep(scanner).thenCompose(response -> completeEStep(response, scanner));
    }


    // REPL : R
    private CompletableFuture<MetadataContainer> completeRStep(Scanner scanner) {
        String prompt = contextManager.buildPromptContext();
        String model = ModelRegistry.LOCAL_RESPONSE_MODEL.asString();
        return aim.completeLocalShellRequest(prompt, null, model, "response");
    }

    // REPL : E
    private CompletableFuture<String> completeEStep(MetadataContainer response, Scanner scanner) {
        List<String> commands = response.get(ResponseObject.LOCALSHELLTOOL_COMMANDS);
        boolean finished = response.getOrDefault(ResponseObject.LOCALSHELLTOOL_FINISHED, false);
        if (commands == null || commands.isEmpty()) {
            return CompletableFuture.completedFuture("No shell commands to execute.");
        }
        return completeESubStep(commands, 0, response, scanner).thenCompose(transcript -> {
            if (finished) {
                System.out.println("✅ Task complete.");
                return CompletableFuture.completedFuture(transcript);
            }
            return completeLStep(scanner);
        });
    }

    // REPL : E
    private CompletableFuture<String> completeESubStep(
            List<String> commands,
            int index,
            MetadataContainer response,
            Scanner scanner
    ) {
        if (index >= commands.size()) {
            return CompletableFuture.completedFuture("");
        }
        String command = commands.get(index);
        if (requiresApproval(command)) {
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
                    return completeESubStep(commands, index + 1, response, scanner);
                }
                return completeESubSubStep(command, commands, index, response, scanner);
            });
        } else {
            return completeESubSubStep(command, commands, index, response, scanner);
        }
    }

    // REPL : E
    private CompletableFuture<String> completeESubSubStep(
            String command,
            List<String> commands,
            int index,
            MetadataContainer response,
            Scanner scanner
    ) {
        contextManager.addEntry(new ContextEntry(ContextEntry.Type.COMMAND, command));
        return toolHandler.completeShellCommand(response, command).thenCompose(output -> {
            System.out.println("> " + command + "\n" + output);
            contextManager.addEntry(new ContextEntry(ContextEntry.Type.SHELL_OUTPUT, output));
            return completeESubStep(commands, index + 1, response, scanner);
        });
    }
    
    // REPL : P
    private void completePStep(MetadataContainer response) {
        String summary = new ResponseUtils(response).completeGetLocalShellToolSummary().join();
        if (summary != null && !summary.isBlank()) {
            System.out.println("\n[Model Summary]:\n" + summary + "\n");
        }
        long tokens = contextManager.getContextTokenCount();
        System.out.println("Current context token count: " + tokens);
    }

    // REPL : L
    private CompletableFuture<String> completeLStep(Scanner scanner) {
        return completeRStep(scanner).thenCompose(response -> {
            completePStep(response);
            return completeEStep(response, scanner);
        });
    }
}
