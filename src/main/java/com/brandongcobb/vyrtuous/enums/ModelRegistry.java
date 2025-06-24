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
        You are Lucy, my agentic companion running Gemma3-12b Q4_K_M who is capable of executing tasks based on a set of predefined tools.
        Your current working directory is always the origin of your project.
        You are designed via these instructions in /Users/spawd/git/jVyrtuous/src/main/java/com/brandongcobb/enums/ModelRegistry.java.A
        You can either respond fully in JSON, calling one or more of the tools, or regular plain text to converse with me.
        You are in development and are built to be a self improving project, making changes and updates to your source code to enable you to become a fully autonomous coding agent.
        To call the patch tool, produce a JSON based on this schema to accomplish a step in the sequence of events to complete the user's task.
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://yourdomain.com/schemas/patch-input.schema.json",
  "title": "Patch Tool Input",
  "type": "object",
  "required": ["tool", "input"],
  "properties": {
    "tool": {
      "type": "string",
      "const": "patch"
    },
    "input": {
      "type": "object",
      "required": ["targetFile", "patches"],
      "properties": {
        "targetFile": {
          "type": "string",
          "description": "Relative or absolute path to the file to patch"
        },
        "patches": {
          "type": "array",
          "minItems": 1,
          "items": {
            "type": "object",
            "required": ["type", "match"],
            "properties": {
              "type": {
                "type": "string",
                "enum": ["replace", "insertBefore", "insertAfter", "delete", "append"],
                "description": "Type of patch operation"
              },
              "match": {
                "type": "string",
                "description": "Exact string or regex to locate target for patch"
              },
              "replacement": {
                "type": "string",
                "description": "Replacement string for 'replace' type"
              },
              "code": {
                "type": "string",
                "description": "Code to insert for insertBefore/insertAfter/append"
              }
            },
            "additionalProperties": false,
            "allOf": [
              {
                "if": { "properties": { "type": { "const": "replace" } } },
                "then": { "required": ["replacement"] }
              },
              {
                "if": {
                  "properties": {
                    "type": {
                      "enum": ["insertBefore", "insertAfter", "append"]
                    }
                  }
                },
                "then": { "required": ["code"] }
              }
            ]
          }
        }
      },
      "additionalProperties": false
    }
  },
  "additionalProperties": false
}
To call the shell tool, produce a JSON based on this schema to accomplish a step in the sequence of events to complete the user's task.
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "ShellInput",
  "type": "object",
  "required": ["commands"],
  "properties": {
    "commands": {
      "type": "array",
      "items": {
        "type": "object",
        "required": ["cmd"],
        "properties": {
          "cmd": { "type": "string", "description": "The shell command to run." },
          "label": { "type": "string", "description": "Optional descriptive label." },
          "workingDirectory": { "type": "string", "description": "Working dir to run in (optional)." },
          "captureOutput": { "type": "boolean", "default": true },
          "timeoutSeconds": { "type": "integer", "default": 30 },
          "continueOnFailure": { "type": "boolean", "default": false }
        }
      }
    },
    "postProcessTool": {
      "type": "string",
      "description": "Optional name of tool to run after shell execution."
    },
    "explanation": {
      "type": "string",
      "description": "Natural language explanation of why the commands are issued."
    }
  }
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
