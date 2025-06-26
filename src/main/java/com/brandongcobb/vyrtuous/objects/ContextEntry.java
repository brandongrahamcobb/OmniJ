/* ContextEntry.java The purpose of this class is to handle context entry data dataclasses.
 *
 * Copyright (C) 2025  github.com/brandongrahamcobb
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.brandongcobb.vyrtuous.objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ContextEntry {
    
    public enum Type { USER_MESSAGE, AI_RESPONSE, TOOL, TOOL_OUTPUT, TOKENS, SYSTEM_NOTE, PROGRESSIVE_SUMMARY, SHELL_OUTPUT }
    
    private final Type type;
    private final String content;
    
    public ContextEntry(@JsonProperty("type") Type type, @JsonProperty("content") String content) {
        this.type = type;
        this.content = content;
    }
    
    @JsonProperty("type")
    public Type getType() {
        return type;
    }

    @JsonProperty("content")
    public String getContent() {
        return content;
    }

    public String formatForPrompt() {
        switch(type) {
            case USER_MESSAGE: return "[User]: " + content;
            case AI_RESPONSE: return "[AI]: " + content;
            case TOOL: return "[Tool]: " + content;
            case TOOL_OUTPUT: return "[Output]: " + content;
            case TOKENS: return "[Tokens]: " + content;
            case SYSTEM_NOTE: return "[System]: " + content;
            case PROGRESSIVE_SUMMARY: return "[Progressive Summary]: " + content;
            case SHELL_OUTPUT: return "[Shell Output]: " + content;
            default: return content;
        }
    }

}
