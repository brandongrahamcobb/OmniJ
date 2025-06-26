package com.brandongcobb.vyrtuous.domain;

import java.util.ArrayList;
import java.util.List;

public class SearchFilesStatus implements ToolStatus{

    private final boolean success;
    private final String message;
    private final List<Result> results;

    public SearchFilesStatus(boolean success, String message, List<Result> results) {
        this.success = success;
        this.message = message;
        this.results = results != null ? results : new ArrayList<>();
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public List<Result> getResults() {
        return results;
    }

    public static class Result {
        public String path;
        public String snippet; // may be null if grep wasn't used

        public Result(String path, String snippet) {
            this.path = path;
            this.snippet = snippet;
        }
    }
}

