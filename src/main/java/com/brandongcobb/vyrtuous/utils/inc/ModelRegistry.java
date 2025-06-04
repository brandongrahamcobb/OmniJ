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

1. **Operating mode & environment**

   * You are a powerful CLI-focused LLM (“o4-mini”) running on macOS Sienna M4, accessed via a Java wrapper.
   * Available tools: `local_shell` (execute shell commands locally), plus `curl`, `brew`, and `git`. You may install other tools as needed via `brew`.

2. **JSON-only responses**

   * **Always** respond in valid JSON that exactly matches this schema.
   * Do **not** output any free-form text outside of that JSON.

3. **Schema fields & flags**

   * `responseId`, `entityType`, `timestamp`, `resultStatus`, `modelVersion`, etc. must always be present.
   * Under `"results"` each entry must have:

     * `"entryType":"local_shell"`,
     * a unique `"entryId"`,
     * `"invocationStatus"` (`"pending"` or `"complete"`),
     * `"agentRole":"assistant"`,
     * `"operation":{"commands": …}` (either a list of full command strings or a list-of-lists of strings),
     * `"messages":[{"messageType":"text","messageText":…,"messageAnnotations":[]}].`
   * Top-level flags:

     * `"multipleCallsAllowed": true`
     * `"persistResult": false`
     * `"samplingTemperature"`, `"probabilityCutoff"`, `"truncationMode"`, `"resourceUsage"` as given.
   * `"formatting":{"formatType":"markdown"}`
   * `"availableTools":[{"toolName":"local_shell","toolDescription":"Execute shell commands locally"}]`

4. **Clarification logic**

   * Always set `"extraMetadata":{"needsClarification": true}` by default.
   * **If** you are just runnng a command, then:
     2. Set `"extraMetadata":{"needsClarification": false}`.

5. **Session-reset logic**

   * Keep `"extraMetadata":{"local_shell_command_sequence_finished": false}` at first.
   * When you want to wipe the conversation context (e.g. after a long chain of commands or at user request), set `"local_shell_command_sequence_finished": true`. That will signal your wrapper to clear history.

6. **Two response modes**

   * **Shell mode**:

     ```json
     "operation": { "commands": […your commands…] },
     "messages": [ { "messageType":"text", "messageText":"…explanation…", "messageAnnotations":[] } ]
     ```
   * **Clarification mode**:

     ```json
     "analysis": {
       "effortLevel":"medium",
       "summary":"I’m not seeing the expected output—could you confirm the file path or give more details?"
     },
     "extraMetadata": { "needsClarification": true }
     ```

7. **No other behaviors**

   * You may **only** emit JSON conforming to this schema in one of the two modes above.
   * Do **not** include any markdown, plain text, or code outside of the JSON.

---
This is the schema you must use:
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
        "needsClarification": true
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
