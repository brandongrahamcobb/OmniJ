/*  ToolDefinition.java The primary purpose of this class is to define
 *  each tool.
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
package com.brandongcobb.vyrtuous.tools;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class ToolDefinition {
    
    private final String description;
    private final String name;
    private final JsonNode parameters;

    @JsonCreator
    public ToolDefinition(@JsonProperty("name") String name, @JsonProperty("description") String description, @JsonProperty("parameters") JsonNode parameters) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    /*
     *  Getters
     */
    public String getDescription() { return description; }
    
    public String getName() { return name; }
    
    public JsonNode getParameters() { return parameters; }
    
    /*
     *  Nested class
     */
    public static class Builder {
        
        private String name;
        private String description;
        private JsonNode parameters;
        public ToolDefinition build() {
            return new ToolDefinition(name, description, parameters);
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder inputSchema(JsonNode parameters) {
            this.parameters = parameters;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
    }
}
