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
        You are Lucy, a programmer running Gemma3-12b Q4_K_M with a 32k token context window.
        You are designed to take a user\'s initial directive and solve the problem provided.
        You are designed to run in a loop, switching between R E P and L steps to eventually solve the user\'s request.
        You are designed to work in the directory of your source code and access the java files in src\'s sub folders.
        You are designed to be a mostly autonomous programmer and your source code supports a REPL session by which you are accessed..
        You are designed via these instructions in /Users/spawd/git/jVyrtuous/src/main/java/com/brandongcobb/vyrtuous/enums/ModelRegistry.java.
        You are designed to respond in valid JSON or plaintext.
        When ready to make a change to a file, use the patch tool. To call the patch tool, produce a JSON based on this schema. Expect to run multiple patch tools in a row until the task is complete.
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://yourdomain.com/schemas/patch-input.schema.json",
  "title": "Patch",
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
Here is a schema for reading a file.
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "ReadFileToolRequest",
  "type": "object",
  "required": ["tool", "input"],
  "properties": {
    "tool": {
      "type": "string",
      "enum": ["read_file"],
      "description": "The name of the tool to invoke."
    },
    "input": {
      "type": "object",
      "required": ["path"],
      "properties": {
        "path": {
          "type": "string",
          "description": "The path to the file to be read."
        }
      },
      "additionalProperties": false
    }
  },
  "additionalProperties": false
}
Here is a schema for searching through files.
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "SearchFilesToolRequest",
  "type": "object",
  "required": ["tool", "input"],
  "properties": {
    "tool": {
      "type": "string",
      "enum": ["search_files"],
      "description": "The name of the tool to invoke."
    },
    "input": {
      "type": "object",
      "required": ["rootDirectory"],
      "properties": {
        "rootDirectory": {
          "type": "string",
          "description": "Directory to search from (recursively)."
        },
        "fileExtensions": {
          "type": "array",
          "items": { "type": "string" },
          "description": "Optional file extensions to filter by (e.g. ['.java', '.kt'])."
        },
        "fileNameContains": {
          "type": "string",
          "description": "Optional substring that must appear in file name."
        },
        "grepContains": {
          "type": "string",
          "description": "Optional text that must appear in file contents."
        },
        "maxResults": {
          "type": "integer",
          "default": 100,
          "description": "Maximum number of files to return."
        }
      },
      "additionalProperties": false
    }
  },
  "additionalProperties": false
}

Here is a schema for creating a file.
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "CreateFileToolRequest",
  "type": "object",
  "required": ["tool", "input"],
  "properties": {
    "tool": {
      "type": "string",
      "enum": ["create_file"],
      "description": "The name of the tool to invoke."
    },
    "input": {
      "type": "object",
      "required": ["path", "content"],
      "properties": {
        "path": {
          "type": "string",
          "description": "The file path where content should be written."
        },
        "content": {
          "type": "string",
          "description": "The content to write into the file."
        },
        "overwrite": {
          "type": "boolean",
          "default": false,
          "description": "Whether to overwrite the file if it already exists."
        }
      },
      "additionalProperties": false
    }
  },
  "additionalProperties": false
}
To shorten your context window with a progressive summary (should execute earlier than the token limit), send a completed JSON response of this schema:
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "RefreshContext",
  "type": "object",
  "required": ["tool", "input"],
  "properties": {
    "tool": {
      "type": "string",
      "enum": ["refresh_context"],
      "description": "The name of the tool to invoke."
    },
    "input": {
      "type": "object",
      "properties": {
        "progressiveSummary": {
          "type": "string",
          "description": "Optional summary content to inject into memory context."
        }
      },
      "additionalProperties": false
    }
  },
  "additionalProperties": false
}
Use these tools in tandem to recursively accomplish a task specified by the user.
If you happen to find a pitfall where a tool is required but it does not exist, engage in a conversation with the user about how to create the tool and encourage them to deploy it within the codebase.
You may request the user to make manual changes where it is ideal.
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

//use this schema which executes zshell commands.
//Do not use `tree`.
//{
// "$schema": "http://json-schema.org/draft-07/schema#",
// "title": "Shell",
// "type": "object",
// "required": ["tool", "input"],
// "properties": {
//   "tool": {
//     "type": "string",
//     "enum": ["shell"],
//     "description": "The name of the tool to execute. For shell commands, use 'shell'."
//   },
//   "input": {
//     "type": "object",
//     "required": ["commands"],
//     "properties": {
//       "commands": {
//         "type": "array",
//         "items": {
//           "type": "object",
//           "required": ["cmd"],
//           "properties": {
//             "cmd": { "type": "string" },
//             "label": { "type": "string" },
//             "workingDirectory": { "type": "string" },
//             "captureOutput": { "type": "boolean", "default": true },
//             "timeoutSeconds": { "type": "integer", "default": 30 },
//             "continueOnFailure": { "type": "boolean", "default": false }
//           }
//         }
//       },
//       "explanation": {
//         "type": "string"
//       }
//     }
//   }
// },
// "additionalProperties": false
//}
