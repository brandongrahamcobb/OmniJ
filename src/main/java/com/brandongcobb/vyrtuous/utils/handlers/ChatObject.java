/* ChatObject.java The purpose of this class is to interpret and
 * containerize the metadata of OpenAI's response object.
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.HashMap;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;

public class ChatObject extends MetadataContainer {
    
    public ChatObject(Map<String, Object> responseMap) {
        MetadataKey<String> modelKey = new MetadataKey<>("model", Metadata.STRING);
        MetadataKey<String> createdAtKey = new MetadataKey<>("created_at", Metadata.STRING); // Could be DATE if parsed
        MetadataKey<String> roleKey = new MetadataKey<>("role", Metadata.STRING);
        MetadataKey<String> contentKey = new MetadataKey<>("content", Metadata.STRING);
        MetadataKey<String> doneReasonKey = new MetadataKey<>("done_reason", Metadata.STRING);
        MetadataKey<Boolean> doneKey = new MetadataKey<>("done", Metadata.BOOLEAN);
        MetadataKey<Long> totalDurationKey = new MetadataKey<>("total_duration", Metadata.LONG);
        MetadataKey<Integer> loadDurationKey = new MetadataKey<>("load_duration", Metadata.INTEGER);
        MetadataKey<Integer> promptEvalCountKey = new MetadataKey<>("prompt_eval_count", Metadata.INTEGER);
        //MetadataKey<Long> promptEvalDurationKey = new MetadataKey<>("prompt_eval_duration", Metadata.LONG);
        MetadataKey<Integer> evalCountKey = new MetadataKey<>("eval_count", Metadata.INTEGER);
        //MetadataKey<Integer> evalDurationKey = new MetadataKey<>("eval_duration", Metadata.INTEGER);

        Map<String, Object> messageMap = (Map<String, Object>) responseMap.get("message");
        put(modelKey, (String) responseMap.get("model"));
        put(contentKey, (String) messageMap.get("content"));
        put(createdAtKey, (String) responseMap.get("created_at"));
        put(doneReasonKey, (String) responseMap.get("done_reason"));
        put(doneKey, (Boolean) responseMap.get("done"));
        put(totalDurationKey, (Long) responseMap.get("total_duration"));
        put(loadDurationKey, (Integer) responseMap.get("load_duration"));
        put(promptEvalCountKey, (Integer) responseMap.get("prompt_eval_count"));
        //put(promptEvalDurationKey, (Long) responseMap.get("prompt_eval_duration"));
        put(evalCountKey, (Integer) responseMap.get("eval_count"));
        //put(evalDurationKey, (Integer) responseMap.get("eval_duration"));
    }


    public CompletableFuture<String> completeGetContent() {
        MetadataKey<String> contentKey = new MetadataKey<>("content", Metadata.STRING);
        return CompletableFuture.completedFuture(this.get(contentKey));
    }

}
