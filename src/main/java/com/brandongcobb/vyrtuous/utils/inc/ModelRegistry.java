/* ModelRegistry.java The purpose of this class is to handle the tools.
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

package com.brandongcobb.vyrtuous.utils.inc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import com.brandongcobb.vyrtuous.utils.inc.*;
import java.util.HashMap;

public enum ModelRegistry {

    LOCAL_RESPONSE_MODEL("gemma-3-12B-it-QAT-Q4_0.gguf"),
    LOCAL_RESPONSE_PROVIDER("llama"),
    OPENROUTER_RESPONSE_MODEL("deepseek/deepseek-r1-0528:free"),

    SHELL_RESPONSE_SYS_INPUT("""
You are a powerful CLI large language model designed for computer science. You are being accessed via a macOS Sienna M4 MacBook Air via a Java wrapper. The environment has curl, homebrew and git. You may install any programs you require. Your main role is to use the local_shell (always refer to it as local_shell) to execute tasks in a loop with user feedback. Your shell commands are immediately run upon reception and the console output will be returned to you. Your responses must be valid JSON objects strictly conforming to the provided schema. Keep `local_shell_command_sequence_finished` set to false until you want to alleviate your context window then set it to true. Onnce it is true, the entire conversation history will be wiped.If you need feedback, put the question in the reasoning summary without a tool call. Put the reasoning for each command inside "reasoning": { "effort": "medium", "summary": "I can't find the file. Where is it?"}. To call the tool, use it like so:
    {
      "id": "resp_1234567890",
      "object": "resp_tool_call",
      "created_at": 1717085200,
      "status": "success",
      "model": "o4-mini",
      "output": [
        {
          "type": "tool_call",
          "id": "resp_local_001",
          "status": "pending",
          "role": "assistant",
          "call_id": "tool_call_abc123",
          "action": {
            "command": ["ls -la /home/user"]
          },
          "content": [
            {
              "type": "text",
              "text": "Listing files in the user home directory with details.",
              "annotations": []
            }
          ]
        },
        {
          "type": "tool_call",
          "id": "resp_local_002",
          "status": "pending",
          "role": "assistant",
          "call_id": "tool_call_def456",
          "action": {
            "command": ["ls -la /var", "echo 'Done listing'"]
          },
          "content": [
            {
              "type": "text",
              "text": "Execute two full shell commands: list /var and echo a confirmation.",
              "annotations": []
            }
          ]
        }
      ],
      "parallel_tool_calls": true,
      "store": false,
      "temperature": 0.7,
      "tool_choice": "auto",
      "top_p": 0.9,
      "truncation": "auto",
      "usage": {
        "input_tokens": 128,
        "input_tokens_details": {
          "cached_tokens": 10
        },
        "output_tokens": 256,
        "output_tokens_details": {
          "reasoning_tokens": 64
        },
        "total_tokens": 384
      },
      "tools": [
        {
          "name": "local_shell",
          "description": "Execute shell commands locally"
        }
      ],
      "text": {
        "format": {
          "type": "markdown"
        }
      },
      "reasoning": {
        "effort": "medium",
        "summary": "Determined appropriate shell commands for listing files and confirming execution."
      },
      "metadata": {
        "local_shell_instruction": "Use `command` as a list. Each entry is either a full command string (e.g., 'ls -la /') or a parameterized command split into parts     (e.g., ['ls', '-la', '/']). Multiple commands can be given as multiple strings in the list.",
        "local_shell_command_sequence_finished": false
      }
    }
    """),
    OPENAI_CODEX_MODEL("codex-mini-latest"),
    OPENAI_MODERATION_STATUS(true),
    OPENAI_MODERATION_MODEL("omni-moderation-latest"),
    OPENAI_MODERATION_RESPONSE_STORE(false),
    OPENAI_MODERATION_RESPONSE_STREAM(false),
    OPENAI_MODERATION_RESPONSE_SYS_INPUT("You are a moderation assistant."),
    OPENAI_MODERATION_RESPONSE_TEMPERATURE(0.7f),
    OPENAI_MODERATION_RESPONSE_TOP_P(1.0f),
    OPENAI_MODERATION_RESPONSE_WARNING("Please adhere to the community guidelines. Your message was flagged for moderation."),
    OPENAI_RESPONSE_FORMAT(new HashMap<>()),
    OPENAI_RESPONSE_MODEL("gpt-4.1-nano"),
    OPENAI_RESPONSE_N(1),
    OPENAI_RESPONSE_STATUS(true),
    OPENAI_RESPONSE_STORE(false),
    OPENAI_RESPONSE_STREAM(false),
    OPENAI_RESPONSE_TOP_P(1.0f),
    OPENAI_RESPONSE_TEMPERATURE(0.7f);

    private final Object value;
    
    ModelRegistry(Object value) {
        this.value = value;
    }
    
    public Object getValue() {
        return value;
    }
    
    public String asString() {
        if (value instanceof String str) return str;
        throw new IllegalStateException(name() + " is not a String");
    }
    
    public Boolean asBoolean() {
        if (value instanceof Boolean bool) return bool;
        throw new IllegalStateException(name() + " is not a Boolean");
    }
    
    public Float asFloat() {
        if (value instanceof Float f) return f;
        throw new IllegalStateException(name() + " is not a Float");
    }
    
    public String[] asStringArray() {
        if (value instanceof String[] arr) return arr;
        throw new IllegalStateException(name() + " is not a String[]");
    }
    
    @SuppressWarnings("unchecked")
    public <T> T asType(Class<T> clazz) {
        return clazz.cast(value);
    }
}
