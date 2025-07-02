//
//  ListLatexStructureInput.swift
//  
//
//  Created by Brandon Cobb on 7/1/25.
//

package com.brandongcobb.vyrtuous.domain;

import com.fasterxml.jackson.databind.JsonNode;

public class ListLatexStructureInput implements ToolInput {
    private String path;
    private transient JsonNode originalJson;
    
    
    public JsonNode getOriginalJson() {
        return originalJson;
    }
    
    public String getPath() {
        return this.path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    @Override
    public void setOriginalJson(JsonNode originalJson) {
        this.originalJson = originalJson;
    }
}
