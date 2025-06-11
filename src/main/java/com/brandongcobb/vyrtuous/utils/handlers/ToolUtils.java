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

public class ToolUtils {
    
    private MetadataContainer container;
    private ToolHandler th = new ToolHandler();
    
    public ToolUtils(MetadataContainer container) {
        this.container = container;
    }
    
    /*
     *    Getters
     */
    
    public CompletableFuture<String> completeGetCustomReasoning() {
        MetadataKey<String> summaryKey = new MetadataKey<>("summary", Metadata.STRING);
        return CompletableFuture.completedFuture(this.container.get(summaryKey));
    }
    
    
    public CompletableFuture<String> completeGetText() {
        return CompletableFuture.supplyAsync(() -> {
            MetadataKey<String> textKey = new MetadataKey<>("results", Metadata.STRING);
            return this.container.get(textKey);
        });
    }
    
    public CompletableFuture<List<String>> completeGetShellToolCommand() {
        return CompletableFuture.completedFuture(this.container.get(th.LOCALSHELLTOOL_COMMANDS));
    }
    
    public CompletableFuture<String> completeGetResponseId() {
        return CompletableFuture.supplyAsync(() -> {
            MetadataKey<String> previousResponseIdKey = new MetadataKey<>("id", Metadata.STRING);
            return this.container.get(previousResponseIdKey);
        });
    }
    
    public CompletableFuture<Boolean> completeGetAcceptingTokens() {
        return CompletableFuture.supplyAsync(() -> {
            MetadataKey<Boolean> acceptingTokensMetadataKey = new MetadataKey<>("acceptingTokens", Metadata.BOOLEAN);
            return this.container.get(acceptingTokensMetadataKey);
        });
    }
 
    public CompletableFuture<Integer> completeGetTotalTokenCount() {
        return CompletableFuture.supplyAsync(() -> {
            MetadataKey<Integer> totalTokenCountKey = new MetadataKey<>("totalTokenCount", Metadata.INTEGER);
            return this.container.get(totalTokenCountKey);
        });
    }
}

