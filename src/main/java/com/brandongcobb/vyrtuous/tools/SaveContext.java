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

    private final ContextManager modelContextManager;
    private final ContextManager userContextManager;

    public SaveContext(ContextManager modelContextManager, ContextManager userContextManager) {
        this.modelContextManager = modelContextManager;
        this.userContextManager = userContextManager;
    }

    @Override
    public String getName() {
        return "save_context";
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

