/*  ChatObject.java The purpose of this class is to extend and
 *  a MetadataContainer for completion objects.
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
package com.brandongcobb.vyrtuous.objects;

import com.brandongcobb.metadata.Metadata;
import com.brandongcobb.metadata.MetadataContainer;
import com.brandongcobb.metadata.MetadataKey;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MainContainer extends MetadataContainer {

    public static final MetadataKey<Boolean> LOCALSHELLTOOL_FINISHED = new MetadataKey<>("localshelltool_finished", Metadata.BOOLEAN);
    
    /*
     *  Getters
     */
    public CompletableFuture<String> completeGetContent() {
        MetadataKey<String> contentKey = new MetadataKey<>("content", Metadata.STRING);
        return CompletableFuture.completedFuture(this.get(contentKey));
    }
    
    public CompletableFuture<String> completeGetResponseMap() {
        MetadataKey<String> responseMapKey = new MetadataKey<>("response_map", Metadata.STRING);
        return CompletableFuture.completedFuture(this.get(responseMapKey));
    }
    
    public CompletableFuture<Boolean> completeGetShellToolFinished() {
        return CompletableFuture.completedFuture(this.get(LOCALSHELLTOOL_FINISHED));
    }
    
    /*
     *  Helpers
     */
    public static String mapToJsonString(Map<String, Object> map) {
        try {
            return new ObjectMapper().writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert map to JSON string", e);
        }
    }
}
    

