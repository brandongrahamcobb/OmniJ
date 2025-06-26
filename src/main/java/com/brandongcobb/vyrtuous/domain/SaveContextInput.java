package com.brandongcobb.vyrtuous.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.brandongcobb.vyrtuous.tools.*;
import com.brandongcobb.vyrtuous.objects.*;

public class SaveContextInput {

    private String name;  // e.g., "before_refactor", "v1.0 checkpoint"
    private String description; // optional
    private transient JsonNode originalJson;
    public JsonNode getOriginalJson() {
        return originalJson;
    }

    public void setOriginalJson(JsonNode originalJson) {
        this.originalJson = originalJson;
    }
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

