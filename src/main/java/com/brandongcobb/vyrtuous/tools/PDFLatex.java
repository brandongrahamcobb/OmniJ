/*  PDFLatex.java
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

import com.brandongcobb.vyrtuous.*;
import com.brandongcobb.vyrtuous.domain.*;
import com.brandongcobb.vyrtuous.domain.input.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

@Component
public class PDFLatex implements CustomTool<PDFLatexInput, ToolStatus> {

    private static final Logger LOGGER = Logger.getLogger(Vyrtuous.class.getName());
    private static final ObjectMapper mapper = new ObjectMapper();
    private final ChatMemory chatMemory;

    @Autowired
    public PDFLatex(ChatMemory replChatMemory) {
        this.chatMemory = replChatMemory;
    }

    @Override
    public String getDescription() {
        return "Compiles a LaTeX file to PDF using `pdflatex`, returning success or error.";
    }

    @Override
    public JsonNode getJsonSchema() {
        try {
            String schemaJson = """
            {
              "type": "object",
              "required": ["path"],
              "properties": {
                "path": {
                  "type": "string",
                  "description": "Path to the LaTeX .tex file to compile."
                },
                "outputDirectory": {
                  "type": "string",
                  "description": "Optional output directory for the PDF file."
                }
              },
              "additionalProperties": false
            }
            """;
            return mapper.readTree(schemaJson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build pidflatex schema", e);
        }
    }

    @Override
    public Class<PDFLatexInput> getInputClass() {
        return PDFLatexInput.class;
    }

    @Override
    public String getName() {
        return "pdflatex";
    }

    @Override
    public CompletableFuture<ToolStatus> run(PDFLatexInput input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path texPath = Paths.get(input.getPath()).toAbsolutePath();
                if (!Files.exists(texPath) || !texPath.toString().endsWith(".tex")) {
                    return new ToolStatusWrapper("Invalid LaTeX file: " + texPath, false);
                }

                Path workingDir = texPath.getParent();
                ProcessBuilder builder = new ProcessBuilder(
                    "pdflatex",
                    "-interaction=nonstopmode",
                    "-halt-on-error",
                    texPath.getFileName().toString()
                );
                builder.directory(workingDir.toFile());
                builder.redirectErrorStream(true);

                Process process = builder.start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    StringBuilder outputLog = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        outputLog.append(line).append("\n");
                    }

                    int exitCode = process.waitFor();
                    if (exitCode == 0) {
                        String pdfPath = texPath.toString().replace(".tex", ".pdf");
                        chatMemory.add("assistant", new AssistantMessage("{\"tool\":\"" + getName() + "\",\"arguments\":" + input.getOriginalJson() + "}"));
                        return new ToolStatusWrapper("PDF created: " + pdfPath, true);
                    } else {
                        return new ToolStatusWrapper("LaTeX compilation failed:\n" + outputLog, false);
                    }
                }
            } catch (Exception e) {
                return new ToolStatusWrapper("Error during pdflatex execution: " + e.getMessage(), false);
            }
        });
    }
}
