
package com.brandongcobb.vyrtuous.domain;

public class CreateFileStatus implements ToolStatus {

    private final boolean success;
    private final String message;

    public CreateFileStatus(boolean success, String message) {
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
