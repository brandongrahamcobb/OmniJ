//
//  ReadLatexSegmentInput.swift
//  
//
//  Created by Brandon Cobb on 7/1/25.
//


//
//  ReadLatexSegmentInput.swift
//  
//
//  Created by Brandon Cobb on 7/1/25.
//
package com.brandongcobb.vyrtuous.domain.input;

import com.fasterxml.jackson.databind.JsonNode;

public class SummarizeLatexSectionInput  implements ToolInput {
    
    private int endLine;
    private String filePath;
    private int startLine;
    private transient JsonNode originalJson;

    
    public int getEndLine() {
        return this.endLine;
    }
    
    public String getPath() {
        return this.filePath;
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
    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }
    
    public void setPath(String filePath) {
        this.filePath = filePath;
    }
    
    public void setStartLine() {
        this.startLine = startLine;
    }
    
    @Override
    public void setOriginalJson(JsonNode originalJson) {
        this.originalJson = originalJson;
    }
}
