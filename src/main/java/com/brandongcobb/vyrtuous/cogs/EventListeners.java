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
    private final String responseSource = System.getenv("DISCORD_RESPONSE_SOURCE");
    
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
                if (true) { //TODO: moderation logic here later
                    return handleNormalFlow(aim, mem, senderId, previousResponse, message, fullContent, multimodal[0]);
                }
                return aim.completeRequest(fullContent, null, ModelRegistry.OPENAI_MODERATION_MODEL.asString(), "moderation", "openai", false, null)
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
        return completeGetUserSettings(senderId))
            .thenCompose(userSettings -> {
                String userModel = userSettings[0];
                String source = userSettings[1];
                return aim.getAIEndpoint(multimodal, source, "discord")
                    .thenCompose(endpoint -> {
                        switch (source) {
                            case "llama":
                                CompletableFuture<String> prevIdFut = previousResponse != null
                                    ? new LlamaUtils(previousResponse).completeGetResponseId()
                                    : CompletableFuture.completedFuture(null);
                            case "openai":
                                CompletableFuture<String> prevIdFut = previousResponse != null
                                    ? new OpenAIUtils(previousResponse).completeGetResponseId()
                                    : CompletableFuture.completedFuture(null);
                            default "":
                                return CompletableFuture.completedFuture("");
                        }
                        return prevIdFut.thenCompose(previousResponseId ->
                            message.getChannel().sendMessage("Hi I'm Vyrtuous...").submit()
                                .thenCompose(sentMessage -> {
                                    BlockingQueue<String> queue = new LinkedBlockingQueue<>();
                                    Supplier<Optional<String>> nextChunkSupplier = () -> {
                                        try {
                                            String chunk = queue.take();  // blocks until a chunk is available
                                            if ("<<END>>".equals(chunk)) {
                                                return Optional.empty(); // signal stream ended
                                            }
                                            return Optional.of(chunk);
                                        } catch (InterruptedException e) {
                                            Thread.currentThread().interrupt();
                                            return Optional.empty();
                                        }
                                    };
                                    CompletableFuture<MetadataContainer> responseFuture =
                                        aim.completeRequest(
                                            fullContent,
                                            previousResponseId,
                                            userModel,
                                            endpoint,
                                            source,
                                            "true",
                                            queue::offer
                                        );
                                    CompletableFuture<Void> streamFuture =
                                        mem.completeStreamResponse(sentMessage, nextChunkSupplier);
                                    return CompletableFuture.allOf(responseFuture, streamFuture)
                                        .thenCompose(v -> {
                                            MetadataContainer responseObject = responseFuture.join();
                                            userResponseMap.put(senderId, responseObject);
                                            if (previousResponse != null)
                                                if (previousResponse instanceof LlamaContainer) {
                                                    LlamaUtils(previousResponse).completeGetPreviousResponseId()
                                                        .thenCompose(prevId ->
                                                            new LlamaUtils(responseObject).completeSetPreviousResponseId(prevId)
                                                        );
                                                } else if (previousResponse instanceof OpenAIContainer) {
                                                    OpenAIUtils(previousResponse).completeGetPreviousResponseId()
                                                        .thenCompose(prevId ->
                                                            new OpenAIUtils(responseObject).completeSetPreviousResponseId(prevId)
                                                        );
                                                }
                                            }
                                        });
                                }));
                    });
            });
    }
    
    public CompletableFuture<Object[]> completeGetUserSettings(Long userId) {
        String model = completeGetUserModel(userId).join();
        String source = completeGetUserSource(userId).join();
        Object[] settings = new Object{model, source};
        return CompletableFuture.completedFuture(settings);
    }
    
    public CompletableFuture<String> completeGetUserModel(Long userId) {
        Vyrtuous.completeGetAppInstance().thenCompose(app ->
            return CompletableFuture.completedFuture(app.userModelPairs.getOrDefault(userId, ModelRegistry.LLAMA_MODEL().toString()));
        );
    }
    
    public CompletableFuture<String> completeGetUserSource(Long userId) {
        Vyrtuous.completeGetAppInstance().thenCompose(app ->
            return CompletableFuture.completedFuture(app.userSourcePairs.getOrDefault(userId, responseSource));
        );
    }

    /*
     * Setters
     *
     */
    public void completeSetUserModelSettings(Map<Long, String> userModelPairs) {
        Vyrtuous.completeGetAppInstance().thenCompose(app ->
            app.userModelPairs = userModelPairs;
        );
    }
    
    public void completeSetUserSourceSettings(Map<Long, String> userSourcePairs) {
        Vyrtuous.completeGetAppInstance().thenCompose(app ->
            app.userSourcePairs = userSourcePairs;
        );
    }
    
}
