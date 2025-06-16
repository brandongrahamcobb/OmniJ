//
//  ServerRequest.java
//  
//
//  Created by Brandon Cobb on 6/16/25.
//
package com.brandongcobb.vyrtuous.objects;

import java.util.ArrayList;
import java.util.List;

public class ServerRequest {
    
    public String instructions;
    public String prompt;
    public String model;
    public boolean store;
    public boolean stream;
    public List<String> conversationHistory;
    public long previousResponseId;
    public String source;
    public String requestType;
    public String endpoint;
    
    public ServerRequest (String instructions, String prompt, String model, boolean store, boolean stream, List<String> history, String endpoint, long previousResponseId, String source, String requestType) {
        this.instructions = instructions;
        this.prompt = prompt;
        this.model = model;
        this.stream = stream;
        this.store = store;
        this.conversationHistory = history;
        this.previousResponseId = previousResponseId;
        this.source = source;
        this.requestType = requestType;
        this.endpoint = endpoint;
    }
}
