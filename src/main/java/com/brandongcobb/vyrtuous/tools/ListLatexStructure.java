/*  ListLatexStructure.java The primary purpose of this class is to act as a tool
 *  for listing the structure of a LaTeXe document.
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

import com.brandongcobb.vyrtuous.domain.input.ListLatexStructureInput;
import com.brandongcobb.vyrtuous.domain.ToolStatus;
import com.brandongcobb.vyrtuous.domain.ToolStatusWrapper;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.brandongcobb.vyrtuous.service.REPLService.printIt;

@Component
public class ListLatexStructure implements CustomTool<ListLatexStructureInput, ToolStatus> {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final ChatMemory chatMemory;
    
    @Autowired
    public ListLatexStructure(ChatMemory replChatMemory) {
        this.chatMemory = replChatMemory;
    }

    @Override
    public String getName() {
        return "list_latex_structure";
    }

    @Override
    public Class<ListLatexStructureInput> getInputClass() {
        return ListLatexStructureInput.class;
    }
    
    @Override
    public String getDescription() {
        return "Parses the LaTeX file for sectioning commands and returns their structure with line numbers.";
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
                  "description": "Path to the LaTeX file to parse."
                }
              }
            }
            """);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompletableFuture<ToolStatus> run(ListLatexStructureInput input) {
        return CompletableFuture.supplyAsync(() -> {
            List<LatexStructureEntry> structure = new ArrayList<>();
            String message;
            try (BufferedReader reader = Files.newBufferedReader(Paths.get(input.getPath()))) {
                String line;
                int lineNumber = 0;
                Pattern pattern = Pattern.compile("\\\\(chapter|section|subsection|subsubsection)\\{(.+?)\\}");
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        structure.add(new LatexStructureEntry(
                            matcher.group(1),
                            matcher.group(2),
                            lineNumber
                        ));
                    }
                    lineNumber++;
                }
                StringBuilder sb = new StringBuilder();
                for (LatexStructureEntry entry : structure) {
                    sb.append(String.format("[%d] \\%s{%s}\n", entry.getLine(), entry.getCommand(), entry.getTitle()));
                }
                message = sb.toString().trim();
                
                chatMemory.add("assistant", new AssistantMessage("{\"tool\":" + "\"" + getName() + "\",\"arguments\":" + input.getOriginalJson().toString() + "}"));
                chatMemory.add("user", new AssistantMessage("{\"tool\":" + "\"" + getName() + "\",\"arguments\":" + input.getOriginalJson().toString() + "}"));
                printIt();
            } catch (IOException e) {
                return new ToolStatusWrapper("Failed: " + e.getMessage(), false);
            }
            
            return new ToolStatusWrapper(message, true);
        });
    }
    
    public class LatexStructureEntry {
        private String command; // section, subsection, etc.
        private String title;
        private int line;
        
        public LatexStructureEntry(String command, String title, int line) {
            this.command = command;
            this.title = title;
            this.line = line;
        }
        
        public String getCommand() {
            return this.command;
        }
        
        public int getLine() {
            return this.line;
        }
        
        
        public String getTitle() {
            return this.title;
        }// constructor, getters
    }

}
