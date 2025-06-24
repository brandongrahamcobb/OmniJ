/*  OllamaContainer.java The purpose of this class is to extend and
 *  a MetadataContainer for ollama HTTP posts.
 *
 *  Copyright (th.C) 2025  github.com/brandongrahamcobb
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

import com.brandongcobb.vyrtuous.utils.handlers.*;
import com.brandongcobb.metadata.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

public class LMStudioContainer extends MainContainer {
    
    
    public LMStudioContainer(Map<String, Object> responseMap) {
        
        MetadataKey<String> idKey = new MetadataKey<>("id", Metadata.STRING);
        MetadataKey<String> modelKey = new MetadataKey<>("model", Metadata.STRING);
        MetadataKey<Integer> createdKey = new MetadataKey<>("created", Metadata.INTEGER);
        put(idKey, (String) responseMap.get("id"));
        put(modelKey, (String) responseMap.get("model"));
        put(createdKey, (Integer) responseMap.get("created"));
        MetadataKey<Map<String, Object>> usageKey = new MetadataKey<>("usage", Metadata.MAP);
        put(usageKey, (Map<String, Object>) responseMap.get("usage"));
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
        if (choices == null || choices.isEmpty()) return;
        Map<String, Object> choice = choices.get(0);
        Map<String, Object> message = (Map<String, Object>) choice.get("message");
        if (message != null) {
            MetadataKey<String> roleKey = new MetadataKey<>("role", Metadata.STRING);
            put(roleKey, (String) message.get("role"));
            Object contentObj = message.get("content");
            MetadataKey<String> contentKey = new MetadataKey<>("content", Metadata.STRING);
            if (contentObj instanceof String contentStr) {
                put(contentKey, contentStr);
            }
        }
    }
}
