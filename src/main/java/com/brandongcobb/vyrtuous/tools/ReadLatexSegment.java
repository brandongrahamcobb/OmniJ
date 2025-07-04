/*  ReadLatexSegmentjava The primary purpose of this class is to act as a tool
 *  for reading a segment of text in a LaTeXe document.
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

import com.brandongcobb.vyrtuous.domain.ToolStatus;
import com.brandongcobb.vyrtuous.domain.ToolStatusWrapper;
import com.brandongcobb.vyrtuous.domain.input.ReadLatexSegmentInput;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.brandongcobb.vyrtuous.service.REPLService.printIt;

@Component
public class ReadLatexSegment implements CustomTool<ReadLatexSegmentInput, ToolStatus> {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final ChatMemory chatMemory;

    @Autowired
    public ReadLatexSegment(ChatMemory replChatMemory) {
        this.chatMemory = replChatMemory;
    }
    
    @Override
    public String getDescription() {
        return "Reads a segment of a LaTeX file without loading the full document.";
    }
    
    @Override
    public Class<ReadLatexSegmentInput> getInputClass() {
        return ReadLatexSegmentInput.class;
    }

    @Override
    public JsonNode getJsonSchema() {
        try {
            return mapper.readTree("""
            {
              "type": "object",
              "required": ["path", "startLine", "numLines"],
              "properties": {
                "path": {
                  "type": "string",
                  "description": "Path to the LaTeX file."
                },
                "startLine": {
                  "type": "integer",
                  "minimum": 0,
                  "description": "Starting line number (0-indexed)."
                },
                "numLines": {
                  "type": "integer",
                  "minimum": 1,
                  "description": "Number of lines to read."
                }
              }
            }
            """);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return "read_latex_segment";
    }

    @Override
    public CompletableFuture<ToolStatus> run(ReadLatexSegmentInput input) {
        return CompletableFuture.supplyAsync(() -> {
            Path path = Paths.get(input.getPath());
            List<String> lines;
            try (Stream<String> stream = Files.lines(path)) {
                lines = stream
                    .skip(input.getStartLine())
                    .limit(input.getNumLines())
                    .collect(Collectors.toList());
                chatMemory.add("assistant", new AssistantMessage("{\"tool\":" + "\"" + getName() + "\",\"arguments\":" + input.getOriginalJson().toString() + "}"));
                chatMemory.add("user", new AssistantMessage("{\"tool\":" + "\"" + getName() + "\",\"arguments\":" + input.getOriginalJson().toString() + "}"));
                printIt();
            } catch (IOException e) {
                return new ToolStatusWrapper("Error", false);
            }

            return new ToolStatusWrapper(lines.toString(), true);
        });
    }
}
