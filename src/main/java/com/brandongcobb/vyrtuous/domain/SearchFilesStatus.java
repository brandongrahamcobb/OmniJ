/*  SearchFilesStatus.java The primary purpose of this class is to
 *  provide status information about the SearchFiles.java.
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
 package com.brandongcobb.vyrtuous.domain;

import java.util.ArrayList;
import java.util.List;

public class SearchFilesStatus implements ToolStatus{

    private final String message;
    private final List<Result> results;
    private final boolean success;

    public SearchFilesStatus(String message, List<Result> results, boolean success) {
        this.results = results != null ? results : new ArrayList<>();
        this.message = message;
        this.success = success;
    }

    /*
     *    Getters
     */
    @Override
    public String getMessage() {
        return message;
    }

    public List<Result> getResults() {
        return results;
    }

    @Override
    public boolean getSuccess() {
        return success;
    }
    
    public static class Result {
    
        public String path;
        public String snippet;
        
        public Result(String path, String snippet) {
            this.path = path;
            this.snippet = snippet;
        }
    }
}

