package com.brandongcobb.vyrtuous.utils.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import com.brandongcobb.vyrtuous.utils.inc.*;

public class REPLManager {
    
    private final ShellCommandExecutor executor = new ShellCommandExecutor();

    private final Map<Long, ResponseObject> userResponseMap = new ConcurrentHashMap<>();
    private ApprovalMode approvalMode = ApprovalMode.FULL_AUTO;
    private final ContextManager contextManager = new ContextManager(50);
    private final List<String> shellHistory = new ArrayList<>();
    private final long maxSessionDurationMillis;
    private final ExecutorService inputExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService replExecutor = Executors.newFixedThreadPool(2);
    private static final long TIMEOUT_MILLIS = 30_000;
    
    /**
     * Constructor with approval mode and optional session timeout.
     */
    public REPLManager(ApprovalMode mode, long maxSessionDurationMillis) {
        setApprovalMode(mode);
        this.maxSessionDurationMillis = maxSessionDurationMillis;
    }

    /**
     * Constructor with approval mode and no timeout.
     */
    public REPLManager(ApprovalMode mode) {
        this(mode, 0L);
    }
    
    public void setApprovalMode(ApprovalMode mode) {
        this.approvalMode = mode;
    }

    /**
     * Requests async approval from user on destructive commands.
     */
    private CompletableFuture<Boolean> requestApprovalAsync(String command, Scanner scanner, ResponseObject response) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("Approval required for command: " + command);
            System.out.print("Approve? g(yes/no): ");
            while (true) {
                String input = scanner.nextLine().trim().toLowerCase();
                if (input.equals("yes") || input.equals("y")) return true;
                if (input.equals("no") || input.equals("n")) return false;
                System.out.print("Please type 'yes' or 'no': ");
            }
        });
    }

    /**
     * Main REPL process loop with AI calls and shell command execution.
     */
    private CompletableFuture<String> runReplLoop(
        String input,
        AIManager aim,
        StringBuilder transcript,
        Scanner scanner,
        String modelSetting,
        ApprovalMode approvalMode,
        long startTimeMillis
    ) {
        if (maxSessionDurationMillis > 0) {
            long elapsed = System.currentTimeMillis() - startTimeMillis;
            if (elapsed > maxSessionDurationMillis) {
                System.out.println("[DEBUG] Timeout reached after " + (maxSessionDurationMillis / 1000) + " seconds. Ending REPL.");
                transcript.append("\n⏰ REPL session timed out after ").append(maxSessionDurationMillis / 1000).append(" seconds.\n");
                return CompletableFuture.completedFuture(transcript.toString());
            }
        }
        // STEP 1: Store user input in structured context
        contextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, input));

        // STEP 2: Build structured prompt
        String prompt = contextManager.buildPromptContext();

        // STEP 3: Send structured prompt to the AI
        return aim.completeLocalRequest(prompt, null, modelSetting, "completion")
            .thenCompose(response -> {
                System.out.println("[DEBUG] Received AI response");

                return response.completeGetShellToolFinished()
                    .thenCompose(isFinished -> {
                        System.out.println("[DEBUG] Shell finished flag: " + isFinished);
                        if (isFinished) {
                            return CompletableFuture.completedFuture(transcript.toString());
                        }

                        String shellCommand = response.get(ResponseObject.LOCALSHELLTOOL_COMMAND);
                        if (shellCommand == null || shellCommand.isBlank()) {
                            return CompletableFuture.completedFuture(transcript.toString());
                        }

                        System.out.println("AI suggests running: " + shellCommand);
                        contextManager.addEntry(new ContextEntry(ContextEntry.Type.COMMAND, shellCommand));

                        ToolHandler toolHandler = new ToolHandler();
                        return toolHandler.executeShellCommandAsync(response)
                            .thenCompose(output -> {
                                System.out.println("[DEBUG] Shell output: " + output);
                                transcript.append("> ").append(shellCommand).append("\n");
                                transcript.append(output).append("\n");
                                contextManager.addEntry(new ContextEntry(ContextEntry.Type.COMMAND_OUTPUT, output));

                                if (approvalMode == ApprovalMode.FULL_AUTO) {
                                    // Automatically continue without waiting for input
                                    return runReplLoop("", aim, transcript, scanner, modelSetting, approvalMode, startTimeMillis);
                                } else {
                                    // Ask for next user input
                                    System.out.print("> ");
                                    String nextInput = scanner.nextLine();
                                    return runReplLoop(nextInput, aim, transcript, scanner, modelSetting, approvalMode, startTimeMillis);
                                }
                            });
                    });
            });
    }



    /**
     * Await AI completion with timeout and retry on failure.
     */
    private CompletableFuture<ResponseObject> awaitWithTimeoutRetry(Supplier<CompletableFuture<ResponseObject>> supplier) {
        return supplier.get()
                .orTimeout(600, TimeUnit.SECONDS)
                .handle((resp, ex) -> {
                    if (ex == null) return CompletableFuture.completedFuture(resp);
                    Throwable cause = (ex instanceof CompletionException && ex.getCause() != null) ? ex.getCause() : ex;
                    if (cause instanceof TimeoutException) {
                        System.err.println("?? OpenAI request timed out after 600 seconds. Retrying...");
                        return awaitWithTimeoutRetry(supplier);
                    }
                    return CompletableFuture.<ResponseObject>failedFuture(cause);
                })
                .thenCompose(f -> f);
    }

    /**
     * Checks if a shell command requires user approval.
     */
    private boolean requiresApproval(String command) {
        if (command == null) return false;

        List<String> dangerous = List.of("rm", "mv", "git", "patch", "shutdown", "reboot", "mvn compile");
        boolean isDangerous = dangerous.stream().anyMatch(command::contains);

        return switch (approvalMode) {
            case FULL_AUTO -> false;
            case EDIT_APPROVE_ALL -> true;
            case EDIT_APPROVE_DESTRUCTIVE -> isDangerous;
        };
    }
    
    /**
     * Starts the thread listening for user input for the REPL.
     */
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

    // Placeholder for your REPL async completion method.
    private CompletableFuture<String> completeREPLAsync(Scanner scanner, String initialMessage) {
        AIManager aim = new AIManager();
        StringBuilder transcript = new StringBuilder();
        String model = ModelRegistry.GEMINI_RESPONSE_MODEL.asString();
        ApprovalMode approvalMode = ApprovalMode.FULL_AUTO;
        // Kick off the loop with initial input
        return runReplLoop(initialMessage, aim, transcript, scanner, model, approvalMode, System.currentTimeMillis());
    }

}

