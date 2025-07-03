/*  SearchFilesInput.java The primary purpose of this class is to
 *  provide input information about the SearchFiles.java tool call.
 *
 *  Copyright (C) 2025  github.com/brandongrahamcobb
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.brandongcobb.vyrtuous.domain.input;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public class SearchFilesInput implements ToolInput {

    private String rootDirectory;
    private List<String> fileExtensions;
    private List<String> fileNameContains;
    private List<String> grepContains;
    private int maxResults = 100;
    private transient JsonNode originalJson;
    
    /*
     *  Getters
     */
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
    
    public JsonNode getOriginalJson() {
        return originalJson;
    }

    public String getRootDirectory() {
        return rootDirectory;
    }

    /*
     *  Setters
     */
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
    
    public void setOriginalJson(JsonNode originalJson) {
        this.originalJson = originalJson;
    }
    
    public void setRootDirectory(String rootDirectory) {
        this.rootDirectory = rootDirectory;
    }
}

