///*  SaveContext.java The primary purpose of this class is to act as a tool
// *  for making saving context snapshots.
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
//import com.fasterxml.jackson.databind.SerializationFeature;
//
//@Component
//public class SaveContext implements CustomTool<SaveContextInput, ToolStatus> {
//
//    private static final ObjectMapper mapper = new ObjectMapper();
//    private final ChatMemory chatMemory;
//    private ContextManager contextManager;
//    
//    @Autowired
//    public SaveContext(ChatMemory replChatMemory) {
//        this.chatMemory = replChatMemory;
//    }
//    /*
//     *  Getters
//     */
//    @Override
//    public String getDescription() {
//        return "Saves the current state under a given name for later recall using load_context.";
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
//                  "description": "A unique identifier for the context snapshot."
//                },
//                "description": {
//                  "type": "string",
//                  "description": "Optional description or annotation for the snapshot."
//                }
//              },
//              "additionalProperties": false
//            }
//            """);
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to build save_context schema", e);
//        }
//    }
//    
//    @Override
//    public String getName() {
//        return "save_context";
//    }
//    
//    /*
//     * Tool
//     */
//    @Override
//    public CompletableFuture<ToolStatus> run(SaveContextInput input) {
//        return CompletableFuture.supplyAsync(() -> {
//            try {
//                // Create your snapshot object
//                Snapshot snapshot = new Snapshot();
//                snapshot.description = input.getDescription();
//                snapshot.entries = new ArrayList<>(); // Or pull from somewhere if needed
//
//                // Serialize to JSON
//                ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
//                String snapshotJson = mapper.writeValueAsString(snapshot);
//
//                // Wrap in a message with metadata to tag it as a snapshot
//                Message snapshotMsg = new SystemMessage(snapshotJson, Map.of(
//                    "snapshot", true,
//                    "name", input.getName(),
//                    "description", input.getDescription()
//                ));
//
//                chatMemory.add("assistant", snapshotMsg); // Save to memory
//                chatMemory.add("user", snapshotMsg);
//                
//                String confirm = "Snapshot '" + input.getName() + "' saved in chat memory.";
//                return new ToolStatusWrapper(confirm, true);
//            } catch (Exception e) {
//                return new ToolStatusWrapper("Failed to save context snapshot: " + e.getMessage(), false);
//            }
//        });
//    }
//    
//    /*
//     *  Nested class
//     */
//    private static class Snapshot {
//        public String description;
//        public List<ContextEntry> entries;
//    }
//}
