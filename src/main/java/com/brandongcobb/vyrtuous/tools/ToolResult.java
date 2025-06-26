//
//  ToolResult.java
//  
//
//  Created by Brandon Cobb on 6/26/25.
//
package com.brandongcobb.vyrtuous.tools;
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

