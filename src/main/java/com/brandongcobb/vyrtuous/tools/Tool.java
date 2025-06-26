//
//  Tool.java
//  
//
//  Created by Brandon Cobb on 6/24/25.
//
package com.brandongcobb.vyrtuous.tools;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.concurrent.CompletableFuture;

public interface Tool<I, O> {
    String getName();
    String getDescription();
    JsonNode getJsonSchema();
    CompletableFuture<O> run(I input) throws Exception;
}

