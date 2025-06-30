/*  RefreshContext.java The primary purpose of this class is to act as a tool
 *  for making progressive summaries to handle context limitations. This is a crude class.
 *
 *  Copyright (C) 2025  github.com/brandongrahamcobb
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.brandongcobb.vyrtuous.tools;

import com.brandongcobb.vyrtuous.domain.*;
import com.brandongcobb.vyrtuous.utils.handlers.*;
import com.brandongcobb.vyrtuous.objects.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.CompletableFuture;

public class RefreshContext implements Tool<RefreshContextInput, ToolStatus> {
    
    private static final ObjectMapper mapper = new ObjectMapper();
    private final ContextManager modelContextManager;
    private final ContextManager userContextManager;

    public RefreshContext(ContextManager modelContextManager, ContextManager userContextManager) {
        this.modelContextManager = modelContextManager;
        this.userContextManager = userContextManager;
    }

    /*
     *  Getters
     */
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
    public String getName() {
        return "refresh_context";
    }

    /*
     * Tool
     */
    @Override
    public CompletableFuture<ToolStatus> run(RefreshContextInput input) {
        return CompletableFuture.supplyAsync(() -> {
           try {
               String summary = input.getProgressiveSummary();
               modelContextManager.addEntry(new ContextEntry(ContextEntry.Type.TOOL, "{\"name\":" + "\"" + getName() + "\", \"input\":" + input.getOriginalJson().toString() + "\""));
               userContextManager.addEntry(new ContextEntry(ContextEntry.Type.TOOL, "{\"name\":" + "\"" + getName() + "\", \"input\":" + input.getOriginalJson().toString() + "\""));
               modelContextManager.addEntry(new ContextEntry(ContextEntry.Type.PROGRESSIVE_SUMMARY, summary));
               userContextManager.addEntry(new ContextEntry(ContextEntry.Type.PROGRESSIVE_SUMMARY, summary));
               userContextManager.clearModified();
               userContextManager.printNewEntries(true, true, true, true, true, true, true, true);
               return new ToolStatusWrapper("Memory has been summarized and execution can continue.", true);
           } catch (Exception e) {
               return new ToolStatusWrapper("Failed to refresh context: " + e.getMessage(), false);
           }
        });
    }
}
