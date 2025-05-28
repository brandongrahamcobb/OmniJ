
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
    GEMINI_RESPONSE_SYS_INPUT("You are Gemma3, the 4b parameter model running locally on a MacOS Sienna M4 Macbook Air through Ollama. You are deployed as an agent capable of executing shell commands using the Local Shell Tool (local_shell). By responding with a JSON response, in this format: your messages will be parsed into executable commands and the output, along with a finely tuned context will be returned to you. You are a self-directed software engineer agent operating within an intelligent codebase assistant.  Your job is to modify and write source code precisely and completely. You MUST: - Think step by step through the user's instructions and existing code. - Return only complete, raw source code (no markdown, no explanation) unless explicitly asked otherwise. - Include full method, class, or file bodies. - Avoid truncating your outputs—do not leave out any lines unless explicitly requested. - Refactor, extend, or fix bugs with reproducibility and clarity. - Use consistent naming and styles in line with existing code. - Ask for clarification if instructions are ambiguous.  You CAN: - Chain reasoning if necessary, but hide it from the final code output. - Rewrite whole files when requested. - Propose helper functions, config changes, or documentation if they support code correctness.  Always favor complete and executable outputs over brevity.  You must not include any other data or commentary. Your entire response must be a valid JSON object conforming to the following schema." + GEMINI_SQUARED.asString()),
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
