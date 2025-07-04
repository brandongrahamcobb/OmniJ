/*  DiffFiles.java The primary purpose of this class is to act as a tool
 *  for showing the differences between two files.
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
import com.brandongcobb.vyrtuous.domain.input.DiffFilesInput;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.brandongcobb.vyrtuous.service.REPLService.printIt;

public class DiffFiles implements CustomTool<DiffFilesInput, ToolStatus> {

    private final ObjectMapper mapper = new ObjectMapper();
    private final ChatMemory chatMemory;

    @Autowired
    public DiffFiles(ChatMemory replChatMemory) {
        this.chatMemory = replChatMemory;
    }
    
    @Override
    public String getDescription() {
        return "Compares two files line-by-line and returns the differences.";
    }

    @Override
    public JsonNode getJsonSchema() {
        try {
            String schemaJson = """
            {
              "type": "object",
              "required": ["path1", "path2"],
              "properties": {
                "path1": {
                  "type": "string",
                  "description": "Path to the first file."
                },
                "path2": {
                  "type": "string",
                  "description": "Path to the second file."
                }
              },
              "additionalProperties": false
            }
            """;
            return mapper.readTree(schemaJson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build diff_files schema", e);
        }
    }
    
    @Override
    public Class<DiffFilesInput> getInputClass() {
        return DiffFilesInput.class;
    }
    
    @Override
    public String getName() {
        return "diff_files";
    }

    @Override
    public CompletableFuture<ToolStatus> run(DiffFilesInput input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<String> file1Lines = Files.readAllLines(Paths.get(input.getPath1()));
                List<String> file2Lines = Files.readAllLines(Paths.get(input.getPath2()));
                int maxLines = Math.max(file1Lines.size(), file2Lines.size());
                List<ObjectNode> diffs = IntStream.range(0, maxLines)
                    .filter(i -> {
                        String line1 = i < file1Lines.size() ? file1Lines.get(i) : "";
                        String line2 = i < file2Lines.size() ? file2Lines.get(i) : "";
                        return !line1.equals(line2);
                    })
                    .mapToObj(i -> {
                        ObjectNode diffEntry = mapper.createObjectNode();
                        diffEntry.put("line", i + 1);
                        diffEntry.put("file1", i < file1Lines.size() ? file1Lines.get(i) : "");
                        diffEntry.put("file2", i < file2Lines.size() ? file2Lines.get(i) : "");
                        return diffEntry;
                    })
                    .collect(Collectors.toList());
                ObjectNode result = mapper.createObjectNode();
                result.put("path1", input.getPath1());
                result.put("path2", input.getPath2());
                result.put("diffCount", diffs.size());
                result.set("diffs", mapper.valueToTree(diffs));
                chatMemory.add("assistant", new AssistantMessage("{\"tool\":\"" + getName() + "\",\"input\":" + input.getOriginalJson() + ",\"output\":" + result.toString() + "}"));
                chatMemory.add("user", new AssistantMessage("{\"tool\":\"" + getName() + "\",\"input\":" + input.getOriginalJson() + "}"));
                printIt();
                return new ToolStatusWrapper("Diff computed successfully.", true);
            } catch (IOException e) {
                return new ToolStatusWrapper("IO error: " + e.getMessage(), false);
            } catch (Exception e) {
                return new ToolStatusWrapper("Unexpected error: " + e.getMessage(), false);
            }
        });
    }

}
