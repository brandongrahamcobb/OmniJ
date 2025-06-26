//
//  LoadContextInput.swift
//  
//
//  Created by Brandon Cobb on 6/25/25.
//


package com.brandongcobb.vyrtuous.domain;

import com.fasterxml.jackson.databind.JsonNode;
public class LoadContextInput {

    private String name;
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

    public void setName(String name) {
        this.name = name;
    }
}
