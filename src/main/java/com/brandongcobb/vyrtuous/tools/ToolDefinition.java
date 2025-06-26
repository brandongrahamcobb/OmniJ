//
//  ToolDefinition.java
//  
//
//  Created by Brandon Cobb on 6/26/25.
//
package com.brandongcobb.vyrtuous.tools;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;


public class ToolDefinition {
    private final String name;
    private final String description;
    private final JsonNode parameters; // JSON Schema for tool input

    @JsonCreator
    public ToolDefinition(
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("parameters") JsonNode parameters
    ) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public JsonNode getParameters() { return parameters; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String description;
        private JsonNode parameters;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder inputSchema(JsonNode parameters) {
            this.parameters = parameters;
            return this;
        }

        public ToolDefinition build() {
            return new ToolDefinition(name, description, parameters);
        }
    }
}
