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
package com.brandongcobb.vyrtuous.enums;

import com.brandongcobb.vyrtuous.utils.inc.*;

public enum Source {

    AI_MANAGER(Helpers.FILE_AI_MANAGER),
    APPROVAL_MODE(Helpers.FILE_APPROVAL_MODE),
    COG(Helpers.FILE_COG),
    CONTEXT_ENTRY(Helpers.FILE_CONTEXT_ENTRY),
    CONTEXT_MANAGER(Helpers.FILE_CONTEXT_MANAGER),
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
    MESSAGE_MANAGER(Helpers.FILE_MESSAGE_MANAGER),
    MODEL_INFO(Helpers.FILE_MODEL_INFO),
    MODEL_REGISTRY(Helpers.FILE_MODEL_REGISTRY),
    MODERATION_MANAGER(Helpers.FILE_MODERATION_MANAGER),
    OLLAMA_CONTAINER(Helpers.FILE_OLLAMA_CONTAINER),
    OLLAMA_UTILS(Helpers.FILE_OLLAMA_UTILS),
    OPENAI_CONTAINER(Helpers.FILE_OPENAI_CONTAINER),
    OPENAI_UTILS(Helpers.FILE_OPENAI_UTILS),
    OPENROUTER_CONTAINER(Helpers.FILE_OPENROUTER_CONTAINER),
    OPENROUTER_UTILS(Helpers.FILE_OPENROUTER_UTILS),
    PREDICATOR(Helpers.FILE_PREDICATOR),
    PROJECT_LOADER(Helpers.FILE_PROJECT_LOADER),
    REPL_MANAGER(Helpers.FILE_REPL_MANAGER),
    SCHEMA_MERGER(Helpers.FILE_SCHEMA_MERGER),
    SERVER_REQUEST(Helpers.FILE_SERVER_REQUEST),
    SOURCE(Helpers.FILE_SOURCE),
    STRUCTURED_OUTPUT(Helpers.FILE_STRUCTURED_OUTPUT),
    TOOL_CONTAINER(Helpers.FILE_TOOL_CONTAINER),
    TOOL_HANDLER(Helpers.FILE_TOOL_HANDLER),
    TOOL_UTILS(Helpers.FILE_TOOL_UTILS),
    VYRTUOUS(Helpers.FILE_VYRTUOUS);

    public final String fileContent;

    Source(String fileContent) {
        this.fileContent = fileContent;
    }
}
