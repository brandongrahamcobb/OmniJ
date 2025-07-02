/*  ToolService.java The primary purpose of this class is to serve as
 *  the Model Context Protocol server for the Vyrtuous spring boot application.
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
package com.brandongcobb.vyrtuous.utils.handlers;

import com.brandongcobb.vyrtuous.*;
import com.brandongcobb.vyrtuous.domain.*;
import com.brandongcobb.vyrtuous.enums.*;
import com.brandongcobb.vyrtuous.tools.*;
import com.brandongcobb.vyrtuous.utils.handlers.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.*;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ai.tool.annotation.Tool;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.ai.chat.memory.ChatMemory;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;

@Service
public class ToolService {
    
    private final ObjectMapper mapper = new ObjectMapper();
    private final ChatMemory replChatMemory;
    
    public ToolService(ChatMemory replChatMemory) {
        this.replChatMemory = replChatMemory;
    }
    
    private static final Logger LOGGER = Logger.getLogger(Vyrtuous.class.getName());

    @Tool(name = "count_file_lines", description = "Count the number of lines in a file")
    public CompletableFuture<ToolStatus> countFileLines(CountFileLinesInput input) {
        
        return new CountFileLines(replChatMemory).run(input);
    }

    @Tool(name = "create_file", description = "Create a new file with specified content")
    public CompletableFuture<ToolStatus> createFile(CreateFileInput input) {
        
        return new CreateFile(replChatMemory).run(input);
    }

    @Tool(name = "find_in_file", description = "Provides context for found strings inside a file")
    public CompletableFuture<ToolStatus> findInFile(FindInFileInput input) {
        
        return new FindInFile(replChatMemory).run(input);
    }

    @Tool(name = "list_latex_structure", description = "List the LaTeX structure of a .tex file")
    public CompletableFuture<ToolStatus> listLatexStructure(ListLatexStructureInput input) {
        
        return new ListLatexStructure(replChatMemory).run(input);
    }

//    @Tool(name = "load_context", description = "Load context from a source")
//    public CompletableFuture<ToolStatus> loadContext(LoadContextInput input) {
//        
//        return new LoadContext(replChatMemory).run(input);
//    }

    @Tool(name = "patch", description = "Apply patches to files")
    public CompletableFuture<ToolStatus> patch(PatchInput input) {
        
        return new Patch(replChatMemory).run(input);
    }

    @Tool(name = "read_file", description = "Read the contents of a file")
    public CompletableFuture<ToolStatus> readFile(ReadFileInput input) {
        
        return new ReadFile(replChatMemory).run(input);
    }

    @Tool(name = "read_latex_segment", description = "Read a segment of LaTeXe in a .tex file")
    public CompletableFuture<ToolStatus> readLatexSegment(ReadLatexSegmentInput input) {
        return new ReadLatexSegment(replChatMemory).run(input);
    }

//    @Tool(name = "refresh_context", description = "Summarize the context")
//    public CompletableFuture<ToolStatus> refreshContext(RefreshContextInput input) {
//        return new RefreshContext(replChatMemory).run(input);
//    }

//    @Tool(name = "save_context", description = "Save current context")
//    public CompletableFuture<ToolStatus> saveContext(SaveContextInput input) {
//        
//        return new SaveContext(replChatMemory).run(input);
//    }

    @Tool(name = "search_files", description = "Search for files matching criteria")
    public CompletableFuture<ToolStatus> searchFiles(SearchFilesInput input) {
        
        return new SearchFiles(replChatMemory).run(input);
    }

    @Tool(name = "search_web", description = "Search the web for matching criteria")
    public CompletableFuture<ToolStatus> searchWeb(SearchWebInput input) {
        
        return new SearchWeb(replChatMemory).run(input);
    }

    @Tool(name = "summarize_latex_section", description = "Summarize a section in a LaTeXe document")
    public CompletableFuture<ToolStatus> summarizeLatexSection(SummarizeLatexSectionInput input) {
        
        return new SummarizeLatexSection(replChatMemory).run(input);
    }
    
    public CompletableFuture<ToolStatus> callTool(String toolName, JsonNode arguments) throws Exception {
        // Loop through all methods
        for (Method method : this.getClass().getMethods()) {
            Tool annotation = method.getAnnotation(Tool.class);
            if (annotation != null && annotation.name().equals(toolName)) {
                // Get the input parameter type of the method (assuming only one param)
                Class<?> inputType = method.getParameterTypes()[0];
                // Convert arguments JsonNode to input POJO
                Object inputObj = mapper.treeToValue(arguments, inputType);
                // Call the method reflectively (pass inputObj)
                Object result = method.invoke(this, inputObj);
                // The result should be CompletableFuture<ToolStatus>
                return (CompletableFuture<ToolStatus>) result;
            }
        }
        throw new IllegalArgumentException("Tool '" + toolName + "' not found");
    }
}

