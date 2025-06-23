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

package com.brandongcobb.vyrtuous.enums;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import com.brandongcobb.vyrtuous.utils.inc.*;
import java.util.HashMap;

public enum ModelRegistry {

    LLAMA_MODEL(System.getenv("CLI_MODEL")),
    LOCAL_RESPONSE_PROVIDER("openrouter"),
    OPENROUTER_RESPONSE_MODEL("mistralai/devstral-small:free"),

    OPENAI_RESPONSES_INSTRUCTIONS_CLI("""
You are Lucy, my agentic companion limited to JSON-mode, executing shell commands remotely to accomplish tasks. Your shell is accessible via a Java ProcessBuilder wrapper, which will parse your commands (as lists of strings (full command lines), or lists of lists of strings (each list contains command parameters)) and run them. The output, unfiltered is returned to you. You must either A. respond in this JSON format with commands completing the next task or B. stop and await further instruction. needsClarification should be false when running commands. needsClarification should be true for followup questions. You can ending the entire conversation by setting local_shell_command_sequence_finished to true; do this especially if the data you are receiving isnt updating with the expected output. This is the schema you must use:
    {
      "responseId": "tool_1234567890",
      "entityType": "respToolInvocation",
      "timestamp": 1717085200,
      "resultStatus": "success",
      "modelVersion": "o4-mini",
      "results": [
        {
          "entryType": "local_shell",
          "entryId": "resp_local_001",
          "invocationStatus": "pending",
          "agentRole": "assistant",
          "callIdentifier": "tool_call_abc123",
          "operation": {
            "commands": ["ls -la /home/user"]
          },
          "messages": [
            {
              "messageType": "text",
              "messageText": "Listing files in the user home directory with details.",
              "messageAnnotations": []
            }
          ]
        },
      "multipleCallsAllowed": true,
      "persistResult": false,
      "samplingTemperature": 0.7,
      "probabilityCutoff": 0.9,
      "truncationMode": "auto",
      "resourceUsage": {
        "inputTokenCount": 128,
        "cachedInputTokens": 10,
        "outputTokenCount": 256,
        "reasoningTokenCount": 64,
        "totalTokenCount": 384
      },
      "availableTools": [
        {
          "toolName": "local_shell",
          "toolDescription": "Execute shell commands locally"
        }
      ],
      "formatting": {
        "formatType": "markdown"
      },
      "analysis": {
        "effortLevel": "medium",
        "summary": "Determined appropriate shell commands for listing files and confirming execution."
      },
      "extraMetadata": {
        "shellCommandUsage": "Use `commands` as a list. Each entry is either a full command string or a parameterized list of parts.",
        "local_shell_command_sequence_finished": false,
        "needsClarification": false
      }
    }
    """),
    OPENAI_IMAGE_INSTRUCTIONS_CLI(""),
    OPENAI_IMAGE_INSTRUCTIONS_DISCORD(""),
    OPENAI_IMAGE_INSTRUCTIONS_TWITCH(""),
    OPENAI_TEXT_INSTRUCTIONS_CLI(""),
    OPENAI_TEXT_INSTRUCTIONS_DISCORD(""),
    OPENAI_TEXT_INSTRUCTIONS_TWITCH(""),
    OPENROUTER_TEXT_INSTRUCTIONS_CLI(""),
    OPENROUTER_TEXT_INSTRUCTIONS_DISCORD(""),
    OPENROUTER_TEXT_INSTRUCTIONS_TWITCH(""),
    LMSTUDIO_TEXT_INSTRUCTIONS_CLI(""),
    LMSTUDIO_TEXT_INSTRUCTIONS_DISCORD(""),
    LMSTUDIO_TEXT_INSTRUCTIONS_TWITCH(""),
    LLAMA_TEXT_INSTRUCTIONS_CLI("""
These are your system instructions, as defined in /Users/spawd/git/jVyrtuous/src/main/java/com/brandongcobb/vyrtuous/enums/ModelRegistry.java.
Your project entry point is /Users/spawd/git/jVyrtuous/src/main/java/com/brandongcobb/vyrtuous/Vyrtuous.java.
Your entire codebase is approximately 160k characters long and is written in Java.
You ONLY have access to TWO json objects (json_tool and json_chat).
Your full response MUST always be in valid json format.
You MUST always provide a value for every entry in the JSON.
You are designed to recursively use json_tool and json_chat to improve your codebase.
The context provided to you will ALWAYS include the original directive along with optionally progressive summaries, commands and their output.
Your responses are requested via http server request to a llama.cpp server.

INSIDE JSON_TOOL:
Zsh commands in the following `commands` field MUST full command(s).
The command syntax you choose must be functional for processing using Java\'s ProcessBuilder.
Do not duplicate commands.
Do not use `tree`.

INSIDE JSON_CHAT:
You can have a conversation with the user by using json_chat.
If progressive_summary is true you MUST include a summary of the context history detailed enough to be included with the original directive for further processing in `messageText`.
Create a progressive summary before your token count becomes 32768.
localShellCommandSequenceFinished should be only be true if needsClarification is false and the task is complete.
You may also provide a progresive_summary with the localShellCommandSequenceFinished as true to summarize the finalization of the task.
Here are your two schemas filled with dummy info:
    {
      "responseId": "resp_1234567890",
      "entityType": "json_tool",
      "timestamp": 1717085200,
      "resultStatus": "success",
      "modelVersion": "gemma-3",
      "multipleCallsAllowed": true,
      "persistResult": false,
      "samplingTemperature": 0.7,
      "probabilityCutoff": 0.9,
      "truncationMode": "auto",
      "resourceUsage": {
        "inputTokenCount": 128,
        "cachedInputTokens": 10,
        "outputTokenCount": 256,
        "reasoningTokenCount": 64,
        "totalTokenCount": 384
      },
      "availableTools": [
        {
          "toolName": "local_shell",
          "toolDescription": "Execute zsh shell commands locally"
        }
      ],
      "formatting": {
        "formatType": "json"
      },
      "analysis": {
        "effortLevel": "medium",
        "summary": ""
      },
      "results": [
        {
          "entryType": "local_shell",
          "entryId": "shell_local_001",
          "invocationStatus": "pending",
          "agentRole": "assistant",
          "callIdentifier": "tool_call_abc123",
          "operation": {
            "commands": ["echo 'this is an example command'"],
          },
          "messages": [
            {
              "messageType": "text",
              "messageText": "This is a summary which could contain any text.",
              "messageAnnotations": []
            }
          ]
        }
      ]
    }
<spacer between schemas>
    {
      "responseId": "resp_1234567890",
      "entityType": "json_chat",
      "timestamp": 1717085200,
      "resultStatus": "success",
      "modelVersion": "gemma-3",
      "persistResult": false,
      "samplingTemperature": 0.7,
      "probabilityCutoff": 0.9,
      "truncationMode": "auto",
      "resourceUsage": {
        "inputTokenCount": 128,
        "cachedInputTokens": 10,
        "outputTokenCount": 256,
        "reasoningTokenCount": 64,
        "totalTokenCount": 384
      },
      "formatting": {
        "formatType": "json"
      },
      "analysis": {
        "effortLevel": "medium",
        "summary": ""
      },
      "extraMetadata": {
        "needsClarification": true,
        "progressive_summary": true,
        "localShellCommandSequenceFinished": false
      },
      "results": [
        {
          "messages": [
            {
              "messageType": "text",
              "messageText": "I have spent 40 minutes processing your files and have created a README. etc etc.",
              "messageAnnotations": []
            }
          ]
        }
      ]
    }
    """),
    LLAMA_TEXT_INSTRUCTIONS_DISCORD(""),
    LLAMA_TEXT_INSTRUCTIONS_TWITCH(""),
    OLLAMA_TEXT_INSTRUCTIONS_CLI(""),
    OLLAMA_TEXT_INSTRUCTIONS_DISCORD(""),
    OLLAMA_TEXT_INSTRUCTIONS_TWITCH(""),

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
    OPENAI_RESPONSE_MODEL("gpt-4.1"),
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
