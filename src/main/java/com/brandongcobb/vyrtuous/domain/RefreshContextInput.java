//
//  RefreshContextInput.java
//  
//
//  Created by Brandon Cobb on 6/24/25.
//
//
//  PatchInput.java
//
//
//  Created by Brandon Cobb on 6/24/25.
//
package com.brandongcobb.vyrtuous.domain;

import com.fasterxml.jackson.databind.JsonNode;

public class RefreshContextInput {
    
    private String progressiveSummary; // optional, default
    private transient JsonNode originalJson;
    public JsonNode getOriginalJson() {
        return originalJson;
    }

    public void setOriginalJson(JsonNode originalJson) {
        this.originalJson = originalJson;
    }
    public String getProgressiveSummary() {
        return progressiveSummary;
    }
}

