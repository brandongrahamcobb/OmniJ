//        private CompletableFuture<String> processLoop(
            String initialMessage,
            String input,
            AIManager aim,
            StringBuilder fullTranscript,
            Scanner scanner,
            String modelSetting
    ) {
        return aim.completeLocalRequest(input, null, modelSetting, "completion")
            .thenCompose(response -> {
                System.out.println("[DEBUG] Received AI response");
                
                // Get AI output
                String aiText = response.get("content");
                if (aiText != null) {
                    fullTranscript.append("AI: ").append(aiText).append("\n");
                }

                // Check if shell command is finished
                return response.completeGetShellToolFinished()
                    .thenCompose(isFinished -> {
                        System.out.println("[DEBUG] Shell finished flag: " + isFinished);
                        if (Boolean.TRUE.equals(isFinished)) {
                            System.out.println("[DEBUG] REPL finished, returning transcript");
                            return CompletableFuture.completedFuture(fullTranscript.toString());
                        }

                        // Get shell command
                        return response.completeGetShellToolCommand()
                            .thenCompose(shellCommand -> {
                                if (shellCommand == null || shellCommand.trim().isEmpty()) {
                                    System.out.println("[DEBUG] No shell command, continuing transcript");
                                    return CompletableFuture.completedFuture(fullTranscript.toString());
                                }

                                System.out.println("[DEBUG] Shell command: " + shellCommand);
                                fullTranscript.append("\n> ").append(shellCommand).append("\n");

                                // Execute shell command
                                ToolHandler toolHandler = new ToolHandler();
                                return toolHandler.executeShellCommandAsync(response)
                                    .thenCompose(output -> {
                                        if (output != null && !output.trim().isEmpty()) {
                                            System.out.println("[DEBUG] Shell command output: " + output);
                                            fullTranscript.append("Command output:\n").append(output).append("\n");
                                        } else {
                                            fullTranscript.append("Command executed with no output\n");
                                        }

                                        // Recursive call with updated context
                                        String context = fullTranscript.toString();
                                        System.out.println("[DEBUG] Recursing with updated context...");
                                        return processLoop(initialMessage, context, aim, fullTranscript, scanner, modelSetting);
                                    })
                                    .exceptionally(throwable -> {
                                        String errorMessage = "⚠️ Shell command failed: " + throwable.getMessage();
                                        System.err.println(errorMessage);
                                        fullTranscript.append(errorMessage).append("\n");
                                        return processLoop(initialMessage, fullTranscript.toString(), aim, fullTranscript, scanner, modelSetting);
                                    });
                            });
                    });
            })
            .exceptionally(throwable -> {
                String errorMessage = "⚠️ AI request failed: " + throwable.getMessage();
                System.err.println(errorMessage);
                fullTranscript.append(errorMessage).append("\n");
                return CompletableFuture.completedFuture(fullTranscript.toString());
            });
    }
/*
 * Vyrtuous.java
 * The primary purpose of this class is to integrate Discord, LinkedIn, OpenAI, Patreon, Twitch, and many more into one hub.
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.brandongcobb.vyrtuous.utils.handlers;

import com.brandongcobb.vyrtuous.Vyrtuous;
import com.brandongcobb.vyrtuous.bots.DiscordBot;
import com.brandongcobb.metadata.MetadataContainer;
import com.brandongcobb.vyrtuous.utils.inc.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.dv8tion.jda.api.JDA;

/**
 * Manages a REPL loop integrating AI calls, shell command execution, and user approval.
 */
sys
