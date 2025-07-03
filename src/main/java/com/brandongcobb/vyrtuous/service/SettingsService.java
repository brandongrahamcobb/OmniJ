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
package com.brandongcobb.vyrtuous.service;

import com.brandongcobb.vyrtuous.enums.ModelRegistry;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SettingsService {

    private static SettingsService settingsService = new SettingsService();
    private final Map<Long, String> userModelPairs = new ConcurrentHashMap<>();
    private final Map<Long, String> userSourcePairs = new ConcurrentHashMap<>();
    private static final String DEFAULT_MODEL = ModelRegistry.LLAMA_MODEL.toString();
    private static final String DEFAULT_SOURCE = "llama";

    /*
     *  Getters
     */
    public static CompletableFuture<SettingsService> completeGetSettingsInstance() {
        return CompletableFuture.completedFuture(settingsService);
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

    /*
     *  Setters
     */
    public CompletableFuture<Void> completeSetUserModel(Long userId, String model) {
        return CompletableFuture.runAsync(() -> userModelPairs.put(userId, model));
    }

    public CompletableFuture<Void> completeSetUserSource(Long userId, String source) {
        return CompletableFuture.runAsync(() -> userSourcePairs.put(userId, source));
    }

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

    /*
     *  Helpers
     */
    public CompletableFuture<Void> completeClearUserSettings(Long userId) {
        return CompletableFuture.runAsync(() -> {
            userModelPairs.remove(userId);
            userSourcePairs.remove(userId);
        });
    }
}
