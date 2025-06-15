/* EventListeners.java The purpose of this program is to listen for Discord
 * events and handle them.
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
package com.brandongcobb.vyrtuous.cogs;

import com.brandongcobb.vyrtuous.Vyrtuous;
import com.brandongcobb.vyrtuous.bots.DiscordBot;
import com.brandongcobb.vyrtuous.utils.handlers.*;
import com.brandongcobb.vyrtuous.utils.inc.*;
import com.brandongcobb.metadata.*;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Optional;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.session.ReadyEvent;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.HashMap;
import java.util.function.Supplier;

import java.util.function.Consumer;

public class EventListeners extends ListenerAdapter implements Cog {

    private final Map<Long, MetadataContainer> userResponseMap = new ConcurrentHashMap<>();
    private JDA api;
    private DiscordBot bot;
    final StringBuilder messageBuilder = new StringBuilder();
    private final String discordResponseSource = System.getenv("DISCORD_RESPONSE_SOURCE");

    @Override
    public void register(JDA api, DiscordBot bot) {
        this.bot = bot.completeGetBot();
        this.api = api;
        api.addEventListener(this);
        api.addEventListener(new ListenerAdapter() {
            @Override
            public void onReady(ReadyEvent event) {
                System.out.println("Bot is ready!");

                // Run your scheduled task here
                startScheduledTask();
            }
        });
    }

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final long targetChannelId = 1383632703475421237L; // replace with your actual channel ID


//    @Override
//    public void onMessageReceived(MessageReceivedEvent event) {
//        Message message = event.getMessage();
//        String messageContent = message.getContentDisplay();
//        if (message.getAuthor().isBot() || messageContent.startsWith(".")) return;
//        AIManager aim = new AIManager();
//        MessageManager mem = new MessageManager(api);
//        User sender = event.getAuthor();
//        long senderId = sender.getIdLong();
//        List<Attachment> attachments = message.getAttachments();
//        MetadataContainer previousResponse = userResponseMap.get(senderId);
//        final boolean[] multimodal = new boolean[]{false};
//        String content = messageContent.replace("@Vyrtuous", "");
//        CompletableFuture<String> fullContentFuture;
//        if (attachments != null && !attachments.isEmpty()) {
//            fullContentFuture = mem.completeProcessAttachments(attachments)
//                .thenApply(attachmentContentList -> {
//                    multimodal[0] = true;
//                    return String.join("\n", attachmentContentList) + "\n" + content;
//                });
//        } else {
//            fullContentFuture = CompletableFuture.completedFuture(content);
//        }
//        fullContentFuture
//            .thenCompose(fullContent -> {
//                if (true) { // TODO: moderation logic here later
//                    return handleNormalFlow(aim, mem, senderId, previousResponse, message, fullContent, multimodal[0]);
//                }
//                try {
//                    return aim.completeRequest(fullContent, null, ModelRegistry.OPENAI_MODERATION_MODEL.asString(), "placeholder", false, null)
//                        .thenCompose(moderationOpenAIContainer -> {
//                            OpenAIUtils utils = new OpenAIUtils(moderationOpenAIContainer);
//                            return utils.completeGetFlagged()
//                                .thenCompose(flagged -> {
//                                    if (flagged) {
//                                        ModerationManager mom = new ModerationManager(api);
//                                        return utils.completeGetFormatFlaggedReasons()
//                                            .thenCompose(reason -> mom.completeHandleModeration(message, reason)
//                                                .thenApply(ignored -> null));
//                                    } else {
//                                        return handleNormalFlow(aim, mem, senderId, previousResponse, message, fullContent, multimodal[0]);
//                                    }
//                                });
//                        });
//                } catch (Exception e) {
//                    return CompletableFuture.failedFuture(e);
//                }
//            })
//            .exceptionally(ex -> {
//                ex.printStackTrace();
//                return null;
//            });
//
//    }
    private CompletableFuture<Void> handleChainedResponse(
        long senderId,
        MetadataContainer previousResponse,
        MetadataContainer responseObject
    ) {
        userResponseMap.put(senderId, responseObject);

        if (previousResponse == null) {
            return CompletableFuture.completedFuture(null);
        }

        if (previousResponse instanceof OpenAIContainer) {
            return new OpenAIUtils(previousResponse).completeGetPreviousResponseId()
                .thenCompose(prevId ->
                    new OpenAIUtils(responseObject).completeSetPreviousResponseId(prevId)
                );
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }


    private CompletableFuture<Void> handleNormalFlow(
        AIManager aim,
        MessageManager mem,
        long senderId,
        MetadataContainer previousResponse,
        Message messageToUpdate,
        String fullContent,
        boolean multimodal
    ) {
        return SettingsManager.completeGetSettingsInstance()
            .thenCompose(settingsManager -> settingsManager.completeGetUserSettings(senderId)
                .thenCompose(userSettings -> {
                    String userModel = userSettings[0];
                    String source = userSettings[1];

                    return aim.getAIEndpointWithState(multimodal, source, "discord", "completions")
                        .thenCompose(endpoint -> {
                            CompletableFuture<String> prevIdFut;

                            switch (source) {
                                case "openai":
                                    prevIdFut = previousResponse != null
                                        ? new OpenAIUtils(previousResponse).completeGetResponseId()
                                        : CompletableFuture.completedFuture(null);
                                    break;
                                default:
                                    prevIdFut = CompletableFuture.completedFuture(null);
                                    break;
                            }

                            return prevIdFut.thenCompose(previousResponseId -> {
                                try {
                                    return aim.completeRequest(
                                            fullContent,
                                            previousResponseId,
                                            userModel,
                                            endpoint,
                                            false,
                                            null // no streaming consumer
                                        )
                                        .thenCompose(responseObject -> {
                                            try {
                                                LlamaUtils lu = new LlamaUtils(responseObject);
                                                String content = lu.completeGetContent().join();
                                                return mem.completeSendResponse(messageToUpdate, content);
                                            } catch (Exception e) {
                                                CompletableFuture<Void> failed = new CompletableFuture<>();
                                                failed.completeExceptionally(e);
                                                return failed;
                                            }
                                        })
                                        .exceptionally(ex -> {
                                            ex.printStackTrace();
                                            return null;
                                        });
                                } catch (Exception e) {
                                    CompletableFuture<Void> failed = new CompletableFuture<>();
                                    failed.completeExceptionally(e);
                                    return failed;
                                }
                            });
                        });
                }));
    }
    private volatile Message scheduledMessage = null;

    public void startScheduledTask() {
        scheduler.scheduleAtFixedRate(() -> {
            if (api == null) return;
            long guildId = 1347284827350630591L;
            Guild guild = api.getGuildById(guildId);
            if (guild == null) {
                System.err.println("Guild not found");
                return;
            }
            System.out.println(targetChannelId);
            var channel = api.getTextChannelById(targetChannelId);
            if (channel == null) {
                System.err.println("Channel not found");
                return;
            }

            if (scheduledMessage == null) {
                // Send the dummy message first time only
                channel.sendMessage("Generating...").queue(sentMessage -> {
                    scheduledMessage = sentMessage;
                    runHandleNormalFlowUpdate(sentMessage);
                }, error -> {
                    System.err.println("Failed to send dummy message: " + error.getMessage());
                });
            } else {
                // Edit the existing message
                runHandleNormalFlowUpdate(scheduledMessage);
            }
        }, 0, 3, TimeUnit.MINUTES);
    }

    private void runHandleNormalFlowUpdate(Message messageToUpdate) {
        AIManager aim = new AIManager();
        MessageManager mem = new MessageManager(api);
        MetadataContainer previousResponse = userResponseMap.get(0L); // Or wherever you store this state
        String content = """
            You are a routine Discord agent who can send commands to draw molecular images every 5 minutes.
            The format is simple:
            The first parameter is (without the quotes) "!d".
            Delimited by a space, follows 3 options, "2", "glow", "gsrs". These would then be "!d 2", "!d glow", and "!d gsrs".
            Delimited by another space, follows 3 ways of representing molecules.
            A. common molecules names (if multi words then they are in quotes).
            B. peptide sequences (AKTP...).
            Delimited by a period, multiple molecules can be drawn in a single image "!d glow ketamine.aspirin.PKQ".
            These are followed by a space and a title in quotes, becoming: "!d glow ketamine.aspirin.PKQ "Figure 1. Title here""
            The unique case is with option "2" where only two molecules (ketamine.aspirin) can be provided at a given time because
            it compares them on a single image.
            You must only respond with a random molecule or multiple molecules for every request including the full command syntax ("!d <option> <molecule1>.<molecule2> "Title").
            Title is a string encapsulated by quotes with a space between it and the molecule(s).
            You MUST use this syntax.
        """;
        boolean multimodal = false;

        handleNormalFlow(aim, mem, 0L, previousResponse, messageToUpdate, content, multimodal)
            .exceptionally(ex -> {
                ex.printStackTrace();
                return null;
            });
    }
}
