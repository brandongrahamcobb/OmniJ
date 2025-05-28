///*  RequestObject.java The primary purpose of this class is to manage the
// *  request object before submission to the AI model.
// *
// *  Copyright (C) 2025  github.com/brandongrahamcobb
// *
// *  This program is free software: you can redistribute it and/or modify
// *  it under the terms of the GNU General Public License as published by
// *  the Free Software Foundation, either version 3 of the License, or
// *  (at your option) any later version.
// *
// *  This program is distributed in the hope that it will be useful,
// *  but WITHOUT ANY WARRANTY; without even the implied warranty of
// *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// *  GNU General Public License for more details.
// *
// *  You should have received a copy of the GNU General Public License
// *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
// */
//package com.brandongcobb.vyrtuous.utils.handlers;
//
//import com.brandongcobb.vyrtuous.Vyrtuous;
//import com.brandongcobb.vyrtuous.records.ModelInfo;
//import com.brandongcobb.vyrtuous.utils.handlers.ResponseObject;
//import com.brandongcobb.vyrtuous.utils.inc.Helpers;
//import com.brandongcobb.vyrtuous.utils.inc.ModelRegistry;
//import com.brandongcobb.metadata.*;
//
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.CompletableFuture;
//
//public class RequestObject extends MetadataContainer {
//
//    public RequestObject(Map<String, Object> requestMap) {
//        MetadataKey<String> modelKey = new MetadataKey<>("model", new MetadataString());
//        String model = (String) requestMap.get("model");
//        if (model == null) {
//            throw new NullPointerException("Request map is missing the mandatory 'model' field.");
//        }
//        put(modelKey, model);
//        MetadataKey<Float> temperatureKey = new MetadataKey<>("temperature", Metadata.FLOAT);
//        Object tempObj = requestMap.get("temperature");
//        if (tempObj != null) {
//            // Parse or cast the value to Float
//            Float temperature = Float.parseFloat(tempObj.toString());
//            put(temperatureKey, temperature);
//        }
//        MetadataKey<Float> topPKey = new MetadataKey<>("top_p", Metadata.FLOAT);
//        Object topPObj = requestMap.get("top_p");
//        if (topPObj != null) {
//            Float topP = Float.parseFloat(topPObj.toString());
//            put(topPKey, topP);
//        }
//        MetadataKey<Boolean> streamKey = new MetadataKey<>("stream", Metadata.BOOLEAN);
//        Object streamObj = requestMap.get("stream");
//        if (streamObj != null) {
//            Boolean stream = Boolean.parseBoolean(streamObj.toString());
//            put(streamKey, stream);
//        }
//        MetadataKey<List<Map<String, Object>>> inputKey = new MetadataKey<>("input", Metadata.LIST_MAP);
//
//        List<Map<String, Object>> input = (List<Map<String, Object>>) requestMap.get("input");
//        if (input != null) {
//            put(inputKey, input);
//        } else {
//            throw new NullPointerException("Request map is missing the mandatory 'input' field.");
//        }
//        if (requestMap.containsKey("metadata")) {
//            MetadataKey<List<Map<String, String>>> metadataKey = new MetadataKey<>("metadata", Metadata.LIST_MAP);
//            @SuppressWarnings("unchecked")
//            List<Map<String, String>> metadata = (List<Map<String, String>>) requestMap.get("metadata");
//            put(metadataKey, metadata);
//        }
//        if (requestMap.containsKey("max_output_tokens")) {
//            MetadataKey<Long> maxOutputTokensKey = new MetadataKey<>("max_output_tokens", Metadata.LONG);
//            Object maxOutputTokensObj = requestMap.get("max_output_tokens");
//            if (maxOutputTokensObj != null) {
//                Long maxOutputTokens = Long.parseLong(maxOutputTokensObj.toString());
//                put(maxOutputTokensKey, maxOutputTokens);
//            }
//        } else if (requestMap.containsKey("max_tokens")) {
//            MetadataKey<Long> maxTokensKey = new MetadataKey<>("max_tokens", Metadata.LONG);
//            Object maxTokensObj = requestMap.get("max_tokens");
//            if (maxTokensObj != null) {
//                Long maxTokens = Long.parseLong(maxTokensObj.toString());
//                put(maxTokensKey, maxTokens);
//            }
//        }
//        if (requestMap.containsKey("tools")) {
//            MetadataKey<List<Map<String, Object>>> toolsKey = new MetadataKey<>("tools", Metadata.LIST_MAP);
//            Object toolsObj = requestMap.get("tools");
//    
//            if (toolsObj instanceof List<?> toolsList && !toolsList.isEmpty()) {
//                @SuppressWarnings("unchecked")
//                List<Map<String, Object>> tools = (List<Map<String, Object>>) toolsList;
//                put(toolsKey, tools); // Save raw list
//            
//                for (Map<String, Object> toolMap : tools) {
//                    Object typeObj = toolMap.get("type");
//    
//                    if (typeObj instanceof String type) {
//                        switch (type) {
//                            case "file_search" -> {
//                                if (toolMap.containsKey("vector_store_ids")) {
//                                    put(new MetadataKey<>("filesearchtool_vector_store_ids", Metadata.LIST_STRING),
//                                        (List<String>) toolMap.get("vector_store_ids"));
//                                }
//                                if (toolMap.containsKey("filters")) {
//                                    put(new MetadataKey<>("filesearchtool_filters", Metadata.MAP),
//                                        (Map<String, Object>) toolMap.get("filters"));
//                                }
//                                if (toolMap.containsKey("max_num_results")) {
//                                    put(new MetadataKey<>("filesearchtool_max_num_results", Metadata.INTEGER),
//                                        Integer.parseInt(toolMap.get("max_num_results").toString()));
//                                }
//                                if (toolMap.containsKey("ranking_options")) {
//                                    put(new MetadataKey<>("filesearchtool_ranking_options", Metadata.MAP),
//                                        (Map<String, Object>) toolMap.get("ranking_options"));
//                                }
//                            }
//    
//                            case "local_shell" -> {
//                                // Capture the local shell tool type and command
//                                put(new MetadataKey<>("localshelltool_type", Metadata.STRING), "local_shell");
//                                Object cmdObj = toolMap.get("command");
//                                if (cmdObj instanceof String cmd) {
//                                    put(new MetadataKey<>("localshelltool_command", Metadata.STRING), cmd);
//                                }
//                            }
//    
//                            default -> {
//                                // Optionally log or skip unknown tool types
//                                System.out.println("⚠️ Unknown tool type: " + type);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
