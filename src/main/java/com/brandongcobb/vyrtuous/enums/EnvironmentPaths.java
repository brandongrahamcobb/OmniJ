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

import com.brandongcobb.vyrtuous.utils.inc.Helpers;

import java.nio.file.Path;

public enum EnvironmentPaths {
    
    AI_SERVICE(Helpers.PATH_AI_SERVICE),
    COG(Helpers.PATH_COG),
    CONTEXT_ENTRY(Helpers.PATH_CONTEXT_ENTRY),
    CONTEXT_MANAGER(Helpers.PATH_CONTEXT_MANAGER),
    CREATE_FILE(Helpers.PATH_CREATE_FILE),
    CREATE_FILE_INPUT(Helpers.PATH_CREATE_FILE_INPUT),
    CREATE_FILE_STATUS(Helpers.PATH_CREATE_FILE_STATUS),
    DISCORD_BOT(Helpers.PATH_DISCORD_BOT),
    EVENT_LISTENERS(Helpers.PATH_EVENT_LISTENERS),
    HELPERS(Helpers.PATH_HELPERS),
    HYBRID_COMMANDS(Helpers.PATH_HYBRID_COMMANDS),
    LLAMA_CONTAINER(Helpers.PATH_LLAMA_CONTAINER),
    LLAMA_UTILS(Helpers.PATH_LLAMA_UTILS),
    LMSTUDIO_CONTAINER(Helpers.PATH_LMSTUDIO_CONTAINER),
    LMSTUDIO_UTILS(Helpers.PATH_LMSTUDIO_UTILS),
    LOAD_CONTEXT(Helpers.PATH_LOAD_CONTEXT),
    LOAD_CONTEXT_INPUT(Helpers.PATH_LOAD_CONTEXT_INPUT),
    LOAD_CONTEXT_STATUS(Helpers.PATH_LOAD_CONTEXT_STATUS),
    MAIN_CONTAINER(Helpers.PATH_MAIN_CONTAINER),
    MARKDOWN_CONTAINER(Helpers.PATH_MARKDOWN_CONTAINER),
    MARKDOWN_UTILS(Helpers.PATH_MARKDOWN_UTILS),
    MESSAGE_SERVICE(Helpers.PATH_MESSAGE_SERVICE),
    MODEL_INFO(Helpers.PATH_MODEL_INFO),
    MODEL_REGISTRY(Helpers.PATH_MODEL_REGISTRY),
    MODERATION_SERVICE(Helpers.PATH_MODERATION_SERVICE),
    OLLAMA_CONTAINER(Helpers.PATH_OLLAMA_CONTAINER),
    OLLAMA_UTILS(Helpers.PATH_OLLAMA_UTILS),
    OPENAI_CONTAINER(Helpers.PATH_OPENAI_CONTAINER),
    OPENAI_UTILS(Helpers.PATH_OPENAI_UTILS),
    OPENROUTER_CONTAINER(Helpers.PATH_OPENROUTER_CONTAINER),
    OPENROUTER_UTILS(Helpers.PATH_OPENROUTER_UTILS),
    PATCH(Helpers.PATH_PATCH),
    PATCH_INPUT(Helpers.PATH_PATCH_INPUT),
    PATCH_OPERATION(Helpers.PATH_PATCH_OPERATION),
    PATCH_STATUS(Helpers.PATH_PATCH_STATUS),
    PREDICATOR(Helpers.PATH_PREDICATOR),
    PROJECT_LOADER(Helpers.PATH_PROJECT_LOADER),
    READ_FILE(Helpers.PATH_READ_FILE),
    READ_FILE_INPUT(Helpers.PATH_READ_FILE_INPUT),
    READ_FILE_STATUS(Helpers.PATH_READ_FILE_STATUS),
    REFRESH_CONTEXT(Helpers.PATH_REFRESH_CONTEXT),
    REFRESH_CONTEXT_INPUT(Helpers.PATH_REFRESH_CONTEXT_INPUT),
    REFRESH_CONTEXT_STATUS(Helpers.PATH_REFRESH_CONTEXT_STATUS),
    REPL_MANAGER(Helpers.PATH_REPL_MANAGER),
    SAVE_CONTEXT(Helpers.PATH_SAVE_CONTEXT),
    SAVE_CONTEXT_INPUT(Helpers.PATH_SAVE_CONTEXT_INPUT),
    SAVE_CONTEXT_STATUS(Helpers.PATH_SAVE_CONTEXT_STATUS),
    SCHEMA_MERGER(Helpers.PATH_SCHEMA_MERGER),
    SEARCH_FILES(Helpers.PATH_SEARCH_FILES),
    SEARCH_FILES_INPUT(Helpers.PATH_SEARCH_FILES_INPUT),
    SEARCH_FILES_STATUS(Helpers.PATH_SEARCH_FILES_STATUS),
    SERVER_REQUEST(Helpers.PATH_SERVER_REQUEST),
    SHELL(Helpers.PATH_SHELL),
    SHELL_INPUT(Helpers.PATH_SHELL_INPUT),
    SHELL_STATUS(Helpers.PATH_SHELL_STATUS),
    SOURCE(Helpers.PATH_SOURCE),
    STRUCTURED_OUTPUT(Helpers.PATH_STRUCTURED_OUTPUT),
    TOOL(Helpers.PATH_TOOL),
    TOOL_CONTAINER(Helpers.PATH_TOOL_CONTAINER),
    TOOL_HANDLER(Helpers.PATH_TOOL_HANDLER),
    TOOL_INPUT(Helpers.PATH_TOOL_INPUT),
    TOOL_STATUS(Helpers.PATH_TOOL_STATUS),
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
