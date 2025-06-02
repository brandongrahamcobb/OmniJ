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

import java.util.function.Consumer;

public class EventListeners extends ListenerAdapter implements Cog {
    
    private final Map<Long, MetadataContainer> userResponseMap = new ConcurrentHashMap<>();
    private JDA api;
    private DiscordBot bot;
    final StringBuilder messageBuilder = new StringBuilder();
    
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
        return Vyrtuous.completeGetInstance()
            .thenCompose(vyr -> vyr.completeGetUserModelSettings())
            .thenCompose(userModelSettings -> {
                String setting = userModelSettings.getOrDefault(
                    senderId,
                    ModelRegistry.LOCAL_RESPONSE_MODEL.asString()
                );
                return aim.completeResolveModel(fullContent, multimodal, setting)
                    .thenCompose(model -> {
                        CompletableFuture<String> prevIdFut = previousResponse != null
                            ? new OpenAIUtils(previousResponse).completeGetResponseId()
                            : CompletableFuture.completedFuture(null);

                        return prevIdFut.thenCompose(previousResponseId -> {
                            // Step 1: send initial placeholder message to Discord
                            return message.getChannel().sendMessage("🤖 Generating response...").submit()
                                .thenCompose(sentMessage -> {

                                    StringBuilder messageBuilder = new StringBuilder();

                                    // Step 2: define streaming chunk handler
                                    Consumer<String> chunkHandler = chunk -> {
                                        messageBuilder.append(chunk);
                                        sentMessage.editMessage(messageBuilder.toString()).queue(); // stream updates
                                    };

                                    // Step 3: build and stream LLaMA response
                                    return aim.completeRequest(fullContent, previousResponseId, model, "completion", "llama", true, chunkHandler)
                                        .thenCompose(responseObject -> {
                                            // Save response object for later follow-ups
                                            userResponseMap.put(senderId, responseObject);

                                            // Chain set previous ID if needed
                                            if (previousResponse != null) {
                                                OpenAIUtils openaiUtilsPrev = new OpenAIUtils(previousResponse);
                                                return openaiUtilsPrev.completeGetPreviousResponseId()
                                                    .thenCompose(prevRespId ->
                                                        new OpenAIUtils(responseObject).completeSetPreviousResponseId(prevRespId));
                                            }

                                            return CompletableFuture.completedFuture(null);
                                        });
                                });
                        });
                    });
            });
    }

}
