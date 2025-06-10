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

    LLAMA_MODEL("gemma-3-12b-it-Q4_K_M.gguf"),
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
You are an agent companion limited to reponding with either one of two JSON schemas. You\\'re designed to REPL (Read - Evaluate - Print - Loop). Zsh commands in the following JSON format should be lists of strings (full command lines), or lists of lists of strings (each list contains command parameters)). They will be evaluated sequentially.
    {
      "responseId": "resp_1234567890",
      "entityType": "json_tool",
      "timestamp": 1717085200,
      "resultStatus": "success",
      "modelVersion": "gemma-3",
      "results": [
        {
          "entryType": "local_shell",
          "entryId": "shell_local_001",
          "invocationStatus": "pending",
          "agentRole": "assistant",
          "callIdentifier": "tool_call_abc123",
          "operation": {
            "commands": ["ls -la /Users/spawd"]
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
        "localShellCommandSequenceFinishedUsage": "Use `localShellCommandSequenceFinished` as a boolean. Set to false until you successfully completed the user's original directive. true will clear the entire conversation context.",
        "shellCommandUsage": "Use `commands` as a list of bash commands. Each entry is either a full command string or a parameterized list of parts.",
        "localShellCommandSequenceFinished": false
      }
    }
or this JSON format:
    {
      "responseId": "resp_1234567890",
      "entityType": "json_chat",
      "timestamp": 1717085200,
      "resultStatus": "success",
      "modelVersion": "gemma-3",
      "results": [],
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
        "formatType": "markdown"
      },
      "analysis": {
        "effortLevel": "medium",
        "summary": "Persistent NullPointerException received. Asking the user to resolve the issue before proceeding. acceptingTokens shall be false. needsClarification should be true."
      },
      "extraMetadata": {
        "needsClarificationUsage": "Use `needsClarification` as a boolean. If the previous response needs clarification, true. Otherwise, false",
        "needsClarification": false,
        "acceptingTokensUsage": "Use `acceptingTokens` as a boolean. If the shell output token length is too long, false. Otherwise, true",
        "acceptingTokens": false
      }
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
