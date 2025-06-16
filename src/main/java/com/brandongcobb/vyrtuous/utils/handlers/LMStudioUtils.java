/*  ResponseUtils.java The purpose of this class is access the response
 *  object metadata.
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
 *  aInteger with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.brandongcobb.vyrtuous.utils.handlers;

import com.brandongcobb.metadata.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class LMStudioUtils {
    
    private MetadataContainer container;
    
    public LMStudioUtils(MetadataContainer container) {
        this.container = container;
    }
    
    /*
     *    Getters
     */
    public CompletableFuture<String> completeGetContent() {
        MetadataKey<String> outputKey = new MetadataKey<>("content", Metadata.STRING);
        return CompletableFuture.completedFuture(this.container.get(outputKey));
    }
    

    public CompletableFuture<MetadataContainer> completeGetContainer() {
        MetadataKey<MetadataContainer> containerKey = new MetadataKey<>("container", Metadata.METADATA);
        return CompletableFuture.completedFuture(container);
    }
    
    public CompletableFuture<Boolean> completeGetFlagged() {
        return CompletableFuture.supplyAsync(() -> {
            MetadataKey<Boolean> flaggedKey = new MetadataKey<>("flagged", Metadata.BOOLEAN);
            Object flaggedObj = this.container.get(flaggedKey);
            return flaggedObj != null && Boolean.parseBoolean(String.valueOf(flaggedObj));
        });
    }
    
    public CompletableFuture<String> completeGetPreviousResponseId() {
        return CompletableFuture.supplyAsync(() -> {
            MetadataKey<String> previousResponseIdKey = new MetadataKey<>("previous_response_id", Metadata.STRING);
            return this.container.get(previousResponseIdKey);
        });
    }
    
    /*
     *    Setters
     */
    public CompletableFuture<Void> completeSetPreviousResponseId(long previousResponseId) {
        return CompletableFuture.runAsync(() -> {
            MetadataKey<Long> previousResponseIdKey = new MetadataKey<>("previous_response_id", Metadata.LONG);
            this.container.put(previousResponseIdKey, previousResponseId);
        });
    }
}
