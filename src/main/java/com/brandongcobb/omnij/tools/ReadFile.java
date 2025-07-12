/*  Patch.java The primary purpose of this class is to act as a tool
 *  for patching file contents.
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
package com.brandongcobb.omnij.tools;

import com.brandongcobb.omnij.domain.ToolStatus;
import com.brandongcobb.omnij.domain.ToolStatusWrapper;
import com.brandongcobb.omnij.domain.input.ReadFileInput;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.concurrent.CompletableFuture;

@Component
public class ReadFile implements CustomTool<ReadFileInput, ToolStatus> {
    
    private static final ObjectMapper mapper = new ObjectMapper();
    private final ChatMemory chatMemory;

    @Autowired
    public ReadFile(ChatMemory replChatMemory) {
        this.chatMemory = replChatMemory;
    }

    /*
     *  Getters
     */
    @Override
    public String getDescription() {
        return "Reads and returns the contents of a file.";
    }
    
    @Override
    public Class<ReadFileInput> getInputClass() {
        return ReadFileInput.class;
    }

    @Override
    public JsonNode getJsonSchema() {
        try {
            return mapper.readTree("""
            {
              "type": "object",
              "required": ["path"],
              "properties": {
                "path": {
                  "type": "string",
                  "description": "The path to the file to be read."
                }
              },
              "additionalProperties": false
            }
            """);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build read_file schema", e);
        }
    }

    @Override
    public String getName() {
        return "read_file";
    }
    
    /*
     * Tool
     */
    @Override
    public CompletableFuture<ToolStatus> run(ReadFileInput input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path filePath = Paths.get(input.getPath());
                if (!Files.exists(filePath)) {
                    return new ToolStatusWrapper("File not found: " + filePath, false);
                }
                byte[] rawBytes = Files.readAllBytes(filePath);
                CharsetDecoder decoder = StandardCharsets.UTF_8
                        .newDecoder()
                        .onMalformedInput(CodingErrorAction.REPLACE)
                        .onUnmappableCharacter(CodingErrorAction.REPLACE);

                CharBuffer decodedBuffer = decoder.decode(ByteBuffer.wrap(rawBytes));
                String safeContent = decodedBuffer.toString();
                safeContent = Normalizer.normalize(safeContent, Normalizer.Form.NFC);
                chatMemory.add("assistant", new AssistantMessage("{\"tool\":" + "\"" + getName() + "\",\"arguments\":" + input.getOriginalJson().toString() + "}"));
                chatMemory.add("user", new AssistantMessage("{\"tool\":" + "\"" + getName() + "\",\"arguments\":" + input.getOriginalJson().toString() + "}"));
                return new ToolStatusWrapper(safeContent, true);
            } catch (IOException e) {
                return new ToolStatusWrapper("IO error: " + e.getMessage(), false);
            } catch (Exception e) {
                return new ToolStatusWrapper("Unexpected error: " + e.getMessage(), false);
            }
        });
    }
}
