//
//  SearchWebTool.swift
//  
//
//  Created by Brandon Cobb on 6/30/25.
//


package com.brandongcobb.vyrtuous.tools;

import com.brandongcobb.vyrtuous.domain.input.SearchWebInput;
import com.brandongcobb.vyrtuous.domain.ToolStatus;
import com.brandongcobb.vyrtuous.domain.ToolStatusWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.brandongcobb.vyrtuous.service.REPLService.printIt;

@Component
public class SearchWeb implements CustomTool<SearchWebInput, ToolStatus> {

    private static final int MAX_QUERY_LENGTH = 500;
    private static final ObjectMapper mapper = new ObjectMapper();
    private final ChatMemory chatMemory;
    
    @Autowired
    public SearchWeb(ChatMemory replChatMemory) {
        this.chatMemory = replChatMemory;
    }
    
    @Override
    public String getDescription() {
        return "Searches the web with a query and returns the results.";
    }

    @Override
    public JsonNode getJsonSchema() {
        try {
            return mapper.readTree("""
                {
                    "type": "object",
                    "required": ["query"],
                    "properties": {
                        "query": {
                        "type": "string",
                        "description": "The search query to run using the Google Programmable Search API."
                        }
                    },
                    "additionalProperties": false
                }
            """);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build search_web schema", e);
        }
    }
    
    @Override
    public Class<SearchWebInput> getInputClass() {
        return SearchWebInput.class;
    }
    
    @Override
    public String getName() {
        return "search_web";
    }

    @Override
    public CompletableFuture<ToolStatus> run(SearchWebInput input) {
        return CompletableFuture.supplyAsync(() -> {
            String query = input.getQuery();
            
            if (query == null || query.trim().isEmpty()) {
                return new ToolStatusWrapper("Empty query provided. No search performed.", true);
            }
            
            if (query.length() > MAX_QUERY_LENGTH) {
                return new ToolStatusWrapper("Query exceeds maximum allowed length of " + MAX_QUERY_LENGTH + " characters.", false);
            }
            
            try {
                List<String> results = performGoogleSearch(query);
                
                String content = results.isEmpty()
                ? "No results found."
                : "Search results:\n" + String.join("\n", results);
                chatMemory.add("assistant", new AssistantMessage("{\"tool\":" + "\"" + getName() + "\",\"input\":" + input.getOriginalJson().toString() + "}"));
                chatMemory.add("user", new AssistantMessage("{\"tool\":" + "\"" + getName() + "\",\"input\":" + input.getOriginalJson().toString() + "}"));
                printIt();
                return new ToolStatusWrapper(content, true);
            } catch (Exception e) {
                return new ToolStatusWrapper("Search failed: " + e.getMessage(), false);
            }
        });
    }

    private List<String> performGoogleSearch(String query) throws IOException, InterruptedException {
        String apiKey = System.getenv("GOOGLE_API_KEY");
        String cx = System.getenv("GOOGLE_CSE_ID");

        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = String.format(
            "https://www.googleapis.com/customsearch/v1?key=%s&cx=%s&q=%s",
            apiKey, cx, encodedQuery
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Google Search API failed: " + response.statusCode());
        }

        JsonNode root = new ObjectMapper().readTree(response.body());
        List<String> results = new ArrayList<>();
        JsonNode items = root.path("items");

        if (items.isArray()) {
            for (JsonNode item : items) {
                String title = item.path("title").asText();
                String link = item.path("link").asText();
                results.add(title + " - " + link);
            }
        }

        return results;
    }

}
