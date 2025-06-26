//
//  RefreshContext.java
//  
//
//  Created by Brandon Cobb on 6/24/25.
//
package com.brandongcobb.vyrtuous.tools;

import com.brandongcobb.vyrtuous.domain.*;
import com.brandongcobb.vyrtuous.utils.handlers.*;
import com.brandongcobb.vyrtuous.objects.*;

import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
public class RefreshContext implements Tool<RefreshContextInput, RefreshContextStatus> {
    
    private static final ObjectMapper mapper = new ObjectMapper();
    private final ContextManager modelContextManager;
    private final ContextManager userContextManager;

    public RefreshContext(ContextManager modelContextManager, ContextManager userContextManager) {
        this.modelContextManager = modelContextManager;
        this.userContextManager = userContextManager;
    }

    @Override
    public String getName() {
        return "refresh_context";
    }
    
    @Override
    public String getDescription() {
        return "Deprecated.";
    }

    @Override
    public JsonNode getJsonSchema() {
        try {
            return mapper.readTree("""
            {
                "type": "object",
                "properties": {
                    "progressiveSummary": {
                        "type": "string",
                        "description": "Optional summary content to inject into memory context."
                    }
                },
                    "additionalProperties": false
                }
            }
            """);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build save_context schema", e);
        }
    }

    @Override
    public CompletableFuture<RefreshContextStatus> run(RefreshContextInput input) {
        return CompletableFuture.supplyAsync(() -> {
           try {
               String summary = input.getProgressiveSummary();
               modelContextManager.addEntry(new ContextEntry(ContextEntry.Type.PROGRESSIVE_SUMMARY, summary));
               userContextManager.addEntry(new ContextEntry(ContextEntry.Type.PROGRESSIVE_SUMMARY, summary));
               userContextManager.clearModified();
               userContextManager.printNewEntries(true, true, true, true, true, true, true, true);
               return new RefreshContextStatus(true, "Memory has been summarized and execution can continue.");
           } catch (Exception e) {
               return new RefreshContextStatus(false, "Failed to refresh context: " + e.getMessage());
           }
        });
    }

}

