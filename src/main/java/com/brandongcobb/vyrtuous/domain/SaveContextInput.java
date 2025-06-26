/*  SaveContextInput.java The primary purpose of this class is to
 *  provide input information about the SaveContext.java tool call.
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
import com.brandongcobb.vyrtuous.tools.*;
import com.brandongcobb.vyrtuous.objects.*;

public class SaveContextInput {
    
    private String description;
    private String name;
    private transient JsonNode originalJson;
    
    /*
     *  Getters
     */
    public String getDescription() {
        return description;
    }
    
    public String getName() {
        return name;
    }
    
    public JsonNode getOriginalJson() {
        return originalJson;
    }
    
    /*
     *  Setters
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public void setOriginalJson(JsonNode originalJson) {
        this.originalJson = originalJson;
    }
}

