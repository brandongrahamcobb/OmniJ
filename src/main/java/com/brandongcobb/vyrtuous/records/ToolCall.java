//
//  ToolCall.java
//  
//
//  Created by Brandon Cobb on 7/3/25.
//


package com.brandongcobb.vyrtuous.records;

import com.fasterxml.jackson.databind.JsonNode;

public record ToolCall(String name, JsonNode arguments) {}
