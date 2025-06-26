package com.brandongcobb.vyrtuous.domain;

import com.brandongcobb.vyrtuous.tools.*;
import com.brandongcobb.vyrtuous.objects.*;

public class SaveContextStatus implements ToolStatus {

    private final boolean success;
    private final String message;

    public SaveContextStatus(boolean success, String message) {
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

