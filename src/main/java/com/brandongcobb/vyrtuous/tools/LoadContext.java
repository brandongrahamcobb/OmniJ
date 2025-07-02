///*  LoadContext.java The primary purpose of this class is to act as a tool
// *  for loading context snapshots.
// *
// *  Copyright (C) 2025  github.com/brandongrahamcobb
// *
// *  This program is free software: you can redistribute it and/or modify
// *  it under the terms of the GNU General Public License as published by
// *  the Free Software Foundation, either version 3 of the License, or
// *  (at your option) any later version.
// *
// *  This program is distributed in the hope that it will be useful,
// *  but WITHOUT ANY WARRANTY; without even the implied warranty of
// *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// *  GNU General Public License for more details.
// *
// *  You should have received a copy of the GNU General Public License
// *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
// */
//package com.brandongcobb.vyrtuous.tools;
//
//import com.brandongcobb.vyrtuous.domain.*;
//import com.brandongcobb.vyrtuous.objects.*;
//import com.brandongcobb.vyrtuous.utils.handlers.*;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import java.util.concurrent.CompletableFuture;
//import org.springframework.ai.chat.memory.ChatMemory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.ai.chat.messages.UserMessage;
//import org.springframework.ai.chat.messages.AssistantMessage;
//import org.springframework.ai.chat.messages.SystemMessage;
//import org.springframework.ai.chat.messages.Message;
//import static com.brandongcobb.vyrtuous.utils.handlers.REPLManager.printIt;
//
//@Component
//public class LoadContext implements CustomTool<LoadContextInput, ToolStatus> {
//
//    private final ContextManager modelContextManager;
//    private final ContextManager userContextManager;
//    private final ChatMemory chatMemory;
//    
//    @Autowired
//    public LoadContext(ChatMemory replChatMemory) {
//        this.chatMemory = replChatMemory;
//    }
//    /*
//     *  Getters
//     */
//    @Override
//    public String getDescription() {
//        return "Loads a context snapshot to build the snapshot";
//    }
//
//    @Override
//    public JsonNode getJsonSchema() {
//        try {
//            return mapper.readTree("""
//            {
//              "type": "object",
//              "required": ["name"],
//              "properties": {
//                "name": {
//                  "type": "string",
//                  "description": "The unique name of the previously saved context snapshot to load."
//                }
//              },
//              "additionalProperties": false
//            }
//            """);
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to build load_context schema", e);
//        }
//    }
//
//    @Override
//    public String getName() {
//        return "load_context";
//    }
//    
//    /*
//     *  Tool
//     */
//    @Override
//    public CompletableFuture<ToolStatus> run(LoadContextInput input) {
//        return CompletableFuture.supplyAsync(() -> {
//            try {
//                List<Message> history = chatMemory.get("assistant");
//                for (Message msg : history) {
//                    if (msg instanceof SystemMessage sys && sys.getMetadata().get("snapshot") == Boolean.TRUE) {
//                        if (sys.getMetadata().get("name").equals("my_snapshot_name")) {
//                            String json = sys.getText();
//                            Snapshot snap = mapper.readValue(json, Snapshot.class);
//                            // use snap.entries and snap.description
//                        }
//                    }
//                }
//                chatMemory.add("assistant", new AssistantMessage("{\"tool\":" + "\"" + getName() + "\",\"input\":" + input.getOriginalJson().toString() + "}"));
//                chatMemory.add("user", new AssistantMessage("{\"tool\":" + "\"" + getName() + "\",\"input\":" + input.getOriginalJson().toString() + "}"));
//                printIt();
//                return new ToolStatusWrapper(msg, true);
//            } catch (Exception e) {
//                return new ToolStatusWrapper("Failed to load context: " + e.getMessage(), false);
//            }
//        });
//    }
//}
