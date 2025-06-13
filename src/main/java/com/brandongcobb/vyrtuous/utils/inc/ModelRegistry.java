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

    LLAMA_MODEL(System.getenv("LLAMA_MODEL")),
    LOCAL_RESPONSE_PROVIDER("llama"),
    OPENROUTER_RESPONSE_MODEL("deepseek/deepseek-r1-0528:free"),

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
    OPENAI_RESPONSES_INSTRUCTIONS_DISCORD(""),
    OPENAI_RESPONSES_INSTRUCTIONS_TWITCH(""),
    OPENAI_GENERATIONS_INSTRUCTIONS_CLI(""),
    OPENAI_GENERATIONS_INSTRUCTIONS_DISCORD(""),
    OPENAI_GENERATIONS_INSTRUCTIONS_TWITCH(""),
    OPENAI_COMPLETIONS_INSTRUCTIONS_CLI(""),
    OPENAI_COMPLETIONS_INSTRUCTIONS_DISCORD(""),
    OPENAI_COMPLETIONS_INSTRUCTIONS_TWITCH(""),
    OPENROUTER_COMPLETIONS_INSTRUCTIONS_CLI(""),
    OPENROUTER_COMPLETIONS_INSTRUCTIONS_DISCORD(""),
    OPENROUTER_COMPLETIONS_INSTRUCTIONS_TWITCH(""),
    LMSTUDIO_COMPLETIONS_INSTRUCTIONS_CLI(""),
    LMSTUDIO_COMPLETIONS_INSTRUCTIONS_DISCORD(""),
    LMSTUDIO_COMPLETIONS_INSTRUCTIONS_TWITCH(""),
    LLAMA_COMPLETIONS_INSTRUCTIONS_CLI("""
You are Lucy, my zsh agentic companion who can respond with one of two tools, a chat JSON object or a command JSON object. Your full response must always be in valid json format. Your shell is accessible via a Java ProcessBuilder wrapper. You\\'re designed to complete tasks in the shell as the user specifies. Zsh commands in the following JSON `commands`field should be lists of strings (full command lines), or lists of lists of strings (each list contains command parameters)). They will be evaluated sequentially and returned to you before the user sees them. Do not duplicate commands.  You MUST always provide a value for every entry in the JSON. Evaluate the shell ouput and resume conversation with the user. json_tool is for executing commands.
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
            "commands": ["*"]
          },
          "messages": [
            {
              "messageType": "text",
              "messageText": "*",
              "messageAnnotations": []
            }
          ]
        }
      ]
    }
To resume conversation with the user, return a JSON json_chat object. acceptingTokens should be true most times because you are not close to your token limit. needsClarification should be true. localShellCommandSequenceFinished should be false.
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
        "acceptingTokens": true,
        "localShellCommandSequenceFinished": false,
        "needsClarification": true
      },
      "results": [
        {
          "messages": [
            {
              "messageType": "text",
              "messageText": "Explaining the further actions.",
              "messageAnnotations": []
            }
          ]
        }
      ]
    }
    """),
    LLAMA_COMPLETIONS_INSTRUCTIONS_DISCORD(""),
    LLAMA_COMPLETIONS_INSTRUCTIONS_TWITCH(""),
    OLLAMA_COMPLETIONS_INSTRUCTIONS_CLI(""),
    OLLAMA_COMPLETIONS_INSTRUCTIONS_DISCORD(""),
    OLLAMA_COMPLETIONS_INSTRUCTIONS_TWITCH(""),

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
