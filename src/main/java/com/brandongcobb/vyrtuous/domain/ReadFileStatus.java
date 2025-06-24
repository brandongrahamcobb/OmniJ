
package com.brandongcobb.vyrtuous.domain;

public class ReadFileStatus {

    private final boolean success;
    private final String message;
    private final String content;

    public ReadFileStatus(boolean success, String message, String content) {
        this.success = success;
        this.message = message;
        this.content = content;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getContent() {
        return content;
    }
}
