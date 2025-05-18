/*  RequestObject.java The primary purpose of this class is to manage the
 *  request object before submission to the AI model.
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

import com.brandongcobb.vyrtuous.Vyrtuous;
import com.brandongcobb.vyrtuous.records.ModelInfo;
import com.brandongcobb.vyrtuous.utils.handlers.ResponseObject;
import com.brandongcobb.vyrtuous.utils.inc.Helpers;
import com.brandongcobb.vyrtuous.utils.inc.ModelRegistry;
import com.brandongcobb.metadata.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class RequestObject extends MetadataContainer {

    private static final MetadataType<String> STRING = new MetadataString();
    private static final MetadataType<Long> LONG = new MetadataLong();
    private static final MetadataType<Integer> INTEGER = new MetadataInteger();
    private static final MetadataType<Double> DOUBLE = new MetadataDouble();
    private static final MetadataType<Float> FLOAT = new MetadataFloat();
    private static final MetadataType<Boolean> BOOLEAN = new MetadataBoolean();
    private static final MetadataType<Map<String, Object>> MAP = new MetadataMap();
    private static final MetadataType<List<String>> LIST = new MetadataList(STRING);

    public RequestObject(Map<String, Object> requestMap) {
        MetadataKey<String> modelKey = new MetadataKey<>("model", new MetadataString());
        String model = (String) requestMap.get("model");
        if (model == null) {
            throw new NullPointerException("Request map is missing the mandatory 'model' field.");
        }
        put(modelKey, model);
        MetadataKey<Float> temperatureKey = new MetadataKey<>("temperature", FLOAT);
        Object tempObj = requestMap.get("temperature");
        if (tempObj != null) {
            // Parse or cast the value to Float
            Float temperature = Float.parseFloat(tempObj.toString());
            put(temperatureKey, temperature);
        }
        MetadataKey<Float> topPKey = new MetadataKey<>("top_p", FLOAT);
        Object topPObj = requestMap.get("top_p");
        if (topPObj != null) {
            Float topP = Float.parseFloat(topPObj.toString());
            put(topPKey, topP);
        }
        MetadataKey<Boolean> streamKey = new MetadataKey<>("stream", BOOLEAN);
        Object streamObj = requestMap.get("stream");
        if (streamObj != null) {
            Boolean stream = Boolean.parseBoolean(streamObj.toString());
            put(streamKey, stream);
        }
        MetadataKey<List<Map<String, Object>>> inputKey = new MetadataKey<>("input", LIST);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> input = (List<Map<String, Object>>) requestMap.get("input");
        if (input != null) {
            put(inputKey, input);
        } else {
            throw new NullPointerException("Request map is missing the mandatory 'input' field.");
        }
        if (requestMap.containsKey("metadata")) {
            MetadataKey<List<Map<String, String>>> metadataKey = new MetadataKey<>("metadata", LIST);
            @SuppressWarnings("unchecked")
            List<Map<String, String>> metadata = (List<Map<String, String>>) requestMap.get("metadata");
            put(metadataKey, metadata);
        }
        if (requestMap.containsKey("max_output_tokens")) {
            MetadataKey<Long> maxOutputTokensKey = new MetadataKey<>("max_output_tokens", LONG);
            Object maxOutputTokensObj = requestMap.get("max_output_tokens");
            if (maxOutputTokensObj != null) {
                Long maxOutputTokens = Long.parseLong(maxOutputTokensObj.toString());
                put(maxOutputTokensKey, maxOutputTokens);
            }
        } else if (requestMap.containsKey("max_tokens")) {
            MetadataKey<Long> maxTokensKey = new MetadataKey<>("max_tokens", LONG);
            Object maxTokensObj = requestMap.get("max_tokens");
            if (maxTokensObj != null) {
                Long maxTokens = Long.parseLong(maxTokensObj.toString());
                put(maxTokensKey, maxTokens);
            }
        }

    }
}
