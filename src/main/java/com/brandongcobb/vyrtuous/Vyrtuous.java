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

import com.brandongcobb.vyrtuous.bots.DiscordBot;
import com.brandongcobb.metadata.MetadataContainer;
import com.brandongcobb.vyrtuous.utils.handlers.*;
import com.brandongcobb.vyrtuous.utils.inc.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CompletionException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.NoSuchElementException;
import net.dv8tion.jda.api.JDA;

public class Vyrtuous {

    private static Vyrtuous app;
    private static final Logger LOGGER = Logger.getLogger(Vyrtuous.class.getName());
    private static Boolean isInputThreadRunning = false;
    public Map<Long, String> userModelPairs = new HashMap<>();
    public Map<Long, String> userSourcePairs = new HashMap<>();
    
    
    public static void main(String[] args) {
        app = new Vyrtuous();
        DiscordBot bot = new DiscordBot();
        boolean isInputThreadRunning = false;
        if (!isInputThreadRunning) {
            ApprovalMode approvalMode = ApprovalMode.FULL_AUTO;
            REPLManager repl = new REPLManager(approvalMode);
            repl.startResponseInputThread();
            isInputThreadRunning = true;
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
