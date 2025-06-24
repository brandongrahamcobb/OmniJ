//
//  PatchOperation.java
//  
//
//  Created by Brandon Cobb on 6/24/25.
//
package com.brandongcobb.vyrtuous.domain;

public class PatchOperation {

    private String type;        // e.g. "replace", "insertAfter", "delete", etc.
    private String match;       // Required match string
    private String replacement; // Only for "replace"
    private String code;        // For insert/append

    public String getType() {
        return type;
    }

    public String getMatch() {
        return match;
    }

    public String getReplacement() {
        return replacement;
    }

    public String getCode() {
        return code;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setMatch(String match) {
        this.match = match;
    }

    public void setReplacement(String replacement) {
        this.replacement = replacement;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

