package com.brandongcobb.vyrtuous.domain;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

public class PatchInput {

    private String targetFile;
    private List<PatchOperation> patches;
    private transient JsonNode originalJson;

    // Required by Tool interface
    public String getTargetFile() {
        return targetFile;
    }

    public List<PatchOperation> getPatches() {
        return patches;
    }

    public JsonNode getOriginalJson() {
        return originalJson;
    }

    public void setOriginalJson(JsonNode originalJson) {
        this.originalJson = originalJson;
    }
}
