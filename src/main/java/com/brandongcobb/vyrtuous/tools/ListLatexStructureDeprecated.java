//
//  ListLatexStructure.swift
//  
//
//  Created by Brandon Cobb on 7/1/25.
//

package com.brandongcobb.vyrtuous.tools;
import com.brandongcobb.vyrtuous.domain.*;
import com.brandongcobb.vyrtuous.objects.*;
import com.brandongcobb.vyrtuous.utils.handlers.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.BufferedReader;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;


import java.io.IOException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import java.util.stream.Collectors;

public class ListLatexStructureDeprecated implements CustomTool<ListLatexStructureInput, ToolStatus> {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final ContextManager modelContextManager;
    private final ContextManager userContextManager;
    
    public ListLatexStructureDeprecated(ContextManager modelContextManager, ContextManager userContextManager) {
        this.modelContextManager = modelContextManager;
        this.userContextManager = userContextManager;
    }
    
    @Override
    public String getName() {
        return "list_latex_structure";
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
