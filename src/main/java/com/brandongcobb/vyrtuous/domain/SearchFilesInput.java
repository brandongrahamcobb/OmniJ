//
//  Untitled.swift
//  
//
//  Created by Brandon Cobb on 6/24/25.
//
package com.brandongcobb.vyrtuous.domain;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

public class SearchFilesInput {

    private String rootDirectory;
    private List<String> fileExtensions; // optional filter like .java, .kt
    private List<String> fileNameContains;     // optional substring filter
    private List<String> grepContains;         // optional text content filter
    private int maxResults = 100;
    private transient JsonNode originalJson;
    public JsonNode getOriginalJson() {
        return originalJson;
    }

    public void setOriginalJson(JsonNode originalJson) {
        this.originalJson = originalJson;
    }
    public String getRootDirectory() {
        return rootDirectory;
    }

    public List<String> getFileExtensions() {
        return fileExtensions;
    }

    public List<String> getFileNameContains() {
        return fileNameContains;
    }

    public List<String> getGrepContains() {
        return grepContains;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setRootDirectory(String rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public void setFileExtensions(List<String> fileExtensions) {
        this.fileExtensions = fileExtensions;
    }

    public void setFileNameContains(List<String> fileNameContains) {
        this.fileNameContains = fileNameContains;
    }

    public void setGrepContains(List<String> grepContains) {
        this.grepContains = grepContains;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }
}

