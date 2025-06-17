/*  EnvironmentPaths.java The purpose of this program is to hold path variables for
 *  the main program.
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
package com.brandongcobb.vyrtuous.enums;

import com.brandongcobb.vyrtuous.utils.inc.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public enum EnvironmentPaths {
    
    AI_MANAGER(Helpers.PATH_AI_MANAGER),
    APPROVAL_MODE(Helpers.PATH_APPROVAL_MODE),
    COG(Helpers.PATH_COG),
    CONTEXT_ENTRY(Helpers.PATH_CONTEXT_ENTRY),
    CONTEXT_MANAGER(Helpers.PATH_CONTEXT_MANAGER),
    DISCORD_BOT(Helpers.PATH_DISCORD_BOT),
    EVENT_LISTENERS(Helpers.PATH_EVENT_LISTENERS),
    HELPERS(Helpers.PATH_HELPERS),
    HYBRID_COMMANDS(Helpers.PATH_HYBRID_COMMANDS),
    LLAMA_CONTAINER(Helpers.PATH_LLAMA_CONTAINER),
    LLAMA_UTILS(Helpers.PATH_LLAMA_UTILS),
    LMSTUDIO_CONTAINER(Helpers.PATH_LMSTUDIO_CONTAINER),
    LMSTUDIO_UTILS(Helpers.PATH_LMSTUDIO_UTILS),
    MAIN_CONTAINER(Helpers.PATH_MAIN_CONTAINER),
    MARKDOWN_CONTAINER(Helpers.PATH_MARKDOWN_CONTAINER),
    MARKDOWN_UTILS(Helpers.PATH_MARKDOWN_UTILS),
    MESSAGE_MANAGER(Helpers.PATH_MESSAGE_MANAGER),
    MODEL_INFO(Helpers.PATH_MODEL_INFO),
    MODEL_REGISTRY(Helpers.PATH_MODEL_REGISTRY),
    MODERATION_MANAGER(Helpers.PATH_MODERATION_MANAGER),
    OLLAMA_CONTAINER(Helpers.PATH_OLLAMA_CONTAINER),
    OLLAMA_UTILS(Helpers.PATH_OLLAMA_UTILS),
    OPENAI_CONTAINER(Helpers.PATH_OPENAI_CONTAINER),
    OPENAI_UTILS(Helpers.PATH_OPENAI_UTILS),
    OPENROUTER_CONTAINER(Helpers.PATH_OPENROUTER_CONTAINER),
    OPENROUTER_UTILS(Helpers.PATH_OPENROUTER_UTILS),
    PREDICATOR(Helpers.PATH_PREDICATOR),
    PROJECT_LOADER(Helpers.PATH_PROJECT_LOADER),
    REPL_MANAGER(Helpers.PATH_REPL_MANAGER),
    SCHEMA_MERGER(Helpers.PATH_SCHEMA_MERGER),
    SOURCE(Helpers.PATH_SOURCE),
    STRUCTURED_OUTPUT(Helpers.PATH_STRUCTURED_OUTPUT),
    TOOL_CONTAINER(Helpers.PATH_TOOL_CONTAINER),
    TOOL_HANDLER(Helpers.PATH_TOOL_HANDLER),
    TOOL_UTILS(Helpers.PATH_TOOL_UTILS),
    VYRTUOUS(Helpers.PATH_VYRTUOUS);


    private final Path path;

    EnvironmentPaths(Path relativePath) {
        this.path = Helpers.DIR_BASE.resolve(relativePath);
    }

    Path get() {
        return path;
    }
}
