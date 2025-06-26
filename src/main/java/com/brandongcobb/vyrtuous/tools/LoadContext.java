//
//  LoadContext.swift
//  
//
//  Created by Brandon Cobb on 6/25/25.
//


package com.brandongcobb.vyrtuous.tools;

import com.brandongcobb.vyrtuous.domain.*;
import com.brandongcobb.vyrtuous.objects.*;
import com.brandongcobb.vyrtuous.utils.handlers.ContextManager;

import java.util.concurrent.CompletableFuture;

public class LoadContext implements Tool<LoadContextInput, LoadContextStatus> {

    private final ContextManager modelContextManager;
    private final ContextManager userContextManager;

    public LoadContext(ContextManager modelContextManager, ContextManager userContextManager) {
        this.modelContextManager = modelContextManager;
        this.userContextManager = userContextManager;
    }

    @Override
    public String getName() {
        return "load_context";
    }

    @Override
    public CompletableFuture<LoadContextStatus> run(LoadContextInput input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                modelContextManager.loadSnapshot(input.getName());
                String msg = "Context snapshot '" + input.getName() + "' loaded successfully.";
                userContextManager.addEntry(new ContextEntry(ContextEntry.Type.TOOL, input.getOriginalJson().toString()));
                return new LoadContextStatus(true, msg);
            } catch (Exception e) {
                return new LoadContextStatus(false, "Failed to load context: " + e.getMessage());
            }
        });
    }
}
