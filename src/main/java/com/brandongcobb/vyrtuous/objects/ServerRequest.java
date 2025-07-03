/*  ServerRequest.java The primary purpose of this class is to
 *  be an object representing a model request.
 *
 *  Copyright (C) 2025  github.com/brandongrahamcobb
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.brandongcobb.vyrtuous.objects;

import java.util.List;

public class ServerRequest {
    
    public String instructions;
    public String prompt;
    public String model;
    public boolean store;
    public boolean stream;
    public List<String> conversationHistory;
    public String previousResponseId;
    public String provider;
    public String requestType;
    public String endpoint;
    
    public ServerRequest (String instructions, String prompt, String model, boolean store, boolean stream, List<String> history, String endpoint, String previousResponseId, String provider, String requestType) {
        
        this.instructions = instructions;
        this.prompt = prompt;
        this.model = model;
        this.stream = stream;
        this.store = store;
        this.conversationHistory = history;
        this.previousResponseId = previousResponseId;
        this.provider = provider;
        this.requestType = requestType;
        this.endpoint = endpoint;
    }
}
