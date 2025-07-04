/*  DiffFilesInput.java The primary purpose of this class is to
 *  provide input information about the DiffFiles.java tool call.
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
package com.brandongcobb.vyrtuous.domain.input;

import com.fasterxml.jackson.databind.JsonNode;

public class DiffFilesInput implements ToolInput {

    private transient JsonNode originalJson;
    private String path1;
    private String path2;

    /*
     * Getters
     */
    public String getPath1() {
        return path1;
    }

    public String getPath2() {
        return path2;
    }

    @Override
    public JsonNode getOriginalJson() {
        return originalJson;
    }

    /*
     * Setters
     */
    public void setPath1(String path1) {
        this.path1 = path1;
    }

    public void setPath2(String path2) {
        this.path2 = path2;
    }

    @Override
    public void setOriginalJson(JsonNode originalJson) {
        this.originalJson = originalJson;
    }
}
