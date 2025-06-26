//
//  SaveContext.java
//  
//
//  Created by Brandon Cobb on 6/25/25.
//
package com.brandongcobb.vyrtuous.tools;

import com.brandongcobb.vyrtuous.utils.handlers.ContextManager;

import com.brandongcobb.vyrtuous.objects.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.brandongcobb.vyrtuous.domain.*;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SaveContext implements Tool<SaveContextInput, SaveContextStatus> {

    private final ContextManager modelContextManager;
    private final ContextManager userContextManager;
    private static final ObjectMapper mapper = new ObjectMapper();
    
    public SaveContext(ContextManager modelContextManager, ContextManager userContextManager) {
        this.modelContextManager = modelContextManager;
        this.userContextManager = userContextManager;
    }

    @Override
    public String getName() {
        return "save_context";
    }
    
    @Override
    public String getDescription() {
        return "Saves the current state under a given name.";
    }

    @Override
    public JsonNode getJsonSchema() {
        try {
            return mapper.readTree("""
            {
              "type": "object",
              "required": ["name"],
              "properties": {
                "name": {
                  "type": "string",
                  "description": "A unique identifier for the context snapshot."
                },
                "description": {
                  "type": "string",
                  "description": "Optional description or annotation for the snapshot."
                }
              },
              "additionalProperties": false
            }
            """);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build save_context schema", e);
        }
    }

    @Override
    public CompletableFuture<SaveContextStatus> run(SaveContextInput input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                modelContextManager.saveSnapshot(input.getName(), input.getDescription());
                String msg = "Context snapshot '" + input.getName() + "' saved successfully.";
                userContextManager.addEntry(new ContextEntry(ContextEntry.Type.TOOL, input.getOriginalJson().toString()));
                return new SaveContextStatus(true, msg);
            } catch (Exception e) {
                return new SaveContextStatus(false, "Failed to save context: " + e.getMessage());
            }
        });
    }
}

