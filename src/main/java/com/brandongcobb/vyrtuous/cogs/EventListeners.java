/*
 * EventListeners.java
 * The purpose of this program is to listen for any of the program's endpoints and handle them.
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
    }
    
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();
        String messageContent = message.getContentDisplay();
        if (message.getAuthor().isBot() || messageContent.startsWith(".")) return;
        AIManager aim = new AIManager();
        MessageManager mem = new MessageManager(api);
        User sender = event.getAuthor();
        long senderId = sender.getIdLong();
        List<Attachment> attachments = message.getAttachments();
        MetadataContainer previousResponse = userResponseMap.get(senderId);
        final boolean[] multimodal = new boolean[]{false};
        String content = messageContent.replace("@Vyrtuous", "");
        CompletableFuture<String> fullContentFuture;
        if (attachments != null && !attachments.isEmpty()) {
            fullContentFuture = mem.completeProcessAttachments(attachments)
                .thenApply(attachmentContentList -> {
                    multimodal[0] = true;
                    return String.join("\n", attachmentContentList) + "\n" + content;
                });
        } else {
            fullContentFuture = CompletableFuture.completedFuture(content);
        }
        fullContentFuture
            .thenCompose(fullContent -> {
                if (true) { // TODO: moderation logic here later
                    return handleNormalFlow(aim, mem, senderId, previousResponse, message, fullContent, multimodal[0]);
                }
                try {
                    return aim.completeRequest(fullContent, null, ModelRegistry.OPENAI_MODERATION_MODEL.asString(), "placeholder", false, null)
                        .thenCompose(moderationOpenAIContainer -> {
                            OpenAIUtils utils = new OpenAIUtils(moderationOpenAIContainer);
                            return utils.completeGetFlagged()
                                .thenCompose(flagged -> {
                                    if (flagged) {
                                        ModerationManager mom = new ModerationManager(api);
                                        return utils.completeGetFormatFlaggedReasons()
                                            .thenCompose(reason -> mom.completeHandleModeration(message, reason)
                                                .thenApply(ignored -> null));
                                    } else {
                                        return handleNormalFlow(aim, mem, senderId, previousResponse, message, fullContent, multimodal[0]);
                                    }
                                });
                        });
                } catch (Exception e) {
                    return CompletableFuture.failedFuture(e);
                }
        })
        .exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });

    }
    
    private CompletableFuture<Void> handleNormalFlow(
        AIManager aim,
        MessageManager mem,
        long senderId,
        MetadataContainer previousResponse,
        Message message,
        String fullContent,
        boolean multimodal
    ) {
        return SettingsManager.completeGetSettingsInstance()
            .thenCompose(settingsManager -> settingsManager.completeGetUserSettings(senderId)
                .thenCompose(userSettings -> {
                    String userModel = userSettings[0];
                    String source = userSettings[1];

                    return aim.getAIEndpointWithState(multimodal, source, "discord", "completion") // TODO: make the requestType a setting.
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

                            return prevIdFut.thenCompose(previousResponseId ->
                                message.getChannel().sendMessage("Hi I'm Vyrtuous...").submit()
                                    .thenCompose(sentMessage -> {
                                        BlockingQueue<String> queue = new LinkedBlockingQueue<>();
                                        Supplier<Optional<String>> nextChunkSupplier = () -> {
                                            try {
                                                String chunk = queue.take();
                                                if ("<<END>>".equals(chunk)) return Optional.empty();
                                                return Optional.of(chunk);
                                            } catch (InterruptedException e) {
                                                Thread.currentThread().interrupt();
                                                return Optional.empty();
                                            }
                                        };

                                        final CompletableFuture<MetadataContainer> responseFuture;
                                        try {
                                            responseFuture = aim.completeRequest(
                                                fullContent,
                                                previousResponseId,
                                                userModel,
                                                endpoint,
                                                true,
                                                queue::offer
                                            );
                                        } catch (Exception e) {
                                            return CompletableFuture.failedFuture(e);
                                        }

                                        CompletableFuture<Void> streamFuture =
                                            mem.completeStreamResponse(sentMessage, nextChunkSupplier);

                                        return CompletableFuture.allOf(responseFuture, streamFuture)
                                            .thenCompose(v -> responseFuture)
                                            .thenCompose(responseObject -> {
                                                userResponseMap.put(senderId, responseObject);

                                                if (previousResponse == null) return CompletableFuture.completedFuture(null);

                                                if (previousResponse instanceof OpenAIContainer) {
                                                    return new OpenAIUtils(previousResponse).completeGetPreviousResponseId()
                                                        .thenCompose(prevId ->
                                                            new OpenAIUtils(responseObject).completeSetPreviousResponseId(prevId)
                                                        );
                                                } else {
                                                    return CompletableFuture.completedFuture(null);
                                                }
                                            });
                                    }));
                        });
                }));
    }


}
