/*  FindInFilesInput.java The primary purpose of this class is to
 *  provide input information about the FindInFiles.java tool call.
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
package com.brandongcobb.vyrtuous.domain;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public class FindInFileInput implements ToolInput {

    private int contextLines = 2;
    private String filePath;
    private boolean ignoreCase = true;
    private transient JsonNode originalJson;
    private List<String> searchTerms;
    private boolean useRegex = false;
    private int maxResults = 10;

    /*
     *  Getters
     */
    public int getContextLines() {
        return contextLines;
    }

    public String getFilePath() {
        return filePath;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    @Override
    public JsonNode getOriginalJson() {
        return originalJson;
    }

    public List<String> getSearchTerms() {
        return searchTerms;
    }

    public boolean isUseRegex() {
        return useRegex;
    }

    public int getMaxResults() {
        return maxResults;
    }

    /*
     *  Setters
     */
    public void setContextLines(int contextLines) {
        this.contextLines = contextLines;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    @Override
    public void setOriginalJson(JsonNode originalJson) {
        this.originalJson = originalJson;
    }

    public void setSearchTerms(List<String> searchTerms) {
        this.searchTerms = searchTerms;
    }

    public void setUseRegex(boolean useRegex) {
        this.useRegex = useRegex;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }
}
