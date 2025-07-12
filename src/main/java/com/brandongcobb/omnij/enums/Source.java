/*  FileObject.java
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
package com.brandongcobb.omnij.enums;

import com.brandongcobb.omnij.utils.inc.Helpers;

public enum Source {

    AI_SERVICE(Helpers.FILE_AI_SERVICE),
    COG(Helpers.FILE_COG),
    CREATE_FILE(Helpers.FILE_CREATE_FILE),
    CREATE_FILE_INPUT(Helpers.FILE_CREATE_FILE_INPUT),
    CUSTOM_TOOL(Helpers.FILE_CUSTOM_TOOL),
    DISCORD_BOT(Helpers.FILE_DISCORD_BOT),
    EVENT_LISTENERS(Helpers.FILE_EVENT_LISTENERS),
    HELPERS(Helpers.FILE_HELPERS),
    HYBRID_COMMANDS(Helpers.FILE_HYBRID_COMMANDS),
    LLAMA_CONTAINER(Helpers.FILE_LLAMA_CONTAINER),
    LLAMA_UTILS(Helpers.FILE_LLAMA_UTILS),
    LMSTUDIO_CONTAINER(Helpers.FILE_LMSTUDIO_CONTAINER),
    LMSTUDIO_UTILS(Helpers.FILE_LMSTUDIO_UTILS),
    MAIN_CONTAINER(Helpers.FILE_MAIN_CONTAINER),
    MARKDOWN_CONTAINER(Helpers.FILE_MARKDOWN_CONTAINER),
    MARKDOWN_UTILS(Helpers.FILE_MARKDOWN_UTILS),
    MESSAGE_SERVICE(Helpers.FILE_MESSAGE_SERVICE),
    MODEL_INFO(Helpers.FILE_MODEL_INFO),
    MODEL_REGISTRY(Helpers.FILE_MODEL_REGISTRY),
    MODERATION_SERVICE(Helpers.FILE_MODERATION_SERVICE),
    OLLAMA_CONTAINER(Helpers.FILE_OLLAMA_CONTAINER),
    OLLAMA_UTILS(Helpers.FILE_OLLAMA_UTILS),
    OPENAI_CONTAINER(Helpers.FILE_OPENAI_CONTAINER),
    OPENAI_UTILS(Helpers.FILE_OPENAI_UTILS),
    OPENROUTER_CONTAINER(Helpers.FILE_OPENROUTER_CONTAINER),
    OPENROUTER_UTILS(Helpers.FILE_OPENROUTER_UTILS),
    PATCH(Helpers.FILE_PATCH),
    PATCH_INPUT(Helpers.FILE_PATCH_INPUT),
    PATCH_OPERATION(Helpers.FILE_PATCH_OPERATION),
    READ_FILE(Helpers.FILE_READ_FILE),
    READ_FILE_INPUT(Helpers.FILE_READ_FILE_INPUT),
    REPL_SERVICE(Helpers.FILE_REPL_SERVICE),
    SEARCH_FILES(Helpers.FILE_SEARCH_FILES),
    SEARCH_FILES_INPUT(Helpers.FILE_SEARCH_FILES_INPUT),
    SERVER_REQUEST(Helpers.FILE_SERVER_REQUEST),
    SOURCE(Helpers.FILE_SOURCE),
    STRUCTURED_OUTPUT(Helpers.FILE_STRUCTURED_OUTPUT),
    TOOL_SERVICE(Helpers.FILE_TOOL_SERVICE),
    TOOL_INPUT(Helpers.FILE_TOOL_INPUT),
    VYRTUOUS(Helpers.FILE_VYRTUOUS);

    public final String fileContent;

    Source(String fileContent) {
        this.fileContent = fileContent;
    }
}
