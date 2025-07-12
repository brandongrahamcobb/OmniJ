/*  PatchInput.java The primary purpose of this class is to
 *  provide input information about the Patch.java tool call.
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
package com.brandongcobb.omnij.domain.input;

import com.brandongcobb.omnij.domain.PatchOperation;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public class PatchInput implements ToolInput {
    
    private transient JsonNode originalJson;
    private List<PatchOperation> patches;
    private String targetFile;
    
    /*
     *  Getters
     */
    @Override
    public JsonNode getOriginalJson() {
        return originalJson;
    }
    
    public List<PatchOperation> getPatches() {
        return patches;
    }
    
    public String getTargetFile() {
        return targetFile;
    }
    
    /*
     *  Setters
     */
    @Override
    public void setOriginalJson(JsonNode originalJson) {
        this.originalJson = originalJson;
    }
}
