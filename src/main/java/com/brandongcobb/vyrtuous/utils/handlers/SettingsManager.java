/*  SettingsManager.java The purpose of this class is to handle configuration

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

import com.brandongcobb.vyrtuous.enums.*;
import com.brandongcobb.vyrtuous.utils.handlers.*;
import com.brandongcobb.vyrtuous.utils.inc.*;
import com.brandongcobb.vyrtuous.records.ModelInfo;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

public class SettingsManager {

    private static SettingsManager settingsManager = new SettingsManager();
    private final Map<Long, String> userModelPairs = new ConcurrentHashMap<>();
    private final Map<Long, String> userSourcePairs = new ConcurrentHashMap<>();
    private static final String DEFAULT_MODEL = ModelRegistry.LLAMA_MODEL.toString();
    private static final String DEFAULT_SOURCE = "llama";

    public static CompletableFuture<SettingsManager> completeGetSettingsInstance() {
        return CompletableFuture.completedFuture(settingsManager);
    }

    public CompletableFuture<String> completeGetUserModel(Long userId) {
        return CompletableFuture.completedFuture(
            userModelPairs.getOrDefault(userId, DEFAULT_MODEL)
        );
    }

    public CompletableFuture<String> completeGetUserSource(Long userId) {
        return CompletableFuture.completedFuture(
            userSourcePairs.getOrDefault(userId, DEFAULT_SOURCE)
        );
    }

    // --- Combined async getter ---

    public CompletableFuture<String[]> completeGetUserSettings(Long userId) {
        return CompletableFuture.allOf(
            completeGetUserModel(userId),
            completeGetUserSource(userId)
        ).thenCompose(v ->
            completeGetUserModel(userId).thenCombine(
                completeGetUserSource(userId),
                (model, source) -> new String[]{model, source}
            )
        );
    }

    // --- Individual async setters ---

    public CompletableFuture<Void> completeSetUserModel(Long userId, String model) {
        return CompletableFuture.runAsync(() -> userModelPairs.put(userId, model));
    }

    public CompletableFuture<Void> completeSetUserSource(Long userId, String source) {
        return CompletableFuture.runAsync(() -> userSourcePairs.put(userId, source));
    }

    // --- Batch map setters (not commonly needed) ---

    public CompletableFuture<Void> completeSetUserModelPair(Map<Long, String> newModelPairs) {
        return CompletableFuture.runAsync(() -> {
            userModelPairs.clear();
            userModelPairs.putAll(newModelPairs);
        });
    }

    public CompletableFuture<Void> completeSetUserSourcePair(Map<Long, String> newSourcePairs) {
        return CompletableFuture.runAsync(() -> {
            userSourcePairs.clear();
            userSourcePairs.putAll(newSourcePairs);
        });
    }

    // Optional: Clear or reset individual users
    public CompletableFuture<Void> completeClearUserSettings(Long userId) {
        return CompletableFuture.runAsync(() -> {
            userModelPairs.remove(userId);
            userSourcePairs.remove(userId);
        });
    }
}
