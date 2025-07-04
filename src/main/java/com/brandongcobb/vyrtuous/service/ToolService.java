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
package com.brandongcobb.vyrtuous.service;

import com.brandongcobb.vyrtuous.Vyrtuous;
import com.brandongcobb.vyrtuous.domain.ToolStatus;
import com.brandongcobb.vyrtuous.domain.input.*;
import com.brandongcobb.vyrtuous.tools.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

@Service
public class ToolService {
    
    private static final Logger LOGGER = Logger.getLogger(Vyrtuous.class.getName());
    private final ObjectMapper mapper = new ObjectMapper();
    private final ChatMemory replChatMemory;
    private final Map<String, CustomTool<?, ?>> tools = new HashMap<>();

    public ToolService(ChatMemory replChatMemory) {
        this.replChatMemory = replChatMemory;
    }
    
    public CompletableFuture<JsonNode> callTool(String name, JsonNode arguments) {
        CustomTool<?, ?> customTool = tools.get(name);
        if (customTool == null) {
            CompletableFuture<JsonNode> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IllegalArgumentException("Tool not found: " + name));
            return failed;
        }
        try {
            Object inputObj = mapper.treeToValue(arguments, customTool.getInputClass()); // Deserialize
            if (inputObj instanceof ToolInput toolInput) {
                toolInput.setOriginalJson(arguments);
            }
            CustomTool<Object, ?> typedTool = (CustomTool<Object, ?>) customTool;
            return typedTool.run(inputObj)
                .thenApply(result -> {
                    if (result instanceof ToolResult tr) {
                        return tr.getOutput();
                    } else {
                        return mapper.valueToTree(result);
                    }
                });
        } catch (Exception e) {
            CompletableFuture<JsonNode> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }

    public Collection<CustomTool<?, ?>> getTools() {
        return tools.values();
    }
    
    public void registerTool(CustomTool<?, ?> tool) {
        tools.put(tool.getName(), tool);
    }

    /*
     *  Tools
     */
    @Tool(name = "count_file_lines", description = "Count the number of lines in a file")
    public CompletableFuture<ToolStatus> countFileLines(CountFileLinesInput input) {
        return new CountFileLines(replChatMemory).run(input);
    }

    @Tool(name = "create_file", description = "Create a new file with specified content")
    public CompletableFuture<ToolStatus> createFile(CreateFileInput input) {
        return new CreateFile(replChatMemory).run(input);
    }

    @Tool(name = "diff_files", description = "Shows the differences between two files.")
    public CompletableFuture<ToolStatus> diffFiles(DiffFilesInput input) {
        return new DiffFiles(replChatMemory).run(input);
    }

    @Tool(name = "find_in_file", description = "Provides context for found strings inside a file")
    public CompletableFuture<ToolStatus> findInFile(FindInFileInput input) {
        return new FindInFile(replChatMemory).run(input);
    }

    @Tool(name = "list_latex_structure", description = "List the LaTeX structure of a .tex file")
    public CompletableFuture<ToolStatus> listLatexStructure(ListLatexStructureInput input) {
        return new ListLatexStructure(replChatMemory).run(input);
    }

    @Tool(name = "maven", description = "Execute a Maven command")
    public CompletableFuture<ToolStatus> maven(MavenInput input) {
        return new Maven(replChatMemory).run(input);
    }
    
    @Tool(name = "patch", description = "Apply patches to files")
    public CompletableFuture<ToolStatus> patch(PatchInput input) {
        return new Patch(replChatMemory).run(input);
    }

    @Tool(name = "read_file", description = "Read the contents of a file")
    public CompletableFuture<ToolStatus> readFile(ReadFileInput input) {
        return new ReadFile(replChatMemory).run(input);
    }

    @Tool(name = "read_file_line", description = "Read the contents of a file")
    public CompletableFuture<ToolStatus> readFileLine(ReadFileLinesInput input) {
        return new ReadFileLines(replChatMemory).run(input);
    }
    
    @Tool(name = "read_latex_segment", description = "Read a segment of LaTeXe in a .tex file")
    public CompletableFuture<ToolStatus> readLatexSegment(ReadLatexSegmentInput input) {
        return new ReadLatexSegment(replChatMemory).run(input);
    }

    @Tool(name = "search_files", description = "Search for files matching criteria")
    public CompletableFuture<ToolStatus> searchFiles(SearchFilesInput input) {
        return new SearchFiles(replChatMemory).run(input);
    }

    @Tool(name = "search_web", description = "Search the web for matching criteria")
    public CompletableFuture<ToolStatus> searchWeb(SearchWebInput input) {
        return new SearchWeb(replChatMemory).run(input);
    }
    
//    public CompletableFuture<ToolStatus> callTool(String toolName, JsonNode arguments) throws Exception {
//        for (Method method : this.getClass().getMethods()) {
//            Tool annotation = method.getAnnotation(Tool.class);
//            if (annotation != null && annotation.name().equals(toolName)) {
//                Class<?> inputType = method.getParameterTypes()[0];
//                Object inputObj = mapper.treeToValue(arguments, inputType);
//                Object result = method.invoke(this, inputObj);
//                return (CompletableFuture<ToolStatus>) result;
//            }
//        }
//        throw new IllegalArgumentException("Tool '" + toolName + "' not found");
//    }
}

