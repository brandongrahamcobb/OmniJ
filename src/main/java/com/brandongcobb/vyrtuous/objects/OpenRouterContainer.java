/*  OpenRouterContainer.java The purpose of this class is to extend and
 *  a MetadataContainer for OpenRouter HTTP posts.
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
import com.brandongcobb.metadata.MetadataKey;

import java.util.List;
import java.util.Map;

public class OpenRouterContainer extends MainContainer {
    
    public OpenRouterContainer(Map<String, Object> responseMap) {
        
        List<Map<String, Object>> completionChoices = (List<Map<String, Object>>) responseMap.get("choices");
        if (completionChoices == null || completionChoices.isEmpty()) return;
        Map<String, Object> completionChoice = completionChoices.get(0);
        Map<String, Object> completionMessage = (Map<String, Object>) completionChoice.get("message");
        if (completionMessage != null) {
            String role = (String) completionMessage.get("role");
            MetadataKey<String> completionContentKey = new MetadataKey<>("content", Metadata.STRING);
            Object contentObj = completionMessage.get("content");
            String content = (String) contentObj;
            put(completionContentKey, content);
            if (!(contentObj instanceof List<?> contentList)) return;
            for (Object itemObj : contentList) {
                MetadataKey<String> objectKey = new MetadataKey<>("object", Metadata.STRING);
                String requestObject = (String) responseMap.get("object");
                if (requestObject == null) {
                    throw new NullPointerException("The response map is missing the mandatory 'object' field.");
                }
                put(objectKey, requestObject);
                MetadataKey<Integer> completionCreatedKey = new MetadataKey<>("created", Metadata.INTEGER);
                Integer completionCreated = (Integer) responseMap.get("created");
                put(completionCreatedKey, completionCreated);
                MetadataKey<String> completionModelKey = new MetadataKey<>("model", Metadata.STRING);
                String completionModel = (String) responseMap.get("model");
                put(completionModelKey, completionModel);
            }
        }
    }
}
