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

import com.brandongcobb.metadata.*;
import com.brandongcobb.vyrtuous.Vyrtuous;
import com.brandongcobb.vyrtuous.bots.DiscordBot;
import com.brandongcobb.vyrtuous.objects.*;
import com.brandongcobb.vyrtuous.utils.handlers.*;
import com.brandongcobb.vyrtuous.utils.inc.*;
import java.util.concurrent.CompletableFuture;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.function.Consumer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class EventListeners extends ListenerAdapter implements Cog {

    public static AIManager aim = new AIManager();
    private JDA api;
    private DiscordBot bot;
    private final Map<Long, MetadataContainer> genericUserResponseMap = new ConcurrentHashMap<>();
    private final Map<Long, List<String>> genericHistoryMap = new ConcurrentHashMap<>();
    private MessageManager mem = new MessageManager(api);
    
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
            ? mem.completeProcessAttachments(attachments).thenApply(list -> {
                multimodal[0] = true;
                return String.join("\n", list) + "\n" + message.getContentDisplay().replace("@Vyrtuous ", "");
            })
            : CompletableFuture.completedFuture(message.getContentDisplay());
        contentFuture
            .thenCompose(prompt -> completeCreateServerRequest(prompt,  senderId, multimodal[0], Integer.valueOf(System.getenv("DISCORD_CONTEXT_LENGTH")), previousResponse))
            .thenCompose(serverRequest -> {
                try {
                    return aim.completeRequest(
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
                                ModerationManager mom = new ModerationManager(api);
                                return mom.completeHandleModeration(message, "Flagged for moderation").thenApply(ignored -> null);
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
        return SettingsManager.completeGetSettingsInstance()
                .thenCompose(settingsManager -> settingsManager.completeGetUserSettings(senderId)
                    .thenCompose(userSettings -> {
                        String userModel = System.getenv("DISCORD_MODEL");
                        String provider = System.getenv("DISCORD_PROVIDER");
                        String requestType = System.getenv("DISCORD_REQUEST_TYPE");
                        return aim.completeGetAIEndpoint(multimodal, provider, "discord", requestType)
                            .thenCombine(aim.completeGetInstructions(multimodal, provider, "discord"),
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
                    CompletableFuture<MetadataContainer> responseFuture = aim.completeRequest(
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
                    CompletableFuture<Void> streamFuture = mem.completeStreamResponse(sentMessage, nextChunkSupplier);
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
            return aim.completeRequest(
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
                    mem.completeSendResponse(message, content.strip())
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

//private Message biologyScheduledMessage;
//startChemistryTask();
//startBiologyTask();
//private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
//    private final Map<Long, MetadataContainer> biolUserResponseMap = new ConcurrentHashMap<>();
//    private final Map<Long, MetadataContainer> chemUserResponseMap = new ConcurrentHashMap<>();
//    private final Map<Long, List<String>> bioHistoryMap = new ConcurrentHashMap<>();
//    private final Map<Long, List<String>> chemHistoryMap = new ConcurrentHashMap<>();
    
    //    public void shutdown() {
    //        scheduler.shutdownNow();
    //    }
    //
    //
    //    private void startChemistryTask() {
    //        scheduler.scheduleAtFixedRate(() -> {
    //            AIManager aim = new AIManager();
    //            MessageManager mem = new MessageManager(api);
    //            MetadataContainer previousResponse = chemUserResponseMap.get(0L);
    //
    //            String updateContent = """
    //                You are a routine Discord agent who can send commands to draw organic molecular images every minute.
    //                The format is simple:
    //                The first parameter is (without the quotes) "!d".
    //                Delimited by a space, follows 4 options, "2", "glow", "gsrs", "shadow". These would then be "!d 2", "!d glow", "!d gsrs" and "!d shadow".
    //                Delimited by another space, follows 3 ways of representing molecules:
    //                    A. common molecule names (if multi-word then they are in quotes).
    //                    B. peptide sequences (AKTP...).
    //                    C. SMILES.
    //                Delimited by a period, multiple molecules can be drawn in a single image "!d glow ketamine.aspirin.PKQ".
    //                These are followed by a space and a title in quotes, becoming: "!d glow ketamine.aspirin.PKQ "Figure 1. Title here""
    //                The unique case is with option "2" where only two molecules (ketamine.aspirin) can be provided at a given time because
    //                it compares them on a single image.
    //                You must only respond with a random molecule or multiple molecules for every request including the full command syntax ("!d <option> <molecule1>.<molecule2> "Title").
    //                Title is a string encapsulated by quotes with a space between it and the molecule(s).
    //                Molecules with spaces in them must be encapsulated in quotes. Each molecule should be separated by a period.
    //                Be creative. Do not pick molecules included in your context history. Only pick chemicals on PubChem. Only use gsrs with 1 molecule. Gsrs doesn't take a title argument. Single-word molecules must not be encapsulated in quotes.
    //                You MUST use this syntax.
    //            """;
    //                                                                                                                                    ;
    //
    //            handleNormalFlow(aim, mem, 0L, message, previousResponse, updateContent, false, 1383632703475421237L, chemUserResponseMap, chemHistoryMap, 50)
    //                .exceptionally(ex -> {
    //                    ex.printStackTrace();
    //                    return null;
    //                });
    //        }, 0, 1, TimeUnit.MINUTES); // or 1, 1, TimeUnit.MINUTES
    //    }


    //    private void startBiologyTask() {
    //        scheduler.scheduleAtFixedRate(() -> {
    //            AIManager aim = new AIManager();
    //            MessageManager mem = new MessageManager(api);
    //            MetadataContainer previousResponse = userResponseMap.get(0L);
    //
    //            String updateContent = """
    //                You are a routine Discord agent who shares a one to three sentence biology fact every minute. Do not repeat facts in your history. You can extrapolate on the previous facts.
    //            """;
    //
    //            handleNormalFlow(aim, mem, 0L, message, previousResponse, updateContent, false, 1383632681467904124L, biolUserResponseMap, bioHistoryMap, 10)
    //                .exceptionally(ex -> {
    //                    ex.printStackTrace();
    //                    return null;
    //                });
    //        }, 0, 5, TimeUnit.MINUTES);
    //    }
    //
    //    public CompletableFuture<Void> completeSendResponse(TextChannel channel, String content) {
    //        CompletableFuture<Void> future = new CompletableFuture<>();
    //        channel.sendMessage(content).queue(
    //            message -> future.complete(null),
    //            error -> future.completeExceptionally(error)
    //        );
    //        return future;
    //    }
            
