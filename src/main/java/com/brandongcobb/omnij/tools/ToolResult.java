/*  ToolResult.java The primary purpose of this class is to hold the tool
 *  result outputs.
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
package com.brandongcobb.omnij.tools;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ToolResult {
    
    private final JsonNode output;

    @JsonCreator
    public ToolResult(@JsonProperty("output") JsonNode output) {
        this.output = output;
    }

    /*
     *  Getters
     */
    public JsonNode getOutput() {
        return output;
    }

    public static ToolResult of(ObjectMapper mapper, Object obj) {
        return new ToolResult(mapper.valueToTree(obj));
    }

    public static ToolResult from(JsonNode node) {
        return new ToolResult(node);
    }
}

