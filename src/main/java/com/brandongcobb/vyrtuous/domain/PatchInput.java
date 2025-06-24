//
//  PatchInput.java
//  
//
//  Created by Brandon Cobb on 6/24/25.
//
package com.brandongcobb.vyrtuous.domain;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

public class PatchInput {
    private String path;
    private String patch;
    private String mode = "replace"; // optional, default
    private transient JsonNode originalJson;
    
    public String getPath() {
        return path;
    }

    public String getPatch() {
        return patch;
    }

    public String getMode() {
        return mode;
    }
    
    public JsonNode getOriginalJson() {
        return originalJson;
    }

    public void setOriginalJson(JsonNode originalJson) {
        this.originalJson = originalJson;
    }
}
