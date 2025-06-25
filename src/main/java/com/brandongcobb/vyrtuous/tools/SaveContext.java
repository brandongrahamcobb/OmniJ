//
//  SaveContext.java
//  
//
//  Created by Brandon Cobb on 6/25/25.
//
package com.brandongcobb.vyrtuous.tools;

import com.brandongcobb.vyrtuous.utils.handlers.ContextManager;

import com.brandongcobb.vyrtuous.objects.*;

import com.brandongcobb.vyrtuous.domain.*;
import java.util.concurrent.CompletableFuture;

public class SaveContext implements Tool<SaveContextInput, SaveContextStatus> {

    private final ContextManager contextManager;

    public SaveContext(ContextManager contextManager) {
        this.contextManager = contextManager;
    }

    @Override
    public String getName() {
        return "save_context";
    }

    @Override
    public CompletableFuture<SaveContextStatus> run(SaveContextInput input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                contextManager.saveSnapshot(input.getName(), input.getDescription());
                String msg = "Context snapshot '" + input.getName() + "' saved successfully.";
                contextManager.addEntry(new ContextEntry(ContextEntry.Type.TOOL_OUTPUT, msg));
                return new SaveContextStatus(true, msg);
            } catch (Exception e) {
                return new SaveContextStatus(false, "Failed to save context: " + e.getMessage());
            }
        });
    }
}

