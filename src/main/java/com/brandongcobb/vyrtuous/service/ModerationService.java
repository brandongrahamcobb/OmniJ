/*  ModerationService.java The purpose of this program is to handle all explicit
 *  strings from the program's endpoints.
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
package com.brandongcobb.vyrtuous.service;

import com.brandongcobb.vyrtuous.Vyrtuous;
import com.brandongcobb.vyrtuous.enums.ModelRegistry;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.logging.Logger;

@Service
public class ModerationService {

    private Vyrtuous app;
    private final Object fileLock = new Object();
    private Lock lock;
    private Logger logger;
    private File tempDirectory = new File(System.getProperty("java.io.tmpdir"));
    private File tempFile = new File(System.getProperty("java.io.tmpdir"), "config.yml");
    private Map<Long, Integer> userCounts;
    private JDA jda;
    
    public ModerationService(JDA jda) {
        this.jda = jda;
    }
    
    public CompletableFuture<Void> completeHandleModeration(Message message, String reasonStr) {
        MessageService mess = new MessageService(jda);
        User author = message.getAuthor();
        Guild guild = message.getGuild();
        if (guild == null || author == null) {
            return CompletableFuture.completedFuture(null);
        }
        Member member = guild.getMember(author);
        if (member == null) {
            return CompletableFuture.completedFuture(null);
        }
        String warningMsg = ModelRegistry.OPENAI_MODERATION_RESPONSE_WARNING.asString();
        long userId = author.getIdLong();
        return CompletableFuture.supplyAsync(() -> {
            synchronized (fileLock) {
                Map<Long, Integer> userCounts = new HashMap<>();
                if (tempFile.exists()) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String[] parts = line.split(":");
                            if (parts.length == 2) {
                                try {
                                    long id = Long.parseLong(parts[0]);
                                    int count = Integer.parseInt(parts[1]);
                                    userCounts.put(id, count);
                                } catch (NumberFormatException ignored) {}
                            }
                        }
                    } catch (IOException e) {
                        logger.severe("Failed to read tempFile: " + e.getMessage());
                        return null;
                    }
                }
                int currentCount = userCounts.getOrDefault(userId, 0);
                currentCount++;
                userCounts.put(userId, currentCount);
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile, false))) {
                    for (Map.Entry<Long, Integer> entry : userCounts.entrySet()) {
                        writer.write(entry.getKey() + ":" + entry.getValue());
                        writer.newLine();
                    }
                } catch (IOException e) {
                    logger.severe("Failed to write to tempFile: " + e.getMessage());
                    return null;
                }
                return currentCount;
            }
        }).thenCompose(flaggedCount -> {
            if (flaggedCount == null) {
                return CompletableFuture.completedFuture(null);
            }
            message.delete().queue();
            return mess.completeSendDiscordMessage(message, warningMsg)
                .thenCompose(msg -> {
                    if (flaggedCount >= 5) {
                        CompletableFuture<Void> timeoutFuture = mess.completeSendDiscordMessage(message,
                                "You have been timed out for 5 minutes due to repeated violations.")
                            .thenRun(() -> {
                                member.timeoutFor(Duration.ofSeconds(300)).queue();
                                synchronized (fileLock) {
                                    Map<Long, Integer> userCountsReset = new HashMap<>();
                                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile, false))) {
                                        for (Map.Entry<Long, Integer> entry : userCountsReset.entrySet()) {
                                            writer.write(entry.getKey() + ":" + entry.getValue());
                                            writer.newLine();
                                        }
                                    } catch (IOException e) {
                                        logger.severe("Failed to reset counts in tempFile: " + e.getMessage());
                                    }
                                }
                            });
                        return timeoutFuture;
                    } else {
                        return CompletableFuture.completedFuture(null);
                    }
                });
        });
    }
}
