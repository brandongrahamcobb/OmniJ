/*  Vyrtuous.java The primary purpose of this class is to integrate
 *  Discord, LinkedIn, OpenAI, Patreon, Twitch and many more into one
 *  hub.
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
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.brandongcobb.vyrtuous.utils.handlers;

import com.brandongcobb.vyrtuous.Vyrtuous;
import com.brandongcobb.vyrtuous.bots.DiscordBot;
import com.brandongcobb.metadata.MetadataContainer;
import com.brandongcobb.vyrtuous.utils.handlers.*;
import com.brandongcobb.vyrtuous.utils.inc.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CompletionException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.NoSuchElementException;
import net.dv8tion.jda.api.JDA;
import java.util.function.Supplier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;
import java.util.NoSuchElementException;
import java.util.function.Supplier;


public class REPLManager {
    
    private final Map<Long, ResponseObject> userResponseMap = new ConcurrentHashMap<>();
    
    private ApprovalMode approvalMode = ApprovalMode.FULL_AUTO;
    private final List<String> shellHistory = new ArrayList<>();
    // maximum duration for a REPL session in milliseconds (0 = unlimited)
    private final long maxSessionDurationMillis;
    private final ExecutorService inputExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService replExecutor = Executors.newFixedThreadPool(2);


    /**
     * Create a REPLManager with approval mode and optional time limit.
     * @param mode approval mode for destructive commands
     * @param maxSessionDurationMillis session timeout in milliseconds (0 = no limit)
     */
    public REPLManager(ApprovalMode mode, long maxSessionDurationMillis) {
        setApprovalMode(mode);
        this.maxSessionDurationMillis = maxSessionDurationMillis;
    }
    /**
     * Create a REPLManager with approval mode and no time limit.
     */
    public REPLManager(ApprovalMode mode) {
        this(mode, 0L);
    }
    
    public void setApprovalMode(ApprovalMode mode) {
        this.approvalMode = mode;
    }
    
    private CompletableFuture<String> completeREPLAsync(Scanner scanner, String initialMessage) {
        AIManager aim = new AIManager();
        StringBuilder fullTranscript = new StringBuilder();
        String modelSetting = ModelRegistry.GEMINI_RESPONSE_MODEL.asString();

        return processLoop(initialMessage, initialMessage, aim, fullTranscript, scanner, modelSetting)
            .handle((result, ex) -> {
                if (ex != null) {
                    String msg = (ex instanceof CompletionException && ex.getCause() != null) ? ex.getCause().getMessage() : ex.getMessage();
                    fullTranscript.append("⚠️ AI request failed: ").append(msg).append("\n");
                }
                return fullTranscript.toString();
            });
    }


    private CompletableFuture<Boolean> requestApprovalAsync(String command, Scanner scanner, ResponseObject response) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("Approval required for command: " + command);
            System.out.print("Approve? (yes/no): ");
            while (true) {
                String input = scanner.nextLine().trim().toLowerCase();
                if (input.equals("yes") || input.equals("y")) return true;
                if (input.equals("no") || input.equals("n")) return false;
                System.out.print("Please type 'yes' or 'no': ");
            }
        });
    }

    private CompletableFuture<Void> processLoop(
        String directive,
        String input,
        AIManager aim,
        StringBuilder fullTranscript,
        Scanner scanner,
        String modelSetting
    ) {
        StringBuilder prompt = new StringBuilder(directive);
        if (!shellHistory.isEmpty()) prompt.append("\n").append(String.join("\n", shellHistory));
        if (!input.equals(directive)) prompt.append("\n").append(input);
        System.out.println("🧠 Prompt: " + prompt);
        System.out.println("📦 Model setting: " + modelSetting);
        return awaitWithTimeoutRetry(() ->
                aim.completeLocalRequest(prompt.toString(), null, modelSetting, "completion")
        ).thenCompose(response -> {
            String command = response.get(ToolHandler.LOCALSHELLTOOL_COMMAND);

            if (command == null || command.isBlank()) {
                return response.completeGetOutput().thenAccept(output -> {
                    if (output != null && !output.isBlank()) {
                        fullTranscript.append("🤖 Message:\n").append(output).append("\n\n");
                    }
                });
            }

            if (requiresApproval(command)) {
                return requestApprovalAsync(command, scanner, response).thenCompose(approved -> {
                    if (!approved) {
                        fullTranscript.append("❌ Command not approved.\n");
                        return CompletableFuture.completedFuture(null);
                    }
                    return runAndRecordCommandAsync(response).thenCompose(result -> {
                        fullTranscript.append("🔁 Command:\n").append(command)
                                      .append("\n📤 Output:\n").append(result).append("\n\n");
                        return processLoop(directive, result, aim, fullTranscript, scanner, modelSetting);
                    });
                });
            }

            return runAndRecordCommandAsync(response).thenCompose(result -> {
                fullTranscript.append("🔁 Command:\n").append(command)
                              .append("\n📤 Output:\n").append(result).append("\n\n");
                return processLoop(directive, result, aim, fullTranscript, scanner, modelSetting);
            });
        });
    }

    private CompletableFuture<ResponseObject> awaitWithTimeoutRetry(Supplier<CompletableFuture<ResponseObject>> supplier) {
        return supplier.get()
                .orTimeout(600, TimeUnit.SECONDS)
                .handle((resp, ex) -> {
                    if (ex == null) return CompletableFuture.completedFuture(resp);
                    Throwable cause = (ex instanceof CompletionException && ex.getCause() != null) ? ex.getCause() : ex;
                    if (cause instanceof TimeoutException) {
                        System.err.println("?? OpenAI request timed out after 30 seconds. Retrying...");
                        return awaitWithTimeoutRetry(supplier);
                    }
                    return CompletableFuture.<ResponseObject>failedFuture(cause);
                })
                .thenCompose(f -> f);
    }

    private boolean requiresApproval(String command) {
        if (command == null) return false; // ⬅️ Prevents the crash

        List<String> dangerous = List.of("rm", "mv", "git", "patch", "shutdown", "reboot", "mvn compile");
        boolean isDangerous = dangerous.stream().anyMatch(command::contains);

        return switch (approvalMode) {
            case FULL_AUTO -> false;
            case EDIT_APPROVE_ALL -> true;
            case EDIT_APPROVE_DESTRUCTIVE -> isDangerous;
        };
    }

    private CompletableFuture<String> runAndRecordCommandAsync(ResponseObject responseObject) {
        ToolHandler th = new ToolHandler();
        return CompletableFuture.supplyAsync(() -> th.executeShellCommand(responseObject))
                .thenApply(result -> {
                    String command = responseObject.get(ToolHandler.LOCALSHELLTOOL_COMMAND);
                    shellHistory.add("> " + command + "\n" + result);
                    return result;
                });
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

                    // Call the async method directly and handle result on replExecutor
                    completeREPLAsync(scanner, input)
                        .thenAcceptAsync(response -> System.out.println("Bot: " + response), replExecutor);
                }
            } catch (IllegalStateException e) {
                System.out.println("System.in is unavailable.");
            }
        });
    }

}
