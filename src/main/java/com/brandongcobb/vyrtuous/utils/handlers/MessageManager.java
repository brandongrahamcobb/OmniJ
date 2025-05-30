/*  MessageManager.java The purpose of this program is to manage responding to
 *  users on Discord.
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
import com.brandongcobb.metadata.*;
import com.brandongcobb.vyrtuous.utils.handlers.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Locale;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.utils.FileUpload;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.nio.file.Files;

public class MessageManager {

    private Lock lock;
    private ObjectMapper mapper = new ObjectMapper();
    private File tempDirectory;

    public MessageManager() {
        this.tempDirectory = new File(System.getProperty("java.io.tmpdir"));
    }

    public CompletableFuture<List<String>> completeProcessAttachments(List<Attachment> attachments) {
        List<String> results = Collections.synchronizedList(new ArrayList<>());
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Attachment attachment : attachments) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    String url = attachment.getUrl();
                    String fileName = attachment.getFileName();
                    String contentType = attachment.getContentType();
                    File tempFile = new File(tempDirectory, fileName);
                    try (InputStream in = new URL(url).openStream()) {
                        Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                    if (contentType != null && contentType.startsWith("text/")) {
                        String textContent = Files.readString(tempFile.toPath(), StandardCharsets.UTF_8);
                        results.add(textContent);
                    } else if (contentType != null && contentType.startsWith("image/")) {
                        results.add("""
                            {
                              "type": "image_url",
                              "image_url": {
                                "url": "%s"
                              }
                            }
                            """.formatted(url));
                    } else {
                        results.add("Skipped non-image or non-text attachment: " + fileName);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    results.add("Failed to process: " + attachment.getFileName());
                }
            });
            futures.add(future);
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> results)
            .exceptionally(ex -> {
                ex.printStackTrace();
                return List.of("Error occurred");
            });
    }

    private String encodeImage(byte[] imageBytes) {
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    private String getContentTypeFromFileName(String fileName) {
        String lowerName = fileName.toLowerCase(Locale.ROOT);
        if (lowerName.endsWith(".png")) return "image/png";
        if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) return "image/jpeg";
        if (lowerName.endsWith(".gif")) return "image/gif";
        if (lowerName.endsWith(".bmp")) return "image/bmp";
        if (lowerName.endsWith(".webp")) return "image/webp";
        if (lowerName.endsWith(".svg")) return "image/svg+xml";
        if (lowerName.endsWith(".txt")) return "text/plain";
        if (lowerName.endsWith(".md")) return "text/markdown";
        if (lowerName.endsWith(".csv")) return "text/csv";
        if (lowerName.endsWith(".json")) return "application/json";
        if (lowerName.endsWith(".xml")) return "application/xml";
        if (lowerName.endsWith(".html") || lowerName.endsWith(".htm")) return "text/html";
        return "application/octet-stream";
    }

    private List<CompletableFuture<Message>> sendInChunks(Message message, String text) {
        List<CompletableFuture<Message>> chunks = new ArrayList<>();
        int maxLength = 2000;
        int index = 0;
        while (index < text.length()) {
            int end = Math.min(index + maxLength, text.length());
            if (end < text.length()) {
                int lastNewline = text.lastIndexOf("\n", end);
                int lastSpace = text.lastIndexOf(" ", end);
                if (lastNewline > index) end = lastNewline;
                else if (lastSpace > index) end = lastSpace;
            }
            String chunk = text.substring(index, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(completeSendDiscordMessage(message, chunk));
            }
            index = end;
        }
        return chunks;
    }
    
    public CompletableFuture<Void> completeSendResponse(Message message, String response) {
        List<CompletableFuture<Message>> futures = new ArrayList<>();
        Pattern codeBlockPattern = Pattern.compile("```(\\w+)?\\n([\\s\\S]*?)```");
        Matcher matcher = codeBlockPattern.matcher(response);
        int fileIndex = 0;
        int lastEnd = 0;
        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                String beforeCode = response.substring(lastEnd, matcher.start()).trim();
                if (!beforeCode.isEmpty()) {
                    futures.addAll(sendInChunks(message, beforeCode));
                }
            }
            String fileType = matcher.group(1) != null ? matcher.group(1) : "txt";
            String codeContent = matcher.group(2).trim();
            if (codeContent.length() < 1900) {
                String codeMessage = "```" + fileType + "\n" + codeContent + "\n```";
                futures.add(completeSendDiscordMessage(message, codeMessage));
            } else {
                File file = new File(tempDirectory, "response_" + (fileIndex++) + "." + fileType);
                try {
                    Files.writeString(file.toPath(), codeContent, StandardCharsets.UTF_8);
                    futures.add(completeSendDiscordMessage(message, "📄 Long code block attached:", file));
                } catch (IOException e) {
                    String error = "❌ Error writing code block to file: " + e.getMessage();
                    futures.add(completeSendDiscordMessage(message, error));
                }
            }
            lastEnd = matcher.end();
        }
        if (lastEnd < response.length()) {
            String remaining = response.substring(lastEnd).trim();
            if (!remaining.isEmpty()) {
                futures.addAll(sendInChunks(message, remaining));
            }
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }
    
    public CompletableFuture<Message> completeSendDiscordMessage(Message message, String content, MessageEmbed embed) {
        return message.getGuildChannel()
            .asTextChannel()
            .sendMessage(content)
            .addEmbeds(embed)
            .submit();
    }

    public CompletableFuture<Message> completeSendDiscordMessage(Message message, String content) {
        return message.getGuildChannel()
            .asTextChannel()
            .sendMessage(content)
            .submit();
    }

    public CompletableFuture<Message> completeSendDiscordMessage(Message message, String content, File file) {
        return message.getGuildChannel()
            .asTextChannel()
            .sendMessage(content)
            .addFiles(FileUpload.fromData(file))
            .submit();
    }

    public CompletableFuture<Message> completeSendDiscordMessage(PrivateChannel channel, String content, File file) {
        return channel.sendMessage(content)
            .addFiles(FileUpload.fromData(file))
            .submit();
    }

    public CompletableFuture<Message> completeSendDiscordMessage(PrivateChannel channel, String content, MessageEmbed embed) {
        return channel.sendMessage(content)
            .addEmbeds(embed)
            .submit();
    }
    
    public CompletableFuture<Message> completeSendDM(User user, String content) {
        return user.openPrivateChannel()
            .submit()
            .thenCompose(channel -> channel.sendMessage(content).submit());
    }
}
