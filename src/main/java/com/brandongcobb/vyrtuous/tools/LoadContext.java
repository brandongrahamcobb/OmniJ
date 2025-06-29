/*  LoadContext.java The primary purpose of this class is to act as a tool
 *  for loading context snapshots.
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
import com.brandongcobb.vyrtuous.objects.*;
import com.brandongcobb.vyrtuous.utils.handlers.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.CompletableFuture;

public class LoadContext implements Tool<LoadContextInput, LoadContextStatus> {

    private final ContextManager modelContextManager;
    private final ContextManager userContextManager;
    private static final ObjectMapper mapper = new ObjectMapper();
    
    public LoadContext(ContextManager modelContextManager, ContextManager userContextManager) {
        this.modelContextManager = modelContextManager;
        this.userContextManager = userContextManager;
    }

    /*
     *  Getters
     */
    @Override
    public String getDescription() {
        return "Loads a context snapshot to build the snapshot";
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
                  "description": "The unique name of the previously saved context snapshot to load."
                }
              },
              "additionalProperties": false
            }
            """);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build load_context schema", e);
        }
    }

    @Override
    public String getName() {
        return "load_context";
    }
    
    /*
     *  Tool
     */
    @Override
    public CompletableFuture<LoadContextStatus> run(LoadContextInput input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                modelContextManager.loadSnapshot(input.getName());
                String msg = "Context snapshot '" + input.getName() + "' loaded successfully.";
                modelContextManager.addEntry(new ContextEntry(ContextEntry.Type.TOOL, "{\"name\": " + "\"" + getName()+ "\"," + input.getOriginalJson().toString() + "\""));
                userContextManager.addEntry(new ContextEntry(ContextEntry.Type.TOOL, "{\"name\": " + "\"" + getName()+ "\"," + input.getOriginalJson().toString() + "\""));
                return new LoadContextStatus(msg, true);
            } catch (Exception e) {
                return new LoadContextStatus("Failed to load context: " + e.getMessage(), false);
            }
        });
    }
}
