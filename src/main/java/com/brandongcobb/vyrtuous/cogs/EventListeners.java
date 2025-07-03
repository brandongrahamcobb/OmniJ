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

import com.brandongcobb.metadata.MetadataContainer;
import com.brandongcobb.vyrtuous.component.bot.DiscordBot;
import com.brandongcobb.vyrtuous.objects.*;
import com.brandongcobb.vyrtuous.service.*;
import com.brandongcobb.vyrtuous.utils.handlers.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EventListeners extends ListenerAdapter implements Cog {

    public static AIService ais = new AIService();
    private JDA api;
    private DiscordBot bot;
    private final Map<Long, MetadataContainer> genericUserResponseMap = new ConcurrentHashMap<>();
    private final Map<Long, List<String>> genericHistoryMap = new ConcurrentHashMap<>();
    private MessageService mess = new MessageService(api);
    
    @Override
    public void register(JDA api, DiscordBot bot) {
        this.bot = bot.completeGetBot();
        this.api = api;
        api.addEventListener(this);
        api.addEventListener(new ListenerAdapter() {
            @Override
            public void onReady(ReadyEvent event) {
                System.out.println("I've always wanted to do this.");
            }
        });
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();
        if (message.getAuthor().isBot() || message.getContentRaw().startsWith((String) System.getenv("DISCORD_COMMAND_PREFIX"))) {
            return;
        }
        if (message.getReferencedMessage() != null) {
            if (!message.getReferencedMessage().getAuthor().getId().equals(event.getJDA().getSelfUser().getId())) {
                return;
            }
        }
        else if (!message.getContentRaw().contains("<@1318597210119864385>")) {
            return;
        }
        long senderId = event.getAuthor().getIdLong();
        List<Attachment> attachments = message.getAttachments();
        MetadataContainer previousResponse = genericUserResponseMap.get(senderId);
        final boolean[] multimodal = new boolean[] { false };
        CompletableFuture<String> contentFuture = (attachments != null && !attachments.isEmpty())
            ? mess.completeProcessAttachments(attachments).thenApply(list -> {
                multimodal[0] = true;
                return String.join("\n", list) + "\n" + message.getContentDisplay().replace("@Vyrtuous ", "");
            })
            : CompletableFuture.completedFuture(message.getContentDisplay());
        contentFuture
            .thenCompose(prompt -> completeCreateServerRequest(prompt,  senderId, multimodal[0], Integer.valueOf(System.getenv("DISCORD_CONTEXT_LENGTH")), previousResponse))
            .thenCompose(serverRequest -> {
                try {
                    return ais.completeRequest(
                        serverRequest.instructions,
                        serverRequest.prompt,
                        serverRequest.previousResponseId,
                        serverRequest.model,
                        serverRequest.requestType,
                        serverRequest.endpoint,
                        serverRequest.stream,
                        (Consumer<String>) null,
                        serverRequest.provider
                    ).thenCompose(moderationContainer -> {
                        CompletableFuture<Boolean> flaggedFuture = switch (moderationContainer) {
                            case OpenAIContainer o -> new OpenAIUtils(o).completeGetFlagged();
                            case LlamaContainer l -> new LlamaUtils(l).completeGetFlagged();
                            case LMStudioContainer lm -> new LMStudioUtils(lm).completeGetFlagged();
                            case OllamaContainer ol -> new OllamaUtils(ol).completeGetFlagged();
                            case OpenRouterContainer or -> new OpenRouterUtils(or).completeGetFlagged();
                            default -> CompletableFuture.completedFuture(false);
                        };
                        return flaggedFuture.thenCompose(flagged -> {
                            if (flagged) {
                                ModerationService mos = new ModerationService(api);
                                return mos.completeHandleModeration(message, "Flagged for moderation").thenApply(ignored -> null);
                            }
                            if (serverRequest.stream) {
                                return handleStreamedResponse(message, senderId, previousResponse, serverRequest);
                            } else {
                                return handleNonStreamedResponse(message, senderId, previousResponse, serverRequest);
                            }
                        });
                    })
                    .exceptionally(ex -> {
                        ex.printStackTrace();
                        return null;
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            })
            .exceptionally(ex -> {
                ex.printStackTrace();
                return null;
            });
    }
    
    private CompletableFuture<ServerRequest> completeCreateServerRequest(String prompt, long senderId, boolean multimodal, int historySize, MetadataContainer previousResponse) {
        return SettingsService.completeGetSettingsInstance()
                .thenCompose(settingsManager -> settingsManager.completeGetUserSettings(senderId)
                    .thenCompose(userSettings -> {
                        String userModel = System.getenv("DISCORD_MODEL");
                        String provider = System.getenv("DISCORD_PROVIDER");
                        String requestType = System.getenv("DISCORD_REQUEST_TYPE");
                        return ais.completeGetAIEndpoint(multimodal, provider, "discord", requestType)
                            .thenCombine(ais.completeGetInstructions(multimodal, provider, "discord"),
                                (endpoint, instructions) -> new Object[] { endpoint, instructions })
                            .thenCompose(data -> {
                                String endpoint = (String) data[0];
                                String instructions = (String) data[1];
                                if ("openai".equals(provider) && previousResponse instanceof OpenAIContainer openai) {
                                    return new OpenAIUtils(openai)
                                        .completeGetResponseId()
                                        .thenApply(previousId -> new ServerRequest(
                                            instructions,
                                            prompt,
                                            userModel,
                                            Boolean.parseBoolean(System.getenv("DISCORD_STREAM")),
                                            Boolean.parseBoolean(System.getenv("DISCORD_STORE")),
                                            null,
                                            endpoint,
                                            previousId,
                                            provider,
                                            requestType
                                        ));
                                } else {
                                    List<String> history = genericHistoryMap.computeIfAbsent(senderId, k -> new ArrayList<>());
                                    trimHistory(history, historySize);
                                    String fullPrompt = buildFullPrompt(history, prompt);
                                    return CompletableFuture.completedFuture(new ServerRequest(
                                        instructions,
                                        fullPrompt,
                                        userModel,
                                        Boolean.parseBoolean(System.getenv("DISCORD_STREAM")),
                                        Boolean.parseBoolean(System.getenv("DISCORD_STORE")),
                                        history,
                                        endpoint,
                                        null,
                                        provider,
                                        requestType
                                    ));
                                }
                            });
                    }));
        }

    private CompletableFuture<Void> handleStreamedResponse(Message originalMessage, long senderId, MetadataContainer previousResponse, ServerRequest serverRequest) {
        return originalMessage.getChannel().sendMessage("Hi I'm Vyrtuous...").submit()
            .thenCompose(sentMessage -> {
                BlockingQueue<String> queue = new LinkedBlockingQueue<>();
                Supplier<Optional<String>> nextChunkSupplier = () -> {
                    try {
                        String chunk = queue.take();
                        return "<<END>>".equals(chunk) ? Optional.empty() : Optional.of(chunk);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return Optional.empty();
                    }
                };
                try {
                    CompletableFuture<MetadataContainer> responseFuture = ais.completeRequest(
                        serverRequest.instructions,
                        serverRequest.prompt,
                        serverRequest.previousResponseId,
                        serverRequest.model,
                        serverRequest.requestType,
                        serverRequest.endpoint,
                        serverRequest.stream,
                        queue::offer,
                        System.getenv("DISCORD_PROVIDER")
                    );
                    CompletableFuture<Void> streamFuture = mess.completeStreamResponse(sentMessage, nextChunkSupplier);
                    return CompletableFuture.allOf(responseFuture, streamFuture)
                        .thenCompose(v -> responseFuture)
                        .thenCompose(responseObject -> {
                            genericUserResponseMap.put(senderId, responseObject);
                            String newResponseId = serverRequest.previousResponseId;
                            if (responseObject instanceof OpenAIContainer openai) {
                                return new OpenAIUtils(openai).completeSetPreviousResponseId(newResponseId);
                            } else if (responseObject instanceof LlamaContainer llama) {
                                return new LlamaUtils(llama).completeSetPreviousResponseId(newResponseId);
                            } else if (responseObject instanceof LMStudioContainer lmstudio) {
                                return new LMStudioUtils(lmstudio).completeSetPreviousResponseId(newResponseId);
                            } else if (responseObject instanceof OllamaContainer ollama) {
                                return new OllamaUtils(ollama).completeSetPreviousResponseId(newResponseId);
                            } else if (responseObject instanceof OpenRouterContainer router) {
                                return new OpenRouterUtils(router).completeSetPreviousResponseId(newResponseId);
                            }
                            return CompletableFuture.completedFuture(null); // fallback
                        });
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            });
    }

    
    private CompletableFuture<Void> handleNonStreamedResponse(Message message, long senderId, MetadataContainer previousResponse, ServerRequest serverRequest) {
        try {
            return ais.completeRequest(
                serverRequest.instructions,
                serverRequest.prompt,
                serverRequest.previousResponseId,
                serverRequest.model,
                serverRequest.requestType,
                serverRequest.endpoint,
                serverRequest.stream,
                null,
                System.getenv("DISCORD_PROVIDER")
            ).thenCompose(responseObject -> {
                genericUserResponseMap.put(senderId, responseObject);
                CompletableFuture<String> contentFuture;
                if (responseObject instanceof OpenAIContainer openai) {
                    contentFuture = new OpenAIUtils(openai).completeGetContent();
                } else if (responseObject instanceof LlamaContainer llama) {
                    contentFuture = new LlamaUtils(llama).completeGetContent();
                } else if (responseObject instanceof LMStudioContainer lmstudio) {
                    contentFuture = new LMStudioUtils(lmstudio).completeGetContent();
                } else if (responseObject instanceof OllamaContainer ollama) {
                    contentFuture = new OllamaUtils(ollama).completeGetContent();
                } else if (responseObject instanceof OpenRouterContainer router) {
                    contentFuture = new OpenRouterUtils(router).completeGetContent();
                } else {
                    contentFuture = CompletableFuture.completedFuture("Unknown response type.");
                }
                return contentFuture.thenCompose(content ->
                    mess.completeSendResponse(message, content.strip())
                );
            })
            .exceptionally(ex -> {
                ex.printStackTrace();
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String buildFullPrompt(List<String> history, String prompt) {
        return history.isEmpty() ? prompt : String.join("\n", history) + "\n\n" + prompt;
    }

    private void trimHistory(List<String> history, int maxSize) {
        if (history.size() > maxSize) {
            int trimSize = Math.max(1, maxSize / 20); // always trim at least one
            history.subList(0, history.size() - trimSize).clear();
        }
    }

    private void updateHistory(List<String> history, String prompt, String response) {
        history.add("User: " + prompt.strip());
        history.add("Bot: " + response.strip());
    }
}
            
