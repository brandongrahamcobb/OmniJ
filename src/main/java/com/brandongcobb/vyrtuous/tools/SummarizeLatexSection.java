/*  SummarizeLatexSection.java The primary purpose of this class is to act as a tool
 *  for summarizing a latex section.
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
import com.brandongcobb.vyrtuous.domain.input.SummarizeLatexSectionInput;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.brandongcobb.vyrtuous.service.REPLService.printIt;

@Component
public class SummarizeLatexSection implements CustomTool<SummarizeLatexSectionInput, ToolStatus> {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final ChatMemory chatMemory;
    
    @Autowired
    public SummarizeLatexSection(ChatMemory replChatMemory) {
        this.chatMemory = replChatMemory;
    }
    
    @Override
    public Class<SummarizeLatexSectionInput> getInputClass() {
        return SummarizeLatexSectionInput.class;
    }
    
    @Override
    public String getName() {
        return "summarize_latex_section";
    }

    @Override
    public String getDescription() {
        return "Reads and summarizes a section from a LaTeX file based on line boundaries.";
    }

    @Override
    public JsonNode getJsonSchema() {
        try {
            return mapper.readTree("""
            {
              "type": "object",
              "required": ["path", "startLine", "endLine"],
              "properties": {
                "path": { "type": "string" },
                "startLine": { "type": "integer", "minimum": 0 },
                "endLine": { "type": "integer", "minimum": 0 }
              }
            }
            """);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompletableFuture<ToolStatus> run(SummarizeLatexSectionInput input) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> lines = new ArrayList<>();
            try (BufferedReader reader = Files.newBufferedReader(Paths.get(input.getPath()))) {
                int lineNum = 0;
                String line;
                while ((line = reader.readLine()) != null) {
                    if (lineNum >= input.getStartLine() && lineNum <= input.getEndLine()) {
                        lines.add(line);
                    }
                    if (lineNum > input.getEndLine()) break;
                    lineNum++;
                }
                chatMemory.add("assistant", new AssistantMessage("{\"tool\":" + "\"" + getName() + "\",\"input\":" + input.getOriginalJson().toString() + "}"));
                chatMemory.add("user", new AssistantMessage("{\"tool\":" + "\"" + getName() + "\",\"input\":" + input.getOriginalJson().toString() + "}"));
                printIt();
            } catch (IOException e) {
                return new ToolStatusWrapper("Failed: " + e.getMessage(), false);
            }

            // placeholder summary logic — replace with AI summarization
            String rawText = String.join("\n", lines);
            String summary = rawText.length() > 300 ? rawText.substring(0, 300) + "..." : rawText;

            return new ToolStatusWrapper(summary, true);
        });
    }
}
