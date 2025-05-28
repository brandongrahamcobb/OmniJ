package com.brandongcobb.vyrtuous.utils.handlers;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.logging.*;
import com.brandongcobb.vyrtuous.utils.inc.*;

public class REPLManager {
    private static final Logger LOGGER = Logger.getLogger(REPLManager.class.getName());

    private final ShellCommandExecutor executor = new ShellCommandExecutor();
    private String originalDirective;
    private ApprovalMode approvalMode = ApprovalMode.FULL_AUTO;
    private final ContextManager contextManager = new ContextManager(29);
    private final List<String> shellHistory = new ArrayList<>();
    private final long maxSessionDurationMillis;
    private final ExecutorService inputExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService replExecutor = Executors.newFixedThreadPool(2);

    public REPLManager(ApprovalMode mode, long maxSessionDurationMillis) {
        setApprovalMode(mode);
        this.maxSessionDurationMillis = maxSessionDurationMillis;
        LOGGER.info("REPLManager initialized with mode " + mode + " and max duration " + maxSessionDurationMillis + "ms");
    }

    public REPLManager(ApprovalMode mode) {
        this(mode, 0L);
    }

    public void setApprovalMode(ApprovalMode mode) {
        LOGGER.info("Approval mode set to: " + mode);
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

    private CompletableFuture<Boolean> requestApprovalAsync(String command, Scanner scanner) {
        return CompletableFuture.supplyAsync(() -> {
            LOGGER.info("Requesting user approval for command: " + command);
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

    private CompletableFuture<String> runReplLoop(
        String input,
        AIManager aim,
        StringBuilder transcript,
        Scanner scanner,
        String modelSetting,
        long startTimeMillis
    ) {
        LOGGER.info("Running REPL loop with input: " + input);
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

        return aim.completeLocalRequest(prompt, null, modelSetting, "completion")
            .thenCompose(response -> processResponseLoop(response, aim, transcript, scanner, modelSetting, startTimeMillis));
    }

    private CompletableFuture<String> processResponseLoop(
        ResponseObject response,
        AIManager aim,
        StringBuilder transcript,
        Scanner scanner,
        String modelSetting,
        long startTimeMillis
    ) {
        String summary = response.completeGetLocalShellToolSummary().join();
        if (summary != null && !summary.isBlank()) {
            System.out.println("\n[Model Summary]: " + summary + "\n");
            contextManager.addEntry(new ContextEntry(ContextEntry.Type.AI_RESPONSE, summary));
            LOGGER.info("Model summary: " + summary);
        }

        return response.completeGetShellToolFinished().thenCompose(finished -> {
            if (Boolean.TRUE.equals(finished)) {
                LOGGER.info("AI indicated task is finished.");
                System.out.println("✅ Task complete.");
                System.out.println("\nFinal Summary:\n" + transcript.toString());
                return CompletableFuture.completedFuture(transcript.toString());
            }

            String shellCommand = response.get(ResponseObject.LOCALSHELLTOOL_COMMAND);
            LOGGER.info("Received shell command: " + shellCommand);

            if (shellCommand == null || shellCommand.isBlank()) {
                LOGGER.warning("AI response did not include a shell command. Asking user for clarification.");
                System.out.println("[Model]: I need clarification before proceeding.");
                System.out.print("> ");
                String userInput = scanner.nextLine();
                contextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, userInput));
                return runReplLoop(userInput, aim, transcript, scanner, modelSetting, startTimeMillis);
            }

            long tokens = contextManager.getContextTokenCount();
            LOGGER.fine("Current context token count: " + tokens);

            if (requiresApproval(shellCommand)) {
                return requestApprovalAsync(shellCommand, scanner).thenCompose(approved -> {
                    if (!approved) {
                        String rejectionMsg = "⛔ Command rejected by user.";
                        System.out.println(rejectionMsg);
                        transcript.append(rejectionMsg).append("\n");
                        LOGGER.warning("User rejected command: " + shellCommand);
                        return CompletableFuture.completedFuture(transcript.toString());
                    }
                    return executeCommandAndContinue(response, aim, transcript, scanner, modelSetting, startTimeMillis);
                });
            } else {
                return executeCommandAndContinue(response, aim, transcript, scanner, modelSetting, startTimeMillis);
            }
        });
    }

    private CompletableFuture<String> executeCommandAndContinue(
        ResponseObject response,
        AIManager aim,
        StringBuilder transcript,
        Scanner scanner,
        String modelSetting,
        long startTimeMillis
    ) {
        String shellCommand = response.get(ResponseObject.LOCALSHELLTOOL_COMMAND);
        LOGGER.info("Executing shell command: " + shellCommand);

        contextManager.addEntry(new ContextEntry(ContextEntry.Type.COMMAND, shellCommand));
        ToolHandler toolHandler = new ToolHandler();

        return toolHandler.executeShellCommandAsync(response).thenCompose(output -> {
            transcript.append("> ").append(shellCommand).append("\n").append(output).append("\n");
            System.out.println("> " + shellCommand);
            System.out.println(output);
            contextManager.addEntry(new ContextEntry(ContextEntry.Type.SHELL_OUTPUT, output));
            LOGGER.fine("Shell command output: " + output);

            String updatedPrompt = contextManager.buildPromptContext();
            return aim.completeLocalRequest(updatedPrompt, null, modelSetting, "completion")
                .thenCompose(nextResponse -> processResponseLoop(nextResponse, aim, transcript, scanner, modelSetting, startTimeMillis));
        });
    }

    public void startResponseInputThread() {
        LOGGER.info("Starting response input thread...");
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
                        LOGGER.info("User requested REPL shutdown.");
                        System.out.println("Exiting response input thread.");
                        break;
                    }

                    LOGGER.info("Received user input: " + input);
                    completeREPLAsync(scanner, input)
                        .thenAcceptAsync(response -> {
                            System.out.println("Bot: " + response);
                            LOGGER.info("REPL output: " + response);
                        }, replExecutor);
                }
            } catch (IllegalStateException e) {
                LOGGER.severe("System.in is unavailable.");
                System.out.println("System.in is unavailable.");
            } finally {
                shutdownExecutors();
            }
        });
    }

    private void shutdownExecutors() {
        LOGGER.info("Shutting down executors...");
        inputExecutor.shutdown();
        replExecutor.shutdown();
        try {
            if (!inputExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                inputExecutor.shutdownNow();
            }
            if (!replExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                replExecutor.shutdownNow();
            }
            LOGGER.info("Executors shut down cleanly.");
        } catch (InterruptedException e) {
            LOGGER.warning("Executor shutdown interrupted.");
            inputExecutor.shutdownNow();
            replExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private CompletableFuture<String> completeREPLAsync(Scanner scanner, String initialMessage) {
        this.originalDirective = initialMessage;
        LOGGER.info("Starting REPL session with: " + initialMessage);
        contextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, initialMessage));
        AIManager aim = new AIManager();
        StringBuilder transcript = new StringBuilder();
        String model = ModelRegistry.GEMINI_RESPONSE_MODEL.asString();
        return runReplLoop(initialMessage, aim, transcript, scanner, model, System.currentTimeMillis());
    }
}
