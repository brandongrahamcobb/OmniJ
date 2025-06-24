
package com.brandongcobb.vyrtuous.domain;

public class CreateFileStatus {

    private final boolean success;
    private final String message;

    public CreateFileStatus(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
