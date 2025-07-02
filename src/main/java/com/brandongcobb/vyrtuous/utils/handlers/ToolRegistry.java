/*  ToolRegistry.java The primary purpose of this class is to act include
 *  call, convert and list tools.
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
package com.brandongcobb.vyrtuous.utils.handlers;

import com.brandongcobb.vyrtuous.domain.*;
import com.brandongcobb.vyrtuous.tools.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.Collection;

public class ToolRegistry {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, CustomTool<?, ?>> tools = new HashMap<>();

    public CompletableFuture<JsonNode> callTool(String name, JsonNode arguments) {
        CustomTool<?, ?> customTool = tools.get(name);
        if (customTool == null) {
            CompletableFuture<JsonNode> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IllegalArgumentException("Tool not found: " + name));
            return failed;
        }
        try {
            Object inputObj = mapper.treeToValue(arguments, customTool.getInputClass()); // Deserialize
            
            // If your input object has setOriginalJson method, call it here:
            if (inputObj instanceof ToolInput toolInput) {
                toolInput.setOriginalJson(arguments);
            }

            CustomTool<Object, ?> typedTool = (CustomTool<Object, ?>) customTool;
            return typedTool.run(inputObj)
                .thenApply(result -> {
                    if (result instanceof ToolResult tr) {
                        return tr.getOutput();
                    } else {
                        return mapper.valueToTree(result);
                    }
                });
        } catch (Exception e) {
            CompletableFuture<JsonNode> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }


    public Collection<CustomTool<?, ?>> getTools() {
        return (Collection<CustomTool<?, ?>>) (Collection<?>) tools.values();
    }
    
    public void registerTool(CustomTool<?, ?> tool) {
        tools.put(tool.getName(), tool);
    }
}
