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

public class RefreshContext implements Tool<RefreshContextInput, RefreshContextStatus> {

    private final ContextManager contextManager;

    public RefreshContext(ContextManager contextManager) {
        this.contextManager = contextManager;
    }

    @Override
    public String getName() {
        return "refresh_context";
    }

    @Override
    public CompletableFuture<RefreshContextStatus> run(RefreshContextInput input) {
        return CompletableFuture.supplyAsync(() -> {
           try {
               String summary = input.getProgressiveSummary();
               contextManager.addEntry(new ContextEntry(ContextEntry.Type.PROGRESSIVE_SUMMARY, summary));
               contextManager.clearModified();
               contextManager.printNewEntries(true, true, true, true, true, true, true, true);
               return new RefreshContextStatus(true, "Memory has been summarized and execution can continue.");
           } catch (Exception e) {
               return new RefreshContextStatus(false, "Failed to refresh context: " + e.getMessage());
           }
        });
    }

}

