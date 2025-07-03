/*  SearchWebInput.java The primary purpose of this class is to
 *  provide input information about the SearchWeb.java tool call.
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class SearchWebInput {
    
    private String description;
    private String name;
    private transient JsonNode originalJson;
    private final String query;

    @JsonCreator
    public SearchWebInput(@JsonProperty("query") String query) {
        this.query = query;
    }
    
    public JsonNode getOriginalJson() {
        return originalJson;
    }
    public String getQuery() {
        return query;
    }
    
    public void setOriginalJson(JsonNode originalJson) {
        this.originalJson = originalJson;
    }
}
