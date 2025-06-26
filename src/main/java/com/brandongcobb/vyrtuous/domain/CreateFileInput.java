package com.brandongcobb.vyrtuous.domain;

import com.fasterxml.jackson.databind.JsonNode;
public class CreateFileInput {

    private String path;
    private String content;
    private boolean overwrite = false; // default: do not overwrite
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

    public String getContent() {
        return content;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }
}

