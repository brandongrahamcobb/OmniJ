//
//  ReadFileInput.java
//  
//
//  Created by Brandon Cobb on 6/24/25.
//

package com.brandongcobb.vyrtuous.domain;

import com.fasterxml.jackson.databind.JsonNode;
public class ReadFileInput {

    private String path;
    private transient JsonNode originalJson;
    public JsonNode getOriginalJson() {
        return originalJson;
    }

    public void setOriginalJson(JsonNode originalJson) {
        this.originalJson = originalJson;
    }
    
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
