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

import com.brandongcobb.vyrtuous.component.server.CustomMCPServer;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootApplication
public class Vyrtuous {

    private static Vyrtuous app;
    private static final Logger LOGGER = Logger.getLogger(Vyrtuous.class.getName());
    private static Boolean isInputThreadRunning = false;
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
        SpringApplication.run(Vyrtuous.class, args);
        LOGGER.setLevel(Level.OFF);
        app = new Vyrtuous();
        for (Handler h : LOGGER.getParent().getHandlers()) {
            h.setLevel(Level.OFF);
        }
        try {
            new CountDownLatch(1).await();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    public static CompletableFuture<Vyrtuous> completeGetAppInstance() {
        return CompletableFuture.completedFuture(app);
    }

}
