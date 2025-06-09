/* SchemaMerger.java The purpose of this program is to create JSON
 * schemas.
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
package com.brandongcobb.vyrtuous.utils.handlers;


import com.brandongcobb.vyrtuous.utils.inc.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class SchemaMerger {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String mergeLocalShell(String baseSchemaString, String metadataSnippet) throws IOException {
        JsonNode baseSchema = objectMapper.readTree(baseSchemaString);
        JsonNode metadataNode = baseSchema.path("properties").path("metadata").path("properties");
        if (metadataNode instanceof ObjectNode) {
            JsonNode metadataToAdd = objectMapper.readTree(metadataSnippet);
            ((ObjectNode) metadataNode).set("local_shell_command_sequence_finished", metadataToAdd.path("local_shell_command_sequence_finished"));
        }
        return objectMapper.writeValueAsString(baseSchema);
    }
    
    public static String mergeModeration(String baseSchemaString, String outputItemSnippet) throws IOException {
        JsonNode baseSchema = objectMapper.readTree(baseSchemaString);
        JsonNode outputItemsNode = baseSchema.path("properties").path("output").path("items").path("properties");
        if (outputItemsNode instanceof ObjectNode) {
            JsonNode outputItemToAdd = objectMapper.readTree(outputItemSnippet);
            ((ObjectNode) outputItemsNode).setAll((ObjectNode) outputItemToAdd);
        }
        return objectMapper.writeValueAsString(baseSchema);
    }
    
    public CompletableFuture<String> completeGetModerationSchemaNestResponse() {
        return CompletableFuture.supplyAsync(() -> {
            String baseSchema = StructuredOutput.RESPONSE.asString();
            try {
                String mergedSchemas = mergeModeration(
                    baseSchema,
                    StructuredOutput.MODERATION.asString()
                );
                return mergedSchemas;
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return ioe.getMessage();
            }
        });
    }
    
    public static CompletableFuture<String> completeGetShellToolSchemaNestResponse() {
        return CompletableFuture.supplyAsync(() -> {
            String baseSchema = StructuredOutput.RESPONSE.asString();
            try {
                String mergedSchemas = mergeLocalShell(
                    baseSchema,
                    StructuredOutput.LOCALSHELLTOOL.asString()
                );
                return mergedSchemas;
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return ioe.getMessage();
            }
        });
    }
}
