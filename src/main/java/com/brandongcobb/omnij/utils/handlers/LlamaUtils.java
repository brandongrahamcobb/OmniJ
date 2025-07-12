/*  LlamaUtils.java The purpose of this class is to access the data
 *  stored in an LlamaContainer object.
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
package com.brandongcobb.omnij.utils.handlers;

import com.brandongcobb.metadata.Metadata;
import com.brandongcobb.metadata.MetadataContainer;
import com.brandongcobb.metadata.MetadataKey;

import java.util.concurrent.CompletableFuture;

public class LlamaUtils {
    
    private MetadataContainer container;
    
    public LlamaUtils(MetadataContainer container) {
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
    
    public CompletableFuture<String> completeGetResponseId() {
        return CompletableFuture.supplyAsync(() -> {
            MetadataKey<String> responseIdKey = new MetadataKey<>("id", Metadata.STRING);
            return (String) this.container.get(responseIdKey);
        });
    }
    
    public CompletableFuture<Integer> completeGetTokens() {
        return CompletableFuture.supplyAsync(() -> {
            MetadataKey<Integer> responseTokensKey = new MetadataKey<>("token_count", Metadata.INTEGER);
            return (Integer) this.container.get(responseTokensKey);
        });
    }
    
    /*
     *    Setters
     */
    public CompletableFuture<Void> completeSetPreviousResponseId(String previousResponseId) {
        return CompletableFuture.runAsync(() -> {
            MetadataKey<String> previousResponseIdKey = new MetadataKey<>("previous_response_id", Metadata.STRING);
            this.container.put(previousResponseIdKey, previousResponseId);
        });
    }
}
