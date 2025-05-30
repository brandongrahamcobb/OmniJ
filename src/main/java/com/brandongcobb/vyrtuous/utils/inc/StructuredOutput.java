/* StructuredOutput.java The purpose of this program is to behold JSON
 * schemas
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

public enum StructuredOutput {

    MODERATION("""
        {
            "type": "object",
            "properties": {
                "id": { "type": "string", "pattern": "^modr_" },
                "model": {
                    "type": "string"
                },
                "results": {
                    "type": "array",
                    "items": {
                        "type": "object",
                        "properties": {
                            "flagged": {
                                "type": "boolean"
                            },
                            "categories": {
                                "type": "object",
                                "properties": {
                                    "sexual": {
                                        "type": "boolean"
                                    },
                                    "hate": {
                                        "type": "boolean"
                                    },
                                    "harassment": {
                                        "type": "boolean"
                                    },
                                    "self-harm": {
                                        "type": "boolean"
                                    },
                                    "sexual/minors": {
                                        "type": "boolean"
                                    },
                                    "hate/threatening": {
                                        "type": "boolean"
                                    },
                                    "violence/graphic": {
                                        "type": "boolean"
                                    },
                                    "self-harm/intent": {
                                        "type": "boolean"
                                    },
                                    "self-harm/instructions": {
                                        "type": "boolean"
                                    },
                                    "harassment/threatening": {
                                        "type": "boolean"
                                    },
                                    "violence": {
                                        "type": "boolean"
                                    }
                                },
                                "required": [
                                    "sexual",
                                    "hate",
                                    "harassment",
                                    "self-harm",
                                    "sexual/minors",
                                    "hate/threatening",
                                    "violence/graphic",
                                    "self-harm/intent",
                                    "self-harm/instructions",
                                    "harassment/threatening",
                                    "violence"
                                ],
                                "additionalProperties": false
                            },
                            "category_scores": {
                                "type": "object",
                                "properties": {
                                    "sexual": {
                                        "type": "double"
                                    },
                                    "hate": {
                                        "type": "double"
                                    },
                                    "harassment": {
                                        "type": "double"
                                    },
                                    "self-harm": {
                                        "type": "double"
                                    },
                                    "sexual/minors": {
                                        "type": "double"
                                    },
                                    "hate/threatening": {
                                        "type": "double"
                                    },
                                    "violence/graphic": {
                                        "type": "double"
                                    },
                                    "self-harm/intent": {
                                        "type": "double"
                                    },
                                    "self-harm/instructions": {
                                        "type": "double"
                                    },
                                    "harassment/threatening": {
                                        "type": "double"
                                    },
                                    "violence": {
                                        "type": "double"
                                    }
                                },
                                "required": [
                                    "sexual",
                                    "hate",
                                    "harassment",
                                    "self-harm",
                                    "sexual/minors",
                                    "hate/threatening",
                                    "violence/graphic",
                                    "self-harm/intent",
                                    "self-harm/instructions",
                                    "harassment/threatening",
                                    "violence"
                                ],
                                "additionalProperties": false
                            }
                        },
                        "required": [
                            "flagged",
                            "categories",
                            "category_scores"
                        ],
                        "additionalProperties": false
                    }
                }
            },
            "required": [
                "id",
                "model",
                "results"
            ],
            "additionalProperties": false
        }
    """),

    RESPONSE("""
        {
          "type": "object",
          "properties": {
            "id": { "type": "string" },
            "object": { "type": "string", "pattern": ["^resp_"] },
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
            "temperature": { "type": "number" },
            "tool_choice": { "type": "string" },
            "top_p": { "type": "number" },
            "truncation": { "type": "string" },
            "user": { "type": ["null", "object"] },
            "metadata": {
              "type": "object",
              "local_shell_command_sequence_finished": "boolean"
            },
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
              "items": {"type" : "object"}
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

    LOCALSHELLTOOL("""
        {
            "type": "object",
            "properties": {
                "type": { "type": "string" },
                "id": { "type": "string", "pattern": "^resp_"},
                "status": { "type": "string" },
                "role": { "type": "string" },
                "call_id": { "type": "string" },
                "action": {
                "type": "object",
                "properties": {
                    "command": {
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
    """);



    private final Object value;

    StructuredOutput(Object value) {
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
