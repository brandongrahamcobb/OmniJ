//
//  RefreshContextStatus.java
//  
//
//  Created by Brandon Cobb on 6/24/25.
//
package com.brandongcobb.vyrtuous.domain;

public class RefreshContextStatus implements ToolStatus {
    
    private final boolean success;
    private final String message;

    public RefreshContextStatus(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
