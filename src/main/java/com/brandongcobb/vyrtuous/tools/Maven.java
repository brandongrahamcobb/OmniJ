/*  Maven.java The primary purpose of this class is to act as a tool
 *  for executing Maven commands.
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
import com.brandongcobb.vyrtuous.domain.input.MavenInput;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import static com.brandongcobb.vyrtuous.service.REPLService.printIt;

@Component
public class Maven implements CustomTool<MavenInput, ToolStatus> {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final ChatMemory chatMemory;

    @Autowired
    public Maven(ChatMemory chatMemory) {
        this.chatMemory = chatMemory;
    }

    @Override
    public String getName() {
        return "maven";
    }

    @Override
    public String getDescription() {
        return "Runs a Maven command (clean, install, test, etc.) inside the current project directory.";
    }

    @Override
    public JsonNode getJsonSchema() {
        try {
            return mapper.readTree("""
            {
              "type": "object",
              "required": ["goal"],
              "properties": {
                "goal": {
                  "type": "string",
                  "description": "The Maven goal to execute, such as 'clean', 'install', 'test', etc."
                },
                "arguments": {
                  "type": "array",
                  "items": {
                    "type": "string"
                  },
                  "description": "Optional list of additional arguments to pass to Maven"
                }
              },
              "additionalProperties": false
            }
            """);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Maven schema", e);
        }
    }

    @Override
    public Class<MavenInput> getInputClass() {
        return MavenInput.class;
    }

    @Override
    public CompletableFuture<ToolStatus> run(MavenInput input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String goal = input.getGoal();
                List<String> arguments = input.getArguments() != null ? input.getArguments() : List.of();

                List<String> command = new ArrayList<>();
                command.add("mvn");
                command.add(goal);
                command.addAll(arguments);

                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.directory(new File(System.getProperty("user.dir"))); // Optional: set working directory
                processBuilder.redirectErrorStream(true); // Combine stderr into stdout

                Process process = processBuilder.start();

                StringBuilder output = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append(System.lineSeparator());
                    }
                }

                int exitCode = process.waitFor();
                String result = output.toString().trim();

                // Record invocation in memory
                chatMemory.add("assistant", new AssistantMessage("{\"tool\":\"maven\",\"arguments\":" + input.getOriginalJson().toString() + "}"));
                chatMemory.add("user", new AssistantMessage( "{\"tool\":\"maven\",\"arguments\":" + input.getOriginalJson().toString() + "}"));

                if (exitCode == 0) {
                    return new ToolStatusWrapper(result, true);
                } else {
                    return new ToolStatusWrapper("Maven command failed with exit code " + exitCode + ":\n\n" + result, false);
                }

            } catch (Exception e) {
                return new ToolStatusWrapper("Error running Maven command: " + e.getMessage(), false);
            }
        });
    }

}
