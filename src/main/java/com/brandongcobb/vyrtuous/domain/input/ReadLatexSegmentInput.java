//
//  ReadLatexSegmentInput.swift
//  
//
//  Created by Brandon Cobb on 7/1/25.
//

package com.brandongcobb.vyrtuous.domain.input;

import com.fasterxml.jackson.databind.JsonNode;

public class ReadLatexSegmentInput implements ToolInput {
    
    private String path;
    private int numLines;
    private int startLine;
    private transient JsonNode originalJson;

    
    public int getNumLines() {
        return this.numLines;
    }
    
    public String getPath() {
        return this.path;
    }
    
    public int getStartLine() {
        return this.startLine;
    }
    
    @Override
    public JsonNode getOriginalJson() {
        return originalJson;
    }

    /*
     *  Setters
     */
    public void setNumLines(int numLines) {
        this.numLines = numLines;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public void setStartLine() {
        this.startLine = startLine;
    }
    
    @Override
    public void setOriginalJson(JsonNode originalJson) {
        this.originalJson = originalJson;
    }
}
