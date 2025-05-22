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

public class REPLManager {
    
    private final Map<Long, ResponseObject> userResponseMap = new ConcurrentHashMap<>();
    
    private ApprovalMode approvalMode = ApprovalMode.FULL_AUTO;
    private final List<String> shellHistory = new ArrayList<>();

    public REPLManager(ApprovalMode mode) {
        setApprovalMode(mode);
    }
    
    public void setApprovalMode(ApprovalMode mode) {
        this.approvalMode = mode;
    }
    
    private String completeREPL(String initialMessage) {
        long senderId = 1L;
        AIManager aim = new AIManager();
        ResponseObject previousResponse = userResponseMap.get(senderId);
        boolean multimodal = false;
        String loopInput = initialMessage;
        StringBuilder fullTranscript = new StringBuilder();
        boolean stopLoop = false;

        while (!stopLoop) {
            try {
                String modelSetting = ModelRegistry.OPENAI_RESPONSE_MODEL.asString();
                String prevResponseId = previousResponse != null
                        ? previousResponse.completeGetResponseId().join()
                        : null;

                // Request next step based on last command/output
                ResponseObject response = aim
                        .completeToolRequest(loopInput, prevResponseId, modelSetting, "response")
                        .join();

                // Store this response as previous for the next loop
                previousResponse = response;
                userResponseMap.put(senderId, response);
                String command = response.get(ToolHandler.LOCALSHELLTOOL_COMMAND);
                if (requiresApproval(command)) {
                    System.out.println("🛑 Approval required for command: " + command);
                    System.out.print("Approve? (y = yes, e = edit, a = always auto): ");
                    String approval = new Scanner(System.in).nextLine().trim().toLowerCase();
                    if (approval.equals("e")) {
                        System.out.print("Edit command: ");
                        command = new Scanner(System.in).nextLine();
                        response.put(ToolHandler.LOCALSHELLTOOL_COMMAND, command);
                    } else if (approval.equals("a")) {
                        setApprovalMode(ApprovalMode.FULL_AUTO);
                    } else if (!approval.equals("y")) {
                        System.out.println("❌ Command not approved.");
                        break;
                    }
                }
                // Execute shell comman
                String shellResult = runAndRecordCommand(response);
                fullTranscript.append("🔁 Command:\n").append(loopInput)
                              .append("\n📤 Output:\n").append(shellResult).append("\n\n");

                // Exit condition? Model might tell us.
                String summary = summarizeShellSession();
                if (summary.toLowerCase().contains("exit") || summary.contains("🛑")) {
                    stopLoop = true;
                    fullTranscript.append("🧠 Summary of session:\n").append(summary);
                    break;
                }

                // Feed shell output as new input
                loopInput = summary + "\nPrevious output:\n" + shellResult;

            } catch (Exception e) {
                e.printStackTrace();
                return "❌ REPL loop error: " + e.getMessage();
            }
        }

        return fullTranscript.toString();
    }

    private boolean requiresApproval(String command) {
        List<String> dangerous = List.of("rm", "mv", "git", "patch", "shutdown", "reboot", "mvn compile");
        boolean isDangerous = dangerous.stream().anyMatch(command::contains);

        return switch (approvalMode) {
            case FULL_AUTO -> false;
            case EDIT_APPROVE_ALL -> true;
            case EDIT_APPROVE_DESTRUCTIVE -> isDangerous;
        };
    }

    private String runAndRecordCommand(ResponseObject responseObject) {
        ToolHandler th = new ToolHandler();
        String result = th.executeShellCommand(responseObject);
        String command = responseObject.get(ToolHandler.LOCALSHELLTOOL_COMMAND);
        shellHistory.add("> " + command + "\n" + result);
        return result;
    }
    
    private String summarizeShellSession() {
        if (shellHistory.isEmpty()) return "📝 No session activity to summarize.";

        String context = String.join("\n", shellHistory);
        AIManager aim = new AIManager();

        return aim.completeRequest(context, null, ModelRegistry.OPENAI_RESPONSE_MODEL.asString(), "response")
            .thenCompose(resp -> resp.completeGetOutput())
            .exceptionally(ex -> "⚠️ Error summarizing session: " + ex.getMessage())
            .join();
    }
    
    public void startResponseInputThread() {
            Thread inputThread = new Thread(() -> {
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
                        String response = completeREPL(input);
                        System.out.println("Bot: " + response);
                    }
                } catch (IllegalStateException e) {
                    System.out.println("System.in is unavailable.");
                }
            });
            inputThread.setName("ResponseInputThread");
            inputThread.setDaemon(false); // Important: keep it non-daemon so it doesn't exit immediately
            inputThread.start();
        }
}
