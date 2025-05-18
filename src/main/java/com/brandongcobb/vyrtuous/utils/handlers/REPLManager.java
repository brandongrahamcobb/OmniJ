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
import com.brandongcobb.vyrtuous.metadata.MetadataContainer;
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

    private String completeREPL(String message) {
        if (message.startsWith(".")) {
            return null;
        }
        AIManager aim = new AIManager();
        long senderId = 1L;
        ResponseObject previousResponse = userResponseMap.get(senderId);
        boolean multimodal = false;
        try {
            CompletableFuture<String> resultFuture = Vyrtuous
                .completeGetInstance()
                .thenCompose(vyr -> vyr.completeGetUserModelSettings())
                .thenCompose(userModelSettings -> {
                    String modelSetting = userModelSettings
                        .getOrDefault(senderId,
                                     ModelRegistry.OPENAI_RESPONSE_MODEL.asString());
                    return aim.completeResolveModel(message, multimodal, modelSetting)
                        .thenCompose(model -> {
                            CompletableFuture<String> prevIdFut = previousResponse != null
                                ? previousResponse.completeGetResponseId()
                                : CompletableFuture.completedFuture(null);
                            return prevIdFut.thenCompose(prevId ->
                                aim.completeRequest(message, prevId, model, "response")
                                    .thenCompose(responseObject -> {
                                        CompletableFuture<Void> setPrevFut;
                                        if (previousResponse != null) {
                                            setPrevFut = previousResponse
                                                .completeGetPreviousResponseId()
                                                .thenCompose(prevRespId ->
                                                    responseObject.completeSetPreviousResponseId(prevRespId)
                                                );
                                        } else {
                                            setPrevFut = responseObject.completeSetPreviousResponseId(null);
                                        }
                                        return setPrevFut.thenCompose(v -> {
                                            userResponseMap.put(senderId, responseObject);
                                            return responseObject.completeGetOutput();
                                        });
                                    })
                            );
                        });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return "Error during completion: " + ex.getMessage();
                });
            return resultFuture.join();
        } catch (CompletionException ce) {
            ce.printStackTrace();
            return "Unhandled exception: " + ce.getCause().getMessage();
        }
    }
}
