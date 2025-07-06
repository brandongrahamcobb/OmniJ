/*  Vyrtuous.java The primary purpose of this class is to integrate
 *  local and remote AI tools.
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
package com.brandongcobb.vyrtuous;

import com.brandongcobb.vyrtuous.component.bot.*;
import com.brandongcobb.vyrtuous.component.server.*;
import com.brandongcobb.vyrtuous.service.*;
import net.dv8tion.jda.api.JDA;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.*;
import java.io.*;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

@SpringBootApplication
public class Vyrtuous {

    private JDA api;
    private Vyrtuous app;
    private MessageService mess;
    private static final Logger LOGGER = Logger.getLogger(Vyrtuous.class.getName());
    private static Boolean isInputThreadRunning = false;
    private GuildChannel rawChannel;
    private static ChatMemory replChatMemory = MessageWindowChatMemory.builder().build();
    public Map<Long, String> userModelPairs = new HashMap<>();
    public Map<Long, String> userSourcePairs = new HashMap<>();
    public static final String BLURPLE = "\033[38;5;61m";
    public static final String BRIGHT_BLUE = "\u001B[94m";
    public static final String BRIGHT_CYAN = "\u001B[96m";
    public static final String CYAN = "\u001B[36m";
    public static final String DODGER_BLUE = "\u001B[38;5;33m";
    public static final String FUCHSIA = "\033[38;5;201m";
    public static final String GOLD = "\033[38;5;220m";
    public static final String GREEN = "\u001B[32m";
    public static final String LIME = "\033[38;5;154m";
    public static final String NAVY = "\u001B[38;5;18m";
    public static final String ORANGE = "\033[38;5;208m";
    public static final String PINK = "\033[38;5;205m";
    public static final String PURPLE = "\u001B[35m";
    public static final String RED = "\u001B[31m";
    public static final String RESET = "\u001B[0m";
    public static final String SKY_BLUE = "\u001B[38;5;117m";
    public static final String TEAL = "\u001B[38;5;30m";
    public static final String VIOLET = "\033[38;5;93m";
    public static final String WHITE = "\u001B[37m";
    public static final String YELLOW = "\u001B[33m";
    
    public static void main(String[] args) {
        // Save original streams
        
        ApplicationContext ctx = null;
        
        LOGGER.setLevel(Level.OFF);
        for (Handler h : LOGGER.getParent().getHandlers()) {
            h.setLevel(Level.OFF);
        }
        
        // Start Spring Boot application (only once!)
        ctx = new SpringApplicationBuilder(Vyrtuous.class).run(args);
                
        Vyrtuous app = ctx.getBean(Vyrtuous.class);
        CustomMCPServer server = ctx.getBean(CustomMCPServer.class);
        REPLService replService = ctx.getBean(REPLService.class);
        
        // Main application loop
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                if (replService.isWaitingForInput()) {
                    System.out.print("USER: ");
                    String line = scanner.nextLine();
                    replService.completeWithUserInput(line);
                } else {
                    String line = scanner.nextLine();
                    if (app.looksLikeJsonRpc(line)) {
                        String response = server.handleRequest(line).join();
                        if (response != null && !response.isBlank()) {
                            System.out.println(response);
                            System.out.flush();
                        }
                    } else {
                        replService.startREPL(line)
                            .exceptionally(ex -> {
                                LOGGER.log(Level.SEVERE, "REPL crash", ex);
                                return null;
                            });
                    }
                }
            }
        }
    }
    
    private boolean looksLikeJsonRpc(String line) {
        return line.trim().startsWith("{") && line.contains("\"method\"");
    }

    public CompletableFuture<Vyrtuous> completeGetAppInstance() {
        return CompletableFuture.completedFuture(this);
    }

}
