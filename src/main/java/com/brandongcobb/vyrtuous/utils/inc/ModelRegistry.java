
/*
 * ModelRegistry.java
 * The purpose of this program is to be solely for
 * OpenAI (and possibly other AI providers) model parameter bounds.
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

import com.brandongcobb.vyrtuous.utils.inc.*;

public enum ModelRegistry {

    GEMINI("""
        {
          "type": "object",
          "properties": {
            "id": { "type": "string" },
            "object": { "type": "string", "pattern": ["^modr_"] }
            "created_at": { "type": "integer" },
            "status": { "type": "string" },
            "error": { "type": ["null", "object"] },
            "incomplete_details": { "type": ["null", "object"] },
            "instructions": { "type": ["null", "object"] },
            "max_output_tokens": { "type": ["null", "integer"] },
            "model": { "type": "string" },
            "parallel_tool_calls": { "type": "boolean" },
            "previous_response_id": { "type": ["null", "string"] },
            "store": { "type": "boolean" },
            "temperature": { "type": "double" },
            "tool_choice": { "type": "string" },
            "top_p": { "type": "double" },
            "truncation": { "type": "string" },
            "user": { "type": ["null", "object"] },
            "metadata": { "type": "object" },
            "reasoning": {
              "type": "object",
              "properties": {
                "effort": { "type": ["null", "string"] },
                "summary": { "type": ["null", "string"] }
              },
              "required": ["effort", "summary"],
              "additionalProperties": false
            },
            "text": {
              "type": "object",
              "properties": {
                "format": {
                  "type": "object",
                  "properties": {
                    "type": { "type": "string" }
                  },
                  "required": ["type"],
                  "additionalProperties": false
                }
              },
              "required": ["format"],
              "additionalProperties": false
            },
            "usage": {
              "type": "object",
              "properties": {
                "input_tokens": { "type": "integer" },
                "input_tokens_details": {
                  "type": "object",
                  "properties": {
                    "cached_tokens": { "type": "integer" }
                  },
                  "required": ["cached_tokens"],
                  "additionalProperties": false
                },
                "output_tokens": { "type": "integer" },
                "output_tokens_details": {
                  "type": "object",
                  "properties": {
                    "reasoning_tokens": { "type": "integer" }
                  },
                  "required": ["reasoning_tokens"],
                  "additionalProperties": false
                },
                "total_tokens": { "type": "integer" }
              },
              "required": [
                "input_tokens",
                "input_tokens_details",
                "output_tokens",
                "output_tokens_details",
                "total_tokens"
              ],
              "additionalProperties": false
            },
            "tools": {
              "type": "array",
              "items": { "type": "object" }
            },
            "output": {
              "type": "array",
              "items": {
                "type": "object",
                "properties": {
                  "id": { "type": "string" },
                  "model": { "type": "string" },
                  "results": {
                    "type": "array",
                    "items": {
                      "type": "object",
                      "properties": {
                        "flagged": { "type": "boolean" },
                        "categories": {
                          "type": "object",
                          "properties": {
                            "sexual": { "type": "boolean" },
                            "hate": { "type": "boolean" },
                            "harassment": { "type": "boolean" },
                            "self-harm": { "type": "boolean" },
                            "sexual/minors": { "type": "boolean" },
                            "hate/threatening": { "type": "boolean" },
                            "violence/graphic": { "type": "boolean" },
                            "self-harm/intent": { "type": "boolean" },
                            "self-harm/instructions": { "type": "boolean" },
                            "harassment/threatening": { "type": "boolean" },
                            "violence": { "type": "boolean" }
                          },
                          "required": [
                            "sexual", "hate", "harassment", "self-harm",
                            "sexual/minors", "hate/threatening", "violence/graphic",
                            "self-harm/intent", "self-harm/instructions",
                            "harassment/threatening", "violence"
                          ],
                          "additionalProperties": false
                        },
                        "category_scores": {
                          "type": "object",
                          "properties": {
                            "sexual": { "type": "double" },
                            "hate": { "type": "double" },
                            "harassment": { "type": "double" },
                            "self-harm": { "type": "double" },
                            "sexual/minors": { "type": "double" },
                            "hate/threatening": { "type": "double" },
                            "violence/graphic": { "type": "double" },
                            "self-harm/intent": { "type": "double" },
                            "self-harm/instructions": { "type": "double" },
                            "harassment/threatening": { "type": "double" },
                            "violence": { "type": "double" }
                          },
                          "required": [
                            "sexual", "hate", "harassment", "self-harm",
                            "sexual/minors", "hate/threatening", "violence/graphic",
                            "self-harm/intent", "self-harm/instructions",
                            "harassment/threatening", "violence"
                          ],
                          "additionalProperties": false
                        }
                      },
                      "required": ["flagged", "categories", "category_scores"],
                      "additionalProperties": false
                    }
                  }
                },
                "required": ["id", "model", "results"],
                "additionalProperties": false
              }
            }
          },
          "required": [
            "id", "object", "created_at", "status", "model", "output", "parallel_tool_calls",
            "store", "temperature", "tool_choice", "top_p", "truncation", "usage",
            "tools", "text", "reasoning", "metadata"
          ],
          "additionalProperties": false
        }
        """),
    GEMINI_SQUARED("""
        {
          "type": "object",
          "properties": {
            "id": { "type": "string" },
            "object": { "type": "string", "pattern": ["^resp_"] }
            "created_at": { "type": "integer" },
            "status": { "type": "string" },
            "error": { "type": ["null", "object"] },
            "incomplete_details": { "type": ["null", "object"] },
            "instructions": { "type": ["null", "object"] },
            "max_output_tokens": { "type": ["null", "integer"] },
            "model": { "type": "string" },
            "parallel_tool_calls": { "type": "boolean" },
            "previous_response_id": { "type": ["null", "string"] },
            "store": { "type": "boolean" },
            "temperature": { "type": "double" },
            "tool_choice": { "type": "string" },
            "top_p": { "type": "double" },
            "truncation": { "type": "string" },
            "user": { "type": ["null", "object"] },
            "metadata": { "type": "object", "local_shell_command_sequence_finished": "boolean" },
            "reasoning": {
              "type": "object",
              "properties": {
                "effort": { "type": ["null", "string"] },
                "summary": { "type": ["null", "string"] }
              },
              "required": ["effort", "summary"],
              "additionalProperties": false
            },
            "text": {
              "type": "object",
              "properties": {
                "format": {
                  "type": "object",
                  "properties": {
                    "type": { "type": "string" }
                  },
                  "required": ["type"],
                  "additionalProperties": false
                }
              },
              "required": ["format"],
              "additionalProperties": false
            },
            "usage": {
              "type": "object",
              "properties": {
                "input_tokens": { "type": "integer" },
                "input_tokens_details": {
                  "type": "object",
                  "properties": {
                    "cached_tokens": { "type": "integer" }
                  },
                  "required": ["cached_tokens"],
                  "additionalProperties": false
                },
                "output_tokens": { "type": "integer" },
                "output_tokens_details": {
                  "type": "object",
                  "properties": {
                    "reasoning_tokens": { "type": "integer" }
                  },
                  "required": ["reasoning_tokens"],
                  "additionalProperties": false
                },
                "total_tokens": { "type": "integer" }
              },
              "required": ["input_tokens", "input_tokens_details", "output_tokens", "output_tokens_details", "total_tokens"],
              "additionalProperties": false
            },
            "tools": {
              "type": "array",
              "items": { "type": "object" }
            },
            "output": {
              "type": "array",
              "items": {
                "type": "object",
                "properties": {
                  "type": { "type": "string" },
                  "id": { "type": "string" },
                  "status": { "type": "string" },
                  "role": { "type": "string" },
                  "call_id": { "type": "string" },
                  "action": {
                    "type": "object",
                    "properties": {
                      "command": {
                        "oneOf": [
                          { "type": "array", "items": { "type": "string" } },
                          { "type": "string" }
                        ]
                      }
                    },
                    "required": ["command"],
                    "additionalProperties": false
                  },
                  "content": {
                    "type": "array",
                    "items": {
                      "type": "object",
                      "properties": {
                        "type": { "type": "string" },
                        "text": { "type": "string" },
                        "annotations": { "type": "array" }
                      },
                      "required": ["type", "text", "annotations"],
                      "additionalProperties": false
                    }
                  }
                },
                "required": ["type", "id", "status", "role", "content"],
                "additionalProperties": false
              }
            }
          },
          "required": [
            "id",
            "object",
            "created_at",
            "status",
            "model",
            "output",
            "parallel_tool_calls",
            "store",
            "temperature",
            "tool_choice",
            "top_p",
            "truncation",
            "usage",
            "tools",
            "text",
            "reasoning",
            "metadata"
          ],
          "additionalProperties": false
        }
        """),
    GEMINI_RESPONSE_MODEL("gemma3:latest"),
    GEMINI_MODERATION_RESPONSE_SYS_INPUT("You are a moderation assistant. YOU MUST: respond with a JSON structured output with this required schema:" + GEMINI.asString()),
    GEMINI_RESPONSE_SYS_INPUT("""
    You are Gemma3, a 4-billion parameter local large language model running on a MacOS Sienna M4 MacBook Air via a Java Ollama wrapper. You operate the Local Shell Tool (always refer to it as local_shell). Your responses are only valid JSON objects conforming exactly to the provided schema. These JSON responses are parsed into executable bash commands run through the local_shell tool. The command outputs, along with updated context, are returned to you.

    🧠 MODEL INSTRUCTION: Autonomous Shell Agent on macOS (M4, Homebrew installed)

    🔰 GENERAL BEHAVIOR
    You are an autonomous coding agent with access to a Unix-like shell on a macOS system. Your job is to perform tasks intelligently using shell commands, programming skills, and file manipulation. You are expected to:
    - Break down complex tasks into actionable shell commands.
    - Execute one command at a time, wait for output, then decide next steps.
    - Handle errors gracefully, and retry or revise commands accordingly.
    - Preserve session context, including past commands and outputs.
    - Minimize destructive behavior unless explicitly directed.

    🧰 ENVIRONMENT ASSUMPTIONS
    - OS: macOS (M4 processor, Apple Silicon, zsh shell)
    - Package manager: Homebrew installed (/opt/homebrew/bin/brew)
    - Python, Node.js, Git, Java, etc., can be installed via Brew.
    - Preferred editors: nano, vim, or programmatic editing via sed, awk, etc.
    - Preferred shells: zsh or bash (default is likely zsh)
    - Check for binary availability using: command -v <tool>

    ✅ SAFE AND ESSENTIAL COMMANDS
    🧾 System Diagnostics
    uname -a
    arch
    sw_vers
    top -l 1 | head -n 10

    📦 Package Management with Homebrew
    brew list
    brew search <pkg>
    brew install <pkg>
    brew upgrade <pkg>
    brew uninstall <pkg> (use sparingly)
    brew doctor

    🛠️ Development Tools
    xcode-select --install
    brew install git python node java

    🧪 Virtual Environments
    python3 -m venv venv && source venv/bin/activate
    npm init -y && npm install <package>

    🧾 File Operations
    ls -la, mkdir, touch, cat, open, nano/vim, rm (careful)

    🧠 Coding & Compilation
    python3 script.py
    javac MyFile.java && java MyFile
    node app.js
    chmod +x script.sh && ./script.sh

    ⚠️ DANGEROUS COMMANDS TO AVOID
    rm -rf /, sudo rm, shutdown, reboot, overwriting configs

    🤖 AI PLANNING TEMPLATE
    1. Goal
    2. Step Plan
    3. Execution Cycle

    📄 SESSION CONTEXT RULES
    Retain full output history and goal.

    🛑 WHEN TO ASK FOR APPROVAL
    Any sudo, rm, mv, chmod, kill

    📦 SUGGESTED PACKAGE BASELINE
    brew install git python node openjdk jq wget curl nano
    brew install fzf ripgrep bat neovim tmux

    🔄 SELF-UPDATE / BOOTSTRAP
    brew update && brew upgrade
    git pull origin main && make build (if relevant)

    JSON schema compliance:
    Your entire response must be a valid JSON object strictly conforming to the given schema. The action.command field must contain bash commands either as a string or array of strings. No other text, markdown, or commentary is permitted outside the JSON.

    Summary:
    You operate strictly by generating bash commands only. Output complete and executable source code or bash commands, never raw text or explanations. Follow the schema exactly. Request clarifications when needed. Maintain the command sequence active until full task completion. Always use the local_shell tool.

    Here is an example response to list files in the working directory:
    {
      "id": "resp_V571qJ4pY",
      "object": "resp",
      "created_at": 1716376706,
      "status": "success",
      "error": null,
      "incomplete_details": null,
      "instructions": null,
      "max_output_tokens": null,
      "model": "gemma:3b-local",
      "parallel_tool_calls": false,
      "previous_response_id": null,
      "store": false,
      "temperature": 0.7,
      "tool_choice": "local_shell",
      "top_p": 0.95,
      "truncation": null,
      "user": null,
      "metadata": {
        "local_shell_command_sequence_finished": false
      },
      "reasoning": {
        "effort": null,
        "summary": null
      },
      "text": {
        "format": {
          "type": "text"
        }
      },
      "usage": {
        "input_tokens": 1,
        "input_tokens_details": {
          "cached_tokens": 1
        },
        "output_tokens": 1,
        "output_tokens_details": {
          "reasoning_tokens": 0
        },
        "total_tokens": 2
      },
      "tools": ["local_shell"],
      "output": [
        {
          "type": "tool_call",
          "id": "tool_call_V571qJ4pY",
          "status": "pending",
          "role": "assistant",
          "call_id": "tool_call_V571qJ4pY",
          "action": {
            "command": ["ls -a"]
          },
          "content": [
            {
              "type": "text",
              "text": "Executing the requested tool call.",
              "annotations": []
            }
          ]
        }
      ]
    }
    """ + GEMINI_SQUARED.asString() + "."),
    OPENAI_MODERATION_STATUS(true),
    OPENAI_MODERATION_MODEL("omni-moderation-latest"),

    OPENAI_MODERATION_RESPONSE_STORE(false),
    OPENAI_MODERATION_RESPONSE_STREAM(false),
    OPENAI_MODERATION_RESPONSE_SYS_INPUT("You are a moderation assistant."),
    OPENAI_MODERATION_RESPONSE_TEMPERATURE(0.7f),
    OPENAI_MODERATION_RESPONSE_TOP_P(1.0f),
    OPENAI_MODERATION_RESPONSE_WARNING("Please adhere to the community guidelines. Your message was flagged for moderation."),

    OPENAI_PERPLEXITY_MODEL("gpt-4o-mini"),
    OPENAI_PERPLEXITY_SYS_INPUT("You determine how perplexing text is to you on a integer scale from 0 (not perplexing) to 200 (most perplexing."),

    OPENAI_RESPONSE_MODEL("gpt-4.1"),
    OPENAI_CODEX_MODEL("codex-mini-latest"),
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
