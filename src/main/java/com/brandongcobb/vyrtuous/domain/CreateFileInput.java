/*  CreateFileInput.java The primary purpose of this class is to
 *  provide input information about the CreateFile.java tool call.
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

public class CreateFileInput {

    private String content;
    private transient JsonNode originalJson;
    private boolean overwrite = false;
    private String path;
    
    /*
     *  Getters
     */
    public String getContent() {
        return content;
    }
    
    public JsonNode getOriginalJson() {
        return originalJson;
    }

    public boolean getOverwrite() {
        return overwrite;
    }
    
    public String getPath() {
        return path;
    }

    /*
     *  Setters
     */
    public void setContent(String content) {
        this.content = content;
    }
    
    public void setOriginalJson(JsonNode originalJson) {
        this.originalJson = originalJson;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
}

