//
//  SummarizeLatexSection.swift
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

import java.nio.file.*;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.BufferedReader;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import java.io.IOException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletableFuture;

public class SummarizeLatexSectionDeprecated implements CustomTool<SummarizeLatexSectionInput, ToolStatus> {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final ContextManager modelContextManager;
    private final ContextManager userContextManager;
    
    public SummarizeLatexSectionDeprecated(ContextManager modelContextManager, ContextManager userContextManager) {
        this.modelContextManager = modelContextManager;
        this.userContextManager = userContextManager;
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
