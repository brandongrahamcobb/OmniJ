//
//  ReadLatexSegment.swift
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
import java.util.stream.Stream;
import java.util.stream.Collectors;

public class ReadLatexSegmentDeprecated implements CustomTool<ReadLatexSegmentInput, ToolStatus> {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final ContextManager modelContextManager;
    private final ContextManager userContextManager;
    
    public ReadLatexSegmentDeprecated(ContextManager modelContextManager, ContextManager userContextManager) {
        this.modelContextManager = modelContextManager;
        this.userContextManager = userContextManager;
    }
    
    @Override
    public String getDescription() {
        return "Reads a segment of a LaTeX file without loading the full document.";
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
            } catch (IOException e) {
                return new ToolStatusWrapper("Error", false);
            }

            return new ToolStatusWrapper(lines.toString(), true);
        });
    }
}
