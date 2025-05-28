package com.brandongcobb.vyrtuous.utils.handlers;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import com.brandongcobb.vyrtuous.utils.inc.*;

public class REPLManager {
    private final ShellCommandExecutor executor = new ShellCommandExecutor();
    private String originalDirective;
    private ApprovalMode approvalMode = ApprovalMode.FULL_AUTO;
    private final ContextManager contextManager = new ContextManager(999999999);
    private final List<String> shellHistory = new ArrayList<>();
    private final long maxSessionDurationMillis;
    private final ExecutorService inputExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService replExecutor = Executors.newFixedThreadPool(2);

    public REPLManager(ApprovalMode mode, long maxSessionDurationMillis) {
        setApprovalMode(mode);
        this.maxSessionDurationMillis = maxSessionDurationMillis;
    }

    public REPLManager(ApprovalMode mode) {
        this(mode, 0L);
    }

    public void setApprovalMode(ApprovalMode mode) {
        this.approvalMode = mode;
    }

    // Check if command is dangerous for approval purposes
    private boolean isDangerousCommand(String command) {
        if (command == null) return false;
        List<String> dangerous = List.of("rm", "mv", "git", "patch", "shutdown", "reboot", "mvn compile");
        return dangerous.stream().anyMatch(command::contains);
    }

    // Determine if approval is needed based on mode and command
    private boolean requiresApproval(String command) {
        return switch (approvalMode) {
            case FULL_AUTO -> false;
            case EDIT_APPROVE_ALL -> true;
            case EDIT_APPROVE_DESTRUCTIVE -> isDangerousCommand(command);
        };
    }

    // Async approval prompt to user
    private CompletableFuture<Boolean> requestApprovalAsync(String command, Scanner scanner) {
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

    // Main REPL loop handling input, AI response, execution, and approval logic
    private CompletableFuture<String> runReplLoop(
        String input,
        AIManager aim,
        StringBuilder transcript,
        Scanner scanner,
        String modelSetting,
        long startTimeMillis
    ) {
        if (maxSessionDurationMillis > 0) {
            long elapsed = System.currentTimeMillis() - startTimeMillis;
            if (elapsed > maxSessionDurationMillis) {
                transcript.append("\n⏰ REPL session timed out after ")
                         .append(maxSessionDurationMillis / 1000).append(" seconds.\n");
                return CompletableFuture.completedFuture(transcript.toString());
            }
        }

        contextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, input));
        String prompt = contextManager.buildPromptContext();

        return aim.completeLocalRequest(prompt, null, modelSetting, "completion")
            .thenCompose(response -> processResponseLoop(response, aim, transcript, scanner, modelSetting, startTimeMillis));
    }

    // Process AI response: summarize, check finish, get command, approval, execution
    private CompletableFuture<String> processResponseLoop(
        ResponseObject response,
        AIManager aim,
        StringBuilder transcript,
        Scanner scanner,
        String modelSetting,
        long startTimeMillis
    ) {
        // Print and store AI summary if present
        String summary = response.completeGetLocalShellToolSummary().join();
        if (summary != null && !summary.isBlank()) {
            System.out.println("\n[Model Summary]: " + summary + "\n");
            contextManager.addEntry(new ContextEntry(ContextEntry.Type.AI_RESPONSE, summary));
        }

        // Check if AI signals the REPL is finished
        return response.completeGetShellToolFinished().thenCompose(finished -> {
            if (Boolean.TRUE.equals(finished)) {
                System.out.println("✅ Task complete.");
                System.out.println("\nFinal Summary:\n" + transcript.toString());
                return CompletableFuture.completedFuture(transcript.toString());
            }

            // Get next shell command from AI
            String shellCommand = response.get(ResponseObject.LOCALSHELLTOOL_COMMAND);

            if (shellCommand == null || shellCommand.isBlank()) {
                // If no command, get user input and continue
                System.out.println("[Model]: I need clarification before proceeding.");
                System.out.print("> ");
                String userInput = scanner.nextLine();
                contextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, userInput));
                return runReplLoop(userInput, aim, transcript, scanner, modelSetting, startTimeMillis);
            }

            // Depending on approval mode, approve or directly run command
            if (requiresApproval(shellCommand)) {
                return requestApprovalAsync(shellCommand, scanner).thenCompose(approved -> {
                    if (!approved) {
                        System.out.println("⛔ Command rejected by user.");
                        transcript.append("⛔ Command rejected by user.\n");
                        return CompletableFuture.completedFuture(transcript.toString());
                    }
                    return executeCommandAndContinue(response, aim, transcript, scanner, modelSetting, startTimeMillis);
                });
            } else {
                // No approval needed, execute directly
                long tokens = contextManager.getContextTokenCount();
                System.out.println("Current context token count: " + tokens);
                return executeCommandAndContinue(response, aim, transcript, scanner, modelSetting, startTimeMillis);
            }
        });
    }

    // Execute the shell command, append output to transcript, then loop again
    private CompletableFuture<String> executeCommandAndContinue(
        ResponseObject response,
        AIManager aim,
        StringBuilder transcript,
        Scanner scanner,
        String modelSetting,
        long startTimeMillis
    ) {
        String shellCommand = response.get(ResponseObject.LOCALSHELLTOOL_COMMAND);
        contextManager.addEntry(new ContextEntry(ContextEntry.Type.COMMAND, shellCommand));
        ToolHandler toolHandler = new ToolHandler();

        return toolHandler.executeShellCommandAsync(response).thenCompose(output -> {
            transcript.append("> ").append(shellCommand).append("\n").append(output).append("\n");
            System.out.println("> " + shellCommand);
            System.out.println(output);
            contextManager.addEntry(new ContextEntry(ContextEntry.Type.SHELL_OUTPUT, output));

            // Prepare prompt for next AI response
            String updatedPrompt = contextManager.buildPromptContext();
            return aim.completeLocalRequest(updatedPrompt, null, modelSetting, "completion")
                .thenCompose(nextResponse -> processResponseLoop(nextResponse, aim, transcript, scanner, modelSetting, startTimeMillis));
        });
    }

    // Entry point to start the REPL with input thread that reads user input continuously
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
            } finally {
                shutdownExecutors();
            }
        });
    }

    private void shutdownExecutors() {
        inputExecutor.shutdown();
        replExecutor.shutdown();
        try {
            if (!inputExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                inputExecutor.shutdownNow();
            }
            if (!replExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                replExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            inputExecutor.shutdownNow();
            replExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // Kick off REPL logic given initial user input and Scanner for approval prompts if needed
    private CompletableFuture<String> completeREPLAsync(Scanner scanner, String initialMessage) {
        this.originalDirective = initialMessage;
        contextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, initialMessage));
        AIManager aim = new AIManager();
        StringBuilder transcript = new StringBuilder();
        String model = ModelRegistry.GEMINI_RESPONSE_MODEL.asString();
        return runReplLoop(initialMessage, aim, transcript, scanner, model, System.currentTimeMillis());
    }
}

//public class REPLManager {
//    private final ShellCommandExecutor executor = new ShellCommandExecutor();
//    private String originalDirective;
//    private final Map<Long, ResponseObject> userResponseMap = new ConcurrentHashMap<>();
//    private ApprovalMode approvalMode = ApprovalMode.FULL_AUTO;
//    private final ContextManager contextManager = new ContextManager(50);
//    private final List<String> shellHistory = new ArrayList<>();
//    private final long maxSessionDurationMillis;
//    private final ExecutorService inputExecutor = Executors.newSingleThreadExecutor();
//    private final ExecutorService replExecutor = Executors.newFixedThreadPool(2);
//    private static final long TIMEOUT_MILLIS = 30_000;
//
//    public REPLManager(ApprovalMode mode, long maxSessionDurationMillis) {
//        setApprovalMode(mode);
//        this.maxSessionDurationMillis = maxSessionDurationMillis;
//    }
//
//    public REPLManager(ApprovalMode mode) {
//        this(mode, 0L);
//    }
//
//    public void setApprovalMode(ApprovalMode mode) {
//        this.approvalMode = mode;
//    }
//
//    private CompletableFuture<Boolean> requestApprovalAsync(String command, Scanner scanner, ResponseObject response) {
//        return CompletableFuture.supplyAsync(() -> {
//            System.out.println("Approval required for command: " + command);
//            System.out.print("Approve? (yes/no): ");
//            while (true) {
//                String input = scanner.nextLine().trim().toLowerCase();
//                if (input.equals("yes") || input.equals("y")) return true;
//                if (input.equals("no") || input.equals("n")) return false;
//                System.out.print("Please type 'yes' or 'no': ");
//            }
//        });
//    }
//
//    private CompletableFuture<String> runReplLoop(
//        String input,
//        AIManager aim,
//        StringBuilder transcript,
//        Scanner scanner,
//        String modelSetting,
//        ApprovalMode approvalMode,
//        long startTimeMillis
//    ) {
//        if (maxSessionDurationMillis > 0) {
//            long elapsed = System.currentTimeMillis() - startTimeMillis;
//            if (elapsed > maxSessionDurationMillis) {
//                transcript.append("\n⏰ REPL session timed out after ")
//                         .append(maxSessionDurationMillis / 1000).append(" seconds.\n");
//                return CompletableFuture.completedFuture(transcript.toString());
//            }
//        }
//
//        contextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, input));
//        String prompt = contextManager.buildPromptContext();
//
//        return aim.completeLocalRequest(prompt, null, modelSetting, "completion")
//            .thenCompose(response -> processResponseLoop(response, aim, transcript, scanner, modelSetting, approvalMode, startTimeMillis));
//    }
//
//    private CompletableFuture<String> processResponseLoop(
//        ResponseObject response,
//        AIManager aim,
//        StringBuilder transcript,
//        Scanner scanner,
//        String modelSetting,
//        ApprovalMode approvalMode,
//        long startTimeMillis
//    ) {
//        String summary = response.completeGetLocalShellToolSummary().join();
//        if (summary != null && !summary.isBlank()) {
//            System.out.println("\n[Model Summary]: " + summary + "\n");
//            contextManager.addEntry(new ContextEntry(ContextEntry.Type.AI_RESPONSE, summary));
//        }
//
//        return response.completeGetShellToolFinished().thenCompose(finished -> {
//            if (Boolean.TRUE.equals(finished)) {
//                System.out.println("✅ Task complete.");
//                return CompletableFuture.completedFuture(transcript.toString());
//            }
//
//            String shellCommand = response.get(ResponseObject.LOCALSHELLTOOL_COMMAND);
//            if (shellCommand == null || shellCommand.isBlank()) {
//                System.out.println("[Model]: I need clarification before proceeding.");
//                System.out.print("> ");
//                String userInput = scanner.nextLine();
//                contextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, userInput));
//                return runReplLoop(userInput, aim, transcript, scanner, modelSetting, approvalMode, startTimeMillis);
//            }
//
//            contextManager.addEntry(new ContextEntry(ContextEntry.Type.COMMAND, shellCommand));
//            ToolHandler toolHandler = new ToolHandler();
//
//            return toolHandler.executeShellCommandAsync(response).thenCompose(output -> {
//                transcript.append("> ").append(shellCommand).append("\n").append(output).append("\n");
//                System.out.println("> " + shellCommand);
//                System.out.println(output);
//                contextManager.addEntry(new ContextEntry(ContextEntry.Type.SHELL_OUTPUT, output));
//
//                // 🔁 Now re-prompt with updated context
//                String updatedPrompt = contextManager.buildPromptContext();
//                return aim.completeLocalRequest(updatedPrompt, null, modelSetting, "completion")
//                    .thenCompose(nextResponse -> processResponseLoop(
//                        nextResponse, aim, transcript, scanner, modelSetting, approvalMode, startTimeMillis
//                    ));
//            });
//        });
//    }
//
//
//    private CompletableFuture<ResponseObject> awaitWithTimeoutRetry(Supplier<CompletableFuture<ResponseObject>> supplier) {
//        return supplier.get()
//                .orTimeout(600, TimeUnit.SECONDS)
//                .handle((resp, ex) -> {
//                    if (ex == null) return CompletableFuture.completedFuture(resp);
//                    Throwable cause = (ex instanceof CompletionException && ex.getCause() != null) ? ex.getCause() : ex;
//                    if (cause instanceof TimeoutException) {
//                        System.err.println("⚠️ OpenAI request timed out after 600 seconds. Retrying...");
//                        return awaitWithTimeoutRetry(supplier);
//                    }
//                    return CompletableFuture.<ResponseObject>failedFuture(cause);
//                })
//                .thenCompose(f -> f);
//    }
//
//    private boolean requiresApproval(String command) {
//        if (command == null) return false;
//        List<String> dangerous = List.of("rm", "mv", "git", "patch", "shutdown", "reboot", "mvn compile");
//        boolean isDangerous = dangerous.stream().anyMatch(command::contains);
//        return switch (approvalMode) {
//            case FULL_AUTO -> false;
//            case EDIT_APPROVE_ALL -> true;
//            case EDIT_APPROVE_DESTRUCTIVE -> isDangerous;
//        };
//    }
//
//    private CompletableFuture<String> processResponseLoop(ResponseObject response) {
//        ToolHandler toolHandler = new ToolHandler();
//
//        String command = response.get(LOCALSHELLTOOL_COMMAND);
//
//        if (command == null || command.isBlank()) {
//            // No command to execute, maybe just continue or end
//            return CompletableFuture.completedFuture("⚠️ No shell command provided.");
//        }
//
//        // Check if approval is required
//        if (requiresApproval(command)) {
//            boolean approved = promptUserApproval(command); // You need to implement a way to get user approval (sync or async)
//            if (!approved) {
//                return CompletableFuture.completedFuture("⛔ Command rejected by user.");
//            }
//        }
//
//        // Execute shell command asynchronously
//        return toolHandler.executeShellCommandAsync(response)
//            .thenCompose(execOutput -> {
//                Integer exitCode = response.get(SHELL_EXIT_CODE);
//                if (exitCode == null) exitCode = -1;
//
//                if (exitCode != 0) {
//                    // Command failed, notify user, ask to retry or abort
//                    boolean retry = promptUserRetry(execOutput); // Implement user prompt logic
//                    if (retry) {
//                        // Re-run processResponseLoop with same response or modify response as needed
//                        return processResponseLoop(response);
//                    } else {
//                        // Abort session or handle as needed
//                        return CompletableFuture.completedFuture("❌ Aborted due to shell command failure.");
//                    }
//                }
//
//                // Command succeeded, check if finished
//                return response.completeGetShellToolFinished()
//                    .thenCompose(finished -> {
//                        if (Boolean.TRUE.equals(finished)) {
//                            return CompletableFuture.completedFuture("✅ REPL finished successfully.");
//                        } else {
//                            // Call AI to get next command response (your aiManager logic)
//                            return aiManager.completeLocalRequest(execOutput, null, ModelRegistry.GEMINI_RESPONSE_MODEL.asString(), "completion")
//                                .thenCompose(nextResponse -> processResponseLoop(nextResponse));
//                        }
//                    });
//            });
//    }
//
//    public void startResponseInputThread() {
//        inputExecutor.submit(() -> {
//            try (Scanner scanner = new Scanner(System.in)) {
//                System.out.println("Response input thread started. Type your messages:");
//                while (true) {
//                    System.out.print("> ");
//                    String input;
//                    try {
//                        input = scanner.nextLine();
//                    } catch (NoSuchElementException e) {
//                        System.out.println("Input stream closed.");
//                        break;
//                    }
//
//                    if (input.equalsIgnoreCase(".exit") || input.equalsIgnoreCase(".quit")) {
//                        System.out.println("Exiting response input thread.");
//                        break;
//                    }
//
//                    completeREPLAsync(scanner, input)
//                        .thenAcceptAsync(response -> System.out.println("Bot: " + response), replExecutor);
//                }
//            } catch (IllegalStateException e) {
//                System.out.println("System.in is unavailable.");
//            }
//        });
//    }
//
//    private CompletableFuture<String> completeREPLAsync(Scanner scanner, String initialMessage) {
//        this.originalDirective = initialMessage;
//        contextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, initialMessage));
//        AIManager aim = new AIManager();
//        StringBuilder transcript = new StringBuilder();
//        String model = ModelRegistry.GEMINI_RESPONSE_MODEL.asString();
//        ApprovalMode approvalMode = ApprovalMode.FULL_AUTO;
//        return runReplLoop(initialMessage, aim, transcript, scanner, model, approvalMode, System.currentTimeMillis());
//    }
//}
//package com.brandongcobb.vyrtuous.utils.handlers;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Scanner;
//import java.util.NoSuchElementException;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.CompletionException;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.TimeoutException;
//import java.util.function.Supplier;
//import com.brandongcobb.vyrtuous.utils.inc.*;
//
//public class REPLManager {
//    
//    private final ShellCommandExecutor executor = new ShellCommandExecutor();
//    private String originalDirective;
//    private final Map<Long, ResponseObject> userResponseMap = new ConcurrentHashMap<>();
//    private ApprovalMode approvalMode = ApprovalMode.FULL_AUTO;
//    private final ContextManager contextManager = new ContextManager(50);
//    private final List<String> shellHistory = new ArrayList<>();
//    private final long maxSessionDurationMillis;
//    private final ExecutorService inputExecutor = Executors.newSingleThreadExecutor();
//    private final ExecutorService replExecutor = Executors.newFixedThreadPool(2);
//    private static final long TIMEOUT_MILLIS = 30_000;
//    
//    /**
//     * Constructor with approval mode and optional session timeout.
//     */
//    public REPLManager(ApprovalMode mode, long maxSessionDurationMillis) {
//        setApprovalMode(mode);
//        this.maxSessionDurationMillis = maxSessionDurationMillis;
//    }
//
//    /**
//     * Constructor with approval mode and no timeout.
//     */
//    public REPLManager(ApprovalMode mode) {
//        this(mode, 0L);
//    }
//    
//    public void setApprovalMode(ApprovalMode mode) {
//        this.approvalMode = mode;
//    }
//
//    /**
//     * Requests async approval from user on destructive commands.
//     */
//    private CompletableFuture<Boolean> requestApprovalAsync(String command, Scanner scanner, ResponseObject response) {
//        return CompletableFuture.supplyAsync(() -> {
//            System.out.println("Approval required for command: " + command);
//            System.out.print("Approve? g(yes/no): ");
//            while (true) {
//                String input = scanner.nextLine().trim().toLowerCase();
//                if (input.equals("yes") || input.equals("y")) return true;
//                if (input.equals("no") || input.equals("n")) return false;
//                System.out.print("Please type 'yes' or 'no': ");
//            }
//        });
//    }
//
//    /**
//     * Main REPL process loop with AI calls and shell command execution.
//     */
//    private CompletableFuture<String> runReplLoop(
//        String input,
//        AIManager aim,
//        StringBuilder transcript,
//        Scanner scanner,
//        String modelSetting,
//        ApprovalMode approvalMode,
//        long startTimeMillis
//    ) {
//        if (maxSessionDurationMillis > 0) {
//            long elapsed = System.currentTimeMillis() - startTimeMillis;
//            if (elapsed > maxSessionDurationMillis) {
//                System.out.println("[DEBUG] Timeout reached after " + (maxSessionDurationMillis / 1000) + " seconds. Ending REPL.");
//                transcript.append("\n⏰ REPL session timed out after ").append(maxSessionDurationMillis / 1000).append(" seconds.\n");
//                return CompletableFuture.completedFuture(transcript.toString());
//            }
//        }
//        // STEP 1: Store user input in structured context
//        contextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, input));
//
//        // STEP 2: Build structured prompt
//        String prompt = contextManager.buildPromptContext();
//
//        // STEP 3: Send structured prompt to the AI
//        return aim.completeLocalRequest(prompt, null, modelSetting, "completion")
//            .thenCompose(response -> {
//                System.out.println("[DEBUG] Received AI response");
//                contextManager.addEntry(new ContextEntry(ContextEntry.Type.AI_RESPONSE, response.completeGetLocalShellToolSummary().join()));
//                return response.completeGetShellToolFinished()
//                    .thenCompose(isFinished -> {
//                        System.out.println("[DEBUG] Shell finished flag: " + isFinished);
//                        if (isFinished) {
//                            return CompletableFuture.completedFuture(transcript.toString());
//                        }
//
//                        String shellCommand = response.get(ResponseObject.LOCALSHELLTOOL_COMMAND);
//                        if (shellCommand == null || shellCommand.isBlank()) {
//                            return CompletableFuture.completedFuture(transcript.toString());
//                        }
//
//                        System.out.println("AI suggests running: " + shellCommand);
//                        contextManager.addEntry(new ContextEntry(ContextEntry.Type.COMMAND, shellCommand));
//
//                        ToolHandler toolHandler = new ToolHandler();
//                        return toolHandler.executeShellCommandAsync(response)
//                            .thenCompose(output -> {
//                                System.out.println("[DEBUG] Shell output: " + output);
//                                transcript.append("> ").append(shellCommand).append("\n");
//                                transcript.append(output).append("\n");
//                                contextManager.addEntry(new ContextEntry(ContextEntry.Type.SHELL_OUTPUT, "> " + shellCommand + "\n" + output));
//                                if (approvalMode == ApprovalMode.FULL_AUTO) {
//                                    // Sustain the original directive by repeating the user goal, letting AI continue planning
//                                    transcript.append("> ").append(shellCommand).append("\n");
//                                    transcript.append(output).append("\n");
//
//                                    // Instead of repeating the directive, include all prior stdout
//                                    String followupInput = transcript.toString();
//                                    contextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, followupInput));
//                                    return runReplLoop(followupInput, aim, transcript, scanner, modelSetting, approvalMode, startTimeMillis);
//                                    
//                                } else {
//                                    // Ask for next user input
//                                    System.out.print("> ");
//                                    String nextInput = scanner.nextLine();
//                                    return runReplLoop(nextInput, aim, transcript, scanner, modelSetting, approvalMode, startTimeMillis);
//                                }
//                            });
//                    });
//            });
//    }
//
//
//
//    /**
//     * Await AI completion with timeout and retry on failure.
//     */
//    private CompletableFuture<ResponseObject> awaitWithTimeoutRetry(Supplier<CompletableFuture<ResponseObject>> supplier) {
//        return supplier.get()
//                .orTimeout(600, TimeUnit.SECONDS)
//                .handle((resp, ex) -> {
//                    if (ex == null) return CompletableFuture.completedFuture(resp);
//                    Throwable cause = (ex instanceof CompletionException && ex.getCause() != null) ? ex.getCause() : ex;
//                    if (cause instanceof TimeoutException) {
//                        System.err.println("?? OpenAI request timed out after 600 seconds. Retrying...");
//                        return awaitWithTimeoutRetry(supplier);
//                    }
//                    return CompletableFuture.<ResponseObject>failedFuture(cause);
//                })
//                .thenCompose(f -> f);
//    }
//
//    /**
//     * Checks if a shell command requires user approval.
//     */
//    private boolean requiresApproval(String command) {
//        if (command == null) return false;
//
//        List<String> dangerous = List.of("rm", "mv", "git", "patch", "shutdown", "reboot", "mvn compile");
//        boolean isDangerous = dangerous.stream().anyMatch(command::contains);
//
//        return switch (approvalMode) {
//            case FULL_AUTO -> false;
//            case EDIT_APPROVE_ALL -> true;
//            case EDIT_APPROVE_DESTRUCTIVE -> isDangerous;
//        };
//    }
//    
//    /**
//     * Starts the thread listening for user input for the REPL.
//     */
//    public void startResponseInputThread() {
//        inputExecutor.submit(() -> {
//            try (Scanner scanner = new Scanner(System.in)) {
//                System.out.println("Response input thread started. Type your messages:");
//                while (true) {
//                    System.out.print("> ");
//                    String input;
//                    try {
//                        input = scanner.nextLine();
//                    } catch (NoSuchElementException e) {
//                        System.out.println("Input stream closed.");
//                        break;
//                    }
//
//                    if (input.equalsIgnoreCase(".exit") || input.equalsIgnoreCase(".quit")) {
//                        System.out.println("Exiting response input thread.");
//                        break;
//                    }
//
//                    completeREPLAsync(scanner, input)
//                        .thenAcceptAsync(response -> System.out.println("Bot: " + response), replExecutor);
//                }
//            } catch (IllegalStateException e) {
//                System.out.println("System.in is unavailable.");
//            }
//        });
//    }
//
//    // Placeholder for your REPL async completion method.
//    private CompletableFuture<String> completeREPLAsync(Scanner scanner, String initialMessage) {
//        this.originalDirective = initialMessage;
//        contextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, initialMessage));
//        AIManager aim = new AIManager();
//        StringBuilder transcript = new StringBuilder();
//        String model = ModelRegistry.GEMINI_RESPONSE_MODEL.asString();
//        ApprovalMode approvalMode = ApprovalMode.FULL_AUTO;
//        // Kick off the loop with initial input
//        return runReplLoop(initialMessage, aim, transcript, scanner, model, approvalMode, System.currentTimeMillis());
//    }
//
//}

