package com.brandongcobb.vyrtuous.utils.handlers;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.logging.*;
import com.brandongcobb.vyrtuous.utils.inc.*;
import com.brandongcobb.metadata.*;

public class REPLManager {
    private static final Logger LOGGER = Logger.getLogger(REPLManager.class.getName());

  //  private final ShellCommandExecutor executor = new ShellCommandExecutor();
    private String originalDirective;
    private ApprovalMode approvalMode = ApprovalMode.FULL_AUTO;
    private final ContextManager contextManager = new ContextManager(29);
    private final List<String> shellHistory = new ArrayList<>();
    private final long maxSessionDurationMillis;
    private final ExecutorService inputExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService replExecutor = Executors.newFixedThreadPool(2);
    static {
        LOGGER.setLevel(Level.FINE);
    }
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

    private CompletableFuture<Boolean> requestApprovalAsync(String command, Scanner scanner) {
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

    private CompletableFuture<String> runReplLoop(
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

        return aim.completeLocalRequest(prompt, null, modelSetting, "response")
            .thenCompose(response -> processResponseLoop(response, aim, transcript, scanner, modelSetting, startTimeMillis));
    }

    private CompletableFuture<String> processResponseLoop(
        MetadataContainer response,
        AIManager aim,
        StringBuilder transcript,
        Scanner scanner,
        String modelSetting,
        long startTimeMillis
    ) {
        List<String> shellCommands = response.get(ResponseObject.LOCALSHELLTOOL_COMMANDS);
        LOGGER.fine("Shell commands received: " + shellCommands);
        ResponseUtils ru = new ResponseUtils(response);
        String summary = ru.completeGetLocalShellToolSummary().join();
        if (summary != null && !summary.isBlank()) {
            System.out.println("\n[Model Summary]: " + summary + "\n");
            contextManager.addEntry(new ContextEntry(ContextEntry.Type.AI_RESPONSE, summary));
            LOGGER.fine("Model summary: " + summary);
        }

        return ru.completeGetShellToolFinished().thenCompose(finished -> {
            if (Boolean.TRUE.equals(finished)) {
                LOGGER.fine("AI indicated task is finished.");
                System.out.println("✅ Task complete.");
                System.out.println("\nFinal Summary:\n" + transcript.toString());
                return CompletableFuture.completedFuture(transcript.toString());
            }

           // List<String> shellCommands = r"tesponse.get(ResponseObject.LOCALSHELLTOOL_COMMANDS);
            if (shellCommands == null || shellCommands.isEmpty()) {
                LOGGER.warning("No shell commands received. Asking user for clarification.");
                String plainText = ru.completeGetOutput().join();
                System.out.println("[Model]: I need clarification before proceeding. " + plainText);
                System.out.print("> ");
                String userInput = scanner.nextLine();
                contextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, userInput));
                return runReplLoop(userInput, aim, transcript, scanner, modelSetting, startTimeMillis);
            }

            String element = shellCommands.get(0); // Assume only the first for now
            LOGGER.fine("Received shell command: " + element);
            String plainText = ru.completeGetOutput().join();

            if (element == null || element.isBlank()) {
                LOGGER.warning("Shell command is blank. Asking for clarification.");
                System.out.println("[Model]: I need clarification before proceeding. " + plainText);
                System.out.print("> ");
                String userInput = scanner.nextLine();
                contextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, userInput));
                return runReplLoop(userInput, aim, transcript, scanner, modelSetting, startTimeMillis);
            }

            long tokens = contextManager.getContextTokenCount();
            System.out.println("Current context token count: " + tokens);

            if (requiresApproval(element)) {
                return requestApprovalAsync(element, scanner).thenCompose(approved -> {
                    if (!approved) {
                        String rejectionMsg = "⛔ Command rejected by user.";
                        System.out.println(rejectionMsg);
                        transcript.append(rejectionMsg).append("\n");
                        LOGGER.warning("User rejected command: " + element);
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

        return executeCommandSequence(shellCommands, 0, toolHandler, response, aim, transcript, scanner, modelSetting, startTimeMillis);
    }

    private CompletableFuture<String> executeCommandSequence(
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
            // Continue REPL after all commands have been run
            String updatedPrompt = contextManager.buildPromptContext();
            return aim.completeLocalRequest(updatedPrompt, null, modelSetting, "response")
                    .thenCompose(nextResponse -> processResponseLoop(nextResponse, aim, transcript, scanner, modelSetting, startTimeMillis));
        }

        String shellCommand = commands.get(index);
        LOGGER.fine("Executing shell command: " + shellCommand);
        contextManager.addEntry(new ContextEntry(ContextEntry.Type.COMMAND, shellCommand));

        return toolHandler.executeShellCommandAsync(response, shellCommand) // Pass the single command
                .thenCompose(output -> {
                    transcript.append("> ").append(shellCommand).append("\n").append(output).append("\n");
                    System.out.println("> " + shellCommand);
                    contextManager.addEntry(new ContextEntry(ContextEntry.Type.SHELL_OUTPUT, output));
                    LOGGER.info("Shell command output: " + output);
                    // Recurse to next command
                    return executeCommandSequence(commands, index + 1, toolHandler, response, aim, transcript, scanner, modelSetting, startTimeMillis);
                });
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

    private CompletableFuture<String> completeREPLAsync(Scanner scanner, String initialMessage) {
        this.originalDirective = initialMessage;
        LOGGER.fine("Starting REPL session with: " + initialMessage);
        contextManager.addEntry(new ContextEntry(ContextEntry.Type.USER_MESSAGE, initialMessage));
        AIManager aim = new AIManager();
        StringBuilder transcript = new StringBuilder();
        String model = ModelRegistry.GEMINI_RESPONSE_MODEL.asString();
        return runReplLoop(initialMessage, aim, transcript, scanner, model, System.currentTimeMillis());
    }
}
